# Low-Level Design: Restaurant Order & Rating System

**Difficulty:** Medium-Hard ⚡

**Interview Duration:** 90-120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

**Scope note:** The title emphasizes **orders and ratings**; the narrative below still covers **full venue operations** (tables, KDS, billing) like Toast/Square. In a shorter interview, focus on **order lifecycle**, **menu**, **off-prem delivery**, and **per-order ratings**; treat floor/staff sections as optional depth.

---

## 🎯 Step 1: Understand the Problem (5-8 minutes)

### What the Interviewer Says:
*"Design a restaurant management system (similar in scope to Toast or Square for Restaurants) that runs operations across multiple locations: floor plans and tables, reservations and waitlists, menus and modifiers, dine-in and off-premise orders, kitchen routing, billing with splits, payments, inventory, staff, loyalty, and reporting."*

### Clarifying Questions to Ask:

1. **Q:** Is this multi-tenant SaaS (many restaurant brands) or one brand with many stores?  
   **A:** Multi-location per account/brand; data isolated by `locationId`; optional enterprise rollup for reporting.

2. **Q:** How strict is table double-booking vs walk-ins?  
   **A:** Reservations hold table capacity for a time window; walk-ins and waitlist compete for free tables with configurable merge/split rules.

3. **Q:** Order channels: only in-house POS, or also online ordering and third-party delivery?  
   **A:** Dine-in, takeout, and delivery; external aggregator orders map into the same order pipeline with external reference IDs.

4. **Q:** Menu availability: per location, per channel, time-of-day?  
   **A:** Yes—items and categories can be toggled by location, channel, and schedule; 86ing (temporary unavailability) is real-time.

5. **Q:** Kitchen model: one line or multiple stations (grill, fry, cold)?  
   **A:** Multiple stations; tickets route line items to stations; expediters may consolidate for coursing.

6. **Q:** Bill splitting: equal split, by item, or custom shares?  
   **A:** All three; support multiple payers and partial payments until the check is closed.

7. **Q:** Payments: integrated card terminals, cash, house accounts, tips?  
   **A:** Card (with tip adjust), cash, gift cards, split across methods; idempotent captures and voids.

8. **Q:** Inventory granularity: recipe-level depletion on fire or on close?  
   **A:** Configurable per item; typically deduct on fire/sell with waste/spoilage adjustments; low-stock alerts and supplier POs.

9. **Q:** Staff: roles, permissions, tip pooling?  
   **A:** Roles (server, cook, manager), shift schedules, clock-in/out; optional tip pool rules by role and shift.

10. **Q:** Loyalty: points per spend, tiers, redemption at checkout?  
    **A:** Accrual on eligible tender; redemptions as discounts or comps; program rules vary by location or chain.

11. **Q:** Reporting latency: real-time dashboards vs nightly batch?  
    **A:** Near-real-time operational tiles; heavier aggregates (LTV, cohorts) async/batch with SLAs.

12. **Q:** Offline / network loss at the store?  
    **A:** POS should queue orders locally with sync conflict rules; payments may require store-and-forward policies (discuss with interviewer).

13. **Q:** Regulatory: sales tax, service charges, alcohol?  
    **A:** Tax rules per jurisdiction; service charge as separate line type; age-restricted items flagged.

---

## 🔹 Step 2: Gather Requirements (10-15 minutes)

### Functional Requirements

#### Multi-Location & Configuration (FR1-FR8)
1. System shall support many locations under an organization/brand with independent timezones, tax profiles, and business hours
2. Each location shall have address, contact, currency, and fiscal identifiers for reporting
3. Admin shall configure location-level settings (gratuity defaults, auto-gratuity thresholds, rounding mode)
4. System shall support copying menu templates across locations with localized pricing
5. Staff users shall be assignable to one or more locations with role-based access
6. Cross-location reporting shall aggregate with filters while respecting data access scopes
7. System shall maintain audit trail of configuration changes per location
8. Feature flags (e.g., delivery, loyalty) shall be togglable per location

#### Table Management & Layout (FR9-FR16)
9. Each location shall define one or more floor plans with named zones (e.g., Patio, Bar)
10. System shall model tables with identifiers, shape metadata, seat capacity, min/max party size, and join/split capabilities
11. Table shall have operational status: AVAILABLE, OCCUPIED, RESERVED, DIRTY, BLOCKED, OUT_OF_SERVICE
12. Host shall seat parties by assigning tables (single or combined) and updating status transitions
13. System shall support table turns: expected duration, actual duration, and cleanup workflow
14. Layout editor shall persist coordinates for POS and host stand visualization
15. System shall prevent seating that exceeds combined capacity unless overridden by manager role
16. Table assignment shall link to active order/check when dine-in service starts

#### Reservations & Waitlist (FR17-FR25)
17. Guest or staff shall create reservations with party size, date/time, duration, notes, and occasion tags
18. System shall match reservations to eligible tables by capacity, zone preference, and accessibility needs
19. Reservations shall have lifecycle: REQUESTED, CONFIRMED, SEATED, COMPLETED, NO_SHOW, CANCELLED
20. System shall send reminders and support deposit/hold policies (interface to payment gateway)
21. Walk-in waitlist shall capture party size, quote wait times, and notify when table is ready
22. Waitlist entries shall be orderable by policy (FIFO, estimated ready time, VIP priority)
23. System shall detect double-booking conflicts for the same table overlapping reservation windows
24. System shall support waitlist-to-reservation promotion when cancellations occur
25. No-show handling shall release tables and optionally charge fees per policy

#### Menu Management (FR26-FR38)
26. Menu shall be organized as categories and items with names, descriptions, POS codes, and images (optional)
27. Items shall have base price; location-specific price overrides shall be supported
28. Modifier groups shall be attached to items (required/optional, min/max selections, priced modifiers)
29. Items shall support variants (size, protein) as either separate SKUs or modifier-driven pricing
30. Categories and items shall support ordering, hiding, and channel visibility (dine-in vs online vs delivery)
31. System shall support scheduled availability (breakfast-only items) and happy-hour pricing windows
32. Manager shall 86 an item or mark temporarily unavailable with reason and optional auto-restore time
33. Allergen and dietary tags shall be attachable for guest filtering and kitchen alerts
34. Combo/bundle items shall expand to component line items for kitchen and inventory mapping
35. Menu versioning shall allow scheduled publishes ("go live at midnight")
36. Tax category and service charge applicability shall be configurable per item
37. Alcohol and age-restricted items shall require compliance flags per jurisdiction
38. Menu search on POS shall be performant with fuzzy match on name/code

#### Order Management (FR39-FR52)
39. System shall create orders of type DINE_IN, TAKEOUT, or DELIVERY with channel metadata
40. Dine-in orders shall bind to table(s) and serving staff; takeout/delivery to customer contact and pickup/dispatch times
41. Order shall contain lines with item, quantity, modifiers, seat/party label (for split-by-seat), and special instructions
42. Order status shall progress through DRAFT, SUBMITTED, IN_KITCHEN, READY, SERVED, COMPLETED, VOIDED with role-gated transitions
43. System shall support coursing (appetizer/entree) and fire-hold instructions per line or course
44. Takeout shall support promised ready time and customer notifications
45. Delivery shall store address, delivery zone, fees, and third-party delivery IDs when applicable
46. Voids and comps shall require manager approval above configurable thresholds with reason codes
47. Discounts (coupon, staff meal, loyalty redemption) shall apply as line or check level with stacking rules
48. Service charges (large party, delivery) shall appear as distinct line types
49. Order edits after submit shall follow policy (re-fire kitchen, audit trail)
50. System shall link multiple orders to one check or split one order across checks when business rules allow
51. Package tracking for retail items (merch) may attach serials or simple quantity
52. System shall expose idempotent APIs for external ordering integrations

#### Kitchen Display System (KDS) & Routing (FR53-FR60)
53. On order submit, system shall generate kitchen tickets decomposed by station (GRILL, FRY, SALAD, DESSERT, BAR, etc.)
54. Each ticket line shall show modifiers, allergies, coursing, and ticket age/color for SLA
55. Stations shall subscribe to their queue; bump/recall actions shall update state for expo and servers
56. System shall support routing rules: item-to-station map, load balancing, or printer fallback
57. KDS shall show estimated prep times and overdue warnings based on historical data
58. All-day items vs rush items shall be sortable (FIFO vs priority)
59. Expo view shall aggregate tickets for the same table/order for coordination
60. Kitchen metrics (avg bump time) shall feed back to reporting

#### Billing, Checks & Splitting (FR61-FR68)
61. System shall open one or more checks per table/order context; each check has line items, taxes, and totals
62. Line items may be assigned to logical seats or unnamed split groups for guest UX
63. Split modes: equal parts across N guests; by assigned items; custom percentage or amount shares
64. Rounding policy per location (round per line, round at check, banker’s rounding) shall be explicit
65. Partial payments shall reduce open balance; overpayment handling (cash change) shall be supported
66. Transfer of items between checks shall be permissioned and audited
67. Pre-authorization and incremental auth for bar tabs shall be supported where integrated
68. Closed checks shall be immutable except via controlled adjustment workflows (refund, tip adjust window)

#### Payments (FR69-FR76)
69. System shall accept CASH, CARD, GIFT_CARD, HOUSE_ACCOUNT, EXTERNAL_WALLET with extensibility
70. Card flows shall support authorize, capture, void, refund, and tip adjustment within card network rules
71. Split payment shall allocate amounts across tenders until balance is zero
72. Receipts shall be generated per tender or consolidated; digital receipt via email/SMS (interface)
73. Payment intents shall be idempotent using client keys to prevent duplicate charges on retries
74. Cash drawer sessions shall track open/close counts and expected vs actual
75. Reconciliation exports shall map settlements to checks and locations
76. Failed payments shall surface actionable errors to cashier without losing cart state

#### Inventory & Suppliers (FR77-FR86)
77. Ingredient catalog shall define SKU, unit of measure, par levels, and per-location stock records
78. Recipes shall map menu items to ingredient quantities for theoretical depletion
79. Stock shall decrement on configured trigger (fire, serve, close) with negative stock blocked or allowed by policy
80. System shall support manual adjustments (waste, spoilage, theft) with reason codes
81. Low-stock and out-of-stock alerts shall notify managers; optional auto-86 linked menu items
82. Suppliers shall have contact, terms, and catalog pricing; purchase orders shall track order/receive states
83. Receiving shall update on-hand quantities and unit costs (weighted average or FIFO—configurable)
84. Stock counts (cycle count) shall reconcile system vs physical with variance reporting
85. Inter-location transfers shall move stock between stores with in-transit state
86. Expiration and lot tracking shall be optional for perishable ingredients

#### Staff & Shifts (FR87-FR93)
87. Staff profiles shall include roles, certifications (e.g., alcohol service), and employment status
88. Shifts shall be scheduled with role, location, start/end; overtime rules are configuration hooks
89. Clock-in/out shall validate location and role; exceptions flagged for manager approval
90. Order service shall attribute default server by shift assignment; reassignments audited
91. Permission matrix shall gate voids, comps, discounts, price overrides, and reports
92. Tip allocation: direct tips, pooled tips by formula, and payroll export hooks
93. Break compliance tracking may integrate with labor law configurations (jurisdiction-specific)

#### Loyalty Programs (FR94-FR99)
94. Customers may enroll in loyalty with phone/email; duplicate merge workflow supported
95. Points accrue on eligible net spend after tenders settle; earn rules configurable by location and tier
96. Redemption at checkout shall support fixed-value rewards or percentage off with caps
97. Tiers shall unlock benefits (priority seating, birthday offers) evaluated on schedule
98. Points adjustments (goodwill, fraud reversal) shall be manager-audited
99. Integration with external CRM/coalition programs may map external member IDs

#### Reporting & Analytics (FR100-FR106)
100. Sales reports: net sales, gross, discounts, taxes, tips, by location and channel
101. Menu analytics: item popularity, margin proxy (if cost data), 86 frequency
102. Labor vs sales snapshots by hour; peak hour heatmaps per location
103. Staff performance: average check, table turn time, void/comp rates (used carefully for coaching)
104. Inventory valuation and usage variance reports
105. Reservation and waitlist conversion, no-show rates
106. Export to CSV/BI tools and scheduled email digests

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many locations, orders, and concurrent POS devices?"
- Thousands of locations, peak dinner rush with hundreds of orders/minute per metro; global organizations
- Reads (menu, layout) heavy; writes (orders, payments) bursty per location

**Deduced NFRs:**
- ✅ Horizontal scale of stateless API tier; partition operational data by `locationId` (and shard key for hot spots)
- ✅ Read replicas or cached menu/layout snapshots with TTL and invalidation on publish
- ✅ Async workers for notifications, loyalty posting, analytics ETL, and search indexing
- ✅ Rate limiting per API key/device; backpressure on KDS websocket fanout

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"
- No two parties seated on overlapping capacity for the same physical table without explicit combine
- Payment capture totals must match authorized amounts (within tip adjust rules)
- Inventory cannot go negative if policy forbids it; ledger adjustments must balance

**Deduced NFRs:**
- ✅ **Strong consistency** for check totals and payment state transitions (transactional outbox or single-writer per check)
- ✅ **Serializable or per-check serialization** for concurrent split/pay operations
- ✅ **Optimistic locking** on table map and reservation rows with conflict surfacing to host UI
- ✅ **Eventual consistency** acceptable for analytics dashboards within bounded staleness

---

#### 3. **Availability Analysis**

**Think:** "What happens when payment provider or cloud blips?"
- Stores lose money if POS is down; KDS delay backs up kitchen

**Deduced NFRs:**
- ✅ **99.9%+** target for order capture path; degraded mode for read-only menu if writes fail
- ✅ **Circuit breakers** to PSP; queued offline operations with conflict resolution policies
- ✅ **Multi-AZ** deployments; durable messaging for cross-service workflows
- ✅ **Graceful KDS degradation** (fallback printers, last-known queue)

---

#### 4. **Maintainability Analysis**

**Think:** "Who configures this?"
- Menu managers, GMs, finance, IT integrations

**Deduced NFRs:**
- ✅ **Admin consoles** with versioning for menu and tax rules
- ✅ **Structured audit logs** for voids, comps, price overrides, inventory adjustments
- ✅ **Feature flags** per location; configuration validation before publish
- ✅ **Extension points** for payment processors, delivery aggregators, and payroll

---

#### 5. **Performance Analysis**

**Think:** "SLAs at the register and KDS?"
- Menu load < 300ms P95; order submit acknowledgment < 500ms; KDS push < 200ms

**Deduced NFRs:**
- ✅ **Caching** of hot menu data; **CDN** for static assets
- ✅ **WebSocket or SSE** to KDS with incremental ticket updates
- ✅ **Indexed queries** on reservations by window and location
- ✅ **Precomputed aggregates** for heavy reports with refresh jobs

---

#### 6. **Security Analysis**

**Think:** "PII, PCI, fraud?"
- Card data never stored raw (tokenization); staff roles; tip fraud; loyalty abuse

**Deduced NFRs:**
- ✅ **PCI scope minimization** via hosted fields / terminal SDK; no PAN in logs
- ✅ **RBAC** with least privilege; break-glass manager PIN for sensitive actions
- ✅ **Encryption** at rest for PII; TLS everywhere
- ✅ **Fraud controls**: velocity limits on loyalty redemption, idempotent payments, audit on refunds

---

## 🧩 Step 3: Identify Core Entities (12-18 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Multi-location" | Organization, Location, TaxProfile |
| "Floor plan, tables" | FloorPlan, Zone, Table, TableStatus |
| "Reservations, waitlist" | Reservation, WaitlistEntry, Party |
| "Menu, modifiers" | Menu, Category, MenuItem, ModifierGroup, Modifier |
| "Orders, channels" | Order, OrderLine, OrderType, Customer |
| "Kitchen stations" | KitchenTicket, Station, RoutingRule |
| "Checks, split" | Check, CheckLine, SplitGroup, Allocation |
| "Payments" | Payment, Tender, PaymentIntent, CashDrawerSession |
| "Inventory" | Ingredient, Stock, Recipe, Supplier, PurchaseOrder |
| "Staff" | Staff, Role, Shift, TimePunch |
| "Loyalty" | LoyaltyProgram, LoyaltyAccount, RewardRule |
| "Reports" | ReportDefinition, AggregateSnapshot |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Organization | ✅ YES | Tenant/brand root for billing and SSO |
| Location | ✅ YES | Physical store with config and hours |
| TaxProfile | ✅ YES | Jurisdiction rules attached to location |
| FloorPlan | ✅ YES | Versioned layout container |
| Zone | ✅ YES | Named area within floor plan |
| Table | ✅ YES | Bookable/seatable resource |
| TableStatus | ✅ YES | Enum + transition rules |
| Reservation | ✅ YES | Time-bound commitment |
| WaitlistEntry | ✅ YES | Queue element with state |
| Party | ✅ YES | Group of guests for seating (or value object under reservation) |
| Menu | ✅ YES | Published catalog snapshot |
| Category | ✅ YES | Grouping of items |
| MenuItem | ✅ YES | Sellable SKU with pricing hooks |
| ModifierGroup / Modifier | ✅ YES | Structured options |
| Order | ✅ YES | Transactional aggregate root |
| OrderLine | ✅ YES | Child of order |
| KitchenTicket | ✅ YES | Station-scoped work unit |
| Station | ✅ YES | Kitchen sink with queue |
| Check | ✅ YES | Financial document per payment session |
| PaymentIntent | ✅ YES | Idempotent payment lifecycle |
| Ingredient / Stock | ✅ YES | Inventory atoms |
| Recipe | ✅ YES | BOM for depletion |
| Staff / Shift | ✅ YES | Labor scheduling |
| LoyaltyAccount | ✅ YES | Mutable balance with rules |

### Final Entity List

**Org & Location:**
1. **Organization** — Billing, branding, SSO tenant
2. **Location** — Store, timezone, address, settings
3. **BusinessHours** — Recurring/exception hours per location
4. **TaxProfile** — Rates, tax categories, service charge taxability

**Floor & Tables:**
5. **FloorPlan** — Layout revision per location
6. **Zone** — Patio, dining room, bar
7. **Table** — Capacity, shape, position, combinable flags
8. **TableMerge** — Temporary combined seating unit

**Reservations:**
9. **Reservation** — Time window, party, status, deposit reference
10. **WaitlistEntry** — Queue position, quoted wait, notifications
11. **Party** — Size, tags (birthday, accessibility)

**Menu:**
12. **Menu** — Published menu header (version, channel)
13. **MenuCategory** — Ordering, visibility schedule
14. **MenuItem** — Base item with kitchen routing hints
15. **ModifierGroup** — Selection rules
16. **Modifier** — Option with price delta
17. **MenuPublishEvent** — Audit/version for go-live

**Orders & Kitchen:**
18. **Order** — Channel, status, links to check(s)
19. **OrderLine** — Item snapshot, modifiers, seat id, course
20. **KitchenTicket** — Routed subset for a station
21. **Station** — Name, display order, device bindings
22. **RoutingRule** — Item/station mapping

**Billing & Payments:**
23. **Check** — Open/closed financial container
24. **CheckLine** — Charges, taxes, tips (allocation targets)
25. **SplitAllocation** — Maps amounts to payer/split group
26. **PaymentIntent** — Idempotent pay operation
27. **Tender** — Concrete payment record (card/cash/…)
28. **CashDrawerSession** — Open/close reconciliation

**Inventory:**
29. **Ingredient** — Master item
30. **LocationStock** — On-hand per location
31. **Recipe** — Depletion recipe lines
32. **Supplier** — Vendor master
33. **PurchaseOrder** / **GoodsReceipt** — Replenishment flow

**Staff:**
34. **Staff** — Person + employments
35. **Role** — Permission sets
36. **Shift** — Scheduled work interval
37. **TimePunch** — Actual attendance

**Loyalty & Customer:**
38. **Customer** — PII profile
39. **LoyaltyProgram** — Rule container
40. **LoyaltyAccount** — Points balance, tier
41. **LoyaltyLedgerEntry** — Earn/burn/adjust

**Reporting:**
42. **ReportJob** — Scheduled export
43. **SalesAggregate** — Precomputed rollups (optional materialized)

---

## 🔗 Step 4: Establish Relationships (15-20 minutes)

### Pass 1: Organization & Location

#### Organization ↔ Location
**Conclusion:** **Composition** (org owns locations)
```
Organization ◆────→ Location [1..*]
```

#### Location ↔ FloorPlan
**Conclusion:** **Composition**
```
Location ◆────→ FloorPlan [1..*] (historical revisions)
```

#### FloorPlan ↔ Zone ↔ Table
**Conclusion:** **Composition** hierarchy
```
FloorPlan ◆────→ Zone [0..*] ◆────→ Table [1..*]
```

---

### Pass 2: Reservations & Tables

#### Reservation ↔ Table
**Conclusion:** **Association** (optional specific table or table class)
```
Reservation ─────→ Table [0..*] (preferred tables)
Reservation ─────→ Location [1]
```

#### WaitlistEntry ↔ Location
**Conclusion:** **Association**
```
WaitlistEntry ─────→ Location [1]
```

#### Table ↔ Order (dine-in)
**Conclusion:** **Association** during service
```
Table ─────→ Order [0..1] active
Order ─────→ Table [0..*] (combined tables)
```

---

### Pass 3: Menu & Orders

#### Menu ↔ MenuCategory ↔ MenuItem
**Conclusion:** **Composition**
```
Menu ◆────→ MenuCategory [1..*] ◆────→ MenuItem [0..*]
MenuItem ─────→ ModifierGroup [0..*]
ModifierGroup ◆────→ Modifier [1..*]
```

#### Order ↔ OrderLine
**Conclusion:** **Composition**
```
Order ◆────→ OrderLine [1..*]
OrderLine ─────→ MenuItem (snapshot + id reference)
```

#### OrderLine ↔ KitchenTicket
**Conclusion:** **Association** (lines appear on 1+ tickets)
```
KitchenTicket ◆────→ OrderLineRef [1..*]
KitchenTicket ─────→ Station [1]
```

---

### Pass 4: Checks & Payments

#### Order ↔ Check
**Conclusion:** **Association** (split scenarios)
```
Order ─────→ Check [1..*]
Check ◆────→ CheckLine [1..*]
```

#### Check ↔ PaymentIntent
**Conclusion:** **Composition**
```
Check ◆────→ PaymentIntent [0..*]
PaymentIntent ◆────→ Tender [0..*]
```

---

### Pass 5: Inventory & Recipes

#### MenuItem ↔ Recipe
**Conclusion:** **Association**
```
MenuItem ─────→ Recipe [0..1]
Recipe ◆────→ RecipeLine ─────→ Ingredient
LocationStock ─────→ Ingredient [N:1 per location]
```

---

### Pass 6: Staff & Loyalty

#### Staff ↔ Location
**Conclusion:** **Many-to-many** via employment
```
Staff ─────→ LocationAssignment [1..*]
Shift ─────→ Staff [1], Location [1]
```

#### Customer ↔ LoyaltyAccount
**Conclusion:** **Association**
```
Customer ─────→ LoyaltyAccount [0..*] (per program)
LoyaltyAccount ◆────→ LoyaltyLedgerEntry [0..*]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Organization → Location | 1:N | Composition |
| Location → FloorPlan | 1:N | Composition |
| Zone → Table | 1:N | Composition |
| Reservation → Table | N:M | Association (preferred) |
| Order → OrderLine | 1:N | Composition |
| Order → Check | 1:N | Association |
| Check → PaymentIntent | 1:N | Composition |
| MenuItem → Station (via rules) | N:M | Association |
| Recipe → Ingredient | N:M via RecipeLine | Association |
| Staff → Shift | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (18-25 minutes)

### Class Diagram 1: Core Enums

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>>  │
│   TableStatus    │  │    OrderType     │  │   OrderStatus    │
├──────────────────┤  ├──────────────────┤  ├──────────────────┤
│ AVAILABLE        │  │ DINE_IN          │  │ DRAFT            │
│ OCCUPIED         │  │ TAKEOUT          │  │ SUBMITTED        │
│ RESERVED         │  │ DELIVERY         │  │ IN_KITCHEN       │
│ DIRTY            │  │                  │  │ READY            │
│ BLOCKED          │  │                  │  │ SERVED           │
│ OUT_OF_SERVICE   │  │                  │  │ COMPLETED        │
└──────────────────┘  └──────────────────┘  │ VOIDED           │
                                            └──────────────────┘

┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │  │ <<enumeration>>  │
│ ReservationState │  │   PaymentState   │  │   TenderType     │
├──────────────────┤  ├──────────────────┤  ├──────────────────┤
│ REQUESTED        │  │ INITIATED        │  │ CASH             │
│ CONFIRMED        │  │ AUTHORIZED       │  │ CARD             │
│ SEATED           │  │ CAPTURED         │  │ GIFT_CARD        │
│ COMPLETED        │  │ VOIDED           │  │ HOUSE_ACCOUNT    │
│ NO_SHOW          │  │ FAILED           │  │ OTHER            │
│ CANCELLED        │  └──────────────────┘  └──────────────────┘
└──────────────────┘
```

---

### Class Diagram 2: Location, Floor Plan & Table

```
┌─────────────────────────────────────────────────────────────────┐
│                         Organization                            │
├─────────────────────────────────────────────────────────────────┤
│ - id: String                                                    │
│ - name: String                                                  │
├─────────────────────────────────────────────────────────────────┤
│ + getLocations(): List<Location>                                │
└─────────────────────────────────────────────────────────────────┘
                              │ 1
                              │ owns
                              ▼ *
┌─────────────────────────────────────────────────────────────────┐
│                          Location                               │
├─────────────────────────────────────────────────────────────────┤
│ - id, name, timezone, currency                                  │
│ - taxProfile: TaxProfile                                        │
│ - settings: LocationSettings                                   │
├─────────────────────────────────────────────────────────────────┤
│ + getActiveFloorPlan(): FloorPlan                               │
│ + getBusinessHours(day): List<TimeWindow>                        │
└─────────────────────────────────────────────────────────────────┘
         │ 1                              *
         │ composes                     │
         ▼                              │
┌──────────────────┐                    │
│    FloorPlan     │◄───────────────────┘
├──────────────────┤
│ - id, version    │
│ - effectiveFrom  │
├──────────────────┤
│ + getZones()     │
└──────────────────┘
         │ 1
         ▼ *
┌──────────────────┐       ┌──────────────────┐
│      Zone        │◄──────│      Table       │
├──────────────────┤   *   ├──────────────────┤
│ - id, name       │       │ - id, label      │
│ - sortOrder      │       │ - capacity       │
└──────────────────┘       │ - pos: Point     │
                           │ - status: TableStatus
                           │ - combinableIds  │
                           ├──────────────────┤
                           │ + canSeat(n): bool│
                           └──────────────────┘
```

---

### Class Diagram 3: Reservation & Waitlist

```
┌─────────────────────────────────────────────────────────────────┐
│                        Reservation                                │
├─────────────────────────────────────────────────────────────────┤
│ - id, locationId                                                │
│ - party: Party                                                  │
│ - window: TimeRange                                             │
│ - state: ReservationState                                       │
│ - depositPaymentId: Optional<String>                            │
│ - preferredZoneIds: List<String>                                │
├─────────────────────────────────────────────────────────────────┤
│ + confirm(): void                                               │
│ + seat(tables: List<Table>): void                               │
│ + markNoShow(): void                                            │
└─────────────────────────────────────────────────────────────────┘
           │ 0..*                          │
           │ prefers                       │ 1
           ▼                               ▼
      ┌────────┐                      ┌──────────┐
      │ Table  │                      │ Location │
      └────────┘                      └──────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     WaitlistEntry                                │
├─────────────────────────────────────────────────────────────────┤
│ - id, locationId, partySize                                     │
│ - quotedMinutes: int                                            │
│ - state: WAITING | NOTIFIED | SEATED | LEFT                     │
│ - createdAt                                                     │
├─────────────────────────────────────────────────────────────────┤
│ + notifyGuest(): void                                           │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Menu, Modifiers & Item

```
┌─────────────────────────────────────────────────────────────────┐
│                           Menu                                  │
├─────────────────────────────────────────────────────────────────┤
│ - id, version, channel: Channel                                 │
│ - effectiveRange: Optional<TimeRange>                           │
├─────────────────────────────────────────────────────────────────┤
│ + getCategories(): List<MenuCategory>                           │
└─────────────────────────────────────────────────────────────────┘
                         △
                         │ published as
┌─────────────────────────────────────────────────────────────────┐
│                       MenuCategory                                │
├─────────────────────────────────────────────────────────────────┤
│ - id, name, sortIndex                                           │
│ - visibility: SchedulePredicate                                 │
├─────────────────────────────────────────────────────────────────┤
│ + getItems(): List<MenuItem>                                    │
└─────────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                        MenuItem                                 │
├─────────────────────────────────────────────────────────────────┤
│ - id, name, posCode                                            │
│ - basePrice: Money                                              │
│ - taxCategoryId, stationId: Station                             │
│ - allergenTags: Set<Allergen>                                   │
│ - eightySixedUntil: Optional<Instant>                           │
├─────────────────────────────────────────────────────────────────┤
│ + priceAt(locationId): Money                                    │
│ + isAvailable(ctx: SaleContext): boolean                        │
└─────────────────────────────────────────────────────────────────┘
         │
         │ 0..*
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ModifierGroup                               │
├─────────────────────────────────────────────────────────────────┤
│ - minSelect, maxSelect                                          │
│ - required: boolean                                             │
├─────────────────────────────────────────────────────────────────┤
│ + getModifiers(): List<Modifier>                                │
└─────────────────────────────────────────────────────────────────┘
                         │ 1..*
                         ▼
                  ┌────────────┐
                  │  Modifier  │
                  ├────────────┤
                  │ - name     │
                  │ - priceDelta
                  └────────────┘
```

---

### Class Diagram 5: Order, KDS & Check

```
┌─────────────────────────────────────────────────────────────────┐
│                          Order                                  │
├─────────────────────────────────────────────────────────────────┤
│ - id, locationId, type: OrderType                               │
│ - status: OrderStatus                                           │
│ - tableIds: List<String>                                        │
│ - serverStaffId: Optional<String>                               │
│ - customer: Optional<CustomerRef>                               │
├─────────────────────────────────────────────────────────────────┤
│ + addLine(item, qty, mods): OrderLine                           │
│ + submit(kitchen: KitchenRouter): List<KitchenTicket>           │
│ + toChecks(splitter: CheckFactory): List<Check>                 │
└─────────────────────────────────────────────────────────────────┘
                         │ 1
                         ▼ *
┌─────────────────────────────────────────────────────────────────┐
│                        OrderLine                                │
├─────────────────────────────────────────────────────────────────┤
│ - id, menuItemId, snapshot: ItemSnapshot                        │
│ - quantity, seatLabel, course: int                              │
│ - modifiers: List<AppliedModifier>                              │
│ - specialInstructions: String                                   │
├─────────────────────────────────────────────────────────────────┤
│ + routedStation(): Station                                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     KitchenTicket                               │
├─────────────────────────────────────────────────────────────────┤
│ - id, orderId, stationId                                        │
│ - lines: List<OrderLineRef>                                   │
│ - kdsState: NEW | IN_PROGRESS | DONE | RECALLED               │
├─────────────────────────────────────────────────────────────────┤
│ + bump(): void                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    <<interface>>                               │
│                    KitchenRouter                                │
├─────────────────────────────────────────────────────────────────┤
│ + route(order: Order): List<KitchenTicket>                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          Check                                  │
├─────────────────────────────────────────────────────────────────┤
│ - id, orderIds: List<String>                                    │
│ - state: OPEN | CLOSED                                         │
│ - lines: List<CheckLine>                                        │
│ - taxLines, serviceChargeLines                                  │
├─────────────────────────────────────────────────────────────────┤
│ + addSplit(alloc: SplitAllocation): void                        │
│ + balanceDue(): Money                                           │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Payments & Loyalty

```
┌─────────────────────────────────────────────────────────────────┐
│                     PaymentIntent                               │
├─────────────────────────────────────────────────────────────────┤
│ - idempotencyKey: String                                        │
│ - checkId                                                       │
│ - state: PaymentState                                           │
│ - requestedAmount: Money                                        │
├─────────────────────────────────────────────────────────────────┤
│ + authorize(): void                                             │
│ + capture(): void                                               │
└─────────────────────────────────────────────────────────────────┘
                         │ 1
                         ▼ 0..*
┌─────────────────────────────────────────────────────────────────┐
│                          Tender                                 │
├─────────────────────────────────────────────────────────────────┤
│ - type: TenderType                                              │
│ - amount: Money                                                 │
│ - tipAmount: Money                                              │
│ - externalTxnRef: String                                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     LoyaltyAccount                              │
├─────────────────────────────────────────────────────────────────┤
│ - customerId, programId                                         │
│ - pointsBalance: int                                            │
│ - tier: Tier                                                    │
├─────────────────────────────────────────────────────────────────┤
│ + earn(check: Check): List<LoyaltyLedgerEntry>                  │
│ + redeem(offer, check): Money                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35-45 minutes)

### Enums (Java)

```java
// TableStatus.java
public enum TableStatus {
    AVAILABLE, OCCUPIED, RESERVED, DIRTY, BLOCKED, OUT_OF_SERVICE
}

// OrderType.java
public enum OrderType {
    DINE_IN, TAKEOUT, DELIVERY
}

// OrderStatus.java
public enum OrderStatus {
    DRAFT, SUBMITTED, IN_KITCHEN, READY, SERVED, COMPLETED, VOIDED
}
```

---

### Table & Seating

```java
// Table.java
public class Table {
    private final String id;
    private final String zoneId;
    private final int capacity;
    private TableStatus status;

    public boolean canSeat(int partySize) {
        return status == TableStatus.AVAILABLE && partySize <= capacity;
    }

    public void seat() {
        if (status != TableStatus.AVAILABLE) throw new IllegalStateException();
        this.status = TableStatus.OCCUPIED;
    }

    public void releaseToDirty() {
        this.status = TableStatus.DIRTY;
    }

    public void markReady() {
        this.status = TableStatus.AVAILABLE;
    }

    // getters ...
}
```

---

### Kitchen Routing (Strategy-friendly)

```java
// KitchenRouter.java
public interface KitchenRouter {
    List<KitchenTicket> route(Order order);
}

// StationRoutingByItem.java
public class StationRoutingByItem implements KitchenRouter {
    private final Map<String, String> menuItemIdToStationId;

    @Override
    public List<KitchenTicket> route(Order order) {
        Map<String, KitchenTicket> byStation = new LinkedHashMap<>();
        for (OrderLine line : order.getLines()) {
            String stationId = menuItemIdToStationId.getOrDefault(
                line.getMenuItemId(), "DEFAULT");
            byStation.computeIfAbsent(stationId,
                id -> new KitchenTicket(order.getId(), id))
                .addLineRef(line.getId());
        }
        return new ArrayList<>(byStation.values());
    }
}
```

---

### Order Submit Flow

```java
// Order.java (excerpt)
public class Order {
    private OrderStatus status = OrderStatus.DRAFT;

    public void submit(KitchenRouter router, KdsPublisher kds) {
        if (lines.isEmpty()) throw new IllegalStateException();
        this.status = OrderStatus.SUBMITTED;
        List<KitchenTicket> tickets = router.route(this);
        for (KitchenTicket t : tickets) {
            kds.publish(t);
        }
        this.status = OrderStatus.IN_KITCHEN;
    }
}
```

---

### Bill Splitting

```java
// SplitMode.java
public enum SplitMode { EQUAL, BY_ITEM, CUSTOM_AMOUNT }

// CheckSplitter.java
public class CheckSplitter {

    public List<Check> splitByEqualParts(Check original, int parts, RoundingPolicy rounding) {
        Money subtotal = original.getPreTaxSubtotal();
        List<Money> shares = rounding.splitEvenly(subtotal, parts);
        List<Check> checks = new ArrayList<>();
        for (int i = 0; i < parts; i++) {
            Check c = original.spawnEmptyChild();
            c.addProportionalLines(original, shares.get(i), subtotal);
            checks.add(c);
        }
        return checks;
    }

    public Check extractItems(Check original, Set<String> lineIds) {
        return original.moveLinesToNewCheck(lineIds);
    }
}
```

---

### Payment Idempotency

```java
// PaymentService.java (excerpt)
public class PaymentService {
    private final Map<String, PaymentIntent> idempotency = new ConcurrentHashMap<>();

    public Tender charge(String idempotencyKey, Check check, CardToken token, Money amount) {
        PaymentIntent intent = idempotency.computeIfAbsent(idempotencyKey,
            k -> new PaymentIntent(k, check.getId(), amount));
        synchronized (intent) {
            if (intent.getState() == PaymentState.CAPTURED) {
                return intent.getTenders().get(0);
            }
            intent.authorize(gateway -> gateway.authorize(token, amount));
            intent.capture(gateway -> gateway.capture(intent.getAuthRef()));
            return intent.getLastTender();
        }
    }
}
```

---

### Inventory Depletion (simplified)

```java
// InventoryService.java (excerpt)
public class InventoryService {
    public void onOrderFired(String locationId, OrderLine line, RecipeRepository recipes) {
        Recipe recipe = recipes.findByMenuItem(line.getMenuItemId()).orElse(null);
        if (recipe == null) return;
        for (RecipeLine rl : recipe.getLines()) {
            LocationStock stock = stockRepo.lock(locationId, rl.getIngredientId());
            Money qty = rl.getQuantity().multiply(line.getQuantity());
            if (!stock.tryDecrement(qty)) {
                throw new InsufficientStockException(rl.getIngredientId());
            }
        }
    }
}
```

---

### Loyalty Earn Hook

```java
// LoyaltyService.java (excerpt)
public class LoyaltyService {
    public void postEarn(CustomerId customerId, Check closedCheck, LoyaltyProgram rules) {
        if (!rules.isEligible(closedCheck)) return;
        Money eligible = closedCheck.getEligibleSubtotalAfterDiscounts();
        int points = rules.pointsForSpend(eligible);
        LoyaltyAccount acc = accounts.getOrCreate(customerId, rules.getProgramId());
        acc.post(new LoyaltyLedgerEntry(EARN, points, closedCheck.getId()));
    }
}
```

---

### Demo Scenario

```java
// RestaurantDemo.java
public class RestaurantDemo {
    public static void main(String[] args) {
        Location loc = new Location("loc-1", "Downtown", ZoneId.of("America/New_York"));
        Table t1 = new Table("t1", "z-main", 4);
        Party party = new Party(3);
        t1.seat();

        Order order = new Order("o1", loc.getId(), OrderType.DINE_IN);
        order.setTableIds(List.of(t1.getId()));
        order.addLine("burger-1", 2, List.of());
        order.addLine("salad-2", 1, List.of(new AppliedModifier("dressing", "ranch", Money.ZERO)));

        KitchenRouter router = new StationRoutingByItem(Map.of(
            "burger-1", "GRILL",
            "salad-2", "COLD"
        ));
        order.submit(router, ticket -> System.out.println("KDS push: " + ticket));

        Check check = Check.fromOrder(order);
        check.addServiceCharge("large_party", Money.of("6.00"));
        CheckSplitter splitter = new CheckSplitter();
        List<Check> checks = splitter.splitByEqualParts(check, 3, RoundingPolicy.BANKERS);
        System.out.println("Split checks: " + checks.size());
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `KitchenRouter`, tax calculation, tip suggestion engines, loyalty earn rules  
**Why:** Different locations need different routing graphs and regulatory rules without branching chaos  
**Interview Justification:** "We inject `KitchenRouter` per location so a sushi bar’s expedite rules don’t complicate a burger concept’s fry-only routing."

---

### Pattern 2: State Pattern (or explicit state machine)
**Where:** `OrderStatus`, `TableStatus`, `PaymentState`, reservation lifecycle  
**Why:** Illegal transitions (e.g., CAPTURED → AUTHORIZED) are rejected centrally  
**Interview Justification:** "Table can’t go AVAILABLE→OCCUPIED without host action; state object documents allowed transitions for QA."

---

### Pattern 3: Domain Events + Observer
**Where:** Order submitted → KDS; check closed → loyalty earn; inventory decrement  
**Why:** Decouple core transactional writes from side effects (websockets, analytics)  
**Interview Justification:** "POS latency stays bounded; consumers retry independently with idempotent handlers."

---

### Pattern 4: Repository + Unit of Work
**Where:** Persistence of `Order`, `Check`, `Reservation` aggregates  
**Why:** One business transaction spans DB rows; commit/rollback is atomic  
**Interview Justification:** "Split payment touches `Check`, `PaymentIntent`, and `Tender`—we commit once."

---

### Pattern 5: Saga / Process Manager (for distributed payments)
**Where:** Authorize → kitchen fire → capture on service completion; or refund on void  
**Why:** Long-running flows with compensating transactions across PSP and internal ledger  
**Interview Justification:** "If capture fails after fire, we need compensating kitchen recall + guest communication—choreography with timeouts."

---

### Pattern 6: Template Method / Pipeline
**Where:** Report generation (extract → transform → load)  
**Why:** Shared steps, pluggable filters per location  
**Interview Justification:** "Finance gets consistent CSV headers; ops swaps in BigQuery sink."

---

## 💡 Step 8: Interview Discussion Points

### 1. Concurrency: Same Check, Two Servers Paying

**Problem:** Two handhelds try to close the last $20 on a split check.  
**Answer:** Serialize on `checkId` (distributed lock or DB row `version` optimistic lock). Use **idempotency keys** on `PaymentIntent`. Show user **stale balance** if version conflict—refresh check totals. Prefer **single writer per check** micro-service partition.

---

### 2. Reservation vs Walk-In: Table State Machine

**Problem:** RESERVED blocks seating but party is late.  
**Answer:** Timer job transitions to NO_SHOW; release `Table` to AVAILABLE per policy. **Grace period** configurable. **Manager override** with audit. For **combined tables**, merge unit must reserve/release atomically.

---

### 3. KDS Ordering: FIFO vs Course

**Problem:** Drinks rush while appetizers lag.  
**Answer:** **Priority field** on `OrderLine` or station policy (BAR sorts by promised time). **Expo view** groups by `tableId`. **SLA colors** from historical prep distributions (moving average per item/station).

---

### 4. Split Bill Rounding

**Problem:** $100/3 = endless decimals.  
**Answer:** Fix a **rounding policy** at location: distribute remainder cents to first N shares or use banker’s rounding. Ensure **sum(shares) == original** invariant. Display **running balance** on each sub-check.

---

### 5. Inventory & 86ing

**Problem:** Stock hits zero mid-service.  
**Answer:** On `tryDecrement` failure, optionally **auto-86** menu item via real-time menu cache invalidation. For **over-sell tolerance**, manager flag allows negative stock with alert. **Recipe drift** (actual vs theoretical) handled in nightly variance report.

---

### 6. Multi-Location Data Isolation

**Problem:** Cross-tenant data leaks in queries.  
**Answer:** Every row carries `organizationId` + `locationId`; ORM **scoped repository** enforces predicate. **JWT claims** bind staff to allowed locations. **Row-level security** in warehouse for BI.

---

### 7. Offline POS (Brief)

**Problem:** Brief outage at store.  
**Answer:** Local queue of **idempotent commands**; sync on reconnect with **version vectors** per check. Conflicts (duplicate table seat) surface to manager. **Payments** may be store-and-forward only if processor supports—otherwise cash-only contingency.

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `KitchenRouter` routes only; `PaymentService` handles tenders only; `CheckSplitter` handles allocation math only

### Open/Closed ✅
```java
public class LoadBalancedKitchenRouter implements KitchenRouter { }
// Add without changing Order#submit
```

### Liskov Substitution ✅
- Any `KitchenRouter` implementation can replace another for tests vs production graphs

### Interface Segregation ✅
```java
interface ReadableMenu { MenuSnapshot load(); }
interface WritableMenu { void publish(MenuDraft d); }
// KDS device might only need ReadableMenu
```

### Dependency Inversion ✅
```java
public class OrderService {
    private final KitchenRouter router;
    private final PaymentGateway gateway;
    public OrderService(KitchenRouter router, PaymentGateway gateway) {
        this.router = router;
        this.gateway = gateway;
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Location-partitioned** operational data with template-driven menus
- ✅ **Table + reservation** workflows with explicit status machines and conflict rules
- ✅ **Order aggregate** drives KDS tickets via **pluggable routing**
- ✅ **Check** as financial boundary; **splits** as derived checks or allocations
- ✅ **Idempotent payments** and **optimistic locking** for concurrent tendering
- ✅ **Inventory** tied to recipes with configurable depletion timing
- ✅ **Loyalty** as post-settlement side effect with rule engine
- ✅ **Reporting** split between operational near-real-time and async aggregates

### Differentiators (Interview Shine)
- Toast/Square-class systems unify **POS + KDS + payments + limited ops**; calling out **idempotency**, **split checks**, **multi-location RBAC**, and **event-driven side effects** shows production maturity.

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
