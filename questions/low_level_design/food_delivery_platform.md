# Low-Level Design: Food Delivery Platform (Zomato / Swiggy)

**Difficulty:** Hard 🔥

**Interview Duration:** 75-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

**Scope note:** This is a **three-sided marketplace** (customers, restaurants, delivery partners), not an internal restaurant management system (tables, floor staff, in-venue inventory). Emphasis: **partner matching**, **real-time coordination**, and **multi-party order lifecycle**.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a food delivery platform that connects customers with restaurants and delivery partners: discovery, ordering, payments, live tracking, and settlement for restaurants and riders."*

### Clarifying Questions to Ask:

1. **Q:** Are we designing for one city/region or global multi-tenant?  
   **A:** Start with one region; design for horizontal scale (sharding by geo).

2. **Q:** Who owns menu truth and pricing—the restaurant POS or the platform?  
   **A:** Platform is source of truth for customer-facing menu; restaurants sync via dashboard/API; availability can be near real-time.

3. **Q:** Payment methods and refunds?  
   **A:** Cards, wallets, UPI/cash-equivalents, platform credit; partial refunds; **split payment** (e.g., two customers on one order).

4. **Q:** How is a delivery partner assigned?  
   **A:** **Proximity + availability + capacity**; batching optional; re-assignment on cancel/timeout.

5. **Q:** Real-time tracking expectations?  
   **A:** Customer sees rider location updates (e.g., every few seconds); restaurant sees KDS ticket states.

6. **Q:** Commission and partner earnings—when calculated?  
   **A:** Commission on order completion; partner earnings per delivery leg; surge and incentives layered on top.

7. **Q:** Rating scope?  
   **A:** Restaurant and delivery partner ratings tied to completed orders; disputes can void or defer publication.

8. **Q:** Promo rules complexity?  
   **A:** Codes, cart-level discounts, restaurant-funded vs platform-funded; stackability rules configurable.

9. **Q:** SLA for restaurant accept/reject?  
   **A:** Timeout auto-cancel or re-offer; customer notified.

10. **Q:** Out of scope for this LLD?  
    **A:** Deep logistics (own fleet vs 3PL), ML ranking v1, full fraud engine—mention as extensions.

---

## 🔹 Step 2: Gather Requirements (7-10 minutes)

### Functional Requirements

#### User & Identity (FR1-FR6)
1. Customers register/login (email, phone, OAuth); manage addresses and preferences  
2. Restaurants onboard (KYC, bank, serviceable area, timings)  
3. Delivery partners onboard (documents, vehicle type, background checks)  
4. Role-based access: customer, restaurant admin, kitchen staff, partner app, support agent  
5. Account states: ACTIVE, SUSPENDED, UNDER_REVIEW  
6. Session and device management for mobile clients  

#### Restaurant Onboarding & Management (FR7-FR11)
7. Restaurant profile: name, cuisines, photos, geo-fence for delivery  
8. Operating hours, holidays, temporary closures  
9. Accept/reject order with **acceptance timeout**  
10. Pause incoming orders (rush mode)  
11. Payout account and tax identifiers for settlement  

#### Menu Management (FR12-FR20)
12. Categories, items, descriptions, images  
13. Base price per item; **variants** (size, crust, spice level) with price deltas  
14. **Add-ons** (optional/mandatory groups, min/max selections)  
15. **Availability** per item (in stock, 86’d) and per variant  
16. Scheduled menu (breakfast vs dinner)  
17. Item tags: veg/non-veg, allergens, spicy level  
18. Price updates with effective time (optional versioning)  
19. Restaurant can mark item unavailable until next open  
20. Bulk import/export for large menus  

#### Search & Discovery (FR21-FR27)
21. List/filter restaurants by **cuisine**, **rating**, **distance**, **delivery time (ETA)**  
22. Sort: relevance, rating, cost for two, delivery time  
23. Full-text search on restaurant and dish names  
24. “Open now” and “delivery to address” filtering  
25. Sponsored/placement hooks (interface only)  
26. Favorite restaurants and reorder shortcuts  
27. Serviceability check from customer coordinates + address  

#### Cart & Checkout (FR28-FR33)
28. Cart per customer (and optionally per device/session)  
29. Add/remove lines; apply variants and add-ons  
30. Validate cart against current menu prices and availability  
31. Delivery instructions, contactless preference  
32. **Split payment** across multiple payers for one order  
33. Fees preview: item subtotal, taxes, delivery fee, surge, discounts  

#### Order Placement & Lifecycle (FR34-FR44)
34. Place order → create immutable **Order** with line items snapshot  
35. States: PLACED → PAYMENT_PENDING → **RESTAURANT_PENDING** → ACCEPTED → PREPARING → READY_FOR_PICKUP → PARTNER_ASSIGNED → PICKED_UP → EN_ROUTE → DELIVERED / CANCELLED / REFUNDED  
36. Payment capture timing: authorize on place, capture on accept (configurable)  
37. Order modification rules (only before accept; strict)  
38. Cancellation by customer/restaurant/platform with policy engine  
39. Partial fulfillment (item unavailable) with customer consent or auto-adjust  
40. Order versioning for audit (who changed what, when)  
41. Idempotent **place order** API (client order key)  
42. **Order history** with filters (date, restaurant, status)  
43. Reorder from history (rebuild cart from snapshot)  
44. Scheduled orders (fire at time T)  

#### Delivery Partner Assignment (FR45-FR52)
45. Eligible partners: ONLINE, in service area, correct vehicle class, not over concurrent cap  
46. **Proximity-based** scoring: haversine/road-network ETA, distance to restaurant then to customer  
47. Offer to partner (push); **accept timeout**; fallback to next candidate  
48. Reassignment on partner decline, timeout, or incident  
49. Partner can go OFFLINE; in-flight orders honored  
50. Stacked deliveries (optional): assign second pickup if compatible  
51. Fairness: rotate offers, penalize frequent declines  
52. Surge and peak boost visibility to partner before accept  

#### Real-Time Order Tracking (FR53-FR57)
53. Stream **partner GPS** updates to customer (throttled)  
54. Map ETA recomputation as location changes  
55. Restaurant sees order state transitions (KDS)  
56. Customer timeline: confirmed, preparing, picked up, nearby, delivered  
57. WebSocket or SSE; fallback polling  

#### Payments (FR58-FR65)
58. Multiple **payment methods** per account  
59. **Split payment**: partition amounts across payers; all must succeed or roll back  
60. Wallet + external instrument combination  
61. Refunds: full/partial; original instrument vs wallet credit  
62. Failed payment retry and expiry  
63. Invoice/receipt generation  
64. Tip handling (optional): add to partner earnings settlement  
65. PCI scope: tokenize cards; no raw PAN in core services  

#### Dynamic Pricing (FR66-FR71)
66. **Delivery fee** base + distance component + platform fee  
67. **Surge pricing** by zone and demand (multiplier caps)  
68. Restaurant/service fee and small order fee  
69. **Discounts**: promo, membership, first-order  
70. Price quote API: returns breakdown valid for TTL (e.g., 60s)  
71. Re-quote if cart or location changes beyond threshold  

#### Ratings & Reviews (FR72-FR76)
72. Post-delivery rating for **restaurant** (food, packaging) and **partner** (delivery)  
73. Optional text review; moderation queue for abuse  
74. Aggregate rating update (weighted, recency)  
75. Block rating if order disputed until resolution  
76. Report/flag inappropriate reviews  

#### Commission & Partner Earnings (FR77-FR81)
77. **Restaurant commission** % or flat per order; tier by contract  
78. Compute on **delivered** (or captured payment) basis  
79. **Partner earnings**: base fare + per-km + surge share + tips + incentives − penalties  
80. Settlement statements per period; adjustments for refunds/chargebacks  
81. Tax/GST line items per jurisdiction (simplified in model)  

#### Notifications (FR82-FR87)
82. **Order confirmation** (payment success)  
83. **Restaurant accepted** / rejected  
84. **Partner assigned**; partner app: new offer  
85. **Out for delivery** (picked up)  
86. **Delivered**; request rating  
87. Channels: push, SMS, email; user preferences  

#### Promo Codes & Offers (FR88-FR93)
88. Promo definition: percent/fixed off, max cap, min cart  
89. User/restaurant/platform funded split  
90. Per-user redemption limits; validity window  
91. Stackability rules (one platform + one restaurant, etc.)  
92. Auto-applied “offers” vs explicit codes  
93. Audit when promo applied at payment time  

#### Delivery Time Estimation (FR94-FR98)
94. ETA = restaurant **prep time** (historical + queue) + **partner travel** + buffer  
95. Live ETA updates on state changes and location ticks  
96. Display range (e.g., 25–35 min) to manage expectations  
97. ML hook: `EtaEstimator` strategy behind interface  
98. Bad weather / strike modifiers (config)  

#### Kitchen Display System (KDS) Integration (FR99-FR102)
99. Push accepted orders to restaurant **KDS** queue (ticket id, items, modifiers)  
100. KDS events: STARTED, BUMP (course done), READY  
101. Platform order state transitions driven by KDS or manual dashboard  
102. Offline tolerant: queue outbox; retry with idempotency  

#### Customer Support & Disputes (FR103-FR108)
103. Ticket linked to order; categories: wrong item, not delivered, payment  
104. Agent actions: refund, credit, re-dispatch, escalate  
105. **Dispute** freezes rating publication for involved parties  
106. Audit log for compliance  
107. Self-service: cancel within window, chatbot FAQ  
108. SLA timers per severity  

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "Orders per second, concurrent riders, geo distribution?"
- Peak: tens of thousands orders/hour in mega-city; millions of location updates/minute  
- Reads-heavy: search, menus; writes: orders, payments, location  

**Deduced NFRs:**
- ✅ Horizontal scale: stateless API tier; **geo-sharded** order and location services  
- ✅ CQRS: read models for search vs write path for orders  
- ✅ Caching: menu CDN, Redis for hot restaurants  
- ✅ Async pipelines: notifications, settlements, analytics  

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"
- Payment vs order state (no paid ghost orders)  
- Inventory of concurrent partner assignment (one primary assignee)  
- Cart vs menu price at checkout  

**Deduced NFRs:**
- ✅ **Strong consistency** for payment capture and order state transitions (per order aggregate)  
- ✅ **Serializable or optimistic locking** for partner offer (prevent double-assign)  
- ✅ **Saga / outbox** for cross-service place order (menu, pricing, payment, order)  
- ✅ **Eventual consistency** acceptable for search indexes and aggregate ratings  

---

#### 3. **Availability Analysis**

**Think:** "Dinner peak; payment provider down?"
- Degrade: show cached menu with stale banner; queue orders if payment deferred  
- Location service can tolerate brief gaps (interpolate)  

**Deduced NFRs:**
- ✅ 99.9%+ for core ordering path  
- ✅ **Graceful degradation:** read-only menu, retry payment  
- ✅ Multi-AZ; failover for primary DB  
- ✅ Idempotent APIs for partners and payments  

---

#### 4. **Maintainability Analysis**

**Think:** "Debug wrong ETA; change surge rules?"
- Trace id per order; structured logs  
- Feature flags for pricing and assignment strategies  

**Deduced NFRs:**
- ✅ **Policy/strategy plugins** for pricing, assignment, ETA  
- ✅ Observability: metrics (accept time, prep time, delivery time), distributed tracing  
- ✅ **Event sourcing** or append-only order event log for disputes  

---

#### 5. **Performance Analysis**

**Think:** "SLAs?"
- Search p95 < 300 ms; place order p95 < 2 s including payment RPC  
- Partner offer round-trip < 5 s; location ingest < 100 ms  

**Deduced NFRs:**
- ✅ Geo indexes (S2/H3) for restaurant and rider lookup  
- ✅ In-memory candidate pool for riders near restaurant  
- ✅ Rate limit public APIs; backoff on partner devices  

---

#### 6. **Security Analysis**

**Think:** "Fraud, account takeover, PII?"
- OAuth tokens; scoped restaurant/partner credentials  
- PII encryption at rest; least-privilege for support  

**Deduced NFRs:**
- ✅ Authentication/authorization (RBAC + optional ABAC for support)  
- ✅ Rate limiting, bot detection hooks  
- ✅ Audit all refund and account changes  
- ✅ No sensitive payment data in order service (tokens only)  

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Customer places order" | Customer, Order, Cart |
| "Restaurant accepts" | Restaurant, Acceptance, Timeout |
| "Menu variants and add-ons" | MenuItem, Variant, ModifierGroup, AddOn |
| "Search by cuisine and distance" | Cuisine, GeoLocation, Filter, Sort |
| "Assign delivery partner" | DeliveryPartner, Assignment, Offer |
| "Real-time tracking" | LocationUpdate, Trip, Route |
| "Split payment" | Payment, PaymentSplit, Payer |
| "Surge and delivery fee" | PriceQuote, SurgeZone, FeeBreakdown |
| "Rating restaurant and partner" | Rating, Review, Order |
| "Commission and earnings" | CommissionRule, Payout, EarningsStatement |
| "Promo code" | PromoCampaign, PromoRedemption |
| "KDS ticket" | KdsTicket, KitchenStation |
| "Support ticket" | SupportTicket, Dispute |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Cuisine | ❌ NO | Tag/enum on restaurant or filter dimension |
| Filter | ❌ NO | Query parameter / value object |
| Timeout | ❌ NO | Policy parameter |
| Route | ⚠️ Value object | Polyline + ETA; may be embedded in Trip |
| FeeBreakdown | ✅ YES (VO) | Immutable snapshot on order |
| PaymentSplit | ✅ YES | Multiple payers for one order |

### Final Entity List

**Actors & Accounts:**
1. **User** (base)
2. **Customer** extends User
3. **Restaurant** (business entity)
4. **RestaurantUser** (staff; links User + Restaurant + role)
5. **DeliveryPartner** extends User
6. **SupportAgent** extends User

**Location & Geo:**
7. **Address** (formatted + lat/lng)
8. **ServiceArea** (polygon or geo-hash set per restaurant)
9. **GeoLocation** (value object: lat, lng, accuracy, timestamp)

**Menu:**
10. **Menu**
11. **MenuCategory**
12. **MenuItem**
13. **ItemVariant**
14. **ModifierGroup** (add-on group)
15. **ModifierOption**
16. **MenuAvailability** (schedule, stock flag)

**Commerce:**
17. **Cart**
18. **CartLine** (item snapshot refs + qty + selected modifiers)
19. **PriceQuote** (TTL, breakdown)
20. **PromoCampaign**
21. **PromoRedemption**

**Order & Fulfillment:**
22. **Order**
23. **OrderLine** (price snapshot)
24. **OrderState** (enum / state machine)
25. **OrderEvent** (audit timeline)
26. **DeliveryAssignment** (offer lifecycle)
27. **Trip** (partner + order + path metadata)
28. **LocationUpdate** (streamed points)

**Payment:**
29. **PaymentIntent**
30. **PaymentSplit**
31. **PaymentInstrument** (token ref)
32. **Refund**

**Trust & Money:**
33. **Rating**
34. **Review**
35. **CommissionRule**
36. **PartnerEarningsLedger** (entry)
37. **RestaurantPayoutBatch**

**Ops:**
38. **KdsTicket**
39. **Notification** (outbox)
40. **SupportTicket**
41. **Dispute**

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Customer ↔ Cart ↔ Order

**Conclusion:** **Composition** for cart owned by customer session; **aggregation** for order created from cart.
```
Customer ◆────→ Cart [0..1]
Cart ◆────→ CartLine [1..*]
Customer ─────→ Order [0..*]
Order ◆────→ OrderLine [1..*]
```

### Pass 2: Restaurant ↔ Menu ↔ Order

**Conclusion:** **Composition** menu under restaurant; **association** order references restaurant.
```
Restaurant ◆────→ Menu [1..*]
Menu ◆────→ MenuCategory [1..*]
MenuCategory ◆────→ MenuItem [1..*]
MenuItem ◆────→ ItemVariant [0..*]
MenuItem ─────→ ModifierGroup [0..*]
Order ─────→ Restaurant [1]
Order ─────→ Customer [1]
```

### Pass 3: Delivery Partner ↔ Assignment ↔ Trip

**Conclusion:** **Association** assignment links partner to order; trip aggregates location stream.
```
DeliveryPartner ─────→ DeliveryAssignment [0..*]
Order ─────→ DeliveryAssignment [0..1]
DeliveryAssignment ─────→ Trip [0..1]
Trip ◆────→ LocationUpdate [0..*]  (stream / external store)
```

### Pass 4: Payment & Promo

```
Order ─────→ PaymentIntent [1..*]
PaymentIntent ◆────→ PaymentSplit [0..*]
PaymentSplit ─────→ Customer [1]  (payer)
Order ─────→ PromoRedemption [0..*]
PromoRedemption ─────→ PromoCampaign [1]
```

### Pass 5: Support & KDS

```
Order ─────→ KdsTicket [0..1]
Order ─────→ SupportTicket [0..*]
SupportTicket ─────→ Dispute [0..1]
```

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Customer → Cart | 1:1 (active) | Composition |
| Cart → CartLine | 1:N | Composition |
| Customer → Order | 1:N | Association |
| Restaurant → Menu | 1:N | Composition |
| Order → OrderLine | 1:N | Composition |
| Order → DeliveryAssignment | 1:0..1 | Association |
| DeliveryPartner → DeliveryAssignment | 1:N | Association |
| Order → PaymentIntent | 1:N | Association |
| PaymentIntent → PaymentSplit | 1:N | Composition |
| Order → PromoRedemption | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (12-18 minutes)

### Class Diagram 1: Core Enums

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>     │  │ <<enumeration>>     │  │ <<enumeration>>     │
│   OrderState        │  │  PartnerStatus      │  │  AssignmentOfferState│
├─────────────────────┤  ├─────────────────────┤  ├─────────────────────┤
│ PLACED              │  │ OFFLINE             │  │ PENDING             │
│ PAYMENT_PENDING     │  │ ONLINE              │  │ ACCEPTED            │
│ RESTAURANT_PENDING  │  │ BUSY                │  │ DECLINED            │
│ ACCEPTED            │  │ BREAK               │  │ EXPIRED             │
│ PREPARING           │  └─────────────────────┘  └─────────────────────┘
│ READY_FOR_PICKUP    │
│ PARTNER_ASSIGNED    │  ┌─────────────────────┐  ┌─────────────────────┐
│ PICKED_UP           │  │ <<enumeration>>     │  │ <<enumeration>>     │
│ EN_ROUTE            │  │  PaymentStatus      │  │  UserRole           │
│ DELIVERED           │  ├─────────────────────┤  ├─────────────────────┤
│ CANCELLED           │  │ INITIATED           │  │ CUSTOMER            │
│ REFUNDED            │  │ AUTHORIZED          │  │ RESTAURANT_ADMIN    │
└─────────────────────┘  │ CAPTURED            │  │ KITCHEN_STAFF       │
                         │ FAILED              │  │ DELIVERY_PARTNER    │
                         │ REFUNDED            │  │ SUPPORT_AGENT       │
                         └─────────────────────┘  └─────────────────────┘
```

---

### Class Diagram 2: Order Aggregate (Simplified)

```
┌────────────────────────────────────────────────────────────────────────┐
│                              Order                                     │
├────────────────────────────────────────────────────────────────────────┤
│ - orderId: String                                                      │
│ - idempotencyKey: String                                               │
│ - customerId: String                                                   │
│ - restaurantId: String                                                 │
│ - deliveryAddress: Address                                             │
│ - state: OrderState                                                    │
│ - placedAt: Instant                                                    │
│ - promisedEtaRange: DurationRange                                        │
│ - feeBreakdown: FeeBreakdown                                           │
│ - lines: List<OrderLine>                         ◆──────────────────┐   │
│ - version: long (optimistic lock)                                  │   │
├────────────────────────────────────────────────────────────────────────┤
│ + place(...): void                                                   │   │
│ + transitionTo(newState, actor): void                                │   │
│ + canCancel(actor): boolean                                          │   │
│ + snapshotForHistory(): OrderSnapshot                                │   │
└────────────────────────────────────────────────────────────────────────┘   │
         │                                                                   │
         │ 1                                                                 │
         │                                                                    │
         ▼                                                                    │
┌────────────────────────────────────────────────────────────────────────┐  │
│                            OrderLine                                   │  │
├────────────────────────────────────────────────────────────────────────┤  │
│ - menuItemId: String                                                   │  │
│ - nameSnapshot: String                                                 │  │
│ - variantId: String                                                    │  │
│ - unitPrice: Money                                                     │  │
│ - quantity: int                                                        │  │
│ - modifiers: List<SelectedModifier>                                  │  │
└────────────────────────────────────────────────────────────────────────┘  │
                                                                              │
┌────────────────────────────────────────────────────────────────────────┐  │
│                        DeliveryAssignment                              │  │
├────────────────────────────────────────────────────────────────────────┤  │
│ - assignmentId: String                                                 │  │
│ - partnerId: String                                                    │  │
│ - offerState: AssignmentOfferState                                     │  │
│ - offeredAt: Instant                                                   │  │
│ - expiresAt: Instant                                                   │  │
│ - score: double  (ranking)                                             │  │
├────────────────────────────────────────────────────────────────────────┤  │
│ + accept(): void  + decline(): void  + expire(): void                  │  │
└────────────────────────────────────────────────────────────────────────┘  │
```

---

### Class Diagram 3: Cart & Price Quote

```
┌──────────────────────────────┐       ┌──────────────────────────────┐
│           Cart               │       │        PriceQuote            │
├──────────────────────────────┤       ├──────────────────────────────┤
│ - cartId: String             │       │ - quoteId: String            │
│ - customerId: String         │       │ - validUntil: Instant        │
│ - restaurantId: String       │       │ - subtotal: Money            │
│ - lines: List<CartLine>      │       │ - deliveryFee: Money         │
├──────────────────────────────┤       │ - surgeMultiplier: BigDecimal│
│ + addLine(...): void         │       │ - tax: Money                 │
│ + validateAgainst(menu):     │       │ - discount: Money            │
│   ValidationResult           │       │ - total: Money               │
│ + toOrderDraft(): OrderDraft │       │ - fundingSplit: PromoFunding │
└──────────────────────────────┘       └──────────────────────────────┘
```

---

### Class Diagram 4: Partner Matching (Strategy)

```
┌────────────────────────────────────────────────────────────────────────┐
│                    PartnerAssignmentService                            │
├────────────────────────────────────────────────────────────────────────┤
│ - candidateFinder: PartnerCandidateFinder                              │
│ - rankingStrategy: PartnerRankingStrategy                              │
│ - offerPolicy: OfferPolicy                                             │
├────────────────────────────────────────────────────────────────────────┤
│ + assignForOrder(order: Order): DeliveryAssignment                    │
│ + reassign(order: Order, reason: String): DeliveryAssignment          │
└────────────────────────────────────────────────────────────────────────┘
           │ uses                          │ uses
           ▼                               ▼
┌────────────────────────────┐   ┌────────────────────────────────────┐
│ <<interface>>              │   │ <<interface>>                      │
│ PartnerCandidateFinder     │   │ PartnerRankingStrategy             │
├────────────────────────────┤   ├────────────────────────────────────┤
│ + findNear(restaurantLoc,  │   │ + score(partner, orderCtx): double │
│   radius): List<Partner>   │   └────────────────────────────────────┘
└────────────────────────────┘                    △
           △                                      │
           │                          ┌───────────┴────────────┐
           │                          │                        │
┌──────────────────────────┐  ┌───────────────┐      ┌──────────────────┐
│ GeoHashCandidateFinder   │  │ ProximityScore│      │ FairnessWeighted │
│ (S2/H3 index)            │  │ Strategy      │      │ Strategy         │
└──────────────────────────┘  └───────────────┘      └──────────────────┘
```

---

### Class Diagram 5: Payments & Split

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PaymentIntent                                   │
├────────────────────────────────────────────────────────────────────────┤
│ - intentId: String                                                     │
│ - orderId: String                                                      │
│ - status: PaymentStatus                                                │
│ - splits: List<PaymentSplit>                     ◆──────────────────┐  │
├────────────────────────────────────────────────────────────────────────┤
│ + authorize(): void                                                    │
│ + capture(): void                                                      │
│ + failIfAnySplitFails(): void  // saga coordination                    │
└────────────────────────────────────────────────────────────────────────┘  │
                                                                             │
┌────────────────────────────────────────────────────────────────────────┐  │
│                        PaymentSplit                                    │  │
├────────────────────────────────────────────────────────────────────────┤  │
│ - payerCustomerId: String                                              │  │
│ - amount: Money                                                        │
│ - instrumentId: String                                                 │
│ - status: PaymentStatus                                                │
└────────────────────────────────────────────────────────────────────────┘  │
```

---

### Class Diagram 6: Notifications & KDS

```
┌──────────────────────────────┐       ┌──────────────────────────────┐
│     OrderEventPublisher      │       │         KdsTicket            │
├──────────────────────────────┤       ├──────────────────────────────┤
│ + publish(OrderEvent)        │       │ - ticketId: String           │
└──────────────────────────────┘       │ - orderId: String            │
           │                             │ - station: String            │
           ▼                             │ - kdsState: KdsState         │
┌──────────────────────────────┐       ├──────────────────────────────┤
│     NotificationService      │       │ + bump(): void               │
├──────────────────────────────┤       └──────────────────────────────┘
│ + notifyCustomer(template)   │
│ + notifyRestaurant(template) │
│ + notifyPartner(template)    │
└──────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-35 minutes)

### Enums

```java
// OrderState.java
public enum OrderState {
    PLACED,
    PAYMENT_PENDING,
    RESTAURANT_PENDING,
    ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    PARTNER_ASSIGNED,
    PICKED_UP,
    EN_ROUTE,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
```

```java
// AssignmentOfferState.java
public enum AssignmentOfferState {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED
}
```

---

### Order State Transitions (Guarded)

```java
// Order.java (core methods — illustrative)
public class Order {
    private final String orderId;
    private OrderState state;
    private final List<OrderLine> lines;
    private long version;

    public void transitionTo(OrderState next, OrderActor actor) {
        if (!OrderStateMachine.isAllowed(state, next, actor)) {
            throw new IllegalStateException("Invalid transition " + state + " -> " + next);
        }
        this.state = next;
        this.version++;
    }

    public boolean canCancel(OrderActor actor) {
        return OrderCancellationPolicy.allows(state, actor);
    }
}
```

```java
// OrderStateMachine.java
public final class OrderStateMachine {
    private static final Map<OrderState, Set<OrderState>> EDGES = Map.of(
        OrderState.PAYMENT_PENDING, Set.of(OrderState.RESTAURANT_PENDING, OrderState.CANCELLED),
        OrderState.RESTAURANT_PENDING, Set.of(OrderState.ACCEPTED, OrderState.CANCELLED),
        OrderState.ACCEPTED, Set.of(OrderState.PREPARING, OrderState.CANCELLED),
        OrderState.PREPARING, Set.of(OrderState.READY_FOR_PICKUP, OrderState.CANCELLED),
        OrderState.READY_FOR_PICKUP, Set.of(OrderState.PARTNER_ASSIGNED, OrderState.CANCELLED),
        OrderState.PARTNER_ASSIGNED, Set.of(OrderState.PICKED_UP, OrderState.CANCELLED),
        OrderState.PICKED_UP, Set.of(OrderState.EN_ROUTE),
        OrderState.EN_ROUTE, Set.of(OrderState.DELIVERED, OrderState.CANCELLED)
    );

    public static boolean isAllowed(OrderState from, OrderState to, OrderActor actor) {
        return EDGES.getOrDefault(from, Set.of()).contains(to);
    }
}
```

---

### Delivery Partner Matching

```java
// PartnerRankingStrategy.java
public interface PartnerRankingStrategy {
    double score(DeliveryPartner partner, OrderAssignmentContext ctx);
}

// ProximityAndLoadStrategy.java
public class ProximityAndLoadStrategy implements PartnerRankingStrategy {
    @Override
    public double score(DeliveryPartner partner, OrderAssignmentContext ctx) {
        double distToRest = ctx.restaurantLocation().distanceMeters(partner.getLocation());
        double distRestToCust = ctx.restaurantLocation().distanceMeters(ctx.customerLocation());
        double etaHint = distToRest / partner.getAvgSpeedMps();

        double loadPenalty = partner.getActiveDeliveries() * 120.0;
        double ratingBoost = partner.getRating() * 50.0;

        return -(etaHint + loadPenalty) + ratingBoost; // higher is better
    }
}
```

```java
// PartnerAssignmentService.java
public class PartnerAssignmentService {
    private final PartnerCandidateFinder candidateFinder;
    private final PartnerRankingStrategy rankingStrategy;
    private final Duration offerTtl;

    public DeliveryAssignment offerPartner(Order order) {
        List<DeliveryPartner> candidates = candidateFinder.findNear(
            order.getRestaurantLocation(), order.getVehicleClass(), 3000 /* meters */
        );

        return candidates.stream()
            .sorted((a, b) -> Double.compare(
                rankingStrategy.score(b, OrderAssignmentContext.from(order)),
                rankingStrategy.score(a, OrderAssignmentContext.from(order))
            ))
            .findFirst()
            .map(p -> new DeliveryAssignment(order.getOrderId(), p.getId(),
                Instant.now().plus(offerTtl)))
            .orElseThrow(() -> new NoPartnerAvailableException(order.getOrderId()));
    }
}
```

---

### Real-Time Location Ingest (Throttled)

```java
// LocationIngestService.java
public class LocationIngestService {
    private final LocationEventBus eventBus;
    private final Map<String, Long> lastPushMs = new ConcurrentHashMap<>();

    public void ingest(String partnerId, GeoLocation loc, String activeOrderId) {
        long now = System.currentTimeMillis();
        long last = lastPushMs.getOrDefault(partnerId, 0L);
        if (now - last < 3000) { // throttle 1 update / 3s per partner
            return;
        }
        lastPushMs.put(partnerId, now);
        eventBus.publish(new PartnerLocationUpdated(partnerId, activeOrderId, loc, now));
    }
}
```

---

### Split Payment Orchestration

```java
// PaymentIntent.java
public class PaymentIntent {
    private final String intentId;
    private final String orderId;
    private final List<PaymentSplit> splits;
    private PaymentStatus status;

    public void authorizeAll(PaymentGateway gateway) {
        List<String> authIds = new ArrayList<>();
        try {
            for (PaymentSplit s : splits) {
                String auth = gateway.authorize(s.getInstrumentId(), s.getAmount());
                authIds.add(auth);
                s.markAuthorized(auth);
            }
            this.status = PaymentStatus.AUTHORIZED;
        } catch (Exception ex) {
            for (int i = 0; i < authIds.size(); i++) {
                gateway.voidAuthorization(authIds.get(i));
            }
            this.status = PaymentStatus.FAILED;
            throw new PaymentException("Split authorize failed; all voided", ex);
        }
    }

    public void captureAll(PaymentGateway gateway) {
        for (PaymentSplit s : splits) {
            gateway.capture(s.getLastAuthId(), s.getAmount());
        }
        this.status = PaymentStatus.CAPTURED;
    }
}
```

---

### Dynamic Pricing Quote

```java
// PricingService.java
public class PricingService {
    private final SurgePricingStrategy surgeStrategy;
    private final DeliveryFeeCalculator deliveryFeeCalculator;

    public PriceQuote quote(Cart cart, Address deliveryAddress, Instant now) {
        Money subtotal = cart.subtotal();
        Money deliveryFee = deliveryFeeCalculator.compute(cart.getRestaurantId(), deliveryAddress);
        BigDecimal surge = surgeStrategy.multiplierFor(deliveryAddress.getGeoHash(), now);
        Money surgedDelivery = deliveryFee.multiply(surge);
        Money tax = taxService.estimate(subtotal, deliveryAddress);
        Money discount = promoEngine.apply(cart, deliveryAddress);
        Money total = subtotal.add(surgedDelivery).add(tax).subtract(discount);
        return new PriceQuote(subtotal, surgedDelivery, surge, tax, discount, total,
            now.plusSeconds(60));
    }
}
```

---

### Commission & Partner Earnings (Ledger Entries)

```java
// EarningsEntry.java
public class EarningsEntry {
    public enum Type { BASE_FARE, PER_KM, SURGE_BONUS, TIP, PENALTY, PLATFORM_FEE }

    private final String partnerId;
    private final String orderId;
    private final Type type;
    private final Money amount;
    private final Instant occurredAt;
}

// CommissionService.java
public class CommissionService {
    public Money restaurantCommission(Order order, CommissionRule rule) {
        Money foodTotal = order.foodSubtotal();
        return rule.isPercent()
            ? foodTotal.multiply(rule.getRate())
            : rule.getFlatPerOrder();
    }
}
```

---

### ETA Estimation Interface

```java
// EtaEstimator.java
public interface EtaEstimator {
    DurationRange estimate(Order order, Instant now);
}

// HeuristicEtaEstimator.java
public class HeuristicEtaEstimator implements EtaEstimator {
    @Override
    public DurationRange estimate(Order order, Instant now) {
        Duration prep = prepTimeService.p90(order.getRestaurantId());
        Duration partnerLeg = tripTimeService.estimate(order.getRestaurantLocation(),
            order.getDeliveryAddress().getLocation());
        Duration buffer = Duration.ofMinutes(3);
        Duration low = prep.plus(partnerLeg);
        Duration high = low.plus(buffer);
        return new DurationRange(low, high);
    }
}
```

---

### KDS Outbound Event (Idempotent)

```java
// KdsIntegrationService.java
public class KdsIntegrationService {
    private final MessageQueue queue;

    public void onOrderAccepted(Order order) {
        KdsTicketMessage msg = new KdsTicketMessage(
            order.getOrderId(), order.getRestaurantId(), order.getLines(), UUID.randomUUID().toString()
        );
        queue.send("kds.tickets", msg); // consumer idempotent on orderId + version
    }
}
```

---

### Demo Flow (Pseudo-Orchestration)

```java
// PlaceOrderFlow.java (high level)
public class PlaceOrderFlow {
    public Order place(PlaceOrderCommand cmd, PaymentGateway payments) {
        cartRepository.lock(cmd.getCartId());
        MenuSnapshot menu = menuService.snapshot(cmd.getRestaurantId());
        cartValidator.validate(cmd.getCartId(), menu);

        PriceQuote quote = pricingService.quote(cart, cmd.getAddress(), Instant.now());
        Order order = orderFactory.createFromCart(cmd, quote);
        order.transitionTo(OrderState.PAYMENT_PENDING, OrderActor.SYSTEM);

        PaymentIntent pi = paymentFactory.createSplits(order, cmd.getPayers());
        pi.authorizeAll(payments);

        order.transitionTo(OrderState.RESTAURANT_PENDING, OrderActor.SYSTEM);
        orderRepository.save(order);
        notificationService.notifyRestaurantNewOrder(order);
        return order;
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: State Pattern / State Machine
**Where:** `Order` lifecycle  
**Why:** Enforce legal transitions; separate concerns per state for complex rules  
**Interview Justification:** "Restaurant cannot mark DELIVERED from ACCEPTED. Central machine avoids scattered if-else."

---

### Pattern 2: Strategy Pattern
**Where:** `PartnerRankingStrategy`, `SurgePricingStrategy`, `EtaEstimator`  
**Why:** Swap algorithms (fairness vs pure proximity; ML ETA later) without changing orchestration  
**Interview Justification:** "A/B test assignment strategies per city."

---

### Pattern 3: Saga / Process Manager
**Where:** Place order (menu lock → authorize → create order → notify); capture on accept  
**Why:** Cross-service consistency with compensating actions (void auth)  
**Interview Justification:** "Split payment must all authorize or none."

---

### Pattern 4: Observer / Domain Events
**Where:** `OrderEvent` → `NotificationService`, analytics, KDS  
**Why:** Decouple side effects from core aggregate  
**Interview Justification:** "Same event drives push, SMS, and warehouse-style consumers."

---

### Pattern 5: Outbox Pattern
**Where:** Notifications, KDS, search index updates  
**Why:** At-least-once delivery without losing events if broker is down  
**Interview Justification:** "Transactional outbox beside order row."

---

### Pattern 6: Repository Pattern
**Where:** `OrderRepository`, `CartRepository`  
**Why:** Testability and persistence abstraction  

---

## 💡 Step 8: Interview Discussion Points

### 1. Delivery Partner Matching: Proximity vs Fairness

**Interviewer:** "How do you assign riders without starving edge cases?"

**Answer:**
"**Phase 1 — Candidate generation:** Use **geo index** (S2/H3) around restaurant; filter ONLINE, vehicle class, max concurrent deliveries, not on blocked order.

**Phase 2 — Ranking score:**
```
score = w1 * (-etaToRestaurant) + w2 * rating + w3 * acceptanceRate
        - w4 * activeLoad - w5 * recentDeclinePenalty
```
Pick top K; send offer to #1 with TTL.

**Phase 3 — Fallback:** On EXPIRED/DECLINE, offer #2; if pool empty, escalate (extend radius, surge bonus, or delay promise).

**Fairness:** Rotate offers across pool over time; decay decline penalty. Optional **batching**: second order only if detour < X minutes.

**Concurrency:** Use **compare-and-set** on assignment row: only one `PENDING` offer active per order; partner accept is conditional on still valid."

---

### 2. Real-Time Tracking: Scale and Consistency

**Interviewer:** "Millions of location updates—how?"

**Answer:**
"- **Ingest path:** Edge API writes to **Kafka** topic `partner.locations`; **throttle** per partner (e.g., 1 update / 3s unless turn detected).  
- **Read path:** Customer subscribes to **WebSocket** room `order:{id}`; server pushes **derived** ETA + polyline snippets from **stream processor** (Flink/ksql) or periodic recomputation.  
- **Storage:** Hot path in Redis (last location); cold archive for disputes.  
- **Consistency:** Eventual for map UI; order state remains strongly consistent in DB.  
- **Privacy:** Coarse location after delivery ends; TTL on location history."

---

### 3. Split Payment: Failure Modes

**Interviewer:** "Two friends pay; one card fails?"

**Answer:**
"**All-or-nothing authorize:**  
1. Create `PaymentIntent` with N `PaymentSplit` rows.  
2. Authorize splits **sequentially or parallel**; on any failure, **void** all successful authorizations (idempotent void).  
3. Optionally hold cart with short lock during attempt.  
4. On restaurant accept, **capture** all splits; partial capture only if business allows partial fulfillment (rare).  
**Idempotency:** Client `Idempotency-Key` on place order to prevent double charges on retry."

---

### 4. Dynamic Pricing: Surge and Restaurant Trust

**Interviewer:** "Customers hate surge—how do you justify it?"

**Answer:**
"- Transparent **breakdown** in UI: base fee, distance, surge multiplier, discounts.  
- **Caps** per city regulation or product policy.  
- Surge funds can partially flow to **partner** to improve supply.  
- **Re-quote** if TTL expired or address moves > threshold.  
- Audit log for dispute: 'quoteId X shown at T'."

---

### 5. Multi-Party Coordination: Who Owns Timeout?

**Interviewer:** "Restaurant slow to accept—what happens?"

**Answer:**
"- **SCHEDULER** emits timeout event for `RESTAURANT_PENDING`.  
- Policy: auto-cancel + notify customer + void/capture reversal per payment rules; **or** re-route to backup restaurant (advanced).  
- **Saga** ensures payment state matches order state (compensating transaction).  
- Metrics feed **ETA estimator** (restaurants with low accept rate get longer quoted prep)."

---

### 6. KDS Integration and Eventual Consistency

**Interviewer:** "Kitchen says READY but platform shows PREPARING?"

**Answer:**
"- **KdsTicket** events are source of truth for kitchen side; platform applies **idempotent** state transitions (`orderId`, `eventVersion`).  
- Conflict resolution: **last writer wins** with audit; support can override.  
- **Outbox** from restaurant edge device if offline; sync when reconnected."

---

### 7. Disputes and Ratings

**Interviewer:** "Customer claims not delivered—rating?"

**Answer:**
"- Open **SupportTicket** + optional **Dispute**; **block** publication of rating for that order until resolved.  
- Resolution outcomes: refund, partial credit, ban bad actor.  
- **Ledger** reversals adjust partner earnings and restaurant commission."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `PartnerAssignmentService`: matching only  
- `PricingService`: quotes only  
- `Order`: invariant + transitions; not notification content  

### Open/Closed ✅
```java
public class MlEtaEstimator implements EtaEstimator { }
// Add without modifying HeuristicEtaEstimator callers
```

### Liskov Substitution ✅
- Any `PartnerRankingStrategy` usable wherever strategy is injected  

### Interface Segregation ✅
```java
interface ReadableMenu { MenuSnapshot load(String restaurantId); }
interface WritableMenu { void applyChange(MenuChange c); }
// Kitchen app may only get WritableMenu for its restaurant
```

### Dependency Inversion ✅
```java
public class PartnerAssignmentService {
    private final PartnerRankingStrategy rankingStrategy;
    public PartnerAssignmentService(PartnerRankingStrategy rankingStrategy) {
        this.rankingStrategy = rankingStrategy;
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Three-sided marketplace** with distinct lifecycles for customer, restaurant, and partner  
- ✅ **Order aggregate** with strict state machine and optimistic concurrency  
- ✅ **Geo-indexed** partner discovery + **strategy-based** ranking + timed offers  
- ✅ **Split payment** with atomic authorize + compensating void  
- ✅ **Throttled** location pipeline; **event-driven** notifications and KDS  
- ✅ **Pricing quote** with TTL; surge, fees, and promos as pluggable policies  
- ✅ **Commission + earnings** as ledger entries post-delivery  
- ✅ **Support/disputes** linked to orders; ratings gated by resolution  

### Multi-Party Coordination
- ✅ Timeouts on restaurant accept and partner offer; explicit **reassignment** path  
- ✅ **Saga** for place order; **outbox** for reliable side effects  

### Real-Time
- ✅ WebSocket rooms per order; derived ETA from stream processing + state changes  

---

**Food Delivery Platform LLD** — Hard difficulty, full interview-style coverage.

Ready for review!
