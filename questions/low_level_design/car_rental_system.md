# Low-Level Design: Car Rental System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a car rental platform (similar to Hertz, Enterprise, or Zipcar) that manages vehicle inventory across branches, supports reservations and rentals, handles pricing and payments, and tracks maintenance and loyalty."*

### Clarifying Questions to Ask:

1. **Q:** Who are the primary users—consumers only, or also fleet admins and branch staff?  
   **A:** Customers book online or in-branch; staff handle pickup, return, inspections, and maintenance.

2. **Q:** Is this one-way rentals (different drop-off) or round-trip only?  
   **A:** Support both; one-way may incur a repositioning fee.

3. **Q:** How do we model vehicle types vs individual VINs?  
   **A:** Search is often by class (economy, SUV); assignment is a specific vehicle at pickup.

4. **Q:** What payment models—pay now, pay at counter, deposits?  
   **A:** Support authorization at booking, capture at pickup, and final settlement at return (damage, fuel, late fees).

5. **Q:** Cancellation and modification rules?  
   **A:** Free cancel within window; modifications subject to availability and repricing.

6. **Q:** Insurance and add-ons?  
   **A:** Collision waiver, liability, GPS, child seats, additional drivers—each priced per day or flat.

7. **Q:** Loyalty scope?  
   **A:** Points on spend, tier benefits (upgrade priority, fee waivers), partner earning rules.

8. **Q:** Maintenance—block vehicles automatically?  
   **A:** Yes; vehicles in service or inspection are not rentable until cleared.

9. **Q:** Concurrency expectations?  
   **A:** Many users search same dates; prevent double-booking the same physical car; idempotent payments.

10. **Q:** Scale and regions?  
    **A:** Hundreds of branches, tens of thousands of vehicles, peak holiday demand; multi-timezone pricing.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Vehicle Inventory & Locations (FR1-FR8)
1. System shall maintain a catalog of vehicle types (class, make/model template, baggage capacity, transmission).
2. System shall track individual vehicles by unique identifier (VIN or internal ID), mileage, and license plate.
3. System shall assign each vehicle to a home branch and optionally track current branch for one-way flows.
4. System shall expose vehicle lifecycle states (available, reserved, rented, maintenance, retired, inspection_pending).
5. System shall record and enforce minimum rental age and license requirements per vehicle class or jurisdiction.
6. System shall support transferring vehicles between branches (with audit trail).
7. System shall block rental of vehicles with open safety recalls or failed inspection until resolved.
8. System shall support optional telematics or odometer readings for mileage validation at return.

#### Reservations (FR9-FR14)
9. Customer shall search availability by pickup/drop-off location, date-time range, and vehicle class or filters.
10. Customer shall create a reservation holding a vehicle class (or specific vehicle if business rules allow).
11. Customer shall modify reservation dates, location, or add-ons subject to availability and updated pricing.
12. Customer shall cancel reservation with policy-based refunds or penalties.
13. System shall enforce maximum advance booking window and minimum rental duration.
14. System shall send confirmation and reminders (email/SMS) with reservation identifiers.

#### Rental Process (FR15-FR19)
15. Staff or kiosk shall convert a reservation to an active rental agreement at pickup after identity and license verification.
16. System shall assign a concrete vehicle at pickup if reservation was class-level, or validate the pre-assigned unit.
17. System shall record pickup checklist (fuel level, damage marks, odometer) and customer acknowledgment.
18. System shall record return checklist, compute fuel delta, late return, and damage charges.
19. System shall close the rental, release the vehicle to available or maintenance queue, and email a receipt.

#### Pricing, Insurance & Extras (FR20-FR23)
20. System shall compute price from base daily/weekly rates, seasonal multipliers, and location surcharges.
21. System shall add line items for insurance products, equipment, young driver fee, and one-way fees.
22. System shall apply corporate codes, coupons, and member tier discounts in deterministic priority order.
23. System shall support tax and fee rules that vary by pickup location jurisdiction.

#### Payments & Loyalty (FR24-FR27)
24. System shall authorize or charge payment instruments according to booking channel and risk rules.
25. System shall support partial captures, additional charges post-return, and refunds with idempotency keys.
26. System shall accrue and redeem loyalty points; enforce expiration and tier rules.
27. System shall produce immutable payment and ledger entries for reconciliation and disputes.

#### Maintenance & Operations (FR28-FR30)
28. System shall schedule maintenance intervals by mileage or time and mark vehicles unavailable during service.
29. System shall log incidents, repair orders, and next-available date after major repairs.
30. System shall provide operational dashboards: utilization by branch, fleet health, and overdue returns.

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many branches, vehicles, and concurrent bookings?"
- Branches: 500+; vehicles: 50k+; peak concurrent searches: 10k+ RPS; holiday spikes 10× baseline.

**Deduced NFRs:**
- ✅ Horizontal scaling of stateless API tier; read replicas for search and reporting.
- ✅ Partition data by region or tenant; cache hot availability slices with TTL.
- ✅ Async jobs for notifications, pricing recomputation, and loyalty posting.
- ✅ Elastic capacity for batch end-of-day settlement and analytics.

---

#### 2. **Consistency Analysis**

**Think:** "What must be accurate?"
- A physical vehicle cannot be on two active rentals simultaneously.
- Reservation-to-rental handoff must be atomic with payment authorization rules.
- Inventory counts and availability indexes must not show phantom cars.

**Deduced NFRs:**
- ✅ **Strong consistency** for assignment and rental state transitions (transactions + row locks or optimistic versioning).
- ✅ **Serializable or strict ordering** for the same vehicle ID when competing pickups occur.
- ✅ **Eventual consistency acceptable** for search facets and analytics with bounded staleness (seconds to minutes).
- ✅ **Idempotent payment APIs** to prevent duplicate charges under retries.

---

#### 3. **Availability Analysis**

**Think:** "Acceptable downtime?"
- Booking flow is revenue-critical; read-only degradation may be acceptable during partial outages.

**Deduced NFRs:**
- ✅ **99.95%** availability for booking and payment paths in core regions.
- ✅ **Graceful degradation:** serve cached availability with "confirm at counter" banner if live inventory fails.
- ✅ **Circuit breakers** on downstream payment and identity providers.
- ✅ **Active-active or regional failover** for customer-facing APIs where feasible.

---

#### 4. **Maintainability Analysis**

**Think:** "How to change pricing rules and onboard new countries?"
- Business changes weekly; compliance and tax rules vary by location.

**Deduced NFRs:**
- ✅ **Pluggable pricing and tax engines** (Strategy / rule tables) without redeploying core rental logic.
- ✅ **Structured audit logs** for who changed rates, vehicle status, or manual overrides.
- ✅ **Feature flags** for new loyalty tiers or insurance bundles.
- ✅ **Clear bounded contexts:** inventory, reservations, rentals, billing, loyalty.

---

#### 5. **Performance Analysis**

**Think:** "Latency expectations?"
- Search results: p95 &lt; 300 ms; reservation commit: p95 &lt; 500 ms; payment round-trip: bounded by PSP SLA.

**Deduced NFRs:**
- ✅ **Indexed queries** on (branch_id, class_id, date_range) and materialized availability for common corridors.
- ✅ **O(log n)** or better for interval overlap checks per small candidate set after index pruning.
- ✅ **Background precomputation** of surge pricing where possible.
- ✅ **Pagination** and cursor-based APIs for large admin lists.

---

#### 6. **Security Analysis**

**Think:** "What can go wrong?"
- PII and payment data exposure; fraudulent bookings; insider abuse at branch.

**Deduced NFRs:**
- ✅ **PCI scope reduction:** tokenize cards; no PAN storage in rental domain DB.
- ✅ **RBAC** for corporate, branch staff, and system admin roles.
- ✅ **Rate limiting** on search and booking to mitigate scraping and inventory denial.
- ✅ **Encryption at rest and in transit**; field-level encryption for driver license numbers where required.
- ✅ **Audit trails** for manual vehicle status changes and refunds.

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Vehicle types and classes" | VehicleType, VehicleClass, Vehicle |
| "Branches and locations" | Location, Branch, Address, TimeZone |
| "Reservation" | Reservation, Customer, DateRange |
| "Rental agreement" | RentalAgreement, Contract, Pickup, Return |
| "Pricing and discounts" | PricingRule, RatePlan, Coupon, TaxRule |
| "Insurance and extras" | InsuranceProduct, AddOn, LineItem |
| "Payment" | Payment, PaymentMethod, LedgerEntry |
| "Loyalty" | LoyaltyProgram, LoyaltyAccount, Tier, PointsTransaction |
| "Maintenance" | MaintenanceRecord, ServiceOrder, InspectionReport |
| "Search" | SearchCriteria, AvailabilitySlot |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| VehicleType | ✅ YES | Distinct SKU for pricing and capacity |
| VehicleClass | ✅ YES | Economy/SUV abstraction for search |
| Vehicle | ✅ YES | Physical unit with state and location |
| Location | ✅ YES | Geographic and regulatory context |
| Branch | ✅ YES | Operational unit with hours and fleet |
| Address | ❌ NO | Value object on Location |
| TimeZone | ❌ NO | Attribute of Location |
| Reservation | ✅ YES | Lifecycle entity with holds |
| Customer | ✅ YES | Actor with profile and eligibility |
| DateRange | ❌ NO | Value object (pickup/return instants) |
| RentalAgreement | ✅ YES | Legal and financial wrapper for active rental |
| Contract | ❌ NO | Synonym; merge into RentalAgreement |
| Pickup | ❌ NO | Event or sub-record on rental |
| Return | ❌ NO | Event or sub-record on rental |
| PricingRule | ✅ YES | Rule or strategy input |
| RatePlan | ✅ YES | Named bundle of base rates |
| Coupon | ✅ YES | Promotion entity |
| TaxRule | ✅ YES | Jurisdiction-specific computation |
| InsuranceProduct | ✅ YES | Sellable coverage |
| AddOn | ✅ YES | GPS, seat, etc. |
| LineItem | ✅ YES | Billable row on quote or invoice |
| Payment | ✅ YES | Charge or refund attempt |
| PaymentMethod | ✅ YES | Token reference + metadata |
| LedgerEntry | ✅ YES | Immutable accounting record |
| LoyaltyProgram | ✅ YES | Rule container |
| LoyaltyAccount | ✅ YES | Balance per customer in program |
| Tier | ✅ YES | Enum or entity with thresholds |
| PointsTransaction | ✅ YES | Earn/burn/adjust audit |
| MaintenanceRecord | ✅ YES | Service history |
| ServiceOrder | ✅ YES | Work order blocking availability |
| InspectionReport | ✅ YES | Pre/post rental checks |
| SearchCriteria | ❌ NO | DTO for query |
| AvailabilitySlot | ❌ NO | Computed view or cache row |

### Final Entity List

**Fleet & Location:**
1. **VehicleClass** - Search-facing category (economy, compact, SUV).
2. **VehicleType** - Template linking class to typical make/model and attributes.
3. **Vehicle** - Individual unit (VIN, plate, odometer, home branch, status).
4. **Branch** - Physical depot with hours, capacity, and address.
5. **Location** - City/airport metadata used in search and tax (may merge with Branch in smaller designs).

**Customer & Booking:**
6. **Customer** - Profile, license, age, corporate affiliation.
7. **Reservation** - Intent to rent with class, window, options, and status.

**Rental & Inspection:**
8. **RentalAgreement** - Active or completed contract binding customer and vehicle.
9. **InspectionReport** - Pickup/return damage and fuel snapshot.
10. **VehicleTransfer** - Inter-branch movement audit (optional first-class entity).

**Commercial:**
11. **RatePlan** - Base rates by class and season.
12. **PricingRule** - Multipliers, minimum days, one-way fees.
13. **InsuranceProduct** - Coverage SKU.
14. **AddOn** - Extras catalog.
15. **Quote** - Computed snapshot before commit (optional; often embedded in reservation).
16. **LineItem** - Monetary line on quote or invoice.

**Payments & Loyalty:**
17. **Payment** - Authorization/capture/refund records.
18. **PaymentMethod** - Tokenized instrument.
19. **LedgerEntry** - Immutable financial posting.
20. **LoyaltyProgram** - Program definition.
21. **LoyaltyAccount** - Points balance and tier.
22. **PointsTransaction** - Earn/redeem/adjust.

**Operations:**
23. **MaintenanceRecord** - Scheduled or ad-hoc service.
24. **ServiceOrder** - Open work blocking vehicle.

**System / Cross-Cutting:**
25. **RentalService** (facade), **AvailabilityEngine**, **PricingEngine** — application services (shown in diagrams as orchestrators).

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Fleet & Location

#### Branch ↔ Vehicle
**Conclusion:** **Aggregation** (branch owns fleet; vehicles can be transferred)
```
Branch ◇────→ Vehicle [1..*]   (home branch)
Vehicle ─────→ Branch [0..1]   (current branch if different)
```

#### VehicleType ↔ VehicleClass
**Conclusion:** **Association** (type specializes class)
```
VehicleType ─────→ VehicleClass [1]
```

#### Vehicle ↔ VehicleType
**Conclusion:** **Association**
```
Vehicle ─────→ VehicleType [1]
```

---

### Pass 2: Reservation & Rental

#### Customer ↔ Reservation
**Conclusion:** **Composition** (reservation belongs to customer)
```
Customer ◆────→ Reservation [0..*]
```

#### Reservation ↔ VehicleClass
**Conclusion:** **Association** (book by class)
```
Reservation ─────→ VehicleClass [1]
```

#### Reservation ↔ RentalAgreement
**Conclusion:** **One-to-one** optional (0..1 until pickup)
```
Reservation ─────→ RentalAgreement [0..1]
```

#### RentalAgreement ↔ Vehicle
**Conclusion:** **Association** (concrete assignment)
```
RentalAgreement ─────→ Vehicle [1]
```

#### RentalAgreement ↔ InspectionReport
**Conclusion:** **Composition** (pickup and return reports)
```
RentalAgreement ◆────→ InspectionReport [1..2]  (pickup + return)
```

---

### Pass 3: Commercial, Payment, Loyalty

#### RentalAgreement ↔ LineItem
**Conclusion:** **Composition**
```
RentalAgreement ◆────→ LineItem [1..*]
```

#### Payment ↔ RentalAgreement
**Conclusion:** **Association**
```
Payment ─────→ RentalAgreement [0..1]  (also link to Reservation pre-auth)
```

#### Customer ↔ LoyaltyAccount
**Conclusion:** **Composition**
```
Customer ◆────→ LoyaltyAccount [0..*]  (per program)
```

#### Vehicle ↔ MaintenanceRecord / ServiceOrder
**Conclusion:** **Association**
```
Vehicle ─────→ MaintenanceRecord [0..*]
Vehicle ─────→ ServiceOrder [0..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Branch → Vehicle (home) | 1:N | Aggregation |
| VehicleClass → VehicleType | 1:N | Association |
| VehicleType → Vehicle | 1:N | Association |
| Customer → Reservation | 1:N | Composition |
| Reservation → VehicleClass | N:1 | Association |
| Reservation → RentalAgreement | 1:0..1 | Association |
| RentalAgreement → Vehicle | N:1 | Association |
| RentalAgreement → LineItem | 1:N | Composition |
| RentalAgreement → Payment | 1:N | Association |
| Customer → LoyaltyAccount | 1:N | Composition |
| Vehicle → MaintenanceRecord | 1:N | Association |
| Vehicle → ServiceOrder | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums & Value Objects

```
┌────────────────────┐  ┌─────────────────────┐  ┌──────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>     │  │ <<enumeration>>      │
│  VehicleStatus     │  │ ReservationStatus   │  │  RentalStatus        │
├────────────────────┤  ├─────────────────────┤  ├──────────────────────┤
│ AVAILABLE          │  │ PENDING_PAYMENT     │  │ DRAFT                │
│ RESERVED           │  │ CONFIRMED           │  │ ACTIVE               │
│ RENTED             │  │ MODIFIED            │  │ RETURNED             │
│ MAINTENANCE        │  │ CANCELLED           │  │ CLOSED               │
│ INSPECTION_PENDING │  │ EXPIRED             │  │ DISPUTED             │
│ RETIRED            │  └─────────────────────┘  └──────────────────────┘
└────────────────────┘

┌────────────────────┐  ┌─────────────────────┐  ┌──────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>     │  │   RentalPeriod       │
│  PaymentStatus     │  │  InspectionKind     │  │   (value object)     │
├────────────────────┤  ├─────────────────────┤  ├──────────────────────┤
│ AUTHORIZED         │  │ PICKUP              │  │ - pickupAt: Instant  │
│ CAPTURED           │  │ RETURN              │  │ - returnAt: Instant  │
│ FAILED             │  └─────────────────────┘  │ - pickupBranchId     │
│ REFUNDED           │                           │ - returnBranchId     │
│ VOIDED             │                           └──────────────────────┘
└────────────────────┘
```

---

### Class Diagram 2: Fleet — Branch, Class, Type, Vehicle

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            Branch                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ - branchId: String                                                      │
│ - name: String                                                          │
│ - timezone: ZoneId                                                      │
│ - addressLine: String                                                   │
│ - isAirportLocation: boolean                                            │
├─────────────────────────────────────────────────────────────────────────┤
│ + getBranchId(): String                                                 │
│ + getOperatingHours(day): Optional<TimeWindow>                          │
└─────────────────────────────────────────────────────────────────────────┘
        △
        │ (optional generalization)
        │
┌─────────────────────────────────────────────────────────────────────────┐
│                          VehicleClass                                   │
├─────────────────────────────────────────────────────────────────────────┤
│ - classId: String    // e.g. ECAR, SFAR                                 │
│ - displayName: String                                                   │
│ - typicalSeats: int                                                     │
│ - baggageUnits: int                                                     │
├─────────────────────────────────────────────────────────────────────────┤
│ + meetsFilter(f: SearchFilter): boolean                                 │
└─────────────────────────────────────────────────────────────────────────┘
        △
        │ 1
        │
        │ *
┌─────────────────────────────────────────────────────────────────────────┐
│                          VehicleType                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ - typeId: String                                                        │
│ - makeModel: String                                                     │
│ - transmission: Transmission { AUTO, MANUAL }                         │
│ - fuelType: FuelType                                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ + getVehicleClass(): VehicleClass                                       │
└─────────────────────────────────────────────────────────────────────────┘
        △
        │ 1
        │
        │ *
┌─────────────────────────────────────────────────────────────────────────┐
│                           Vehicle                                       │
├─────────────────────────────────────────────────────────────────────────┤
│ - vehicleId: String                                                     │
│ - vin: String                                                           │
│ - licensePlate: String                                                  │
│ - odometerMiles: long                                                   │
│ - status: VehicleStatus                                                 │
│ - version: long            // optimistic lock for concurrent assignment │
│ - homeBranch: Branch                                                    │
│ - currentBranch: Branch                                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ + markRented(): void                                                    │
│ + markAvailable(): void                                                 │
│ + markMaintenance(reason: String): void                                 │
│ + canBeRentedAt(when: Instant): boolean                                 │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Reservation & Rental Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Reservation                                     │
├─────────────────────────────────────────────────────────────────────────┤
│ - reservationId: String                                                 │
│ - customerId: String                                                    │
│ - vehicleClassId: String                                                │
│ - period: RentalPeriod                                                  │
│ - status: ReservationStatus                                             │
│ - selectedAddOnIds: List<String>                                        │
│ - insuranceProductId: Optional<String>                                  │
│ - quotedTotalCents: long                                                │
│ - idempotencyKey: String                                                │
├─────────────────────────────────────────────────────────────────────────┤
│ + confirmAfterPayment(): void                                           │
│ + modifyPeriod(newPeriod, engine): PricingDelta                         │
│ + cancel(policy: CancellationPolicy): RefundQuote                       │
└─────────────────────────────────────────────────────────────────────────┘
           │
           │ 0..1 creates
           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      RentalAgreement                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ - rentalId: String                                                      │
│ - reservationId: Optional<String>                                       │
│ - customerId: String                                                    │
│ - vehicleId: String                                                     │
│ - status: RentalStatus                                                  │
│ - period: RentalPeriod                                                  │
│ - lineItems: List<LineItem>                         ◆────────────────┐    │
│ - pickupInspection: InspectionReport                 ◆──────────────┼──┐ │
│ - returnInspection: Optional<InspectionReport>       ◆──────────────┼──┼┐│
├─────────────────────────────────────────────────────────────────────────┤│
│ + startFromReservation(res, vehicle): void                            ││
│ + completeReturn(report, pricing): void                               ││
└─────────────────────────────────────────────────────────────────────────┘│
                                                                             │
┌─────────────────────────────────────────────────────────────────────────┐│
│                      InspectionReport                                   ││
├─────────────────────────────────────────────────────────────────────────┤│
│ - reportId: String                                                      ││
│ - kind: InspectionKind                                                  ││
│ - fuelLevelPercent: int                                                 ││
│ - odometerMiles: long                                                   ││
│ - damageNotes: List<DamageMark>                                         ││
│ - signedByCustomer: boolean                                             ││
├─────────────────────────────────────────────────────────────────────────┤│
│ + diffFuel(other: InspectionReport): FuelCharge                         ││
└─────────────────────────────────────────────────────────────────────────┘│
                                                                             │
┌─────────────────────────────────────────────────────────────────────────┐│
│                         LineItem                                        ││
├─────────────────────────────────────────────────────────────────────────┤│
│ - code: String     // BASE, INSURANCE, GPS, ONE_WAY, TAX...             ││
│ - description: String                                                   ││
│ - quantity: int                                                         ││
│ - unitAmountCents: long                                                 ││
│ - totalCents: long                                                      ││
└─────────────────────────────────────────────────────────────────────────┘┘
```

---

### Class Diagram 4: Pricing Engine (Strategy)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    <<interface>>                                        │
│                    PricingStrategy                                      │
├─────────────────────────────────────────────────────────────────────────┤
│ + quote(ctx: PricingContext): PricedQuote                               │
└─────────────────────────────────────────────────────────────────────────┘
                    △
                    │
     ┌──────────────┼──────────────┬──────────────────┐
     │              │              │                  │
     ▼              ▼              ▼                  ▼
┌───────────┐ ┌────────────┐ ┌─────────────┐ ┌──────────────┐
│Standard   │ │Corporate   │ │Promotional  │ │SurgePricing  │
│DailyRate  │ │Negotiated  │ │Bundle       │ │Strategy      │
│Strategy   │ │RateStrategy│ │Strategy     │ │              │
└───────────┘ └────────────┘ └─────────────┘ └──────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                       PricingEngine                                     │
├─────────────────────────────────────────────────────────────────────────┤
│ - strategies: List<PricingStrategy>   // chain or composite             │
│ - taxCalculator: TaxCalculator                                          │
├─────────────────────────────────────────────────────────────────────────┤
│ + quote(ctx: PricingContext): PricedQuote                               │
│   // applies strategies in order, then tax                              │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Payment & Loyalty

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Payment                                        │
├─────────────────────────────────────────────────────────────────────────┤
│ - paymentId: String                                                     │
│ - rentalId: Optional<String>                                            │
│ - reservationId: Optional<String>                                       │
│ - amountCents: long                                                     │
│ - currency: Currency                                                    │
│ - status: PaymentStatus                                                 │
│ - idempotencyKey: String                                                │
│ - providerRef: String     // PSP charge id                              │
├─────────────────────────────────────────────────────────────────────────┤
│ + authorize(): void                                                   │
│ + capture(): void                                                       │
│ + refund(partialCents: long): void                                      │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                       LoyaltyProgram                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ - programId: String                                                     │
│ - earnRatePointsPerDollar: BigDecimal                                   │
│ - tiers: List<TierRule>                                                 │
└─────────────────────────────────────────────────────────────────────────┘
           │
           │ 1
           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       LoyaltyAccount                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ - accountId: String                                                     │
│ - customerId: String                                                    │
│ - programId: String                                                     │
│ - pointsBalance: int                                                    │
│ - tier: String                                                          │
│ - transactions: List<PointsTransaction>         ◆───────────────────┐   │
├─────────────────────────────────────────────────────────────────────────┤│
│ + postEarn(rentalId, amountCents): void                               ││
│ + redeem(points, rentalId): void                                        ││
└─────────────────────────────────────────────────────────────────────────┘│
                                                                             │
┌─────────────────────────────────────────────────────────────────────────┐│
│                    PointsTransaction                                    ││
├─────────────────────────────────────────────────────────────────────────┤│
│ - txnId: String                                                         ││
│ - type: { EARN, REDEEM, ADJUST, EXPIRE }                                ││
│ - pointsDelta: int                                                      ││
│ - referenceId: String                                                   ││
└─────────────────────────────────────────────────────────────────────────┘┘
```

---

### Class Diagram 6: Maintenance & Service Blocking

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    MaintenanceRecord                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ - recordId: String                                                      │
│ - vehicleId: String                                                     │
│ - serviceType: String   // OIL, TIRE, RECALL, INSPECTION                │
│ - performedAt: Instant                                                  │
│ - nextDueOdometer: Optional<long>                                       │
│ - nextDueDate: Optional<LocalDate>                                      │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                       ServiceOrder                                      │
├─────────────────────────────────────────────────────────────────────────┤
│ - orderId: String                                                       │
│ - vehicleId: String                                                     │
│ - openedAt: Instant                                                     │
│ - expectedCompleteAt: Optional<Instant>                               │
│ - blocksRental: boolean                                                 │
│ - status: { OPEN, IN_PROGRESS, COMPLETED, CANCELLED }                     │
├─────────────────────────────────────────────────────────────────────────┤
│ + onComplete(): void   // returns vehicle to AVAILABLE if inspection OK │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              │ associated with
                              ▼
                        ┌──────────┐
                        │ Vehicle  │
                        └──────────┘
```

---

### Class Diagram 7: Application Services (Facade)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      CarRentalFacade                                    │
│                    (or RentalApplicationService)                        │
├─────────────────────────────────────────────────────────────────────────┤
│ - availabilityService: AvailabilityService                              │
│ - reservationRepository: ReservationRepository                          │
│ - rentalRepository: RentalRepository                                    │
│ - vehicleRepository: VehicleRepository                                  │
│ - pricingEngine: PricingEngine                                          │
│ - paymentGateway: PaymentGateway                                        │
│ - loyaltyService: LoyaltyService                                        │
├─────────────────────────────────────────────────────────────────────────┤
│ + search(req: SearchRequest): List<AvailabilityOffer>                   │
│ + bookReservation(cmd: BookReservationCommand): Reservation             │
│ + modifyReservation(cmd: ModifyReservationCommand): Reservation         │
│ + cancelReservation(id, reason): CancellationResult                     │
│ + pickupVehicle(cmd: PickupCommand): RentalAgreement                    │
│ + returnVehicle(cmd: ReturnCommand): RentalAgreement                    │
└─────────────────────────────────────────────────────────────────────────┘
         │ uses                    │ uses                 │ uses
         ▼                         ▼                      ▼
┌──────────────────┐   ┌────────────────────┐   ┌─────────────────────┐
│ Availability     │   │ PricingEngine      │   │ PaymentGateway      │
│ Service          │   │                    │   │                     │
└──────────────────┘   └────────────────────┘   └─────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums

```java
// VehicleStatus.java — operational state of a physical unit
public enum VehicleStatus {
    /** On lot and assignable */
    AVAILABLE,
    /** Linked to a confirmed reservation window */
    RESERVED,
    /** On active rental agreement */
    RENTED,
    /** In shop or awaiting parts */
    MAINTENANCE,
    /** Post-incident or post-rental check not cleared */
    INSPECTION_PENDING,
    /** Permanently removed from fleet */
    RETIRED
}
```

```java
// ReservationStatus.java
public enum ReservationStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    MODIFIED,
    CANCELLED,
    EXPIRED
}
```

```java
// RentalStatus.java
public enum RentalStatus {
    DRAFT,
    ACTIVE,
    RETURNED,
    CLOSED,
    DISPUTED
}
```

```java
// PaymentStatus.java
public enum PaymentStatus {
    AUTHORIZED,
    CAPTURED,
    FAILED,
    REFUNDED,
    VOIDED
}
```

---

### Value object: rental period and search

```java
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable pickup/return window. All comparisons use Instant in UTC;
 * Branch applies timezone for "local business day" rules at service layer.
 */
public final class RentalPeriod {
    private final Instant pickupAt;
    private final Instant returnAt;
    private final String pickupBranchId;
    private final String returnBranchId;

    public RentalPeriod(Instant pickupAt, Instant returnAt,
                        String pickupBranchId, String returnBranchId) {
        if (!returnAt.isAfter(pickupAt)) {
            throw new IllegalArgumentException("returnAt must be after pickupAt");
        }
        this.pickupAt = pickupAt;
        this.returnAt = returnAt;
        this.pickupBranchId = Objects.requireNonNull(pickupBranchId);
        this.returnBranchId = Objects.requireNonNull(returnBranchId);
    }

    /** True if [pickupAt, returnAt) overlaps the other period (half-open). */
    public boolean overlaps(RentalPeriod other) {
        return this.pickupAt.isBefore(other.returnAt)
                && other.pickupAt.isBefore(this.returnAt);
    }

    public long approximateRentalDays() {
        long seconds = returnAt.getEpochSecond() - pickupAt.getEpochSecond();
        // Ceiling to whole days for pricing approximation in demo
        return (seconds + 86400 - 1) / 86400;
    }

    public Instant getPickupAt() { return pickupAt; }
    public Instant getReturnAt() { return returnAt; }
    public String getPickupBranchId() { return pickupBranchId; }
    public String getReturnBranchId() { return returnBranchId; }
}
```

```java
import java.time.Instant;
import java.util.Optional;

/**
 * Customer-facing search criteria. Service maps to DB predicates + cache keys.
 */
public class SearchRequest {
    private final String pickupBranchId;
    private final Optional<String> dropoffBranchId;
    private final Instant pickupAt;
    private final Instant returnAt;
    private final Optional<String> vehicleClassId; // empty = any
    private final boolean automaticOnly;

    public SearchRequest(String pickupBranchId, Optional<String> dropoffBranchId,
                         Instant pickupAt, Instant returnAt,
                         Optional<String> vehicleClassId, boolean automaticOnly) {
        this.pickupBranchId = pickupBranchId;
        this.dropoffBranchId = dropoffBranchId;
        this.pickupAt = pickupAt;
        this.returnAt = returnAt;
        this.vehicleClassId = vehicleClassId;
        this.automaticOnly = automaticOnly;
    }

    public RentalPeriod toPeriod() {
        String drop = dropoffBranchId.orElse(pickupBranchId);
        return new RentalPeriod(pickupAt, returnAt, pickupBranchId, drop);
    }

    // getters omitted for brevity in interview — expose in real code
    public String getPickupBranchId() { return pickupBranchId; }
    public Instant getPickupAt() { return pickupAt; }
    public Instant getReturnAt() { return returnAt; }
    public Optional<String> getVehicleClassId() { return vehicleClassId; }
    public boolean isAutomaticOnly() { return automaticOnly; }
}
```

---

### Vehicle with optimistic locking field (concurrency hook)

```java
import java.time.Instant;
import java.util.Objects;

public class Vehicle {
    private final String vehicleId;
    private final String vin;
    private final String vehicleTypeId;
    private final String homeBranchId;
    private String currentBranchId;
    private VehicleStatus status;
    private long odometerMiles;
    /**
     * Incremented on each state-changing update; persistence layer uses
     * UPDATE ... WHERE vehicle_id = ? AND version = ? to detect conflicts.
     */
    private long version;

    public Vehicle(String vehicleId, String vin, String vehicleTypeId,
                   String homeBranchId, long odometerMiles) {
        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.vin = vin;
        this.vehicleTypeId = vehicleTypeId;
        this.homeBranchId = homeBranchId;
        this.currentBranchId = homeBranchId;
        this.odometerMiles = odometerMiles;
        this.status = VehicleStatus.AVAILABLE;
        this.version = 0L;
    }

    /** Business rule: only AVAILABLE at pickup branch and not in open service. */
    public boolean canBeAssignedAt(Instant when, String branchId,
                                   boolean hasOpenServiceOrder) {
        if (hasOpenServiceOrder) return false;
        if (!Objects.equals(currentBranchId, branchId)) return false;
        return status == VehicleStatus.AVAILABLE || status == VehicleStatus.RESERVED;
    }

    public void markRented() {
        if (status != VehicleStatus.AVAILABLE && status != VehicleStatus.RESERVED) {
            throw new IllegalStateException("Cannot rent vehicle in status: " + status);
        }
        this.status = VehicleStatus.RENTED;
        bumpVersion();
    }

    public void markAvailableAfterReturn(long newOdometer) {
        if (newOdometer < this.odometerMiles) {
            throw new IllegalArgumentException("Odometer rollback not allowed");
        }
        this.odometerMiles = newOdometer;
        this.status = VehicleStatus.INSPECTION_PENDING;
        bumpVersion();
    }

    public void clearInspectionReady() {
        if (status != VehicleStatus.INSPECTION_PENDING) {
            throw new IllegalStateException("Vehicle not awaiting inspection");
        }
        this.status = VehicleStatus.AVAILABLE;
        bumpVersion();
    }

    public void markMaintenance() {
        this.status = VehicleStatus.MAINTENANCE;
        bumpVersion();
    }

    private void bumpVersion() {
        this.version++;
    }

    public String getVehicleId() { return vehicleId; }
    public String getVehicleTypeId() { return vehicleTypeId; }
    public String getCurrentBranchId() { return currentBranchId; }
    public VehicleStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public long getOdometerMiles() { return odometerMiles; }
}
```

---

### Reservation

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Reservation {
    private final String reservationId;
    private final String customerId;
    private final String vehicleClassId;
    private RentalPeriod period;
    private ReservationStatus status;
    private final List<String> addOnIds;
    private String insuranceProductId;
    private long quotedTotalCents;
    private final String idempotencyKey;

    public Reservation(String customerId, String vehicleClassId,
                       RentalPeriod period, String idempotencyKey) {
        this.reservationId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.vehicleClassId = vehicleClassId;
        this.period = period;
        this.status = ReservationStatus.PENDING_PAYMENT;
        this.addOnIds = new ArrayList<>();
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
    }

    public void confirmAfterPayment() {
        if (status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Invalid transition to CONFIRMED");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void applyModification(RentalPeriod newPeriod, long newQuoteCents) {
        if (status != ReservationStatus.CONFIRMED && status != ReservationStatus.MODIFIED) {
            throw new IllegalStateException("Cannot modify reservation in status: " + status);
        }
        this.period = newPeriod;
        this.quotedTotalCents = newQuoteCents;
        this.status = ReservationStatus.MODIFIED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void addAddOn(String addOnId) {
        this.addOnIds.add(addOnId);
    }

    public void setInsuranceProductId(String insuranceProductId) {
        this.insuranceProductId = insuranceProductId;
    }

    public void setQuotedTotalCents(long quotedTotalCents) {
        this.quotedTotalCents = quotedTotalCents;
    }

    public String getReservationId() { return reservationId; }
    public String getCustomerId() { return customerId; }
    public String getVehicleClassId() { return vehicleClassId; }
    public RentalPeriod getPeriod() { return period; }
    public ReservationStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public long getQuotedTotalCents() { return quotedTotalCents; }
}
```

---

### Pricing strategy interface and engine

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Input DTO for pricing — rates resolved from DB in real system. */
public class PricingContext {
    private final RentalPeriod period;
    private final String vehicleClassId;
    private final List<String> addOnIds;
    private final String couponCode; // nullable

    public PricingContext(RentalPeriod period, String vehicleClassId,
                          List<String> addOnIds, String couponCode) {
        this.period = period;
        this.vehicleClassId = vehicleClassId;
        this.addOnIds = addOnIds;
        this.couponCode = couponCode;
    }

    public RentalPeriod getPeriod() { return period; }
    public String getVehicleClassId() { return vehicleClassId; }
    public List<String> getAddOnIds() { return addOnIds; }
    public String getCouponCode() { return couponCode; }
}

/** Single line on a quote — maps to LineItem / invoice row. */
public class PricedLine {
    private final String code;
    private final String description;
    private final int quantity;
    private final long unitAmountCents;
    private final long totalCents;

    public PricedLine(String code, String description, int quantity,
                      long unitAmountCents, long totalCents) {
        this.code = code;
        this.description = description;
        this.quantity = quantity;
        this.unitAmountCents = unitAmountCents;
        this.totalCents = totalCents;
    }

    public String getCode() { return code; }
    public long getTotalCents() { return totalCents; }
}

public class PricedQuote {
    private final List<PricedLine> lines;
    private final long subtotalCents;
    private final long taxCents;
    private final long totalCents;

    public PricedQuote(List<PricedLine> lines, long subtotalCents, long taxCents, long totalCents) {
        this.lines = lines;
        this.subtotalCents = subtotalCents;
        this.taxCents = taxCents;
        this.totalCents = totalCents;
    }

    public List<PricedLine> getLines() { return Collections.unmodifiableList(lines); }
    public long getTotalCents() { return totalCents; }
}

public interface PricingStrategy {
    void apply(PricingContext ctx, List<PricedLine> accumulator);
}

/** Applies base daily rate from a rate table (here hard-coded for interview). */
public class StandardDailyRateStrategy implements PricingStrategy {
    private final long dailyRateCentsByClass;

    public StandardDailyRateStrategy(long dailyRateCentsByClass) {
        this.dailyRateCentsByClass = dailyRateCentsByClass;
    }

    @Override
    public void apply(PricingContext ctx, List<PricedLine> accumulator) {
        long days = ctx.getPeriod().approximateRentalDays();
        long total = days * dailyRateCentsByClass;
        accumulator.add(new PricedLine(
                "BASE",
                "Base rate (" + days + " days)",
                (int) days,
                dailyRateCentsByClass,
                total
        ));
    }
}

public class OneWayFeeStrategy implements PricingStrategy {
    private final long oneWayFeeCents;

    public OneWayFeeStrategy(long oneWayFeeCents) {
        this.oneWayFeeCents = oneWayFeeCents;
    }

    @Override
    public void apply(PricingContext ctx, List<PricedLine> accumulator) {
        if (!ctx.getPeriod().getPickupBranchId().equals(ctx.getPeriod().getReturnBranchId())) {
            accumulator.add(new PricedLine(
                    "ONE_WAY",
                    "One-way drop-off fee",
                    1,
                    oneWayFeeCents,
                    oneWayFeeCents
            ));
        }
    }
}

public class PricingEngine {
    private final List<PricingStrategy> strategies;
    private final double taxRate; // simplified; production uses TaxCalculator per jurisdiction

    public PricingEngine(List<PricingStrategy> strategies, double taxRate) {
        this.strategies = strategies;
        this.taxRate = taxRate;
    }

    public PricedQuote quote(PricingContext ctx) {
        List<PricedLine> lines = new ArrayList<>();
        for (PricingStrategy s : strategies) {
            s.apply(ctx, lines);
        }
        long subtotal = lines.stream().mapToLong(PricedLine::getTotalCents).sum();
        long tax = Math.round(subtotal * taxRate);
        lines.add(new PricedLine("TAX", "Estimated tax", 1, tax, tax));
        return new PricedQuote(lines, subtotal, tax, subtotal + tax);
    }
}
```

---

### Rental service: pickup with transactional assignment (core flow)

```java
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates pickup: load reservation, pick a vehicle, transition states.
 * In production this method runs inside a DB transaction and calls repositories.
 */
public class RentalApplicationService {
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;

    public RentalApplicationService(VehicleRepository vehicleRepository,
                                    ReservationRepository reservationRepository,
                                    RentalRepository rentalRepository) {
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.rentalRepository = rentalRepository;
    }

    /**
     * Picks first eligible vehicle of correct class at branch with row lock in DB.
     * Throws if no vehicle or reservation not CONFIRMED.
     */
    public RentalAgreement pickup(PickupCommand cmd) {
        Reservation res = reservationRepository.findById(cmd.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (res.getStatus() != ReservationStatus.CONFIRMED
                && res.getStatus() != ReservationStatus.MODIFIED) {
            throw new IllegalStateException("Reservation not confirmable for pickup");
        }

        Optional<Vehicle> candidate = vehicleRepository
                .findAssignableVehicle(res.getVehicleClassId(),
                        res.getPeriod().getPickupBranchId(),
                        res.getPeriod(),
                        cmd.getStaffBranchId());

        Vehicle vehicle = candidate.orElseThrow(() ->
                new IllegalStateException("No vehicle available for class/period"));

        // Optimistic lock: repository updates with WHERE version = expected
        vehicleRepository.lockAndMarkRented(vehicle.getVehicleId(), vehicle.getVersion());

        RentalAgreement agreement = new RentalAgreement(
                UUID.randomUUID().toString(),
                res.getReservationId(),
                res.getCustomerId(),
                vehicle.getVehicleId(),
                res.getPeriod()
        );
        agreement.setStatus(RentalStatus.ACTIVE);

        rentalRepository.save(agreement);
        return agreement;
    }
}

/** Command DTO from staff UI */
class PickupCommand {
    private final String reservationId;
    private final String staffBranchId;

    PickupCommand(String reservationId, String staffBranchId) {
        this.reservationId = reservationId;
        this.staffBranchId = staffBranchId;
    }

    String getReservationId() { return reservationId; }
    String getStaffBranchId() { return staffBranchId; }
}
```

```java
import java.util.List;

public class RentalAgreement {
    private final String rentalId;
    private final String reservationId;
    private final String customerId;
    private final String vehicleId;
    private final RentalPeriod period;
    private RentalStatus status;
    private final List<PricedLine> lineItems; // use LineItem entity in full model

    public RentalAgreement(String rentalId, String reservationId, String customerId,
                           String vehicleId, RentalPeriod period) {
        this.rentalId = rentalId;
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.period = period;
        this.status = RentalStatus.DRAFT;
        this.lineItems = new java.util.ArrayList<>();
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    public String getRentalId() { return rentalId; }
    public String getVehicleId() { return vehicleId; }
    public RentalStatus getStatus() { return status; }
}
```

---

### Repository interfaces (dependency inversion)

```java
import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    Optional<Vehicle> findAssignableVehicle(String vehicleClassId, String branchId,
                                            RentalPeriod period, String actingBranchId);

    void lockAndMarkRented(String vehicleId, long expectedVersion);
}

public interface ReservationRepository {
    Optional<Reservation> findById(String id);
    void save(Reservation reservation);
}

public interface RentalRepository {
    void save(RentalAgreement agreement);
}
```

---

### Payment idempotency sketch

```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gateway wrapper ensuring duplicate idempotency keys return same result.
 * Production: store keys in DB with unique constraint.
 */
public class IdempotentPaymentGateway {
    private final PaymentGateway delegate;
    private final Map<String, PaymentResult> cache = new ConcurrentHashMap<>();

    public IdempotentPaymentGateway(PaymentGateway delegate) {
        this.delegate = delegate;
    }

    public PaymentResult authorize(String idempotencyKey, long amountCents) {
        return cache.computeIfAbsent(idempotencyKey,
                k -> delegate.authorize(k, amountCents));
    }

    public PaymentResult capture(String idempotencyKey, String authorizationId) {
        String compound = idempotencyKey + "|CAPTURE|" + authorizationId;
        return cache.computeIfAbsent(compound, k -> delegate.capture(authorizationId));
    }
}

interface PaymentGateway {
    PaymentResult authorize(String idempotencyKey, long amountCents);
    PaymentResult capture(String authorizationId);
}

class PaymentResult {
    private final String providerRef;
    private final PaymentStatus status;

    PaymentResult(String providerRef, PaymentStatus status) {
        this.providerRef = providerRef;
        this.status = status;
    }

    public String getProviderRef() { return providerRef; }
    public PaymentStatus getStatus() { return status; }
}
```

---

### Loyalty earn (post-close)

```java
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoyaltyService {
    private final BigDecimal pointsPerDollar;

    public LoyaltyService(BigDecimal pointsPerDollar) {
        this.pointsPerDollar = pointsPerDollar;
    }

    /**
     * Called after rental CLOSED and payment captured — async in production.
     */
    public PointsTransaction earnForRental(String customerId, String rentalId, long capturedAmountCents) {
        BigDecimal dollars = BigDecimal.valueOf(capturedAmountCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        int points = dollars.multiply(pointsPerDollar).intValue();
        return new PointsTransaction(customerId, rentalId, points, PointsTxnType.EARN);
    }
}

enum PointsTxnType { EARN, REDEEM, ADJUST, EXPIRE }

class PointsTransaction {
    private final String customerId;
    private final String referenceId;
    private final int pointsDelta;
    private final PointsTxnType type;

    PointsTransaction(String customerId, String referenceId, int pointsDelta, PointsTxnType type) {
        this.customerId = customerId;
        this.referenceId = referenceId;
        this.pointsDelta = pointsDelta;
        this.type = type;
    }
}
```

---

### Demo entry point

```java
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

public class CarRentalDemo {
    public static void main(String[] args) {
        Instant now = Instant.now();
        RentalPeriod period = new RentalPeriod(
                now.plus(1, ChronoUnit.DAYS),
                now.plus(4, ChronoUnit.DAYS),
                "BR-SFO-01",
                "BR-LAX-01" // one-way
        );

        PricingEngine engine = new PricingEngine(
                Arrays.asList(
                        new StandardDailyRateStrategy(4999), // $49.99 / day demo
                        new OneWayFeeStrategy(15000)
                ),
                0.10
        );

        PricingContext ctx = new PricingContext(
                period,
                "SFAR",
                Arrays.asList("GPS"),
                null
        );

        PricedQuote quote = engine.quote(ctx);
        System.out.println("Quote total (cents): " + quote.getTotalCents());
        for (PricedLine line : quote.getLines()) {
            System.out.println(line.getCode() + " -> " + line.getTotalCents());
        }

        Reservation res = new Reservation("cust-1", "SFAR", period, "idem-book-1");
        res.setQuotedTotalCents(quote.getTotalCents());
        res.confirmAfterPayment();
        System.out.println("Reservation " + res.getReservationId() + " status=" + res.getStatus());
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `PricingStrategy` implementations (standard daily, one-way fee, corporate, surge)  
**Why:** Pricing rules change by market, channel, and season without modifying the orchestrating engine.  
**Interview Justification:** "We compose strategies in order or use a chain; A/B tests can swap implementations per customer segment."

---

### Pattern 2: State Pattern (or explicit state machine)
**Where:** `VehicleStatus`, `ReservationStatus`, `RentalStatus` with guarded transitions  
**Why:** Illegal transitions (e.g., rent a MAINTENANCE car) are rejected in domain methods.  
**Interview Justification:** "Pickup and return are transitions with invariants; state objects could encapsulate allowed operations per state if complexity grows."

---

### Pattern 3: Facade / Application Service
**Where:** `CarRentalFacade` / `RentalApplicationService`  
**Why:** Single entry for use cases: search, book, modify, pickup, return—hides repositories and gateways.  
**Interview Justification:** "API controllers call one service per use case; keeps domain rules out of HTTP layer."

---

### Pattern 4: Repository Pattern
**Where:** `VehicleRepository`, `ReservationRepository`, `RentalRepository`  
**Why:** Domain stays persistence-agnostic; integration tests use fakes.  
**Interview Justification:** "Swap PostgreSQL for Oracle or add read replicas without touching pickup logic."

---

### Pattern 5: Template Method (implicit in workflows)
**Where:** Pickup and return checklists share structure: load entity → validate state → record inspection → persist → emit events.  
**Why:** Consistent extension points for branch-specific verification (international vs domestic).  
**Interview Justification:** "Base class or pipeline steps define skeleton; subclasses plug document checks."

---

### Pattern 6: Decorator / Wrapper
**Where:** `IdempotentPaymentGateway` around `PaymentGateway`  
**Why:** Retries from mobile clients must not double-charge; decorator adds cross-cutting behavior.  
**Interview Justification:** "Idempotency, metrics, and circuit breaking stack as decorators on the PSP client."

---

## 💡 Step 8: Interview Discussion Points

### 1. Class-level reservation vs specific VIN assignment

**Interviewer:** "Do customers reserve a car model or a specific car?"

**Answer:**
"**Typical model:** reservation holds a **vehicle class** and time window; **VIN assignment** happens at pickup (or shortly before) from available inventory at that branch.

**Why:** Maximizes utilization—any compact that passes rules can satisfy the reservation.

**Edge cases:**
- **Guaranteed model** for premium/luxury: pre-assign VIN and hold it out of pool.
- **One-way:** assign vehicle that is allowed to be dropped at destination (repositioning cost already in quote).

**Data model:**
```
Reservation(vehicleClassId, period) → at pickup → RentalAgreement(vehicleId)
```

**Concurrency:** when assigning, use **transaction + `SELECT ... FOR UPDATE`** on chosen row or **optimistic version** on `Vehicle` to prevent two staff picking the same unit."

---

### 2. Availability search at scale

**Interviewer:** "How do you implement search by location and dates efficiently?"

**Answer:**
"**Naive:** count vehicles where class matches and subtract those with overlapping reservations or rentals — expensive at peak.

**Better:**
1. **Index** reservations and rentals on `(branch_id, vehicle_class_id, pickup_at, return_at)` using GiST or btree on range types (PostgreSQL) or separate start/end columns with interval queries.
2. **Materialized availability buckets** per branch/class/day for read-heavy search; refresh incrementally on booking events.
3. **Cache** popular airport + weekend queries with short TTL; **stale OK** with message 'price may update at checkout'.

**Correctness vs speed:** checkout re-validates against **authoritative rows** under lock; search can be eventually consistent."

---

### 3. Pricing: taxes, coupons, and corporate rates

**Interviewer:** "How do you avoid a mess of if-statements in pricing?"

**Answer:**
"Use a **Strategy chain** or **rule engine**:
- Each rule outputs `PricedLine` rows (base, fees, discounts).
- **Order matters:** e.g., base → add-ons → percentage coupon → fixed coupon → tax.
- **Tax** pulled from `TaxRule` table keyed by pickup location and product tax category.

**Corporate:** negotiated `RatePlan` per account overrides public `StandardDailyRateStrategy` when customer has active contract.

**Audit:** persist **quote snapshot** on reservation so disputes compare against frozen line items."

---

### 4. Post-rental charges and disputes

**Interviewer:** "How do you handle damage billing after return?"

**Answer:**
"**Flow:**
1. Return inspection creates `InspectionReport`; compare to pickup photos/notes.
2. If new damage, staff opens **case** with estimate; system creates **adjustment line items** and performs **secondary capture** or **new charge** on stored payment method (within card network rules).
3. `RentalStatus` → **DISPUTED** until customer accepts or SLA expires.

**Idempotency:** each capture has idempotency key `rentalId|ADJUSTMENT|seq`.

**Ledger:** append-only `LedgerEntry` for finance; `Payment` rows link to PSP references."

---

### 5. Loyalty vs revenue recognition

**Interviewer:** "When do you award points?"

**Answer:**
"**Default:** post **CLOSED** rental and **successful capture** of final amount—avoids clawback complexity on free cancellations.

**Options:**
- **Partial earn** on non-refundable prepaid amounts.
- **Expire** points via scheduled job reading `PointsTransaction` and tier rules.

**Double earn prevention:** unique constraint on `(program_id, rental_id, type=EARN)`."

---

## 🗄️ Step 9: Database Schema (SQL)

```sql
-- Branches / locations
CREATE TABLE branch (
    branch_id       VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    timezone        VARCHAR(64) NOT NULL,
    address_line    VARCHAR(500),
    is_airport      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE vehicle_class (
    class_id        VARCHAR(32) PRIMARY KEY,
    display_name    VARCHAR(100) NOT NULL,
    typical_seats   SMALLINT NOT NULL,
    baggage_units   SMALLINT NOT NULL
);

CREATE TABLE vehicle_type (
    type_id         VARCHAR(36) PRIMARY KEY,
    class_id        VARCHAR(32) NOT NULL REFERENCES vehicle_class(class_id),
    make_model      VARCHAR(200) NOT NULL,
    transmission    VARCHAR(16) NOT NULL,
    fuel_type       VARCHAR(32) NOT NULL
);

CREATE TABLE vehicle (
    vehicle_id      VARCHAR(36) PRIMARY KEY,
    vin             VARCHAR(32) UNIQUE NOT NULL,
    type_id         VARCHAR(36) NOT NULL REFERENCES vehicle_type(type_id),
    home_branch_id  VARCHAR(36) NOT NULL REFERENCES branch(branch_id),
    current_branch_id VARCHAR(36) NOT NULL REFERENCES branch(branch_id),
    license_plate   VARCHAR(32),
    odometer_miles  BIGINT NOT NULL,
    status          VARCHAR(32) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_vehicle_branch_class ON vehicle(current_branch_id, type_id, status);

-- Reservations & rentals
CREATE TABLE reservation (
    reservation_id  VARCHAR(36) PRIMARY KEY,
    customer_id     VARCHAR(36) NOT NULL,
    class_id        VARCHAR(32) NOT NULL REFERENCES vehicle_class(class_id),
    pickup_branch_id VARCHAR(36) NOT NULL REFERENCES branch(branch_id),
    return_branch_id VARCHAR(36) NOT NULL REFERENCES branch(branch_id),
    pickup_at       TIMESTAMPTZ NOT NULL,
    return_at       TIMESTAMPTZ NOT NULL,
    status          VARCHAR(32) NOT NULL,
    quoted_total_cents BIGINT NOT NULL,
    idempotency_key VARCHAR(128) UNIQUE NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reservation_window ON reservation(pickup_branch_id, class_id, pickup_at, return_at)
    WHERE status IN ('CONFIRMED', 'MODIFIED');

CREATE TABLE rental_agreement (
    rental_id       VARCHAR(36) PRIMARY KEY,
    reservation_id  VARCHAR(36) REFERENCES reservation(reservation_id),
    customer_id     VARCHAR(36) NOT NULL,
    vehicle_id      VARCHAR(36) NOT NULL REFERENCES vehicle(vehicle_id),
    pickup_at       TIMESTAMPTZ NOT NULL,
    return_at       TIMESTAMPTZ NOT NULL,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rental_vehicle_time ON rental_agreement(vehicle_id, pickup_at, return_at);

CREATE TABLE line_item (
    line_id         BIGSERIAL PRIMARY KEY,
    rental_id       VARCHAR(36) NOT NULL REFERENCES rental_agreement(rental_id),
    code            VARCHAR(64) NOT NULL,
    description     VARCHAR(500),
    quantity        INT NOT NULL,
    unit_amount_cents BIGINT NOT NULL,
    total_cents     BIGINT NOT NULL
);

CREATE TABLE inspection_report (
    report_id       VARCHAR(36) PRIMARY KEY,
    rental_id       VARCHAR(36) NOT NULL REFERENCES rental_agreement(rental_id),
    kind            VARCHAR(16) NOT NULL, -- PICKUP / RETURN
    fuel_level_pct  SMALLINT NOT NULL,
    odometer_miles  BIGINT NOT NULL,
    signed_by_customer BOOLEAN NOT NULL DEFAULT FALSE
);

-- Payments
CREATE TABLE payment (
    payment_id      VARCHAR(36) PRIMARY KEY,
    rental_id       VARCHAR(36) REFERENCES rental_agreement(rental_id),
    reservation_id  VARCHAR(36) REFERENCES reservation(reservation_id),
    amount_cents    BIGINT NOT NULL,
    currency        CHAR(3) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128) UNIQUE NOT NULL,
    provider_ref    VARCHAR(128),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Loyalty
CREATE TABLE loyalty_account (
    account_id      VARCHAR(36) PRIMARY KEY,
    customer_id     VARCHAR(36) NOT NULL,
    program_id      VARCHAR(36) NOT NULL,
    points_balance  INT NOT NULL DEFAULT 0,
    tier            VARCHAR(32) NOT NULL,
    UNIQUE (customer_id, program_id)
);

CREATE TABLE points_transaction (
    txn_id          VARCHAR(36) PRIMARY KEY,
    account_id      VARCHAR(36) NOT NULL REFERENCES loyalty_account(account_id),
    type            VARCHAR(16) NOT NULL,
    points_delta    INT NOT NULL,
    reference_id    VARCHAR(64) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, reference_id, type)
);

-- Maintenance
CREATE TABLE service_order (
    order_id        VARCHAR(36) PRIMARY KEY,
    vehicle_id      VARCHAR(36) NOT NULL REFERENCES vehicle(vehicle_id),
    status          VARCHAR(32) NOT NULL,
    blocks_rental   BOOLEAN NOT NULL DEFAULT TRUE,
    opened_at       TIMESTAMPTZ NOT NULL,
    expected_complete_at TIMESTAMPTZ
);

CREATE INDEX idx_service_open ON service_order(vehicle_id) WHERE status IN ('OPEN', 'IN_PROGRESS');
```

---

## ⚙️ Step 10: Concurrency Handling (3 examples)

### Example 1: Double assignment of the same vehicle at pickup

**Problem:** Two agents pick up different customers and select the same `vehicle_id` simultaneously.

**Approach:**
- **Database transaction** around: validate reservation → select vehicle → update `vehicle.status` to `RENTED`.
- Use **`SELECT ... FOR UPDATE SKIP LOCKED`** when choosing a candidate row so workers do not block each other on different cars.
- Alternatively **optimistic locking**: `UPDATE vehicle SET status='RENTED', version=version+1 WHERE vehicle_id=? AND version=?`; retry on 0 rows updated.

**Invariant:** At most one `ACTIVE` rental per `vehicle_id` at a time (enforce with partial unique index in DB if modeling intervals as rows).

---

### Example 2: Overbooking a class (two reservations same last car)

**Problem:** Two customers complete payment for the same class/window when only one car remains.

**Approach:**
- On **confirm**, insert **`inventory_hold`** or decrement a **counter** in the same transaction as payment authorization—or use **serializable isolation** for the confirm path.
- At pickup, if no car satisfies class, **upgrade** or **deny with compensation** per policy (business fallback).

**Interview nuance:** prefer **optimistic** holds with periodic reconciliation for scale; **pessimistic** holds for high-value guaranteed models.

---

### Example 3: Duplicate payment capture on retry

**Problem:** Mobile client retries `POST /payments/capture`; gateway might charge twice.

**Approach:**
- Require **`Idempotency-Key`** header; store `(key, response)` atomically in `payment` table with **unique constraint** on `idempotency_key`.
- Second request returns **same** `provider_ref` and status without calling PSP again.

**Related:** use **outbox pattern** to publish `RentalClosed` exactly once to loyalty consumers.

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `Vehicle` encapsulates fleet unit state transitions; does not calculate tax.
- `PricingEngine` composes quotes; does not persist reservations.
- `IdempotentPaymentGateway` handles retry semantics only.

### Open/Closed ✅
```java
// Add new fee (e.g., young driver) without editing PricingEngine
public class YoungDriverFeeStrategy implements PricingStrategy { /* ... */ }
```

### Liskov Substitution ✅
```java
// Any PricingStrategy can replace another in the list without breaking quote()
List<PricingStrategy> strategies = List.of(new StandardDailyRateStrategy(4999), new OneWayFeeStrategy(15000));
```

### Interface Segregation ✅
```java
interface ReadableVehicleRepository { Optional<Vehicle> findById(String id); }
interface WritableVehicleRepository { void lockAndMarkRented(String id, long version); }
// Read-heavy search service depends only on readable side
```

### Dependency Inversion ✅
```java
public class RentalApplicationService {
    private final VehicleRepository vehicles; // abstraction, not JDBC
    // ...
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Class-level reservation** with **VIN assignment at pickup** balances utilization and customer expectations.
- ✅ **Pricing** as composable **strategies** plus persisted **quote snapshots** for disputes.
- ✅ **Explicit state machines** for vehicle, reservation, and rental lifecycles.
- ✅ **Optimistic or pessimistic concurrency** on vehicle assignment; **idempotent payments**.

### Domain breadth
- ✅ Branches, classes, types, and maintenance **block** availability.
- ✅ **Inspection reports** anchor fuel and damage charges.
- ✅ **Loyalty** posts after financial close with **deduplicated** earn transactions.

### Operations
- ✅ Search can be **cached**; **checkout** must be **strongly consistent**.
- ✅ **Ledger + payment** tables support reconciliation and partial captures.

---

**Ready for review!**
