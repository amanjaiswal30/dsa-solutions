# Low-Level Design: Airline Management System

**Difficulty:** Hard 🔥

**Interview Duration:** 90-120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (8-10 minutes)

### What the Interviewer Says:
*"Design the core of an airline management system: scheduling flights on routes with aircraft assignment, selling seats across cabins with yield-based pricing, full booking and check-in flows including boarding passes and baggage, crew rostering, loyalty and ancillaries, codeshare and overbooking policies, and real-time flight status."*

### Clarifying Questions to Ask:

1. **Q:** Single airline or multi-carrier (alliance / codeshare as first-class concerns)?  
   **A:** Primary airline operates flights; codeshare partners market the same inventory under their flight numbers with revenue-sharing rules.

2. **Q:** How is seat inventory sold—by cabin only or specific seats at booking?  
   **A:** Inventory is tracked per flight leg and cabin; optional paid seat selection maps to physical seats; default auto-assign before departure.

3. **Q:** Booking modification and cancellation—what are the constraints?  
   **A:** Fare rules define change fees, refundability, and same-day standby; modifications re-price against current inventory and rules.

4. **Q:** Waitlist behavior when cabins are full?  
   **A:** Priority queue per flight/cabin; auto-confirm on cancellation or inventory release; optional manual clearance by inventory control.

5. **Q:** Check-in channels—do they share one state machine?  
   **A:** Yes: online, kiosk, and counter converge on a single check-in domain; counter can override (irregular ops, document issues).

6. **Q:** Baggage scope—interlining and transfers?  
   **A:** Model tags per segment with last-known scan events; interline handoff as optional extension (bag custody transfer event).

7. **Q:** Crew scheduling—regulatory constraints?  
   **A:** Duty time limits, rest periods, qualification matching (type rating, rank); solver may be external—system stores rosters and leg assignments.

8. **Q:** Dynamic pricing inputs?  
   **A:** Load factor forecasts, competitor reference fares (if available), booking curve, seasonality, events; rule engine now, pluggable ML later.

9. **Q:** Loyalty—accrual and redemption on own metal only?  
   **A:** Accrue on eligible fares and ancillaries; redeem for awards (inventory bucket) or upgrade certificates; tier from rolling qualification metrics.

10. **Q:** Overbooking—how are denied boardings handled?  
    **A:** Policy sets target overbook % per cabin; solicit volunteers first (VDB compensation offers); IDB only per regulatory workflow and audit trail.

11. **Q:** Flight status—who publishes updates?  
    **A:** Operations center and ACARS/ground systems emit events (delay, cancel, divert, gate change); subscribers include passengers, airports, and GDS.

12. **Q:** Scale and peak expectations?  
    **A:** Millions of searches/day, tens of thousands of concurrent bookings during sales; strong consistency on inventory commits; 99.95% for booking/check-in path.

13. **Q:** Compliance and PII?  
    **A:** PNR/ticket data, travel documents, and payment tokens require encryption, retention policies, audit logs, and SAR/export workflows.

---

## 🔹 Step 2: Gather Requirements (10-15 minutes)

### Functional Requirements

#### Network, Schedule & Aircraft (FR1-FR10)
1. System shall maintain airports with codes, terminals, curfews, and handling constraints
2. System shall define routes as ordered legs (origin → destination pairs) with block times and equipment restrictions
3. System shall publish flight schedules with flight numbers, effective dates, days-of-operation, and seasonal variations
4. System shall assign aircraft types and tail numbers to operating flights subject to maintenance and range rules
5. System shall support equipment swaps with seat-map compatibility checks or automated re-accommodation workflows
6. System shall track turn times, minimum connect times at hubs, and gate/slot placeholders (integration-level)
7. System shall record codeshare agreements: marketing vs operating carrier, applicable routes, and inventory participation rules
8. System shall expose operating and marketing flight views for search (same physical flight, multiple display numbers)
9. System shall support ad-hoc flights (charters, extra sections) with their own inventory buckets
10. System shall maintain aircraft configuration versions linked to seat maps and galley layouts

#### Seat Inventory & Cabins (FR11-FR18)
11. System shall represent cabins (First, Business, Economy) per flight with capacity and sell limits
12. System shall map physical seats to cabin, row, position, and attributes (exit, bassinet, blocked)
13. System shall enforce availability per cabin and optional per-seat locks for paid selection
14. System shall support married-segment logic and origin-destination level availability where fare rules require it
15. System shall manage inventory controls (allotments to channels, group blocks, crew rest seats)
16. System shall support upgrades and cabin changes subject to inventory and fare difference
17. System shall record no-show and go-show behavior for analytics and overbooking tuning
18. System shall synchronize seat state with check-in (assigned, blocked, occupied)

#### Booking & PNR (FR19-FR32)
19. User shall search itineraries by OD, dates, cabin preference, passenger count, and loyalty program
20. System shall price itineraries using filed fares, rules, surcharges, taxes, and dynamic yield adjustments
21. User shall book a PNR with passengers, contacts, SSRs (special service requests), and payment authorization
22. System shall hold seats optimistically with TTL; on payment success, commit inventory atomically
23. User shall modify PNR (dates, routing, names per policy) with repricing and change fees
24. User shall cancel PNR or individual coupons; refunds per fare rule and void window
25. System shall maintain waitlists ordered by priority (status, fare, time) per flight/cabin
26. System shall auto-confirm waitlist when inventory appears or staff releases seats
27. System shall issue e-ticket records with coupon status (open, flown, exchanged, refunded)
28. System shall support split PNR, name changes (fee), and infant/lap-child attachments
29. System shall integrate payment capture, authorization reversal, and chargeback references
30. System shall emit booking events to loyalty (accrual pending) and to operations (special loads)
31. System shall enforce idempotent booking APIs for retries from channels and OTAs
32. System shall support corporate and agency profiles with negotiated fares where configured

#### Check-In & Boarding (FR33-FR42)
33. Passenger shall check in via web/mobile within time windows per airport and flight
34. Passenger shall check in at kiosk with PNR lookup and document scan stub
35. Agent shall perform counter check-in with override capabilities and fee collection
36. System shall validate travel documents, visa requirements (rule stub), and watchlist screening handoff
37. System shall assign or confirm seats, issue boarding pass with gate, zone, sequence, and QR/barcode payload
38. System shall support bag drop after online check-in with tag printing integration
39. System shall enforce check-in cutoff and flight closure rules
40. System shall manage boarding zones and real-time gate updates propagated to boarding passes where supported
41. System shall record no-show at departure and trigger inventory/loyalty adjustments
42. System shall support standby and same-day change flows per policy

#### Boarding Pass & Documents (FR43-FR46)
43. System shall generate standardized boarding pass data (IATA BCBP-style logical record) for print and mobile wallet
44. System shall version boarding pass payloads when gate or time changes materially
45. System shall revoke superseded mobile passes and honor scan-once semantics at gate
46. System shall archive issued documents for regulatory retention

#### Baggage (FR47-FR54)
47. Passenger shall purchase and track extra baggage allowances linked to PNR
48. System shall issue baggage tags with unique identifiers and itinerary routing
49. System shall record scan events (check-in, security, sort, load, unload, arrival, mishand)
50. System shall notify passenger of delayed or misconnected bags with last known location
51. System shall compute excess baggage fees from rules and loyalty benefits
52. System shall support priority tagging for elite tiers and premium cabins
53. System shall model interline custody transfer as optional status transition with partner id
54. System shall integrate with weight and piece policy per route and aircraft

#### Crew Scheduling (FR55-FR60)
55. System shall maintain crew members, ranks, qualifications, and base assignments
56. System shall publish pairings and rosters assigning crew to flight legs
57. System shall enforce hard constraints (rest, duty limits, qualification on equipment) at assignment time
58. System shall support reserve crew activation and deadhead positioning segments
59. System shall track crew no-show/sick and replacement workflows
60. System shall expose crew manifests to operating flights (read-integrated from roster service)

#### Dynamic Pricing & Yield (FR61-FR66)
61. System shall compute bid prices or hurdle rates per cabin/OD/date from demand signals
62. System shall adjust published fares within fare class buckets using rules and floors/ceilings
63. System shall support bid-up for upgrades and last-seat premium pricing
64. System shall log price quotes with inputs for audit and regression testing
65. System shall throttle rapid repricing to avoid channel instability
66. System shall accept override campaigns (sales, flash) with start/end and inventory caps

#### Ancillary Services (FR67-FR72)
67. Catalog shall offer meals, Wi-Fi, lounge, extra baggage, and seat selection with availability rules
68. Passenger shall add ancillaries to PNR pre-departure subject to flight and channel rules
69. System shall price ancillaries dynamically where configured (e.g., exit row premium by demand)
70. System shall fulfill or revoke ancillaries on itinerary change with proration rules
71. System shall post ancillary revenue to accounting and loyalty earn where eligible
72. System shall support bundles and branded fare families mapping included ancillaries

#### Loyalty (FR73-FR80)
73. Member shall enroll with tier qualification metrics (miles, segments, spend) per program rules
74. System shall accrue redeemable miles and elite-qualifying credits after travel completion or billing cycle
75. Member shall redeem miles for award tickets subject to award inventory buckets
76. System shall apply tier benefits (baggage, priority boarding, lounge) at booking and check-in
77. System shall handle mileage expiration, promotions, and partner earn (stub)
78. System shall support upgrade instruments and waitlist for upgrades
79. System shall prevent double accrual on exchanged tickets via coupon lineage
80. System shall expose statement and balance APIs with dispute workflow

#### Codeshare (FR81-FR85)
81. System shall display partner-marketed flights on own-operated legs with correct operating carrier disclosure
82. System shall map inventory between marketing and operating flight instances per prorate agreement
83. Booking on marketing flight shall resolve to operating inventory and crew
84. System shall support checks that frequent flyer and ancillary policies follow marketing or operating rules per agreement
85. System shall handle irregular ops notifications across marketing and operating PNRs

#### Overbooking (FR86-FR90)
86. System shall configure authorized overbook limits per cabin/flight curve based on historical no-show
87. System shall forecast show-up probability and recommend inventory adjustments
88. At check-in/boarding, system shall identify shortage and initiate voluntary denied boarding (VDB) offer sequencing
89. System shall document involuntary denied boarding (IDB) with compensation calculation and regulatory reporting fields
90. System shall rebook protected passengers on alternate flights per contract of carriage rules

#### Flight Status & Irregular Ops (FR91-FR96)
91. System shall maintain flight operational status: scheduled, delayed, cancelled, diverted, returned
92. System shall apply delay reasons (ATC, weather, mechanical, crew) for analytics
93. System shall broadcast gate, terminal, and equipment changes to subscribers
94. System shall trigger rebooking suggestions and waivers on mass cancellations
95. System shall update ETA/ETD from operational feed and adjust connection protection flags
96. System shall archive disruption timeline for customer service and regulatory inquiry

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many flights, searches, and concurrent bookings?"
- Global network: 10³–10⁴ daily departures; peak holiday sales with massive search fan-out
- Seat-level contention on hot flights; ancillary and check-in spikes before departure windows

**Deduced NFRs:**
- ✅ Horizontal scale for stateless API tier; partition operational data by `flightId`, `date`, or `station`
- ✅ Read replicas and search indexes for availability with bounded staleness for browse; strict path for commit
- ✅ Caching of reference data (airports, schedules, seat maps) with versioned invalidation
- ✅ Async workers for notifications, loyalty posting, baggage telemetry ingestion, and pricing batch jobs

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"
- No double-sale of the same seat coupon beyond authorized overbook policy
- Payment state aligned with ticket and inventory commit
- Boarding pass sequence and gate data consistent at scan time with authoritative flight record

**Deduced NFRs:**
- ✅ **Strong consistency** for inventory commit (transaction, optimistic concurrency with version, or serializable per flight inventory shard)
- ✅ **Idempotent** booking and check-in endpoints with idempotency keys
- ✅ **Saga or outbox** for cross-service flows (payment ↔ ticketing ↔ loyalty)
- ✅ **Eventual consistency** acceptable for search indexes and status fan-out with SLA (e.g., < 60s for non-critical displays)

---

#### 3. **Availability Analysis**

**Think:** "What can degrade?"
- Search/browse may serve slightly stale availability; booking and airport check-in cannot afford extended outage

**Deduced NFRs:**
- ✅ **99.95%+** for booking, ticketing, and check-in; degraded read-only mode for status only during partial failures
- ✅ **Multi-AZ** active-active for APIs; durable queues for side effects
- ✅ **Circuit breakers** to payment and partner systems; graceful queueing at airport counters (offline stub)

---

#### 4. **Maintainability Analysis**

**Think:** "Who operates this?"
- Revenue management, network planning, airport staff, crew scheduling, customer service

**Deduced NFRs:**
- ✅ **Audit trails** for inventory changes, price quotes, PNR edits, IDB/VDB, and override actions
- ✅ **Admin and ops consoles** with role-based access (RBAC)
- ✅ **Feature flags** for pricing experiments, codeshare rollout, overbooking aggressiveness
- ✅ **Configurable rules** (fare families, fees, check-in windows) without redeploy where possible

---

#### 5. **Performance Analysis**

**Think:** "Latency expectations?"
- Search results: sub-second at P95 for typical queries
- Price quote: < 200 ms P95 on warm caches
- Booking commit: < 2 s including payment authorization handshake
- Check-in and boarding pass issue: < 3 s P95

**Deduced NFRs:**
- ✅ **O(1)** or **O(log n)** seat lookups via segment+cabin indexes and precomputed availability buckets where applicable
- ✅ **Bulkhead** isolation between search and commit workloads
- ✅ **Rate limiting** on search and hold creation to prevent inventory scraping

---

#### 6. **Security Analysis**

**Think:** "Risks?"
- PNR enumeration, fraudulent refunds, insider inventory manipulation, boarding pass forgery

**Deduced NFRs:**
- ✅ **Authentication** for agents; **strong customer auth** for manage-booking; CAPTCHA/rate limits on lookup
- ✅ **Encryption** at rest for PII and payment tokens; TLS in transit
- ✅ **Signed boarding pass payloads** and short-lived mobile wallet updates
- ✅ **SOC-style logging** for access to sensitive manifests and document data

---

## 🧩 Step 3: Identify Core Entities (12-18 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Routes and schedules" | Airport, Route, Leg, FlightInstance, SchedulePattern |
| "Aircraft assignment" | Aircraft, AircraftType, SeatMap, MaintenanceEvent |
| "Seat inventory" | CabinClass, Seat, InventoryBucket, SeatAssignment |
| "Booking" | PNR, Passenger, Ticket, Coupon, Fare, Payment |
| "Waitlist" | WaitlistEntry, PriorityRule |
| "Check-in" | CheckInRecord, BoardingPass, TravelDocument |
| "Baggage" | BaggageTag, BaggageEvent, Itinerary |
| "Crew" | CrewMember, Qualification, Roster, Duty |
| "Pricing" | PriceQuote, FareRule, YieldPolicy |
| "Ancillary" | AncillaryProduct, AncillaryOrder |
| "Loyalty" | LoyaltyProgram, MemberAccount, MileLedger |
| "Codeshare" | CodeshareAgreement, MarketingFlight, OperatingFlight |
| "Overbooking" | OverbookingPolicy, DeniedBoardingCase |
| "Status" | FlightStatus, OperationalEvent |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Airport | ✅ YES | Stable master with operational attributes |
| Route | ✅ YES | Logical OD path; may have many flight numbers |
| Leg | ✅ YES | Single takeoff-landing segment |
| FlightInstance | ✅ YES | Dated occurrence with tail assignment |
| SchedulePattern | ✅ YES | Recurrence template |
| Aircraft / AircraftType | ✅ YES | Equipment and capabilities |
| SeatMap | ✅ YES | Versioned layout |
| CabinClass | ✅ YES | Enum + per-flight capacity wrapper |
| Seat | ✅ YES | Physical seat with state per flight |
| InventoryBucket | ✅ YES | Sell limits / allotments |
| PNR | ✅ YES | Booking container |
| Passenger | ✅ YES | Person on PNR |
| Ticket / Coupon | ✅ YES | Financial and flight entitlement |
| Fare | ✅ YES | Priced offer snapshot |
| Payment | ✅ YES | Money movement record |
| WaitlistEntry | ✅ YES | Queue element with priority |
| CheckInRecord | ✅ YES | Per-passenger per-leg check-in |
| BoardingPass | ✅ YES | Issued document instance |
| BaggageTag | ✅ YES | Tracking unit |
| BaggageEvent | ✅ YES | Scan or exception |
| CrewMember | ✅ YES | Human resource |
| Roster / Duty | ✅ YES | Assignments (could merge; split for clarity) |
| PriceQuote | ✅ YES | Explainable pricing result |
| YieldPolicy | ✅ YES | Rule set or strategy config |
| AncillaryProduct | ✅ YES | Catalog item |
| AncillaryOrder | ✅ YES | Purchased line |
| LoyaltyProgram / MemberAccount | ✅ YES | Program and member state |
| MileLedger | ✅ YES | Transaction log (or Value Object on account) |
| CodeshareAgreement | ✅ YES | Contract terms |
| OverbookingPolicy | ✅ YES | Parameterized policy |
| DeniedBoardingCase | ✅ YES | Legal/commercial case record |
| FlightStatus | ✅ YES | Current operational snapshot |

### Final Entity List

**Network & Schedule:**
1. **Airport** – Location, timezone, constraints
2. **Route** – Logical path through legs
3. **Leg** – Origin, destination, block time
4. **SchedulePattern** – DOW, times, seasonal validity
5. **FlightInstance** – Dated flight with operating and marketing identifiers

**Fleet & Cabin:**
6. **AircraftType** – Capacity, range, seat map templates
7. **Aircraft** – Tail, maintenance state
8. **SeatMapVersion** – Rows, seats, cabin splits
9. **CabinInventory** – Per-flight cabin caps and controls
10. **SeatInventory** – Per-seat state for a flight instance

**Commercial:**
11. **FareBasis** / **FareRule** – Rules reference (simplified)
12. **PriceQuote** – Computed offer with inputs trace
13. **YieldPolicy** – Demand multipliers, floors

**Booking:**
14. **PNR** – Record locator, contacts, status
15. **Passenger** – Name, type, SSRs
16. **Ticket** – Ticket number, coupons
17. **FlightCoupon** – Segment entitlement (status, seat, cabin)
18. **PaymentIntent** – Auth/capture lifecycle
19. **WaitlistEntry** – Flight, cabin, priority, status

**Airport Journey:**
20. **CheckInContext** – Channel, station, agent id
21. **CheckInRecord** – Completed check-in per coupon
22. **BoardingPass** – Token, zone, sequence
23. **BaggageTag** – Weight/pieces, routing string
24. **BaggageEvent** – Type, location, timestamp

**Operations:**
25. **CrewMember** – Qualifications, base
26. **CrewAssignment** – Leg + role
27. **FlightOperationalStatus** – Delay, cancel, divert, reasons
28. **OperationalEvent** – Append-only timeline

**Products:**
29. **AncillaryProduct** – Meal, bag, seat, lounge
30. **AncillaryLineItem** – On PNR, fulfillment state

**Loyalty:**
31. **LoyaltyProgram** – Rules engine hook
32. **MemberAccount** – Balances, tier
33. **LoyaltyTransaction** – Accrual/redemption/adjustment

**Partners & Risk:**
34. **CodeshareAgreement** – Mapping marketing ↔ operating
35. **OverbookingPolicy** – Limits and escalation
36. **DeniedBoardingCase** – VDB/IDB outcome

**System:**
37. **InventoryHold** – TTL hold for booking flow
38. **IdempotencyRecord** – Dedup key store

---

## 🔗 Step 4: Establish Relationships (15-20 minutes)

### Pass 1: Schedule & Flight

#### Route ↔ Leg
**Conclusion:** **Composition** (route defined by ordered legs)
```
Route ◆────→ Leg [1..*]
```

#### SchedulePattern → FlightInstance
**Conclusion:** **Association** (pattern generates instances)
```
SchedulePattern ─────→ FlightInstance [0..*]
```

#### FlightInstance ↔ Aircraft
**Conclusion:** **Association** (assignment)
```
FlightInstance ─────→ Aircraft [1]
FlightInstance ─────→ SeatMapVersion [1]
```

---

### Pass 2: Inventory & Booking

#### FlightInstance ↔ CabinInventory / SeatInventory
**Conclusion:** **Composition**
```
FlightInstance ◆────→ CabinInventory [1..*]
FlightInstance ◆────→ SeatInventory [0..*]
```

#### PNR ↔ Passenger / Ticket / FlightCoupon
**Conclusion:** **Composition**
```
PNR ◆────→ Passenger [1..*]
PNR ◆────→ Ticket [0..*]
Ticket ◆────→ FlightCoupon [1..*]
```

#### FlightCoupon → FlightInstance
**Conclusion:** **Association**
```
FlightCoupon ─────→ FlightInstance [1]
```

#### WaitlistEntry → FlightInstance
**Conclusion:** **Association**
```
WaitlistEntry ─────→ FlightInstance [1]
WaitlistEntry ─────→ CabinClass [1]
```

---

### Pass 3: Check-In, Baggage, Crew, Codeshare

#### CheckInRecord → FlightCoupon
**Conclusion:** **Association** (1:1 per coupon per check-in completion)
```
CheckInRecord ─────→ FlightCoupon [1]
CheckInRecord ◆────→ BoardingPass [0..1]
```

#### BaggageTag → PNR / FlightCoupon
**Conclusion:** **Association**
```
BaggageTag ─────→ PNR [1]
BaggageTag ─────→ FlightCoupon [1] (outbound segment)
```

#### CrewAssignment → FlightInstance & CrewMember
**Conclusion:** **Association**
```
CrewAssignment ─────→ FlightInstance [1]
CrewAssignment ─────→ CrewMember [1]
```

#### Codeshare: Marketing vs Operating FlightInstance
**Conclusion:** **Association** (same physical operation)
```
FlightInstance (marketing) ─────→ FlightInstance (operating) [1]
CodeshareAgreement governs mapping
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Route → Leg | 1:N | Composition |
| FlightInstance → CabinInventory | 1:N | Composition |
| FlightInstance → SeatInventory | 1:N | Composition |
| PNR → Passenger | 1:N | Composition |
| Ticket → FlightCoupon | 1:N | Composition |
| FlightCoupon → FlightInstance | N:1 | Association |
| PNR → AncillaryLineItem | 1:N | Composition |
| MemberAccount → LoyaltyTransaction | 1:N | Composition |
| FlightInstance → CrewAssignment | 1:N | Association |
| Operating FlightInstance ↔ Marketing FlightInstance | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (18-25 minutes)

### Class Diagram 1: Enums

```
┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐
│<<enumeration>>   │  │<<enumeration>>   │  │<<enumeration>>      │
│  CabinClass      │  │  CouponStatus    │  │  FlightOpStatus     │
├──────────────────┤  ├──────────────────┤  ├────────────────────┤
│ FIRST            │  │ OPEN             │  │ SCHEDULED          │
│ BUSINESS         │  │ CHECKED_IN       │  │ DELAYED            │
│ ECONOMY          │  │ BOARDED          │  │ CANCELLED          │
└──────────────────┘  │ FLOWN            │  │ DIVERTED           │
                      │ EXCHANGED        │  │ RETURNED_TO_GATE   │
                      │ REFUNDED         │  └────────────────────┘
                      └──────────────────┘

┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐
│<<enumeration>>   │  │<<enumeration>>   │  │<<enumeration>>      │
│  CheckInChannel  │  │  BoardingZone    │  │  BaggageEventType  │
├──────────────────┤  ├──────────────────┤  ├────────────────────┤
│ WEB              │  │ ZONE_1..N        │  │ CHECKED            │
│ MOBILE           │  │ PRIORITY         │  │ SORTED             │
│ KIOSK            │  └──────────────────┘  │ LOADED             │
│ COUNTER          │                          │ UNLOADED           │
└──────────────────┘                          │ MISHANDLED         │
                                              └────────────────────┘
```

---

### Class Diagram 2: Flight & Inventory

```
┌─────────────────────────────────────────────────────────────────┐
│                      FlightInstance                              │
├─────────────────────────────────────────────────────────────────┤
│ - id: String                                                    │
│ - operatingFlightNumber: String                                 │
│ - marketingFlightNumbers: List<String>  // codeshare            │
│ - std, sta: Instant  // scheduled times                         │
│ - origin, dest: Airport                                         │
│ - opStatus: FlightOpStatus                                      │
│ - aircraft: Aircraft                                            │
│ - seatMap: SeatMapVersion                                       │
├─────────────────────────────────────────────────────────────────┤
│ + getAvailableSeats(cabin: CabinClass): int                     │
│ + tryHoldSeats(req: SeatHoldRequest): InventoryHold             │
│ + commitHold(holdId): void                                      │
│ + releaseHold(holdId): void                                     │
└─────────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌──────────────┐    ┌────────────────┐    ┌──────────────────┐
│ CabinInventory│    │ SeatInventory   │    │ OverbookingPolicy │
├──────────────┤    ├────────────────┤    ├──────────────────┤
│- cabin       │    │- seatId         │    │- limitPct        │
│- capacity    │    │- cabin          │    │- cabin           │
│- sold        │    │- status         │    │+ authorizedOversell│
│- authorized  │    │  (FREE,HELD,    │    └──────────────────┘
│  oversell    │    │   OCCUPIED)     │
└──────────────┘    └────────────────┘
```

---

### Class Diagram 3: PNR, Ticket, Coupon

```
┌─────────────────────────────────────────────────────────────────┐
│                           PNR                                   │
├─────────────────────────────────────────────────────────────────┤
│ - recordLocator: String                                           │
│ - contactEmail, phone: String                                     │
│ - status: PnrStatus                                             │
├─────────────────────────────────────────────────────────────────┤
│ + addPassenger(p: Passenger): void                              │
│ + addCoupon(...): FlightCoupon                                    │
│ + quoteModify(...): PriceQuote                                   │
└─────────────────────────────────────────────────────────────────┘
      │ ◆───────────────────────────────│
      ▼                               ▼
┌──────────────┐              ┌───────────────────────────────────┐
│ Passenger    │              │ Ticket                             │
├──────────────┤              ├───────────────────────────────────┤
│- passengerId │              │ - ticketNumber                    │
│- name         │              │ - passengerRef                    │
│- type (ADT/CHD│              ├───────────────────────────────────┤
│  /INF)       │              │ + getOpenCoupons(): List<Coupon>  │
└──────────────┘              └───────────────────────────────────┘
                                         │ ◆
                                         ▼
                              ┌───────────────────────────────────┐
                              │ FlightCoupon                       │
                              ├───────────────────────────────────┤
                              │ - couponId                        │
                              │ - flight: FlightInstance          │
                              │ - cabin: CabinClass               │
                              │ - status: CouponStatus            │
                              │ - seatId: Optional<String>        │
                              ├───────────────────────────────────┤
                              │ + canCheckIn(now): boolean        │
                              └───────────────────────────────────┘
```

---

### Class Diagram 4: Pricing & Ancillary

```
┌─────────────────────────────────────────────────────────────────┐
│                    <<interface>>                                 │
│                  PricingStrategy                                 │
├─────────────────────────────────────────────────────────────────┤
│ + quote(itinerary, pax, ctx: PricingContext): PriceQuote        │
└─────────────────────────────────────────────────────────────────┘
                         △
         ┌───────────────┴───────────────┐
         ▼                               ▼
┌─────────────────┐            ┌──────────────────────┐
│ RuleBasedPricing│            │ YieldAdjustedPricing │
├─────────────────┤            ├──────────────────────┤
│ + filed fares   │            │ + demand multipliers │
└─────────────────┘            │ + bid price floor    │
                               └──────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ AncillaryService                                                 │
├─────────────────────────────────────────────────────────────────┤
│ - catalog: Map<String, AncillaryProduct>                        │
│ + addToPnr(pnr, productId, flight): AncillaryLineItem           │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Check-In, Boarding Pass, Baggage

```
┌─────────────────────────────────────────────────────────────────┐
│ CheckInService                                                   │
├─────────────────────────────────────────────────────────────────┤
│ - flightRepo, pnrRepo, inventory: FlightInstance                │
├─────────────────────────────────────────────────────────────────┤
│ + checkIn(couponId, channel, ctx): BoardingPass                 │
│ + undoCheckIn(couponId): void  // time window                   │
└─────────────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│ BoardingPass                                                     │
├─────────────────────────────────────────────────────────────────┤
│ - token: String  // signed                                       │
│ - sequence: int                                                  │
│ - zone: BoardingZone                                             │
│ - gate: String                                                   │
│ - version: int                                                   │
├─────────────────────────────────────────────────────────────────┤
│ + toBarcodePayload(): String                                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ BaggageTag                                                       │
├─────────────────────────────────────────────────────────────────┤
│ - tagNumber: String                                              │
│ - pnr: PNR                                                       │
│ - coupon: FlightCoupon                                           │
│ - pieces, weightKg: int                                        │
├─────────────────────────────────────────────────────────────────┤
│ + recordEvent(type, station): void                               │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│ BaggageEvent                                                     │
├─────────────────────────────────────────────────────────────────┤
│ - type: BaggageEventType                                         │
│ - station: Airport                                               │
│ - at: Instant                                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Loyalty & Denied Boarding

```
┌─────────────────────────────────────────────────────────────────┐
│ MemberAccount                                                    │
├─────────────────────────────────────────────────────────────────┤
│ - memberId: String                                               │
│ - tier: TierStatus                                               │
│ - redeemableMiles: long                                          │
├─────────────────────────────────────────────────────────────────┤
│ + accrue(tx: LoyaltyTransaction): void                           │
│ + redeem(miles, purpose): void                                   │
└─────────────────────────────────────────────────────────────────┘
                    │ ◆
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│ LoyaltyTransaction                                               │
├─────────────────────────────────────────────────────────────────┤
│ - type (ACCRUAL, REDEMPTION, ADJ)                                │
│ - miles: long                                                    │
│ - reference: String  // PNR / ticket                             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ DeniedBoardingCoordinator                                        │
├─────────────────────────────────────────────────────────────────┤
│ + solicitVolunteers(flight, shortage): List<Offer>               │
│ + confirmVDB(passenger, compensation): DeniedBoardingCase        │
│ + processIDB(...): DeniedBoardingCase                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35-45 minutes)

### Key Types & Holds

```java
// CabinClass.java
public enum CabinClass { FIRST, BUSINESS, ECONOMY }

// CouponStatus.java
public enum CouponStatus {
    OPEN, CHECKED_IN, BOARDED, FLOWN, EXCHANGED, REFUNDED
}

// FlightOpStatus.java
public enum FlightOpStatus {
    SCHEDULED, DELAYED, CANCELLED, DIVERTED, RETURNED_TO_GATE
}
```

```java
// InventoryHold.java — optimistic hold during booking
public final class InventoryHold {
    private final String holdId;
    private final String flightId;
    private final Map<CabinClass, Integer> seatsByCabin;
    private final Instant expiresAt;
    private final int version; // for OCC on flight inventory

    public InventoryHold(String holdId, String flightId,
                         Map<CabinClass, Integer> seatsByCabin,
                         Instant expiresAt, int version) {
        this.holdId = holdId;
        this.flightId = flightId;
        this.seatsByCabin = Map.copyOf(seatsByCabin);
        this.expiresAt = expiresAt;
        this.version = version;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public String getHoldId() { return holdId; }
    public String getFlightId() { return flightId; }
    public Map<CabinClass, Integer> getSeatsByCabin() { return seatsByCabin; }
    public int getVersion() { return version; }
}
```

```java
// FlightInstance.java (inventory core — simplified)
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlightInstance {
    private final String id;
    private final EnumMap<CabinClass, CabinInventory> cabins;
    private final Map<String, SeatState> seats; // seatId -> state
    private volatile int inventoryVersion = 0;
    private final Map<String, InventoryHold> activeHolds = new ConcurrentHashMap<>();
    private FlightOpStatus opStatus = FlightOpStatus.SCHEDULED;
    private final AtomicInteger boardingSeq = new AtomicInteger(0);
    private String gate = "A12";

    public FlightInstance(String id, EnumMap<CabinClass, CabinInventory> cabins,
                          Map<String, SeatState> seats) {
        this.id = id;
        this.cabins = cabins;
        this.seats = new ConcurrentHashMap<>(seats);
    }

    public String getGate() { return gate; }

    public int nextBoardingSequence() { return boardingSeq.incrementAndGet(); }

    public synchronized Optional<InventoryHold> createHold(
            Map<CabinClass, Integer> requested, Duration ttl) {
        // Validate availability including authorized oversell caps
        for (var e : requested.entrySet()) {
            CabinInventory c = cabins.get(e.getKey());
            if (!c.canSell(e.getValue())) {
                return Optional.empty();
            }
        }
        for (var e : requested.entrySet()) {
            cabins.get(e.getKey()).reserve(e.getValue());
        }
        String holdId = UUID.randomUUID().toString();
        InventoryHold hold = new InventoryHold(holdId, id, requested,
                Instant.now().plus(ttl), ++inventoryVersion);
        activeHolds.put(holdId, hold);
        return Optional.of(hold);
    }

    public synchronized void commitHold(String holdId) {
        InventoryHold h = activeHolds.remove(holdId);
        if (h == null || h.isExpired(Instant.now())) {
            throw new IllegalStateException("Invalid or expired hold");
        }
        for (var e : h.getSeatsByCabin().entrySet()) {
            cabins.get(e.getKey()).confirmSale(e.getValue());
        }
    }

    public synchronized void releaseHold(String holdId) {
        InventoryHold h = activeHolds.remove(holdId);
        if (h == null) return;
        for (var e : h.getSeatsByCabin().entrySet()) {
            cabins.get(e.getKey()).releaseReservation(e.getValue());
        }
    }

    public void updateOperationalStatus(FlightOpStatus s, String reason) {
        this.opStatus = s;
        // publish OperationalEvent to subscribers
    }

    public FlightOpStatus getOpStatus() { return opStatus; }
    public String getId() { return id; }
}
```

```java
// CabinInventory.java
public class CabinInventory {
    private final CabinClass cabin;
    private int capacity;
    private int sold;
    private int held;
    private int authorizedOversell; // from OverbookingPolicy

    public CabinInventory(CabinClass cabin, int capacity, int authorizedOversell) {
        this.cabin = cabin;
        this.capacity = capacity;
        this.authorizedOversell = authorizedOversell;
    }

    public boolean canSell(int n) {
        int maxSellable = capacity + authorizedOversell;
        return sold + held + n <= maxSellable;
    }

    public void reserve(int n) { held += n; }

    public void releaseReservation(int n) { held -= n; }

    public void confirmSale(int n) {
        held -= n;
        sold += n;
    }

    public CabinClass getCabin() { return cabin; }
}
```

```java
// SeatState.java
public enum SeatState { FREE, HELD, OCCUPIED, BLOCKED }
```

---

### Pricing: Strategy + Yield

```java
// PricingContext.java
public record PricingContext(
        Instant queryTime,
        double loadFactor,
        int daysBeforeDeparture,
        Optional<String> promoCode
) {}

// PriceQuote.java
public record PriceQuote(
        String quoteId,
        Money baseFare,
        Money surcharges,
        Money taxes,
        Money total,
        String fareBasis,
        Map<String, String> debugInputs // yield factors for audit
) {}
```

```java
// PricingStrategy.java
public interface PricingStrategy {
    PriceQuote quote(Itinerary itin, List<PassengerSpec> pax, PricingContext ctx);
}

// YieldAdjustedPricing.java
public class YieldAdjustedPricing implements PricingStrategy {
    private final FareRepository fares;
    private final YieldPolicy policy;

    public YieldAdjustedPricing(FareRepository fares, YieldPolicy policy) {
        this.fares = fares;
        this.policy = policy;
    }

    @Override
    public PriceQuote quote(Itinerary itin, List<PassengerSpec> pax, PricingContext ctx) {
        Money base = fares.lookupBase(itin);
        double mult = policy.multiplier(ctx.loadFactor(), ctx.daysBeforeDeparture());
        Money adjusted = base.multiply(mult);
        Money taxes = TaxEngine.estimate(itin, adjusted);
        return new PriceQuote(
                UUID.randomUUID().toString(),
                adjusted, Money.zero(), taxes, adjusted.add(taxes),
                fares.resolveFareBasis(itin),
                Map.of("mult", String.valueOf(mult))
        );
    }
}
```

---

### Booking Service (idempotent)

```java
// BookingService.java
public class BookingService {
    private final FlightRepository flights;
    private final PnrRepository pnrs;
    private final PaymentClient payments;
    private final Map<String, String> idempotency = new ConcurrentHashMap<>();

    public BookingResult book(BookingRequest req) {
        if (req.idempotencyKey() != null) {
            String existing = idempotency.get(req.idempotencyKey());
            if (existing != null) {
                return BookingResult.existing(existing);
            }
        }

        FlightInstance fl = flights.get(req.flightId());
        Optional<InventoryHold> hold = fl.createHold(req.cabinCounts(), Duration.ofMinutes(15));
        if (hold.isEmpty()) {
            return BookingResult.soldOut();
        }

        try {
            payments.authorize(req.payment());
            fl.commitHold(hold.get().getHoldId());
            PNR pnr = pnrs.create(req, hold.get());
            if (req.idempotencyKey() != null) {
                idempotency.put(req.idempotencyKey(), pnr.locator());
            }
            return BookingResult.confirmed(pnr.locator());
        } catch (Exception e) {
            fl.releaseHold(hold.get().getHoldId());
            throw e;
        }
    }
}

// Stubs for demo compile
record BookingRequest(String flightId, Map<CabinClass, Integer> cabinCounts,
                      Payment payment, String idempotencyKey) {}
record Payment() {}
record PassengerSpec() {}
class Itinerary {}
interface FlightRepository { FlightInstance get(String id); }
interface PnrRepository { PNR create(BookingRequest r, InventoryHold h); }
class PNR { String locator() { return "ABC123"; } }

class BookingResult {
    static BookingResult soldOut() { return new BookingResult(); }
    static BookingResult confirmed(String loc) { return new BookingResult(); }
    static BookingResult existing(String loc) { return new BookingResult(); }
}
```

---

### Check-In & Boarding Pass

```java
// CheckInChannel.java
public enum CheckInChannel { WEB, MOBILE, KIOSK, COUNTER }

// CheckInService.java
public class CheckInService {
    private final FlightRepository flights;
    private final PnrRepository pnrs;
    private final BoardingPassSigner signer;

    public BoardingPass checkIn(String couponId, CheckInChannel channel, Instant now) {
        FlightCoupon c = pnrs.findCoupon(couponId);
        FlightInstance fl = flights.get(c.getFlightId());
        if (fl.getOpStatus() == FlightOpStatus.CANCELLED) {
            throw new IllegalStateException("Flight cancelled");
        }
        if (!c.canCheckIn(now)) {
            throw new IllegalStateException("Outside check-in window");
        }
        c.setStatus(CouponStatus.CHECKED_IN);
        int seq = fl.nextBoardingSequence();
        BoardingPass bp = new BoardingPass(
                signer.sign(couponId + "|" + seq),
                seq,
                BoardingZone.fromCabin(c.getCabin()),
                fl.getGate(),
                1
        );
        return bp;
    }
}

// FlightCoupon partial API for demo
class FlightCoupon {
    private CouponStatus status = CouponStatus.OPEN;
    private CabinClass cabin;
    private String flightId;
    boolean canCheckIn(Instant now) { return true; }
    void setStatus(CouponStatus s) { this.status = s; }
    String getFlightId() { return flightId; }
    CabinClass getCabin() { return cabin; }
}

class BoardingPass {
    public BoardingPass(String token, int sequence, BoardingZone zone, String gate, int version) {}
}

enum BoardingZone {
    ZONE_1, ZONE_2, PRIORITY;

    static BoardingZone fromCabin(CabinClass c) {
        return c == CabinClass.ECONOMY ? ZONE_2 : ZONE_1;
    }
}

class BoardingPassSigner {
    String sign(String payload) { return Base64.getEncoder().encodeToString(payload.getBytes()); }
}
```

---

### Waitlist (priority queue sketch)

```java
// WaitlistService.java
import java.util.*;

public class WaitlistService {
    private final NavigableSet<WaitlistEntry> queue =
            new TreeSet<>(Comparator
                    .comparing(WaitlistEntry::tierPriority).reversed()
                    .thenComparing(WaitlistEntry::fareClassRank)
                    .thenComparing(WaitlistEntry::createdAt));

    public void enqueue(WaitlistEntry e) { queue.add(e); }

    public Optional<WaitlistEntry> popNextForFlight(String flightId, CabinClass cabin) {
        return queue.stream()
                .filter(w -> w.flightId().equals(flightId) && w.cabin() == cabin)
                .findFirst();
    }
}

record WaitlistEntry(
        String id,
        String flightId,
        CabinClass cabin,
        int tierPriority,
        int fareClassRank,
        Instant createdAt
) {}
```

---

### Denied Boarding & Overbooking

```java
// DeniedBoardingCoordinator.java
public class DeniedBoardingCoordinator {
    public List<VolunteerOffer> solicitVolunteers(FlightInstance fl, int shortage) {
        List<VolunteerOffer> offers = new ArrayList<>();
        for (int i = 0; i < shortage + 2; i++) { // oversolicit volunteers
            offers.add(new VolunteerOffer(fl.getId(), Money.of("400")));
        }
        return offers;
    }

    public DeniedBoardingCase confirmVDB(String passengerId, Money compensation) {
        return new DeniedBoardingCase(passengerId, compensation, CaseType.VDB);
    }
}

record VolunteerOffer(String flightId, Money compensation) {}
record DeniedBoardingCase(String passengerId, Money compensation, CaseType type) {}
enum CaseType { VDB, IDB }

// Money stub
class Money {
    static Money of(String s) { return new Money(); }
    Money add(Money o) { return this; }
    static Money zero() { return new Money(); }
    Money multiply(double d) { return this; }
}
```

---

### Flight Status Publisher

```java
// FlightStatusService.java
public class FlightStatusService {
    public void applyEvent(String flightId, OperationalEvent evt) {
        // persist append-only timeline
        // update FlightInstance opStatus + ETA
        // notify subscribers: PNR contacts, airport FIDS, GDS adapters
    }
}

record OperationalEvent(
        String flightId,
        FlightOpStatus newStatus,
        Optional<Instant> newEta,
        String reasonCode
) {}
```

---

### Demo

```java
// AirlineSystemDemo.java
public class AirlineSystemDemo {
    public static void main(String[] args) {
        EnumMap<CabinClass, CabinInventory> cabins = new EnumMap<>(CabinClass.class);
        cabins.put(CabinClass.ECONOMY, new CabinInventory(CabinClass.ECONOMY, 180, 5));
        cabins.put(CabinClass.BUSINESS, new CabinInventory(CabinClass.BUSINESS, 24, 1));

        FlightInstance flight = new FlightInstance("AA100-2026-04-08", cabins, Map.of());
        Optional<InventoryHold> hold = flight.createHold(
                Map.of(CabinClass.ECONOMY, 2), java.time.Duration.ofMinutes(10));
        System.out.println("Hold created: " + hold.isPresent());
        hold.ifPresent(h -> flight.commitHold(h.getHoldId()));

        YieldAdjustedPricing pricing = new YieldAdjustedPricing(
                new FareRepository() {}, new YieldPolicy() {});
        // ... quote and book flow

        flight.updateOperationalStatus(FlightOpStatus.DELAYED, "ATC");
        System.out.println("Status: " + flight.getOpStatus());
    }
}

interface FareRepository {}
interface YieldPolicy {
    double multiplier(double load, int dbd);
}
class TaxEngine {
    static Money estimate(Itinerary it, Money m) { return Money.zero(); }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `PricingStrategy` (rule-based vs yield-adjusted vs bid-price)  
**Why:** Revenue management evolves; strategies change without rewriting booking core  
**Interview Justification:** "Airlines tune pricing daily. Strategy encapsulates valuation logic while `BookingService` depends on the interface."

---

### Pattern 2: Saga / Outbox (conceptual)
**Where:** Booking = inventory commit + payment + ticketing + optional loyalty accrual queue  
**Why:** Distributed steps need compensating actions (release hold, void auth)  
**Interview Justification:** "I would use a local outbox table and async consumers so we never commit inventory without a matching durable intent."

---

### Pattern 3: State Machine
**Where:** `CouponStatus`, `FlightOpStatus`, check-in eligibility  
**Why:** Illegal transitions (e.g., board without check-in) must be rejected  
**Interview Justification:** "Gate scan and counter overrides still flow through the same state rules for auditability."

---

### Pattern 4: Repository + Domain Service
**Where:** `FlightRepository`, `PnrRepository`, `CheckInService`  
**Why:** Keeps aggregates consistent and testable  
**Interview Justification:** "Flight inventory is an aggregate root; PNR is another—cross-aggregate operations go through application services."

---

### Pattern 5: Template Method / Hook (optional)
**Where:** Check-in pipeline: validate → seat → baggage → pay fees → issue pass  
**Why:** Channel-specific steps (kiosk scan vs counter override) plug into the same skeleton  
**Interview Justification:** "Web skips document scan; counter adds supervisor PIN—shared template reduces duplication."

---

## 💡 Step 8: Interview Discussion Points

### 1. Inventory: How to Prevent Double Booking at Scale?

**Answer:**
"**Per flight-date shard** as the consistency boundary: all seat sells for `flightInstanceId` go through one transactional store partition or use **optimistic locking** on an inventory version.

**Flow:**
1. `createHold` decrements available-to-sell (or increments held) under lock.
2. Payment authorized.
3. `commitHold` moves held → sold in the same transaction as ticket issuance.

**Waitlist** consumes inventory on cancel via ordered queue; **overbooking** raises `authorizedOversell` cap so `canSell` allows more than physical seats until policy says stop."

---

### 2. Codeshare: One Operation, Two Flight Numbers?

**Answer:**
"Model a single **operating** `FlightInstance` with tail and crew. **Marketing** flight numbers are aliases pointing to the same internal id for inventory.

Search expands marketing flights to operating inventory. Fare and FF rules may follow marketing carrier per **CodeshareAgreement**—store which rule set applies so pricing and loyalty don't mix incorrectly."

---

### 3. Dynamic Pricing vs Published Fares?

**Answer:**
"Regulated markets use **filed fares** in buckets (RBDs). Yield management moves **availability** of buckets or applies **authorized adjustments** within fare rules.

Interview answer: expose **PriceQuote** with inputs (load factor, DBD, competitor ref) for audit. Start **rule-based** multipliers; later swap `PricingStrategy` for ML model returning hurdle rates without changing booking API."

---

### 4. Overbooking: Operational Algorithm?

**Answer:**
"**Historical no-show rate** by route/cabin → set oversell cap. At departure, if `show-ups > seats`, run **VDB auction** (increasing compensation) until enough volunteers. **IDB** only with documented order (e.g., last check-in without special needs) and regulatory compensation.

Persist **DeniedBoardingCase** with amounts, reason, and passenger id for compliance."

---

### 5. Check-In & Boarding Pass Security?

**Answer:**
"**Signed JWT or HMAC** payload with flight id, coupon id, sequence, expiry. Gate validates signature and scans once (idempotent consume). **Version** bumps on gate/time change; mobile wallet gets push update. Counter can reprint with agent auth logged."

---

### 6. Baggage: Event Sourcing?

**Answer:**
"Each **BaggageTag** has append-only **BaggageEvent** stream (checked, sorted, loaded…). MISCONNECT rules compare tag timeline to flight milestones. Optional **interline** = new event with partner custody id. Queries for 'where is my bag' read last N events."

---

### 7. Crew: In Scope for LLD?

**Answer:**
"Treat **CrewAssignment** as integration: hard constraints validated by roster service. Flight domain only needs **manifest** read model for weight/balance and regulatory headcount. Don't implement full solver in interview—define interfaces."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `FlightInstance`: inventory and operational pointer for one departure
- `BookingService`: orchestrates hold/pay/commit
- `CheckInService`: check-in and boarding pass issuance
- `DeniedBoardingCoordinator`: disruption compensation workflow

### Open/Closed ✅
```java
public class BidPricePricing implements PricingStrategy { }
// Add new pricing without modifying BookingService
```

### Liskov Substitution ✅
```java
PricingStrategy s = new YieldAdjustedPricing(...);
s = new RuleBasedPricing(...); // same contract
```

### Interface Segregation ✅
```java
interface InventoryPort { InventoryHold hold(...); }
interface Notifier { void notifyFlightChange(String pnr); }
// BookingService depends on small ports, not a fat god interface
```

### Dependency Inversion ✅
```java
public class BookingService {
    private final FlightRepository flights;
    private final PaymentClient payments;
    // depend on abstractions, inject mocks in tests
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **FlightInstance** as aggregate for seat/cabin inventory with **hold → commit** and **versioned concurrency**
- ✅ **PNR / Ticket / Coupon** model maps cleanly to GDS reality and change/refund flows
- ✅ **PricingStrategy** for yield and demand-based pricing behind stable quote API
- ✅ **Codeshare** as marketing alias → operating inventory
- ✅ **Overbooking** as explicit caps + **VDB/IDB** case records
- ✅ **Operational events** for delay/cancel/divert driving notifications and rebooking hooks

### Critical Paths
- ✅ Search (eventually consistent) vs **booking commit** (strongly consistent)
- ✅ **Idempotent** booking and payment alignment
- ✅ **Check-in** state machine + **signed boarding pass**
- ✅ **Baggage** timeline for tracking and mishandling

### Interview Story
- Start from **inventory + booking**, add **pricing**, then **check-in/boarding**, then **loyalty & ancillaries**, then **codeshare & overbooking**, close with **ops status** and **scale/consistency** tradeoffs.

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
