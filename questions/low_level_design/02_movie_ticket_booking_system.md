# Low-Level Design: Movie Ticket Booking System

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

**Contrast with Concert Booking:** Same core inventory/hold/checkout spine, but movies add **many showtimes per day per screen**, **screen-specific seating layouts**, **simpler pricing** (no surge/resale marketplace), and **in-theater F&B** attached to bookings.

---

## 🎯 Step 1: Understand the Problem (6–10 minutes)

### What the Interviewer Says:
*"Design a movie ticket booking platform (like BookMyShow or Fandango) that supports multiple theater locations, many screens, daily showtime schedules, interactive seat maps with types, high-concurrency checkout, optional food orders, payments, tickets with QR/barcode, and operational reporting."*

### Clarifying Questions to Ask:

1. **Q:** Do we model a single chain or many exhibitors as tenants?  
   **A:** Multi-location **theater operator** (one org); extensible to multi-tenant later via `chainId` / `operatorId`.

2. **Q:** Can the same movie play on multiple screens at overlapping times?  
   **A:** Yes; each **show** is `(movie, screen, start time)`; scheduling prevents **double-booking a screen** at overlapping times.

3. **Q:** Is seating always reserved with row/column?  
   **A:** Yes for this LLD: **2D grid per screen layout** with seat types (normal, premium, recliner). No GA floor for simplicity (could extend).

4. **Q:** How long is the seat hold during checkout?  
   **A:** **10 minutes** default; configurable per chain or cinema.

5. **Q:** What consistency is required for seats?  
   **A:** **At most one** successful booking per `(showId, seatId)`; no double sale under concurrent users.

6. **Q:** How does pricing work compared to concerts?  
   **A:** **Base price per show or tier** × **seat-type multiplier** × **day/time rules** (weekend, matinee); no dynamic surge or resale marketplace in scope.

7. **Q:** Are food orders part of the same transaction as tickets?  
   **A:** Prefer **single checkout** with line items; kitchen fulfillment is async; partial failure policy defined (e.g., refund F&B if show cancelled).

8. **Q:** Payment scope?  
   **A:** Model authorize/capture, refunds, idempotency; PSP is external adapter.

9. **Q:** Cancellation and refunds?  
   **A:** User-cancel before show policy (e.g., up to 2h before); admin cancel show → mass refund; idempotent webhook handling.

10. **Q:** Notifications?  
    **A:** Booking confirmation, payment receipt, **reminder** (e.g., 2h before show), show cancellation.

11. **Q:** Ticket format?  
    **A:** Signed **QR** and/or **Code128 barcode**; first scan wins at gate; optional rotating nonce for app tickets.

12. **Q:** Scale?  
    **A:** Read-heavy browse; write bursts when blockbuster drops; **hot rows** per `showId` + `seatId`.

---

## 🔹 Step 2: Gather Requirements (10–14 minutes)

### Functional Requirements

#### Theater & Location Management (FR1–FR6)
1. System shall support **theater chains** with **multiple cinema locations** (address, timezone, amenities).
2. Each **cinema** shall have metadata: name, city, geo coordinates, contact, operating hours.
3. Admin shall **onboard screens** under a cinema with human-readable names (e.g., "Screen 3", "IMAX 1").
4. System shall support **screen deactivation** for maintenance without deleting historical shows.
5. Each screen shall reference a **seating layout template** (versioned); layout changes do not mutate past shows.
6. Role-based access: **chain admin**, **cinema manager**, **box office**, **support** (view/refund).

#### Screen & Seating Layout (FR7–FR14)
7. **Layout** shall define **rows** and **seats** with `(rowLabel, columnIndex)` e.g., `H-12`.
8. Each seat shall have **type**: `NORMAL`, `PREMIUM`, `RECLINER` (extensible enum).
9. Seats may be **BLOCKED** (broken, reserved for staff, accessibility hold) — not sellable.
10. Optional **aisle** / **wheelchair companion** flags for UX and compliance.
11. **LayoutVersion** is immutable once attached to a **Show**; edits create new version.
12. UI shall load seat map by `layoutVersionId` + live **inventory state** per show.
13. Preview mode for admin: ghost seats vs sellable.
14. **Capacity** per screen = count of sellable seats in layout (excluding blocked).

#### Movie Catalog (FR15–FR22)
15. **Movie** entity: title, synopsis, **genres** (multi), **rating** (e.g., PG-13), **duration minutes**, **primary language**, additional languages, cast/crew (optional), poster URLs.
16. Movies shall be **searchable** by title, genre, language, rating, city, date range.
17. Support **coming soon** vs **now showing** based on first/last show dates (materialized or computed).
18. Admin shall **schedule** which movie plays on which screen at which times.
19. **Certificates** / regional censorship tags optional per territory.
20. **3D / IMAX / Dolby** flags as **experience tags** affecting **base price** or **screen binding**.
21. Content updates shall not break tickets for already sold shows (snapshot show title on ticket optional).
22. Duplicate movie entries merged via canonical `movieId` (out of scope: full CMS).

#### Showtime Scheduling (FR23–FR30)
23. A **Show** = `movieId` + `screenId` + `startsAt` + `endsAt` (endsAt = startsAt + duration + buffer).
24. **Scheduling validator** shall reject overlapping shows on the **same screen** (interval overlap detection).
25. Multiple shows **per day per screen** allowed; **same movie** may run on **multiple screens** concurrently.
26. Show states: `DRAFT`, `ON_SALE`, `SOLD_OUT`, `CANCELLED`, `COMPLETED`.
27. **On-sale window**: `onSaleAt` … `salesCutoff` (e.g., until show start or 15 min after).
28. **Blackout** for private bookings (corporate) — mark seats blocked or whole show private.
29. **Timezone**: store instants in UTC; display in **cinema local** timezone.
30. Bulk schedule import (CSV) — nice-to-have; mention interface only.

#### Seat Inventory & Selection (FR31–FR38)
31. Per show, each sellable seat has state: `AVAILABLE`, `HELD`, `BOOKED`.
32. User flow: **search** → pick **show** → **select seats** → optional **F&B** → **pay**.
33. **Hold** created on seat selection with **TTL = 10 minutes** (configurable).
34. **Real-time** map may use **WebSocket/SSE** or short polling; **checkout path** strongly consistent.
35. **Adjacent seat** suggestion optional (greedy); not required for correctness.
36. **Max seats per order** (e.g., 10) for fraud control.
37. **Seat release** on hold expiry, payment failure, or user abandon.
38. Admin **manual release** of stuck holds with audit.

#### Pricing (FR39–FR44)
39. **Base price** configurable per **show** (or per `(movie, cinema tier)` with override).
40. **Seat-type multiplier**: e.g., normal 1.0×, premium 1.25×, recliner 1.5×.
41. **Day/time rules**: e.g., weekend +20%, **matinee** (before 5pm) −15%; evaluated as ordered rule list.
42. **Fees and taxes** as separate line items; transparent breakdown pre-pay.
43. **Promotions** (code-based % off) applied to ticket subtotal per stacking rules.
44. **Price snapshot** stored on **hold** and **order** for dispute resolution.

#### Booking, Payment & Tickets (FR45–FR54)
45. **Booking session** ties user, hold, optional cart id, `idempotency_key`.
46. **Payment**: authorize then capture on success; handle partial failures with **compensating refund**.
47. On success: seats `HELD` → `BOOKED`; issue **tickets** with unique ids.
48. **QR** payload signed; **barcode** derived from ticket id + secret segment.
49. **Order confirmation** email/SMS/push via notification service.
50. **Reminder** notification N hours before show (scheduled job / queue).
51. **Digital wallet** delivery — adapter pattern (PDF, email, Apple/Google — interface level).
52. **Box office** flow: staff selects seats and completes payment on behalf — same inventory rules.
53. **Idempotent** checkout and PSP calls.
54. **Duplicate submit** returns same order response.

#### Food & Beverage (FR55–FR60)
55. **Menu** per cinema or chain: items, categories, **prep time**, availability windows.
56. User may add **F&B line items** to order; pickup time default **show start** or **interval** (pre-show).
57. **Inventory** for limited items (e.g., combo buckets) — optional simple stock count.
58. F&B **fulfillment status**: `PENDING`, `PREPARING`, `READY`, `HANDED_OVER`, `REFUNDED`.
59. Kitchen **display system** consumes order events (out-of-band integration).
60. Refund policy: if show cancelled, **full order** refund including F&B.

#### Cancellation & Refunds (FR61–FR65)
61. User **cancel order** per policy window; seats return to `AVAILABLE`; **refund** initiated.
62. **Show cancellation**: void all tickets; batch refund; notify attendees.
63. **Partial refund** (e.g., only tickets, keep F&B) — support decision; model as line-level refunds.
64. Refund idempotency via `refund_idempotency_key`.
65. **No-show** does not auto-refund (business rule).

#### Notifications (FR66–FR69)
66. Channels: email, SMS, push; user preferences and consent.
67. Templates: confirmation, reminder, cancellation, refund processed.
68. **Async** sending with retry and dead-letter queue.
69. Link to **order** and **add-to-calendar** (ICS) optional.

#### Reporting (FR70–FR74)
70. **Occupancy**: tickets sold / capacity per show, screen, cinema, date range.
71. **Revenue**: gross, net after refunds, by movie, genre, channel (online vs box office).
72. **Popular movies**: ranking by tickets, revenue, velocity.
73. **F&B attachment rate** and top items.
74. Export CSV; **real-time** ops dashboard vs **nightly** cube (mention both).

### Non-Functional Requirements — Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "Friday 7pm blockbuster; thousands pick seats concurrently."
- Catalog and search: millions of reads; **hot show** = many writers on **different seat rows**
- Partition inventory by `showId`; shard by `cinemaId` or `showId` for orders

**Deduced NFRs:**
- ✅ Stateless API tier; CDN for posters and static layout JSON
- ✅ Search index (Elasticsearch/OpenSearch) for movies and showtimes
- ✅ **Per-show seat rows** or key-value sharding for hottest keys
- ✅ Rate limiting, bot detection, optional **waiting room** for mega premieres

---

#### 2. **Consistency Analysis**

**Think:** "Same seat sold twice is catastrophic."
- Seat state transitions must be **atomic** and **serializable** per seat
- Payment captured without ticket → **reconciliation** and refund

**Deduced NFRs:**
- ✅ **Strong consistency** on hold → book path
- ✅ **Fencing token** or version check on commit after payment
- ✅ **Idempotent** order creation and payment
- ✅ Eventual consistency OK for **aggregate "seats left"** banners with short SLA

---

#### 3. **Availability Analysis**

**Think:** "PSP down during checkout; cinema still selling at counter?"
- Browse should stay up; checkout may degrade gracefully

**Deduced NFRs:**
- ✅ **99.9%+** read path; retries and circuit breakers on PSP
- ✅ **Read-through cache** for movie/show lists with TTL
- ✅ Graceful messaging when hold service degraded; do not oversell

---

#### 4. **Maintainability Analysis**

**Think:** "Pricing rules and layouts change weekly."
- Version layouts; audit price rule changes; feature flags for F&B

**Deduced NFRs:**
- ✅ Admin APIs; **strategy** for pricing rules
- ✅ **Audit log** for inventory overrides and refunds
- ✅ Configurable **hold TTL** (default 10 min)

---

#### 5. **Performance Analysis**

**Think:** "Seat map load; hold latency."
- Search p95 < 200ms (cached)
- Hold p95 < 150ms on DB path; payment dominates
- Scheduler **O(S log S)** for S shows per screen when validating overlaps

**Deduced NFRs:**
- ✅ Short DB transactions: lock only selected seat rows
- ✅ **Batch** seat state fetch for map (`WHERE show_id=?`)
- ✅ Background **expiry job** for holds (or lazy expire in next transaction)

---

#### 6. **Security Analysis**

**Think:** "Forged QR, scalper bots, staff abuse."
- Signed tickets; rate limits; RBAC for admin

**Deduced NFRs:**
- ✅ **HMAC/asymmetric** signing for QR; optional nonce store for one-time display
- ✅ **PCI**: tokenize cards at PSP; never store PAN
- ✅ OAuth2/OIDC for users; audit staff actions
- ✅ CAPTCHA / velocity limits on checkout

---

## 🧩 Step 3: Identify Core Entities (12–16 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|---------------|-------|
| "Chain, cinema, screen" | TheaterChain, Cinema, Screen |
| "Layout, row, seat" | SeatLayout, LayoutVersion, Row, Seat, SeatType |
| "Movie, genre, rating" | Movie, Genre, ContentRating |
| "Show, schedule" | Show, ShowScheduleValidator |
| "Available, held, booked" | SeatInventory, SeatState, Hold |
| "Booking, payment" | BookingSession, Order, OrderLine, PaymentIntent |
| "Pricing rules" | PricingRule, PriceQuote, SeatPricingContext |
| "QR, ticket" | Ticket, TicketSigner, BarcodeEncoder |
| "F&B" | Menu, MenuItem, FoodOrderLine, ConcessionOrder |
| "Notify" | Notification, NotificationTemplate |
| "Reports" | OccupancyReport, RevenueReport |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| TheaterChain | ✅ YES | Org root |
| Cinema | ✅ YES | Physical location |
| Screen | ✅ YES | Projection hall; schedule anchor |
| SeatLayout / LayoutVersion | ✅ YES | Versioned seating template |
| Row / Seat | ✅ YES | Structural; Seat has type |
| Movie | ✅ YES | Catalog aggregate |
| Show | ✅ YES | Sellable instance (movie + screen + time) |
| SeatInventory | ✅ YES | Per (show, seat) state |
| Hold | ✅ YES | TTL lease with fence |
| BookingSession | ✅ YES (optional VO) | Often folded into Hold + cart |
| Order / OrderLine | ✅ YES | Commerce |
| PaymentIntent | ✅ YES | Payment lifecycle |
| MenuItem | ✅ YES | F&B catalog |
| ConcessionOrder | ✅ YES | Child of order or parallel aggregate |
| Ticket | ✅ YES | Fulfillment + scan |
| Notification | ✅ YES | Outbound message record |

### Final Entity List

**Organization:** `TheaterChain`, `Cinema`, `Screen`  
**Layout:** `SeatLayout`, `LayoutVersion`, `SeatRow`, `Seat`, `SeatType` (enum)  
**Catalog:** `Movie`, `Genre`, `Show`  
**Inventory:** `SeatInventory`, `Hold`  
**Commerce:** `Order`, `OrderLine`, `FeeBreakdown`, `PaymentIntent`  
**Pricing:** `ShowBasePrice`, `SeatTypeMultiplierTable`, `PricingRule`, `PricingEngine`  
**F&B:** `Menu`, `MenuItem`, `FoodOrderLine`, `ConcessionFulfillment`  
**Fulfillment:** `Ticket`, `TicketDelivery`, `TicketSigner`  
**Engagement:** `Notification`, `ScheduledNotification`  
**Operations:** `ShowScheduleValidator`, `HoldExpiryWorker`, `ScanRecord`  
**Reporting (views):** `ShowMetrics`, `DailyRevenueSnapshot` (materialized)

---

## 🔗 Step 4: Establish Relationships (14–18 minutes)

### Pass 1: Organization & Layout

#### TheaterChain ↔ Cinema  
**Composition (logical):** Chain owns cinemas.  
```
TheaterChain ◆────→ Cinema [1..*]
```

#### Cinema ↔ Screen  
```
Cinema ◆────→ Screen [1..*]
```

#### Screen ↔ SeatLayout  
**Association:** Screen uses a layout family; concrete version on each show.  
```
Screen ─────→ SeatLayout [1]
SeatLayout ◆────→ LayoutVersion [1..*]
```

#### LayoutVersion ↔ SeatRow ↔ Seat  
```
LayoutVersion ◆────→ SeatRow [1..*] ◆────→ Seat [1..*]
```

---

### Pass 2: Catalog & Shows

#### Movie ↔ Show  
```
Movie ◆────→ Show [1..*]
```

#### Show ↔ Screen  
```
Show ─────→ Screen [1]
Show ─────→ LayoutVersion [1]   // snapshot for this show
```

**Constraint:** No two shows on **same screen** with overlapping `[startsAt, endsAt)`.

---

### Pass 3: Inventory & Orders

#### Show ↔ SeatInventory  
```
Show ◆────→ SeatInventory [1..*]   // one per sellable seat in layout
```

#### Hold ↔ SeatInventory  
```
Hold ─────→ SeatInventory [1..*]
```

#### Order ↔ OrderLine ↔ Ticket  
```
Order ◆────→ OrderLine [1..*] ─────→ Ticket [0..1 per seat line]
Order ◆────→ FoodOrderLine [0..*]
Order ─────→ PaymentIntent [1..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Chain → Cinema | 1:N | Composition |
| Cinema → Screen | 1:N | Composition |
| Screen → SeatLayout | N:1 | Association |
| LayoutVersion → Seat | 1:N | Composition |
| Movie → Show | 1:N | Composition |
| Show → Screen | N:1 | Association |
| Show → SeatInventory | 1:N | Composition |
| Hold → SeatInventory | 1:N | Association |
| Order → OrderLine | 1:N | Composition |

---

## 📐 Step 5: Design Class Diagrams (16–22 minutes)

### Class Diagram 1: Enums

```
┌────────────────────┐  ┌────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>      │
│   SeatType         │  │  SeatState         │  │   ShowStatus         │
├────────────────────┤  ├────────────────────┤  ├─────────────────────┤
│ NORMAL             │  │ AVAILABLE          │  │ DRAFT                │
│ PREMIUM            │  │ HELD               │  │ ON_SALE              │
│ RECLINER           │  │ BOOKED             │  │ SOLD_OUT             │
└────────────────────┘  └────────────────────┘  │ CANCELLED            │
                        │ BLOCKED (layout)     │  │ COMPLETED            │
                        └────────────────────┘  └─────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>      │
│   OrderStatus      │  │  PaymentState      │  │   TicketStatus       │
├────────────────────┤  ├────────────────────┤  ├─────────────────────┤
│ DRAFT              │  │ CREATED            │  │ ISSUED               │
│ PAYMENT_PENDING    │  │ AUTHORIZED         │  │ VOID                 │
│ CONFIRMED          │  │ CAPTURED           │  │ USED                 │
│ CANCELLED          │  │ FAILED             │  └─────────────────────┘
│ REFUNDED           │  │ REFUNDED           │
└────────────────────┘  └────────────────────┘
```

---

### Class Diagram 2: Organization & Screen

```
┌────────────────────────────────────────────────────────────────┐
│                      TheaterChain                               │
├────────────────────────────────────────────────────────────────┤
│ - chainId: String                                              │
│ - name: String                                                 │
│ - defaultHoldTtl: Duration   // e.g. 10 minutes                │
├────────────────────────────────────────────────────────────────┤
│ + listCinemas(): List<Cinema>                                 │
└────────────────────────────────────────────────────────────────┘
                              │ 1..*
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                         Cinema                                  │
├────────────────────────────────────────────────────────────────┤
│ - cinemaId, name, timezone, address, city, geo                 │
├────────────────────────────────────────────────────────────────┤
│ + listScreens(): List<Screen>                                   │
└────────────────────────────────────────────────────────────────┘
                              │ 1..*
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                         Screen                                  │
├────────────────────────────────────────────────────────────────┤
│ - screenId, cinemaId, name, seatLayoutId                       │
│ - experienceTags: Set<ExperienceTag>  // IMAX, DOLBY, etc.    │
├────────────────────────────────────────────────────────────────┤
│ + isFree(interval): boolean   // scheduling helper             │
└────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Movie & Show

```
┌────────────────────────────────────────────────────────────────┐
│                          Movie                                  │
├────────────────────────────────────────────────────────────────┤
│ - movieId, title, synopsis, durationMinutes                    │
│ - genres: Set<Genre>, rating: ContentRating                    │
│ - primaryLanguage, languages: Set<String>                      │
│ - posterUrls, experienceEligible: Set<ExperienceTag>           │
├────────────────────────────────────────────────────────────────┤
│ + validateForScreen(screen: Screen): boolean                   │
└────────────────────────────────────────────────────────────────┘
                              │ 1..*
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                          Show                                   │
├────────────────────────────────────────────────────────────────┤
│ - showId, movieId, screenId, layoutVersionId                   │
│ - startsAt, endsAt, timezone, status                           │
│ - onSaleAt, salesCutoffAt, basePrice: Money                    │
│ - holdTtl: Duration   // override chain default                 │
├────────────────────────────────────────────────────────────────┤
│ + overlaps(other: Show): boolean                               │
│ + isOnSale(now: Instant): boolean                              │
└────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Layout & Seat

```
┌──────────────────────────┐       ┌─────────────────────────────┐
│      SeatLayout          │       │      LayoutVersion           │
├──────────────────────────┤       ├─────────────────────────────┤
│ - layoutId               │◆──────│ - versionId                   │
│ - name                   │  1..* │ - rows: List<SeatRow>         │
└──────────────────────────┘       └─────────────────────────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │     SeatRow      │
                                    ├──────────────────┤
                                    │ - rowLabel       │
                                    │ - seats: List<Seat>             │
                                    └──────────────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │      Seat        │
                                    ├──────────────────┤
                                    │ - seatId         │
                                    │ - columnLabel    │
                                    │ - type: SeatType │
                                    │ - blocked: bool  │
                                    └──────────────────┘
```

---

### Class Diagram 5: Inventory, Hold, Order

```
┌─────────────────────────────────────────────────────────────────┐
│                     SeatInventory                                │
├─────────────────────────────────────────────────────────────────┤
│ - inventoryId, showId, seatId                                  │
│ - state: SeatState, version: long                              │
│ - holdId, holdFence, holdExpiresAt                             │
├─────────────────────────────────────────────────────────────────┤
│ + isHoldable(now: Instant): boolean                            │
└─────────────────────────────────────────────────────────────────┘
                              ▲
┌─────────────────────────────────────────────────────────────────┐
│                          Hold                                    │
├─────────────────────────────────────────────────────────────────┤
│ - holdId, userId, showId, seatIds: List<String>                │
│ - expiresAt, fencingToken: long                                │
│ - priceSnapshot: Map<seatId, Money>                            │
├─────────────────────────────────────────────────────────────────┤
│ + isExpired(now: Instant): boolean                             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Order                                    │
├─────────────────────────────────────────────────────────────────┤
│ - orderId, userId, cinemaId, showId                            │
│ - status, idempotencyKey, createdAt                             │
│ - lines: List<OrderLine>, foodLines: List<FoodOrderLine>        │
├─────────────────────────────────────────────────────────────────┤
│ + confirm(): void                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Pricing & F&B

```
┌─────────────────────────────────────────────────────────────────┐
│              <<interface>> PricingEngine                         │
├─────────────────────────────────────────────────────────────────┤
│ + quoteSeat(ctx: MoviePricingContext): Map<String, Money>      │
└─────────────────────────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┴────────────────┐
        ▼                            ▼
┌──────────────────┐        ┌──────────────────────┐
│ RuleBasedMovie   │        │ SeatTypeMultiplier   │
│ PricingEngine    │        │ (table per chain)    │
├──────────────────┤        └──────────────────────┘
│ + quoteSeat()    │
└──────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       MenuItem                                   │
├─────────────────────────────────────────────────────────────────┤
│ - itemId, cinemaId, name, price: Money, prepMinutes            │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    FoodOrderLine                                 │
├─────────────────────────────────────────────────────────────────┤
│ - lineId, menuItemId, quantity, unitPrice                       │
│ - fulfillment: ConcessionStatus                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 7: Ticket & Notification

```
┌─────────────────────────────────────────────────────────────────┐
│                        Ticket                                    │
├─────────────────────────────────────────────────────────────────┤
│ - ticketId, orderLineId, showId, seatId                        │
│ - barcodeValue, qrSignature, status                              │
├─────────────────────────────────────────────────────────────────┤
│ + void(reason: String): void                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                  NotificationService                             │
├─────────────────────────────────────────────────────────────────┤
│ + sendBookingConfirmation(order: Order): void                   │
│ + scheduleShowReminder(showId, userId, when: Instant): void       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35–50 minutes)

### Enums and value objects

```java
// SeatState.java
public enum SeatState {
    AVAILABLE, HELD, BOOKED
}

// SeatType.java
public enum SeatType {
    NORMAL, PREMIUM, RECLINER
}

// Money.java (sketch)
public record Money(BigDecimal amount, String currency) {
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor).setScale(2, RoundingMode.HALF_UP), currency);
    }
}
```

---

### Show overlap validation (scheduling)

```java
// ShowScheduleValidator.java
public class ShowScheduleValidator {
    private final ShowRepository shows;

    /**
     * Reject if any show on same screen overlaps [startsAt, endsAt).
     */
    public void assertNoOverlap(String screenId, Instant startsAt, Instant endsAt, String excludeShowId) {
        List<Show> existing = shows.findByScreenAndWindow(screenId, startsAt, endsAt);
        for (Show s : existing) {
            if (excludeShowId != null && s.getShowId().equals(excludeShowId)) continue;
            if (intervalsOverlap(startsAt, endsAt, s.getStartsAt(), s.getEndsAt())) {
                throw new SchedulingConflictException("Screen busy: " + s.getShowId());
            }
        }
    }

    private boolean intervalsOverlap(Instant a0, Instant a1, Instant b0, Instant b1) {
        return a0.isBefore(b1) && b0.isBefore(a1);
    }
}
```

---

### Pricing: base × seat type × day/time rules

```java
// MoviePricingContext.java
public record MoviePricingContext(
        String showId,
        String cinemaId,
        Money basePricePerTicket,
        Map<String, SeatType> seatIdToType,
        ZoneId cinemaZone,
        Instant now
) {}

// SimpleMoviePricingEngine.java — no surge/resale
public class SimpleMoviePricingEngine implements PricingEngine {
    private final SeatTypeMultiplierTable multipliers;
    private final List<DayTimePricingRule> rules;

    @Override
    public Map<String, Money> quoteSeat(MoviePricingContext ctx) {
        ZonedDateTime local = ctx.now().atZone(ctx.cinemaZone());
        BigDecimal dayTimeFactor = BigDecimal.ONE;
        for (DayTimePricingRule r : rules) {
            if (r.matches(local)) {
                dayTimeFactor = dayTimeFactor.multiply(r.multiplier());
            }
        }
        Map<String, Money> out = new HashMap<>();
        for (var e : ctx.seatIdToType().entrySet()) {
            BigDecimal seatFactor = multipliers.factor(e.getValue());
            Money seatPrice = ctx.basePricePerTicket()
                    .multiply(dayTimeFactor)
                    .multiply(seatFactor);
            out.put(e.getKey(), seatPrice);
        }
        return out;
    }
}
```

---

### Seat hold with 10-minute TTL and fencing

```java
// Hold.java
public class Hold {
    private final String holdId;
    private final String userId;
    private final String showId;
    private final List<String> seatIds;
    private final Map<String, Money> priceSnapshot;
    private final Instant expiresAt;
    private final long fencingToken;

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public long getFencingToken() { return fencingToken; }
    public String getHoldId() { return holdId; }
    public List<String> getSeatIds() { return seatIds; }
    public String getShowId() { return showId; }
}
```

```java
// SeatInventoryService.java — core interview logic
public class SeatInventoryService {
    private final SeatInventoryRepository repo;
    private final Clock clock;
    private final HoldRepository holds;
    private final Duration defaultHoldTtl; // e.g. 10 minutes

    public Optional<Hold> tryHoldSeats(String userId, String showId, List<String> seatIds,
                                       Optional<Duration> ttlOverride, PricingEngine pricing,
                                       MoviePricingContext priceCtx) {
        Duration ttl = ttlOverride.orElse(defaultHoldTtl);
        Instant now = clock.instant();
        return repo.transaction(() -> {
            List<SeatInventoryRow> rows = repo.lockSeatsForUpdate(showId, seatIds);
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

            Map<String, Money> prices = pricing.quoteSeat(priceCtx);
            Hold hold = new Hold(holdId, userId, showId, seatIds, prices, exp, fence);
            holds.save(hold);
            return Optional.of(hold);
        });
    }

    public boolean commitHold(String holdId, long fencingToken) {
        return repo.transaction(() -> {
            Hold h = holds.findByIdForUpdate(holdId);
            if (h == null || h.isExpired(clock.instant())) return false;
            if (h.getFencingToken() != fencingToken) return false;

            List<SeatInventoryRow> rows = repo.lockSeatsForUpdate(h.getShowId(), h.getSeatIds());
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
// SeatInventoryRow.java
public class SeatInventoryRow {
    private String inventoryId;
    private String showId;
    private String seatId;
    private SeatState state;
    private String activeHoldId;
    private long holdFence;
    private Instant holdExpiresAt;
    private long version;

    public boolean isHoldable(Instant now) {
        if (state == SeatState.BOOKED) return false;
        if (state == SeatState.AVAILABLE) return true;
        if (state == SeatState.HELD && holdExpiresAt != null && now.isAfter(holdExpiresAt))
            return true; // lazy expire inside transaction
        return false;
    }

    public void transitionToHeld(String holdId, long fence, Instant exp, Instant now) {
        if (state == SeatState.HELD && holdExpiresAt != null && now.isAfter(holdExpiresAt)) {
            state = SeatState.AVAILABLE;
        }
        if (state != SeatState.AVAILABLE) throw new IllegalStateException();
        this.state = SeatState.HELD;
        this.activeHoldId = holdId;
        this.holdFence = fence;
        this.holdExpiresAt = exp;
        this.version++;
    }

    public void transitionToBooked() {
        this.state = SeatState.BOOKED;
        this.activeHoldId = null;
        this.version++;
    }

    public boolean isHeldBy(String holdId, long fence) {
        return state == SeatState.HELD
                && Objects.equals(this.activeHoldId, holdId)
                && this.holdFence == fence;
    }
}
```

---

### Checkout orchestration (tickets + optional F&B)

```java
// CheckoutService.java
public class CheckoutService {
    private final SeatInventoryService seats;
    private final PaymentClient payments;
    private final TicketIssuer tickets;
    private final OrderRepository orders;
    private final HoldRepository holds;
    private final Clock clock;
    private final NotificationService notifications;

    public CheckoutResult checkout(CheckoutCommand cmd) {
        Optional<Order> existing = orders.findByIdempotencyKey(cmd.idempotencyKey());
        if (existing.isPresent()) return CheckoutResult.duplicate(existing.get());

        Hold hold = holds.get(cmd.holdId());
        if (hold == null || hold.isExpired(clock.instant()))
            return CheckoutResult.failed("HOLD_EXPIRED");

        Money ticketTotal = sum(hold.getPriceSnapshot().values());
        Money foodTotal = cmd.foodLines().stream()
                .map(FoodOrderLine::subtotal).reduce(Money.ZERO, Money::add);
        Money total = ticketTotal.add(foodTotal).add(cmd.fees());

        PaymentIntent pi = payments.authorize(cmd.userId(), total, cmd.paymentMethodToken(), cmd.idempotencyKey());
        if (pi.getState() != PaymentState.AUTHORIZED)
            return CheckoutResult.failed("PAYMENT_DECLINED");

        boolean committed = seats.commitHold(hold.getHoldId(), hold.getFencingToken());
        if (!committed) {
            payments.cancelAuthorization(pi.getPaymentIntentId());
            return CheckoutResult.failed("INVENTORY_LOST");
        }

        Order order = orders.createConfirmed(cmd.userId(), hold, cmd.foodLines(), pi, cmd.idempotencyKey());
        payments.capture(pi.getPaymentIntentId(), order.getOrderId());
        tickets.issueTickets(order);
        notifications.sendBookingConfirmation(order);
        notifications.scheduleShowReminder(order.getShowId(), cmd.userId(), order.getShowStartsAt().minus(2, ChronoUnit.HOURS));
        return CheckoutResult.success(order);
    }
}
```

---

### Hold expiry worker

```java
// HoldExpiryWorker.java — periodic or event-driven
public class HoldExpiryWorker {
    public void releaseExpiredHolds(Instant now) {
        // Either: scan holds table WHERE expires_at < now AND status=ACTIVE
        // For each: transactionally flip seats back if still HELD with matching holdId
        // Or: lazy release only on next touch (already handled in isHoldable)
    }
}
```

---

### Ticket QR / barcode sketch

```java
// TicketIssuer.java
public class TicketIssuer {
    private final TicketSigner signer;
    private final BarcodeEncoder barcode;

    public void issueTickets(Order order) {
        for (OrderLine line : order.getSeatLines()) {
            String ticketId = UUID.randomUUID().toString();
            String barcodeValue = barcode.encode(ticketId, order.getShowId());
            String qrPayload = signer.sign(ticketId, order.getShowId(), line.getSeatId(), Instant.now().plus(48, ChronoUnit.HOURS));
            tickets.save(new Ticket(ticketId, line.getLineId(), order.getShowId(), line.getSeatId(), barcodeValue, qrPayload));
        }
    }
}
```

---

### Cancellation refund (user-initiated)

```java
// CancellationService.java
public class CancellationService {
    public RefundResult cancelOrder(String orderId, String userId, Instant now, CancellationPolicy policy) {
        return repo.transaction(() -> {
            Order order = orders.lock(orderId);
            if (!order.getUserId().equals(userId)) throw new ForbiddenException();
            Show show = shows.get(order.getShowId());
            if (!policy.mayCancel(show.getStartsAt(), now))
                return RefundResult.rejected("OUTSIDE_WINDOW");

            for (OrderLine line : order.getSeatLines()) {
                seatInventory.releaseBookedSeat(order.getShowId(), line.getSeatId());
                tickets.voidTicket(line.getTicketId(), "USER_CANCEL");
            }
            payments.refund(order.getPaymentIntentId(), order.getRefundAmount());
            order.markCancelled();
            notifications.sendRefundProcessed(order);
            return RefundResult.ok();
        });
    }
}
```

---

### Demo driver (interview)

```java
// MovieBookingDemo.java
public class MovieBookingDemo {
    public static void main(String[] args) {
        // 1. Search movies in city for date
        // 2. List shows for movie
        // 3. Hold 2 seats (10 min timer)
        // 4. Add popcorn line item
        // 5. Checkout idempotently
        // 6. Print QR; simulate gate scan
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `PricingEngine`, `NotificationChannel` | Swap pricing rules and delivery channels |
| **State** | `SeatState`, `PaymentState`, `OrderStatus`, `ShowStatus` | Valid transitions only |
| **Repository** | `SeatInventoryRepository`, `OrderRepository` | Persistence abstraction + test doubles |
| **Saga / orchestration** | `CheckoutService`: authorize → commit seats → capture → tickets → notify | Partial failure compensation |
| **Factory** | `TicketIssuer` | Centralize signing and encoding |
| **Observer / events** | Domain events → F&B kitchen, analytics, reminders | Decouple core booking from side effects |
| **Template method** | Optional `AbstractCheckoutPipeline` | Shared steps for web vs box office |
| **Adapter** | `PaymentClient` implementation for Stripe/Adyen | PSP interchangeability |

---

## 🔒 Step 8: Concurrency Handling (14–20 minutes)

### Problem
Opening weekend: thousands of users compete for **overlapping seat sets** on the same **show**. Naive read-modify-write causes **double booking**; long transactions hurt throughput.

### Goals
- **Linearizable** mutations per `(showId, seatId)`
- High aggregate QPS across **different** seats and shows
- **Fair** release when holds expire (10 minutes)
- No lost updates between payment callback retries and inventory commit

### Techniques

1. **Pessimistic row locks (primary pattern)**  
   `SELECT * FROM seat_inventory WHERE show_id=? AND seat_id IN (?) FOR UPDATE`  
   Verify all rows **AVAILABLE** or **HELD** with `hold_expires_at < now`, then set `HELD` in same transaction. Keep transaction **short** — only inventory + hold insert.

2. **Optimistic locking (low contention shows)**  
   `UPDATE seat_inventory SET state='HELD', version=version+1 WHERE ... AND version=?`  
   Retry with bounded backoff; good for matinee weekdays.

3. **Fencing tokens**  
   Random `fencingToken` on hold. **Commit** path must match `holdId` + `fence` on seat rows to defeat stale workers (delayed job after hold was reassigned).

4. **Per-show partition single-writer (advanced)**  
   Route all seat mutations for `showId` to one partition (actor / queue). Preserves order; scales by **number of hot shows**.

5. **Scheduling conflicts (screen serialization)**  
   When creating shows, **lock** `screen_id` or use **serializable** transaction to check overlaps so two admins cannot double-book the screen interval.

6. **Idempotency**  
   `idempotency_key` on `orders` and PSP calls; checkout retried after network failure must not double-charge or double-issue tickets (unique constraint on `order_id` in `tickets`).

7. **Cache (Redis) for display only**  
   Optional cache of **available count** or **seat bitmap** with TTL; **never** trust cache for final commit — DB (or durable KV with Lua + reconciliation) is source of truth.

8. **Hold expiry**  
   **Lazy**: next transaction treats expired `HELD` as free. **Active**: sweeper job batches expired `hold_id`s and releases rows — reduces "ghost held" time.

9. **Food inventory (if constrained)**  
   Single-row `menu_item_stock` locked `FOR UPDATE` or atomic `UPDATE ... WHERE available_qty >= :q`.

### What to say in interview
*"I serialize on the smallest key: each **seat row** for a show. Hold is a **10-minute lease** with a **fencing token**. Payment success triggers a second short transaction: lock the same seats, verify still held by this hold, flip to **BOOKED**. Scheduling uses **interval overlap** checks per screen. I never rely on Redis alone for seat truth unless I also have reconciliation."*

---

## 📊 Step 9: Database Schema (12–16 minutes)

### Core tables (relational sketch)

**theater_chains** (`chain_id`, `name`, `default_hold_ttl_seconds`, …)

**cinemas** (`cinema_id`, `chain_id`, `name`, `timezone`, `address_json`, `city`, …)

**screens** (`screen_id`, `cinema_id`, `name`, `seat_layout_id`, `metadata_json`)

**seat_layouts** (`layout_id`, `name`)

**layout_versions** (`version_id`, `layout_id`, `published_at`, `json_blob` or normalized rows)

**seat_rows** (`row_id`, `version_id`, `row_label`)

**seats** (`seat_id`, `row_id`, `column_label`, `seat_type`, `blocked`)

**movies** (`movie_id`, `title`, `duration_minutes`, `rating`, `primary_language`, `metadata_json`, …)

**movie_genres** (`movie_id`, `genre`)

**shows** (`show_id`, `movie_id`, `screen_id`, `layout_version_id`, `starts_at`, `ends_at`, `status`, `on_sale_at`, `sales_cutoff_at`, `base_price`, `currency`, `hold_ttl_seconds`)  
- INDEX(`screen_id`, `starts_at`, `ends_at`) for overlap queries

**seat_inventory** (`inventory_id`, `show_id`, `seat_id`, `state`, `hold_id`, `hold_fence`, `hold_expires_at`, `version`, `updated_at`)  
- UNIQUE(`show_id`, `seat_id`)

**holds** (`hold_id`, `user_id`, `show_id`, `seat_ids_json`, `fence`, `expires_at`, `status`, `price_snapshot_json`)

**orders** (`order_id`, `user_id`, `show_id`, `cinema_id`, `status`, `idempotency_key` UNIQUE, `created_at`)

**order_lines** (`line_id`, `order_id`, `seat_id`, `unit_price`, `currency`, `ticket_id` NULL)

**food_order_lines** (`line_id`, `order_id`, `menu_item_id`, `qty`, `unit_price`, `fulfillment_status`)

**menu_items** (`item_id`, `cinema_id`, `name`, `price`, `stock_optional`)

**payments** (`payment_id`, `order_id`, `psp_ref`, `state`, `amount`, `currency`, `idempotency_key` UNIQUE)

**tickets** (`ticket_id`, `order_line_id`, `show_id`, `seat_id`, `barcode`, `qr_payload`, `status`, `issued_at`)

**ticket_nonces** (`ticket_id`, `nonce`, `expires_at`) — optional rotating QR

**notifications** (`notification_id`, `user_id`, `type`, `payload_json`, `status`, `scheduled_at`, `sent_at`)

**scan_records** (`scan_id`, `ticket_id`, `terminal_id`, `scanned_at`, `result`)

**pricing_rules** (`rule_id`, `chain_id` NULL, `cinema_id` NULL, `priority`, `rule_json`)

**audit_log** (`id`, `entity`, `entity_id`, `action`, `actor`, `payload_json`, `ts`)

### Search index (document example)

```json
{
  "movieId": "M1",
  "title": "Nebula Rising",
  "genres": ["Sci-Fi"],
  "rating": "PG-13",
  "cityShows": [
    { "cinemaId": "C1", "showId": "S1", "startsAt": "2026-04-10T19:30:00-07:00", "minPrice": 14.99 }
  ]
}
```

---

## 💡 Step 10: Interview Discussion Points (14–20 minutes)

### 1. Multiple showtimes vs concert drops
Movies repeat **many times per day**; inventory hot spots are **per show**, not one mega event. Cache and shard by `showId`; blockbuster still needs **waiting room** optionally.

### 2. Screen layout versioning
Never mutate layout for past shows; **snapshot** `layout_version_id` on `shows` so seat maps stay stable for issued tickets.

### 3. Price snapshot on hold
User sees stable price during **10-minute** hold; if admin changes base price mid-checkout, policy chooses **honor snapshot** vs reject — snapshot is simpler for support.

### 4. Payment then inventory vs inventory then payment
**Authorize first**, commit inventory, **capture** — or **reserve inventory first** then pay. Interview: compare **orphan auth** vs **oversell risk**; common pattern is **authorize → commit seats → capture** with cancel auth on failure.

### 5. F&B coupling
Single **order** simplifies UX; **fulfillment** is async. If kitchen cannot fulfill, partial refund or substitute — product decision; model **line-level** states.

### 6. Show cancellation
Batch job: void tickets, **refund** with idempotent PSP calls, publish `ShowCancelled` event, notify all buyers.

### 7. Box office vs web
Same `SeatInventoryService`; staff role bypasses CAPTCHA; optional **offline** mode with sync (advanced).

### 8. Reporting
**Occupancy** = booked seats / capacity from `seat_inventory`; **revenue** from `orders` + refunds ledger; stream **Kafka** events for near-real-time dashboards.

### 9. Testing concurrency
Stress test: parallel holds on **same seat** → exactly one winner; property test **overlap** detection for schedules.

### 10. Internationalization
Multi-language **movie** metadata; **currency** per cinema chain; tax rules by jurisdiction.

---

## ✅ Step 11: SOLID Principles Verification

### Single Responsibility
- `ShowScheduleValidator` only interval constraints; `SeatInventoryService` only seat lifecycle; `CheckoutService` orchestrates; `TicketSigner` only crypto surface.

### Open/Closed
- New **day/time rule**: add `DayTimePricingRule` without changing hold/commit path. New **notification channel**: implement interface.

### Liskov Substitution
- Any `PricingEngine` must return per-seat `Money` for all requested seat ids.

### Interface Segregation
- Split `PaymentClient` (authorize/capture) from `RefundClient` if refunds grow specialized methods.

### Dependency Inversion
- Application services depend on **repository and client interfaces**; DB and PSP are infrastructure.

---

## 📈 Step 12: Complexity Analysis

| Operation | Time | Notes |
|-----------|------|-------|
| Search movies / shows | O(log N) typical | Indexed search; N documents |
| Load seat map | O(S) | S seats in layout version; cache JSON |
| Validate schedule overlap | O(K) | K existing shows overlapping window; index by screen |
| Hold K seats | O(K) row locks | Short transaction; K small |
| Commit hold | O(K) | Re-lock same seats; fence check |
| Checkout happy path | O(K) + O(F) + O(1) PSP | F food lines; network dominates |
| Pricing quote | O(K + R) | R rules; small constants |
| Hold expiry sweep | O(B) batch | B expired holds per tick |
| Gate scan | O(1) amortized | Unique ticket lookup + first-scan constraint |
| Hot show throughput | Serialized **per seat** | Different seats parallelize |

**Scaling mantra:** **Partition by `showId`**; keep seat transactions **short**; push notifications and F&B to **async** workers.

---

## 🎓 Step 13: Key Takeaways

1. **Show** is the scheduling + pricing unit; **screen** enforces **no temporal overlap**; **layout version** is immutable per show.
2. **Seat row** is the consistency choke point — pessimistic locks or equivalent; **10-minute hold** + **fencing token** + **idempotent checkout** is the interview backbone.
3. **Pricing** is simpler than concerts: **base × seat-type × day/time rules**; still **snapshot** on hold/order.
4. **F&B** attaches to the same **order** but fulfillment is **async**; model line-level status and refunds.
5. **Tickets**: signed **QR** + **barcode**; **first scan wins**; void on cancel/refund.
6. **Notifications**: confirmation immediate; **reminder** via scheduler/queue.
7. **Reports**: occupancy and revenue from `seat_inventory` + `orders` (+ refund adjustments); stream events for live ops.
8. **Concurrency**: serialize per `(showId, seatId)`; optional single-writer per show for extreme hot keys.

**Interview success formula:** Clarify **chain → cinema → screen → layout** → **show scheduling** → **seat FSM** → **10m hold** → **pricing snapshot** → **checkout + F&B** → **QR** → **cancellation** → **reports**.

---

**Movie Ticket Booking System LLD — Hard difficulty — ready for review.**
