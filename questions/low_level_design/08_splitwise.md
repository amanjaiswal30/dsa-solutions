# Low-Level Design: Splitwise (Expense Sharing Application)

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

**Scope note:** This design covers **friends, groups, expenses with multiple split strategies, balances, settlement simplification (minimize transactions), payments, multi-currency, recurring expenses, categories, activity feed, notifications, and exports**—with emphasis on **ledger correctness** and the **debt simplification algorithm**.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design an expense-sharing application like Splitwise where users can split bills with friends and groups, track who owes whom, settle up, and optionally simplify debts to reduce the number of payments."*

### Clarifying Questions to Ask:

1. **Q:** Is this **non-group** (friends only), **group-only**, or both?  
   **A:** Both—**friend-level** balances and **group-scoped** expenses; balances can be aggregated for dashboard or computed per group.

2. **Q:** Can one expense have **multiple payers** (e.g., two cards)?  
   **A:** Yes—model **payer shares** that sum to the expense total; each payer gets credited proportionally.

3. **Q:** Which **split types** are required?  
   **A:** **Equal**, **exact amounts**, **percentages**, and **shares** (ratio-based); all must validate against total amount.

4. **Q:** Should **simplification** change who actually owes whom, or only **suggest** settlement paths?  
   **A:** **Net balances** remain the same; simplification produces an **equivalent** set of fewer transactions (same net for every user).

5. **Q:** **Multi-currency**—convert at expense time or settlement time?  
   **A:** Store **amount + currency** per expense; persist **FX rate snapshot** at posting time for audit; optional **display currency** per user/group.

6. **Q:** **Payments**—in-app only or external (cash, bank)?  
   **A:** Record **SettlementPayment** (marks debts reduced); external rails abstracted behind **PaymentProvider**; idempotent processing.

7. **Q:** **Recurring expenses**—same participants every time?  
   **A:** **RecurringTemplate** + schedule; each instance creates a normal **Expense** (immutable history).

8. **Q:** **Notifications**—real-time or digest?  
   **A:** Both—**push/email** for "you owe" / "you are owed" / "expense added"; configurable **digest** window.

9. **Q:** **Activity feed**—per user, per group, global?  
   **A:** **GroupActivity** and **UserActivity** views; paginated, cursor-based.

10. **Q:** Consistency expectations for balances?  
    **A:** **Strong consistency** for ledger writes (single source of truth); **eventual** OK for feed and notification fan-out.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### User & Social Graph (FR1-FR8)
1. User can **register**, **authenticate**, and maintain a **profile** (name, email, default currency).
2. User can **invite** or **add friends** (bidirectional friendship after accept).
3. User can **create**, **rename**, **archive**, and **leave** groups.
4. User can **add/remove members** in a group (subject to permissions: admin vs member).
5. System supports **group roles** (e.g., ADMIN, MEMBER) for moderation and splits default.
6. User can **block** another user (no new shared expenses; historical data policy configurable).
7. **Search** users by email/handle for friend add and group invite.
8. **Soft-delete** user: anonymize PII; retain ledger for legal/audit with frozen account.

#### Expenses & Splits (FR9-FR20)
9. User can **create an expense** with **title**, **amount**, **currency**, **date**, **category**, optional **notes** and **receipt** attachment.
10. Expense records **who paid** (one or more payers with **paid amounts** summing to total).
11. Expense records **who owes** via a **split strategy**: **equal**, **exact**, **percent**, **shares**.
12. **Equal split:** amount divided across selected participants; system handles **remainder** to avoid rounding drift (assign to payer or first participant—**document rule**).
13. **Exact split:** per-participant owed amounts must **sum exactly** to total (within currency epsilon).
14. **Percent split:** percentages sum to **100%**; compute owed = round(total × pct/100) with **penny reconciliation** to total.
15. **Shares split:** integer or rational weights; owed proportional to **weight / sum(weights) × total** with reconciliation.
16. Expense may be **group-scoped** or **non-group (friend)** among a **participant set**.
17. **Edit/delete** expense: **versioned** or **compensating entries** in ledger (prefer **adjustment** events for audit).
18. **Itemization** (optional): line items with sub-splits—model as child **ExpenseLine** rows rolling into parent total.
19. **Tax/tip** modes (optional): add **fixed** or **proportional** modifiers to base—applied before split engine runs.
20. **Validation pipeline** rejects invalid splits before any ledger write.

#### Balances & Settlement (FR21-FR30)
21. System computes **net balance** per user **per group** and optionally **global across friends**.
22. System shows **who owes whom** as **pairwise balances** or **net positions** (product choice: often net per user + drill-down).
23. **Simplify debts** within a group: produce **minimum (or near-minimum) transaction count** that preserves **every user's net balance**.
24. User can **record settlement** (partial or full) between two users in a context (group or global).
25. **Settlement** reduces outstanding **canonical debts**; supports **multiple currencies** separately.
26. **Balance as of date** for statements (point-in-time from **event log** or **snapshot**).
27. **Dispute** or **comment** on expense (optional)—does not change ledger until resolution.
28. **Currency conversion** for display using **latest** or **user preference** rates; **stored rates** on expense for historical truth.
29. **Primary balance summary**: "You owe X / You are owed Y / Net" per scope.
30. **Idempotent expense create** via client **idempotency key** to avoid double post on retry.

#### Recurring, Categories, Feed, Notifications, Export (FR31-FR40)
31. **Recurring expense template** with cadence (weekly/monthly), next run date, and same split configuration.
32. **Scheduler** materializes **ExpenseInstance** from template (skip on failure with alert).
33. **Categories** (hierarchical optional): Food, Travel, Utilities—used for filtering and reports.
34. **Activity feed**: append-only **events** (expense added, edited, payment recorded, member joined).
35. **Notifications**: triggers when user **owes** above threshold, **is owed**, **added to expense**, **payment received**.
36. **Export reports**: CSV/PDF for date range, group, category—totals and per-user breakdown.
37. **Privacy**: group visibility rules; hide amounts from non-participants where applicable.
38. **Multi-device**: same account; **optimistic UI** with server reconciliation.
39. **Offline queue** (mobile): optional client queue with **sync** conflict policy (server wins on amount).
40. **Admin/moderation**: remove inappropriate content; audit log for compliance.

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many users, groups, expenses per second?"
- Millions of users; large groups (weddings, trips) with hundreds of members (edge case).
- Write-heavy on weekends (trips); read-heavy on dashboards.

**Deduced NFRs:**
- ✅ **Shard** by `userId` or `groupId` for ledger partitions.
- ✅ **CQRS-style** separation: command path for postings; read models for balances/feed.
- ✅ **Pagination** everywhere (feed, expenses, notifications).
- ✅ **Async workers** for notifications, exports, recurring materialization.

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"
- **Net balances** derived from ledger must match sum of postings.
- **Split totals** must equal expense total.
- **Settlement** cannot exceed outstanding debt in that currency/context.

**Deduced NFRs:**
- ✅ **Strong consistency** on ledger append (single writer per group or distributed transaction).
- ✅ **Atomic posting**: one expense → many **LedgerEntry** lines in one transaction.
- ✅ **Invariant checks**: Σ debits = Σ credits per expense event.
- ✅ **Optimistic locking** on expense edit with **version** field.

---

#### 3. **Availability Analysis**

**Think:** "Can users add expenses during outage?"
- Read **cached balances** may be stale briefly; writes should **queue** or **fail clearly**.

**Deduced NFRs:**
- ✅ **99.9%+** API availability; **degraded mode** (read-only) acceptable briefly.
- ✅ **Idempotency** and **retry-safe** settlement APIs.
- ✅ **DLQ** for failed notification/export jobs.

---

#### 4. **Maintainability Analysis**

**Think:** "New split type? New currency policy?"
- **Strategy pattern** for splits; **policy** classes for rounding.

**Deduced NFRs:**
- ✅ **Event sourcing** or **append-only ledger** for audit and replay.
- ✅ **Feature flags** for simplification algorithm variants (greedy vs LP for research).
- ✅ **Structured logging** with `expenseId`, `groupId`, `correlationId`.

---

#### 5. **Performance Analysis**

**Think:** "Simplification on 50 people?"
- Greedy **O(n log n)** sort + **O(n)** sweep; balance computation **O(E)** expenses or **O(1)** with maintained aggregates.

**Deduced NFRs:**
- ✅ **Precomputed balance snapshots** updated on each posting (amortized O(1) read).
- ✅ **Simplification** runs on **group member set** (typically n < 50); acceptable on demand.
- ✅ **Indexes** on `(groupId, createdAt)`, `(userId, groupId)` for feeds and dashboards.

---

#### 6. **Security Analysis**

**Think:** "Who can modify an expense?"
- Only **participants** or **group admins**; **ACL** on every API.

**Deduced NFRs:**
- ✅ **AuthN/AuthZ** (OAuth2/OIDC); **resource-level** checks.
- ✅ **PII encryption** at rest; **rate limiting** on invites and exports.
- ✅ **Audit trail** for deletes and admin actions.

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|---------------|-------|
| "User, friends, groups" | User, Friendship, Group, GroupMembership, Role |
| "Expense, who paid, who owes" | Expense, PayerShare, Split, Participant |
| "Equal, exact, percent, shares" | SplitStrategy, SplitLine |
| "Balances, simplify" | BalanceSnapshot, DebtSimplifier, SettlementPayment |
| "Currency, FX" | Money, Currency, ExchangeRate |
| "Recurring" | RecurringTemplate, Schedule |
| "Category" | ExpenseCategory |
| "Activity, notification" | ActivityEvent, Notification |
| "Export" | ReportJob, ExportFormat |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| User | ✅ YES | Core aggregate root for identity |
| Friendship | ✅ YES | Relationship with lifecycle |
| Group | ✅ YES | Container for shared expenses |
| GroupMembership | ✅ YES | User–group association + role |
| Expense | ✅ YES | Core transactional record |
| PayerShare | ✅ YES | How much each payer covered |
| SplitLine | ✅ YES | Owed amount per participant for one expense |
| SplitStrategy | ✅ YES | Enum + strategy implementation |
| Money | ✅ VALUE OBJECT | Amount + currency |
| LedgerEntry | ✅ YES | Append-only accounting line |
| BalanceSnapshot | ✅ YES | Materialized view per (user, group, currency) |
| SettlementPayment | ✅ YES | Records pay-down between two users |
| DebtSimplifier | ✅ SERVICE | Pure algorithm, stateless |
| ExchangeRate | ✅ YES | Rate table or quote at time |
| RecurringTemplate | ✅ YES | Pattern for generated expenses |
| ExpenseCategory | ✅ YES | Lookup / taxonomy |
| ActivityEvent | ✅ YES | Feed item |
| Notification | ✅ YES | Outbox to channels |
| ReportJob | ✅ YES | Async export task |

### Final Entity List

**Identity & Social:**
1. **User** — Account, profile, default currency
2. **Friendship** — Two users, status (PENDING, ACTIVE, BLOCKED)
3. **Group** — Metadata, default currency, settings
4. **GroupMembership** — userId, groupId, role, joinedAt

**Money & FX:**
5. **Money** — `BigDecimal amount`, `Currency currency`
6. **ExchangeRate** — base, quote, rate, effectiveAt, source

**Expense Domain:**
7. **Expense** — Group/friends context, total Money, metadata, payer lines, split lines, categoryId, createdBy
8. **PayerShare** — userId, paid: Money
9. **SplitLine** — userId, owed: Money (or computed reference for %/shares)
10. **ExpenseCategory** — id, name, parentId optional
11. **RecurringTemplate** — cron/rrule, template expense payload, nextRunAt, ownerId

**Ledger & Settlement:**
12. **LedgerEntry** — type (EXPENSE_POST, ADJUSTMENT, SETTLEMENT, REVERSAL), references, debit/credit legs
13. **BalanceSnapshot** — userId, groupId (nullable for global), currency, netAmount
14. **SettlementPayment** — fromUser, toUser, amount, currency, groupId?, externalRef, status

**Cross-Cutting:**
15. **ActivityEvent** — type, actorId, payload, groupId?, createdAt
16. **Notification** — userId, channel, template, readAt
17. **ReportJob** — userId, filters, format, artifactUrl, status

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Core Relationships

#### User ↔ Friendship
**Conclusion:** **Association** (many-to-many via Friendship edge)
```
User ────── Friendship ────── User  (two endpoints userA, userB)
```

#### User ↔ Group
**Conclusion:** **Many-to-many** via GroupMembership
```
User ────── GroupMembership ────── Group
```

#### Group ↔ Expense
**Conclusion:** **Composition** (expenses belong to a context)
```
Group ◆────→ Expense [0..*]   OR   Expense with groupId nullable (friends-only)
```

---

### Pass 2: Expense & Ledger

#### Expense ↔ PayerShare / SplitLine
**Conclusion:** **Composition**
```
Expense ◆────→ PayerShare [1..*]
Expense ◆────→ SplitLine [1..*]
```

#### Expense → LedgerEntry
**Conclusion:** **Association** (1 expense → N ledger lines)
```
Expense ─────→ LedgerEntry [1..*]
```

#### SettlementPayment → Users
**Conclusion:** **Association**
```
SettlementPayment ─────→ User (fromUser)
SettlementPayment ─────→ User (toUser)
```

---

### Pass 3: Read Models & Services

#### BalanceSnapshot → User, Group
**Conclusion:** **Derived** (not ownership)
```
BalanceSnapshot ─────→ User
BalanceSnapshot ─────→ Group (optional)
```

#### DebtSimplifier
**Conclusion:** **Service** — reads net balances, outputs suggested `SettlementEdge[]`

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| User ↔ User (Friendship) | M:N | Association |
| User ↔ Group | M:N | GroupMembership |
| Group → Expense | 1:N | Composition |
| Expense → PayerShare | 1:N | Composition |
| Expense → SplitLine | 1:N | Composition |
| Expense → LedgerEntry | 1:N | Association |
| User → BalanceSnapshot | 1:N | Derived |
| User → SettlementPayment | 1:N (as from/to) | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>> │
│ FriendshipStatus│  │  GroupRole       │  │ SplitStrategyType│
├─────────────────┤  ├──────────────────┤  ├─────────────────┤
│ PENDING         │  │ ADMIN            │  │ EQUAL           │
│ ACTIVE          │  │ MEMBER           │  │ EXACT           │
│ BLOCKED         │  └──────────────────┘  │ PERCENT         │
└─────────────────┘                        │ SHARES          │
                                           └─────────────────┘

┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>> │
│ LedgerEntryType │  │ ActivityType     │  │ SettlementStatus│
├─────────────────┤  ├──────────────────┤  ├─────────────────┤
│ EXPENSE_POST    │  │ EXPENSE_CREATED  │  │ PENDING         │
│ ADJUSTMENT      │  │ EXPENSE_UPDATED  │  │ COMPLETED       │
│ SETTLEMENT      │  │ PAYMENT_RECORDED│  │ FAILED          │
│ REVERSAL        │  │ MEMBER_JOINED    │  └─────────────────┘
└─────────────────┘  └──────────────────┘
```

---

### Class Diagram 2: Money, Expense, Splits

```
┌───────────────────────────────────────────────────────────┐
│                      Money (Value Object)                 │
├───────────────────────────────────────────────────────────┤
│ - amount: BigDecimal                                      │
│ - currency: Currency                                      │
├───────────────────────────────────────────────────────────┤
│ + add(m: Money): Money                                    │
│ + subtract(m: Money): Money                               │
│ + negate(): Money                                         │
│ + isSameCurrency(m: Money): boolean                       │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                      Expense                              │
├───────────────────────────────────────────────────────────┤
│ - expenseId: String                                       │
│ - groupId: String (optional)                              │
│ - title: String                                           │
│ - total: Money                                            │
│ - expenseDate: Instant                                    │
│ - categoryId: String                                      │
│ - createdBy: String                                       │
│ - splitStrategy: SplitStrategyType                        │
│ - version: int                                            │
│ - payerShares: List<PayerShare>        ◆────────────────┐ │
│ - splitLines: List<SplitLine>          ◆────────────────┼─┐
├───────────────────────────────────────────────────────────┤ │
│ + validate(): ValidationResult                            │ │
└───────────────────────────────────────────────────────────┘ │
         │                              │                     │
         ▼                              ▼                     ▼
┌─────────────────┐          ┌─────────────────────────────┐
│   PayerShare    │          │        SplitLine              │
├─────────────────┤          ├─────────────────────────────┤
│ - userId        │          │ - userId                      │
│ - paid: Money   │          │ - owed: Money (or raw fields) │
└─────────────────┘          │ - percent? / shares?          │
                             └─────────────────────────────┘
```

---

### Class Diagram 3: Split Strategy (Strategy Pattern)

```
┌───────────────────────────────────────────────────────────┐
│          <<interface>> SplitCalculator                    │
├───────────────────────────────────────────────────────────┤
│ + computeLines(total: Money, participants: List<UserRef>, │
│   params: SplitParams): List<SplitLine>                   │
└───────────────────────────────────────────────────────────┘
                    △
        ┌───────────┼───────────┬─────────────────┐
        │           │           │                 │
        ▼           ▼           ▼                 ▼
┌─────────────┐ ┌──────────┐ ┌────────────┐ ┌────────────┐
│EqualSplit   │ │ExactSplit│ │PercentSplit│ │SharesSplit │
│Calculator   │ │Calculator│ │Calculator  │ │Calculator  │
└─────────────┘ └──────────┘ └────────────┘ └────────────┘
```

---

### Class Diagram 4: Ledger & Balance

```
┌───────────────────────────────────────────────────────────┐
│                   LedgerEntry                             │
├───────────────────────────────────────────────────────────┤
│ - entryId: String                                         │
│ - type: LedgerEntryType                                   │
│ - groupId: String?                                        │
│ - expenseId: String?                                      │
│ - userId: String                                          │
│ - delta: Money   // net effect on "owed" convention       │
│ - seq: long                                               │
│ - createdAt: Instant                                      │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                 BalanceSnapshot                           │
├───────────────────────────────────────────────────────────┤
│ - userId: String                                          │
│ - groupId: String?                                        │
│ - currency: Currency                                    │
│ - netOwed: Money   // negative => user is owed overall    │
│ - updatedAt: Instant                                      │
└───────────────────────────────────────────────────────────┘

        Convention (example): positive netOwed means user owes
        the group more than they are owed; pick one convention
        and apply consistently in APIs and UI copy.
```

---

### Class Diagram 5: Services (Application Layer)

```
┌───────────────────────────────────────────────────────────┐
│                ExpenseService                             │
├───────────────────────────────────────────────────────────┤
│ + createExpense(cmd): Expense                             │
│ + updateExpense(cmd): Expense                             │
│ + deleteExpense(expenseId): void                          │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                BalanceService                             │
├───────────────────────────────────────────────────────────┤
│ + getNetBalances(groupId): Map<userId, Money>             │
│ + getPairwiseDebts(groupId): List<DebtEdge>               │
│ + recomputeSnapshot(groupId, currency): void              │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│              DebtSimplificationService                    │
├───────────────────────────────────────────────────────────┤
│ + simplify(groupId, currency): List<SimplifiedPayment>    │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│              SettlementService                            │
├───────────────────────────────────────────────────────────┤
│ + recordSettlement(cmd): SettlementPayment                │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│              FxService                                    │
├───────────────────────────────────────────────────────────┤
│ + convert(m: Money, to: Currency, at: Instant): Money     │
│ + rateAt(pair, at): BigDecimal                            │
└───────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Balance convention

Use a single convention everywhere:

- For each **expense**, each **non-payer participant** **owes** their split to the **payers pool** (or allocate credit to payers by **paid ratio**).
- **Net balance** `B[u]` in a group and currency = **sum of (credits − debits)** from ledger for user `u`.

**Posting model (simplified “splitwise-style”):**

For each expense, after splits are known:

1. For each participant `u` with owed amount `o_u` (Money, same currency as expense):
   - Post **DEBIT** `o_u` to `u` (increases what they owe).
2. For each payer `p` with paid amount `paid_p`:
   - Post **CREDIT** `paid_p` to `p` (reduces net owed by what they advanced).

**Net:** User who paid more than their share ends up **owed**; user who paid less **owes**.

```java
// LedgerPostingEngine.java — conceptual posting for one expense
public void postExpense(Expense expense) {
    String groupId = expense.getGroupId();
    Currency ccy = expense.getTotal().getCurrency();

    for (SplitLine line : expense.getSplitLines()) {
        Money owed = line.getOwed();
        ledger.append(LedgerEntry.debit(groupId, expense.getId(), line.getUserId(), owed));
    }
    for (PayerShare ps : expense.getPayerShares()) {
        Money paid = ps.getPaid();
        ledger.append(LedgerEntry.credit(groupId, expense.getId(), ps.getUserId(), paid));
    }

    balanceService.applyDeltas(groupId, ccy, /* from entries */);
    activity.emit(ActivityType.EXPENSE_CREATED, expense);
}
```

---

### Split calculators (with rounding reconciliation)

```java
// SplitCalculator.java
public interface SplitCalculator {
    List<SplitLine> compute(Money total, List<String> participantIds, SplitParams params);
}

// EqualSplitCalculator.java
public class EqualSplitCalculator implements SplitCalculator {
    @Override
    public List<SplitLine> compute(Money total, List<String> ids, SplitParams params) {
        int n = ids.size();
        if (n == 0) throw new IllegalArgumentException("participants required");

        BigDecimal[] raw = total.getAmount().divideAndRemainder(BigDecimal.valueOf(n));
        BigDecimal base = raw[0];
        BigDecimal remainderCents = raw[1]; // currency-aware in production

        List<SplitLine> lines = new ArrayList<>();
        // Assign extra smallest units to first k participants (documented policy)
        for (int i = 0; i < n; i++) {
            BigDecimal amt = base;
            if (i < remainderCents.intValue()) {
                amt = amt.add(BigDecimal.ONE); // for minor units = 1 cent
            }
            lines.add(new SplitLine(ids.get(i), new Money(amt, total.getCurrency())));
        }
        reconcileToTotal(lines, total);
        return lines;
    }

    private void reconcileToTotal(List<SplitLine> lines, Money total) {
        Money sum = Money.zero(total.getCurrency());
        for (SplitLine sl : lines) sum = sum.add(sl.getOwed());
        Money diff = total.subtract(sum);
        if (!diff.isZero()) {
            // adjust first line by diff (policy)
            SplitLine first = lines.get(0);
            first.setOwed(first.getOwed().add(diff));
        }
    }
}
```

```java
// PercentSplitCalculator.java
public class PercentSplitCalculator implements SplitCalculator {
    @Override
    public List<SplitLine> compute(Money total, List<String> ids, SplitParams params) {
        Map<String, BigDecimal> pct = params.getPercents();
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal sumPct = pct.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumPct.compareTo(hundred) != 0) {
            throw new IllegalArgumentException("percents must sum to 100");
        }

        List<SplitLine> lines = new ArrayList<>();
        Money running = Money.zero(total.getCurrency());

        List<String> ordered = new ArrayList<>(ids);
        for (int i = 0; i < ordered.size(); i++) {
            String uid = ordered.get(i);
            boolean last = (i == ordered.size() - 1);
            Money share;
            if (last) {
                share = total.subtract(running); // absorb rounding
            } else {
                BigDecimal p = pct.get(uid);
                share = total.multiply(p).divide(hundred);
                running = running.add(share);
            }
            lines.add(new SplitLine(uid, share));
        }
        return lines;
    }
}
```

```java
// SharesSplitCalculator.java — weights w_i, owed_i = total * w_i / sum(w)
public class SharesSplitCalculator implements SplitCalculator {
    @Override
    public List<SplitLine> compute(Money total, List<String> ids, SplitParams params) {
        Map<String, Integer> shares = params.getShares();
        int sum = shares.values().stream().mapToInt(Integer::intValue).sum();
        if (sum <= 0) throw new IllegalArgumentException("invalid shares");

        List<SplitLine> lines = new ArrayList<>();
        Money running = Money.zero(total.getCurrency());
        for (int i = 0; i < ids.size(); i++) {
            String uid = ids.get(i);
            boolean last = (i == ids.size() - 1);
            Money part;
            if (last) {
                part = total.subtract(running);
            } else {
                int w = shares.get(uid);
                part = total.multiply(new BigDecimal(w))
                           .divide(new BigDecimal(sum), RoundingMode.HALF_UP);
                running = running.add(part);
            }
            lines.add(new SplitLine(uid, part));
        }
        return lines;
    }
}
```

---

### Net balance from ledger

```java
// BalanceService.java
public Map<String, Money> getNetBalances(String groupId, Currency ccy) {
    // net[user] = sum(credits) - sum(debits)  OR opposite — match UI
    Map<String, Money> net = new HashMap<>();
    for (LedgerEntry e : ledger.findByGroupAndCurrency(groupId, ccy)) {
        net.merge(e.getUserId(), e.getSignedDelta(), Money::add);
    }
    return net;
}
```

**Pairwise expansion (for display only):**  
Full pairwise is **not unique** from net alone; typical product shows **simplified** edges or **per-friend** aggregates computed from **non-group** + **group** ledgers separately.

---

### Debt simplification — minimize number of transactions

**Problem:** Given net balances `B[1..n]` for users in a group for a fixed currency, with `Σ B[i] = 0` (under debit/credit convention), find a multiset of directed payments `(debtor → creditor, amount)` such that after settling all payments, all balances become **zero**, and the **number of payments is minimized**.

**Key facts (interview gold):**

1. Treat users with **non-zero net** as nodes; we only need flows between **debtors** (negative net under “owes” convention) and **creditors** (positive net).
2. A classic **greedy matching** between **largest remaining debtor** and **largest remaining creditor** works:
   - Sort or use two pointers on **sorted** lists by absolute amount.
   - Pay `min(|debt|, credit)`; reduce both; count one transaction.
3. This yields **at most (k − 1)** payments where `k` is the number of users with **non-zero** net (and **at most n − 1** overall).
4. **Optimality:** This greedy minimizes the **number of transactions** (not necessarily minimizing total cash moved, which is the same total anyway for zero-sum).

**Proof sketch:** Each payment can eliminate **at least one** participant from the non-zero set (when it exactly clears one side), and you need **at least k−1** edges to zero a tree on **k** non-zero nodes in a flow decomposition; greedy achieves **k−1**.

```java
// SimplifiedPayment.java
public class SimplifiedPayment {
    private final String fromUserId;
    private final String toUserId;
    private final Money amount;

    public SimplifiedPayment(String fromUserId, String toUserId, Money amount) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
    }

    public String getFromUserId() { return fromUserId; }
    public String getToUserId() { return toUserId; }
    public Money getAmount() { return amount; }
}

// DebtSimplificationService.java
public class DebtSimplificationService {

    /**
     * Convention: positive net => user is owed that amount (creditor).
     * Negative net => user owes |net| (debtor).
     */
    public List<SimplifiedPayment> simplify(Map<String, Money> netBalances) {
        // Filter zeros
        List<NameAmount> debtors = new ArrayList<>();
        List<NameAmount> creditors = new ArrayList<>();

        for (Map.Entry<String, Money> e : netBalances.entrySet()) {
            Money m = e.getValue();
            if (m.isZero()) continue;
            if (m.isNegative()) {
                debtors.add(new NameAmount(e.getKey(), m.negate()));
            } else {
                creditors.add(new NameAmount(e.getKey(), m));
            }
        }

        debtors.sort(Comparator.comparing(NameAmount::getAmount).reversed());
        creditors.sort(Comparator.comparing(NameAmount::getAmount).reversed());

        int i = 0, j = 0;
        List<SimplifiedPayment> result = new ArrayList<>();

        while (i < debtors.size() && j < creditors.size()) {
            NameAmount d = debtors.get(i);
            NameAmount c = creditors.get(j);
            Money pay = d.amount.min(c.amount);
            result.add(new SimplifiedPayment(d.getUserId(), c.getUserId(), pay));

            d.amount = d.amount.subtract(pay);
            c.amount = c.amount.subtract(pay);

            if (d.amount.isZero()) i++;
            if (c.amount.isZero()) j++;
        }
        return result;
    }

    private static class NameAmount {
        private final String userId;
        private Money amount;

        NameAmount(String userId, Money amount) {
            this.userId = userId;
            this.amount = amount;
        }

        String getUserId() { return userId; }
        Money getAmount() { return amount; }
    }
}
```

**Multi-currency:** Run simplification **per currency** independently; never merge currencies in one edge without FX policy.

---

### Settlement (settle up)

```java
// SettlementService.java
public SettlementPayment recordSettlement(SettlementCommand cmd) {
    // Validate: cmd.amount <= outstanding debt from -> to in (group, currency) per policy
    validateOutstanding(cmd);

    SettlementPayment sp = repository.save(new SettlementPayment(cmd));
    ledger.append(LedgerEntry.settlementDebit(cmd.getGroupId(), cmd.getFrom(), cmd.getMoney()));
    ledger.append(LedgerEntry.settlementCredit(cmd.getGroupId(), cmd.getTo(), cmd.getMoney()));
    balanceService.applySettlement(cmd.getGroupId(), cmd.getMoney().getCurrency(), sp);
    notifications.send(cmd.getTo(), "You received a payment");
    return sp;
}
```

---

### Multi-currency

```java
// FxService.java
public class FxService {
    public Money convert(Money m, Currency to, Instant at) {
        if (m.getCurrency().equals(to)) return m;
        BigDecimal rate = rateProvider.getRate(m.getCurrency(), to, at);
        return new Money(m.getAmount().multiply(rate), to);
    }
}

// Expense stores snapshot for audit
public class Expense {
    private Money total;
    private Map<Currency, BigDecimal> fxRatesAtCreation; // optional: display conversions
}
```

---

### Recurring expenses

```java
// RecurringTemplate.java
public class RecurringTemplate {
    private String templateId;
    private String ownerId;
    private String groupId;
    private String rrule; // e.g. "FREQ=MONTHLY;BYMONTHDAY=1"
    private ExpenseBlueprint blueprint; // title, category, split config, payers
    private Instant nextRunAt;
}

// RecurringSchedulerJob.java
public void tick() {
    List<RecurringTemplate> due = repo.findDue(Instant.now());
    for (RecurringTemplate t : due) {
        try {
            expenseService.createExpense(t.materializeCommand());
            t.advanceNextRun();
            repo.save(t);
        } catch (Exception e) {
            alertOwner(t.getOwnerId(), e);
        }
    }
}
```

---

### Activity feed & notifications

```java
// ActivityEvent.java
public class ActivityEvent {
    private String id;
    private ActivityType type;
    private String groupId;
    private String actorUserId;
    private JsonNode payload;
    private Instant createdAt;
}

// NotificationService.java
public void onBalanceThresholdCrossed(String userId, Money youOwe, Money youAreOwed) {
    if (youOwe.greaterThan(policy.getOweAlertThreshold())) {
        push.send(userId, "You owe " + youOwe);
    }
}
```

---

### Export reports

```java
// ReportJob.java
public class ReportJob {
    public void run(ExportQuery q) {
        List<Expense> rows = expenseRepo.query(q);
        CsvWriter w = new CsvWriter();
        for (Expense e : rows) {
            w.row(e.getExpenseDate(), e.getTitle(), e.getTotal(), e.getCategoryId(), e.getCreatedBy());
        }
        storage.upload(q.getUserId(), w.toBytes());
    }
}
```

---

### Demo (simplification + balances)

```java
// SplitwiseSimplifyDemo.java
public class SplitwiseSimplifyDemo {
    public static void main(String[] args) {
        // Net: A owed 60 (creditor), B owes 40, C owes 20 => sum 0
        Map<String, Money> net = new HashMap<>();
        net.put("A", money(60));   // creditor +60
        net.put("B", money(-40));  // debtor
        net.put("C", money(-20));  // debtor

        DebtSimplificationService svc = new DebtSimplificationService();
        List<SimplifiedPayment> pays = svc.simplify(net);

        // Expected (one optimal set): B -> A 40, C -> A 20  (2 payments)
        pays.forEach(p ->
            System.out.println(p.getFromUserId() + " -> " + p.getToUserId() + " : " + p.getAmount())
        );
    }

    private static Money money(long amt) {
        return new Money(BigDecimal.valueOf(amt), Currency.getInstance("USD"));
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `SplitCalculator` implementations (equal, exact, percent, shares)  
**Why:** Add split types without changing `ExpenseService`  
**Interview Justification:** "Split rules are business rules that evolve; strategy keeps validation and computation cohesive per type."

---

### Pattern 2: Template Method (optional)
**Where:** `AbstractSplitCalculator` with hooks: `preValidate`, `computeRaw`, `reconcile`  
**Why:** Shared rounding/reconciliation across strategies  
**Interview Justification:** "Percent and shares both need 'last participant absorbs remainder'—factor common flow."

---

### Pattern 3: Saga / Outbox (distributed)
**Where:** Expense create + ledger + feed + notifications  
**Why:** Atomic core + reliable async side effects  
**Interview Justification:** "Ledger must commit once; notifications can retry via outbox."

---

### Pattern 4: Event Sourcing (optional deep dive)
**Where:** `LedgerEntry` as source of truth; snapshots for read  
**Why:** Audit, replay, time-travel balances  
**Interview Justification:** "Financial apps benefit from append-only history."

---

### Pattern 5: Value Object
**Where:** `Money`  
**Why:** Prevent mixed-currency adds; centralize rounding  
**Interview Justification:** "Currency bugs are expensive; make illegal states unrepresentable."

---

## 💡 Step 8: Interview Discussion Points

### 1. Why not store pairwise balances only?

**Answer:**  
Pairwise debts **explode** (O(n²)) and are **redundant**: many pairwise patterns yield the **same net**. Ledger postings from **expenses** naturally compress into **net per user**. Pairwise UI can be **derived** via simplification or shown only for **direct settlements**.

---

### 2. Does simplification change who is "responsible" morally?

**Answer:**  
**No**—it's a **payment routing** suggestion. Legally/socially, users still participated in the same expenses. The **net** is unchanged; only **how many transfers** to zero nets is optimized.

---

### 3. Greedy simplification — edge cases?

**Answer:**  
- **Zeros:** exclude users with settled net.  
- **Floating money:** operate in **integer minor units** (cents) to avoid drift.  
- **Multi-currency:** run **per currency**; cross-currency settlement requires explicit **FX leg** (two entries).

---

### 4. Minimum transactions vs minimum total cash volume?

**Answer:**  
For **zero-sum** nets, **total volume** paid equals sum of positive nets (fixed). The greedy minimizes **number of edges** (transactions), not volume—volume is essentially constrained by conservation.

---

### 5. Conflict: edit expense after someone settled

**Answer:**  
Prefer **adjustment ledger entries** or **new correcting expense** rather than silent mutation. If settlement already happened, **reopen** requires **refund** or **counter-payment** workflow.

---

### 6. How to test split rounding?

**Answer:**  
Property tests:  
- Σ split lines = total (in minor units).  
- Percents sum to 100 rejected otherwise.  
- Random partitions + exact sum invariant.

---

### 7. Scale: 10k expenses in one group?

**Answer:**  
Don't re-scan all expenses for each read—maintain **running BalanceSnapshot** updated transactionally with each posting; **rebuild job** for repair.

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `ExpenseService`: expense lifecycle only  
- `BalanceService`: aggregates only  
- `DebtSimplificationService`: graph/settlement suggestion only  

### Open/Closed ✅
```java
public class CustomSplitCalculator implements SplitCalculator { }
// Register in factory without modifying ExpenseService
```

### Liskov Substitution ✅
All `SplitCalculator` implementations honor `compute` contract: totals match, participants preserved.

### Interface Segregation ✅
```java
interface ExpenseReader { Expense get(String id); }
interface ExpenseWriter { Expense create(CreateExpenseCommand c); }
```

### Dependency Inversion ✅
```java
public class ExpenseService {
    private final SplitCalculatorFactory splitFactory; // abstraction
    private final LedgerRepository ledger;             // abstraction
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Append-only ledger** (or equivalent postings) as source of truth for balances  
- ✅ **Split strategies** isolated; **rounding reconciliation** on one participant or explicit **adjustment line**  
- ✅ **Per-currency** simplification; **FX** explicit and **snapshotted** on expense when needed  

### Algorithms
- ✅ **Net balance:** linear in number of postings (or O(1) with snapshots)  
- ✅ **Debt simplification:** greedy **debtor/creditor** matching → **≤ n−1** payments, **minimum transaction count**  
- ✅ **Validation:** split sums, payer sums, currency uniformity per expense  

### Product Features
- ✅ Groups, friends, recurring, categories, activity, notifications, exports  
- ✅ **Settlement** as first-class **ledger event** with **idempotency**  

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
