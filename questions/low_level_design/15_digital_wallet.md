# Low-Level Design: Digital Wallet

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

**Scope note:** This design focuses on **digital wallet balances, P2P, merchant checkout, bank rails, settlement, and risk**—not physical cash dispensing or card-present ATM authorization (which is a separate ATM System LLD).

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a digital wallet service (similar to PayPal, Venmo, or Google Pay) that lets users hold money, pay friends and merchants, link bank accounts, and manage payments at scale."*

### Clarifying Questions to Ask:

1. **Q:** Are we designing a closed-loop wallet (balance only) or also card/network rails?  
   **A:** Wallet balance + bank ACH/card funding + outbound settlement; abstract card networks behind a `PaymentRail` interface.

2. **Q:** Single currency or multi-currency?  
   **A:** Multi-currency with per-wallet `Money` balances and FX at defined conversion points.

3. **Q:** What consistency level for money movement?  
   **A:** Strong consistency for ledger postings; eventual consistency OK for notifications and some analytics.

4. **Q:** P2P: instant or batched?  
   **A:** Instant internal transfers between wallet users; external bank movements may be async with status tracking.

5. **Q:** Merchant payments: which channels?  
   **A:** QR (static/dynamic), NFC/tokenized tap—model as `PaymentIntent` + `PaymentMethod`.

6. **Q:** Fraud and compliance?  
   **A:** 2FA for sensitive actions, velocity limits, device risk scoring; disputes trigger escrow holds.

7. **Q:** Split bills and request money—same ledger as P2P?  
   **A:** Yes; split creates multiple `LedgerEntry` lines from one orchestrated payment.

8. **Q:** Recurring payments?  
   **A:** `RecurringSchedule` + mandate + retry/dunning policy.

9. **Q:** Rewards/cashback?  
   **A:** Separate `RewardsAccount` credited asynchronously from rules engine; does not bypass ledger.

10. **Q:** Settlement vs user-facing balance?  
    **A:** User sees available balance after holds; settlement workers reconcile with banks/acquirers.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Wallet & Accounts (FR1-FR8)
1. Each user may own one or more **wallet accounts** identified by wallet ID.
2. Each wallet holds **balances per currency** (e.g., USD, EUR) with configurable **precision**.
3. System enforces **per-transaction, daily, and monthly limits** (velocity and amount caps).
4. Users can **freeze** a wallet or **block** specific counterparties.
5. **Available balance** = ledger balance − active **holds** (escrow, pending debit).
6. Support **primary currency** per user for display and default charges.
7. **KYC tier** gates limits and features (e.g., higher limits after verification).
8. **Audit trail** for all balance-affecting events.

#### P2P Transfers (FR9-FR12)
9. User can **send money** to another user by wallet ID, phone, email, or handle.
10. User can **receive** money; incoming transfers update recipient ledger atomically with sender debit.
11. Optional **note/memo** and **privacy** settings on P2P payments.
12. **Insufficient funds** and **limit exceeded** errors are explicit and idempotent-safe.

#### Merchant Payments (FR13-FR16)
13. **QR payments:** static merchant QR (amount entered by payer) and dynamic QR (amount embedded, time-bound).
14. **NFC / tokenized** payments: bind device token to wallet; charge via `PaymentIntent`.
15. Merchant receives **settlement record**; optional **instant** vs **T+N settlement** configuration.
16. **Refunds** and **partial refunds** reverse or adjust ledger with reference to original transaction.

#### Bank Linking (FR17-FR20)
17. User can **add** external bank account or card (tokenized).
18. **Verification** via micro-deposits, instant bank auth, or 3DS where applicable.
19. **Default funding source** and **withdrawal account** per user.
20. **Unlink** and **re-verify** flows with status machine (`PENDING`, `VERIFIED`, `FAILED`).

#### History & Reporting (FR21-FR23)
21. **Transaction history** with filters: type, date range, counterparty, currency, status.
22. Export (CSV/PDF) and **search** by transaction ID or merchant order ID.
23. **Running balance** view per wallet (point-in-time reconstruction from ledger).

#### Split Bills & Requests (FR24-FR28)
24. **Split bill:** initiator defines total, participants and shares; system collects or requests from each.
25. **Request money:** generate deep link / QR / in-app request with optional expiry.
26. **Reminders** for unpaid split or money requests (notification channel).
27. **Decline** or **expire** requests without posting ledger until acceptance.
28. Split settlement can be **sequential** (one pays after another) or **parallel** (all authorize together).

#### Recurring & Subscriptions (FR29-FR32)
29. **Recurring payment** schedules: interval (daily/weekly/monthly), start/end, amount or variable bill.
30. **Mandate** capture (user consent) and **retry policy** on failure.
31. **Pause**, **cancel**, and **amount change** with audit.
32. Integration with **merchant subscriptions** via `SubscriptionContract` ID.

#### Rewards & Cashback (FR33-FR36)
33. **Earn** rewards on qualifying spend (category, merchant, promo windows).
34. **Redeem** rewards toward purchases or **cash out** to wallet per policy.
35. **Points ledger** separate from cash ledger but **linked** to originating `TransactionId`.
36. **Promo codes** and **campaign caps** (per user, global budget).

#### Multi-Currency (FR37-FR40)
37. Wallets may hold **multiple currency buckets**; transfers specify debit/credit currency or use conversion.
38. **FX rate** sourced from rate provider with **timestamp** and **spread** policy.
39. **Cross-currency P2P** debits sender in one currency, credits receiver in another using locked rate.
40. **Rounding** rules documented per currency (minor units).

#### Security & Fraud (FR41-FR45)
41. **2FA** (TOTP, push, SMS fallback) for high-risk actions: large transfer, new device, new payee.
42. **Step-up auth** mid-flow for merchant payments above threshold.
43. **Fraud scoring** pipeline: device, geo, velocity, behavioral; outcomes `ALLOW`, `CHALLENGE`, `BLOCK`.
44. **Session** and **device binding**; revoke all sessions on password change.
45. **Encryption** at rest for PII; **TLS** for all APIs; **tokenization** for PAN/bank numbers.

#### Settlement & Reconciliation (FR46-FR49)
46. **Settlement batches** group cleared wallet movements for bank/acquirer submission.
47. **Reconciliation** matches internal ledger totals with external statements; flag discrepancies.
48. **Cut-off times** and **holidays** affect ACH/card settlement timelines.
49. **Multi-currency settlement** with separate nostro/vostro or partner accounts per currency.

#### Escrow & Disputes (FR50-FR53)
50. **Escrow hold** places funds in `HELD` state until release conditions (delivery, timeout, manual).
51. **Disputed transactions** freeze disputed amount; **chargeback** workflow updates ledger when final.
52. **Partial release** from escrow (e.g., milestone payments).
53. **Appeal** and **evidence** attachments stored with dispute case (metadata + object storage).

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many users, TPS, and geographic spread?"
- 100M+ registered users; 10M+ DAU; peak checkout and P2P spikes (payday, events).
- Target **10k+ sustained payment TPS** with burst handling via queues and partitioning.

**Deduced NFRs:**
- ✅ **Horizontal scaling** of stateless API and worker tiers.
- ✅ **Sharding** wallet/ledger by `userId` or `walletId` for write scalability.
- ✅ **CQRS** optional: command path for writes, read replicas for history.
- ✅ **Idempotency keys** on all payment APIs to survive retries.

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"
- No double spend; balance invariants; escrow holds consistent with available balance.

**Deduced NFRs:**
- ✅ **Strong consistency** within a wallet partition using **serializable** or **per-wallet mutex** + append-only ledger.
- ✅ **Saga / outbox** for cross-service flows (wallet + notifications + rewards).
- ✅ **Exactly-once effect** from client perspective via idempotency + deduplication store.

---

#### 3. **Availability Analysis**

**Think:** "Can users pay during partial outage?"
- Payments are revenue-critical; read-heavy history can degrade independently.

**Deduced NFRs:**
- ✅ **99.99%** availability for payment API (multi-AZ, failover).
- ✅ **Graceful degradation:** decline new risky payments if fraud service down vs allow low-risk (policy).
- ✅ **Circuit breakers** to bank and FX providers with cached fallback rates (read-only) where safe.

---

#### 4. **Maintainability Analysis**

**Think:** "Regulatory, audits, new rails?"
- Clear module boundaries: wallet, ledger, payments, risk, settlement.

**Deduced NFRs:**
- ✅ **Structured logging** with `traceId`, `walletId`, `transactionId` (PII redacted).
- ✅ **Feature flags** for new rails and risk rules.
- ✅ **Configurable limits** and fees without redeploy (dynamic config service).

---

#### 5. **Performance Analysis**

**Think:** "Latency expectations?"
- P2P and QR confirm: **p99 < 500ms** internal path; bank-dependent flows async with webhooks.

**Deduced NFRs:**
- ✅ **Hot path O(1)** balance read from materialized view or cached aggregate + ledger tail verification.
- ✅ **Async workers** for settlement, rewards, notifications, analytics.
- ✅ **Pagination** and **time-bounded** queries for transaction history.

---

#### 6. **Security Analysis**

**Think:** "Account takeover, money laundering, insider threat?"
- Defense in depth: authn/z, encryption, fraud, audit.

**Deduced NFRs:**
- ✅ **OAuth2/OIDC** + **mTLS** for partner APIs; **RBAC** for admin/support tools.
- ✅ **PCI scope reduction** via tokenization; no raw PAN in wallet service.
- ✅ **SIEM** integration; **tamper-evident** ledger (hash chain optional for compliance narratives).

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Wallet balances per currency" | Wallet, Currency, Money, Balance |
| "Limits and holds" | LimitPolicy, Hold, Escrow |
| "P2P send/receive" | Transfer, User, Counterparty |
| "QR / NFC payment" | PaymentIntent, QRCode, NFCToken, Merchant |
| "Bank link verify" | FundingSource, BankAccount, Verification |
| "Transaction history" | Transaction, LedgerEntry |
| "Split bill" | SplitGroup, SplitParticipant |
| "Request money" | PaymentRequest |
| "Recurring" | RecurringSchedule, Mandate |
| "Rewards" | RewardsAccount, RewardRule, Campaign |
| "FX multi-currency" | FxRate, Conversion |
| "2FA fraud" | RiskAssessment, AuthChallenge |
| "Settlement" | SettlementBatch, ReconciliationReport |
| "Dispute escrow" | DisputeCase, EscrowAgreement |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Money | ❌ NO | Value object (amount + currency) |
| Balance | ❌ NO | Derived / materialized from ledger |
| Transfer | ✅ YES | Use case aggregate root / orchestration |
| QRCode | ✅ YES | Payload + expiry + merchant binding |
| NFCToken | ✅ YES | Device-bound payment instrument |
| Verification | ❌ NO | State on FundingSource |
| Conversion | ❌ NO | Record produced by FxService |
| AuthChallenge | ✅ YES | 2FA attempt lifecycle |

### Final Entity List

**Identity & Wallet:**
1. **User** – Person or business profile.
2. **Wallet** – Container for balances; links to user.
3. **WalletBalance** – Per-currency bucket (amount, holds).
4. **LimitPolicy** – Tiered limits per wallet/user/KYC level.

**Money Movement:**
5. **LedgerEntry** – Append-only accounting line (debit/credit).
6. **Transaction** – Business-level payment record (aggregates entries).
7. **Hold** – Reservation against balance (escrow, pending debit).
8. **PaymentIntent** – Merchant or checkout intent (amount, merchant, method).
9. **P2PTransfer** – Sender/recipient, memo, idempotency key.

**Instruments & Funding:**
10. **FundingSource** – Bank, card token, or wallet-to-wallet routing.
11. **BankAccountLink** – External account metadata + verification state.

**Merchant & Channels:**
12. **Merchant** – Acceptor of payments, settlement config.
13. **QRPaymentPayload** – Static/dynamic QR data.
14. **PaymentToken** – NFC / device token reference.

**Product Features:**
15. **SplitBill** – Group, shares, status.
16. **SplitParticipant** – Owes amount, payment link to child transaction.
17. **MoneyRequest** – Request ID, requester, amount, expiry.
18. **RecurringSchedule** – Cron-like schedule + mandate reference.
19. **RewardsAccount** – Points/cashback balance.
20. **RewardAccrual** – Link to transaction + rule ID.

**Risk & Security:**
21. **RiskAssessment** – Score, rules fired, decision.
22. **AuthChallenge** – 2FA session (OTP, push).

**Settlement & Disputes:**
23. **SettlementBatch** – Outbound grouping to rail.
24. **ReconciliationItem** – Match/mismatch line.
25. **EscrowAgreement** – Release conditions, parties.
26. **DisputeCase** – Chargeback/dispute workflow.

**Supporting:**
27. **FxRateQuote** – Rate snapshot for a conversion.
28. **IdempotencyRecord** – Key → response hash / transaction id.

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Core Relationships

#### User ↔ Wallet
**Conclusion:** **Composition** (user owns wallets; wallet does not exist without user in consumer model)
```
User ◆────→ Wallet [1..*]
```

#### Wallet ↔ WalletBalance
**Conclusion:** **Composition**
```
Wallet ◆────→ WalletBalance [1..*] (per currency)
```

#### Wallet ↔ LedgerEntry
**Conclusion:** **Association** (entries reference wallet)
```
Wallet ─────→ LedgerEntry [0..*]
```

#### Transaction ↔ LedgerEntry
**Conclusion:** **Composition** (transaction groups balanced entries)
```
Transaction ◆────→ LedgerEntry [2..*]
```

---

### Pass 2: Payment & Funding Relationships

#### PaymentIntent ↔ Merchant
**Conclusion:** **Association**
```
PaymentIntent ─────→ Merchant [1]
```

#### P2PTransfer ↔ Transaction
**Conclusion:** **Association** (1:1 typical)
```
P2PTransfer ─────→ Transaction [1]
```

#### FundingSource ↔ User
**Conclusion:** **Aggregation**
```
User ◇────→ FundingSource [0..*]
```

#### Hold ↔ Wallet
**Conclusion:** **Association**
```
Wallet ─────→ Hold [0..*]
```

---

### Pass 3: Split, Recurring, Rewards, Disputes

#### SplitBill ↔ SplitParticipant
**Conclusion:** **Composition**
```
SplitBill ◆────→ SplitParticipant [2..*]
```

#### RecurringSchedule ↔ PaymentIntent / Transaction Template
**Conclusion:** **Association** (template or amount rule)
```
RecurringSchedule ─────→ Mandate [1]
```

#### RewardsAccount ↔ User
**Conclusion:** **Composition**
```
User ◆────→ RewardsAccount [0..1]
```

#### DisputeCase ↔ Transaction
**Conclusion:** **Association**
```
DisputeCase ─────→ Transaction [1]
```

#### EscrowAgreement ↔ Hold
**Conclusion:** **Association**
```
EscrowAgreement ─────→ Hold [1..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| User → Wallet | 1:N | Composition |
| Wallet → WalletBalance | 1:N | Composition |
| Transaction → LedgerEntry | 1:N | Composition |
| Wallet → Transaction | 1:N | Association |
| Merchant → PaymentIntent | 1:N | Association |
| User → FundingSource | 1:N | Aggregation |
| SplitBill → SplitParticipant | 1:N | Composition |
| Transaction → DisputeCase | 1:0..1 | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Value Objects & Enums

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────┐
│ <<value object>> │  │ <<enumeration>>  │  │ <<enumeration>>       │
│     Money       │  │ TransactionType  │  │ TransactionStatus     │
├─────────────────┤  ├──────────────────┤  ├───────────────────────┤
│ - amount: long  │  │ P2P_SEND         │  │ PENDING               │
│   (minor units) │  │ P2P_RECEIVE      │  │ AUTHORIZED            │
│ - currency: CCY │  │ MERCHANT_PAY     │  │ CAPTURED              │
├─────────────────┤  │ TOP_UP           │  │ SETTLED               │
│ + add/subtract  │  │ WITHDRAW         │  │ FAILED                │
│ + convert(rate) │  │ REWARD           │  │ REVERSED              │
└─────────────────┘  │ SPLIT            │  │ DISPUTED              │
                     │ RECURRING        │  └───────────────────────┘
                     └──────────────────┘

┌──────────────────┐  ┌──────────────────┐  ┌─────────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │  │ <<enumeration>>      │
│  FundingKind     │  │   RiskDecision    │  │ HoldReason           │
├──────────────────┤  ├──────────────────┤  ├───────────────────────┤
│ WALLET_BALANCE   │  │ ALLOW            │  │ ESCROW               │
│ BANK_ACCOUNT     │  │ CHALLENGE        │  │ PENDING_DEBIT        │
│ CARD_TOKEN       │  │ BLOCK            │  │ DISPUTE              │
└──────────────────┘  └──────────────────┘  └───────────────────────┘
```

---

### Class Diagram 2: Wallet & Ledger Core

```
┌──────────────────────────────────────────────────────────────────┐
│                           Wallet                                  │
├──────────────────────────────────────────────────────────────────┤
│ - walletId: String                                                │
│ - userId: String                                                  │
│ - status: WalletStatus (ACTIVE, FROZEN, CLOSED)                   │
│ - kycTier: KycTier                                                │
│ - balances: Map<Currency, WalletBalance>                          │
│ - limitPolicyId: String                                           │
├──────────────────────────────────────────────────────────────────┤
│ + debit(m: Money, holdId?): LedgerPostingResult                   │
│ + credit(m: Money): LedgerPostingResult                           │
│ + placeHold(m: Money, reason): Hold                               │
│ + releaseHold(holdId): void                                       │
│ + availableBalance(ccy): Money                                    │
└──────────────────────────────────────────────────────────────────┘
         │
         │ 1..*
         ▼
┌──────────────────────────────────────────────────────────────────┐
│                        WalletBalance                              │
├──────────────────────────────────────────────────────────────────┤
│ - currency: Currency                                              │
│ - ledgerBalance: long                                             │
│ - heldAmount: long                                                │
├──────────────────────────────────────────────────────────────────┤
│ + available(): long                                               │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                        Transaction                                │
├──────────────────────────────────────────────────────────────────┤
│ - transactionId: String                                           │
│ - type: TransactionType                                           │
│ - status: TransactionStatus                                       │
│ - idempotencyKey: String                                          │
│ - createdAt: Instant                                              │
│ - entries: List<LedgerEntry>                                      │
│ - metadata: Map<String,String>                                  │
├──────────────────────────────────────────────────────────────────┤
│ + addEntry(entry): void  // ensures double-entry invariant        │
│ + isBalanced(): boolean                                           │
└──────────────────────────────────────────────────────────────────┘
         │ 2..*
         ▼
┌──────────────────────────────────────────────────────────────────┐
│                        LedgerEntry                                │
├──────────────────────────────────────────────────────────────────┤
│ - entryId: String                                                 │
│ - walletId: String                                                │
│ - amount: Money                                                   │
│ - direction: DEBIT | CREDIT                                       │
│ - accountType: WALLET | FEES | ESCROW | REWARDS | EXTERNAL        │
└──────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Payments (P2P, Intent, Split, Request)

```
┌──────────────────────────────────────────────────────────────────┐
│                        P2PTransfer                                │
├──────────────────────────────────────────────────────────────────┤
│ - senderWalletId: String                                          │
│ - receiverWalletId: String                                        │
│ - amount: Money                                                   │
│ - note: String                                                    │
│ - idempotencyKey: String                                          │
├──────────────────────────────────────────────────────────────────┤
│ + execute(ledger: LedgerService): Transaction                    │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                      PaymentIntent                                │
├──────────────────────────────────────────────────────────────────┤
│ - intentId: String                                                │
│ - merchantId: String                                              │
│ - amount: Money                                                   │
│ - qrNonce: String // for dynamic QR                               │
│ - expiresAt: Instant                                              │
│ - channel: QR | NFC | INAPP                                       │
├──────────────────────────────────────────────────────────────────┤
│ + confirm(payerWalletId, method): Transaction                     │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                        SplitBill                                  │
├──────────────────────────────────────────────────────────────────┤
│ - splitId: String                                                 │
│ - initiatorWalletId: String                                       │
│ - total: Money                                                    │
│ - participants: List<SplitParticipant>                            │
│ - mode: PARALLEL | SEQUENTIAL                                     │
├──────────────────────────────────────────────────────────────────┤
│ + collect(): List<Transaction>                                    │
└──────────────────────────────────────────────────────────────────┘
          ◆──────────────┐
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│                    SplitParticipant                               │
├──────────────────────────────────────────────────────────────────┤
│ - walletId: String                                                │
│ - share: Money                                                    │
│ - status: OWES | PAID | DECLINED                                  │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                      MoneyRequest                                 │
├──────────────────────────────────────────────────────────────────┤
│ - requestId: String                                               │
│ - requesterWalletId: String                                       │
│ - amount: Money                                                   │
│ - expiresAt: Instant                                              │
│ - deepLinkToken: String                                           │
├──────────────────────────────────────────────────────────────────┤
│ + accept(payerWalletId): Transaction                              │
└──────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Funding, Recurring, Rewards, Risk

```
┌──────────────────────────────────────────────────────────────────┐
│                     FundingSource                                 │
├──────────────────────────────────────────────────────────────────┤
│ - sourceId: String                                                │
│ - userId: String                                                  │
│ - kind: FundingKind                                               │
│ - bankDisplayName: String                                         │
│ - maskLast4: String                                               │
│ - verificationStatus: PENDING | VERIFIED | FAILED                  │
├──────────────────────────────────────────────────────────────────┤
│ + verify(code1, code2): boolean                                   │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                   RecurringSchedule                               │
├──────────────────────────────────────────────────────────────────┤
│ - scheduleId: String                                                │
│ - userId: String                                                  │
│ - merchantId: String                                              │
│ - amount: Money                                                   │
│ - cronExpr: String                                                │
│ - mandateId: String                                               │
│ - nextRunAt: Instant                                              │
├──────────────────────────────────────────────────────────────────┤
│ + tick(scheduler): Optional<Transaction>                          │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                     RewardsAccount                                │
├──────────────────────────────────────────────────────────────────┤
│ - userId: String                                                  │
│ - pointsBalance: long                                             │
├──────────────────────────────────────────────────────────────────┤
│ + accrue(ruleId, txnId, points): void                             │
│ + redeem(points): Money // policy-driven                          │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ <<interface>>                                                     │
│                  FraudEvaluator                                   │
├──────────────────────────────────────────────────────────────────┤
│ + evaluate(ctx: PaymentContext): RiskAssessment                   │
└──────────────────────────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┴────────────┐
        ▼                        ▼
┌────────────────┐      ┌────────────────────┐
│ RulesEngine    │      │ MlRiskModelAdapter │
│ FraudEvaluator │      │ FraudEvaluator     │
└────────────────┘      └────────────────────┘
```

---

### Class Diagram 5: Settlement, Escrow, Dispute

```
┌──────────────────────────────────────────────────────────────────┐
│                   SettlementBatch                                 │
├──────────────────────────────────────────────────────────────────┤
│ - batchId: String                                                 │
│ - currency: Currency                                              │
│ - rail: ACH | CARD | RTP | INTERNAL                               │
│ - status: OPEN | SUBMITTED | SETTLED | FAILED                     │
│ - items: List<SettlementItem>                                     │
├──────────────────────────────────────────────────────────────────┤
│ + closeAndSubmit(): void                                          │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                     EscrowAgreement                               │
├──────────────────────────────────────────────────────────────────┤
│ - escrowId: String                                                │
│ - payerWalletId: String                                           │
│ - payeeWalletId: String                                           │
│ - amount: Money                                                   │
│ - releaseCondition: ON_DELIVERY | TIME | MANUAL                   │
│ - disputeCaseId: Optional<String>                                 │
├──────────────────────────────────────────────────────────────────┤
│ + fund(): Hold                                                    │
│ + releaseToPayee(): Transaction                                   │
│ + refundToPayer(): Transaction                                    │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                      DisputeCase                                    │
├──────────────────────────────────────────────────────────────────┤
│ - disputeId: String                                               │
│ - transactionId: String                                           │
│ - status: OPEN | UNDER_REVIEW | WON | LOST                        │
│ - heldAmount: Money                                               │
├──────────────────────────────────────────────────────────────────┤
│ + attachEvidence(uri): void                                       │
│ + resolve(outcome): Transaction                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Value Objects & Enums

```java
// Money.java — amounts in minor units (e.g., cents)
public final class Money {
    private final long minorUnits;
    private final Currency currency;

    public Money(long minorUnits, Currency currency) {
        if (minorUnits < 0) throw new IllegalArgumentException("negative");
        this.minorUnits = minorUnits;
        this.currency = Objects.requireNonNull(currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.minorUnits + other.minorUnits, currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        if (this.minorUnits < other.minorUnits)
            throw new IllegalArgumentException("insufficient");
        return new Money(this.minorUnits - other.minorUnits, currency);
    }

    public long minorUnits() { return minorUnits; }
    public Currency currency() { return currency; }

    private void assertSameCurrency(Money o) {
        if (!this.currency.equals(o.currency))
            throw new IllegalArgumentException("currency mismatch");
    }
}
```

```java
// TransactionType.java
public enum TransactionType {
    P2P_SEND, P2P_RECEIVE, MERCHANT_PAY, TOP_UP, WITHDRAW,
    REWARD_ACCRUAL, REWARD_REDEMPTION, SPLIT, RECURRING,
    ESCROW_FUND, ESCROW_RELEASE, DISPUTE_ADJUSTMENT, FX_CONVERSION
}
```

```java
// TransactionStatus.java
public enum TransactionStatus {
    PENDING, AUTHORIZED, CAPTURED, SETTLED, FAILED, REVERSED, DISPUTED
}
```

---

### Ledger & Wallet (Double-Entry Invariant)

```java
// LedgerEntry.java
public class LedgerEntry {
    private final String entryId;
    private final String walletId;
    private final Money amount;
    private final EntryDirection direction;
    private final LedgerAccountType accountType;

    public LedgerEntry(String walletId, Money amount,
                       EntryDirection direction, LedgerAccountType accountType) {
        this.entryId = UUID.randomUUID().toString();
        this.walletId = walletId;
        this.amount = amount;
        this.direction = direction;
        this.accountType = accountType;
    }
    // getters ...
}

public enum EntryDirection { DEBIT, CREDIT }

public enum LedgerAccountType {
    WALLET, FEES, ESCROW, REWARDS, EXTERNAL_CLEARING
}
```

```java
// Transaction.java
public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private TransactionStatus status;
    private final String idempotencyKey;
    private final Instant createdAt;
    private final List<LedgerEntry> entries = new ArrayList<>();

    public Transaction(TransactionType type, String idempotencyKey) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.status = TransactionStatus.PENDING;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
    }

    public void addEntry(LedgerEntry e) { entries.add(e); }

    public boolean isBalanced() {
        Map<Currency, Long> net = new HashMap<>();
        for (LedgerEntry e : entries) {
            Money m = e.getAmount();
            long sign = e.getDirection() == EntryDirection.DEBIT ? -1 : 1;
            net.merge(m.currency(), sign * m.minorUnits(), Long::sum);
        }
        return net.values().stream().allMatch(v -> v == 0L);
    }

    public void markSettled() { this.status = TransactionStatus.SETTLED; }
    // getters ...
}
```

```java
// Wallet.java — simplified per-currency available balance
public class Wallet {
    private final String walletId;
    private final String userId;
    private final Map<Currency, Long> ledgerMinor = new ConcurrentHashMap<>();
    private final Map<Currency, Long> heldMinor = new ConcurrentHashMap<>();

    public Wallet(String walletId, String userId) {
        this.walletId = walletId;
        this.userId = userId;
    }

    public Money available(Currency ccy) {
        long lb = ledgerMinor.getOrDefault(ccy, 0L);
        long h = heldMinor.getOrDefault(ccy, 0L);
        return new Money(Math.max(0, lb - h), ccy);
    }

    public synchronized void apply(LedgerEntry e) {
        if (!walletId.equals(e.getWalletId())) return;
        Currency c = e.getAmount().currency();
        long delta = e.getDirection() == EntryDirection.CREDIT
                ? e.getAmount().minorUnits()
                : -e.getAmount().minorUnits();
        ledgerMinor.merge(c, delta, Long::sum);
    }

    public synchronized Hold placeHold(Money amount, HoldReason reason) {
        Money avail = available(amount.currency());
        if (avail.minorUnits() < amount.minorUnits())
            throw new IllegalStateException("insufficient available funds");
        heldMinor.merge(amount.currency(), amount.minorUnits(), Long::sum);
        return new Hold(UUID.randomUUID().toString(), walletId, amount, reason);
    }

    public synchronized void releaseHold(Hold h) {
        heldMinor.merge(h.getAmount().currency(), -h.getAmount().minorUnits(), Long::sum);
    }
}
```

```java
// Hold.java
public class Hold {
    private final String holdId;
    private final String walletId;
    private final Money amount;
    private final HoldReason reason;

    public Hold(String holdId, String walletId, Money amount, HoldReason reason) {
        this.holdId = holdId;
        this.walletId = walletId;
        this.amount = amount;
        this.reason = reason;
    }
    // getters ...
}

public enum HoldReason { ESCROW, PENDING_DEBIT, DISPUTE }
```

---

### LedgerService — P2P Transfer (Idempotent)

```java
// LedgerService.java
public class LedgerService {
    private final Map<String, Transaction> idempotency = new ConcurrentHashMap<>();

    public Transaction executeP2P(P2PTransfer cmd) {
        String key = cmd.getIdempotencyKey();
        if (idempotency.containsKey(key)) {
            return idempotency.get(key);
        }

        Transaction txn = new Transaction(TransactionType.P2P_SEND, key);
        Money amt = cmd.getAmount();

        txn.addEntry(new LedgerEntry(cmd.getSenderWalletId(), amt,
                EntryDirection.DEBIT, LedgerAccountType.WALLET));
        txn.addEntry(new LedgerEntry(cmd.getReceiverWalletId(), amt,
                EntryDirection.CREDIT, LedgerAccountType.WALLET));

        if (!txn.isBalanced()) throw new IllegalStateException("unbalanced");

        // In production: persist txn + entries in one DB transaction, then update wallets
        applyToWallets(txn);

        txn.markSettled();
        idempotency.put(key, txn);
        return txn;
    }

    private void applyToWallets(Transaction txn) {
        // resolve wallets from registry...
    }
}
```

```java
// P2PTransfer.java
public class P2PTransfer {
    private final String senderWalletId;
    private final String receiverWalletId;
    private final Money amount;
    private final String note;
    private final String idempotencyKey;

    public P2PTransfer(String senderWalletId, String receiverWalletId,
                       Money amount, String note, String idempotencyKey) {
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
        this.note = note;
        this.idempotencyKey = idempotencyKey;
    }
    // getters ...
}
```

---

### PaymentIntent (QR / NFC)

```java
// PaymentIntent.java
public class PaymentIntent {
    private final String intentId;
    private final String merchantId;
    private final Money amount;
    private final Instant expiresAt;
    private final PaymentChannel channel;
    private final String qrNonce; // null for static QR

    public PaymentIntent(String merchantId, Money amount,
                         Duration ttl, PaymentChannel channel, String qrNonce) {
        this.intentId = UUID.randomUUID().toString();
        this.merchantId = merchantId;
        this.amount = amount;
        this.expiresAt = Instant.now().plus(ttl);
        this.channel = channel;
        this.qrNonce = qrNonce;
    }

    public Transaction confirm(String payerWalletId, LedgerService ledger) {
        if (Instant.now().isAfter(expiresAt)) throw new IllegalStateException("expired");
        Transaction txn = new Transaction(TransactionType.MERCHANT_PAY, intentId);
        txn.addEntry(new LedgerEntry(payerWalletId, amount,
                EntryDirection.DEBIT, LedgerAccountType.WALLET));
        txn.addEntry(new LedgerEntry(merchantWalletId(merchantId), amount,
                EntryDirection.CREDIT, LedgerAccountType.WALLET));
        if (!txn.isBalanced()) throw new IllegalStateException("unbalanced");
        // fees, escrow, etc. can add more lines
        return txn;
    }

    private String merchantWalletId(String merchantId) {
        return "mw_" + merchantId; // lookup in real system
    }
}

public enum PaymentChannel { QR_STATIC, QR_DYNAMIC, NFC, INAPP }
```

---

### Escrow & Dispute (Sketch)

```java
// EscrowService.java
public class EscrowService {
    public Hold fundEscrow(Wallet payer, Money amount, EscrowAgreement agreement) {
        Hold h = payer.placeHold(amount, HoldReason.ESCROW);
        // persist agreement + link holdId
        return h;
    }

    public Transaction releaseToPayee(Wallet payer, Wallet payee,
                                        Hold hold, LedgerService ledger) {
        payer.releaseHold(hold);
        // build txn: debit payer wallet, credit payee wallet (or move from escrow account)
        return ledger.move(hold.getAmount(), payer.getWalletId(), payee.getWalletId(),
                TransactionType.ESCROW_RELEASE, hold.getHoldId());
    }
}
```

---

### Fraud & 2FA (Interfaces)

```java
// PaymentContext.java
public class PaymentContext {
    private final String userId;
    private final String deviceId;
    private final Money amount;
    private final String counterpartyId;
    // geo, history snippets, etc.
}

public class RiskAssessment {
    private final RiskDecision decision;
    private final int score;
    private final List<String> rulesHit;

    public RiskAssessment(RiskDecision decision, int score, List<String> rulesHit) {
        this.decision = decision;
        this.score = score;
        this.rulesHit = rulesHit;
    }
    public RiskDecision decision() { return decision; }
}

public enum RiskDecision { ALLOW, CHALLENGE, BLOCK }

public interface FraudEvaluator {
    RiskAssessment evaluate(PaymentContext ctx);
}

public interface StepUpAuth {
    boolean verify(String challengeId, String otpOrSignedPayload);
}
```

---

### Demo

```java
// DigitalWalletDemo.java
public class DigitalWalletDemo {
    public static void main(String[] args) {
        Wallet alice = new Wallet("w_alice", "u_alice");
        Wallet bob = new Wallet("w_bob", "u_bob");

        // Seed balances via synthetic credits (top-up omitted)
        Transaction seed = new Transaction(TransactionType.TOP_UP, "seed-1");
        seed.addEntry(new LedgerEntry(alice.getWalletId(),
                new Money(10_000, Currency.getInstance("USD")),
                EntryDirection.CREDIT, LedgerAccountType.EXTERNAL_CLEARING));
        seed.addEntry(new LedgerEntry("clearing",
                new Money(10_000, Currency.getInstance("USD")),
                EntryDirection.DEBIT, LedgerAccountType.EXTERNAL_CLEARING));
        // In demo, manually credit alice
        alice.apply(new LedgerEntry(alice.getWalletId(),
                new Money(10_000, Currency.getInstance("USD")),
                EntryDirection.CREDIT, LedgerAccountType.WALLET));

        LedgerService ledger = new LedgerService();
        P2PTransfer p2p = new P2PTransfer(
                alice.getWalletId(), bob.getWalletId(),
                new Money(2_500, Currency.getInstance("USD")),
                "Dinner split", "idem-001");

        Transaction txn = ledger.executeP2P(p2p);
        System.out.println("P2P completed: " + txn);
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `FraudEvaluator`, FX pricing, fee calculation  
**Why:** Swap rules engine vs ML adapter without changing payment orchestration  
**Interview Justification:** "Risk and pricing change frequently; strategy keeps the hot payment path stable."

---

### Pattern 2: Saga / Process Manager
**Where:** Top-up (wallet + bank), merchant capture + settlement, split bill completion  
**Why:** Long-running flows with compensating transactions  
**Interview Justification:** "External rails fail independently; saga coordinates commits and rollbacks per step."

---

### Pattern 3: Repository + Unit of Work
**Where:** Persisting `Transaction`, `LedgerEntry`, `Wallet` snapshots  
**Why:** Atomic commit of balanced entries  
**Interview Justification:** "Ledger must never persist a half-posted transaction."

---

### Pattern 4: State Machine
**Where:** `TransactionStatus`, `DisputeCase`, `FundingSource.verificationStatus`  
**Why:** Invalid transitions (e.g., SETTLED → PENDING) are rejected  
**Interview Justification:** "Money states are legally meaningful; explicit machines reduce bugs."

---

### Pattern 5: Template Method / Pipeline
**Where:** Payment pipeline: authenticate → risk → limit check → ledger → notify → rewards  
**Why:** Shared skeleton with overridable steps per payment type  
**Interview Justification:** "P2P and merchant share validation but differ in settlement hooks."

---

## 💡 Step 8: Interview Discussion Points

### 1. Ledger Design: Double-Entry vs Single Balance Field

**Interviewer:** "Why double-entry inside a wallet service?"

**Answer:**
"**Single balance field** is fast to read but **dangerous** for reconciliation—you cannot explain *why* balance changed or detect silent corruption.

**Double-entry:**
```
User A sends $50 to User B

Debit  A wallet   $50
Credit B wallet   $50
(net zero across system for USD wallet layer)
```

**Benefits:**
- Invariant: sum of wallet-equity entries matches liabilities you owe users.
- Fees, escrow, rewards are **separate lines**—clean audits.
- Easier **partial reversals** and **disputes** (post adjusting entries).

**Trade-off:** More storage and stricter write path; mitigate with **per-wallet partitioning** and **materialized balance** updated in the same transaction as entries."

---

### 2. Idempotency and Exactly-Once User Experience

**Interviewer:** "Network retries caused a duplicate charge—how do you prevent that?"

**Answer:**
"**Client sends `Idempotency-Key`** on POST /payments.

**Server:**
1. Store key in **strong store** (Redis with TTL or DB unique index).
2. On duplicate key, return **same** `transactionId` and status (HTTP 200/409 policy).
3. Ledger write and idempotency record in **one transaction**.

**For async rails:** use **outbox** + worker that also keys off `idempotencyKey`.

**Clock skew:** keys scoped to **user + endpoint**; TTL matches retry window (e.g., 24h)."

---

### 3. Multi-Currency: When to Convert?

**Interviewer:** "Sender pays in EUR, receiver wants USD—walk through it."

**Answer:**
"**Steps:**
1. **Lock FX quote** with expiry (`FxRateQuote`): rate, base, counter, timestamp.
2. Post ledger lines:
   - Debit sender **EUR** wallet (exact amount charged).
   - Credit **FX clearing** EUR / debit clearing USD (internal accounts).
   - Credit receiver **USD** wallet (per quote, with disclosed spread).

3. **Fees** as separate debit lines (transparent).

**Consistency:** entire txn in **one DB transaction** per shard; if sender and receiver shard differently, use **two-phase** internal transfer + **saga** with compensating FX reversal.

**Display:** show **estimated** vs **final** if quote expires—either reject or refresh with user confirm."

---

### 4. Escrow vs Dispute Holds

**Interviewer:** "How is escrow different from a chargeback hold?"

**Answer:**
"**Escrow** is **contractual release** (buyer/seller, milestones). Funds are intentionally **blocked** until `EscrowAgreement` conditions met; may auto-release on timeout.

**Dispute** is **post-settlement** or **pre-arbitration** hold triggered by `DisputeCase`; amount may be **partial**; outcome produces **adjustment entries** (clawback or merchant loss).

**Implementation:** both use `Hold` but different `HoldReason`, policies, and **SLA timers**. Disputes integrate **evidence store** and **network reason codes** for card rails."

---

### 5. Settlement vs Ledger: User Sees SETTLED, Bank Is Async

**Interviewer:** "User sees money sent—when is it actually at the bank?"

**Answer:**
"**Internal ledger** can mark **SETTLED** when wallet-to-wallet completes instantly.

**External settlement** (ACH) is **batched**:
- `SettlementBatch` aggregates net movements per rail/currency.
- **Submitted** → **ACK** → **SETTLED** at bank with **reconciliation** against statement.

**UX:** show **Available** vs **Processing withdrawal** for outbound bank; **do not** spend uncollected ACH pulls.

**Reconciliation service** compares **ledger EXTERNAL_CLEARING** to bank file; discrepancies open **ops tickets**."

---

### 6. Fraud: CHALLENGE Path with 2FA

**Interviewer:** "When does 2FA run?"

**Answer:**
"**RiskDecision.CHALLENGE** triggers `StepUpAuth`:
- New device + high amount
- First-time payee over threshold
- Velocity spike (10 P2Ps in 1 minute)

Flow: `paymentIntent` → `201` with `challengeId` → client completes OTP/push → server **resumes** same idempotent payment.

**BLOCK** returns hard decline + log to SIEM; **ALLOW** skips step-up for low-friction small amounts (policy)."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `Wallet` models balance state; `LedgerService` posts transactions; `FraudEvaluator` scores risk only.
- `SettlementBatch` handles rail batching, not user UX.

### Open/Closed ✅
```java
public class TieredFraudEvaluator implements FraudEvaluator {
    private final List<FraudRule> rules;
    public RiskAssessment evaluate(PaymentContext ctx) {
        // add new FraudRule without changing evaluator contract
    }
}
```

### Liskov Substitution ✅
```java
FraudEvaluator ev = new RulesEngineFraudEvaluator();
ev = new MlRiskModelAdapter(); // same evaluate() contract
```

### Interface Segregation ✅
```java
interface WalletQueries { Money available(Currency c); }
interface WalletCommands { Hold placeHold(Money m, HoldReason r); }
// read models vs write paths for CQRS deployments
```

### Dependency Inversion ✅
```java
public class PaymentOrchestrator {
    private final FraudEvaluator fraud;
    private final LedgerService ledger;
    public PaymentOrchestrator(FraudEvaluator fraud, LedgerService ledger) {
        this.fraud = fraud;
        this.ledger = ledger;
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Double-entry ledger** with **idempotent** payment APIs
- ✅ **Holds** for escrow, pending debits, and disputes
- ✅ **PaymentIntent** abstraction for **QR, NFC, in-app**
- ✅ **Sagas / outbox** for bank settlement and cross-shard flows
- ✅ **Separate rewards ledger** linked by `transactionId`

### Differentiation from ATM System
- ✅ **No cash dispenser**; focus on **digital balance**, **P2P**, **merchants**, **multi-currency**, and **settlement**
- ✅ **Rich product surface:** split, request money, recurring, cashback
- ✅ **Risk/2FA** integrated in **payment orchestration**, not card-present PIN only

### Operations & Compliance
- ✅ **Reconciliation** batches vs external rails
- ✅ **Dispute** workflow with **adjusting entries**, not simple reversal-only
- ✅ **Configurable limits** by **KYC tier** and velocity

---

**Digital Wallet Service LLD — Hard**

Ready for review!
