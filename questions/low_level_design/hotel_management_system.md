# Low-Level Design: Hotel Management System

**Difficulty:** Hard 🔥

**Interview Duration:** 75-120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5-8 minutes)

### What the Interviewer Says:
*"Design a hotel management system that supports multiple properties, room inventory and dynamic pricing, end-to-end guest journeys from search to checkout, operations like housekeeping and room service, payments including deposits and penalties, loyalty, and amenity bookings."*

### Clarifying Questions to Ask:

1. **Q:** Are we designing for a single deployment that serves many hotels (multi-tenant), or one chain with shared loyalty but isolated inventory per property?  
   **A:** Multiple hotels/branches; each hotel has its own rooms and staff, with optional chain-level loyalty and pricing rules.

2. **Q:** How do we prevent double-booking the same room for overlapping dates?  
   **A:** Strong consistency for inventory holds during booking; pessimistic lock or transactional availability check.

3. **Q:** Do we allow overbooking (sell more rooms than physical inventory)?  
   **A:** Optional policy per hotel; if yes, model expected no-show rate and walk risk.

4. **Q:** What modification rules apply to reservations (dates, room type upgrade, guest count)?  
   **A:** Subject to availability repricing, fare rules, and cancellation windows; may incur fees.

5. **Q:** Check-in: do we assign a specific room at booking time or at arrival?  
   **A:** Typically assign at check-in from available inventory of the booked room type; support pre-assigned rooms for VIPs.

6. **Q:** Payment scope: only cards, or also corporate folio, split bills, and third-party OTA prepay?  
   **A:** Deposits at booking, incidentals during stay, final settlement at checkout; support partial refunds and cancellation fees.

7. **Q:** Room service: real-time kitchen queue or simple order tracking?  
   **A:** Orders tied to room/stay, status lifecycle, billing to guest folio.

8. **Q:** Housekeeping: by room, by task type, or both?  
   **A:** Schedules and assignments (clean, inspect, turndown), blocked rooms for maintenance.

9. **Q:** Dynamic pricing: what inputs (season, occupancy, lead time, competitor rates)?  
   **A:** Seasonal base + demand multipliers + rules engine; extensible for ML later.

10. **Q:** Loyalty: points earn/burn, tiers, expiration?  
    **A:** Accrue on eligible spend, redeem for discounts or upgrades, tier benefits per program rules.

11. **Q:** Amenities: shared resources (spa slots, restaurant tables, gym classes) with capacity?  
    **A:** Yes; bookable slots with conflict detection and optional cancellation policy.

12. **Q:** Scale and peak load expectations?  
    **A:** Thousands of concurrent searches, hundreds of simultaneous bookings per property during events; 99.9% availability for booking path.

13. **Q:** Regulatory / PII constraints?  
    **A:** Encrypt guest PII, audit access, retain folios for financial compliance.

---

## 🔹 Step 2: Gather Requirements (8-12 minutes)

### Functional Requirements

#### Multi-Property & Catalog (FR1-FR8)
1. System shall support multiple hotels/branches, each with address, timezone, policies, and tax configuration
2. Each hotel shall maintain room types (e.g., Standard, Deluxe, Suite) with attributes (max occupancy, bed config, smoking policy)
3. System shall track physical rooms with identifiers, floor, adjoining flags, and maintenance/out-of-order status
4. Room inventory shall expose availability by date range and room type for search
5. Base and seasonal rate plans shall be attachable to room types per hotel
6. System shall support restrictions (min/max stay, closed to arrival/departure) per rate plan
7. Admin shall configure amenities catalog (spa services, restaurant seating, gym sessions) per hotel
8. System shall support blackout dates and event-driven inventory holds

#### Booking & Reservation (FR9-FR18)
9. Guest or agent shall search availability with filters (dates, guests, room type, accessibility, price range)
10. Guest shall book a reservation with guest details, room type (or specific room if allowed), and rate plan
11. System shall hold or commit inventory atomically during booking to prevent double-booking
12. Guest shall modify reservation (dates, room type, add-ons) subject to availability and fare rules
13. Guest shall cancel reservation; system shall apply cancellation policy and compute fees/refunds
14. System shall support group/master bookings and multiple rooms under one reservation
15. System shall support waitlist when sold out, with optional auto-convert on cancellation
16. OTA/channel reservations shall be ingested with external confirmation mapping
17. System shall send confirmation and modification notifications (email/SMS/push—interface level)
18. System shall maintain reservation lifecycle states (held, confirmed, in-house, checked-out, no-show, cancelled)

#### Check-In / Check-Out (FR19-FR24)
19. Staff shall check in guest: verify identity, assign physical room, capture payment method, issue keys/mobile key
20. System shall authorize security deposit or pre-auth on check-in per hotel policy
21. System shall extend or shorten stay (repricing, housekeeping impact)
22. Staff shall check out guest: finalize folio, settle payment, release room for housekeeping
23. System shall support early check-in / late checkout fees
24. System shall record no-show and release room per policy timing

#### Guest Management (FR25-FR29)
25. System shall create and update guest profiles (contact, preferences, ID documents metadata, special requests)
26. System shall link guests to reservations and stays; support primary guest and additional occupants
27. System shall enforce duplicate detection (email/phone) with merge workflow
28. System shall support corporate/travel agent profiles and negotiated rates
29. System shall restrict sensitive profile access via roles (front desk, manager, guest self-service)

#### Room Service (FR30-FR34)
30. Guest or staff shall place room service orders against an in-house stay
31. Menu shall be configurable per hotel with items, modifiers, allergens, and service hours
32. Orders shall follow status workflow (placed, acknowledged, preparing, out-for-delivery, delivered, cancelled)
33. Charges shall post to guest folio with line items and service charges
34. Kitchen/staff shall update order status and estimated time

#### Housekeeping & Maintenance (FR35-FR40)
35. System shall generate housekeeping tasks on checkout, stay-over, or manual request
36. Supervisor shall assign tasks to staff with priority and SLA
37. Room shall transition cleanliness states (dirty, cleaning, inspected, ready, blocked)
38. Maintenance shall block rooms from sale and from assignment until cleared
39. System shall support recurring PM tasks (deep clean, HVAC inspection)
40. Housekeeping shall report exceptions (damage, minibar restock needed)

#### Payments & Folio (FR41-FR48)
41. System shall collect deposit at booking (fixed or percentage) via payment gateway
42. System shall maintain folio per stay with charges, payments, adjustments, and taxes
43. System shall compute final bill including room, amenities, room service, fees, and taxes
44. System shall apply cancellation fees per policy and process partial refunds
45. System shall support split payment across cards and loyalty redemption
46. System shall record refunds, chargebacks, and reconciliation references
47. System shall support multi-currency display with settlement currency per hotel
48. System shall generate invoice/receipt artifacts

#### Dynamic Pricing (FR49-FR52)
49. System shall apply seasonal and day-of-week adjustments to BAR (best available rate)
50. System shall adjust prices based on occupancy and lead-time rules (demand-based)
51. System shall compose final quote from base rate + modifiers + taxes + fees
52. Pricing engine shall be pluggable (rule-based now, ML later)

#### Loyalty (FR53-FR56)
53. Guest shall enroll in loyalty program (chain-level or hotel-participating)
54. System shall accrue points/miles on eligible folio charges after stay or checkout
55. Guest shall redeem rewards (discount, upgrade, amenity voucher) subject to caps and blackout rules
56. System shall manage tier status and benefits (late checkout, breakfast) by evaluation period

#### Amenity Bookings (FR57-FR60)
57. Guest shall book spa treatments, restaurant reservations, or gym slots linked to stay dates
58. System shall enforce capacity, therapist/equipment resources, and opening hours
59. Guest shall modify/cancel amenity bookings per amenity policy
60. Charges shall post to folio or require prepayment depending on configuration

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many hotels, rooms, and concurrent users?"
- Hundreds of hotels, 10⁴–10⁶ room-nights searchable; flash sales and events spike booking concurrency
- Search is read-heavy; booking is write-heavy with contention on hot dates

**Deduced NFRs:**
- ✅ Horizontal scaling of read path (search index, caching of availability snapshots)
- ✅ Partitioning by `hotelId` for operational data; chain services for loyalty
- ✅ Async processing for notifications, loyalty posting, analytics
- ✅ Rate limiting on search and booking APIs to protect inventory services

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"
- No double-booking for the same physical room for overlapping stays
- Payment captures and refunds must match ledger entries
- Folio totals must equal sum of line items after tax rules

**Deduced NFRs:**
- ✅ **Strong consistency** for booking commits and inventory allocation (transaction or lease with fencing)
- ✅ **Serializable or serializable-per-key** isolation for `roomId + date range` hot spots
- ✅ **Eventual consistency** acceptable for search index lag (with staleness bounds)
- ✅ **Idempotent** payment and booking APIs (idempotency keys)

---

#### 3. **Availability Analysis**

**Think:** "What can degrade?"
- Booking must survive partial outages; search can serve slightly stale results with disclosure

**Deduced NFRs:**
- ✅ **99.9%+** for booking/checkout path; graceful read-only degradation for non-critical reports
- ✅ **Circuit breakers** to payment providers; queue retries with dead-letter handling
- ✅ **Multi-AZ** deployment for stateless tiers; durable queues for operational workflows

---

#### 4. **Maintainability Analysis**

**Think:** "Who operates this?"
- Revenue managers change rules; ops staff handle housekeeping; finance reconciles payments

**Deduced NFRs:**
- ✅ **Audit logs** for reservation changes, price quotes, role access to PII
- ✅ **Admin consoles** (policies, rate plans, menus, amenity schedules)
- ✅ **Feature flags** for overbooking, demand pricing intensity
- ✅ **Clear bounded contexts** (Booking, Stay, Folio, Housekeeping, Loyalty)

---

#### 5. **Performance Analysis**

**Think:** "SLAs for guest experience?"
- Search < 300ms p95; booking confirmation < 2s including payment auth
- Housekeeping board updates near real-time for front desk

**Deduced NFRs:**
- ✅ **Precomputed availability** or interval trees for date-range queries
- ✅ **Caching** of static catalog; short TTL cache for quoted prices
- ✅ **O(log n + k)** search over indexed room-night inventory (n = horizon, k = results)

---

#### 6. **Security Analysis**

**Think:** "Risks?"
- PCI scope for card data; PII exposure; insider fraud on discounts

**Deduced NFRs:**
- ✅ **Tokenized payments** (no raw PAN storage); vault integration
- ✅ **RBAC** and field-level masking for guest data
- ✅ **Fraud checks** on high-risk bookings (velocity, mismatched geography)
- ✅ **Signed webhooks** for OTA/channel partners

---

## 🧩 Step 3: Identify Core Entities (12-15 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Multiple hotels/branches" | Hotel, Branch, Address, Policy |
| "Room types, physical rooms" | RoomType, Room, Floor, Inventory |
| "Availability, pricing" | Availability, RatePlan, PriceQuote, InventoryHold |
| "Search, book, modify, cancel" | Reservation, BookingService, SearchCriteria |
| "Check-in, check-out" | Stay, Folio, CheckIn, CheckOut, KeyCard |
| "Room service" | RoomServiceOrder, MenuItem, KitchenTicket |
| "Housekeeping" | HousekeepingTask, Staff, RoomStatus |
| "Guest profile" | Guest, GuestProfile, CorporateAccount |
| "Deposits, final bill, fees" | Payment, Charge, Refund, CancellationPolicy |
| "Dynamic pricing" | PricingEngine, PricingRule, SeasonCalendar |
| "Loyalty" | LoyaltyProgram, Membership, Tier, RewardLedger |
| "Spa, restaurant, gym" | AmenityOffering, AmenityBooking, TimeSlot, Resource |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Address | ❌ NO | Value object on Hotel |
| SearchCriteria | ❌ NO | DTO for query |
| CheckIn / CheckOut | ❌ NO | Processes; model as Stay transitions + services |
| KitchenTicket | ⚠️ Optional | Could be value object inside RoomServiceOrder |
| PriceQuote | ❌ NO | Computed result (or short-lived aggregate) |
| SeasonCalendar | ✅ YES | Season definitions drive pricing |
| InventoryHold | ✅ YES | Short-lived lock with TTL |

### Final Entity List

**Property & Inventory:**
1. **Hotel** - Branch/property root
2. **RoomType** - Sellable category with capacity and attributes
3. **Room** - Physical room unit
4. **RatePlan** - Priced offer with rules (refundable, breakfast included)
5. **RoomNightInventory** - Sellable capacity per room type per night (or derived from rooms)
6. **InventoryHold** - Temporary reservation of capacity during checkout

**Booking & Stay:**
7. **Reservation** - Commercial contract for dates and products
8. **ReservationLine** - Line per room type / rate plan / date span
9. **Stay** - Operational in-house record linked to reservation
10. **RoomAssignment** - Maps stay to physical room for date range

**Guest & Parties:**
11. **Guest** - Person profile
12. **CorporateAccount** - B2B negotiated rates and billing

**Folio & Payments:**
13. **Folio** - Financial running account for a stay
14. **FolioLineItem** - Charge, payment, tax, adjustment
15. **PaymentIntent** - Gateway interaction record
16. **CancellationPolicy** - Fee schedule and windows

**Operations:**
17. **RoomServiceOrder** - F&B order for a stay
18. **MenuItem** - Catalog entry
19. **HousekeepingTask** - Work unit for staff
20. **MaintenanceBlock** - Room unavailability for repairs

**Pricing & Loyalty:**
21. **PricingRule** - Rule in engine (season, occupancy band, lead time)
22. **LoyaltyProgram** / **Membership** / **Tier**
23. **LoyaltyLedgerEntry** - Earn/burn/adjust events

**Amenities:**
24. **AmenityOffering** - Bookable offering (spa, table, class)
25. **AmenityBooking** - Confirmed slot with resources
26. **BookableSlot** / **Resource** - Capacity model

**Supporting:**
27. **TaxConfiguration**, **FeeDefinition** - Compliance and surcharges
28. **OTAChannelMapping** - External reference for sync

---

## 🔗 Step 4: Establish Relationships (12-18 minutes)

### Pass 1: Property & Inventory

#### Hotel ↔ RoomType / Room
**Conclusion:** **Composition** (types and rooms belong to one hotel)
```
Hotel ◆────→ RoomType [1..*]
Hotel ◆────→ Room [1..*]
Room ─────→ RoomType [1]
```

#### RoomType ↔ RatePlan
**Conclusion:** **Association** (many-to-many with restrictions)
```
RoomType ←────→ RatePlan  (many:many via join rules)
```

---

### Pass 2: Reservation & Stay

#### Reservation ↔ Guest / Hotel
**Conclusion:** **Association**
```
Reservation ─────→ Hotel [1]
Reservation ─────→ Guest [1] (primary)
Reservation ◆────→ ReservationLine [1..*]
```

#### Reservation ↔ Stay
**Conclusion:** **1:1** typical (extend for split stays if needed)
```
Reservation ─────→ Stay [0..1]
Stay ─────→ Folio [1]
Stay ◆────→ RoomAssignment [1..*]
RoomAssignment ─────→ Room [1]
```

---

### Pass 3: Operations & Payments

#### Stay ↔ RoomServiceOrder / AmenityBooking
**Conclusion:** **Composition** (orders belong to stay)
```
Stay ◆────→ RoomServiceOrder [0..*]
Stay ◆────→ AmenityBooking [0..*]
```

#### Folio ↔ FolioLineItem
**Conclusion:** **Composition**
```
Folio ◆────→ FolioLineItem [1..*]
```

#### Room ↔ HousekeepingTask / MaintenanceBlock
**Conclusion:** **Association**
```
Room ─────→ HousekeepingTask [0..*]
Room ─────→ MaintenanceBlock [0..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Hotel → Room | 1:N | Composition |
| Hotel → RoomType | 1:N | Composition |
| Room → RoomType | N:1 | Association |
| Reservation → ReservationLine | 1:N | Composition |
| Reservation → Stay | 1:0..1 | Association |
| Stay → RoomAssignment | 1:N | Composition |
| Stay → Folio | 1:1 | Composition |
| Guest → Membership | 1:N | Association |
| AmenityOffering → AmenityBooking | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (15-20 minutes)

### Class Diagram 1: Core Enums

```
┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>    │
│ ReservationStatus  │  │ StayStatus         │  │ RoomOperational    │
├────────────────────┤  ├────────────────────┤  │ Status             │
│ HELD               │  │ RESERVED           │  ├────────────────────┤
│ CONFIRMED          │  │ PRE_ARRIVAL        │  │ AVAILABLE          │
│ IN_HOUSE           │  │ IN_HOUSE           │  │ OCCUPIED           │
│ CHECKED_OUT        │  │ CHECKED_OUT       │  │ DIRTY              │
│ NO_SHOW            │  │ NO_SHOW            │  │ CLEANING           │
│ CANCELLED          │  └────────────────────┘  │ INSPECTED          │
└────────────────────┘                          │ READY              │
                                                │ BLOCKED_MAINT      │
                                                │ OUT_OF_ORDER       │
                                                └────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>    │
│ HousekeepingTask   │  │ RoomServiceOrder   │  │ PaymentStatus      │
│ Status             │  │ Status             │  ├────────────────────┤
├────────────────────┤  ├────────────────────┤  │ PENDING            │
│ OPEN               │  │ PLACED             │  │ AUTHORIZED         │
│ ASSIGNED           │  │ ACKNOWLEDGED       │  │ CAPTURED           │
│ IN_PROGRESS        │  │ PREPARING          │  │ REFUNDED           │
│ DONE               │  │ OUT_FOR_DELIVERY   │  │ FAILED             │
│ VERIFIED           │  │ DELIVERED          │  └────────────────────┘
└────────────────────┘  │ CANCELLED          │
                        └────────────────────┘
```

---

### Class Diagram 2: Hotel, Room Type, Room

```
┌────────────────────────────────────────────────────────────────────┐
│                            Hotel                                   │
├────────────────────────────────────────────────────────────────────┤
│ - hotelId: String                                                  │
│ - name: String                                                     │
│ - timezone: ZoneId                                                 │
│ - taxConfig: TaxConfiguration                                      │
│ - policies: HotelPolicy (checkInOut, deposit, overbooking)         │
├────────────────────────────────────────────────────────────────────┤
│ + getRoomTypes(): List<RoomType>                                   │
│ + getRooms(): List<Room>                                           │
└────────────────────────────────────────────────────────────────────┘
         │                                    │
         │ 1                                  │ 1
         ▼ *                                  ▼ *
┌──────────────────────┐            ┌──────────────────────┐
│      RoomType        │            │        Room          │
├──────────────────────┤            ├──────────────────────┤
│ - code: String       │◄───────────│ - roomId: String     │
│ - maxOccupancy: int  │    N:1     │ - floor: int         │
│ - bedConfig: String  │            │ - status: RoomOp...  │
│ - amenities: Set<>   │            │ - adaCompliant: bool │
├──────────────────────┤            ├──────────────────────┤
│ + baseCapacity(): int│            │ + block(reason): void│
└──────────────────────┘            │ + markReady(): void  │
                                    └──────────────────────┘
```

---

### Class Diagram 3: Reservation, Stay, Assignment

```
┌────────────────────────────────────────────────────────────────────┐
│                         Reservation                                │
├────────────────────────────────────────────────────────────────────┤
│ - reservationId: String                                            │
│ - hotelId: String                                                  │
│ - primaryGuestId: String                                           │
│ - status: ReservationStatus                                        │
│ - checkInDate: LocalDate                                           │
│ - checkOutDate: LocalDate                                          │
│ - cancellationPolicyId: String                                   │
│ - totalQuoted: Money                                               │
│ - idempotencyKey: String                                           │
├────────────────────────────────────────────────────────────────────┤
│ + addLine(line: ReservationLine): void                             │
│ + applyModification(cmd: ModifyCommand): void                      │
│ + cancel(now: Instant): CancellationResult                         │
└────────────────────────────────────────────────────────────────────┘
                              │ 1
                              │ composes
                              ▼ *
┌────────────────────────────────────────────────────────────────────┐
│                      ReservationLine                               │
├────────────────────────────────────────────────────────────────────┤
│ - roomTypeId: String                                               │
│ - ratePlanId: String                                               │
│ - adults: int / children: int                                      │
│ - dateRange: DateRange                                             │
│ - nightlyRates: List<Money>                                        │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                            Stay                                    │
├────────────────────────────────────────────────────────────────────┤
│ - stayId: String                                                   │
│ - reservationId: String                                            │
│ - status: StayStatus                                               │
│ - folio: Folio                          ◆──────────────────────┐     │
├────────────────────────────────────────────────────────────────────┤
│ + checkIn(ctx: CheckInContext): void                           │     │
│ + checkOut(ctx: CheckOutContext): void                         │     │
│ + postCharge(line: FolioLineItem): void                        │     │
└────────────────────────────────────────────────────────────────┼─────┘
         │ 1                                                     │     │
         │ composes                                              │     │
         ▼ *                                                     ▼     │
┌────────────────────────────┐                         ┌──────────────┴───┐
│     RoomAssignment         │                         │      Folio       │
├────────────────────────────┤                         ├──────────────────┤
│ - roomId: String           │                         │ - currency       │
│ - from: LocalDate          │                         │ - lines: List    │
│ - to: LocalDate            │                         ├──────────────────┤
└────────────────────────────┘                         │ + balance(): Money│
                                                       └──────────────────┘
```

---

### Class Diagram 4: Pricing & Inventory Hold

```
┌────────────────────────────────────────────────────────────────────┐
│                  <<interface>>                                     │
│                   PricingEngine                                    │
├────────────────────────────────────────────────────────────────────┤
│ + quote(req: PricingRequest): PriceQuote                           │
└────────────────────────────────────────────────────────────────────┘
                              △
                              │
              ┌───────────────┴────────────────┐
              │                              │
┌─────────────────────────┐    ┌─────────────────────────────┐
│ RuleBasedPricingEngine  │    │ OccupancyDemandMultiplier   │
│ - rules: List<PricingRule>│   │ (strategy helper)           │
├─────────────────────────┤    └─────────────────────────────┘
│ + quote(...)            │
└─────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                       InventoryHold                                  │
├────────────────────────────────────────────────────────────────────┤
│ - holdId: String                                                   │
│ - hotelId, roomTypeId, dateRange                                   │
│ - quantity: int                                                    │
│ - expiresAt: Instant                                               │
│ - status: ACTIVE | CONSUMED | RELEASED                            │
├────────────────────────────────────────────────────────────────────┤
│ + consume(): void                                                  │
│ + release(): void                                                  │
└────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Loyalty & Amenity

```
┌──────────────────────────────┐       ┌──────────────────────────────┐
│       LoyaltyProgram         │       │      AmenityOffering         │
├──────────────────────────────┤       ├──────────────────────────────┤
│ - programId: String          │       │ - offeringId: String         │
│ - earningRules: List<Rule>   │       │ - type: SPA | DINING | GYM  │
│ - tiers: List<Tier>          │       │ - durationMinutes: int       │
├──────────────────────────────┤       │ - resources: List<Resource>  │
│ + evaluateTier(m: Membership)  │       ├──────────────────────────────┤
└──────────────────────────────┘       │ + availableSlots(range): ... │
         △                              └──────────────────────────────┘
         │ 1                                        △
         │                                            │ 1
┌────────┴────────────┐                      ┌────────┴────────────────┐
│    Membership     │                      │   AmenityBooking      │
├───────────────────┤                      ├──────────────────────┤
│ - guestId: String │                      │ - bookingId: String  │
│ - points: long    │                      │ - stayId: String     │
│ - tier: Tier      │                      │ - slot: TimeSlot     │
│ - ledger: List<>  │                      │ - resourceIds: List  │
├───────────────────┤                      │ - status: Enum       │
│ + earn/ redeem()  │                      ├──────────────────────┤
└───────────────────┘                      │ + cancel(): void     │
                                           └──────────────────────┘
```

---

### Class Diagram 6: Booking & Payment Services (Application Layer)

```
┌────────────────────────────────────────────────────────────────────┐
│                    ReservationService                                │
├────────────────────────────────────────────────────────────────────┤
│ - inventory: InventoryService                                      │
│ - pricing: PricingEngine                                           │
│ - payments: PaymentService                                         │
│ - policies: PolicyService                                          │
├────────────────────────────────────────────────────────────────────┤
│ + search(criteria): SearchResults                                  │
│ + hold(req): InventoryHold                                         │
│ + confirm(holdId, pay: DepositSpec): Reservation                   │
│ + modify(id, cmd): Reservation                                     │
│ + cancel(id): CancellationResult                                   │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                      PaymentService                                  │
├────────────────────────────────────────────────────────────────────┤
│ + authorize(amount, method): PaymentIntent                         │
│ + capture(intentId, amount): PaymentIntent                         │
│ + refund(intentId, amount, reason): RefundRecord                    │
└────────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (30-40 minutes)

### Value Objects & Enums

```java
// Money.java (simplified)
public final class Money {
    private final BigDecimal amount;
    private final String currency;
    // constructors, add, multiply, negate...
}

// ReservationStatus.java
public enum ReservationStatus {
    HELD, CONFIRMED, IN_HOUSE, CHECKED_OUT, NO_SHOW, CANCELLED
}

// StayStatus.java
public enum StayStatus {
    RESERVED, PRE_ARRIVAL, IN_HOUSE, CHECKED_OUT, NO_SHOW
}
```

---

### Inventory Hold + Booking (Concurrency-Safe Sketch)

```java
// InventoryService.java — conceptual: use DB transaction or distributed lock per (hotelId, roomTypeId, night)
public class InventoryService {
    
    /**
     * Atomically decrements available inventory for room type across nights.
     * Throws if insufficient or overlapping maintenance blocks.
     */
    public InventoryHold createHold(String hotelId, String roomTypeId,
                                    LocalDate checkIn, LocalDate checkOut,
                                    int roomCount, Duration ttl) {
        // BEGIN TRANSACTION
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) {
            int available = roomNightDao.available(hotelId, roomTypeId, d);
            if (available < roomCount) {
                throw new InventoryException("Sold out for " + d);
            }
        }
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) {
            roomNightDao.decrementAvailable(hotelId, roomTypeId, d, roomCount);
        }
        InventoryHold hold = new InventoryHold(hotelId, roomTypeId, checkIn, checkOut, roomCount, ttl);
        holdDao.insert(hold);
        // COMMIT
        return hold;
    }
    
    public void releaseHold(String holdId) {
        // increment back all nights, mark hold RELEASED
    }
    
    public void consumeHold(String holdId, String reservationId) {
        // mark CONSUMED; inventory stays decremented
    }
}
```

---

### Pricing Engine (Seasonal + Demand)

```java
// PricingRequest.java
public class PricingRequest {
    private final String hotelId;
    private final String roomTypeId;
    private final String ratePlanId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guests;
    private final Instant quoteTime;
}

// PriceQuote.java
public class PriceQuote {
    private final List<NightlyRate> nightly;
    private final Money taxesAndFees;
    private final Money total;
    private final String quoteSignature; // hash of inputs + rules version
}

// RuleBasedPricingEngine.java
public class RuleBasedPricingEngine implements PricingEngine {
    private final List<PricingRule> rules;
    private final OccupancyProvider occupancyProvider;
    
    @Override
    public PriceQuote quote(PricingRequest req) {
        List<NightlyRate> nightly = new ArrayList<>();
        for (LocalDate d = req.getCheckIn(); d.isBefore(req.getCheckOut()); d = d.plusDays(1)) {
            Money base = ratePlanDao.getBase(req.getHotelId(), req.getRatePlanId(), req.getRoomTypeId(), d);
            PricingContext ctx = new PricingContext(
                d,
                occupancyProvider.occupancyPercent(req.getHotelId(), d),
                ChronoUnit.DAYS.between(req.getQuoteTime().atZone(ZoneId.systemDefault()).toLocalDate(), d)
            );
            for (PricingRule rule : rules) {
                base = rule.apply(base, ctx);
            }
            nightly.add(new NightlyRate(d, base));
        }
        Money subtotal = nightly.stream().map(NightlyRate::getAmount).reduce(Money.ZERO, Money::add);
        Money taxes = taxService.estimate(req.getHotelId(), subtotal);
        return new PriceQuote(nightly, taxes, subtotal.add(taxes), sign(req, nightly));
    }
}

// Example rule: seasonal multiplier
public class SeasonalMultiplierRule implements PricingRule {
    private final SeasonCalendar calendar;
    
    @Override
    public Money apply(Money base, PricingContext ctx) {
        BigDecimal mult = calendar.multiplierFor(ctx.getDate());
        return base.multiply(mult);
    }
}

// Example rule: high occupancy bump
public class OccupancySurgeRule implements PricingRule {
    @Override
    public Money apply(Money base, PricingContext ctx) {
        if (ctx.getOccupancyPercent() >= 0.90) {
            return base.multiply(new BigDecimal("1.15"));
        }
        return base;
    }
}
```

---

### Reservation Aggregate (Modify / Cancel)

```java
public class Reservation {
    private String reservationId;
    private String hotelId;
    private String primaryGuestId;
    private ReservationStatus status;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String cancellationPolicyId;
    private final List<ReservationLine> lines = new ArrayList<>();
    private Money totalQuoted;
    
    public void assertModifiable(Instant now, HotelPolicy policy) {
        if (status == ReservationStatus.CANCELLED || status == ReservationStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cannot modify");
        }
        if (policy.latestModificationDeadline(this.checkIn).isBefore(now)) {
            throw new IllegalStateException("Past modification cutoff");
        }
    }
    
    public CancellationResult cancel(Instant now, CancellationPolicy policy) {
        if (status == ReservationStatus.CANCELLED) {
            return CancellationResult.noop();
        }
        Money fee = policy.computeFee(checkIn, now, totalQuoted);
        status = ReservationStatus.CANCELLED;
        return new CancellationResult(fee, totalQuoted.subtract(fee));
    }
}
```

---

### Check-In / Check-Out Service

```java
public class StayService {
    private final RoomAssignmentService assignmentService;
    private final PaymentService payments;
    private final HousekeepingService housekeeping;
    
    public Stay checkIn(String reservationId, CheckInContext ctx) {
        Reservation res = reservationDao.get(reservationId);
        if (res.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reservation not confirmable");
        }
        List<Room> rooms = assignmentService.assignRooms(res, ctx.getPreferences());
        payments.authorizeDeposit(ctx.getPaymentMethod(), ctx.getDepositAmount());
        
        Stay stay = Stay.fromReservation(res, StayStatus.IN_HOUSE);
        for (Room r : rooms) {
            stay.addAssignment(new RoomAssignment(r.getRoomId(), res.getCheckIn(), res.getCheckOut()));
            roomDao.updateStatus(r.getRoomId(), RoomOperationalStatus.OCCUPIED);
        }
        res.setStatus(ReservationStatus.IN_HOUSE);
        stayDao.save(stay);
        reservationDao.save(res);
        return stay;
    }
    
    public Folio checkOut(String stayId, CheckOutContext ctx) {
        Stay stay = stayDao.get(stayId);
        Folio folio = stay.getFolio();
        Money balance = folio.balance();
        if (balance.isPositive()) {
            payments.capture(ctx.getPaymentMethod(), balance);
        } else if (balance.isNegative()) {
            payments.refund(ctx.getOriginalIntent(), balance.negate(), "checkout_adjustment");
        }
        stay.setStatus(StayStatus.CHECKED_OUT);
        reservationDao.get(stay.getReservationId()).setStatus(ReservationStatus.CHECKED_OUT);
        housekeeping.scheduleCheckoutTasks(stay.getRoomIds(), stay.getHotelId());
        loyaltyService.postEarn(stay); // async in production
        stayDao.save(stay);
        return folio;
    }
}
```

---

### Room Service Order

```java
public class RoomServiceOrder {
    private final String orderId;
    private final String stayId;
    private final List<Line> lines;
    private RoomServiceOrderStatus status;
    
    public void acknowledge() { this.status = RoomServiceOrderStatus.ACKNOWLEDGED; }
    public void markDelivered() { this.status = RoomServiceOrderStatus.DELIVERED; }
    
    public FolioLineItem toFolioCharge() {
        Money subtotal = lines.stream().map(Line::total).reduce(Money.ZERO, Money::add);
        return FolioLineItem.roomService(orderId, subtotal);
    }
    
    private static class Line {
        String menuItemId; int qty; Money unitPrice; List<String> modifiers;
        Money total() { return unitPrice.multiply(BigDecimal.valueOf(qty)); }
    }
}
```

---

### Housekeeping Task

```java
public class HousekeepingTask {
    private String taskId;
    private String hotelId;
    private String roomId;
    private HousekeepingTaskType type; // CHECKOUT_CLEAN, STAYOVER, INSPECT
    private HousekeepingTaskStatus status;
    private String assigneeStaffId;
    private Instant dueBy;
    
    public void complete() {
        this.status = HousekeepingTaskStatus.DONE;
    }
    
    public void verify() {
        this.status = HousekeepingTaskStatus.VERIFIED;
        roomDao.updateStatus(roomId, RoomOperationalStatus.READY);
    }
}
```

---

### Amenity Booking (Capacity)

```java
public class AmenityBookingService {
    
    public AmenityBooking book(String stayId, String offeringId, TimeSlot slot) {
        Stay stay = stayDao.get(stayId);
        offeringDao.assertWithinOperatingHours(offeringId, slot);
        // TRANSACTION: lock slot row or count bookings for resource
        if (!slotDao.hasCapacity(offeringId, slot)) {
            throw new BookingException("Slot full");
        }
        AmenityBooking b = new AmenityBooking(stayId, offeringId, slot);
        slotDao.incrementBookingCount(offeringId, slot);
        amenityBookingDao.save(b);
        return b;
    }
}
```

---

### Demo Scenario (Orchestrated)

```java
public class HotelManagementDemo {
    public static void main(String[] args) {
        // Search weekend in NYC property
        SearchCriteria c = new SearchCriteria("HTL_NYC_01",
            LocalDate.now().plusDays(7), LocalDate.now().plusDays(9), 2, 0);
        SearchResults results = reservationService.search(c);
        
        // Hold + deposit
        InventoryHold hold = inventoryService.createHold(
            c.getHotelId(), "DLX", c.getCheckIn(), c.getCheckOut(), 1, Duration.ofMinutes(10));
        PriceQuote quote = pricingEngine.quote(/* ... */);
        PaymentIntent dep = paymentService.authorize(quote.getTotal().multiply(new BigDecimal("0.20")), card());
        Reservation res = reservationService.confirm(hold.getHoldId(), dep);
        
        // Check-in assigns room 1204
        Stay stay = stayService.checkIn(res.getReservationId(), checkInCtx());
        
        // Room service + spa
        roomService.placeOrder(stay.getStayId(), menuOrder());
        amenityBookingService.book(stay.getStayId(), "SPA_DEEP_TISSUE", afternoonSlot());
        
        // Checkout settles folio
        Folio folio = stayService.checkOut(stay.getStayId(), checkOutCtx());
        System.out.println("Final balance settled: " + folio.balance());
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `PricingEngine`, cancellation/fee calculators, room assignment heuristics  
**Why:** Swap pricing and assignment algorithms per hotel or season without changing orchestration services  
**Interview Justification:** "Revenue rules change frequently; strategy keeps `ReservationService` stable."

---

### Pattern 2: State Pattern / Explicit State Machine
**Where:** `ReservationStatus`, `StayStatus`, `RoomOperationalStatus`, order statuses  
**Why:** Illegal transitions (e.g., deliver before placed) are rejected centrally  
**Interview Justification:** "Checkout-only operations must not run on a cancelled reservation."

---

### Pattern 3: Saga / Process Manager (Distributed)
**Where:** Confirm booking → payment capture → consume hold → notify → loyalty earn  
**Why:** Long-running flows with compensations (release hold on payment failure)  
**Interview Justification:** "Each step can fail independently; compensating actions maintain inventory and money consistency."

---

### Pattern 4: Repository + Unit of Work
**Where:** Persistence boundaries for `Reservation`, `Stay`, `Folio`  
**Why:** Transactional aggregates and test doubles  
**Interview Justification:** "Booking is an aggregate root; repository enforces load/save invariants."

---

### Pattern 5: Domain Events
**Where:** `ReservationConfirmed`, `GuestCheckedOut`, `RoomMarkedDirty`  
**Why:** Decouple housekeeping, analytics, and search index projection  
**Interview Justification:** "Front desk does not call housekeeping directly; events trigger task generation."

---

### Pattern 6: Specification Pattern (Optional)
**Where:** Complex search filters (ADA, floor range, connecting rooms)  
**Why:** Composable predicates for availability search  
**Interview Justification:** "Avoid exploding `search()` with boolean parameters."

---

## 💡 Step 8: Interview Discussion Points

### 1. Double-Booking Prevention vs Search Performance

**Interviewer:** "How do you scale search without selling the same room twice?"

**Answer:**
"**Separation of concerns:**

- **Search path** can use pre-aggregated `room_night` counts or a read model refreshed every N seconds for *browsing*.
- **Book path** must hit the **system of record** with **transactional decrement** (or `SELECT ... FOR UPDATE` on inventory rows per night) keyed by `(hotelId, roomTypeId, date)`.
- Optional **optimistic versioning** on inventory rows: if version mismatch on commit, retry quote.
- **Holds** are TTL-based; a scheduler releases expired holds back to inventory.

For **physical room assignment**, defer to check-in: booking reserves *capacity* for a room type; assignment picks a free physical `Room` matching type and constraints."

---

### 2. Dynamic Pricing Fairness & Auditing

**Interviewer:** "Guests compare prices—how do you explain differences?"

**Answer:**
"Store a **quote signature**: inputs (dates, occupancy, rule set version, demand snapshot id) and output nightly rates. On dispute, replay rules in a read-only auditor. Expose **fare rules** on confirmation (refundable vs prepaid). Consider **rate parity** constraints with OTAs as a separate channel rule layer."

---

### 3. Cancellation Fees & Refunds

**Interviewer:** "Walk through cancel 48 hours before arrival."

**Answer:**
"`CancellationPolicy` returns **fee = min(max(flat, percent * prepaid), cap)` based on hours-before-check-in tiers. If deposit was captured, **refund = prepaid - fee** via payment gateway partial refund. If only authorized, **void or partial capture**. Folio adjustments logged as `FolioLineItem` adjustments for audit."

---

### 4. Loyalty Accrual Timing

**Interviewer:** "When do points post?"

**Answer:**
"**After checkout** and successful settlement (or after no-show charge) to avoid earn on cancelled unpaid stays. Use **eligible charge categories** (room yes, taxes maybe no). **Redemption** at booking reduces cash total but may have tax implications—model as discount line item. Process earn **asynchronously** with idempotent ledger entries keyed by `stayId`."

---

### 5. Housekeeping vs Front Desk: Room Status Truth

**Interviewer:** "Who marks a room clean?"

**Answer:**
"**Housekeeping** completes task → **inspect** (optional rule per hotel) → status `READY`. **Front desk** assigns only `READY` rooms unless manager override. **Maintenance** sets `BLOCKED` which removes inventory from sale and assignment candidates."

---

### 6. Amenity Double-Book on Shared Resources

**Interviewer:** "Two guests book the same spa therapist?"

**Answer:**
"Model **Resource** (therapist, table, lane) with **capacity 1** per slot. Transaction locks `(resourceId, start, end)` or uses unique DB constraint. Offerings without named resources use **pool capacity** = min(rooms, stations)."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `InventoryService`: availability mutation only
- `PricingEngine`: price computation only
- `StayService`: operational check-in/out orchestration
- `PaymentService`: gateway interactions only

### Open/Closed ✅
```java
public class LeadTimeDiscountRule implements PricingRule { }
// Add new rule without modifying RuleBasedPricingEngine loop contract
```

### Liskov Substitution ✅
```java
PricingEngine engine = new RuleBasedPricingEngine(rulesA);
engine = new MLPricingEngineAdapter(model); // same interface for caller
```

### Interface Segregation ✅
```java
interface ReadInventory { SearchResults search(Criteria c); }
interface WriteInventory { InventoryHold hold(...); void release(String id); }
// Search service depends only on ReadInventory
```

### Dependency Inversion ✅
```java
public class ReservationService {
    private final PricingEngine pricing;
    private final PaymentService payments;
    public ReservationService(PricingEngine pricing, PaymentService payments) {
        this.pricing = pricing;
        this.payments = payments;
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Multi-hotel** model with partitioned inventory and shared loyalty/pricing rule templates
- ✅ **Transactional booking** with TTL **holds** to balance UX and oversell risk
- ✅ **Separated read models** for search vs **strong consistency** for confirm
- ✅ **Folio-centric** charging for room, service, amenities, fees, and taxes
- ✅ **Pluggable pricing** for seasonal and demand-based rates
- ✅ **Operational workflows** for housekeeping, maintenance blocks, and room service statuses
- ✅ **Idempotent payments** and **auditable** quotes/cancellations

### Operations & Guest Experience
- ✅ Check-in assigns **physical rooms** from **room type** capacity
- ✅ Checkout triggers **settlement**, **housekeeping tasks**, and **loyalty earn**
- ✅ Amenities use **resource/slot** capacity to prevent double booking

### Interview Hooks
- Discuss **overbooking**, **waitlist**, and **OTA sync** as extensions
- Mention **event sourcing** for reservation audit if interviewer goes deep

---

**Hotel Management System LLD — Hard difficulty — ready for review.**
