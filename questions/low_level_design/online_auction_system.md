# Low-Level Design: Online Auction System

**Difficulty:** Hard 🔥

**Interview Duration:** 75-90 minutes

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

## 🎯 Step 1: Understand the Problem (5–7 minutes)

### What the Interviewer Says:
*"Design a low-level design for an online auction platform (similar to eBay-style auctions) that supports multiple auction formats, bidding rules, payments, discovery, and trust/safety controls."*

### Clarifying Questions to Ask:

1. **Q:** Which auction types must we support in scope?  
   **A:** English (ascending), Dutch (descending), sealed-bid, and Vickrey (second-price sealed).

2. **Q:** How should proxy / auto-bidding work?  
   **A:** Users set a maximum bid; the system should raise bids automatically up to that max in minimum increments.

3. **Q:** What defines auction start and end?  
   **A:** Scheduled start, live countdown, and automatic close at end time—with possible extensions (anti-snipe).

4. **Q:** How are winners determined and paid?  
   **A:** Rules differ by auction type; payment includes a deposit or authorization hold and a final capture/settlement step.

5. **Q:** What listing content do we model?  
   **A:** Title, rich description, categories, multiple photos, condition, shipping options (high level).

6. **Q:** Real-time expectations?  
   **A:** Sub-second propagation of outbid events to watchers; scalable fan-out.

7. **Q:** Fraud and abuse?  
   **A:** Velocity checks, device/IP risk, shill-bid signals, payment instrument verification—not full ML in LLD, but hooks and rules.

8. **Q:** Search and recommendations?  
   **A:** Keyword/category browse plus personalized or similarity-based recommendations.

9. **Q:** Consistency vs. availability for bids?  
   **A:** Bids on a single auction must be strongly ordered; read paths can be eventually consistent for catalogs.

10. **Q:** Currency and geography?  
    **A:** Single currency per auction for simplicity; multi-currency as extension.

---

## 🔹 Step 2: Gather Requirements (7–10 minutes)

### Functional Requirements

#### Auction formats (FR1–FR4)
1. **English auction:** Open ascending bids; current price is second-highest plus increment (or reserve logic); highest eligible bidder wins at final clearing price.
2. **Dutch auction:** Price decreases from a high starting point on a timer; first acceptance wins at the current price.
3. **Sealed-bid auction:** Bidders submit private bids before deadline; highest valid bid wins at their bid amount (or first-price variant—document assumption).
4. **Vickrey auction:** Sealed bids; highest bidder wins but pays the second-highest bid (second-price sealed).

#### Listings & catalog (FR5–FR10)
5. Sellers create **listings** with title, description, attributes, and **category** (tree or faceted).
6. Listings support **multiple images** with ordering and optional primary image.
7. Listings can be linked to one active **auction** at a time (lifecycle rules).
8. Buyers can **watch** listings/auctions for notifications.
9. System supports **search** (keyword, filters, sort) over listings and live auctions.
10. System provides **recommendations** (e.g., similar category, co-view/co-bid signals).

#### Bidding & automation (FR11–FR16)
11. Authenticated users **place bids** subject to eligibility (verified account, payment method on file if required).
12. **Minimum bid increment** enforced per auction configuration.
13. **Proxy bidding:** user sets `maxBid`; engine computes minimum necessary current bid against competing maxima.
14. **Auto-bidding** engine runs on each competing bid to update displayed current price without exposing hidden maxima.
15. Reject bids that violate **reserve price** visibility rules (seller sees reserve; bidders may only see “reserve not met”).
16. **Bid history** for English (visible increments); sealed types hide others’ bids until close.

#### Lifecycle & time (FR17–FR22)
17. Auctions transition: `DRAFT` → `SCHEDULED` → `LIVE` → `ENDED` → `SETTLED` (with cancel paths).
18. **Scheduled start:** auction becomes `LIVE` at `startTime` (job or stream-driven).
19. **Countdown timer** driven by authoritative server clock; clients display synced remaining time.
20. **Auto-close** at `endTime`; no further bids accepted after hard close (subject to extension).
21. **Anti-snipe / soft close:** if bid arrives within window (e.g., last 2 minutes), extend end time by extension slice (cap total extension).
22. **Dutch price steps:** periodic decrements until accept or floor price.

#### Winner & settlement (FR23–FR28)
23. **Winner determination** delegated to type-specific policy after close.
24. **English:** winner = highest eligible bidder; clearing price from second-price proxy resolution or last increment rules.
25. **Dutch:** winner = first accepter at current price.
26. **Sealed / Vickrey:** open bids at deadline; apply tie-break (earlier timestamp, lexicographic bid id).
27. **Deposit / hold:** authorize or capture deposit at bid placement or at win (configurable per marketplace).
28. **Final payment:** charge winner; release holds for non-winners; payout seller minus fees (orchestrated payment state machine).

#### Notifications & realtime (FR29–FR31)
29. Push **real-time bid updates** to auction channel subscribers (WebSocket/SSE/long-poll abstraction).
30. Notify **outbid**, **auction ending soon**, **won/lost**, **payment due** events via in-app + email/SMS adapters.
31. Rate-limit notifications per user to prevent abuse.

#### Trust, safety, compliance (FR32–FR36)
32. **Fraud detection** hooks: velocity (bids/min), new account risk, IP/device clustering, payment declines.
33. **Shill bidding** signals: same device as seller, circular bidding patterns (heuristic flags).
34. Block or step-up verification when risk score exceeds threshold.
35. **Audit log** for bid placement, auction state changes, and admin overrides.
36. GDPR-style **data minimization** for bid logs (extension: retention policies).

### Non-Functional Requirements — Systematic Deduction (SCAMPS)

#### 1. Scalability
- Peak: thousands of concurrent live auctions, 10k+ bid attempts/sec globally.
- Horizontal scale for **read-heavy** catalog and search; **partition** hot auctions by `auctionId`.
- Fan-out for notifications via message bus; connection tier for WebSockets.

#### 2. Consistency
- **Strong ordering** of bids per auction (total order); use single leader per auction partition or optimistic concurrency with retry.
- **Exactly-once semantics** for money movements: idempotent `paymentIntentId` / bid idempotency keys.
- Catalog search can be **eventually consistent**.

#### 3. Availability
- Target 99.9% for browse; bidding path degrades gracefully (queue bid during brief outages with clear UX risk trade-off—usually fail fast).
- Leader failure: elect new writer or return “try again” for bid API.

#### 4. Maintainability
- **Strategy** per auction type for pricing, winner resolution, and validation.
- Feature flags for anti-snipe parameters, deposit rules, recommendation sources.

#### 5. Performance
- Place bid: p99 < 200 ms internal processing on hot path (excluding external payment networks).
- Search p99 < 300 ms for typical queries (indexed).
- Real-time broadcast: < 1 s to subscribers under normal load.

#### 6. Security
- AuthN/AuthZ for all mutations; seller cannot bid on own auction (enforced server-side).
- Encrypt PII; signed webhooks; rate limits on bid and login endpoints.

---

## 🧩 Step 3: Identify Core Entities (10–12 minutes)

### Noun Extraction (selected)

| Requirement fragment | Candidate nouns |
|----------------------|-----------------|
| English / Dutch / sealed / Vickrey | AuctionType, AuctionRule |
| Listing, photos, category | Listing, MediaAsset, Category |
| Place bid, proxy | Bid, ProxyBid, BidIncrementPolicy |
| Scheduled start, countdown | AuctionSchedule, AuctionClock |
| Winner, payment | AuctionResult, PaymentIntent, Settlement |
| Notifications | Notification, Subscription |
| Anti-snipe | AntiSnipePolicy |
| Fraud | FraudSignal, RiskAssessment |
| Search / recommendations | SearchQuery, RecommendationProfile |

### Entity Validation (abbreviated)

| Noun | Entity? | Reason |
|------|---------|--------|
| Money | ❌ | Value object (amount + currency) |
| Bid | ✅ | Core aggregate with lifecycle |
| Auction | ✅ | Root aggregate; type-specific behavior |
| Listing | ✅ | Seller-owned catalog aggregate |
| ProxyBid | ✅ | Stored intent driving auto-bids |
| AntiSnipePolicy | ✅ | Value object or attached policy |
| WebSocket | ❌ | Infrastructure; wrap in `RealtimeChannel` |

### Final Entity List (high level)

**Identity & parties:** `User`, `Account`, `SellerProfile`, `BuyerProfile`

**Catalog:** `Listing`, `Category`, `MediaAsset`, `ListingAttribute`

**Auction core:** `Auction` (aggregate root), `AuctionType` (enum), `AuctionStatus` (enum), `ReservePrice`, `BidIncrementPolicy`, `AuctionSchedule`

**Bidding:** `Bid`, `ProxyBidSettings`, `BidPlacementResult`, `BidVisibility` (enum: OPEN, SEALED)

**Resolution & money:** `WinnerResolver` (strategy interface), `AuctionOutcome`, `PaymentIntent`, `EscrowOrHoldRecord`, `SettlementRecord`

**Realtime & engagement:** `AuctionSubscription`, `NotificationEvent`, `WatchlistEntry`

**Trust:** `FraudCheckRequest`, `RiskScore`, `AuditLogEntry`

**Discovery:** `SearchIndexDocument`, `RecommendationCandidate`

---

## 🔗 Step 4: Establish Relationships (12–15 minutes)

### Pass 1: Core associations

- **Seller 1—N Listings:** a seller owns many listings (`User`/`SellerProfile` → `Listing`).
- **Listing 1—0..1 Auction:** at most one active auction per listing (business rule; historical auctions 1—N).
- **Auction 1—N Bids:** bids belong to one auction; immutable append-only log ideal for audit.
- **User 1—N Bids:** bidder places many bids over time.
- **Auction 1—1 AuctionSchedule:** start/end, extensions tracked on schedule or auction row.

### Pass 2: Composition vs aggregation

- **Listing ◆— MediaAsset:** photos owned by listing; delete with listing (composition).
- **Auction ◇— PaymentIntent:** payment intents may outlive auction; aggregation with reference ids.

### Pass 3: Strategy attachment

- **Auction —► AuctionTypeStrategy:** resolves behavior without subclass explosion on `Auction` (optional: subclass per type).

### Cardinality summary

| Relationship | Cardinality | Notes |
|--------------|-------------|--------|
| Seller → Listing | 1:N | |
| Listing → Auction (active) | 1:0..1 | Historical: 1:N |
| Auction → Bid | 1:N | Ordered by time |
| User → ProxyBidSettings | 1:0..1 | Per auction |
| Auction → AuctionSubscription | 1:N | Many watchers |
| User → WatchlistEntry | 1:N | |

---

## 📐 Step 5: Design Class Diagrams (12–18 minutes)

### Class Diagram 1: Enums & value objects

```
┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>    │
│ AuctionType        │  │ AuctionStatus      │  │ BidVisibility      │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│ ENGLISH            │  │ DRAFT              │  │ OPEN (English/Dutch)│
│ DUTCH              │  │ SCHEDULED          │  │ SEALED             │
│ SEALED_FIRST_PRICE │  │ LIVE               │  └────────────────────┘
│ VICKREY            │  │ ENDED              │
└────────────────────┘  │ SETTLED            │
                        │ CANCELLED          │
                        └────────────────────┘

┌────────────────────┐
│ Money              │
├────────────────────┤
│ - amount: BigDecimal│
│ - currency: String  │
├────────────────────┤
│ + add(Money): Money │
│ + compareTo(): int  │
└────────────────────┘
```

### Class Diagram 2: Listing & media

```
┌─────────────────────────────────────────────────────────────┐
│ Listing                                                     │
├─────────────────────────────────────────────────────────────┤
│ - listingId: String                                         │
│ - sellerId: String                                          │
│ - title: String                                             │
│ - description: String                                       │
│ - categoryId: String                                        │
│ - attributes: Map<String,String>                            │
│ - condition: String                                         │
│ - createdAt: Instant                                        │
├─────────────────────────────────────────────────────────────┤
│ + addMedia(MediaAsset): void                                │
│ + getPrimaryMedia(): MediaAsset                             │
└─────────────────────────────────────────────────────────────┘
        │ 1
        │ ◆─────────────────────────────── *
        ▼
┌─────────────────────────────────────────────────────────────┐
│ MediaAsset                                                  │
├─────────────────────────────────────────────────────────────┤
│ - assetId: String                                           │
│ - url: String                                               │
│ - sortOrder: int                                            │
│ - isPrimary: boolean                                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ Category                                                    │
├─────────────────────────────────────────────────────────────┤
│ - categoryId: String                                        │
│ - name: String                                              │
│ - parentId: String                                          │
└─────────────────────────────────────────────────────────────┘
```

### Class Diagram 3: Auction aggregate (conceptual)

```
┌─────────────────────────────────────────────────────────────┐
│ Auction                                                     │
├─────────────────────────────────────────────────────────────┤
│ - auctionId: String                                         │
│ - listingId: String                                         │
│ - type: AuctionType                                         │
│ - status: AuctionStatus                                     │
│ - visibility: BidVisibility                                 │
│ - schedule: AuctionSchedule                                 │
│ - startPrice: Money                                         │
│ - reservePrice: Money (optional)                            │
│ - buyNowPrice: Money (optional, extension)                  │
│ - incrementPolicy: BidIncrementPolicy                       │
│ - antiSnipePolicy: AntiSnipePolicy                          │
│ - currentPrice: Money                                       │
│ - dutchCurrentOffer: Money (Dutch only)                     │
│ - version: long  (optimistic lock)                          │
├─────────────────────────────────────────────────────────────┤
│ + open(): void                                              │
│ + close(): void                                             │
│ + acceptDutchOffer(userId): BidPlacementResult              │
└─────────────────────────────────────────────────────────────┘
           │
           │ uses
           ▼
┌─────────────────────────────────────────────────────────────┐
│ <<interface>>                                               │
│ AuctionTypeStrategy                                         │
├─────────────────────────────────────────────────────────────┤
│ + validateBid(auction, bid, ctx): ValidationResult        │
│ + onBidAccepted(auction, bid): void                         │
│ + resolveWinner(auction, bids): AuctionOutcome              │
└─────────────────────────────────────────────────────────────┘
        △               △                △               △
        │               │                │               │
   ┌────┴───┐    ┌──────┴────┐   ┌───────┴────┐   ┌─────┴─────┐
   │English │    │ Dutch     │   │ Sealed    │   │ Vickrey   │
   │Strategy│   │ Strategy  │   │ Strategy  │   │ Strategy  │
   └────────┘    └───────────┘   └───────────┘   └───────────┘
```

### Class Diagram 4: Bidding & proxy

```
┌─────────────────────────────────────────────────────────────┐
│ Bid                                                         │
├─────────────────────────────────────────────────────────────┤
│ - bidId: String                                             │
│ - auctionId: String                                         │
│ - bidderId: String                                          │
│ - amount: Money                                             │
│ - placedAt: Instant                                         │
│ - clientRequestId: String (idempotency)                     │
│ - status: BidStatus (ACCEPTED,REJECTED,OUTBID,WINNING)       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ ProxyBidSettings                                            │
├─────────────────────────────────────────────────────────────┤
│ - userId: String                                            │
│ - auctionId: String                                         │
│ - maxAmount: Money                                          │
│ - updatedAt: Instant                                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ BidService                                                  │
├─────────────────────────────────────────────────────────────┤
│ - auctions: AuctionRepository                               │
│ - bids: BidRepository                                       │
│ - proxy: ProxyBidEngine                                     │
│ - fraud: FraudDetectionService                              │
│ - payments: PaymentService                                  │
│ - clock: Clock                                                │
│ - notifier: NotificationService                             │
├─────────────────────────────────────────────────────────────┤
│ + placeBid(cmd: PlaceBidCommand): BidPlacementResult        │
└─────────────────────────────────────────────────────────────┘
        │ uses
        ▼
┌─────────────────────────────────────────────────────────────┐
│ ProxyBidEngine                                              │
├─────────────────────────────────────────────────────────────┤
│ + reconcileEnglish(auction, incomingBid, proxies): Money    │
└─────────────────────────────────────────────────────────────┘
```

### Class Diagram 5: Payments & notifications

```
┌─────────────────────────────────────────────────────────────┐
│ PaymentService                                              │
├─────────────────────────────────────────────────────────────┤
│ + authorizeDeposit(user, amount): PaymentIntent             │
│ + captureFinal(intentId, amount): SettlementRecord          │
│ + releaseHold(intentId): void                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ NotificationService                                         │
├─────────────────────────────────────────────────────────────┤
│ + publishAuctionEvent(topicId, event): void                 │
│ + notifyUser(userId, template, payload): void                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ FraudDetectionService                                       │
├─────────────────────────────────────────────────────────────┤
│ + assessBid(context: BidFraudContext): RiskScore            │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25–35 minutes)

### Enums and value objects

```java
// AuctionType.java
public enum AuctionType {
    ENGLISH,
    DUTCH,
    SEALED_FIRST_PRICE,
    VICKREY
}

// AuctionStatus.java
public enum AuctionStatus {
    DRAFT,
    SCHEDULED,
    LIVE,
    ENDED,
    SETTLED,
    CANCELLED
}

// BidVisibility.java
public enum BidVisibility {
    OPEN,   // English, Dutch (price public)
    SEALED  // Sealed, Vickrey until close
}
```

```java
// Money.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public int compareTo(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
```

### Anti-snipe policy

```java
// AntiSnipePolicy.java
import java.time.Duration;
import java.time.Instant;

public class AntiSnipePolicy {
    private final Duration triggerWindow;   // e.g., last 2 minutes
    private final Duration extendBy;        // e.g., +2 minutes
    private final Duration maxExtension;    // cap from original end

    public AntiSnipePolicy(Duration triggerWindow, Duration extendBy, Duration maxExtension) {
        this.triggerWindow = triggerWindow;
        this.extendBy = extendBy;
        this.maxExtension = maxExtension;
    }

    /**
     * If a bid arrives within triggerWindow of current end, extend end time
     * (until cap measured from scheduled original end — simplified here).
     */
    public Instant maybeExtendEnd(Instant now, Instant currentEnd, Instant originalScheduledEnd) {
        Duration untilEnd = Duration.between(now, currentEnd);
        if (untilEnd.compareTo(triggerWindow) > 0) {
            return currentEnd;
        }
        Instant proposed = currentEnd.plus(extendBy);
        Instant cap = originalScheduledEnd.plus(maxExtension);
        return proposed.isAfter(cap) ? cap : proposed;
    }
}
```

### Proxy bid engine (English auction)

```java
// ProxyBidEngine.java — simplified second-highest + increment resolution
import java.util.*;

public class ProxyBidEngine {

    public static final class BidderMax {
        public final String userId;
        public final Money max;

        public BidderMax(String userId, Money max) {
            this.userId = userId;
            this.max = max;
        }
    }

    /**
     * Given incoming bid with explicit amount and user's max (proxy),
     * compute new current price and winning user for English auction.
     * Stores only necessary visible bids in production; this is interview-core logic.
     */
    public EnglishResolution resolveEnglish(
            Money currentPrice,
            String currentLeaderId,
            Map<String, Money> maxByUser,
            String incomingUser,
            Money incomingDisplayedBid,
            Money incomingMax,
            BidIncrementPolicy increments
    ) {
        Map<String, Money> maxes = new HashMap<>(maxByUser);
        maxes.put(incomingUser, incomingMax);

        // Determine top two maxima
        List<BidderMax> sorted = new ArrayList<>();
        for (var e : maxes.entrySet()) {
            sorted.add(new BidderMax(e.getKey(), e.getValue()));
        }
        sorted.sort(Comparator.comparing(b -> b.max, Comparator.naturalOrder()));
        Collections.reverse(sorted);

        if (sorted.isEmpty()) {
            throw new IllegalStateException("No bidders");
        }

        BidderMax highest = sorted.get(0);
        Money secondMax = sorted.size() > 1 ? sorted.get(1).max : currentPrice;

        Money minimumToLead = increments.nextBidAtOrAbove(secondMax);
        if (minimumToLead.compareTo(highest.max) > 0) {
            // Nobody can lead — edge case with low max; reject in outer service
            return new EnglishResolution(null, currentPrice, false);
        }

        Money newCurrent = minimumToLead;
        // If incoming cannot beat previous leader's max, they lose
        Money incomingUserMax = incomingMax;
        if (incomingUserMax.compareTo(newCurrent) < 0) {
            return new EnglishResolution(currentLeaderId, currentPrice, false);
        }

        // Winner is highest max; current shown price is min(increment above second, highest max)
        Money display = newCurrent;
        if (display.compareTo(highest.max) > 0) {
            display = highest.max;
        }

        return new EnglishResolution(highest.userId, display, true);
    }

    public record EnglishResolution(String leaderId, Money newCurrentPrice, boolean accepted) {}
}
```

```java
// BidIncrementPolicy.java
import java.math.BigDecimal;

public class BidIncrementPolicy {
    public Money nextBidAtOrAbove(Money floor) {
        BigDecimal f = floor.getAmount();
        BigDecimal step;
        if (f.compareTo(new BigDecimal("100")) < 0) step = new BigDecimal("5");
        else if (f.compareTo(new BigDecimal("500")) < 0) step = new BigDecimal("10");
        else step = new BigDecimal("25");
        return new Money(f.add(step), floor.getCurrency());
    }
}
```

### Auction schedule & lifecycle

```java
// AuctionSchedule.java
import java.time.Instant;

public class AuctionSchedule {
    private Instant startTime;
    private Instant endTime;
    private Instant originalEndTime;

    public AuctionSchedule(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.originalEndTime = endTime;
    }

    public boolean shouldOpen(Instant now) {
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }

    public boolean isPastEnd(Instant now) {
        return !now.isBefore(endTime);
    }

    public void extendEnd(Instant newEnd) {
        this.endTime = newEnd;
    }

    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Instant getOriginalEndTime() { return originalEndTime; }
}
```

### Vickrey & sealed resolution (at ENDED)

```java
// AuctionOutcome.java
import java.util.Optional;

public record AuctionOutcome(
        Optional<String> winnerId,
        Money clearingPrice,
        AuctionType type
) {
    public static AuctionOutcome noSale() {
        return new AuctionOutcome(Optional.empty(), null, null);
    }
}
```

```java
// VickreyWinnerResolver.java
import java.util.*;

public class VickreyWinnerResolver {

    public AuctionOutcome resolve(List<Bid> sealedBids) {
        List<Bid> sorted = new ArrayList<>(sealedBids);
        sorted.sort(
                Comparator.comparing(Bid::getAmount, Comparator.naturalOrder()).reversed()
                        .thenComparing(Bid::getPlacedAt)
        );
        if (sorted.isEmpty()) {
            return AuctionOutcome.noSale();
        }
        Bid highest = sorted.get(0);
        Money clearing;
        if (sorted.size() == 1) {
            clearing = highest.getAmount(); // common rule: pay own bid if only one
        } else {
            clearing = sorted.get(1).getAmount(); // second-highest
        }
        return new AuctionOutcome(Optional.of(highest.getBidderId()), clearing, AuctionType.VICKREY);
    }
}

// SealedFirstPriceResolver.java
public class SealedFirstPriceResolver {
    public AuctionOutcome resolve(List<Bid> sealedBids) {
        return sealedBids.stream()
                .max(Comparator.comparing(Bid::getAmount).thenComparing(Bid::getPlacedAt))
                .map(b -> new AuctionOutcome(Optional.of(b.getBidderId()), b.getAmount(), AuctionType.SEALED_FIRST_PRICE))
                .orElse(AuctionOutcome.noSale());
    }
}
```

### Dutch auction tick (sketch)

```java
// DutchAuctionScheduler.java — conceptual
import java.time.*;
import java.util.concurrent.*;

public class DutchAuctionScheduler {
    private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(4);

    public void startDutchDecrement(Auction auction, Duration interval, Money step, Runnable onPriceChange) {
        exec.scheduleAtFixedRate(() -> {
            // In real system: load latest from DB with lock
            Money next = auction.getDutchCurrentOffer().getAmount()
                    .compareTo(step.getAmount()) <= 0
                    ? new Money(step.getAmount(), step.getCurrency()) // floor
                    : new Money(
                        auction.getDutchCurrentOffer().getAmount().subtract(step.getAmount()),
                        step.getCurrency()
                    );
            auction.setDutchCurrentOffer(next);
            onPriceChange.run();
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
    }
}
```

### Bid service orchestration (place bid)

```java
// PlaceBidCommand.java
public record PlaceBidCommand(
        String auctionId,
        String bidderId,
        Money amount,
        Money maxProxyAmount,
        String idempotencyKey,
        Instant clientTime
) {}

// BidPlacementResult.java
public record BidPlacementResult(
        boolean accepted,
        String reasonCode,
        Money currentPrice,
        String currentLeaderId
) {}
```

```java
// BidService.java (core flow — pseudocode-level for interview)
public class BidService {
    private final AuctionRepository auctions;
    private final BidRepository bids;
    private final ProxyBidEngine proxyEngine;
    private final FraudDetectionService fraud;
    private final AntiSnipePolicy antiSnipe;
    private final NotificationService notifier;
    private final Clock clock;

    public BidPlacementResult placeBid(PlaceBidCommand cmd) {
        Auction auction = auctions.findByIdForUpdate(cmd.auctionId());
        Instant now = clock.instant();

        if (auction.getStatus() != AuctionStatus.LIVE) {
            return new BidPlacementResult(false, "NOT_LIVE", auction.getCurrentPrice(), null);
        }
        if (auction.getSchedule().isPastEnd(now)) {
            return new BidPlacementResult(false, "ENDED", auction.getCurrentPrice(), null);
        }

        RiskScore risk = fraud.assessBid(new BidFraudContext(cmd.bidderId(), cmd.auctionId(), cmd.clientTime()));
        if (risk.isBlock()) {
            return new BidPlacementResult(false, "FRAUD_BLOCKED", auction.getCurrentPrice(), null);
        }

        if (cmd.bidderId().equals(auction.getSellerId())) {
            return new BidPlacementResult(false, "SELLER_CANNOT_BID", auction.getCurrentPrice(), null);
        }

        switch (auction.getType()) {
            case ENGLISH -> {
                Map<String, Money> maxByUser = bids.loadMaxProxyByAuction(cmd.auctionId());
                var res = proxyEngine.resolveEnglish(
                        auction.getCurrentPrice(),
                        auction.getCurrentLeaderId(),
                        maxByUser,
                        cmd.bidderId(),
                        cmd.amount(),
                        cmd.maxProxyAmount() != null ? cmd.maxProxyAmount() : cmd.amount(),
                        auction.getIncrementPolicy()
                );
                if (!res.accepted()) {
                    return new BidPlacementResult(false, "OUTBID", auction.getCurrentPrice(), auction.getCurrentLeaderId());
                }
                auction.setCurrentPrice(res.newCurrentPrice());
                auction.setCurrentLeaderId(res.leaderId());
                auction.bumpVersion();

                Instant newEnd = antiSnipe.maybeExtendEnd(now, auction.getSchedule().getEndTime(), auction.getSchedule().getOriginalEndTime());
                auction.getSchedule().extendEnd(newEnd);

                bids.save(new Bid(cmd, res.newCurrentPrice(), BidStatus.ACCEPTED));
                notifier.publishAuctionEvent(cmd.auctionId(), new OutbidEvent(res.leaderId(), res.newCurrentPrice()));
                auctions.save(auction);
                return new BidPlacementResult(true, "OK", res.newCurrentPrice(), res.leaderId());
            }
            case SEALED_FIRST_PRICE, VICKREY -> {
                // Only store sealed bid; no public price change
                bids.saveSealed(new Bid(cmd, cmd.amount(), BidStatus.SEALED));
                return new BidPlacementResult(true, "SEALED_RECORDED", auction.getCurrentPrice(), null);
            }
            case DUTCH -> {
                return new BidPlacementResult(false, "USE_ACCEPT_ENDPOINT", auction.getDutchCurrentOffer(), null);
            }
            default -> throw new IllegalStateException();
        }
    }
}
```

### Real-time notification fan-out (interface)

```java
// NotificationService.java
public interface NotificationService {
    void publishAuctionEvent(String auctionId, AuctionRealtimeEvent event);
}

public sealed interface AuctionRealtimeEvent permits OutbidEvent, AuctionExtendedEvent, AuctionEndedEvent {}

public record OutbidEvent(String leaderId, Money currentPrice) implements AuctionRealtimeEvent {}
public record AuctionExtendedEvent(Instant newEnd) implements AuctionRealtimeEvent {}
public record AuctionEndedEvent(String winnerId, Money clearingPrice) implements AuctionRealtimeEvent {}
```

### Search & recommendations (sketch)

```java
// SearchService.java
public class SearchService {
    public SearchResult search(SearchQuery q) {
        // Backed by inverted index (Elasticsearch/OpenSearch) + filters on category, price band, auction status
        return SearchResult.empty(); // interview: describe indexing listing + denormalized auction fields
    }
}

// RecommendationService.java
public class RecommendationService {
    public List<Listing> similarTo(String listingId, int k) {
        // Content-based: same category + embedding similarity; or co-view graph
        return List.of();
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `AuctionTypeStrategy` per English/Dutch/Sealed/Vickrey | Swap auction rules without massive conditionals |
| **State** | `AuctionStatus` transitions (`LIVE` → `ENDED`) | Enforce valid lifecycle |
| **Template method** | Optional: shared `AbstractAuctionService` with type hooks | Reuse validation skeleton |
| **Observer / Pub-Sub** | `NotificationService`, WebSocket bridge | Decouple bidding core from fan-out |
| **Repository** | `AuctionRepository`, `BidRepository` | Persistence abstraction |
| **Idempotent command** | `clientRequestId` / idempotency key on `placeBid` | Safe retries from mobile clients |
| **Saga / process manager** | Settlement: deposit → capture → payout | Long-running payment consistency |

---

## 🔒 Step 8: Concurrency Handling (10–12 minutes)

### Problem
Many concurrent `placeBid` calls on the same hot auction must not corrupt `currentPrice`, leader, or end time.

### Techniques
1. **Pessimistic:** `SELECT ... FOR UPDATE` on `auction` row for duration of bid transaction.
2. **Optimistic:** `version` column on `Auction`; on conflict, bounded retry with backoff (good for lower contention).
3. **Partition single writer:** Route all bids for `auctionId` to single queue consumer (Kafka partition key = `auctionId`) — strong ordering, horizontal scale across auctions.
4. **Sealed bids:** Append-only insert with unique `(auctionId, bidderId)` if one bid per user allowed.

### English auction specifics
- Re-read `maxProxy` map under same lock as auction row.
- Publish notification **after** commit to avoid ghost updates.

### Dutch accept
- Use `compare-and-set` on `status` to `ENDED` for first accepter only.

### Anti-snipe
- Recompute `endTime` inside same transaction as winning bid update.

---

## 📊 Step 9: Database Schema (10–12 minutes)

### Core tables (relational sketch)

**users** (`user_id`, email, status, …)

**listings** (`listing_id`, `seller_id`, `title`, `description`, `category_id`, `created_at`)

**listing_media** (`asset_id`, `listing_id`, `url`, `sort_order`, `is_primary`)

**categories** (`category_id`, `name`, `parent_id`)

**auctions** (`auction_id`, `listing_id`, `seller_id`, `type`, `status`, `visibility`, `start_time`, `end_time`, `original_end_time`, `start_price`, `reserve_price`, `current_price`, `current_leader_id`, `dutch_current_price`, `version`, `created_at`)

**bids** (`bid_id`, `auction_id`, `bidder_id`, `amount`, `currency`, `placed_at`, `status`, `idempotency_key`, `visible` BOOLEAN)  
- For sealed: `visible = false` until close; optional encrypt `amount` at rest.

**proxy_bids** (`auction_id`, `user_id`, `max_amount`, `updated_at`) UNIQUE(`auction_id`,`user_id`)

**payments** (`payment_id`, `user_id`, `auction_id`, `phase` ENUM('DEPOSIT','FINAL'), `state`, `processor_ref`, `amount`)

**fraud_signals** (`signal_id`, `user_id`, `auction_id`, `type`, `score`, `created_at`)

**watchlist** (`user_id`, `listing_id`, `created_at`)

**audit_log** (`id`, `entity_type`, `entity_id`, `action`, `payload_json`, `actor_id`, `ts`)

### Search index (document per listing)

```json
{
  "listingId": "L1",
  "title": "Vintage camera",
  "categoryPath": ["Electronics", "Cameras"],
  "auctionId": "A1",
  "auctionStatus": "LIVE",
  "currentPrice": 250.00,
  "endTime": "2026-04-10T18:00:00Z"
}
```

---

## 💡 Step 10: Interview Discussion Points (12–15 minutes)

### 1. English vs Vickrey — incentive truthfulness
**Talking point:** Vickrey encourages bidders to bid true value (dominant strategy in independent private values model). English reveals less private information incrementally; good UX but can encourage sniping → anti-snipe.

### 2. Proxy bidding privacy
Never expose other users’ max bids. Only emit minimum necessary current price and leader id (or anonymous paddle in some designs).

### 3. Reserve price handling
If `currentPrice < reserve` at end → `no_sale` or allow second-chance offer (extension). Clarify with interviewer.

### 4. Payment: deposit timing
Options: hold at **bid time** (reduces fake bids) vs **win time** (better conversion). Trade fraud vs friction.

### 5. Sealed-bid integrity
Use server-side timestamps; optional **commit–reveal** scheme for cryptographic binding (advanced follow-up).

### 6. Dutch auction fairness
Fast network wins; consider random tie window or queue of intents (deep dive if asked).

### 7. Real-time stack
WebSockets per auction room + Redis Pub/Sub backplane + horizontal connection servers.

### 8. Recommendations cold start
Fallback to category popularity; blend with collaborative filtering as data grows.

### 9. Fraud
Combine rules (velocity, seller-buyer graph distance) with ML scores asynchronously; block synchronously only on high confidence.

### 10. Idempotency
Mobile retries must not create duplicate bids; return same result for same `idempotency_key`.

---

## ✅ Step 11: SOLID Principles Verification

### Single Responsibility
- `BidService` orchestrates; `ProxyBidEngine` only computes proxy math; `FraudDetectionService` only risk.

### Open/Closed
- New auction type: implement `AuctionTypeStrategy` without modifying `BidService` switch (inject registry map).

### Liskov Substitution
- All `AuctionTypeStrategy` implementations honor same contract for `validateBid` / `resolveWinner`.

### Interface Segregation
- Split `PaymentService` from `PayoutService` if payouts gain different methods.

### Dependency Inversion
- `BidService` depends on `AuctionRepository` interface, not ORM concrete.

---

## 📈 Step 12: Complexity Analysis

| Operation | Time | Notes |
|-----------|------|--------|
| Place bid (English) | O(B) worst for proxy map load | B = bidders with proxy rows; optimize with top-2 heap O(log B) |
| Resolve sealed/Vickrey at close | O(N log N) | N bids for that auction |
| Dutch tick | O(1) | Per scheduled decrement |
| Search | O(log V) typical | V indexed docs + filters |
| Fan-out notify | O(S) | S subscribers; async, bounded workers |

**Hot auction scaling:** partition by `auctionId`; single-writer queue removes lock contention on row.

---

## 🎓 Step 13: Key Takeaways

1. **Model auction variance with strategies**, not a single god-class `Auction`.
2. **English + proxy** is a state-reconciliation problem: lock auction row or use single-writer partition.
3. **Sealed and Vickrey** defer price discovery: integrity and tie-breaking are interview gold.
4. **Lifecycle + anti-snipe** are business rules on `AuctionSchedule`; keep them transactional with bid acceptance.
5. **Payments** deserve a small state machine (authorize, capture, release) with idempotency.
6. **Realtime** is cross-cutting: core domain emits events; infra pushes to WebSockets.
7. **Fraud** is layered: cheap synchronous rules + richer async scoring.
8. **Search/recommendations** sit beside transactional core; denormalize auction fields into listing index for filters.

**Interview success formula:** Clarify auction types → lifecycle → bid concurrency → winner/payment → realtime → trust → discovery.

---

**Total: 137 DSA + 13 LLD Problems**

Ready for review!
