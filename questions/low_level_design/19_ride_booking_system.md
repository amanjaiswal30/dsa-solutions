# Low-Level Design: Ride Booking System (Uber / Ola)

**Difficulty:** Hard 🔥

**Interview Duration:** 90–120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting, with emphasis on **driver matching**, **geolocation (geohash / proximity)**, and **dynamic (surge) pricing**.

---

## 🎯 Step 1: Understand the Problem (8–12 minutes)

### What the Interviewer Says:
*"Design a ride-booking platform (Uber / Ola–style) where riders request trips, drivers are matched by proximity and ETA, fares reflect demand (surge), trips are tracked in real time, payments settle at the end (including split fare), and both sides rate each other."*

### Clarifying Questions to Ask:

1. **Q:** Are we designing globally or one metro / region first?  
   **A:** Start with one **city / geofenced region**; architecture should shard by region for scale.

2. **Q:** What ride products exist?  
   **A:** **UberX, UberPool, UberXL, UberBlack** — different capacity, eligibility, and base pricing.

3. **Q:** How is a driver chosen?  
   **A:** **Proximity + ETA + availability + product fit**; discuss batching, fairness, and re-offers on timeout.

4. **Q:** Surge pricing — how is it computed?  
   **A:** **Demand vs supply** in fine-grained **hex/geohash tiles**; cap multipliers; transparent to rider pre-accept.

5. **Q:** Real-time location — how fresh?  
   **A:** Driver pings every few seconds while online / on-trip; rider sees **interpolated** route progress.

6. **Q:** Payments — scope?  
   **A:** **Authorize** on request or start; **capture** on completion; **split fare** among riders for Pool; refunds for disputes.

7. **Q:** Cancellations?  
   **A:** **Rider vs driver** cancel with **time-based fees**; no-show rules; driver acceptance timeout frees driver.

8. **Q:** Route optimization?  
   **A:** **External routing API** (Mapbox, OSRM, Google) behind `RouteService`; cache polylines; recompute on traffic.

9. **Q:** Notifications?  
   **A:** **Driver found, arriving, trip started, completed, receipt** — push + SMS fallback; idempotent delivery.

10. **Q:** Consistency expectations?  
    **A:** **Strong** for money and ride state transitions; **eventual** for ETA map tiles and surge display lag (bounded).

11. **Q:** Abuse / safety?  
    **A:** Rate limits, device binding, fraud signals on payment; out of scope for deep LLD unless asked.

12. **Q:** Pool matching — shared pickups?  
    **A:** **Virtual stops** or detour caps; **insertion** into existing pool trip subject to SLA.

13. **Q:** Driver documents and onboarding?  
    **A:** **KYC** workflow async; `Driver` has `verificationStatus`; only **APPROVED** drivers enter matching pool.

---

## 🔹 Step 2: Gather Requirements (10–15 minutes)

### Functional Requirements

#### User Management (FR1–FR8)
1. System shall register and authenticate **riders** and **drivers** (separate profiles, linked to one `User` identity if needed).
2. Riders shall manage payment methods, home/work shortcuts, and communication preferences.
3. Drivers shall manage vehicle(s), **product eligibility** (e.g., XL = min seats), documents, and **bank / payout** profile.
4. System shall enforce **driver availability** states: `OFFLINE`, `AVAILABLE`, `EN_ROUTE_PICKUP`, `ON_TRIP`, `PAUSED`.
5. System shall support **suspension** / **deactivation** with reason codes for trust & safety.
6. Riders and drivers shall each maintain **aggregate rating** and **trip counts** (derived or cached).
7. System shall support **multi-device** sessions with secure token refresh (high level).
8. Admin / ops shall override driver status for incidents (optional policy hook).

#### Ride Request & Lifecycle (FR9–FR18)
9. Rider shall request a ride with **pickup** and **destination** (lat/lng or place ID resolved to coordinates).
10. Rider shall select **product type** (UberX, Pool, XL, Black) where offered in the region.
11. System shall show **upfront fare estimate** (or range) using current **surge**, distance, time, and product **multipliers**.
12. System shall create a **Ride** in `REQUESTED` state and enqueue **matching**.
13. Upon driver accept, ride transitions to `DRIVER_ASSIGNED` → `EN_ROUTE` → `ARRIVED` → `IN_PROGRESS` → `COMPLETED` (or terminal cancel states).
14. System shall support **destination changes** mid-trip per policy (re-rate, driver consent).
15. System shall persist **ride history** for both parties with fare breakdown and map snapshot metadata.
16. System shall support **scheduled rides** (optional) — separate queue with time window matching.
17. System shall log **state transitions** with timestamps for support and disputes.
18. System shall enforce **one active assigned driver** per ride and **no double-booking** of same driver across concurrent trips.

#### Driver Matching & Allocation (FR19–FR26)
19. Matching shall consider **geographic proximity** using **geohash (or H3) tiling** and in-memory / geo indexes.
20. Matching shall compute **ETA to pickup** (and for Pool, **detour ETA**) via `RouteService`.
21. System shall **rank** candidate drivers by composite score (ETA, distance, rating floor, fairness, idle time).
22. System shall **offer** the ride to top-K drivers sequentially or in a small **broadcast window** (interview tradeoff).
23. On **timeout** or decline, system shall re-offer to next candidates without starving distant drivers long-term.
24. **UberPool** shall **batch** or **insert** riders subject to **max detour** and **max extra time** constraints.
25. **UberBlack** may restrict to **luxury-eligible** vehicles and higher **minimum rating**.
26. Matching shall respect **driver max working hours** / regional compliance flags.

#### Dynamic Pricing & Fare (FR27–FR34)
27. System shall compute **surge multiplier** per **geo tile** from **open ride requests** vs **available drivers** (and optional moving average).
28. Surge shall apply to **per-mile / per-minute** components (or upfront model inputs) per product rules.
29. System shall show rider **surge disclosure** before confirmation where required.
30. System shall support **promos, credits, and platform fees** in fare breakdown.
31. **Split fare** shall allow N riders to agree to shares; **payment orchestration** charges each or one payer with internal ledger entries.
32. System shall support **tolls and airport fees** as line items when provided by routing or manual rules.
33. System shall recalculate fare on **material route change** (policy: cap delta, notify rider).
34. System shall **freeze** fare version id on trip start for **upfront-priced** trips (audit).

#### Trip Tracking & Geolocation (FR35–FR40)
35. Drivers shall **publish location** at configured interval while `AVAILABLE` or on active trip.
36. Riders shall see **driver on map**, **ETA to pickup**, and **route polyline** (cached, refreshed).
37. System shall run **proximity search** (“drivers near point”) in **O(log N)** or **O(1)** per tile using geohash prefixes + neighbor cells.
38. System shall **snap** noisy GPS to road network optionally (product detail).
39. System shall store **location trail** for trip segment for support (retention policy).
40. **Geofences** (airport, surge boundaries) shall resolve via polygon or tile membership.

#### Payments (FR41–FR45)
41. System shall **authorize** payment instrument before or at trip start per risk policy.
42. System shall **capture** final amount on `COMPLETED` with **idempotent** payment requests.
43. System shall record **ledger entries**: rider charge, driver payout, platform take, tolls, surge allocation.
44. System shall handle **partial failures** in split fare with compensating actions or rollbacks.
45. System shall issue **receipts** and support **refunds** via adjustment workflow.

#### Ratings & Feedback (FR46–FR49)
46. After trip completion, **rider rates driver** (and optional tags); **driver rates rider** (two-way).
47. Ratings shall be **1–5** with optional **minimum comment** for low scores (policy).
48. System shall **de-dupe** rating submission per trip side.
49. Aggregate ratings shall **update** asynchronously to avoid blocking completion API.

#### Notifications (FR50–FR53)
50. Notify rider: **driver matched**, **driver arriving** (geo-triggered proximity), **trip started**, **trip completed**, **receipt ready**.
51. Notify driver: **new offer**, **offer expired**, **rider cancel**, **surge map update** (optional).
52. Notifications shall be **idempotent** (`dedupeKey` per event type + ride id).
53. System shall prefer **push** with **SMS** fallback for critical events.

#### Cancellation & Penalties (FR54–FR58)
54. Rider may cancel in `REQUESTED` / `DRIVER_ASSIGNED` / `EN_ROUTE` with **tiered fees** by time and region rules.
55. Driver may cancel with **consequence** (acceptance rate, strikes) except **safety** reasons.
56. **No-show**: if rider not at pickup after `ARRIVED` + timer, driver may cancel with fee to rider.
57. System shall **free** driver resource atomically on cancel or trip end.
58. **Driver idle** after decline streak may trigger **cooldown** (optional gamification / quality).

#### Route Optimization (FR59–FR62)
59. `RouteService` shall return **duration**, **distance**, and **encoded polyline** for pickup and trip legs.
60. For **Pool**, system shall solve **pickup/dropoff order** minimizing **total weighted time** under constraints (greedy or TSP-lite).
61. System shall **refresh ETA** periodically using traffic-aware routing when available.
62. System shall **cache** routes by (origin cell, dest cell, product) with TTL.

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many concurrent rides, drivers, and location updates?"
- Major city: **10k–50k** concurrent drivers; **peak** ride requests **1k–5k**/minute; location updates **multi-million**/minute at edge.

**Deduced NFRs:**
- ✅ **Horizontal scale**: stateless API + **regional cells**; **sharding** by `regionId` or geohash prefix.
- ✅ **Write-heavy location path** separated from **OLTP ride** path (streaming to Kafka / Pulsar).
- ✅ **Geo index** per region (Redis GEO, Elasticsearch geo, or custom geohash map).
- ✅ **CDN / edge** for static map tiles; **WebSocket** or **gRPC stream** for live trip.

---

#### 2. **Consistency Analysis**

**Think:** "What must never be wrong?"
- **Payment capture** and **ride terminal state**; **driver assignment** (one driver, one active trip); **surge application** to committed fare version.

**Deduced NFRs:**
- ✅ **Strong consistency** for ride state machine and wallet captures (transactional outbox).
- ✅ **Compare-and-swap / row lock** on `Driver.currentRideId` when assigning.
- ✅ **Idempotency keys** on create-ride, accept-offer, complete-ride, charge.
- ✅ **Eventual consistency** OK for **map ETA**, **surge heatmap**, **cached ratings**.

---

#### 3. **Availability Analysis**

**Think:** "What happens when routing API is down?"
- Matching may degrade to **Haversine distance** + static speed assumptions; payments queue in **outbox**.

**Deduced NFRs:**
- ✅ **99.95%** for ride request/assign path in region.
- ✅ **Graceful degradation**: fallback ETA; read-only mode for history.
- ✅ **Multi-AZ** for core DB; **chaos-tested** failover for matching workers.

---

#### 4. **Maintainability Analysis**

**Think:** "How do we tune matching without redeploying everything?"
- **Feature flags** for scoring weights; **dark launch** new surge formula.

**Deduced NFRs:**
- ✅ **Configurable** matching weights, surge caps, cancellation fee tables per region.
- ✅ **Structured logs** with `rideId`, `driverId`, `traceId`.
- ✅ **Replay** matching decisions from event log for disputes.

---

#### 5. **Performance Analysis**

**Think:** "SLAs for match and location?"
- **p99 match offer** < **300ms** after request (excluding client network); **location ingest** < **50ms** at edge.

**Deduced NFRs:**
- ✅ **Hot path** avoids cross-region DB; candidate drivers from **local geo index** only.
- ✅ **Batch** surge recomputation every **N seconds** or on **threshold** change.
- ✅ **Async** notifications and rating updates off critical path.

---

#### 6. **Security Analysis**

**Think:** "PII, payment tokens, location history."
- Tokenize cards with **PCI-compliant** provider; encrypt PII at rest; **least privilege** for support tools.

**Deduced NFRs:**
- ✅ **OAuth2** / device attestation for mobile; **rate limiting** on ride creation.
- ✅ **GDPR-style** delete/export hooks for user profile and trip history.
- ✅ **Audit** who changed surge overrides or manual fare adjustments.

---

## 🧩 Step 3: Identify Core Entities (12–18 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Rider requests pickup and destination" | Rider, RideRequest, Location, Place |
| "Driver accepts trip" | Driver, DriverOffer, Acceptance |
| "Surge by area" | SurgeZone, GeoTile, DemandSnapshot |
| "Track trip on map" | TripSegment, LocationUpdate, Route |
| "Pay and split fare" | Payment, Fare, LedgerEntry, SplitFare |
| "Rate driver and rider" | Rating, ReviewTag |
| "Notify driver found" | Notification, NotificationTemplate |
| "Cancel with fee" | CancellationPolicy, CancellationFee |
| "Geohash proximity search" | GeohashIndex, SpatialIndex |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Location | ✅ YES | Value object / embedded type with lat, lng, heading |
| Place | ✅ YES | Resolved address + coordinates + provider ref |
| RideRequest | ❌ NO | Becomes or merges into `Ride` in `REQUESTED` state |
| SurgeZone | ✅ YES | Policy boundary (could be tile set) |
| GeoTile | ✅ YES | Geohash/H3 cell with surge & demand aggregates |
| DemandSnapshot | ❌ NO | Derived metric / cache row |
| Route | ✅ YES | Polyline + distance + duration snapshot |
| Payment | ✅ YES | Charge/capture lifecycle |
| LedgerEntry | ✅ YES | Double-entry style accounting line |
| Notification | ✅ YES | Outbox / delivery record |
| CancellationPolicy | ✅ YES | Rule engine input (or strategy) |
| GeohashIndex | ✅ YES | Infrastructure service indexing drivers |

### Final Entity List

**Identity & Profiles:**
1. **User** — authentication root (optional shared with Rider/Driver specialization)
2. **Rider** — payment methods, defaults, rating aggregates
3. **Driver** — vehicles, documents, availability, acceptance metrics, current location ref

**Ride Domain:**
4. **Ride** — aggregate root; state machine; pickup/dest; product; fare snapshot id
5. **RideOffer** — driver-specific offer with expiry (timeout)
6. **RideLeg** — pickup → dropoff segment with route snapshot
7. **PoolItinerary** — ordered stops for Pool (multiple riders)

**Geo & Routing:**
8. **GeoTile** — id (geohash/H3), surge multiplier, demand/supply counters
9. **LocationUpdate** — driverId, ts, lat, lng, accuracy, rideId nullable
10. **RouteSnapshot** — encoded polyline, distanceMeters, durationSeconds, provider

**Pricing & Money:**
11. **FareQuote** — upfront estimate version
12. **FareBreakdown** — base, distance, time, surge, fees, tolls, promo
13. **SurgeEngine** (service) / **SurgeConfig** — caps, smoothing window
14. **PaymentIntent** — authorize/capture ids, status
15. **SplitFareAgreement** — participants and shares

**Trust & Engagement:**
16. **Rating** — tripId, fromRole, toRole, stars, tags
17. **NotificationEvent** — type, payload, dedupe key

**Policies:**
18. **CancellationPolicy** — rules by state and role
19. **MatchingStrategy** — scoring interface (Strategy pattern)

---

## 🔗 Step 4: Establish Relationships (14–20 minutes)

### Pass 1: Core Relationships

#### Rider ↔ Ride
**Conclusion:** **Composition / aggregation** — rider initiates many rides over time.
```
Rider ─────→ Ride [1..*]
```

#### Driver ↔ Ride
**Conclusion:** **Association** — driver completes many rides; at most one **active** assignment.
```
Driver ─────→ Ride [0..*]  (temporal: at most one ACTIVE)
```

#### Ride ↔ RouteSnapshot
**Conclusion:** **Composition** — ride owns immutable route snapshots per phase.
```
Ride ◆────→ RouteSnapshot [1..*]
```

---

### Pass 2: Matching & Offers

#### Ride ↔ RideOffer
**Conclusion:** **Composition**
```
Ride ◆────→ RideOffer [0..*]
```

#### MatchingService ↔ Driver
**Conclusion:** **Association** — service reads **candidate** drivers from `GeohashIndex`.

---

### Pass 3: Pricing & Payment

#### Ride ↔ FareBreakdown
**Conclusion:** **Composition** — one settled breakdown per completed ride.
```
Ride ◆────→ FareBreakdown [1]
```

#### Ride ↔ PaymentIntent
**Conclusion:** **Association** — 1:1 typical for single payer; 1:N with split.

#### SplitFareAgreement ↔ Rider
**Conclusion:** **Association** N:M through agreement entity.

---

### Pass 4: Geo

#### Driver ↔ LocationUpdate
**Conclusion:** **Association** — high-volume stream; optional link to `Ride`.

#### GeoTile ↔ Surge multiplier
**Conclusion:** **Attribute** of tile or side table `tile_surge(tile_id, effective_from, multiplier)`.

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Rider → Ride | 1:N | Association |
| Driver → Ride | 1:N | Association (active 1:0..1) |
| Ride → RideOffer | 1:N | Composition |
| Ride → RouteSnapshot | 1:N | Composition |
| Ride → FareBreakdown | 1:1 | Composition |
| Ride → PaymentIntent | 1:1 or 1:N | Association |
| Ride → Rating | 1:2 max | Association (rider→driver, driver→rider) |
| Region → GeoTile | 1:N | Aggregation |

---

## 📐 Step 5: Design Class Diagrams (18–25 minutes)

### Class Diagram 1: Enums

```
┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │  │ <<enumeration>>    │
│   RideStatus     │  │  DriverStatus    │  │   ProductType      │
├──────────────────┤  ├──────────────────┤  ├────────────────────┤
│ REQUESTED        │  │ OFFLINE          │  │ UBER_X             │
│ MATCHING         │  │ AVAILABLE        │  │ UBER_POOL          │
│ DRIVER_ASSIGNED  │  │ EN_ROUTE_PICKUP  │  │ UBER_XL            │
│ EN_ROUTE         │  │ ON_TRIP          │  │ UBER_BLACK         │
│ ARRIVED          │  │ PAUSED           │  └────────────────────┘
│ IN_PROGRESS      │  └──────────────────┘
│ COMPLETED        │
│ CANCELLED_RIDER  │
│ CANCELLED_DRIVER │
│ NO_SHOW          │
└──────────────────┘

┌──────────────────┐  ┌──────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │
│ NotificationType │  │  PaymentStatus   │
├──────────────────┤  ├──────────────────┤
│ DRIVER_MATCHED   │  │ AUTHORIZED       │
│ DRIVER_ARRIVING  │  │ CAPTURED         │
│ TRIP_STARTED     │  │ FAILED           │
│ TRIP_COMPLETED   │  │ REFUNDED         │
│ RECEIPT_READY    │  └──────────────────┘
└──────────────────┘
```

---

### Class Diagram 2: Location, Route, Geo

```
┌─────────────────────────────────────────┐
│              LatLng                     │
├─────────────────────────────────────────┤
│ - lat: double                           │
│ - lng: double                           │
├─────────────────────────────────────────┤
│ + distanceTo(other: LatLng): double     │
│ + geohash(precision: int): String      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│           RouteSnapshot                 │
├─────────────────────────────────────────┤
│ - provider: String                      │
│ - distanceMeters: int                   │
│ - durationSeconds: int                  │
│ - encodedPolyline: String               │
│ - computedAt: Instant                   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│            GeoTile                        │
├─────────────────────────────────────────┤
│ - tileId: String   // geohash or H3     │
│ - regionId: String                      │
│ - openRequests: AtomicInteger           │
│ - availableDrivers: AtomicInteger       │
│ - surgeMultiplier: double               │
├─────────────────────────────────────────┤
│ + recomputeSurge(config: SurgeConfig)   │
└─────────────────────────────────────────┘
```

---

### Class Diagram 3: Rider, Driver, Vehicle

```
┌─────────────────────────────────────────┐
│               Rider                     │
├─────────────────────────────────────────┤
│ - riderId: String                       │
│ - userId: String                        │
│ - defaultPaymentMethodId: String        │
│ - ratingAggregate: double               │
├─────────────────────────────────────────┤
│ + requestRide(req: RideRequestDto)      │
│   : Ride                                │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│              Driver                     │
├─────────────────────────────────────────┤
│ - driverId: String                      │
│ - userId: String                        │
│ - status: DriverStatus                  │
│ - currentLocation: LatLng               │
│ - geohashCell: String                   │
│ - eligibleProducts: Set<ProductType>    │
│ - vehicle: Vehicle                      │
│ - currentRideId: String (nullable)     │
│ - acceptanceRate: double                │
├─────────────────────────────────────────┤
│ + goOnline(): void                      │
│ + goOffline(): void                     │
│ + updateLocation(loc: LatLng): void     │
│ + canAccept(product: ProductType): bool │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│              Vehicle                    │
├─────────────────────────────────────────┤
│ - make, model, color, plate             │
│ - seatCapacity: int                     │
│ - luxuryTier: boolean                   │
└─────────────────────────────────────────┘
```

---

### Class Diagram 4: Ride & Offers

```
┌──────────────────────────────────────────────────────────────┐
│                         Ride                                 │
├──────────────────────────────────────────────────────────────┤
│ - rideId: String                                             │
│ - riderId: String                                            │
│ - driverId: String (nullable)                                  │
│ - product: ProductType                                       │
│ - status: RideStatus                                         │
│ - pickup: Place                                              │
│ - destination: Place                                         │
│ - requestedAt: Instant                                       │
│ - fareQuoteId: String                                        │
│ - surgeMultiplierAtBooking: double                           │
│ - poolItinerary: PoolItinerary (nullable)                    │
├──────────────────────────────────────────────────────────────┤
│ + assignDriver(driverId: String): void                       │
│ + markEnRoute(): void                                        │
│ + markArrived(): void                                        │
│ + startTrip(): void                                          │
│ + complete(breakdown: FareBreakdown): void                   │
│ + cancel(by: Role, reason: String): void                     │
└──────────────────────────────────────────────────────────────┘
              │ 1
              │
              │ *
              ▼
┌──────────────────────────────────────────────┐
│              RideOffer                       │
├──────────────────────────────────────────────┤
│ - offerId: String                            │
│ - driverId: String                           │
│ - expiresAt: Instant                         │
│ - status: PENDING | ACCEPTED | EXPIRED | DECLINED
│ - score: double                              │
└──────────────────────────────────────────────┘
```

---

### Class Diagram 5: Matching, Surge, Fare, Payment

```
┌──────────────────────────────────────────────┐
│         <<interface>>                        │
│      MatchingStrategy                        │
├──────────────────────────────────────────────┤
│ + rankDrivers(ride: Ride, candidates:       │
│   List<Driver>, ctx: MatchingContext)       │
│   : List<ScoredDriver>                       │
└──────────────────────────────────────────────┘
                    △
                    │
                    │ DefaultProximityEtaStrategy
                    │
┌──────────────────────────────────────────────┐
│        DriverMatchingService                 │
├──────────────────────────────────────────────┤
│ - geoIndex: GeohashDriverIndex               │
│ - routeService: RouteService                 │
│ - surgeService: SurgePricingService          │
│ - strategy: MatchingStrategy                 │
├──────────────────────────────────────────────┤
│ + findCandidates(ride: Ride): List<Driver>   │
│ + offerToNextBatch(ride: Ride): void         │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│        SurgePricingService                   │
├──────────────────────────────────────────────┤
│ + getMultiplier(tileId: String): double      │
│ + refreshTileMetrics(regionId: String): void│
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│          FareCalculator                      │
├──────────────────────────────────────────────┤
│ + quote(ride: Ride, route: RouteSnapshot,    │
│   surge: double): FareBreakdown              │
│ + settle(ride: Ride): FareBreakdown          │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│         PaymentOrchestrator                  │
├──────────────────────────────────────────────┤
│ + authorize(ride: Ride): PaymentIntent       │
│ + capture(ride: Ride, fare: FareBreakdown)   │
│ + splitCapture(ride: Ride, splits: Map)      │
└──────────────────────────────────────────────┘
```

---

### Class Diagram 6: Notifications & Cancellation

```
┌──────────────────────────────────────────────┐
│       NotificationService                    │
├──────────────────────────────────────────────┤
│ + send(userId, type, payload, dedupeKey)     │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│       CancellationPolicyEngine               │
├──────────────────────────────────────────────┤
│ + feeFor(ride: Ride, cancelledBy: Role,      │
│   at: Instant): Money                        │
└──────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35–50 minutes)

### Enums (Java)

```java
// ProductType.java
public enum ProductType {
    UBER_X, UBER_POOL, UBER_XL, UBER_BLACK;

    public double baseMultiplier() {
        return switch (this) {
            case UBER_X -> 1.0;
            case UBER_POOL -> 0.85;
            case UBER_XL -> 1.4;
            case UBER_BLACK -> 2.2;
        };
    }
}
```

```java
// RideStatus.java
public enum RideStatus {
    REQUESTED, MATCHING, DRIVER_ASSIGNED, EN_ROUTE, ARRIVED,
    IN_PROGRESS, COMPLETED, CANCELLED_RIDER, CANCELLED_DRIVER, NO_SHOW
}
```

---

### LatLng & Geohash (simplified)

```java
// LatLng.java
public record LatLng(double lat, double lng) {

    /** Haversine km — used when routing unavailable */
    public double distanceKm(LatLng o) {
        final double R = 6371.0;
        double dLat = Math.toRadians(o.lat - this.lat);
        double dLon = Math.toRadians(o.lng - this.lng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(o.lat))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /** Precision ~5: ~4.9km x 4.9km; ~7: ~153m — tune per city */
    public String geohash(int precision) {
        return GeohashUtils.encode(lat, lng, precision);
    }
}
```

```java
// GeohashUtils.java — interview stub; production uses tested library
public final class GeohashUtils {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    public static String encode(double lat, double lng, int precision) {
        // ... standard bit-interleave base32 encoding ...
        // Omitted for brevity — cite "use geohash-java or H3"
        return "9q8yy"; // placeholder
    }

    /** Neighboring cells for edge matching */
    public static List<String> neighbors(String hash) {
        return List.of(); // expand N,NE,E,SE,S,SW,W,NW in full impl
    }
}
```

---

### GeohashDriverIndex — Proximity Search

```java
// GeohashDriverIndex.java — in-region inverted index
public class GeohashDriverIndex {
    private final int precision;
    /** cell -> driver ids currently AVAILABLE in cell */
    private final Map<String, Set<String>> cellToDrivers = new ConcurrentHashMap<>();
    private final Map<String, String> driverToCell = new ConcurrentHashMap<>();

    public GeohashDriverIndex(int precision) {
        this.precision = precision;
    }

    public void updateDriverCell(String driverId, LatLng location) {
        String newCell = location.geohash(precision);
        String old = driverToCell.put(driverId, newCell);
        if (old != null) cellToDrivers.computeIfAbsent(old, k -> ConcurrentHashMap.newKeySet()).remove(driverId);
        cellToDrivers.computeIfAbsent(newCell, k -> ConcurrentHashMap.newKeySet()).add(driverId);
    }

    public void removeDriver(String driverId) {
        String cell = driverToCell.remove(driverId);
        if (cell != null) {
            Set<String> set = cellToDrivers.get(cell);
            if (set != null) set.remove(driverId);
        }
    }

    /** Candidates in cell + neighbors — critical for drivers on tile boundary */
    public Set<String> nearbyDriverIds(LatLng pickup) {
        String center = pickup.geohash(precision);
        Set<String> out = new HashSet<>();
        out.addAll(cellToDrivers.getOrDefault(center, Set.of()));
        for (String n : GeohashUtils.neighbors(center)) {
            out.addAll(cellToDrivers.getOrDefault(n, Set.of()));
        }
        return out;
    }
}
```

---

### RouteService & ETA

```java
// RouteService.java
public interface RouteService {
    RouteSnapshot route(LatLng from, LatLng to);
}

public record RouteSnapshot(
        int distanceMeters,
        int durationSeconds,
        String encodedPolyline,
        String provider,
        Instant computedAt
) {}

// Fallback when API down
public class HaversineRouteService implements RouteService {
    private static final double AVG_URBAN_KMH = 25.0;

    @Override
    public RouteSnapshot route(LatLng from, LatLng to) {
        double km = from.distanceKm(to);
        int meters = (int) (km * 1000);
        int seconds = (int) (km / AVG_URBAN_KMH * 3600);
        return new RouteSnapshot(meters, seconds, "", "HAVERSINE", Instant.now());
    }
}
```

---

### SurgePricingService — Demand / Supply

```java
// SurgePricingService.java
public class SurgePricingService {
    private final Map<String, GeoTile> tiles = new ConcurrentHashMap<>();

    public double getMultiplier(String tileId) {
        return tiles.getOrDefault(tileId, defaultTile()).surgeMultiplier();
    }

    public void incrementOpenRequest(String tileId) {
        tile(tileId).openRequests().incrementAndGet();
    }

    public void decrementOpenRequest(String tileId) {
        tile(tileId).openRequests().updateAndGet(v -> Math.max(0, v - 1));
    }

    public void setAvailableDrivers(String tileId, int count) {
        tile(tileId).availableDrivers().set(count);
    }

    /** Called periodically per region */
    public void recomputeSurge(String tileId, SurgeConfig cfg) {
        GeoTile t = tile(tileId);
        int D = t.openRequests().get();
        int S = Math.max(1, t.availableDrivers().get());
        double ratio = (double) D / S;
        // Piecewise function — cap max surge
        double mult = 1.0 + Math.min(cfg.maxSurge() - 1.0, cfg.alpha() * Math.log1p(ratio));
        t.setSurgeMultiplier(round(mult, 2));
    }

    private GeoTile tile(String tileId) {
        return tiles.computeIfAbsent(tileId, GeoTile::new);
    }

    private GeoTile defaultTile() {
        return new GeoTile("default");
    }
}

public record SurgeConfig(double alpha, double maxSurge) {
    public static SurgeConfig defaultConfig() {
        return new SurgeConfig(0.35, 3.0);
    }
}

public final class GeoTile {
    private final String tileId;
    private final AtomicInteger openRequests = new AtomicInteger(0);
    private final AtomicInteger availableDrivers = new AtomicInteger(0);
    private volatile double surgeMultiplier = 1.0;

    public GeoTile(String tileId) { this.tileId = tileId; }

    public AtomicInteger openRequests() { return openRequests; }
    public AtomicInteger availableDrivers() { return availableDrivers; }
    public double surgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(double m) { this.surgeMultiplier = m; }
}
```

---

### Matching Strategy — Composite Score (ETA + fairness)

```java
public record ScoredDriver(Driver driver, double score) {}

public interface MatchingStrategy {
    List<ScoredDriver> rank(Ride ride, List<Driver> candidates, MatchingContext ctx);
}

public record MatchingContext(
        RouteService router,
        SurgePricingService surge,
        double etaWeight,
        double distanceWeight,
        double ratingWeight,
        double idleBonusWeight
) {}

public class DefaultMatchingStrategy implements MatchingStrategy {

    @Override
    public List<ScoredDriver> rank(Ride ride, List<Driver> candidates, MatchingContext ctx) {
        List<ScoredDriver> scored = new ArrayList<>();
        LatLng pickup = ride.pickup().latLng();

        for (Driver d : candidates) {
            if (!d.canAccept(ride.product())) continue;
            if (d.currentRideId() != null) continue;

            RouteSnapshot toPickup = ctx.router().route(d.currentLocation(), pickup);
            double eta = toPickup.durationSeconds();
            double distKm = d.currentLocation().distanceKm(pickup);
            double rating = d.ratingOrDefault();

            // Lower score is better (like cost)
            double cost = ctx.etaWeight() * eta
                    + ctx.distanceWeight() * distKm * 60 // scale km ~ minutes crude
                    - ctx.ratingWeight() * rating
                    - ctx.idleBonusWeight() * d.idleMinutesNormalized();

            scored.add(new ScoredDriver(d, cost));
        }
        scored.sort(Comparator.comparingDouble(ScoredDriver::score));
        return scored;
    }
}
```

---

### Driver (sketch)

```java
public class Driver {
    private final String driverId;
    private DriverStatus status = DriverStatus.OFFLINE;
    private LatLng currentLocation;
    private String currentRideId;
    private final Set<ProductType> eligibleProducts = EnumSet.of(ProductType.UBER_X);
    private double rating = 5.0;
    private long idleMinutesNormalized = 0;

    public Driver(String driverId, LatLng initial) {
        this.driverId = driverId;
        this.currentLocation = initial;
    }

    public String driverId() { return driverId; }
    public LatLng currentLocation() { return currentLocation; }
    public String currentRideId() { return currentRideId; }
    public void setCurrentRideId(String id) { this.currentRideId = id; }
    public boolean canAccept(ProductType p) { return eligibleProducts.contains(p); }
    public double ratingOrDefault() { return rating; }
    public double idleMinutesNormalized() { return idleMinutesNormalized; }
    public void setLocation(LatLng l) { this.currentLocation = l; }
}
```

```java
// RideOffer.java
public record RideOffer(String rideId, String driverId, Duration ttl) {
    public Instant expiresAt() { return Instant.now().plus(ttl); }
}
```

---

### DriverMatchingService — Offer Loop

```java
public class DriverMatchingService {
    private final GeohashDriverIndex geoIndex;
    private final Map<String, Driver> drivers;
    private final RouteService router;
    private final MatchingStrategy strategy;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public DriverMatchingService(GeohashDriverIndex geoIndex,
                                   Map<String, Driver> drivers,
                                   RouteService router,
                                   MatchingStrategy strategy) {
        this.geoIndex = geoIndex;
        this.drivers = drivers;
        this.router = router;
        this.strategy = strategy;
    }

    public void startMatching(Ride ride) {
        Set<String> ids = geoIndex.nearbyDriverIds(ride.pickup().latLng());
        List<Driver> candidates = ids.stream().map(drivers::get).filter(Objects::nonNull).toList();

        var ctx = new MatchingContext(router, null, 1.0, 0.4, 2.0, 0.5);
        List<ScoredDriver> ranked = strategy.rank(ride, candidates, ctx);

        offerToTopK(ride, ranked, 3, Duration.ofSeconds(15));
    }

    private void offerToTopK(Ride ride, List<ScoredDriver> ranked, int k, Duration timeout) {
        int i = 0;
        for (ScoredDriver sd : ranked) {
            if (i++ >= k) break;
            RideOffer offer = new RideOffer(ride.rideId(), sd.driver().driverId(), timeout);
            // push to driver app — if accept, CAS assign
            scheduler.schedule(() -> expireIfStillPending(offer), timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void expireIfStillPending(RideOffer offer) { /* mark EXPIRED; trigger next batch */ }
}
```

---

### Ride Aggregate (core state)

```java
public record Place(String placeId, String label, LatLng latLng) {}

public class Ride {
    private final String rideId;
    private final String riderId;
    private volatile String driverId;
    private final ProductType product;
    private volatile RideStatus status;
    private final Place pickup;
    private final Place destination;
    private final Instant requestedAtTime;
    private final double surgeMultiplierAtBooking;

    public Ride(String rideId, String riderId, ProductType product,
                Place pickup, Place destination, double surgeMultiplierAtBooking) {
        this.rideId = rideId;
        this.riderId = riderId;
        this.product = product;
        this.pickup = pickup;
        this.destination = destination;
        this.surgeMultiplierAtBooking = surgeMultiplierAtBooking;
        this.status = RideStatus.REQUESTED;
        this.requestedAtTime = Instant.now();
    }

    public synchronized boolean assignDriver(String driverId) {
        if (status != RideStatus.MATCHING && status != RideStatus.REQUESTED) return false;
        this.driverId = driverId;
        this.status = RideStatus.DRIVER_ASSIGNED;
        return true;
    }

    public Instant requestedAt() { return requestedAtTime; }

    public void markEnRoute() { this.status = RideStatus.EN_ROUTE; }
    public void markArrived() { this.status = RideStatus.ARRIVED; }
    public void startTrip() { this.status = RideStatus.IN_PROGRESS; }
    public void complete() { this.status = RideStatus.COMPLETED; }

    // getters ...
    public String rideId() { return rideId; }
    public String riderId() { return riderId; }
    public String driverId() { return driverId; }
    public ProductType product() { return product; }
    public RideStatus status() { return status; }
    public Place pickup() { return pickup; }
    public Place destination() { return destination; }
    public double surgeMultiplierAtBooking() { return surgeMultiplierAtBooking; }
}
```

---

### FareCalculator — Surge + Split

```java
public record FareBreakdown(
        Money base,
        Money distanceComponent,
        Money timeComponent,
        Money surgeComponent,
        Money tolls,
        Money platformFee,
        Money total
) {}

public record Money(String currency, long cents) {
    public Money add(Money o) {
        if (!currency.equals(o.currency)) throw new IllegalArgumentException();
        return new Money(currency, cents + o.cents);
    }
    public Money multiply(double m) {
        return new Money(currency, Math.round(cents * m));
    }
}

public class FareCalculator {
    public FareBreakdown compute(Ride ride, RouteSnapshot tripRoute, int tripSeconds,
                                 SurgePricingService surge, FareRateCard rates) {
        Money base = rates.base(ride.product());
        Money dist = rates.perMeter(tripRoute.distanceMeters(), ride.product());
        Money time = rates.perSecond(tripSeconds, ride.product());

        Money sub = base.add(dist).add(time).multiply(ride.product().baseMultiplier());

        double surge = surge.getMultiplier(ride.pickup().latLng().geohash(7));
        Money afterSurge = sub.multiply(surge);

        Money tolls = rates.tollsFor(ride); // from rules
        Money fee = afterSurge.multiply(rates.platformTakeRate());
        Money total = afterSurge.add(tolls).add(fee);

        return new FareBreakdown(base, dist, time,
                afterSurge.multiply(1.0 - 1.0 / surge), tolls, fee, total);
    }
}

public record FareRateCard(
        Money uberXBase,
        double perMeter,
        double perSecond,
        double platformTakeRate
) {
    public Money base(ProductType p) { return uberXBase; } // simplify
    public Money perMeter(int m, ProductType p) {
        return new Money("USD", Math.round(m * perMeter * p.baseMultiplier() * 100));
    }
    public Money perSecond(int s, ProductType p) {
        return new Money("USD", Math.round(s * perSecond * p.baseMultiplier() * 100));
    }
    public Money tollsFor(Ride ride) { return new Money("USD", 0); }
}
```

```java
// SplitFare — charge shares with idempotency keys per participant
public class SplitFareService {
    public void chargeSplit(String rideId, Map<String, Long> riderIdToCents,
                            long expectedTotalCents, PaymentOrchestrator pay) {
        long sum = riderIdToCents.values().stream().mapToLong(Long::longValue).sum();
        if (sum != expectedTotalCents) throw new IllegalStateException("Split mismatch");
        riderIdToCents.forEach((rider, cents) ->
                pay.capturePartial(rideId, rider, cents, "split-" + rideId + "-" + rider));
    }
}
```

---

### Cancellation fees

```java
public enum Role { RIDER, DRIVER }

public class CancellationPolicyEngine {
    public Money feeFor(Ride ride, Role cancelledBy, Instant when, CancellationTable table) {
        Duration sinceRequest = Duration.between(ride.requestedAt(), when);
        return switch (cancelledBy) {
            case RIDER -> table.riderFee(ride.status(), sinceRequest);
            case DRIVER -> table.driverFee(ride.status());
        };
    }
}
```

---

### Trip tracking — location ingest (sketch)

```java
public class TripTrackingService {
    public void onDriverLocation(String driverId, LatLng loc, Instant ts, GeohashDriverIndex index) {
        index.updateDriverCell(driverId, loc);
        // publish to Kafka: topic driver-locations, key driverId
        // WebSocket fan-out to active rider for that driver's current ride
    }
}
```

---

### Demo Wiring (Pseudo)

```java
public class UberDemo {
    public static void main(String[] args) {
        // 1. Driver goes online → index cell
        // 2. Rider requests UberX pickup→dest → surge read for tile
        // 3. Matching ranks by ETA; offer top driver; accept → assignDriver CAS
        // 4. Stream locations → rider map; geofence "arriving" notification
        // 5. complete trip → fare + capture + dual rating prompts
        // 6. Pool: second rider insertion — recompute itinerary greedy
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `MatchingStrategy`, `CancellationPolicyEngine` | Swap ranking or fee rules per city/A-B test |
| **State** | `RideStatus` transitions | Illegal transitions rejected (e.g., complete from REQUESTED) |
| **Factory** | `RouteService` provider selection | Plug Mapbox/OSRM/Google without leaking SDKs |
| **Repository** | `RideRepository`, `DriverRepository` | Persistence abstraction; test doubles |
| **Domain Events** | `RideAssigned`, `TripCompleted` | Decouple matching from notifications, analytics, surge refresh |
| **Saga / Outbox** | Payment capture + payout | Reliable cross-service money flow with retries |
| **Anti-Corruption** | External routing & payments | Isolate vendor models from core domain |
| **Observer / Pub-Sub** | Notifications, live map | Scale fan-out; idempotent consumers |
| **Composite (scoring)** | Matching objective function | Tune weights without rewriting loop |

---

## 🔒 Step 8: Concurrency Handling (14–20 minutes)

### Problem
Many **concurrent** ride requests compete for the **same subset of drivers**; location updates are **extremely frequent**; **double assignment** of a driver or **lost payment** must not happen.

### Goals
- **Exactly one** winning `assignDriver` per ride; **at most one** active trip per driver.
- **Idempotent** ride creation and payment capture.
- **High throughput** for location writes without blocking matching transactions.

### Techniques

1. **Compare-And-Set on Driver row**  
   `UPDATE drivers SET current_ride_id=? WHERE driver_id=? AND current_ride_id IS NULL AND status='AVAILABLE'` — single winner.

2. **Ride row lock for terminal transitions**  
   `SELECT ... FOR UPDATE` on `rides` when moving to `COMPLETED` and charging.

3. **Idempotency-Key header** on REST: `Idempotency-Key: uuid` stored in `idempotency_keys` table with response replay.

4. **Partitioning**  
   Kafka key = `regionId` + `geohashPrefix` for matching workers; location stream keyed by `driverId` for ordering per driver.

5. **Optimistic locking** on `Ride.version` for rare concurrent admin edits.

6. **Surge counters**  
   Use **CRDT-style** or **single-threaded aggregator per tile** consuming events (avoid lost updates from raw concurrent increments if strong accuracy needed).

7. **Offer expiry**  
   Scheduled job or per-offer timer marks `EXPIRED`; **only** driver with valid `offerToken` may accept (JWT with ride + expiry).

8. **Split fare**  
   **Two-phase**: reserve splits in ledger; if one leg fails, **compensating** void or retry queue.

9. **Read-your-writes**  
   Rider app polls ride state with `If-None-Match` or subscribes via WebSocket after create.

### What to say in interview
*"I serialize assignment on the driver record with an atomic update—if two rides try to grab the same driver, only one UPDATE succeeds. Location updates bypass the OLTP lock and flow through a stream; matching reads a eventually consistent geo index that's refreshed every second. Money moves through an outbox so capture retries don't double-charge."*

---

## 📊 Step 9: Database Schema (14–20 minutes)

### Core tables (relational sketch)

**users** (`user_id`, `phone_hash`, `email`, `created_at`, …)

**riders** (`rider_id`, `user_id`, `default_payment_instrument_id`, `rating_avg`, `trip_count`)

**drivers** (`driver_id`, `user_id`, `status`, `current_ride_id` NULL UNIQUE allowed, `rating_avg`, `acceptance_rate`, `lat`, `lng`, `geohash`, `updated_at`, `version`)

**vehicles** (`vehicle_id`, `driver_id`, `seat_capacity`, `luxury_flag`, `plate`, …)

**rides** (`ride_id`, `rider_id`, `driver_id`, `region_id`, `product`, `status`, `pickup_lat`, `pickup_lng`, `dest_lat`, `dest_lng`, `surge_at_booking`, `fare_quote_id`, `version`, `requested_at`, `completed_at`)

**ride_offers** (`offer_id`, `ride_id`, `driver_id`, `status`, `score`, `expires_at`, `created_at`)

**route_snapshots** (`snapshot_id`, `ride_id`, `phase` [TO_PICKUP|ON_TRIP], `distance_m`, `duration_s`, `polyline`, `provider`, `created_at`)

**fare_breakdowns** (`ride_id` PK, `currency`, `base_cents`, `distance_cents`, `time_cents`, `surge_cents`, `tolls_cents`, `fee_cents`, `total_cents`, `json_detail`)

**payment_intents** (`intent_id`, `ride_id`, `rider_id`, `provider_ref`, `status`, `authorized_cents`, `captured_cents`, `idempotency_key` UNIQUE)

**split_fare_shares** (`ride_id`, `rider_id`, `share_cents`, `status`)

**ratings** (`rating_id`, `ride_id`, `from_user_id`, `to_user_id`, `role_from`, `stars`, `tags_json`, `created_at`, UNIQUE(`ride_id`, `role_from`))

**geo_tile_metrics** (`tile_id`, `region_id`, `open_requests`, `available_drivers`, `surge_multiplier`, `updated_at`)

**location_updates** — time-series or columnar store (`driver_id`, `ts`, `lat`, `lng`, `ride_id` nullable) — partition by day

**notification_outbox** (`id`, `user_id`, `dedupe_key` UNIQUE, `channel`, `template`, `payload_json`, `status`, `created_at`)

**cancellation_fee_rules** (`region_id`, `ride_status`, `window_sec`, `fee_cents`, `role`)

**idempotency_keys** (`key` PK, `response_body`, `created_at`)

### Helpful indexes
- `drivers(geohash, status)` WHERE `status='AVAILABLE'`
- `rides(rider_id, requested_at DESC)`
- `rides(driver_id, status)`
- `ride_offers(ride_id, status)`
- `location_updates(driver_id, ts DESC)` BRIN or time-partition

---

## 💡 Step 10: Interview Discussion Points (18–28 minutes)

### 1. Driver matching: batch vs sequential offer
**Sequential** to top-K reduces driver annoyance but may increase time-to-match; **small parallel broadcast** (2–3 drivers) improves speed but can cause **race** on accept—mitigate with **first-accept-wins** CAS on driver.

### 2. ETA as primary signal
Road-network ETA beats Euclidean in urban canyons; cache **routing** by quantized endpoints to cut API spend.

### 3. Geohash vs H3
**Geohash** is simple and prefix-friendly; **H3** gives uniform hex cells and better **neighbor** symmetry for surge—Uber-scale systems often prefer **H3**.

### 4. Surge ethics & UX
Cap multipliers; show **upfront** inclusive price; **sticky** surge for committed riders short window to avoid bait-and-switch perception.

### 5. UberPool routing
**Insertion heuristic**: try inserting new pickup/drop between each adjacent pair; reject if **detour minutes** > threshold for existing riders.

### 6. Dynamic pricing beyond surge
**Time-of-day** base, **event** overlays (stadium), **weather** API multipliers—keep in **config service**.

### 7. Cancellation economics
Rider fee after driver **en route** compensates opportunity cost; waive on **first cancel** monthly if product policy.

### 8. Fraud patterns
Stolen cards → strong **authorize** on request; collusion driver-rider → graph features (out of scope detail).

### 9. Cold start / rural
Sparse drivers: widen **search radius** and **increase wait** SLA; show honest **ETA range**.

### 10. Fairness & bias
Periodic **rebalancing** so same drivers are not always closest; **earnings** fairness vs **latency** tradeoff.

### 11. Real-time map scale
**WebSocket** per trip room; **snapshot + delta** updates; **disconnect** replay from last sequence.

### 12. Testing matching
Property: assigned driver **must** satisfy `canAccept(product)`; simulation with **Poisson** ride generation.

---

## ✅ Step 11: SOLID Principles Verification

### Single Responsibility
- `DriverMatchingService` orchestrates offer lifecycle only  
- `SurgePricingService` owns multiplier math only  
- `FareCalculator` owns pricing components only  

### Open/Closed
- New product **Uber Green**: extend `ProductType` + rate card row, no change to matching CAS  

### Liskov Substitution
- Any `RouteService` implementation must return non-negative duration/distance  

### Interface Segregation
- Split `PaymentOrchestrator` from `PayoutService` (driver side)  

### Dependency Inversion
- Core domain depends on `RouteService`, `MatchingStrategy` interfaces—not concrete HTTP clients  

---

## 📈 Step 12: Complexity Analysis

| Operation | Time | Notes |
|-----------|------|-------|
| Geohash encode point | O(precision) | Tiny constant |
| Nearby driver candidate fetch | O(K + D_cell) | K neighbors; D_cell drivers in cells |
| Rank M candidates with routing | O(M) × **R** | **R** = routing call cost; batch/matrix APIs help |
| Surge refresh one tile | O(1) | Counter read + formula |
| Assign driver (CAS) | O(1) | Single indexed UPDATE |
| Location ingest (amortized) | O(1) | Append stream + update in-memory cell set |
| Pool insertion try all edges | O(n) per new rider | n = stops; small constant in practice |
| Ride history page | O(log T + P) | Index by rider + cursor pagination |

**Scaling mantra:** **Partition by region**; **separate hot paths** (location, matching, payments); **never** compute global surge in one lock.

---

## 🎓 Step 13: Key Takeaways

1. **Matching is a geo + ETA optimization** under **business constraints** (product, fairness, compliance)—not just nearest neighbor.
2. **Geohash / H3** enables **O(cell-size)** candidate generation; always include **neighbor cells** to fix boundary bias.
3. **Surge** is a **control loop** on **supply/demand signals** per tile—smooth, cap, and audit overrides.
4. **Driver assignment** requires **atomic** claims (`current_ride_id`) to prevent **double booking**.
5. **Fare** should separate **quote**, **surge version**, and **settlement** for dispute clarity; **split fare** needs **sum invariants** and idempotent partial captures.
6. **Real-time** is **async fan-out**; **OLTP** stays small: state transitions + payments.
7. **Cancellations** are a **policy engine** tied to ride state and timestamps—not ad-hoc `if` chains in controllers.
8. **Route optimization** for Pool is a **constrained ordering** problem; production uses **heuristics** + live re-evaluation.

**Interview success formula:** Clarify products → ride state machine → geo index + ETA ranking → atomic assignment → surge formula → fare + split → cancellation fees → notifications → scale partitions.

---

**Uber Ride-Sharing Platform LLD — Hard difficulty — ready for review.**
