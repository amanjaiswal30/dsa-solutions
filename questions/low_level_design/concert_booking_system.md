# Low-Level Design: Concert / Event Booking System

**Difficulty:** Hard 🔥

**Interview Duration:** 75–120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

**Standard sections (1–13)** covered in this LLD:

| # | Section |
|---|---------|
| 1 | Understand the Problem |
| 2 | Gather Requirements |
| 3 | Identify Core Entities |
| 4 | Establish Relationships |
| 5 | Design Class Diagrams |
| 6 | Core Implementation |
| 7 | Design Patterns Applied |
| 8 | Concurrency Handling |
| 9 | Database Schema |
| 10 | Interview Discussion Points |
| 11 | SOLID Principles Verification |
| 12 | Complexity Analysis |
| 13 | Key Takeaways |

---

## 🎯 Step 1: Understand the Problem (6–10 minutes)

### What the Interviewer Says:
*"Design a ticket booking platform (similar to Ticketmaster or StubHub) that supports many event types, reserved and general-admission seating, high-concurrency purchases, dynamic pricing, resale, and validation at the venue."*

### Clarifying Questions to Ask:

1. **Q:** What event categories are in scope (concerts, sports, theater)?  
   **A:** All three; each event has type-specific metadata (e.g., teams, cast, headliners) but shares the same booking and inventory model.

2. **Q:** Is every event seated, or do we support general admission (GA) and mixed layouts?  
   **A:** Support GA (capacity pool), reserved seats (section/row/seat), and hybrid (e.g., floor GA + reserved bowl).

3. **Q:** How long should a seat hold last before checkout timeout?  
   **A:** Configurable per event or promoter (e.g., 5–15 minutes); release inventory on expiry.

4. **Q:** What consistency guarantee is required for seat assignment?  
   **A:** At most one successful booking per seat per showtime; no double-selling under concurrent load.

5. **Q:** Are payments in scope as full implementation or integration boundary?  
   **A:** Model payment intent, authorize/capture, refunds, and idempotency; actual PSP is an external adapter.

6. **Q:** Should resale be peer-to-peer with platform fee, or only official transfer?  
   **A:** Both: optional secondary marketplace with price caps/rules per jurisdiction, plus free/nominal transfer between accounts.

7. **Q:** How does dynamic pricing work?  
   **A:** Base price per tier + rules (demand, velocity, inventory remaining, time-to-event); bounded surge multipliers.

8. **Q:** Waitlist behavior when sold out?  
   **A:** Queue per event/showtime; notify on cancellation, release, or extra capacity; optional auto-purchase with stored payment.

9. **Q:** Ticket format for validation?  
   **A:** Signed QR and/or barcode with rotating nonce or short TTL for screenshot resistance; offline-capable validation with sync.

10. **Q:** Scale expectations?  
    **A:** Thousands of concurrent users targeting the same hot on-sale; read-heavy search, write bursts on inventory rows.

11. **Q:** Multi-venue and timezone?  
    **A:** Yes; showtimes stored in venue timezone; display in user locale.

12. **Q:** Reporting and compliance?  
    **A:** Sales, attendance (scans), popular events; audit trail for price changes and transfers; PII minimization for tickets.

---

## 🔹 Step 2: Gather Requirements (10–14 minutes)

### Functional Requirements

#### Event Catalog & Metadata (FR1–FR8)
1. System shall support event types: `CONCERT`, `SPORTS`, `THEATER` with extensible type-specific attributes (artist, league/teams, production/company).
2. Promoter or admin shall create and publish events with one or more **showtimes** (start/end, doors open, status: draft/on-sale/sold-out/cancelled).
3. Each showtime shall reference a **venue** and a **seating configuration** snapshot (layout version used for that show).
4. System shall expose search and browse by city, date range, category, artist/team name, and price band.
5. Event pages shall show tier-level availability summaries and pricing (subject to dynamic rules).
6. System shall support event cancellation and mass refund workflow (async with notifications).
7. Media and marketing copy shall be attachable per event (not core to booking engine).
8. System shall support presale access codes / fan club lists (entitlement to buy before public on-sale).

#### Venue & Seating Layout (FR9–FR16)
9. Venue shall have address, timezone, capacity metadata, and accessibility notes.
10. **Seating chart** shall define **sections** (e.g., VIP, Floor, Balcony), optional **rows**, and **seats** with coordinates for UI.
11. Seats shall have attributes: tier code, obstructed view, wheelchair companion, aisle.
12. **General admission** areas shall map to `AdmissionZone` with max capacity and optional oversell policy (admin-controlled).
13. Layout changes shall be versioned; existing showtimes keep immutable `layoutVersionId`.
14. Admin shall block or hold seats for production, ADA holds, or promoter holds.
15. System shall support mixed maps: reserved sections + GA pods on same chart.
16. Venue shall support multiple named configurations (e.g., end-stage vs center stage) reused across showtimes.

#### Seat Inventory Lifecycle (FR17–FR24)
17. Each sellable unit (seat instance or GA capacity token) shall have state: `AVAILABLE`, `HELD`, `BOOKED`, `BLOCKED`, `TRANSFER_PENDING`.
18. **Hold** shall be created on user seat selection with TTL; extending TTL may be allowed once per session (policy).
19. On successful payment, held units shall transition to `BOOKED`; on timeout or abandon, to `AVAILABLE`.
20. Inventory operations shall be atomic per showtime and seat (or GA decrement).
21. System shall prevent over-selling below configured GA oversell tolerance.
22. Admin **inventory adjustment** (release holds, add comp seats) shall be audited.
23. **Waitlist** shall enqueue users when no inventory; dequeue on notification with time-bounded purchase window.
24. Real-time availability shown to users may be eventually consistent; **checkout path** must be strongly consistent.

#### Booking & Checkout (FR25–FR32)
25. User shall search → select showtime → pick seats or GA quantity → see line items with fees/taxes.
26. System shall quote **final price** using current dynamic pricing snapshot at hold time (policy: lock price at hold vs at payment—clarify; below we **lock at hold** for fairness).
27. Checkout shall integrate **payment authorize** then **capture** on commit (or single-phase if small amounts).
28. Order shall be idempotent via `idempotency_key` per client session.
29. User shall receive confirmation with order id and ticket delivery method (mobile wallet, email PDF—interface level).
30. Partial failure (payment succeeds, ticketing fails) shall drive **reconciliation** and automatic refund or compensating ticket issue.
31. Cart abandonment analytics shall be out-of-band (async events).
32. Purchase limits per user per showtime (bot/fraud mitigation) shall be enforceable.

#### Pricing Tiers & Dynamic Pricing (FR33–FR38)
33. Base **price tiers** map to sections/zones: VIP, Floor, Balcony, GA, etc.
34. **Dynamic pricing engine** shall consume signals: % sold, sales velocity, time to show, competitor anchors (optional), and event tags.
35. Surge multipliers shall have min/max caps per event and regulatory constraints.
36. Fees (service, facility) and taxes shall compose transparently on checkout breakdown.
37. Promotions (percent off, BOGO) shall stack per rules without breaking inventory invariants.
38. Price history shall be logged for support and disputes.

#### Waitlist & Notifications (FR39–FR42)
39. User shall join waitlist for a showtime with preferred tier/section (optional).
40. On inventory becoming available, system shall notify next eligible waitlist entries (FIFO with fairness jitter to avoid thundering herd).
41. Notifications shall support email/SMS/push via `NotificationService` abstraction.
42. User may opt out; compliance with marketing consent.

#### Ticket Generation & Delivery (FR43–FR47)
43. Each **ticket** shall have unique `ticketId`, barcode payload, and signed QR secret.
44. QR shall embed `showtimeId`, `ticketId`, `nonce`, `expiresAt` or support **rotating OTP** via mobile app.
45. PDF/Apple Wallet / Google Pay adapters are pluggable **delivery strategies**.
46. Ticket shall carry attendee name (optional), tier, seat/GA zone, and transfer restrictions flag.
47. **Bulk issuance** for group orders shall be transactional with parent `orderId`.

#### Resale & Transfer (FR48–FR52)
48. **Official transfer:** sender initiates transfer; recipient accepts; ticket re-bound to recipient account; audit trail.
49. **Marketplace resale:** seller lists at price ≤ max resale cap; buyer purchases; platform fee deducted; original barcode invalidated, new ticket issued (new signing key segment).
50. Transfer shall be blocked after event start or per promoter policy.
51. Fraud checks on resale velocity and account linkage.
52. Refund to original payment method when resale cancelled before completion.

#### Payment Processing (FR53–FR57)
53. `PaymentIntent` states: `CREATED`, `AUTHORIZED`, `CAPTURED`, `FAILED`, `REFUNDED`, `PARTIALLY_REFUNDED`.
54. Webhooks from PSP shall update payment state idempotently.
55. Partial captures (split with venue/promoter) can be modeled as ledger entries (simplified in LLD).
56. Chargebacks shall link to `orderId` and may void ticket validity.
57. Stored payment for waitlist auto-buy shall tokenize card via PSP only (PCI scope minimization).

#### Validation at Venue (FR58–FR61)
58. Scanner app shall validate ticket signature, showtime match, and **first scan wins** (anti-reuse).
59. Offline mode: cached allow-list of valid ticket ids + last sync time; conflict resolution on reconnect.
60. Support **re-entry** flag per ticket type (hand stamp / same-day re-scan window).
61. Scan events feed **attendance** reports in near real time.

#### Reporting & Analytics (FR62–FR66)
62. Sales by event, showtime, tier, channel; gross vs net.
63. Attendance vs sold; no-show rate; scan time histogram.
64. Popular events ranking (velocity, sell-through, geographic demand).
65. Operational dashboards for holds expiring, payment failures, waitlist depth.
66. Export for finance (CSV) with role-based access.

### Non-Functional Requirements — Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "Hot on-sales, many readers, bursty writers on same showtime."
- Millions of catalog reads; 10³–10⁴ concurrent checkout attempts on blockbuster drops
- Seat-level rows or partition-per-showtime for inventory

**Deduced NFRs:**
- ✅ Horizontal scale of stateless API tier; CDN for static chart assets
- ✅ Read replicas or search index for discovery; **primary + sharding** for hot inventory
- ✅ **Partition by `showtimeId`** for inventory and order streams
- ✅ Rate limiting, bot detection, and virtual waiting room (product layer) in front of on-sale

---

#### 2. **Consistency Analysis**

**Think:** "What must never be wrong?"
- Same seat sold twice; GA count negative; payment captured without ticket

**Deduced NFRs:**
- ✅ **Strong consistency** on hold → book transition (transaction or compare-and-swap with fencing)
- ✅ **Idempotent** payment and order creation
- ✅ **Exactly-once ticket issuance** per successful payment (unique constraint on `orderId` + line index)
- ✅ Eventual consistency acceptable for search facets and aggregate availability banners with SLA (e.g., < 5s)

---

#### 3. **Availability Analysis**

**Think:** "What can degrade during drop or payment outage?"
- Browse/search should stay up; checkout may queue or return retryable errors

**Deduced NFRs:**
- ✅ **99.9%+** on read path; **99.95%** on commit path during non-peak
- ✅ Circuit breakers to PSP; queue webhook retries
- ✅ Graceful degradation: disable resale, keep primary sale read-only mode if inventory DB partitioned failover

---

#### 4. **Maintainability Analysis**

**Think:** "Pricing rules change; new event types; regional laws."
- Feature flags for surge, resale caps; configurable hold TTL

**Deduced NFRs:**
- ✅ Audit logs for price, inventory, transfers
- ✅ Admin APIs and **strategy injection** for pricing rules
- ✅ Dark launch of new layout editor with versioned snapshots

---

#### 5. **Performance Analysis**

**Think:** "Latency expectations?"
- Search p95 < 200ms (cached/indexed)
- Hold creation p95 < 150ms; payment round-trip dominates
- Validation scan < 100ms online

**Deduced NFRs:**
- ✅ In-memory cache of **tier availability counts** per showtime with TTL + invalidation on commit
- ✅ Avoid global locks; **per-seat or per-showtime partition** serialization
- ✅ Batch chart loading; lazy load seat map tiles

---

#### 6. **Security Analysis**

**Think:** "Forged tickets, scalping bots, account takeover."
- Signed tickets; rate limits; step-up auth for high-value transfer

**Deduced NFRs:**
- ✅ **HMAC or asymmetric** signing for QR payload; nonce store for one-time scan (or rolling TOTP)
- ✅ **PII** encryption at rest; minimize data on ticket surface
- ✅ OAuth2 for APIs; RBAC for admin/scanner roles
- ✅ Monitoring for abnormal purchase patterns

---

## 🧩 Step 3: Identify Core Entities (12–16 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Event types, showtimes" | Event, Showtime, EventType, Promoter |
| "Venue, seating chart" | Venue, SeatingChart, Section, Row, Seat, AdmissionZone |
| "Hold, book seat" | SeatInventory, Hold, BookingSession, Order |
| "Dynamic pricing" | PriceTier, PriceQuote, PricingRule, SurgePolicy |
| "Waitlist" | WaitlistEntry, Notification |
| "QR, barcode" | Ticket, TicketArtifact, SigningKey |
| "Resale, transfer" | Listing, Transfer, MarketplaceOrder |
| "Payment" | PaymentIntent, LedgerEntry |
| "Validate at gate" | ScanRecord, ValidationDevice |
| "Reports" | SalesReport, AttendanceMetric |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Event | ✅ YES | Aggregate root for catalog |
| Showtime | ✅ YES | Sellable instance with inventory |
| Venue | ✅ YES | Physical place, timezone |
| SeatingChart | ✅ YES | Versioned layout |
| Section / Row / Seat | ✅ YES | Structural; Seat is smallest reserved unit |
| AdmissionZone | ✅ YES | GA capacity bucket |
| SeatInventory | ✅ YES | State per (showtime, seat or zone) |
| Hold | ✅ YES | TTL lease on inventory |
| Order | ✅ YES | Commercial aggregate |
| OrderLine | ✅ YES | Line items referencing seats/zones |
| Ticket | ✅ YES | Fulfillment + validation |
| PriceQuote | ❌ NO | Value object / snapshot on Hold |
| PricingRule | ✅ YES | Config-driven behavior |
| WaitlistEntry | ✅ YES | Queue position |
| PaymentIntent | ✅ YES | Payment lifecycle |
| Transfer / Listing | ✅ YES | Resale domain |
| ScanRecord | ✅ YES | Attendance evidence |

### Final Entity List

**Catalog:** `Promoter`, `Artist` (or `Performer`), `Event`, `Showtime`, `EventType`  
**Venue & layout:** `Venue`, `SeatingChart`, `LayoutVersion`, `Section`, `Row`, `Seat`, `AdmissionZone`  
**Inventory:** `SeatInventory`, `Hold`, `BookingSession`  
**Commerce:** `Order`, `OrderLine`, `FeeBreakdown`, `PaymentIntent`  
**Pricing:** `PriceTier`, `PricingEngine`, `DynamicPricingContext`, `SurgePolicy`  
**Fulfillment:** `Ticket`, `TicketDelivery`, `TicketSigner`  
**Secondary market:** `TransferRequest`, `ResaleListing`, `MarketplaceTransaction`  
**Engagement:** `WaitlistEntry`, `Notification`  
**Operations:** `ValidationTerminal`, `ScanRecord`, `InventoryAuditLog`  
**Reporting (views):** `SalesSnapshot`, `AttendanceAggregate` (materialized)

---

## 🔗 Step 4: Establish Relationships (14–18 minutes)

### Pass 1: Catalog

#### Event ↔ Showtime  
**Composition (logical):** Event owns showtimes for lifecycle.  
```
Event ◆────→ Showtime [1..*]
```

#### Showtime ↔ Venue  
**Association:** Each showtime occurs at one venue.  
```
Showtime ─────→ Venue [1]
```

#### Showtime ↔ LayoutVersion  
**Association:** Immutable snapshot for ticketing.  
```
Showtime ─────→ LayoutVersion [1]
```

---

### Pass 2: Layout

#### SeatingChart ↔ LayoutVersion  
**Composition:** Chart publishes immutable versions.  
```
SeatingChart ◆────→ LayoutVersion [1..*]
```

#### LayoutVersion ↔ Section ↔ Row ↔ Seat  
**Composition tree:**  
```
LayoutVersion ◆────→ Section [1..*] ◆────→ Row [0..*] ◆────→ Seat [1..*]
LayoutVersion ◆────→ AdmissionZone [0..*]
```

---

### Pass 3: Inventory & Orders

#### Showtime ↔ SeatInventory  
**Composition:** Inventory rows created per showtime + seat/zone.  
```
Showtime ◆────→ SeatInventory [1..*]
```

#### Hold ↔ SeatInventory  
**Association:** Hold references one or more inventory cells (GA may decrement quantity).  
```
Hold ─────→ SeatInventory [1..*]
```

#### Order ↔ OrderLine ↔ Ticket  
```
Order ◆────→ OrderLine [1..*] ─────→ Ticket [0..1 per line]
Order ─────→ PaymentIntent [1..*]
```

---

### Pass 4: Secondary Market

```
Ticket ─────→ TransferRequest [0..*]
Ticket ─────→ ResaleListing [0..1]
ResaleListing ─────→ MarketplaceTransaction [0..1]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Event → Showtime | 1:N | Composition |
| Showtime → Venue | N:1 | Association |
| Showtime → LayoutVersion | N:1 | Association |
| LayoutVersion → Seat | 1:N | Composition |
| Showtime → SeatInventory | 1:N | Composition |
| Hold → SeatInventory | 1:N | Association |
| Order → OrderLine | 1:N | Composition |
| OrderLine → Ticket | 1:1 | Composition |
| User → WaitlistEntry | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (16–22 minutes)

### Class Diagram 1: Enums

```
┌────────────────────┐  ┌────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>   │  │ <<enumeration>>    │  │ <<enumeration>>     │
│   EventType        │  │  InventoryState    │  │   OrderStatus       │
├────────────────────┤  ├────────────────────┤  ├─────────────────────┤
│ CONCERT            │  │ AVAILABLE          │  │ DRAFT               │
│ SPORTS             │  │ HELD               │  │ PAYMENT_PENDING     │
│ THEATER            │  │ BOOKED             │  │ CONFIRMED           │
└────────────────────┘  │ BLOCKED            │  │ CANCELLED           │
                        │ TRANSFER_PENDING   │  │ REFUNDED            │
                        └────────────────────┘  └─────────────────────┘

┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>     │
│  PaymentState      │  │  ShowtimeStatus      │
├────────────────────┤  ├────────────────────┤
│ CREATED            │  │ DRAFT                │
│ AUTHORIZED         │  │ ON_SALE              │
│ CAPTURED           │  │ SOLD_OUT             │
│ FAILED             │  │ CANCELLED            │
│ REFUNDED           │  └────────────────────┘
└────────────────────┘
```

---

### Class Diagram 2: Event & Showtime

```
┌────────────────────────────────────────────────────────────────┐
│                         Event                                   │
├────────────────────────────────────────────────────────────────┤
│ - eventId: String                                              │
│ - promoterId: String                                           │
│ - type: EventType                                              │
│ - title: String                                                │
│ - description: String                                          │
│ - metadata: EventMetadata (teams, cast, artists)               │
│ - status: EventStatus                                          │
├────────────────────────────────────────────────────────────────┤
│ + addShowtime(st: Showtime): void                              │
│ + publish(): void                                              │
└────────────────────────────────────────────────────────────────┘
                              │
                              │ 1..*
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                       Showtime                                  │
├────────────────────────────────────────────────────────────────┤
│ - showtimeId: String                                           │
│ - eventId: String                                              │
│ - venueId: String                                              │
│ - layoutVersionId: String                                      │
│ - startsAt: Instant                                            │
│ - endsAt: Instant                                              │
│ - doorsAt: Instant                                             │
│ - status: ShowtimeStatus                                       │
│ - onSaleAt: Instant                                            │
├────────────────────────────────────────────────────────────────┤
│ + isOnSale(now: Instant): boolean                              │
└────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Venue & Seating

```
┌──────────────────────────┐       ┌─────────────────────────────┐
│         Venue            │       │      SeatingChart           │
├──────────────────────────┤       ├─────────────────────────────┤
│ - venueId                │◆──────│ - chartId                   │
│ - name, timezone, addr   │  1    │ - venueId                   │
└──────────────────────────┘       ├─────────────────────────────┘
                                   │ + publishVersion(): LayoutVersion │
                                   └─────────────────────────────┘
                                                  │
                                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      LayoutVersion                               │
├─────────────────────────────────────────────────────────────────┤
│ - versionId                                                      │
│ - sections: List<Section>                                        │
│ - zones: List<AdmissionZone>                                     │
└─────────────────────────────────────────────────────────────────┘
         │                                    │
         │ 1..*                               │ 0..*
         ▼                                    ▼
┌─────────────────┐                  ┌─────────────────────┐
│    Section      │                  │   AdmissionZone     │
├─────────────────┤                  ├─────────────────────┤
│ - sectionId     │                  │ - zoneId            │
│ - tierCode      │                  │ - tierCode          │
│ - rows: List<Row>                  │ - capacity: int     │
└─────────────────┘                  └─────────────────────┘
         │
         ▼
┌─────────────────┐
│      Row        │
├─────────────────┤
│ - rowId         │
│ - seats: List<Seat>                │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│      Seat       │
├─────────────────┤
│ - seatId        │
│ - label (e.g. 12)│
│ - attributes    │
└─────────────────┘
```

---

### Class Diagram 4: Inventory, Hold, Order

```
┌─────────────────────────────────────────────────────────────────┐
│                     SeatInventory                                │
├─────────────────────────────────────────────────────────────────┤
│ - inventoryId                                                    │
│ - showtimeId                                                   │
│ - seatId: Optional<String>   // null for GA aggregate row       │
│ - zoneId: Optional<String>                                      │
│ - state: InventoryState                                         │
│ - holdId: Optional<String>                                      │
│ - version: long            // optimistic locking                 │
├─────────────────────────────────────────────────────────────────┤
│ + tryTransition(to, expectedVersion): boolean                   │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ referenced by
┌─────────────────────────────────────────────────────────────────┐
│                          Hold                                    │
├─────────────────────────────────────────────────────────────────┤
│ - holdId                                                         │
│ - userId / sessionId                                             │
│ - showtimeId                                                     │
│ - seatIds / zoneId + qty                                       │
│ - priceSnapshot: Money                                           │
│ - expiresAt: Instant                                             │
│ - fencingToken: long                                            │
├─────────────────────────────────────────────────────────────────┤
│ + isExpired(now: Instant): boolean                              │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Order                                    │
├─────────────────────────────────────────────────────────────────┤
│ - orderId                                                        │
│ - userId                                                         │
│ - showtimeId                                                     │
│ - status: OrderStatus                                            │
│ - lines: List<OrderLine>                                         │
│ - idempotencyKey: String                                         │
├─────────────────────────────────────────────────────────────────┤
│ + confirm(): void                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Ticket & Validation

```
┌─────────────────────────────────────────────────────────────────┐
│                        Ticket                                    │
├─────────────────────────────────────────────────────────────────┤
│ - ticketId                                                       │
│ - orderLineId                                                    │
│ - showtimeId                                                     │
│ - seatId / zoneId                                                │
│ - barcodePayload: String                                         │
│ - qrPayload: SignedTicketPayload                                │
│ - status: ALIVE | VOID | TRANSFERRED | USED                      │
├─────────────────────────────────────────────────────────────────┤
│ + voidAndReissue(): Ticket  // resale flow                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   TicketValidationService                        │
├─────────────────────────────────────────────────────────────────┤
│ + validateScan(payload, terminalId): ScanResult                 │
│ + recordFirstEntry(ticketId): boolean                           │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Pricing (Strategy)

```
┌─────────────────────────────────────────────────────────────────┐
│              <<interface>> PricingEngine                         │
├─────────────────────────────────────────────────────────────────┤
│ + quote(ctx: DynamicPricingContext): TierPriceMap               │
└─────────────────────────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┴────────────┐
        ▼                        ▼
┌──────────────────┐    ┌──────────────────────┐
│ RuleBasedEngine  │    │  SurgePricingDecorator │
├──────────────────┤    ├──────────────────────┤
│ + quote()        │    │ - delegate: Engine   │
└──────────────────┘    │ + quote() // wraps   │
                        └──────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35–45 minutes)

### Value objects & enums

```java
// InventoryState.java
public enum InventoryState {
    AVAILABLE, HELD, BOOKED, BLOCKED, TRANSFER_PENDING
}

// Money.java (sketch)
public record Money(BigDecimal amount, String currency) {
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor).setScale(2, RoundingMode.HALF_UP), currency);
    }
}
```

---

### Seat hold with fencing token (in-memory sketch; DB maps columns)

```java
// Hold.java
public class Hold {
    private final String holdId;
    private final String userId;
    private final String showtimeId;
    private final List<String> seatIds;
    private final String zoneId;
    private final int gaQuantity;
    private final Map<String, Money> lockedTierPrices;
    private final Instant expiresAt;
    private final long fencingToken;

    public Hold(String holdId, String userId, String showtimeId, List<String> seatIds,
                String zoneId, int gaQuantity, Map<String, Money> lockedTierPrices,
                Instant expiresAt, long fencingToken) {
        this.holdId = holdId;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.zoneId = zoneId;
        this.gaQuantity = gaQuantity;
        this.lockedTierPrices = lockedTierPrices;
        this.expiresAt = expiresAt;
        this.fencingToken = fencingToken;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public long getFencingToken() { return fencingToken; }
    public String getHoldId() { return holdId; }
    public List<String> getSeatIds() { return seatIds; }
}
```

---

### Inventory service: pessimistic path (conceptual)

```java
// SeatInventoryService.java — core interview logic
public class SeatInventoryService {
    private final SeatInventoryRepository repo;
    private final Clock clock;
    private final HoldRepository holds;

    /**
     * Attempt to hold seats for TTL. Returns empty if any seat lost race.
     */
    public Optional<Hold> tryHoldSeats(String userId, String showtimeId,
                                       List<String> seatIds, Duration ttl,
                                       PricingEngine pricing) {
        Instant now = clock.instant();
        return repo.transaction(() -> {
            List<SeatInventoryRow> rows = repo.lockSeatsForUpdate(showtimeId, seatIds);
            if (rows.size() != seatIds.size()) return Optional.empty();

            for (SeatInventoryRow r : rows) {
                if (!r.isHoldable(now)) return Optional.empty();
            }

            long fence = ThreadLocalRandom.current().nextLong();
            String holdId = UUID.randomUUID().toString();
            Instant exp = now.plus(ttl);

            for (SeatInventoryRow r : rows) {
                r.transitionToHeld(holdId, fence, exp, now);
            }
            repo.updateAll(rows);

            var prices = pricing.quote(new DynamicPricingContext(showtimeId, seatIds, now));
            Hold hold = new Hold(holdId, userId, showtimeId, seatIds, null, 0, prices, exp, fence);
            holds.save(hold);
            return Optional.of(hold);
        });
    }

    /**
     * Commit hold to booked after payment — must match fencing token.
     */
    public boolean commitHold(String holdId, long fencingToken) {
        return repo.transaction(() -> {
            Hold h = holds.findByIdForUpdate(holdId);
            if (h == null || h.isExpired(clock.instant())) return false;
            if (h.getFencingToken() != fencingToken) return false;

            List<SeatInventoryRow> rows = repo.lockSeatsForUpdate(h.getShowtimeId(), h.getSeatIds());
            for (SeatInventoryRow r : rows) {
                if (!r.isHeldBy(holdId, fencingToken)) return false;
                r.transitionToBooked();
            }
            repo.updateAll(rows);
            holds.markConsumed(holdId);
            return true;
        });
    }
}
```

```java
// SeatInventoryRow.java (entity sketch)
public class SeatInventoryRow {
    private String inventoryId;
    private String showtimeId;
    private String seatId;
    private InventoryState state;
    private String activeHoldId;
    private long holdFence;
    private Instant holdExpiresAt;
    private long version;

    public boolean isHoldable(Instant now) {
        if (state == InventoryState.BLOCKED || state == InventoryState.BOOKED) return false;
        if (state == InventoryState.AVAILABLE) return true;
        if (state == InventoryState.HELD && holdExpiresAt != null && now.isAfter(holdExpiresAt))
            return true; // lazy expire in transaction
        return false;
    }

    public void transitionToHeld(String holdId, long fence, Instant exp, Instant now) {
        if (state == InventoryState.HELD && holdExpiresAt != null && now.isAfter(holdExpiresAt)) {
            state = InventoryState.AVAILABLE;
        }
        if (state != InventoryState.AVAILABLE) throw new IllegalStateException();
        this.state = InventoryState.HELD;
        this.activeHoldId = holdId;
        this.holdFence = fence;
        this.holdExpiresAt = exp;
        this.version++;
    }

    public void transitionToBooked() {
        this.state = InventoryState.BOOKED;
        this.activeHoldId = null;
        this.version++;
    }

    public boolean isHeldBy(String holdId, long fence) {
        return state == InventoryState.HELD
                && Objects.equals(this.activeHoldId, holdId)
                && this.holdFence == fence;
    }
}
```

---

### GA inventory (quantity decrement)

```java
// GaInventoryService.java
public class GaInventoryService {
    public Optional<Hold> tryHoldGa(String userId, String showtimeId, String zoneId,
                                    int qty, Duration ttl, PricingEngine pricing) {
        Instant now = clock.instant();
        return repo.transaction(() -> {
            GaBucketRow bucket = repo.lockZoneForUpdate(showtimeId, zoneId);
            if (bucket.getAvailable(now) < qty) return Optional.empty();

            long fence = ThreadLocalRandom.current().nextLong();
            String holdId = UUID.randomUUID().toString();
            bucket.addHold(holdId, qty, fence, now.plus(ttl));
            repo.save(bucket);

            var prices = pricing.quoteGa(showtimeId, zoneId, qty, now);
            Hold hold = new Hold(holdId, userId, showtimeId, List.of(), zoneId, qty, prices, now.plus(ttl), fence);
            holds.save(hold);
            return Optional.of(hold);
        });
    }
}
```

---

### Order orchestration (idempotent)

```java
// CheckoutService.java
public class CheckoutService {
    private final SeatInventoryService seats;
    private final PaymentClient payments;
    private final TicketIssuer tickets;
    private final OrderRepository orders;

    public CheckoutResult checkout(CheckoutCommand cmd) {
        Optional<Order> existing = orders.findByIdempotencyKey(cmd.idempotencyKey());
        if (existing.isPresent()) return CheckoutResult.duplicate(existing.get());

        Hold hold = holds.get(cmd.holdId());
        if (hold == null || hold.isExpired(clock.instant()))
            return CheckoutResult.failed("HOLD_EXPIRED");

        Money total = computeTotal(hold, cmd.fees());
        PaymentIntent pi = payments.authorize(cmd.userId(), total, cmd.paymentMethodToken(), cmd.idempotencyKey());

        if (pi.getState() != PaymentState.AUTHORIZED)
            return CheckoutResult.failed("PAYMENT_DECLINED");

        boolean committed = seats.commitHold(hold.getHoldId(), hold.getFencingToken());
        if (!committed) {
            payments.cancelAuthorization(pi.getPaymentIntentId());
            return CheckoutResult.failed("INVENTORY_LOST");
        }

        Order order = orders.createConfirmedOrder(cmd.userId(), hold, pi, cmd.idempotencyKey());
        payments.capture(pi.getPaymentIntentId(), order.getOrderId());
        tickets.issueTickets(order);
        return CheckoutResult.success(order);
    }
}
```

---

### Signed ticket payload (QR)

```java
// TicketSigner.java
public class TicketSigner {
    private final Mac mac; // HMAC-SHA256 with KMS-managed key

    public String signQrPayload(Ticket ticket, Instant validUntil) {
        String canonical = ticket.getTicketId() + "|" + ticket.getShowtimeId() + "|" + validUntil.toEpochMilli();
        byte[] sig = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
    }

    public boolean verify(String ticketId, String showtimeId, Instant validUntil, String signature) {
        // recompute and constant-time equals
        return true; // interview: show intent
    }
}
```

---

### Dynamic pricing (decorator sketch)

```java
// DynamicPricingContext.java
public record DynamicPricingContext(
        String showtimeId,
        List<String> seatIds,
        Instant at,
        double recentSalesVelocity,
        double percentSold
) {}

// SurgePricingDecorator.java
public class SurgePricingDecorator implements PricingEngine {
    private final PricingEngine delegate;
    private final SurgePolicy policy;

    public SurgePricingDecorator(PricingEngine delegate, SurgePolicy policy) {
        this.delegate = delegate;
        this.policy = policy;
    }

    @Override
    public Map<String, Money> quote(DynamicPricingContext ctx) {
        Map<String, Money> base = delegate.quote(ctx);
        BigDecimal mult = policy.multiplier(ctx.percentSold(), ctx.recentSalesVelocity());
        Map<String, Money> out = new HashMap<>();
        base.forEach((tier, m) -> out.put(tier, m.multiply(mult)));
        return out;
    }
}
```

---

### Waitlist notification (async)

```java
// WaitlistService.java
public class WaitlistService {
    private final WaitlistQueue queue;
    private final NotificationService notifier;

    public void onInventoryReleased(String showtimeId, int seatsFreed) {
        List<WaitlistEntry> batch = queue.dequeue(showtimeId, seatsFreed);
        for (WaitlistEntry e : batch) {
            notifier.send(e.getUserId(), new WaitlistWindowEvent(showtimeId, e.getOfferToken(), Duration.ofMinutes(10)));
        }
    }
}
```

---

### Resale: void and reissue

```java
// ResaleService.java
public class ResaleService {
    public Ticket completeResale(String listingId, String buyerId) {
        return repo.transaction(() -> {
            ResaleListing listing = listings.lock(listingId);
            Ticket oldT = tickets.lock(listing.getTicketId());
            oldT.voidTicket("RESALE");
            Ticket fresh = tickets.reissueForBuyer(oldT, buyerId);
            listing.markSold();
            return fresh;
        });
    }
}
```

---

### Demo driver (interview)

```java
// ConcertBookingDemo.java
public class ConcertBookingDemo {
    public static void main(String[] args) {
        // Wire repositories, pricing, PSP stub
        // 1. Search showtimes
        // 2. Hold 2 seats → show countdown
        // 3. Checkout with idempotency
        // 4. Print QR payload
        // 5. Simulate scan → attendance++
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `PricingEngine`, delivery adapters | Swap pricing and fulfillment without touching checkout |
| **Decorator** | `SurgePricingDecorator` | Layer surge on base quotes |
| **State** | `InventoryState`, `PaymentState`, `OrderStatus` | Enforce valid transitions |
| **Repository** | `SeatInventoryRepository`, `OrderRepository` | Testability and persistence abstraction |
| **Saga / orchestration** | `CheckoutService`: authorize → commit inventory → capture | Partial failure handling |
| **Template method** | Optional `AbstractOnSalePipeline` | Shared validation for presale vs public |
| **Factory** | `TicketIssuer` | Centralize signing + encoding |
| **Observer** | Domain events → notifications, analytics | Decouple core from side effects |

---

## 🔒 Step 8: Concurrency Handling (12–18 minutes)

### Problem
Thousands of users attempt to buy overlapping seats during on-sale. Naive read-modify-write causes **double bookings**; long transactions underpay performance.

### Goals
- **Linearizable** seat assignment per `(showtimeId, seatId)`  
- High throughput across **different** seats and showtimes  
- Fair expiration of holds without blocking entire showtime

### Techniques

1. **Pessimistic row locks (default for reserved seats)**  
   `SELECT * FROM seat_inventory WHERE showtime_id=? AND seat_id IN (?) FOR UPDATE`  
   Validate all rows, then update. **Short transaction**: only inventory + hold insert.

2. **Optimistic locking (alternative for low contention)**  
   `UPDATE seat_inventory SET state=HELD, version=version+1 WHERE ... AND version=?`  
   Retry with bounded backoff on conflict.

3. **Partition single writer per showtime**  
   Route all mutations for `showtimeId` to one partition (Kafka partition key / actor mailbox). Preserves order; scales horizontally across showtimes.

4. **Fencing tokens**  
   Hold carries random `fencingToken`. Commit path rejects stale workers that delayed after hold expiry was extended elsewhere (prevents **split-brain** if using cache + DB).

5. **GA buckets**  
   Single row per `(showtimeId, zoneId)` with `available_quantity` and constraint `CHECK (available_quantity >= 0)`; lock row; or use **Redis DECR** with Lua script **and** async reconciliation to DB (advanced).

6. **Compare-and-set in cache (dangerous alone)**  
   If using Redis for hot inventory, use Lua scripts for atomic check-and-set and still reconcile to **source-of-truth** RDBMS for settlement.

7. **Idempotency everywhere**  
   `idempotency_key` on checkout and PSP calls so retries do not duplicate charges or tickets.

8. **Thundering herd on waitlist**  
   Stagger offers (random delay per user), rate-limit notification sends, small purchase window.

9. **Validation scans**  
   `INSERT INTO scan_record ... ON CONFLICT DO NOTHING` returning row count → first scan wins; or Redis `SETNX` per `ticketId` with DB audit.

### What to say in interview
*"I serialize on the smallest key: each seat row for reserved, zone row for GA. I keep the transaction short—lock inventory, write hold, commit—then payment happens outside. Payment success triggers a second transaction with fencing token match to flip HELD→BOOKED. If payment fails or times out, a job or lazy check releases seats."*

---

## 📊 Step 9: Database Schema (12–16 minutes)

### Core tables (relational sketch)

**promoters** (`promoter_id`, `name`, …)

**venues** (`venue_id`, `name`, `timezone`, `address_json`, …)

**seating_charts** (`chart_id`, `venue_id`, `name`)

**layout_versions** (`version_id`, `chart_id`, `published_at`, `svg_asset_url`, …)

**sections** (`section_id`, `version_id`, `name`, `tier_code`, `sort_order`)

**rows** (`row_id`, `section_id`, `label`)

**seats** (`seat_id`, `row_id`, `label`, `attrs_json`)

**admission_zones** (`zone_id`, `version_id`, `name`, `tier_code`, `capacity`)

**events** (`event_id`, `promoter_id`, `type`, `title`, `metadata_json`, `status`)

**showtimes** (`showtime_id`, `event_id`, `venue_id`, `layout_version_id`, `starts_at`, `ends_at`, `doors_at`, `status`, `on_sale_at`, `hold_ttl_seconds`)

**seat_inventory** (`inventory_id`, `showtime_id`, `seat_id` UNIQUE, `state`, `hold_id`, `hold_fence`, `hold_expires_at`, `version`, `updated_at`)  
- UNIQUE(`showtime_id`, `seat_id`)

**ga_inventory** (`showtime_id`, `zone_id`, `sold`, `held`, `capacity`, `version`)  
- UNIQUE(`showtime_id`, `zone_id`)

**holds** (`hold_id`, `user_id`, `showtime_id`, `payload_json`, `fence`, `expires_at`, `status`)

**orders** (`order_id`, `user_id`, `showtime_id`, `status`, `idempotency_key` UNIQUE, `created_at`)

**order_lines** (`line_id`, `order_id`, `seat_id`, `zone_id`, `qty`, `unit_price`, `currency`, `tier_code`)

**payments** (`payment_id`, `order_id`, `psp_ref`, `state`, `amount`, `currency`, `idempotency_key` UNIQUE)

**tickets** (`ticket_id`, `order_line_id`, `showtime_id`, `seat_id`, `zone_id`, `status`, `barcode`, `issued_at`)

**ticket_nonces** (`ticket_id`, `nonce`, `expires_at`) — optional for one-time QR refresh

**transfers** (`transfer_id`, `ticket_id`, `from_user`, `to_user`, `status`, `created_at`)

**resale_listings** (`listing_id`, `ticket_id`, `seller_id`, `price`, `status`)

**waitlist** (`entry_id`, `showtime_id`, `user_id`, `tier_preference`, `position`, `status`, `offer_token`, `offer_expires_at`)

**scan_records** (`scan_id`, `ticket_id`, `terminal_id`, `scanned_at`, `result`)

**price_rules** (`rule_id`, `showtime_id` NULL, `event_id` NULL, `json_expr`, `priority`)

**audit_log** (`id`, `entity`, `entity_id`, `action`, `actor`, `payload_json`, `ts`)

### Search index (document example)

```json
{
  "eventId": "E1",
  "title": "Home Opener",
  "type": "SPORTS",
  "city": "Chicago",
  "showtimes": [
    { "id": "S1", "startsAt": "2026-06-01T19:00:00-05:00", "minPrice": 45.00, "availableLow": true }
  ]
}
```

---

## 💡 Step 10: Interview Discussion Points (14–20 minutes)

### 1. Price lock: hold time vs checkout time
Locking at **hold** reduces surprise; risk if surge increases—promoter may prefer lock at payment. Trade UX vs revenue; disclose policy.

### 2. Virtual waiting room
Edge layer assigns random queue tokens before hitting inventory DB; prevents total DB meltdown on GO time.

### 3. Secondary market and fraud
Reissue tickets on resale to invalidate screenshots; cap prices; velocity limits per account; device fingerprinting (product).

### 4. Partial venue holds
Promoter holds seats: mark `BLOCKED` until release job runs; distinguish from `HELD` (customer).

### 5. Event cancellation
Batch refund via PSP; void all tickets; idempotent refund webhook handling; compensation messaging.

### 6. International and tax
Price components: face value, fees, tax jurisdictions by venue location; VAT/GST rules.

### 7. Accessibility
Companion seats, ADA inventory pools, enforce purchase limits to prevent scalping of accessible seats.

### 8. Offline validation
Signed tickets with **asymmetric keys** rotated per season; terminals cache public keys; conflict if same ticket scanned twice offline—resolve by **first timestamp wins** + manual override UI.

### 9. Reporting accuracy
Materialize nightly sales cubes; real-time use stream processing (Flink) from order events for ops dashboards.

### 10. Testing concurrency
Property tests: parallel holds on same seat → at most one success; chaos tests on lock timeouts.

---

## ✅ Step 11: SOLID Principles Verification

### Single Responsibility
- `SeatInventoryService` only inventory state transitions; `CheckoutService` orchestrates payment + order; `TicketSigner` only crypto encoding.

### Open/Closed
- New pricing signal: extend `PricingEngine` or add decorator; no change to hold/commit path.

### Liskov Substitution
- Any `PricingEngine` implementation must return complete tier map for quoted selection.

### Interface Segregation
- Split `PaymentClient` (authorize/capture) from `RefundClient` if refunds gain different methods.

### Dependency Inversion
- Services depend on repository interfaces; infrastructure implements locking and storage.

---

## 📈 Step 12: Complexity Analysis

| Operation | Time | Notes |
|-----------|------|-------|
| Search events | O(log N) typical | N = indexed documents; filters on attributes |
| Load seat map | O(S) | S = seats for layout version; paginate/tile |
| Hold K seats | O(K) row locks | Short txn; K small (usually < 10) |
| Commit hold | O(K) | Same set locked again with fence check |
| GA hold qty Q | O(1) | Single bucket row locked |
| Checkout (happy path) | O(K) + O(1) PSP | Dominated by network to PSP |
| Dynamic pricing quote | O(T + R) | T tiers, R rules evaluated; cache per showtime minute bucket |
| Waitlist dequeue M | O(M log W) | W queue depth if priority heap; or O(M) if head pointers |
| Scan validate | O(1) amortized | Hash lookup + insert scan row with uniqueness on ticket |
| Hot showtime throughput | **Serialized per seat** | Different seats parallelize across workers |

**Scaling mantra:** Partition by `showtimeId`; keep inventory transactions **short**; push everything else async.

---

## 🎓 Step 13: Key Takeaways

1. **Seat is the consistency choke point** — use row locks or single-writer partition; never rely on cache alone for final truth.
2. **Hold + fencing token + idempotent checkout** is the standard interview pattern for Ticketmaster-class problems.
3. **GA vs reserved** needs different models: row-per-seat vs aggregate bucket with careful locking/Lua.
4. **Dynamic pricing** belongs in a pluggable engine; persist snapshots on holds/orders for disputes.
5. **Resale** almost always implies **void + reissue** to kill old barcodes.
6. **Validation** is a separate write path: first-scan wins + optional nonce rotation for abuse resistance.
7. **Waitlist** must avoid thundering herd—staggered offers and short windows.
8. **Payments** are a state machine; inventory commit and capture ordering must handle reverse failures (compensating refund).

**Interview success formula:** Clarify seating types → inventory FSM → hold/TTL → payment idempotency → concurrency on seat rows → QR/scan → resale → reporting.

---

**Concert / Event Booking System LLD — Hard difficulty — ready for review.**
