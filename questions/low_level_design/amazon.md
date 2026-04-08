# Low-Level Design: Amazon E-commerce Platform

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:

*"Design a low-level object model and core services for a large e-commerce platform like Amazon: catalog, inventory across warehouses, third-party sellers, cart and checkout, orders, payments, pricing and promotions, search, recommendations, reviews, shipping, returns, addresses, notifications, Prime, and time-limited deals."*

### Clarifying Questions to Ask:

1. **Q:** Are we modeling first-party (1P) retail, third-party marketplace (3P), or both?  
   **A:** Both: Amazon-fulfilled and seller-fulfilled listings; unified customer experience.

2. **Q:** How do we handle inventory consistency during high concurrency (flash sales, Lightning Deals)?  
   **A:** Strong consistency for stock decrements at checkout; optimistic locking or reservation pattern; idempotent order placement.

3. **Q:** What is the scope of "search" — keyword only or also browse, filters, and ranking?  
   **A:** Full-text search plus faceted filters (category, price, rating, brand) and business ranking (relevance, sponsored, deals).

4. **Q:** How deep should recommendations go in LLD?  
   **A:** Pluggable `RecommendationStrategy` (collaborative, content-based, session); offline feature store + online scoring; not full ML training.

5. **Q:** Single currency / region or multi-marketplace?  
   **A:** Design for multiple fulfillment nodes (FCs) per region; prices and tax can vary by address.

6. **Q:** Payment failure and partial fulfillment?  
   **A:** Saga or orchestrated steps: authorize → reserve inventory → capture; compensate on failure; split shipments allowed.

7. **Q:** Returns — instant refund vs receive-then-refund?  
   **A:** Configurable return type; state machine; refund via original or gift card; restock rules per SKU/seller.

8. **Q:** Prime — what to model?  
   **A:** Membership tier, benefits (shipping speed, deals access), eligibility checks at cart and checkout.

9. **Q:** Notifications — sync or async?  
   **A:** Event-driven; multiple channels (email, push, SMS); templates per order lifecycle event.

10. **Q:** Scale assumptions for the interview?  
    **A:** Millions of SKUs, thousands of QPS on search/cart, inventory sharded by warehouse/seller; catalog read-heavy, inventory write-heavy at checkout.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Product Catalog (FR1–FR10)

1. System should support product listings with title, description, media, and brand.
2. System should organize products in a hierarchical category tree (many-to-many where needed).
3. System should support structured attributes (e.g., size, color) and attribute groups per category.
4. System should support variations (parent ASIN / product group with child SKUs).
5. System should distinguish offer context: sold by Amazon vs third-party seller, and fulfillment type (FBA, FBM).
6. System should allow multiple sellers to list the same product catalog item (competing offers).
7. System should expose browse paths (category → subcategory → listing).
8. System should support catalog versioning or staged updates for seller edits (optional moderation).
9. System should link listings to compliance and restricted-category flags where applicable.
10. System should support deal badges (Deal of the Day, Lightning Deal) at listing level.

#### Inventory & Fulfillment (FR11–FR22)

11. System should track stock quantity per SKU per warehouse / fulfillment center (FC).
12. System should support seller-owned inventory nodes (seller warehouse) for FBM.
13. System should support allocation strategy: nearest FC, split shipment, or backorder rules.
14. System should reserve inventory during checkout with TTL (soft reservation).
15. System should decrement committed stock atomically on successful payment capture.
16. System should release reservations on timeout or payment failure.
17. System should support inbound shipments and stock adjustments (damaged, lost) with audit trail.
18. System should prevent overselling under concurrent add-to-cart and checkout.
19. System should expose available-to-promise (ATP) to cart and product detail page (eventually consistent OK for display).
20. System should support low-stock thresholds and alerts for sellers and ops.
21. System should record inventory movement types (sale, return restock, transfer between FCs).
22. System should support multi-item carts with per-line fulfillment plan (which FC ships which line).

#### Cart & Checkout (FR23–FR28)

23. Users should add/update/remove line items with selected variation and quantity.
24. Cart should validate price snapshot vs current catalog price before order placement.
25. Cart should apply coupons, promotions, and Prime shipping benefits where eligible.
26. Cart should support saved-for-later and move between wishlist and cart.
27. Checkout should collect or select delivery address, payment method, and shipping speed.
28. Guest checkout vs registered user: persist cart by session or account.

#### Order Management (FR29–FR36)

29. Users should place orders idempotently (client order key).
30. System should maintain order lifecycle: CREATED → PAYMENT_PENDING → PAID → FULFILLING → SHIPPED → DELIVERED (with failure/cancel branches).
31. System should support partial shipment and multiple packages per order.
32. System should support order cancellation rules by state and SLA.
33. System should generate invoices and order summaries.
34. System should link each order line to seller, SKU, price paid, and tax line items.
35. System should support order modification only where business rules allow (e.g., address before ship).
36. System should record timestamps for each state transition for tracking UI.

#### Payment Processing (FR37–FR42)

37. System should support multiple payment methods: card, wallet, net banking, gift card, COD (region-specific).
38. System should authorize then capture (two-phase) for card payments.
39. System should handle partial captures and multiple captures per order when split by shipment (optional).
40. System should vault or tokenize sensitive payment data; never store raw PAN.
41. System should integrate payment gateway behind `PaymentProvider` abstraction.
42. System should record payment attempts, failures, and reconciliation IDs.

#### Pricing & Promotions (FR43–FR50)

43. System should maintain base list price per offer (seller + SKU + channel).
44. System should apply lightning deal price windows with start/end and per-customer limits.
45. System should apply coupons (percentage, fixed, category-scoped) with stacking rules.
46. System should apply cart-level and item-level discounts with priority order.
47. System should compute tax based on address, product tax code, and jurisdiction rules (simplified in LLD).
48. System should support "Deal of the Day" curated SKUs with scheduled pricing.
49. System should expose effective price to PDP, cart, and checkout through `PricingEngine`.
50. Prime-exclusive deals should require active Prime membership at purchase time.

#### Seller Management (FR51–FR56)

51. System should onboard sellers with profile, policies, and performance metrics.
52. System should let sellers create/update offers (price, condition, handling time) on catalog SKUs.
53. System should aggregate seller ratings from delivered orders and buyer feedback.
54. System should enforce seller eligibility for deals and Buy Box logic (simplified scoring).
55. System should support seller dashboards for orders, returns, and inventory feeds.
56. System should suspend listings or sellers for policy violations (account state).

#### Search & Filtering (FR57–FR63)

57. Users should search by keyword with typo tolerance and synonyms (delegated to search index).
58. Users should filter by category, price range, brand, average rating, Prime eligibility, and deal flags.
59. System should sort by relevance, price, rating, and newest.
60. System should support facet counts for filter UI.
61. System should blend organic results with sponsored placements (placement slots).
62. Search index should be updated on catalog/inventory/price changes (near real-time pipeline).
63. System should debounce and batch index updates where possible for efficiency.

#### Reviews & Ratings (FR64–FR68)

64. Verified purchasers should post reviews and star ratings per SKU or product group.
65. System should aggregate rating histogram and average rating for search facets and PDP.
66. System should support helpful votes, review images, and moderation states.
67. System should prevent duplicate reviews per user per product (verified purchase window).
68. One-tap star rating without full review optional.

#### Recommendations (FR69–FR72)

69. PDP and home should show "Customers who bought X also bought" and similar item rails.
70. System should support session-based and user-history-based recommendation strategies.
71. Recommendations should respect inventory availability and regional catalog.
72. System should allow A/B assignment of strategy per user segment (feature flag).

#### Wishlist & Addresses (FR73–FR76)

73. Users should maintain multiple named wishlists or default wishlist.
74. Users should save multiple delivery addresses with default flags.
75. Address validation (format + suggest) via external provider behind interface.
76. Users should set default payment instrument and default address.

#### Shipping, Tracking, Returns (FR77–FR83)

77. System should create shipments with carrier, service level, tracking number.
78. Users should see estimated delivery date based on FC, carrier, and Prime tier.
79. System should emit tracking events (picked, packed, shipped, out for delivery, delivered).
80. Users should initiate returns with reason codes and return method (drop-off, pickup).
81. System should manage return lifecycle: REQUESTED → APPROVED → IN_TRANSIT → RECEIVED → REFUNDED.
82. Refunds should route through payment reversal or store credit per policy.
83. Restocking fees and non-returnable SKUs configurable per category/SKU.

#### Notifications (FR84–FR87)

84. System should notify on order placed, payment failure, shipped, out for delivery, delivered, and return updates.
85. Notifications should support channel preferences per user.
86. Templates should be localized and parameterized (order id, tracking URL).
87. Notification dispatch should be asynchronous and retryable.

#### Prime & Deals (FR88–FR92)

88. System should model Prime membership with start/end and benefit entitlements.
89. Prime should affect shipping options, pricing on select deals, and early access windows.
90. Lightning Deals should enforce quantity limits per customer and countdown inventory.
91. Deal of the Day should be a scheduled campaign linking to a set of SKUs and marketing slots.
92. System should prevent deal stacking where business rules disallow it.

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "Catalog size, QPS, geographic distribution?"

- Hundreds of millions of listings; billions of inventory rows across FCs and sellers.
- Search and PDP are extremely read-heavy; checkout spikes during events.
- Sharding: inventory by `(warehouseId, skuId)` or `(sellerId, skuId)`; orders by `customerId` or `orderId`.

**Deduced NFRs:**

- ✅ Horizontal scaling of stateless services (catalog read, search API, cart, pricing).
- ✅ CQRS-style separation: command path for orders/inventory; query path for search and PDP.
- ✅ Caching for catalog and pricing with TTL and invalidation on change events.
- ✅ Async workers for indexing, notifications, and recommendation feature updates.

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact vs eventually OK?"

- **Strong:** inventory reservation and payment capture; no negative stock for committed orders.
- **Eventual:** search ranking, PDP "only N left", recommendation scores.
- **Per-order:** order and payment states must be consistent with audit trail.

**Deduced NFRs:**

- ✅ **Strong consistency** for inventory commits using DB transactions or compare-and-swap with version.
- ✅ **Idempotent** `PlaceOrder` using client-supplied idempotency key.
- ✅ **Saga / orchestration** across payment, inventory, and order creation with compensating actions.
- ✅ **Outbox pattern** for reliable domain events to search index and notifications.

---

#### 3. **Availability Analysis**

**Think:** "What can degrade?"

- Browse and search should remain available if checkout is overloaded (with stale prices clearly labeled).
- Payment provider outage: queue retries, surface user messaging.

**Deduced NFRs:**

- ✅ **High availability** for read paths (multi-AZ, replicated search index).
- ✅ **Graceful degradation:** disable non-critical recommendations; serve cached catalog.
- ✅ **Circuit breakers** on payment and external address validation.
- ✅ **99.9%+** target for core shopping flows; stricter for payment reconciliation jobs.

---

#### 4. **Maintainability Analysis**

**Think:** "Evolving pricing rules, new seller types, new payment methods?"

**Deduced NFRs:**

- ✅ **Pluggable** `PricingRule`, `PromotionApplicator`, `PaymentProvider`, `RecommendationStrategy`.
- ✅ **Feature flags** for deal types and recommendation algorithms.
- ✅ **Structured logging** and trace IDs across checkout saga.
- ✅ **Configurable** fulfillment and return policies per marketplace region.

---

#### 5. **Performance Analysis**

**Think:** "Latency budgets?"

- Search p95 < 200–300 ms for typical queries (with CDN and index locality).
- PDP with pricing + inventory hint < 150 ms from edge cache where possible.
- Checkout: reservation + payment authorize p95 < 1–2 s excluding user interaction.

**Deduced NFRs:**

- ✅ **Inverted index** and facet engine for search; avoid N+1 DB for PDP (batch or document store).
- ✅ **Read-through cache** for hot SKUs; **write-behind** for non-critical aggregates.
- ✅ **Async** post-order work: emails, analytics, recommendation feedback.

---

#### 6. **Security Analysis**

**Think:** "PII, payments, fraud?"

**Deduced NFRs:**

- ✅ **Tokenization** for payments; PCI scope minimization.
- ✅ **AuthN/AuthZ** for customer data, seller portals, and admin APIs.
- ✅ **Rate limiting** on login, checkout, and coupon application.
- ✅ **Audit** on seller payout, refund, and inventory adjustments.
- ✅ **Input validation** on reviews and listing content to reduce abuse.

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Product listings, categories, attributes" | Product, Category, Attribute, AttributeValue, Listing |
| "Variations, parent/child SKU" | ProductGroup, Sku, VariationAxis |
| "Warehouse, FC, stock" | Warehouse, InventoryLot, StockReservation |
| "Shopping cart" | Cart, CartLineItem |
| "Place order, lifecycle" | Order, OrderLineItem, OrderState |
| "Payment methods" | Payment, PaymentMethod, PaymentIntent |
| "Coupons, deals, Prime" | Coupon, Promotion, LightningDeal, DealOfTheDayCampaign, PrimeMembership |
| "Third-party sellers" | Seller, Offer, SellerRating |
| "Search, filters" | SearchQuery, SearchIndex, Facet |
| "Reviews" | Review, RatingAggregate |
| "Recommendations" | RecommendationRequest, RecommendationResult |
| "Wishlist, address" | Wishlist, WishlistItem, Address |
| "Shipment, tracking" | Shipment, TrackingEvent, Carrier |
| "Return, refund" | ReturnRequest, Refund, RestockLine |
| "Notifications" | Notification, NotificationTemplate |
| "Buy box / winning offer" | BuyBoxPolicy (service), OfferScore |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Product | ✅ YES | Core catalog aggregate root (conceptual product) |
| Sku | ✅ YES | Sellable/stockable unit |
| Category | ✅ YES | Tree node, many-to-many to products |
| Listing | ✅ YES | Customer-facing view combining product + primary offer context |
| Warehouse | ✅ YES | Physical or logical stock location |
| StockReservation | ✅ YES | TTL-based hold for checkout |
| Cart | ✅ YES | Session or user scoped |
| Order | ✅ YES | Aggregate with lines and state machine |
| PaymentIntent | ✅ YES | Tracks authorize/capture lifecycle |
| Seller | ✅ YES | Marketplace participant |
| Offer | ✅ YES | Price + fulfillment promise for a SKU by a seller |
| SearchIndex | ❌ NO | External infrastructure; modeled as gateway interface |
| Facet | ❌ NO | Value object on search response |
| BuyBoxPolicy | ✅ YES | Domain service / policy object |
| PrimeMembership | ✅ YES | Subscription with benefits |
| LightningDeal | ✅ YES | Time-bounded promotion entity |

### Final Entity List

**Catalog:**

1. **Product** – Merchandising identity, brand, description, tax code.
2. **ProductGroup** – Parent for variations; shared reviews often at this level.
3. **Sku** – Concrete variant (size/color); ties to inventory and offers.
4. **Category** – Hierarchical classification.
5. **AttributeDefinition** / **AttributeValue** – Structured specs.
6. **Listing** – PDP-facing aggregate (product + display offers + badges).

**Inventory & Fulfillment:**

7. **Warehouse** – FC or seller node; address, cut-off times.
8. **InventoryBalance** – `(warehouseId, skuId) → quantityAvailable, quantityReserved`.
9. **StockReservation** – quantity, expiry, cartId/orderId reference.
10. **InventoryAdjustment** – audit record for corrections and inbound.
11. **FulfillmentPlan** – per order line: source warehouse, ship window.

**Commerce:**

12. **Cart**, **CartLineItem** – selected SKU, quantity, saved price snapshot optional.
13. **Order**, **OrderLineItem** – immutable commercial record after submit.
14. **OrderState** – enum / state machine.
15. **Shipment**, **TrackingEvent**, **Carrier**.

**Pricing & Promotions:**

16. **Offer** – seller + SKU + list price + condition + fulfillment type.
17. **Coupon**, **CouponRedemption**
18. **Promotion** – base type; **LightningDeal**, **DealOfTheDayCampaign** as subtypes or tagged promotions.
19. **PriceQuote** – value object from `PricingEngine`.

**Payments:**

20. **PaymentMethod** (token reference), **PaymentIntent**, **PaymentTransaction**.

**Sellers & Trust:**

21. **Seller**, **SellerAccountState**, **SellerRatingAggregate**.

**Search & Discovery (boundary):**

22. **SearchService** (interface) – query in, ranked IDs + facets out.

**Reviews & Recs:**

23. **Review**, **RatingSnapshot** (cached aggregate).
24. **RecommendationEngine** (interface), **UserProductInteraction** (event).

**Customer:**

25. **Customer**, **Address**, **Wishlist**, **WishlistItem**.
26. **PrimeMembership**, **PrimeBenefit** (value object / flags).

**Returns & Notifications:**

27. **ReturnRequest**, **ReturnLine**, **Refund**.
28. **Notification**, **NotificationPreference**, **DomainEvent** (integration).

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Catalog & Offers

#### ProductGroup ↔ Sku

**Conclusion:** **Composition** (group owns variant SKUs logically)

```
ProductGroup ◆────→ Sku [1..*]
```

#### Product ↔ Category

**Conclusion:** **Many-to-many**

```
Product ←────→ Category [0..*] : [0..*]
```

#### Sku ↔ Offer

**Conclusion:** **One SKU, many offers** (1P + 3P)

```
Sku ←──── Offer [0..*]
Offer ────→ Seller [1]
```

#### Listing ↔ Product / Offer

**Conclusion:** **Aggregation** (listing presents one product and resolved winning offers)

```
Listing ────→ ProductGroup [1]
Listing ────→ Offer [1..*] (visible offers; Buy Box = one primary)
```

---

### Pass 2: Inventory & Orders

#### Warehouse ↔ InventoryBalance

**Conclusion:** **Composition**

```
Warehouse ◆────→ InventoryBalance [0..*]
InventoryBalance ────→ Sku [1]
```

#### Cart ↔ StockReservation

**Conclusion:** **Association** (reservation optionally keyed by cart)

```
Cart ────→ StockReservation [0..*]
StockReservation ────→ Warehouse [1]
StockReservation ────→ Sku [1]
```

#### Order ↔ OrderLineItem ↔ Shipment

**Conclusion:** **Composition** for lines; **aggregation** for shipments (split over time)

```
Order ◆────→ OrderLineItem [1..*]
Order ────→ Shipment [0..*]
Shipment ◆────→ TrackingEvent [0..*]
```

---

### Pass 3: Customer, Prime, Reviews

#### Customer ↔ Address / Wishlist / PrimeMembership

**Conclusion:** **Composition** for owned collections

```
Customer ◆────→ Address [0..*]
Customer ◆────→ Wishlist [0..*]
Customer ────→ PrimeMembership [0..1]
```

#### Sku / ProductGroup ↔ Review

**Conclusion:** **Association**

```
Review ────→ Customer [1]
Review ────→ ProductGroup or Sku [1]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| ProductGroup → Sku | 1:N | Composition |
| Product ↔ Category | M:N | Association |
| Sku → Offer | 1:N | Association |
| Seller → Offer | 1:N | Composition |
| Warehouse → InventoryBalance | 1:N | Composition |
| Customer → Cart | 1:1 (active) | Association |
| Cart → CartLineItem | 1:N | Composition |
| Order → OrderLineItem | 1:N | Composition |
| Order → Shipment | 1:N | Aggregation |
| Customer → Address | 1:N | Composition |
| Sku → StockReservation | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Core Enums

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>     │  │ <<enumeration>>     │  │ <<enumeration>>     │
│ FulfillmentType     │  │ OrderState          │  │ PaymentStatus       │
├─────────────────────┤  ├─────────────────────┤  ├─────────────────────┤
│ FBA                 │  │ CREATED             │  │ INITIATED           │
│ FBM                 │  │ PAYMENT_PENDING     │  │ AUTHORIZED          │
│ AMAZON_RETAIL       │  │ PAID                │  │ CAPTURED            │
└─────────────────────┘  │ FULFILLING          │  │ FAILED              │
                         │ PARTIALLY_SHIPPED   │  │ REFUNDED            │
                         │ SHIPPED             │  └─────────────────────┘
                         │ DELIVERED           │
                         │ CANCELLED           │
                         └─────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>     │  │ <<enumeration>>     │
│ ReservationStatus   │  │ ReturnState         │
├─────────────────────┤  ├─────────────────────┤
│ ACTIVE              │  │ REQUESTED           │
│ CONSUMED            │  │ APPROVED            │
│ EXPIRED             │  │ IN_TRANSIT          │
│ RELEASED            │  │ RECEIVED            │
                         │ REFUNDED            │
                         │ REJECTED            │
                         └─────────────────────┘
```

---

### Class Diagram 2: Catalog, SKU, Offer, Listing

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ProductGroup                                       │
├────────────────────────────────────────────────────────────────────────┤
│ - productGroupId: String                                                │
│ - title: String                                                         │
│ - brandId: String                                                       │
│ - categoryIds: List<String>                                             │
│ - description: String                                                   │
├────────────────────────────────────────────────────────────────────────┤
│ + getSkus(): List<Sku>                                                  │
└────────────────────────────────────────────────────────────────────────┘
         ◆───────────────────────────────────────
         │ 1
         ▼ *
┌────────────────────────────────────────────────────────────────────────┐
│                            Sku                                            │
├────────────────────────────────────────────────────────────────────────┤
│ - skuId: String                                                         │
│ - variationAttributes: Map<String, String>  // color, size              │
│ - productGroupId: String                                                │
│ - isPrimeEligible: boolean                                              │
├────────────────────────────────────────────────────────────────────────┤
│ + getOffers(): List<Offer>   // via service                             │
└────────────────────────────────────────────────────────────────────────┘
         △
         │ *
┌────────────────────────────────────────────────────────────────────────┐
│                            Offer                                          │
├────────────────────────────────────────────────────────────────────────┤
│ - offerId: String                                                       │
│ - skuId: String                                                         │
│ - sellerId: String                                                      │
│ - listPrice: Money                                                      │
│ - condition: Condition                                                  │
│ - fulfillmentType: FulfillmentType                                      │
│ - handlingTimeDays: int                                                 │
├────────────────────────────────────────────────────────────────────────┤
│ + isBuyBoxCandidate(policy: BuyBoxPolicy): boolean                    │
└────────────────────────────────────────────────────────────────────────┘
         │
         │ *
         ▼ 1
┌────────────────────────────────────────────────────────────────────────┐
│                            Seller                                         │
├────────────────────────────────────────────────────────────────────────┤
│ - sellerId: String                                                      │
│ - displayName: String                                                   │
│ - accountState: SellerAccountState                                      │
│ - performanceScore: BigDecimal                                          │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                          Listing                                          │
├────────────────────────────────────────────────────────────────────────┤
│ - listingId: String                                                     │
│ - productGroupId: String                                                │
│ - primarySkuId: String                                                  │
│ - buyBoxOfferId: String                                                 │
│ - dealBadges: Set<DealBadge>                                            │
├────────────────────────────────────────────────────────────────────────┤
│ + resolveBuyBox(offers: List<Offer>, policy: BuyBoxPolicy): Offer       │
└────────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Inventory (Emphasis)

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Warehouse                                        │
├────────────────────────────────────────────────────────────────────────┤
│ - warehouseId: String                                                   │
│ - regionCode: String                                                    │
│ - type: WarehouseType   // FC, SELLER_NODE                            │
│ - sellerId: Optional<String>  // for FBM seller warehouse               │
├────────────────────────────────────────────────────────────────────────┤
│ + getCutoffForPrimeSameDay(): LocalTime                                 │
└────────────────────────────────────────────────────────────────────────┘
         ◆ 1
         │ *
         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      InventoryBalance                                     │
├────────────────────────────────────────────────────────────────────────┤
│ - warehouseId: String                                                   │
│ - skuId: String                                                         │
│ - quantityOnHand: int                                                   │
│ - quantityReserved: int                                                 │
│ - version: long   // optimistic locking                                 │
├────────────────────────────────────────────────────────────────────────┤
│ + availableToPromise(): int   // onHand - reserved                      │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                      StockReservation                                     │
├────────────────────────────────────────────────────────────────────────┤
│ - reservationId: String                                                 │
│ - skuId: String                                                         │
│ - warehouseId: String                                                   │
│ - quantity: int                                                         │
│ - cartId: String                                                        │
│ - expiresAt: Instant                                                    │
│ - status: ReservationStatus                                             │
├────────────────────────────────────────────────────────────────────────┤
│ + consume(orderId: String): void                                        │
│ + release(): void                                                       │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                    InventoryService                                       │
│                    <<interface>>                                         │
├────────────────────────────────────────────────────────────────────────┤
│ + reserve(request: ReservationRequest): ReservationResult               │
│ + commit(reservationIds: List<String>, orderId: String): void           │
│ + release(reservationIds: List<String>): void                          │
│ + adjust(adjustment: InventoryAdjustment): void                         │
│ + getAtp(skuId: String, region: String): int                          │
└────────────────────────────────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┴────────────┐
        │                        │
┌───────────────────┐   ┌───────────────────┐
│ DbInventoryService│   │CachedInventorySvc│
└───────────────────┘   └───────────────────┘
```

---

### Class Diagram 4: Cart, Order, Payment

```
┌────────────────────────────────────────────────────────────────────────┐
│                            Cart                                           │
├────────────────────────────────────────────────────────────────────────┤
│ - cartId: String                                                        │
│ - customerId: Optional<String>                                          │
│ - sessionId: Optional<String>                                           │
│ - lineItems: List<CartLineItem>                                         │
├────────────────────────────────────────────────────────────────────────┤
│ + addLine(skuId, offerId, qty): void                                    │
│ + applyCoupon(code: String): void                                       │
│ + toPriceQuote(engine: PricingEngine): PriceQuote                       │
└────────────────────────────────────────────────────────────────────────┘
         ◆ 1
         │ *
         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        CartLineItem                                       │
├────────────────────────────────────────────────────────────────────────┤
│ - skuId: String                                                         │
│ - offerId: String                                                       │
│ - quantity: int                                                         │
│ - snapshotPrice: Optional<Money>                                        │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                            Order                                          │
├────────────────────────────────────────────────────────────────────────┤
│ - orderId: String                                                       │
│ - customerId: String                                                    │
│ - state: OrderState                                                     │
│ - shippingAddressId: String                                             │
│ - lineItems: List<OrderLineItem>                                        │
│ - paymentIntentId: String                                               │
│ - idempotencyKey: String                                                │
├────────────────────────────────────────────────────────────────────────┤
│ + transition(to: OrderState): void                                      │
└────────────────────────────────────────────────────────────────────────┘
         ◆ 1
         │ *
         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        OrderLineItem                                      │
├────────────────────────────────────────────────────────────────────────┤
│ - skuId, offerId, sellerId: String                                      │
│ - quantity: int                                                         │
│ - unitPrice: Money                                                      │
│ - tax: Money                                                            │
│ - fulfillmentPlan: FulfillmentPlan                                      │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                       PaymentIntent                                       │
├────────────────────────────────────────────────────────────────────────┤
│ - paymentIntentId: String                                               │
│ - orderId: String                                                       │
│ - amount: Money                                                         │
│ - status: PaymentStatus                                                 │
│ - paymentMethodToken: String                                            │
├────────────────────────────────────────────────────────────────────────┤
│ + authorize(): void                                                   │
│ + capture(): void                                                       │
└────────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Search & Recommendation (Emphasis)

```
┌────────────────────────────────────────────────────────────────────────┐
│                      SearchService                                        │
│                      <<interface>>                                       │
├────────────────────────────────────────────────────────────────────────┤
│ + search(query: SearchQuery): SearchResult                              │
└────────────────────────────────────────────────────────────────────────┘
         △
         │
┌────────────────────────────────────────────────────────────────────────┐
│                   ElasticsearchSearchService                              │
├────────────────────────────────────────────────────────────────────────┤
│ - client: SearchClient                                                  │
│ + search(query: SearchQuery): SearchResult                              │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                      SearchQuery                                          │
├────────────────────────────────────────────────────────────────────────┤
│ - keywords: String                                                      │
│ - categoryId: Optional<String>                                          │
│ - priceMin, priceMax: Optional<Money>                                   │
│ - minRating: Optional<BigDecimal>                                       │
│ - brandId: Optional<String>                                             │
│ - primeOnly: boolean                                                    │
│ - dealTypes: Set<DealBadge>                                             │
│ - sort: SortOption                                                      │
│ - page, pageSize: int                                                   │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│                 RecommendationEngine                                      │
│                 <<interface>>                                            │
├────────────────────────────────────────────────────────────────────────┤
│ + recommend(req: RecommendationRequest): List<RecommendationItem>       │
└────────────────────────────────────────────────────────────────────────┘
         △
         │
    ┌────┴────┬──────────────────┐
    ▼         ▼                  ▼
┌─────────┐ ┌──────────────┐ ┌──────────────────┐
│CoPurchase│ │ContentSimilar│ │SessionBasedRecall│
│Strategy │ │Strategy      │ │Strategy          │
└─────────┘ └──────────────┘ └──────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Value Object: Money

```java
// Money.java
public final class Money {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) throw new IllegalArgumentException("Currency mismatch");
        return new Money(amount.add(other.amount), currency);
    }

    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
```

---

### Inventory: Reservation with Optimistic Locking

```java
// ReservationRequest.java
public class ReservationRequest {
    private final String cartId;
    private final String skuId;
    private final String warehouseId;
    private final int quantity;
    private final Duration ttl;

    // constructor, getters
}

// ReservationResult.java
public class ReservationResult {
    private final boolean success;
    private final String reservationId;
    private final String failureReason;

    public static ReservationResult ok(String reservationId) { /* ... */ }
    public static ReservationResult fail(String reason) { /* ... */ }
}
```

```java
// DbInventoryService.java (simplified core)
public class DbInventoryService implements InventoryService {

    @Override
    @Transactional
    public ReservationResult reserve(ReservationRequest req) {
        InventoryBalance row = balanceRepo.findForUpdate(req.getWarehouseId(), req.getSkuId());
        if (row == null) {
            return ReservationResult.fail("NO_STOCK_NODE");
        }

        int atp = row.getQuantityOnHand() - row.getQuantityReserved();
        if (atp < req.getQuantity()) {
            return ReservationResult.fail("INSUFFICIENT_STOCK");
        }

        row.setQuantityReserved(row.getQuantityReserved() + req.getQuantity());
        boolean saved = balanceRepo.updateWithVersion(row); // WHERE version = ?
        if (!saved) {
            throw new ConcurrentModificationException("retry reserve");
        }

        StockReservation res = StockReservation.newActive(
            req.getCartId(), req.getSkuId(), req.getWarehouseId(), req.getQuantity(), req.getTtl());
        reservationRepo.save(res);
        return ReservationResult.ok(res.getReservationId());
    }

    @Override
    @Transactional
    public void commit(List<String> reservationIds, String orderId) {
        for (String id : reservationIds) {
            StockReservation r = reservationRepo.findById(id);
            r.consume(orderId);
            reservationRepo.save(r);

            InventoryBalance b = balanceRepo.findForUpdate(r.getWarehouseId(), r.getSkuId());
            b.setQuantityReserved(b.getQuantityReserved() - r.getQuantity());
            b.setQuantityOnHand(b.getQuantityOnHand() - r.getQuantity());
            balanceRepo.updateWithVersion(b);
        }
    }

    @Override
    @Transactional
    public void release(List<String> reservationIds) {
        for (String id : reservationIds) {
            StockReservation r = reservationRepo.findById(id);
            if (r.getStatus() != ReservationStatus.ACTIVE) continue;
            r.release();
            reservationRepo.save(r);

            InventoryBalance b = balanceRepo.findForUpdate(r.getWarehouseId(), r.getSkuId());
            b.setQuantityReserved(b.getQuantityReserved() - r.getQuantity());
            balanceRepo.updateWithVersion(b);
        }
    }
}
```

---

### Pricing Engine: Deals, Coupons, Prime

```java
// PricingEngine.java
public class PricingEngine {
    private final List<PricingRule> rules; // ordered: lightning > dotd > coupon > base

    public PriceQuote quote(Cart cart, CustomerContext ctx) {
        Money subtotal = Money.zero(ctx.getCurrency());
        List<LinePrice> lines = new ArrayList<>();

        for (CartLineItem line : cart.getLineItems()) {
            Offer offer = offerRepo.findById(line.getOfferId());
            Money base = offer.getListPrice().multiply(BigDecimal.valueOf(line.getQuantity()));

            PricingContext pctx = new PricingContext(cart, line, offer, ctx);
            for (PricingRule rule : rules) {
                base = rule.apply(base, pctx);
            }
            lines.add(new LinePrice(line.getSkuId(), base));
            subtotal = subtotal.add(base);
        }

        return new PriceQuote(lines, subtotal, estimateTax(cart, ctx));
    }
}

// Example rule: Prime-exclusive deal window
public class PrimeExclusiveDealRule implements PricingRule {
    @Override
    public Money apply(Money current, PricingContext ctx) {
        if (!ctx.getCustomer().hasActivePrime()) return current;
        return lightningDealRepo.findActive(ctx.getLine().getSkuId())
            .map(d -> d.getDealPrice().multiply(BigDecimal.valueOf(ctx.getLine().getQuantity())))
            .orElse(current);
    }
}
```

---

### Checkout Orchestration (Idempotent)

```java
// CheckoutService.java
public class CheckoutService {
    private final InventoryService inventory;
    private final PaymentService payments;
    private final OrderRepository orders;
    private final IdempotencyStore idempotency;

    public Order placeOrder(PlaceOrderCommand cmd) {
        Optional<Order> existing = idempotency.getOrder(cmd.getIdempotencyKey());
        if (existing.isPresent()) return existing.get();

        Cart cart = cartRepo.get(cmd.getCartId());
        FulfillmentPlan plan = fulfillmentPlanner.plan(cart, cmd.getShippingAddressId());
        List<String> reservationIds = inventory.reserveAll(plan, cart.getCartId());

        PriceQuote quote = pricingEngine.quote(cart, cmd.getCustomerContext());
        PaymentIntent pi = payments.createIntent(quote.getTotal(), cmd.getPaymentMethodToken());

        try {
            String orderId = OrderIdGenerator.next();
            payments.authorize(pi.getPaymentIntentId());
            inventory.commit(reservationIds, orderId);
            payments.capture(pi.getPaymentIntentId());

            Order order = OrderFactory.fromCart(orderId, cart, quote, plan, pi, OrderState.PAID);
            orders.save(order);
            idempotency.record(cmd.getIdempotencyKey(), order.getOrderId());
            eventBus.publish(new OrderPlacedEvent(order.getOrderId()));
            return order;
        } catch (Exception ex) {
            inventory.release(reservationIds);
            payments.cancel(pi.getPaymentIntentId());
            throw ex;
        }
    }
}
```

---

### Buy Box Selection (Seller Platform)

```java
// BuyBoxPolicy.java — simplified scoring
public class BuyBoxPolicy {
    public Optional<Offer> selectWinner(List<Offer> offers, CustomerContext ctx) {
        return offers.stream()
            .filter(Offer::isEligible)
            .min(Comparator
                .comparing((Offer o) -> o.getListPrice().getAmount())
                .thenComparing(o -> o.getSeller().getPerformanceScore(), Comparator.reverseOrder())
                .thenComparing(o -> o.getHandlingTimeDays())
                .thenComparing(Offer::getOfferId));
    }
}
```

---

### Search Query Build (Filters)

```java
// SearchQueryFactory.java
public class SearchQueryFactory {
    public SearchQuery fromHttp(Map<String, String> params) {
        return SearchQuery.builder()
            .keywords(params.getOrDefault("q", ""))
            .categoryId(Optional.ofNullable(params.get("category")))
            .priceMin(parseMoney(params.get("priceMin")))
            .priceMax(parseMoney(params.get("priceMax")))
            .minRating(parseBigDecimal(params.get("minRating")))
            .brandId(Optional.ofNullable(params.get("brand")))
            .primeOnly(Boolean.parseBoolean(params.getOrDefault("prime", "false")))
            .dealTypes(parseDeals(params.get("deals")))
            .sort(SortOption.from(params.getOrDefault("sort", "RELEVANCE")))
            .page(parseInt(params.getOrDefault("page", "0"), 0))
            .pageSize(Math.min(parseInt(params.getOrDefault("size", "24"), 24), 48))
            .build();
    }
}
```

---

### Recommendation Facade

```java
// RecommendationFacade.java
public class RecommendationFacade {
    private final Map<String, RecommendationEngine> strategies;

    public List<RecommendationItem> recommend(String strategyKey, RecommendationRequest req) {
        RecommendationEngine engine = strategies.getOrDefault(strategyKey, strategies.get("COPURCHASE"));
        List<RecommendationItem> raw = engine.recommend(req);
        return inventoryFilter.filterInStock(raw, req.getRegionCode());
    }
}
```

---

### Order State Machine (Fragment)

```java
// Order.java
public void transition(OrderState to) {
    switch (state) {
        case CREATED:
            if (to == OrderState.PAYMENT_PENDING || to == OrderState.CANCELLED) { state = to; return; }
            break;
        case PAYMENT_PENDING:
            if (to == OrderState.PAID || to == OrderState.CANCELLED) { state = to; return; }
            break;
        case PAID:
            if (to == OrderState.FULFILLING || to == OrderState.CANCELLED) { state = to; return; }
            break;
        case FULFILLING:
            if (to == OrderState.PARTIALLY_SHIPPED || to == OrderState.SHIPPED) { state = to; return; }
            break;
        // ...
        default:
            break;
    }
    throw new IllegalStateException("Invalid transition " + state + " -> " + to);
}
```

---

### Notification on Domain Event

```java
// OrderPlacedEventHandler.java
public class OrderPlacedEventHandler {
    private final NotificationDispatcher dispatcher;

    @Subscribe
    public void on(OrderPlacedEvent event) {
        Order o = orderRepo.findById(event.getOrderId());
        Notification n = Notification.template("ORDER_PLACED")
            .to(o.getCustomerId())
            .data(Map.of("orderId", o.getOrderId(), "total", o.getTotal().toString()));
        dispatcher.enqueue(n);
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern

**Where:** `PricingRule`, `RecommendationEngine`, `PaymentProvider`, `FulfillmentAllocator`  
**Why:** Swap pricing stacks, recs algorithms, gateways, and FC selection without changing orchestration.  
**Interview Justification:** "Business rules change frequently (deals, Prime); strategies keep `CheckoutService` stable."

---

### Pattern 2: Saga / Process Manager

**Where:** Checkout: reserve inventory → authorize → commit stock → capture payment  
**Why:** Long-running business transaction across aggregates with compensations.  
**Interview Justification:** "Each step can fail independently; we need explicit rollback (release stock, void auth)."

---

### Pattern 3: Repository + Domain Service

**Where:** `OfferRepository`, `BuyBoxPolicy`, `InventoryService`  
**Why:** Persistence decoupled; complex selection logic not crammed into entities.  
**Interview Justification:** "Buy Box uses multiple offers and seller metrics — a domain service keeps `Offer` anemic but clear."

---

### Pattern 4: Observer / Event-Driven

**Where:** `OrderPlacedEvent` → notifications, search popularity signals, recommendations feedback  
**Why:** Decouple core transactional path from side effects.  
**Interview Justification:** "Search index and email must not slow down checkout; async consumers scale independently."

---

### Pattern 5: Template Method (optional)

**Where:** `Promotion` base with hooks `appliesTo()`, `apply()` for Lightning vs DOTD  
**Why:** Shared validation (dates, limits) with specialized price computation.

---

## 💡 Step 8: Interview Discussion Points

### 1. Inventory: ATP vs Reservation vs Overselling

**Interviewer:** "How do you avoid overselling during Lightning Deals?"

**Answer:**

"**Three layers:**

1. **Display ATP** — Cached, may be slightly stale; show 'Low stock' thresholds.
2. **Reservation at checkout** — Short TTL (e.g., 10–15 minutes). Increment `quantityReserved` with optimistic locking on `InventoryBalance`.
3. **Commit on successful payment** — Move from reserved to shipped path: decrement `quantityOnHand` and `quantityReserved` atomically.

For Lightning Deals, also maintain a **deal-specific cap** (per customer and global counter) using a fast store (Redis) with periodic reconciliation to the database, or strict DB row for `deal_sku_remaining` with `SELECT FOR UPDATE` during checkout only.

**Race:** Two users last unit — second reservation fails; cart refreshes with message. **Idempotent orders** prevent double-charge if client retries."

---

### 2. Seller Platform: Offers, Buy Box, FBA vs FBM

**Interviewer:** "How does Buy Box work in your model?"

**Answer:**

"`Offer` is the atomic sellable: **SKU + seller + price + fulfillment + handling time**. Buy Box is **not** a stored entity; it is the output of `BuyBoxPolicy` scoring: landed price (including shipping where relevant), seller performance, delivery promise, stock ATP at customer region, and policy eligibility (e.g., new condition only).

**FBA:** inventory rows under Amazon FC warehouses; Amazon is seller of record for shipping experience.  
**FBM:** inventory under `Warehouse` with `type = SELLER_NODE`; seller ships; higher handling time may lose Buy Box.

Edits to offers are **async-validated** (price floors, MAP) before index update."

---

### 3. Search & Indexing: Facets, Consistency, Sponsored

**Interviewer:** "How do filters stay consistent with price and stock?"

**Answer:**

"**Search documents** denormalize: `minPrice`, `maxPrice`, `buyBoxSellerId`, `avgRating`, `primeFlag`, `dealTags`, `categoryPath`, `brandId`. Ingestion pipeline listens to **catalog, offer, inventory, review** events via outbox.

**Facets** are computed in the search engine from the same documents. Short-term staleness is acceptable for browse; **checkout** revalidates against source of truth.

**Sponsored** results are merged in a **blender** service: organic IDs from `SearchService`, ad candidates from ads API, merge with disclosure and auction rank — keeps search core untainted by ad logic."

---

### 4. Recommendations: Cold Start, Freshness, Prime

**Interviewer:** "What do you store for recommendations?"

**Answer:**

"Offline jobs build **item-item co-purchase matrix** and **content embeddings** (title/brand/category). Online, `RecommendationEngine` loads top-K neighbors for the viewed SKU or for the user's recent events from a **feature store**.

**Cold start:** popularity and category-best-sellers by region.  
**Prime:** boost SKUs with fast shipping for Prime users in ranking layer, not in the matrix itself, so the same engine can re-rank per context.

Feedback loop: **purchase and click events** to Kafka → batch recomputation + near-line session models."

---

### 5. Returns & Refunds vs Inventory

**Interviewer:** "When does stock go back?"

**Answer:**

"`ReturnLine` transitions to **RECEIVED** at a specific **warehouse** (FC or seller). Then **RestockLine** increases `quantityOnHand` if quality check passes; otherwise **write-off** adjustment. Refund state **REFUNDED** is coordinated with payment service (reverse capture or gift card credit). Partial refunds for partial quantities supported by tying refund lines to `OrderLineItem` quantities."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅

- `InventoryService`: stock movements and reservations only.
- `PricingEngine`: computes price quotes; does not place orders.
- `SearchService`: retrieval only; no checkout side effects.
- `CheckoutService`: orchestrates a single use case.

### Open/Closed ✅

```java
public class PricingEngine {
    private final List<PricingRule> rules; // add new rule class without editing engine
}
```

### Liskov Substitution ✅

- Any `PaymentProvider` implementation must honor `authorize`/`capture`/`cancel` contracts for the orchestrator.

### Interface Segregation ✅

- Split `CatalogReadService` vs `SellerCatalogWriteService` so search workers do not depend on seller mutations.

### Dependency Inversion ✅

```java
public class CheckoutService {
    private final InventoryService inventory; // interface
    private final PaymentService payments;    // interface
}
```

---

## 🎯 Key Takeaways

### Architecture

- ✅ **Catalog + Offer** separation enables true marketplace (many sellers, one SKU family).
- ✅ **Inventory** modeled per `(warehouse, sku)` with **reservations + optimistic locking** for concurrency.
- ✅ **Checkout saga** with **compensations** ties payment and stock.
- ✅ **Pricing** as ordered **rules** (deals, coupons, Prime).
- ✅ **Search** via denormalized documents and **event-driven** index updates.
- ✅ **Recommendations** behind **strategies** + inventory-aware post-filtering.
- ✅ **Order/shipment/return** state machines with **notifications** on domain events.

### Marketplace & Trust

- ✅ **Buy Box** as policy over offers, not a static field.
- ✅ **Seller ratings** and performance feed Buy Box and search boost (policy-level).

### Hard Parts (Interview Focus)

- ✅ Inventory **ATP vs reserve vs commit** under flash traffic.
- ✅ **Idempotent** order APIs and **exactly-once** business effect (database idempotency + outbox).
- ✅ **Search freshness** vs **checkout correctness** tradeoff articulated clearly.

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
