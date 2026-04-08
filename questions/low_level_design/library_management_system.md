# Low-Level Design: Library Management System

**Difficulty:** Medium-Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5-8 minutes)

### What the Interviewer Says:
*"Design a library management system for a network of branches that manages physical and digital collections, members with borrowing limits, checkout and returns, holds, fines, reading-room bookings, inter-library loans, inventory, and operational reports."*

### Clarifying Questions to Ask:

1. **Q:** Is the system multi-branch from day one, or single-library with future expansion?  
   **A:** Multiple branches/libraries; each has its own inventory of copies, with shared patron records and optional union catalog search.

2. **Q:** How do we model a *book* vs *copies*?  
   **A:** Bibliographic record (title, ISBN, authors, subjects) is separate from *items* (physical barcodes per branch). Digital titles are another manifestation with license seats.

3. **Q:** What member tiers exist and what limits apply?  
   **A:** Tiers (e.g., Standard, Student, Faculty, Premium) define max concurrent loans, loan duration, renewal count, hold limits, and digital concurrent streams.

4. **Q:** Checkout rules: renewals, grace period, recalls?  
   **A:** Renewals if no holds and under max renewals; grace period before overdue fines; staff may recall high-demand items.

5. **Q:** How do holds work when a copy becomes available?  
   **A:** FIFO (or tier-priority) queue per bibliographic record (or per branch pickup location); notify patron; pickup window before next in queue.

6. **Q:** Fine types and payment?  
   **A:** Overdue (per day or tiered), lost (replacement + processing), damaged (assessed); support waivers, partial payment, suspension when balance exceeds threshold.

7. **Q:** Search requirements?  
   **A:** Full-text on title, author, ISBN, subjects/tags; filter by branch availability, format (print/digital), and on-shelf status.

8. **Q:** Digital resources: checkout or license-based access?  
   **A:** E-books/audiobooks: concurrent license seats per title per branch or consortium; loan period and automatic expiry; offline download optional (policy).

9. **Q:** Reading room booking: resources and conflicts?  
   **A:** Rooms or desks at a branch; time slots; conflict detection; cancellation policy; optional integration with member status.

10. **Q:** Inter-library loan (ILL): who owns the workflow?  
    **A:** Request from home branch, partner supplies item, track in-transit states, due dates, and fees per agreement.

11. **Q:** Inventory scope?  
    **A:** Acquisition, weeding, transfers between branches, cycle counts, condition notes, and lost/damaged write-offs.

12. **Q:** Reports and scale?  
    **A:** Popular titles, overdue lists, member activity; thousands of members and hundreds of thousands of items; reports can be async/materialized.

13. **Q:** Consistency vs availability for catalog search?  
    **A:** Strong consistency for loans, holds, and payments; eventually consistent search index acceptable with short lag.

---

## 🔹 Step 2: Gather Requirements (8-12 minutes)

### Functional Requirements

#### Multi-Branch & Catalog (FR1-FR10)
1. System shall support multiple library branches, each with address, hours, and local policy overrides where allowed
2. System shall maintain bibliographic records with ISBN-10/ISBN-13, title, edition, language, publication year, and subjects/tags
3. System shall associate one or more authors (order matters for display) with each bibliographic record
4. System shall track physical *item copies* (barcode, branch, shelf location, condition, status: available, on loan, on hold shelf, in transit, repair, lost)
5. System shall support multiple copies of the same work across branches
6. System shall attach digital manifestations (e-book, audiobook) to bibliographic records with format metadata
7. System shall enforce digital license pools (max concurrent checkouts per title per scope: branch or consortium)
8. Librarians shall create, update, and merge bibliographic records with audit trail
9. System shall support item transfer between branches with in-transit tracking
10. System shall support acquisition and weeding workflows updating inventory counts

#### Member Management (FR11-FR16)
11. Patrons shall register with verified identity rules per policy (min age, ID type)
12. System shall maintain member profile: contact, home branch, status (active, suspended, expired), and notes
13. System shall assign membership tier defining loan limits, loan length, renewal count, hold limits, fine caps, and digital concurrency
14. System shall enforce suspension when unpaid fines exceed configurable threshold or for policy violations
15. System shall support family/linked accounts or guarantors where policy allows
16. Staff shall renew membership term and upgrade/downgrade tier

#### Checkout & Return (FR17-FR24)
17. Member shall borrow eligible physical item at a branch if within tier limits and item is available
18. System shall record due date from tier and item type rules (e.g., bestseller shorter loan)
19. Member or staff shall renew loan if allowed (no outstanding holds, under renewal cap, not recalled)
20. Staff shall process returns at any branch (if policy allows) or home branch only
21. System shall place returned copy into *hold fulfillment* if a hold queue exists for that bibliographic record at that branch
22. Staff shall check in damaged or incomplete returns and trigger assessment workflow
23. System shall support self-checkout kiosks via barcode/RFID (interface-level)
24. System shall emit events for due-date reminders (email/SMS—adapter interface)

#### Reservations / Holds (FR25-FR30)
25. Member shall place a hold on a bibliographic record with preferred pickup branch
26. System shall queue holds FIFO or tier-weighted per configuration
27. When a copy becomes available at pickup branch, system shall notify patron and start pickup window timer
28. If pickup window expires, system shall offer copy to next hold or return to circulating shelf
29. Staff shall cancel holds and optionally reorder queue with reason codes
30. System shall support *volume-level* holds for multi-volume works when configured

#### Fines & Billing (FR31-FR38)
31. System shall accrue overdue fines daily after grace period per item type and tier
32. System shall apply lost-item charge (replacement cost + processing fee) when declared lost or not returned after maximum delinquency
33. System shall record damaged-item fees based on staff assessment and fee schedule
34. Member shall pay fines online or in branch; partial payments allowed
35. Staff shall waive or adjust fines with role-based authority and mandatory reason
36. System shall suspend borrowing when outstanding balance exceeds policy threshold
37. System shall produce fine statements and payment receipts (interface-level)
38. System shall support refunds/reversals for erroneous charges with audit

#### Search & Discovery (FR39-FR44)
39. Patron shall search by title, author, ISBN, subject/tag, and keyword across union catalog
40. Search shall filter by branch, availability (on shelf), format (print/digital), and age audience
41. System shall rank results by relevance and optionally boost local branch availability
42. System shall avoid showing suppressed or staff-only records to public roles
43. Search shall debounce and tolerate partial ISBN and normalized author names
44. System shall log popular queries for collection development (aggregated, privacy-safe)

#### Digital Resources (FR45-FR49)
45. Patron shall borrow digital title if license seat available; borrowing creates time-bounded entitlement
46. System shall release digital license on early return or automatic expiry
47. System shall track per-patron concurrent digital loans against tier limit
48. System shall integrate with DRM/streaming provider via gateway (interface); core domain owns loan lifecycle
49. Audiobooks and e-books may have different loan lengths and renewal rules

#### Reading Room Booking (FR50-FR54)
50. Patron shall view reading rooms/desks at a branch with capacity and amenities
51. Patron shall book a time slot without double-booking the same room
52. System shall enforce min/max duration and advance booking window
53. Patron shall cancel within policy; no-shows may incur fee or strike per branch rules
54. Staff shall block rooms for maintenance or events

#### Inter-Library Loan (ILL) (FR55-FR60)
55. Patron shall request ILL when local holdings insufficient; request ties to home branch
56. System shall track ILL states: requested, shipped, received, on loan, returned, cancelled, declined
57. System shall manage due dates and renewal requests to partner library per agreement
58. System shall apply ILL fees (postage, lending fee) per policy
59. System shall record partner libraries and contact metadata
60. ILL items shall participate in checkout/return with distinct item type rules

#### Inventory Management (FR61-FR66)
61. Staff shall receive new items into catalog with initial branch and condition
62. Staff shall transfer items between branches with shipment/receipt checkpoints
63. Staff shall perform cycle count and reconcile discrepancies
64. System shall mark items lost, withdrawn, or under repair with reasons
65. System shall support bulk import/update via controlled jobs
66. System shall maintain inventory event history per item

#### Reports & Analytics (FR67-FR72)
67. System shall report most-circulated titles and subjects for a date range and scope (branch/consortium)
68. System shall list overdue items with patron and branch breakdown
69. System shall summarize member activity: loans, holds, fines paid, digital usage
70. System shall export reports (CSV/PDF—interface) and schedule recurring reports
71. System shall support dashboard KPIs: active loans, hold queue depth, ILL turnaround
72. Long-running reports shall run asynchronously without blocking transactional operations

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many branches, items, members, and concurrent operations?"
- Branches: tens to hundreds; items: 100k–10M; members: 10k–1M
- Peak: morning/after-school self-checkout bursts; batch report jobs

**Deduced NFRs:**
- ✅ Horizontal scaling of stateless API tier; read replicas for search and reports
- ✅ Sharding candidate: by `branchId` for item and loan partitions
- ✅ Async workers for notifications, search indexing, and heavy reports
- ✅ Caching of hot bibliographic metadata and branch policy

---

#### 2. **Consistency Analysis**

**Think:** "What must never be wrong?"
- Two patrons cannot borrow the same physical copy simultaneously
- Digital license seats cannot be over-allocated
- Hold queue ordering must be deterministic under concurrency
- Financial adjustments must be auditable

**Deduced NFRs:**
- ✅ **Strong consistency** for loan creation, return, hold fulfillment, and payments (transactional boundaries)
- ✅ **Optimistic locking** on item row or distributed lock per `itemId` for checkout
- ✅ **Serializable or snapshot isolation** for hold dequeue + checkout pairing
- ✅ **Idempotent** payment webhooks and checkout retries

---

#### 3. **Availability Analysis**

**Think:** "Can the library operate if search is down? If checkout is down?"
- Checkout at desk is critical; search degradation is tolerable briefly
- Digital provider outage should not corrupt local loan state

**Deduced NFRs:**
- ✅ **99.9%** availability for circulation APIs; graceful read-only mode for catalog if needed
- ✅ **Circuit breaker** around external DRM/delivery APIs with queue-and-retry
- ✅ **Degraded search**: stale index fallback with banner
- ✅ **Multi-AZ** deployment for core database

---

#### 4. **Maintainability Analysis**

**Think:** "Policies change by branch and over time."
- Loan periods, fine schedules, and tier benefits vary
- New formats (magazine, device lending) may appear

**Deduced NFRs:**
- ✅ **Policy engine** or strategy objects for loan rules and fine calculation
- ✅ **Event sourcing or domain events** for loan/hold lifecycle troubleshooting
- ✅ **Feature flags** per branch for ILL and digital
- ✅ **Admin UI** for schedules, fee tables, and report templates (interface-level)

---

#### 5. **Performance Analysis**

**Think:** "SLAs for desk operations and search?"
- Checkout p95 < 300 ms; search p95 < 500 ms; reports async

**Deduced NFRs:**
- ✅ **Indexed** queries on barcode, memberId, dueDate, branchId
- ✅ **Search:** inverted index (Elasticsearch/OpenSearch) denormalized from catalog
- ✅ **Pagination** everywhere; cursor-based for large member history
- ✅ **Precomputed aggregates** for popular books (nightly rollups)

---

#### 6. **Security Analysis**

**Think:** "PII, payments, staff fraud?"
- Member PII and reading history are sensitive
- Fine waivers need RBAC

**Deduced NFRs:**
- ✅ **RBAC** (patron, circulation clerk, librarian, branch admin, finance)
- ✅ **Encryption** at rest for PII; TLS in transit
- ✅ **Audit log** for waivers, merges, deletions, and ILL cost overrides
- ✅ **PCI:** delegate card handling to payment provider tokens only

---

## 🧩 Step 3: Identify Core Entities (10-14 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "ISBN, title, authors, subjects" | BibliographicRecord, Author, Subject |
| "Physical copies per branch" | ItemCopy, LibraryBranch, ShelfLocation |
| "Member tiers and limits" | Member, MembershipTier, LoanPolicy |
| "Checkout and return" | Loan, CirculationService |
| "Hold / reservation" | Hold, HoldQueue |
| "Overdue, lost, damaged fines" | Fine, FineRule, Payment |
| "Search by title, author, ISBN" | SearchIndex, SearchQuery |
| "E-book, audiobook licenses" | DigitalManifestation, DigitalLicensePool, DigitalEntitlement |
| "Reading room booking" | ReadingRoom, RoomBooking, TimeSlot |
| "Inter-library loan" | ILLRequest, PartnerLibrary, Shipment |
| "Inventory transfer, cycle count" | InventoryEvent, StockTransfer |
| "Popular books, overdue report" | ReportJob, CirculationStatistic |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| ISBN | ❌ NO | Attribute of BibliographicRecord |
| Title | ❌ NO | Attribute |
| ShelfLocation | ⚠️ Value object | String or embedded on ItemCopy |
| LoanPolicy | ✅ YES | Rules object; may be aggregate with Tier |
| CirculationService | ❌ NO | Application/domain service |
| SearchQuery | ❌ NO | DTO / value object |
| TimeSlot | ✅ YES | Value object or embedded in RoomBooking |
| Shipment | ✅ YES | Optional sub-entity of ILL / transfer |

### Final Entity List

**Catalog & Inventory:**
1. **BibliographicRecord** - Work-level metadata (ISBN, title, subjects, authors linkage)
2. **Author** - Canonical name, identifiers
3. **Subject** - Controlled vocabulary / tag
4. **ItemCopy** - Physical unit with barcode, branch, status, condition
5. **DigitalManifestation** - Format-specific digital title attached to record
6. **DigitalLicensePool** - Concurrent seats and scope (branch/consortium)
7. **LibraryBranch** - Branch metadata, hours, overrides

**Members & Policy:**
8. **Member** - Patron aggregate root
9. **MembershipTier** - Limits and durations
10. **LoanPolicy** - Per material type rules (loan length, renewals, grace, fine schedule id)

**Circulation:**
11. **Loan** - Active or historical loan of physical or digital item
12. **Hold** - Reservation with pickup branch and queue position
13. **HoldQueue** - Per bibliographic record + pickup branch ordering (may be implicit collection)

**Financials:**
14. **Fine** - Line items (overdue, lost, damaged, ILL)
15. **Payment** - Settlement against fines/fees
16. **FineCalculator** - Strategy/policy execution (often modeled as service + rule objects)

**Digital Access:**
17. **DigitalEntitlement** - Time-bounded grant consuming a license seat

**Rooms & ILL:**
18. **ReadingRoom** - Bookable resource at branch
19. **RoomBooking** - Patron reservation for slot
20. **PartnerLibrary** - ILL counterparty
21. **ILLRequest** - Cross-library fulfillment workflow

**Operations:**
22. **InventoryEvent** - Receive, transfer, withdraw, adjust
23. **ReportDefinition / ReportJob** - Scheduled or ad hoc analytics

**Search (supporting):**
24. **CatalogSearchDocument** - Denormalized projection for index (not always a first-class persisted entity)

---

## 🔗 Step 4: Establish Relationships (12-16 minutes)

### Pass 1: Catalog & Copies

#### BibliographicRecord ↔ Author
**Conclusion:** **Many-to-Many** (ordered contributor roles)
```
BibliographicRecord ────< WorkContributor >──── Author
  (role: AUTHOR, EDITOR, TRANSLATOR; sequence)
```

#### BibliographicRecord ↔ Subject
**Conclusion:** **Many-to-Many**
```
BibliographicRecord ────< RecordSubject >──── Subject
```

#### BibliographicRecord ↔ ItemCopy
**Conclusion:** **Composition/Aggregation** (copies belong to catalog record conceptually; stored with FK)
```
BibliographicRecord (1) ──────→ ItemCopy [1..*]
```

#### ItemCopy ↔ LibraryBranch
**Conclusion:** **Many-to-One**
```
ItemCopy ─────→ LibraryBranch [1] (home/current branch)
```

#### BibliographicRecord ↔ DigitalManifestation
**Conclusion:** **One-to-Many** (e-book vs audiobook)
```
BibliographicRecord (1) ──────→ DigitalManifestation [0..*]
```

#### DigitalManifestation ↔ DigitalLicensePool
**Conclusion:** **One-to-One or Many-to-One** (pool per consortium scope)
```
DigitalManifestation ─────→ DigitalLicensePool [1]
```

---

### Pass 2: Members & Circulation

#### Member ↔ MembershipTier
**Conclusion:** **Many-to-One**
```
Member ─────→ MembershipTier [1]
```

#### Member ↔ Loan
**Conclusion:** **One-to-Many**
```
Member (1) ──────→ Loan [0..*]
```

#### Loan ↔ ItemCopy / DigitalEntitlement
**Conclusion:** **Exactly one target** (mutually exclusive discriminator)
```
Loan ─────→ ItemCopy [0..1]
Loan ─────→ DigitalEntitlement [0..1]
```

#### Member ↔ Hold
**Conclusion:** **One-to-Many**
```
Member (1) ──────→ Hold [0..*]
```

#### Hold ↔ BibliographicRecord & LibraryBranch
**Conclusion:** **Association**
```
Hold ─────→ BibliographicRecord [1]
Hold ─────→ LibraryBranch (pickup) [1]
```

---

### Pass 3: Financials, Rooms, ILL

#### Member ↔ Fine / Payment
**Conclusion:** **One-to-Many**
```
Member (1) ──────→ Fine [0..*]
Payment ────→ Fine allocations [M:N via PaymentAllocation]
```

#### ReadingRoom ↔ RoomBooking
**Conclusion:** **One-to-Many**
```
ReadingRoom (1) ──────→ RoomBooking [0..*]
Member (1) ──────→ RoomBooking [0..*]
```

#### ILLRequest ↔ PartnerLibrary / Member / BibliographicRecord
**Conclusion:** **Association graph**
```
ILLRequest ─────→ PartnerLibrary [1]
ILLRequest ─────→ Member [1]
ILLRequest ─────→ BibliographicRecord [1]
ILLRequest ─────→ ItemCopy (temporary circulating item) [0..1]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| BibliographicRecord → ItemCopy | 1:N | Aggregation |
| BibliographicRecord → Author | M:N | Association |
| BibliographicRecord → Subject | M:N | Association |
| ItemCopy → LibraryBranch | N:1 | Association |
| Member → MembershipTier | N:1 | Association |
| Member → Loan | 1:N | Composition (from member view) |
| Member → Hold | 1:N | Association |
| Loan → ItemCopy | N:1 | Association |
| DigitalEntitlement → DigitalManifestation | N:1 | Association |
| Hold → BibliographicRecord | N:1 | Association |
| ReadingRoom → RoomBooking | 1:N | Composition |
| ILLRequest → PartnerLibrary | N:1 | Association |

---

## 📐 Step 5: Design Class Diagrams (12-18 minutes)

### Class Diagram 1: Enums

```
┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>    │
│ ItemStatus         │  │ LoanStatus         │  │ HoldStatus         │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│ AVAILABLE          │  │ ACTIVE             │  │ ACTIVE             │
│ ON_LOAN            │  │ RETURNED           │  │ FILLED             │
│ ON_HOLD_SHELF      │  │ OVERDUE            │  │ CANCELLED          │
│ IN_TRANSIT         │  │ LOST               │  │ EXPIRED            │
│ IN_REPAIR          │  │ DECLARED_LOST      │  └────────────────────┘
│ WITHDRAWN          │  └────────────────────┘
│ LOST               │
└────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>    │
│ FineType           │  │ ILLStatus          │  │ DigitalFormat      │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│ OVERDUE            │  │ REQUESTED          │  │ EBOOK              │
│ LOST_REPLACEMENT   │  │ SHIPPED            │  │ AUDIOBOOK          │
│ DAMAGED            │  │ ON_LOAN            │  │ MAGAZINE_DIGITAL   │
│ ILL_FEE            │  │ RETURNED           │  └────────────────────┘
│ READING_ROOM_NO_SHOW│ │ DECLINED           │
└────────────────────┘  └────────────────────┘
```

---

### Class Diagram 2: Catalog & Items

```
┌─────────────────────────────────────────────────────────────────────┐
│                     BibliographicRecord                             │
├─────────────────────────────────────────────────────────────────────┤
│ - recordId: String                                                  │
│ - isbn10: String                                                    │
│ - isbn13: String                                                    │
│ - title: String                                                     │
│ - subtitle: String                                                  │
│ - publicationYear: int                                              │
│ - language: String                                                │
│ - suppressed: boolean                                               │
├─────────────────────────────────────────────────────────────────────┤
│ + addContributor(authorId, role, sequence): void                    │
│ + addSubject(subjectId): void                                       │
│ + attachDigital(m: DigitalManifestation): void                      │
└─────────────────────────────────────────────────────────────────────┘
        │ 1                      │ 1
        │                        │
        ▼ *                      ▼ *
┌────────────────────────────┐    ┌─────────────────────────────────────────────┐
│ ItemCopy                   │    │ DigitalManifestation                        │
├────────────────────────────┤    ├─────────────────────────────────────────────┤
│ - barcode: String          │    │ - manifestId: String                        │
│ - branchId: String         │    │ - format: DigitalFormat                   │
│ - recordId: String         │    │ - providerId: String                        │
│ - materialType: MaterialType│   │ - drmPolicyId: String                       │
│ - status: ItemStatus       │    └─────────────────────────────────────────────┘
│ - condition: String        │                    │ 1
│ - callNumber: String       │                    ▼ 1
├────────────────────────────┤            ┌──────────────────┐
│ + markOnLoan()             │            │ DigitalLicensePool│
│ + markAvailable()          │            ├──────────────────┤
│ + markInTransit()          │            │ - totalSeats: int │
└────────────────────────────┘            │ - scope: enum     │
                                          │   BRANCH, NETWORK │
                                          └──────────────────┘
```

---

### Class Diagram 3: Member, Loan, Hold, Fine

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Member                                     │
├─────────────────────────────────────────────────────────────────────┤
│ - memberId: String                                                  │
│ - fullName: String                                                  │
│ - email: String                                                     │
│ - homeBranchId: String                                              │
│ - status: MemberStatus (ACTIVE, SUSPENDED, EXPIRED)                 │
│ - tierId: String                                                    │
│ - balanceDue: Money                                                 │
├─────────────────────────────────────────────────────────────────────┤
│ + canBorrow(policy: LoanPolicyView): boolean                        │
│ + activeLoanCount(): int                                            │
│ + activeHoldCount(): int                                            │
└─────────────────────────────────────────────────────────────────────┘
       │ 1              │ 1
       │                │
       ▼ *              ▼ *
┌──────────────┐  ┌──────────────┐  ┌─────────────────────────────────┐
│ Loan         │  │ Hold         │  │ Fine                            │
├──────────────┤  ├──────────────┤  ├─────────────────────────────────┤
│ - loanId     │  │ - holdId     │  │ - fineId                        │
│ - borrowedAt │  │ - recordId   │  │ - type: FineType                │
│ - dueAt      │  │ - pickupBranch│ │ - amount: Money                 │
│ - renewedCnt │  │ - queuePos   │  │ - waived: Money                 │
│ - status     │  │ - status     │  │ - createdAt                     │
│ - itemBarcode│  │ - expiresAt  │  ├─────────────────────────────────┤
│ - digitalId? │  ├──────────────┤  │ + applyPayment(p: Money): void  │
├──────────────┤  │ + activate() │  └─────────────────────────────────┘
│ + renew(): bool│ └──────────────┘
│ + return(): void
└──────────────┘
```

---

### Class Diagram 4: Services (Application Layer)

```
┌─────────────────────────────────────────────────────────────────────┐
│                     CirculationService                                │
├─────────────────────────────────────────────────────────────────────┤
│ - itemRepo: ItemRepository                                            │
│ - loanRepo: LoanRepository                                            │
│ - holdRepo: HoldRepository                                            │
│ - policyService: PolicyService                                        │
│ - eventBus: DomainEventPublisher                                      │
├─────────────────────────────────────────────────────────────────────┤
│ + checkoutPhysical(memberId, barcode, branchId): Loan               │
│ + returnPhysical(barcode, branchId): ReturnResult                   │
│ + renew(loanId): RenewalResult                                        │
│ + fulfillHoldIfAny(recordId, branchId, barcode): void                 │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     DigitalLendingService                             │
├─────────────────────────────────────────────────────────────────────┤
│ + borrowDigital(memberId, recordId, format): DigitalEntitlement      │
│ + returnDigital(entitlementId): void                                  │
│ + expireDueEntitlements(): void  // batch job                         │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     FineService                                       │
├─────────────────────────────────────────────────────────────────────┤
│ - calculator: FineCalculationStrategy                                 │
├─────────────────────────────────────────────────────────────────────┤
│ + accrueOverdue(loanId): Fine                                       │
│ + assessLost(loanId): Fine                                          │
│ + assessDamaged(loanId, estimate: Money): Fine                      │
│ + waive(fineId, actorId, reason): void                              │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     SearchService                                     │
├─────────────────────────────────────────────────────────────────────┤
│ + search(query: CatalogSearchQuery): SearchResultPage                 │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     ReadingRoomService                                │
├─────────────────────────────────────────────────────────────────────┤
│ + bookRoom(memberId, roomId, slot): RoomBooking                       │
│ + cancelBooking(bookingId): void                                      │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     ILLService                                        │
├─────────────────────────────────────────────────────────────────────┤
│ + createRequest(memberId, recordId, partnerId): ILLRequest            │
│ + markShipped(illId, tracking): void                                  │
│ + close(illId): void                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Reading Room & ILL

```
┌──────────────────────────────┐       ┌──────────────────────────────┐
│ ReadingRoom                  │       │ RoomBooking                  │
├──────────────────────────────┤       ├──────────────────────────────┤
│ - roomId: String             │ 1   * │ - bookingId: String          │
│ - branchId: String           │◆──────│ - memberId: String           │
│ - capacity: int              │       │ - start: Instant             │
│ - amenities: List<String>    │       │ - end: Instant               │
├──────────────────────────────┤       │ - status: BookingStatus      │
│ + isFree(slot): boolean      │       ├──────────────────────────────┤
└──────────────────────────────┘       │ + overlaps(other): boolean   │
                                       └──────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ ILLRequest                                                          │
├─────────────────────────────────────────────────────────────────────┤
│ - illId: String                                                     │
│ - memberId: String                                                  │
│ - recordId: String                                                  │
│ - partnerLibraryId: String                                          │
│ - status: ILLStatus                                                 │
│ - dueAt: Instant                                                    │
│ - feeEstimate: Money                                                │
├─────────────────────────────────────────────────────────────────────┤
│ + advance(to: ILLStatus): void                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │ PartnerLibrary   │
                    ├──────────────────┤
                    │ - code: String   │
                    │ - name: String   │
                    │ - contactEmail   │
                    └──────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-35 minutes)

### Enums & Value Objects

```java
// ItemStatus.java
public enum ItemStatus {
    AVAILABLE, ON_LOAN, ON_HOLD_SHELF, IN_TRANSIT, IN_REPAIR, WITHDRAWN, LOST
}

// LoanStatus.java
public enum LoanStatus {
    ACTIVE, RETURNED, OVERDUE, LOST, DECLARED_LOST
}

// HoldStatus.java
public enum HoldStatus {
    ACTIVE, FILLED, CANCELLED, EXPIRED
}

// FineType.java
public enum FineType {
    OVERDUE, LOST_REPLACEMENT, DAMAGED, ILL_FEE, READING_ROOM_NO_SHOW
}
```

```java
// Money.java (simplified)
public final class Money {
    private final BigDecimal amount;
    private final String currency;

    public Money add(Money o) {
        assertSameCurrency(o);
        return new Money(this.amount.add(o.amount), currency);
    }

    public Money subtract(Money o) {
        assertSameCurrency(o);
        return new Money(this.amount.subtract(o.amount), currency);
    }

    public boolean isGreaterThan(Money o) {
        assertSameCurrency(o);
        return this.amount.compareTo(o.amount) > 0;
    }

    public Money multiply(long scalar) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(scalar)), currency);
    }

    private void assertSameCurrency(Money o) {
        if (!this.currency.equals(o.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    // ctor, getters, ZERO factory
}
```

---

### Loan Policy View (tier + material type)

```java
// LoanPolicyView.java — resolved for a checkout attempt
public class LoanPolicyView {
    private final int maxConcurrentLoans;
    private final int maxConcurrentHolds;
    private final int loanDays;
    private final int graceDays;
    private final int maxRenewals;
    private final Money overdueFinePerDay;
    private final Money lostProcessingFee;
    private final int maxConcurrentDigital;
    private final Money maxOutstandingForBorrow; // suspend borrowing above this balance

    // constructor, getters: getMaxConcurrentLoans(), getMaxOutstandingForBorrow(), ...

    public boolean renewalsExhausted(int renewalCount) {
        return renewalCount >= maxRenewals;
    }
}
```

---

### Member (aggregate sketch)

```java
// Member.java
public class Member {
    private final String memberId;
    private String tierId;
    private MemberStatus status;
    private Money balanceDue;
    private final String homeBranchId;

    private final List<Loan> activeLoans = new ArrayList<>();
    private final List<Hold> activeHolds = new ArrayList<>();

    public boolean canBorrow(LoanPolicyView policy) {
        if (status != MemberStatus.ACTIVE) return false;
        if (balanceDue.isGreaterThan(policy.getMaxOutstandingForBorrow())) return false;
        long physicalLoans = activeLoans.stream().filter(Loan::isPhysical).count();
        return physicalLoans < policy.getMaxConcurrentLoans();
    }

    public boolean canPlaceHold(LoanPolicyView policy) {
        if (status != MemberStatus.ACTIVE) return false;
        return activeHolds.size() < policy.getMaxConcurrentHolds();
    }

    public void registerLoan(Loan loan) {
        activeLoans.add(loan);
    }

    public void closeLoan(Loan loan) {
        activeLoans.removeIf(l -> l.getLoanId().equals(loan.getLoanId()));
    }

    public void addFine(Fine fine) {
        this.balanceDue = this.balanceDue.add(fine.outstanding());
    }
}
```

---

### Circulation: Checkout with Concurrency Control

```java
// CirculationService.java (core logic — pseudocode aligned with interview)
public class CirculationService {
    private final ItemRepository items;
    private final LoanRepository loans;
    private final HoldRepository holds;
    private final MemberRepository members;
    private final PolicyService policyService;
    private final IdGenerator ids;
    private final DomainEventPublisher events;

    public Loan checkoutPhysical(String memberId, String barcode, String branchId) {
        // 1) Lock item row (DB SELECT ... FOR UPDATE or distributed lock)
        ItemCopy item = items.lockByBarcode(barcode);
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new IllegalStateException("Item not available");
        }
        if (!item.getBranchId().equals(branchId)) {
            throw new IllegalStateException("Item not at this branch");
        }

        Member member = members.findById(memberId);
        LoanPolicyView policy = policyService.resolve(member.getTierId(), item.getMaterialType());

        if (!member.canBorrow(policy)) {
            throw new IllegalStateException("Member cannot borrow");
        }

        Instant now = Instant.now();
        Instant due = now.plus(policy.getLoanDays(), ChronoUnit.DAYS);

        Loan loan = Loan.createPhysical(ids.next(), memberId, barcode, now, due);
        item.markOnLoan();

        loans.save(loan);
        items.save(item);
        member.registerLoan(loan);
        members.save(member);

        events.publish(new PhysicalCheckedOut(loan.getLoanId(), memberId, barcode, due));
        return loan;
    }

    public ReturnResult returnPhysical(String barcode, String branchId) {
        ItemCopy item = items.lockByBarcode(barcode);
        Loan loan = loans.findActiveByBarcode(barcode)
                .orElseThrow(() -> new IllegalStateException("No active loan"));

        Instant now = Instant.now();
        loan.markReturned(now);
        item.markAvailable(); // may transition to ON_HOLD_SHELF in next step

        loans.save(loan);
        items.save(item);

        Member member = members.findById(loan.getMemberId());
        member.closeLoan(loan);
        members.save(member);

        // Hold fulfillment: if queue exists at branch, attach item to next hold
        fulfillNextHold(loan.getRecordId(), branchId, barcode);

        events.publish(new PhysicalReturned(loan.getLoanId(), barcode, now));
        return ReturnResult.ok(loan);
    }

    private void fulfillNextHold(String recordId, String branchId, String barcode) {
        holds.findNextActive(recordId, branchId).ifPresent(hold -> {
            ItemCopy item = items.lockByBarcode(barcode);
            item.markOnHoldShelf();
            items.save(item);
            hold.markFilled(Instant.now(), pickupWindowDays(3));
            holds.save(hold);
            events.publish(new HoldReady(hold.getHoldId(), hold.getMemberId(), barcode));
        });
    }

    private int pickupWindowDays(int d) { return d; }
}
```

---

### Fine Calculation Strategy

```java
// FineCalculationStrategy.java
public interface FineCalculationStrategy {
    Money overduePerDay(Loan loan, LoanPolicyView policy, LocalDate asOf);
    Money lostFee(Loan loan, BibliographicRecord record, LoanPolicyView policy);
    Money damagedFee(DamageAssessment assessment, LoanPolicyView policy);
}

// StandardFineStrategy.java
public class StandardFineStrategy implements FineCalculationStrategy {
    @Override
    public Money overduePerDay(Loan loan, LoanPolicyView policy, LocalDate asOf) {
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            return Money.ZERO;
        }
        LocalDate due = loan.getDueAt().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate graceEnd = due.plusDays(policy.getGraceDays());
        if (!asOf.isAfter(graceEnd)) return Money.ZERO;
        long days = ChronoUnit.DAYS.between(graceEnd, asOf);
        return policy.getOverdueFinePerDay().multiply(days);
    }

    @Override
    public Money lostFee(Loan loan, BibliographicRecord record, LoanPolicyView policy) {
        Money replacement = record.getReplacementCost().orElse(Money.of("25.00", "USD"));
        return replacement.add(policy.getLostProcessingFee());
    }

    @Override
    public Money damagedFee(DamageAssessment assessment, LoanPolicyView policy) {
        return assessment.getEstimatedRepairOrReplace();
    }
}
```

---

### Digital Entitlement (license seat)

```java
// DigitalLendingService.java (sketch)
public class DigitalLendingService {
    private final DigitalLicensePoolRepository pools;
    private final DigitalEntitlementRepository ents;
    private final MemberRepository members;
    private final PolicyService policies;

    public DigitalEntitlement borrowDigital(String memberId, String recordId, DigitalFormat format) {
        DigitalManifestation m = pools.findManifestation(recordId, format);
        DigitalLicensePool pool = pools.lockPool(m.getManifestId());

        Member member = members.findById(memberId);
        LoanPolicyView policy = policies.resolve(member.getTierId(), m.getMaterialType());
        if (member.activeDigitalCount() >= policy.getMaxConcurrentDigital()) {
            throw new IllegalStateException("Digital borrow limit reached");
        }
        if (pool.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No license seats available");
        }

        pool.checkoutSeat();
        pools.save(pool);

        Instant until = Instant.now().plus(policy.getLoanDays(), ChronoUnit.DAYS);
        DigitalEntitlement e = DigitalEntitlement.issue(memberId, m.getManifestId(), until);
        ents.save(e);
        return e;
    }

    public void returnDigital(String entitlementId) {
        DigitalEntitlement e = ents.findById(entitlementId);
        DigitalLicensePool pool = pools.lockPool(e.getManifestId());
        pool.returnSeat();
        pools.save(pool);
        e.revoke();
        ents.save(e);
    }
}
```

---

### Reading Room: Overlap Check

```java
// ReadingRoomService.java (core invariant)
public class ReadingRoomService {
    private final RoomBookingRepository bookings;

    public RoomBooking bookRoom(String memberId, String roomId, Instant start, Instant end) {
        if (!end.isAfter(start)) throw new IllegalArgumentException("Invalid slot");
        List<RoomBooking> existing = bookings.findByRoomAndRange(roomId, start, end);
        boolean conflict = existing.stream().anyMatch(b -> overlaps(b, start, end));
        if (conflict) throw new IllegalStateException("Slot unavailable");
        RoomBooking b = RoomBooking.create(memberId, roomId, start, end);
        bookings.save(b);
        return b;
    }

    private boolean overlaps(RoomBooking b, Instant start, Instant end) {
        return b.getStart().isBefore(end) && start.isBefore(b.getEnd());
    }
}
```

---

### Search Query DTO (application)

```java
// CatalogSearchQuery.java
public class CatalogSearchQuery {
    private String keyword;       // title/author/subject generic
    private String isbn;
    private String author;
    private String subject;
    private String branchId;      // optional filter
    private Boolean onlyAvailable;
    private Set<DigitalFormat> formats;
    private int page;
    private int pageSize;
}
```

---

### Demo

```java
// LibraryDemo.java
public class LibraryDemo {
    public static void main(String[] args) {
        // Wire repositories & services (omitted)

        CirculationService circulation = new CirculationService(/* deps */);
        String memberId = "M-1001";
        String barcode = "ITEM-5551212";
        String branchId = "BR-NYC-01";

        Loan loan = circulation.checkoutPhysical(memberId, barcode, branchId);
        System.out.println("Checked out until: " + loan.getDueAt());

        circulation.returnPhysical(barcode, branchId);
        System.out.println("Returned and possibly fulfilled next hold.");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `FineCalculationStrategy`, optional `HoldPrioritizationStrategy` (FIFO vs tier-weighted)  
**Why:** Branch and consortium rules change; avoid hard-coding fee math in entities  
**Interview Justification:** "Regulations and board policies evolve. Strategy keeps the `Member`/`Loan` aggregates stable while swapping rule implementations."

---

### Pattern 2: Domain Events + Event Handler
**Where:** `PhysicalCheckedOut`, `HoldReady`, `LoanReturned`  
**Why:** Trigger notifications, search index updates, analytics without bloating transaction scripts  
**Interview Justification:** "Checkout must stay fast; email and OpenSearch updates can be asynchronous subscribers."

---

### Pattern 3: Repository Pattern
**Where:** `ItemRepository`, `LoanRepository`, `HoldRepository`  
**Why:** Isolate persistence; in-memory fakes for tests  
**Interview Justification:** "Interview code stays clean; ORM details don't leak into domain."

---

### Pattern 4: Specification / Policy Service
**Where:** `PolicyService.resolve(tierId, materialType)`  
**Why:** Centralize limits that depend on two dimensions  
**Interview Justification:** "Avoid scattering `if (tier == STUDENT)` across services."

---

### Pattern 5: State Pattern (optional refinement)
**Where:** `ILLRequest.advance(ILLStatus)` with validated transitions  
**Why:** Prevent illegal jumps (e.g., RETURNED → REQUESTED)  
**Interview Justification:** "ILL is a workflow; explicit transitions reduce operational mistakes."

---

## 💡 Step 8: Interview Discussion Points

### 1. Concurrency: Same Copy, Two Desks

**Interviewer:** "Two librarians try to check out the same book at the same time. How do you handle it?"

**Answer:**
"**Treat `ItemCopy` as the consistency choke point.**

1. **Database:** `UPDATE item_copy SET status='ON_LOAN' WHERE barcode=? AND status='AVAILABLE'` — check affected rows = 1.  
2. **Alternative:** pessimistic lock `SELECT ... FOR UPDATE` on the item row inside the checkout transaction.  
3. **Distributed systems:** short-lived distributed lock keyed by `barcode` if shards span services.

The loan row is created only after the item flip succeeds. If conflict, second transaction fails fast with 'already checked out'."

---

### 2. Hold Fairness vs Priority

**Interviewer:** "Should faculty skip the line?"

**Answer:**
"**Configurable `HoldPrioritizationStrategy`.**

- **FIFO** default for public fairness.  
- **Weighted queue:** compute score = `enqueueTime + tierBoost(tier)` or strict priority queues merged at dequeue (watch starvation — cap how many high-priority can jump).  
- **Transparency:** show approximate wait position in patron UI.

I would start FIFO in LLD, then show how Strategy swaps implementations per library board policy."

---

### 3. Search: Relational vs Index

**Interviewer:** "How do you implement search across millions of rows?"

**Answer:**
"**Dual-write pattern:**

- **System of record:** normalized catalog tables.  
- **Search:** denormalized documents in OpenSearch with fields `title`, `authors[]`, `isbn`, `subjects[]`, `branchAvailability{}`, `formats[]`.  
- **Updates:** on catalog change, emit event → indexer updates doc.  
- **Availability:** can be approximate (eventual) with TTL refresh job every N minutes for high-traffic branches if needed.

For interview scope, emphasize **strong consistency for circulation**, **eventual for search**."

---

### 4. Digital Seats vs Physical Copies

**Interviewer:** "How is digital different in the domain model?"

**Answer:**
"**Same patron-facing `Loan` concept can unify or split:**

- **Unified `Loan` with discriminator** `PHYSICAL | DIGITAL` — simpler reporting.  
- **Separate `DigitalEntitlement`** — clearer invariants (no `IN_TRANSIT`, DRM provider callbacks).

License pool is an **integer counter** with checkout/return matching seat acquire/release. Expiry job runs hourly to `returnDigital` and free seats idempotently."

---

### 5. ILL Edge Cases

**Interviewer:** "What if the partner never ships?"

**Answer:**
"**SLA timers and escalation states.**

- States: REQUESTED → (timeout) → DECLINED or ESCALATED.  
- Patron notification at each transition.  
- Optional automatic hold on local copy if one appears mid-ILL.  
- Fees: some consortia charge on ship; others on receive — model `feeTrigger` in `PartnerAgreement` value object.

Keep ILL item as special `ItemType` with `PartnerLibrary` ownership metadata for clear due-date extensions."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `CirculationService`: physical checkout/return orchestration only
- `DigitalLendingService`: license seats and entitlements only
- `FineService`: monetary assessments and waivers
- `ReadingRoomService`: room slot conflicts only

### Open/Closed ✅
```java
public class HolidayFineStrategy implements FineCalculationStrategy {
    // new rules without modifying FineService body
}
```

### Liskov Substitution ✅
```java
FineCalculationStrategy s = new StandardFineStrategy();
s = new HolidayFineStrategy();
// FineService remains correct if contracts honored
```

### Interface Segregation ✅
```java
interface ItemReadRepository { Optional<ItemCopy> findByBarcode(String b); }
interface ItemLockRepository { ItemCopy lockByBarcode(String b); }
// Read paths avoid exposing locking to reporting services
```

### Dependency Inversion ✅
```java
public class FineService {
    private final FineCalculationStrategy calculator;
    public FineService(FineCalculationStrategy calculator) {
        this.calculator = calculator;
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Bibliographic record** separated from **ItemCopy**; digital is a parallel manifestation with **license pools**
- ✅ **Strong consistency** on circulation paths; **event-driven** search and notifications
- ✅ **PolicyService + strategies** for fines, holds, and loan rules
- ✅ **Branch-scoped** inventory with transfers and ILL workflows

### Core Flows
- ✅ Checkout/return with **item-level locking** and **hold fulfillment** on return
- ✅ **Fines** for overdue, lost, damaged with **audited waivers**
- ✅ **Reading room** bookings with **interval overlap** detection
- ✅ **Reports** off read models or async jobs to protect OLTP latency

### Interview Hooks
- ✅ Concurrency on last available copy
- ✅ Search denormalization vs transactional store
- ✅ Digital seat management and expiry
- ✅ ILL state machine and partner agreements

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
