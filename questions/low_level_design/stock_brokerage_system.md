# Low-Level Design: Stock Brokerage / Trading System

**Difficulty:** Hard 🔥

**Interview Duration:** 90–120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting, with emphasis on the **order matching engine**, **real-time market data**, and **transactional consistency** across balances, positions, and the order book.

---

## 🎯 Step 1: Understand the Problem (8–12 minutes)

### What the Interviewer Says:
*"Design a stock brokerage platform where retail users open accounts, manage portfolios, place different order types against a central limit order book, see live prices and depth, and where trades settle while we enforce margin, day-trading rules, circuit breakers, and regulatory auditability."*

### Clarifying Questions to Ask:

1. **Q:** Are we building a **broker-dealer** (internalize some flow) or **agency-only** (route everything to external exchanges)?  
   **A:** Design for **agency + smart order router** to external venues; include an **internal matching** path for **odd lots** or **internalization** as an extension.

2. **Q:** Which order types are in scope?  
   **A:** **Market**, **limit**, **stop-loss** (stop-market), **stop-limit**; discuss **IOC/FOK** as extensions.

3. **Q:** How is the **order book** represented—one symbol per process?  
   **A:** **Partition by symbol** (or symbol shard); **single writer** per symbol for matching to guarantee **deterministic** price-time priority.

4. **Q:** Consistency between **cash**, **positions**, and **orders**?  
   **A:** **Strong consistency** for anything that can **lose money** or **oversell**; use **ledger + double-entry** or **single transactional boundary** per accept/cancel/fill.

5. **Q:** Real-time data latency expectations?  
   **A:** **Sub-second** quotes for UI; **WebSocket** fan-out; **coalesced** depth updates under load; **NBBO** from SIP or direct feeds (conceptual).

6. **Q:** Settlement model?  
   **A:** **T+2** (or configurable); **pending settlement** vs **settled** cash; **buying power** includes **margin** per **Reg T**-style rules (simplified).

7. **Q:** Market sessions?  
   **A:** **Pre-market**, **regular**, **after-hours** with different **liquidity** and **order eligibility** flags.

8. **Q:** Short selling?  
   **A:** Optional; if yes, **locate/borrow** flags and **margin** requirements on short positions.

9. **Q:** Tax and compliance depth?  
   **A:** **Cost-basis lots** (FIFO default), **audit trail** (immutable event log), **export** for **1099**-class reporting—no full legal advice in LLD.

10. **Q:** Notifications?  
    **A:** **Order filled**, **partially filled**, **rejected**, **price alerts** on watchlist symbols—**idempotent** delivery.

11. **Q:** Scale assumptions?  
    **A:** **Millions** of accounts, **thousands** of symbols, **burst** order flow at open/close; **read replicas** for history and charts.

12. **Q:** Circuit breakers—market-wide vs symbol?  
    **A:** Both: **exchange-level** halts (ingested as feed events) and **firm-level** **fat-finger** / **position** limits pre-trade.

13. **Q:** Paper trading vs live?  
    **A:** Same code paths with a **mode** flag and **simulated** fills for paper—helps testing without touching real balances.

---

## 🔹 Step 2: Gather Requirements (10–15 minutes)

### Functional Requirements

#### Accounts & Profiles (FR1–FR6)
1. System shall register **users** with **KYC** state (`PENDING`, `VERIFIED`, `SUSPENDED`) and role **`INVESTOR`** or **`TRADER`** (permissions: margin, options-ready flags as extensions).
2. Each user shall have one or more **brokerage accounts** (`CASH`, `MARGIN`) with unique **account number**.
3. System shall maintain **account status** (`ACTIVE`, `RESTRICTED`, `CLOSED`) and **trading halts** per account (e.g., violation of day-trade rule).
4. Users shall authenticate and authorize API/mobile sessions (out-of-band detail; hooks for **OAuth2** / **mTLS** for institutional).
5. System shall link **external bank accounts** for **ACH** funding (high level); internal **ledger** reflects **posted** transfers only.
6. Admins shall apply **manual restrictions** (freeze trading, force liquidation policy hook).

#### Portfolio & Positions (FR7–FR12)
7. System shall maintain **positions** per `(accountId, symbol)` with **quantity**, **average cost**, **market value** (derived from last price).
8. **Long** and optional **short** quantities shall be tracked separately or as signed quantity with **side** validation.
9. **Open orders** shall **reserve** **buying power** or **shares** (for sells) so **double-spend** is impossible.
10. System shall support **fractional shares** (optional) with **precision** rules per symbol.
11. **Unrealized P&L** shall be computable from positions + last trade or mid quote.
12. **Corporate actions** (splits, dividends) as **asynchronous adjustments** to positions and cash (event-driven).

#### Order Placement (FR13–FR22)
13. Users shall place **market orders** (execute at best available; slippage disclosure).
14. Users shall place **limit orders** with **limit price**, **time-in-force** (`DAY`, `GTC`, `IOC`, `FOK` optional).
15. Users shall place **stop-loss** orders: when **stop price** is **crossed**, order becomes a **market** order (sell stop triggers when last <= stop for long exit semantics—define convention clearly).
16. Users shall place **stop-limit** orders: when stop triggers, a **limit** order is **activated** at **limit price**.
17. System shall validate **session**: some orders only allowed in **regular**; extended hours may require explicit **flag**.
18. System shall validate **minimum tick size**, **lot size**, and **max order size** per symbol **config**.
19. System shall **preview** estimated **fees**, **buying power impact**, and **reject** with **reason codes** (`INSUFFICIENT_BUYING_POWER`, `SYMBOL_HALTED`, etc.).
20. Users shall **modify** (price/qty) or **cancel** open orders; partial fills must remain **consistent** with remaining qty.
21. **Idempotent** submission via **client order id** per account.
22. **Short** sells shall require **locate** / **margin** approval when feature enabled.

#### Matching Engine & Order Book (FR23–FR28)
23. System shall maintain a **limit order book** per symbol: **bids** sorted **descending** by price then **time**, **asks** **ascending** by price then **time** (**price-time priority**).
24. **Market orders** shall **consume** opposite side until filled or book exhausted (then **remainder** handling: reject, rest as market-on-close, or convert—product policy).
25. **Incoming limit** that **crosses** the book shall **match** at **resting** order prices (**maker** gets price improvement rules per venue—simplified as **resting price**).
26. System shall publish **trade prints** and **book deltas** to **market data** subscribers.
27. **Stop** and **stop-limit** orders shall rest in a **separate trigger tracker** until **last trade** (or **bid/ask** per policy) satisfies stop condition.
28. Matching shall be **deterministic** and **sequential per symbol** (single partition writer).

#### Trade Execution & Lifecycle (FR29–FR36)
29. Each **fill** shall generate a **Trade** record: `tradeId`, `buyOrderId`, `sellOrderId`, `symbol`, `qty`, `price`, `timestamp`, `venue`.
30. **Order lifecycle** states: `PENDING_NEW` → `NEW` → `PARTIALLY_FILLED` → `FILLED` | `CANCELLED` | `REJECTED` | `EXPIRED`.
31. **Partial fills** shall update **filledQty**, **avgFillPrice**, and **remainingQty** atomically with **ledger** and **position** updates.
32. **Cancels** shall remove **unfilled** quantity from book and **release** reservations.
33. **Reject** on risk, halt, or validation shall **never** leave partial reservations.
34. System shall support **routing** to **simulated** venue for **paper** accounts.
35. **Trade bust** / **correction** as rare **admin** events with compensating **ledger** entries (audit mandatory).
36. **Allocation** of multi-account **block** orders is out of scope unless **prime broker** scenario—mention extension.

#### Real-Time Market Data & Watchlist (FR37–FR42)
37. System shall ingest **top-of-book** and **last sale** (and optional **depth**) from **market data** gateway.
38. Clients shall subscribe via **WebSocket** to **quotes**, **trades**, and **book snapshots + incremental** updates per symbol.
39. **Charts** shall be served from **time-series** store (OHLCV bars: 1m, 5m, 1d) built from **trade** stream.
40. Users shall maintain **watchlists** (named lists of symbols) with **ordering** and **alerts**.
41. **Price alerts** shall fire when **last** or **bid/ask** crosses **threshold** (persistent rules engine).
42. **Staleness** detection: if feed **silent** > N seconds, UI shows **warning** and **blocks** certain order types (policy).

#### Trading Restrictions & Buying Power (FR43–FR50)
43. System shall compute **buying power** = **settled cash** + **margin credit** − **open buy commitments** − **maintenance margin** on positions (simplified formula; configurable).
44. **Day-trading**: track **round trips** in **rolling 5 business days**; enforce **PDT**-style minimum equity if **pattern day trader** (jurisdiction-specific flags).
45. **Margin** accounts shall enforce **initial** and **maintenance** **margin %** per symbol **tier**.
46. **Concentration limits**: max **%** of portfolio in single symbol (risk config).
47. **Options** / **derivatives** out of core scope—**hook** `InstrumentType` for extension.
48. **Restricted symbols** (IPO lock, CTB) shall **reject** or **limit** order types.
49. **Velocity limits**: max **orders per minute** per account (abuse / fat-finger).
50. **Foreign exchange** for multi-currency: optional **cash sub-ledger** per currency.

#### Settlement, Clearing & History (FR51–FR56)
51. On **trade date (T)**, trades move to **pending settlement**; on **T+n**, **cash** and **position** **settlement** posts (two-phase **settled vs unsettled** cash if modeling **good faith** violations).
52. System shall generate **confirmations** (contract notes) per **execution** or **aggregated** daily.
53. **Transaction history** shall list **orders**, **trades**, **transfers**, **fees**, **dividends**, **interest**, filterable by date and type.
54. **Statements** (monthly PDF) as **async** job from **immutable** activity log.
55. **Corporate actions** settlement through **custodian** interface (high level).
56. **Fail-to-deliver** handling as **ops** workflow—mention only.

#### Risk & Circuit Breakers (FR57–FR61)
57. **Pre-trade risk** shall check **position limits**, **notional limits**, **buying power**, and **halt** status **before** order enters book.
58. **Market-wide circuit breaker** events from feed shall **pause** new **risk-increasing** orders for affected symbols or **globally** (tiered).
59. **Intraday volatility bands** (optional) throttle aggressive market orders near **limit down/up** (if modeled).
60. **Kill switch** per account, per desk, or **global** for **incidents**.
61. **Post-trade** surveillance hooks (large trade reports)—**event export** to **compliance** bus.

#### Regulatory, Tax & Audit (FR62–FR66)
62. **Immutable audit trail**: append-only **OrderEvent**, **LedgerEntry**, **RiskDecision** with **user**, **timestamp**, **correlation id**.
63. **Tax lots**: each **buy** creates **lot**; **sell** consumes lots per **FIFO** (default), **LIFO**, or **specific lot** selection.
64. **Realized gains** computed on sell **lot** matching for **year-end** reporting exports.
65. **Best execution** disclosure data: **venue**, **timestamp**, **price**—stored per trade.
66. **Retention** policy: **7 years** typical for records (config).

#### Notifications (FR67–FR70)
67. **Push/email/SMS** (via provider) for **fills**, **cancels**, **rejects**, **margin call** warnings.
68. **Price alerts** from watchlist rules; **dedupe** and **rate limit** per user.
69. **Outbox pattern** for **at-least-once** delivery with **idempotency** on consumer.
70. User preferences for **channel** and **quiet hours**.

#### Market Sessions (FR71–FR74)
71. **PRE_MARKET**, **REGULAR**, **AFTER_HOURS**, **CLOSED**—driven by **exchange calendar** and **symbol** **mic** config.
72. Order eligibility matrix: e.g., **limit** allowed extended, **market** may map to **extended hours** session type.
73. **Auction** open / close phases—optional **model** as **halt** reason on symbol.
74. **Holiday calendar** per **exchange**; **early close** days adjust **session** end.

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many accounts, symbols, orders per second?"
- Accounts: **10M+**; active traders: **1%** peak; symbols: **5k–15k** US equities
- Peak **messages**: open/close **100k+ orders/sec** aggregate (sharded)
- Market data: **millions** of tick updates/sec inbound; fan-out to **subscribers** is **N × updates** (coalesce)

**Deduced NFRs:**
- ✅ **Horizontal scale**: matching **workers** by **symbol partition**; **stateless** API tier
- ✅ **CQRS**: **write path** (orders) vs **read path** (history, charts)
- ✅ **Caching**: **last quote** per symbol in **Redis**; **book snapshot** optional

---

#### 2. **Consistency Analysis**

**Think:** "What must never be wrong?"
- **Cash** and **reserved** amounts; **position** qty; **order** remaining qty; **no double fill**

**Deduced NFRs:**
- ✅ **Serializable** or **single-threaded matching** per symbol + **transactional** ledger updates
- ✅ **Outbox** + **exactly-once** semantics at business level via **idempotency keys**
- ✅ **Invariant**: `sum(ledger) == 0` per currency (double-entry) or equivalent **balance + reservations** consistency

---

#### 3. **Availability Analysis**

**Think:** "Can we trade if charting is down?"
- **Order entry** > **charts**; **read** tier can degrade

**Deduced NFRs:**
- ✅ **99.99%** for **order gateway** (active-active with partition failover)
- ✅ **Graceful degradation**: stale quotes → **block** aggressive orders
- ✅ **Matching engine** **leader per partition** with **hot standby** or **deterministic replay** from log

---

#### 4. **Maintainability Analysis**

**Think:** "Regulators ask *why* was this order rejected?"
- Full **trace** from **risk** → **matching** → **settlement**

**Deduced NFRs:**
- ✅ **Structured logging**, **correlation ids**, **event sourcing** for order state
- ✅ **Feature flags** for **session** rules and **risk** thresholds
- ✅ **Simulation** environment with **replay** of production **ticks** (no PII)

---

#### 5. **Performance Analysis**

**Think:** "Matching loop latency?"
- **< 1 ms** per **match event** in-process for hot symbol (goal); **p99** API **< 50 ms**

**Deduced NFRs:**
- ✅ **In-memory order book** per symbol partition; **batch** persistence **async** or **sync** on critical path (interview tradeoff)
- ✅ **Lock-free** structures optional; **correctness first** = **single writer** + simple structures

---

#### 6. **Security Analysis**

**Think:** "Money movement and PII"
- **Encryption** at rest/in transit; **least privilege**; **fraud** detection hooks

**Deduced NFRs:**
- ✅ **mTLS** internal; **PII** tokenization; **rate limits**; **break-glass** admin with **audit**
- ✅ **Segregation** of **customer** assets (conceptual **SIPC**-aware modeling)

---

## 🧩 Step 3: Identify Core Entities (12–18 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "User opens brokerage account" | User, BrokerageAccount, KycProfile |
| "Portfolio and positions" | Portfolio, Position, Holding, Instrument |
| "Place market/limit/stop order" | Order, MarketOrder, LimitOrder, StopOrder |
| "Order book bid ask depth" | OrderBook, PriceLevel, BookEntry |
| "Matching engine" | MatchingEngine, Trade, MatchResult |
| "Buying power and ledger" | Ledger, LedgerEntry, CashBalance, Reservation |
| "Settlement T+2" | SettlementInstruction, TradeConfirmation |
| "Watchlist and alerts" | Watchlist, PriceAlert |
| "Circuit breaker" | CircuitBreakerState, TradingHalt |
| "Audit and tax lots" | AuditEvent, TaxLot, CostBasisRecord |
| "Market session" | MarketSession, ExchangeCalendar |
| "Notifications" | Notification, NotificationOutbox |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Holding | ❌ NO | Synonym of **Position** in equities context |
| BookEntry | ✅ YES | Resting limit at a price level |
| Reservation | ✅ YES | First-class for buying power / locate |
| PriceLevel | ✅ YES | Aggregate qty at a price on one side |
| NBBO | ❌ NO | Derived view, not persisted entity |
| Tick | ❌ NO | Event into time-series |

### Final Entity List

**Identity & Account:**
1. **User** — authentication identity; links to profiles
2. **BrokerageAccount** — trading account (cash/margin)
3. **KycProfile** — verification state

**Market Reference:**
4. **Instrument** — symbol, tick size, lot size, asset class
5. **ExchangeCalendar** — sessions, holidays per venue

**Trading:**
6. **Order** — base order (polymorphic by type)
7. **OrderBook** — per-symbol bids/asks structure
8. **Trade** — execution record
9. **MatchingEngine** — accepts orders, emits trades (service / aggregate root per partition)

**Portfolio & Money:**
10. **Position** — qty, avg cost per (account, symbol)
11. **LedgerAccount** — logical bucket (cash, payable, fee, etc.)
12. **LedgerEntry** — double-entry line
13. **Reservation** — hold on cash or shares for open orders

**Market Data:**
14. **QuoteSnapshot** — bid/ask/last (cached)
15. **MarketDataSubscription** — client interest registry (operational)

**User Features:**
16. **Watchlist** — named symbol list
17. **PriceAlertRule** — threshold rule

**Risk & Compliance:**
18. **RiskCheckResult** — pre-trade decision artifact
19. **TradingHalt** — symbol or market halt
20. **AuditEvent** — immutable compliance log
21. **TaxLot** — cost basis tracking

**Lifecycle:**
22. **SettlementBatch** — T+n processing group
23. **NotificationOutbox** — reliable notify pipeline

---

## 🔗 Step 4: Establish Relationships (14–20 minutes)

### Pass 1: Account & Portfolio

#### User ↔ BrokerageAccount
**Conclusion:** **Composition** (accounts belong to user)
```
User ◆────→ BrokerageAccount [1..*]
```

#### BrokerageAccount ↔ Position
**Conclusion:** **Composition**
```
BrokerageAccount ◆────→ Position [0..*]  (per symbol)
```

#### BrokerageAccount ↔ LedgerAccount
**Conclusion:** **Association** (chart of accounts per brokerage account)
```
BrokerageAccount ─────→ LedgerAccount [1..*]
```

---

### Pass 2: Orders & Matching

#### BrokerageAccount → Order
**Conclusion:** **Composition** (orders scoped to account)
```
BrokerageAccount ◆────→ Order [0..*]
```

#### OrderBook ↔ Instrument
**Conclusion:** **Association** (1:1 per trading symbol partition)
```
OrderBook ─────→ Instrument [1]
```

#### MatchingEngine → OrderBook
**Conclusion:** **Uses** (engine mutates book)
```
MatchingEngine ─────→ OrderBook [1]
```

#### Trade → Order
**Conclusion:** **Association** (each trade links buy + sell orders)
```
Trade ─────→ Order (buy) [1]
Trade ─────→ Order (sell) [1]
```

---

### Pass 3: Reservations, Watchlist, Compliance

#### Reservation → Order
**Conclusion:** **Association** (1:1 or 1:order side)
```
Reservation ─────→ Order [1]
Reservation ─────→ BrokerageAccount [1]
```

#### User → Watchlist
**Conclusion:** **Composition**
```
User ◆────→ Watchlist [0..*]
```

#### Watchlist → Instrument
**Conclusion:** **Many-to-many** (via join)
```
Watchlist ─────→ Instrument [0..*]
```

#### TaxLot → BrokerageAccount, Instrument
**Conclusion:** **Association**
```
TaxLot ─────→ BrokerageAccount [1]
TaxLot ─────→ Instrument [1]
```

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| User → BrokerageAccount | 1:N | Composition |
| BrokerageAccount → Position | 1:N | Composition |
| BrokerageAccount → Order | 1:N | Composition |
| Instrument → OrderBook | 1:1 | Association |
| MatchingEngine → OrderBook | N:1 | Uses |
| Trade → Order | 2:1 (buy/sell) | Association |
| Order → Reservation | 1:0..1 | Association |
| User → Watchlist | 1:N | Composition |

---

## 📐 Step 5: Design Class Diagrams (18–25 minutes)

### Class Diagram 1: Enums

```
┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │  │ <<enumeration>>    │
│   OrderSide      │  │   OrderType      │  │   OrderStatus      │
├──────────────────┤  ├──────────────────┤  ├────────────────────┤
│ BUY              │  │ MARKET           │  │ PENDING_NEW        │
│ SELL             │  │ LIMIT            │  │ NEW                │
└──────────────────┘  │ STOP_LOSS        │  │ PARTIALLY_FILLED   │
                      │ STOP_LIMIT       │  │ FILLED             │
                      └──────────────────┘  │ CANCELLED          │
                                            │ REJECTED           │
                                            │ EXPIRED            │
                                            └────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ <<enumeration>>    │  │ <<enumeration>>    │  │ <<enumeration>>    │
│   TimeInForce      │  │   MarketSession    │  │   AccountType      │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│ DAY                │  │ CLOSED             │  │ CASH               │
│ GTC                │  │ PRE_MARKET         │  │ MARGIN             │
│ IOC                │  │ REGULAR            │  └────────────────────┘
│ FOK                │  │ AFTER_HOURS        │
└────────────────────┘  └────────────────────┘
```

---

### Class Diagram 2: Order Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                        <<abstract>> Order                         │
├─────────────────────────────────────────────────────────────────┤
│ - orderId: String                                               │
│ - clientOrderId: String                                         │
│ - accountId: String                                             │
│ - symbol: String                                                │
│ - side: OrderSide                                               │
│ - quantity: Decimal                                             │
│ - filledQty: Decimal                                            │
│ - avgFillPrice: Decimal                                         │
│ - status: OrderStatus                                           │
│ - tif: TimeInForce                                              │
│ - createdAt: Instant                                            │
│ - sessionEligibility: Set<MarketSession>                        │
├─────────────────────────────────────────────────────────────────┤
│ + remainingQty(): Decimal                                       │
│ + applyFill(qty, price): void                                   │
└─────────────────────────────────────────────────────────────────┘
        △                    △                    △
        │                    │                    │
┌───────┴───────┐   ┌────────┴────────┐   ┌─────┴──────────┐
│  MarketOrder  │   │   LimitOrder    │   │  StopOrder     │
├───────────────┤   ├─────────────────┤   ├────────────────┤
│               │   │ - limitPrice    │   │ - stopPrice    │
│               │   │                 │   │ - activated:bool│
└───────────────┘   └─────────────────┘   └────────────────┘
                                                │
                                                │ extends
                                                ▼
                                        ┌────────────────┐
                                        │ StopLimitOrder │
                                        ├────────────────┤
                                        │ - limitPrice   │
                                        └────────────────┘

// StopOrder: resting in StopTriggerTracker until market hits stop
// StopLimitOrder: on trigger, child LimitOrder activated
```

---

### Class Diagram 3: Order Book & Price Level

```
┌─────────────────────────────────────────────────────────────────┐
│                         OrderBook                               │
├─────────────────────────────────────────────────────────────────┤
│ - symbol: String                                                │
│ - bids: NavigableMap<Decimal, PriceLevel>  // desc key order   │
│ - asks: NavigableMap<Decimal, PriceLevel>  // asc key order    │
├─────────────────────────────────────────────────────────────────┤
│ + addLimit(order: LimitOrder): List<Trade>                    │
│ + matchMarket(order: MarketOrder): List<Trade>                 │
│ + cancel(orderId: String): boolean                             │
│ + bestBid(): Optional<Decimal>                                 │
│ + bestAsk(): Optional<Decimal>                                 │
│ + depthView(levels: int): BookDepthDTO                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ contains
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       PriceLevel                                │
├─────────────────────────────────────────────────────────────────┤
│ - price: Decimal                                                │
│ - side: OrderSide        // BUY side level vs SELL side        │
│ - entries: Deque<BookEntry>   // time priority FIFO            │
├─────────────────────────────────────────────────────────────────┤
│ + totalQuantity(): Decimal                                      │
│ + add(entry: BookEntry): void                                   │
│ + remove(orderId: String): boolean                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BookEntry                                 │
├─────────────────────────────────────────────────────────────────┤
│ - orderId: String                                               │
│ - remainingQty: Decimal                                         │
│ - enqueuedAt: long                                              │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Matching Engine & Supporting Services

```
┌─────────────────────────────────────────────────────────────────┐
│                    MatchingEngine                               │
├─────────────────────────────────────────────────────────────────┤
│ - books: Map<String, OrderBook>   // or injected per shard     │
│ - stopTracker: StopTriggerTracker                               │
├─────────────────────────────────────────────────────────────────┤
│ + submit(order: Order): SubmitResult                           │
│ + onMarketTick(symbol, lastPrice): void  // activate stops     │
│ + cancel(accountId, orderId): CancelResult                     │
└─────────────────────────────────────────────────────────────────┘
           │ uses                    │ uses
           ▼                         ▼
┌──────────────────────┐   ┌────────────────────────────────────┐
│ PreTradeRiskService  │   │   LedgerReservationService         │
├──────────────────────┤   ├────────────────────────────────────┤
│ + validate(order):   │   │ + reserveForBuy(order): void       │
│   RiskCheckResult    │   │ + reserveForSell(order): void      │
│ + onCircuitBreaker(  │   │ + release(orderId): void           │
│   symbol): boolean   │   │ + applyTrade(trade): void          │
└──────────────────────┘   └────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Trade                                   │
├─────────────────────────────────────────────────────────────────┤
│ - tradeId: String                                               │
│ - symbol: String                                                │
│ - price: Decimal                                                │
│ - quantity: Decimal                                             │
│ - buyOrderId: String                                            │
│ - sellOrderId: String                                           │
│ - executedAt: Instant                                           │
│ - venue: String                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Brokerage Account & Position

```
┌─────────────────────────────────────────────────────────────────┐
│                    BrokerageAccount                             │
├─────────────────────────────────────────────────────────────────┤
│ - accountId: String                                             │
│ - userId: String                                                │
│ - type: AccountType                                             │
│ - status: AccountStatus                                         │
├─────────────────────────────────────────────────────────────────┤
│ + getBuyingPower(): Decimal   // delegates to ledger           │
└─────────────────────────────────────────────────────────────────┘
           │
           │ 1:N
           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Position                                 │
├─────────────────────────────────────────────────────────────────┤
│ - symbol: String                                                │
│ - quantity: Decimal                                             │
│ - avgCost: Decimal                                              │
│ - settledQty / unsettledQty (optional split)                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35–50 minutes)

### Enums

```java
// OrderSide.java
public enum OrderSide { BUY, SELL }

// OrderType.java
public enum OrderType { MARKET, LIMIT, STOP_LOSS, STOP_LIMIT }

// OrderStatus.java
public enum OrderStatus {
    PENDING_NEW, NEW, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED, EXPIRED
}

// MarketSession.java
public enum MarketSession { CLOSED, PRE_MARKET, REGULAR, AFTER_HOURS }

// TimeInForce.java
public enum TimeInForce { DAY, GTC, IOC, FOK }
```

---

### Trade & Submit Result

```java
// Trade.java
public final class Trade {
    private final String tradeId;
    private final String symbol;
    private final String buyOrderId;
    private final String sellOrderId;
    private final java.math.BigDecimal quantity;
    private final java.math.BigDecimal price;
    private final long executedAtEpochMs;
    private final String venue;

    public Trade(String tradeId, String symbol, String buyOrderId, String sellOrderId,
                 java.math.BigDecimal quantity, java.math.BigDecimal price,
                 long executedAtEpochMs, String venue) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.quantity = quantity;
        this.price = price;
        this.executedAtEpochMs = executedAtEpochMs;
        this.venue = venue;
    }
    // getters ...
}

// SubmitResult.java
public record SubmitResult(boolean accepted, String orderId, String rejectReason,
                           java.util.List<Trade> immediateTrades) {}
```

---

### Book Entry & Price Level

```java
// BookEntry.java
public final class BookEntry {
    private final String orderId;
    private java.math.BigDecimal remainingQty;
    private final long enqueuedAtNanos;

    public BookEntry(String orderId, java.math.BigDecimal remainingQty, long enqueuedAtNanos) {
        this.orderId = orderId;
        this.remainingQty = remainingQty;
        this.enqueuedAtNanos = enqueuedAtNanos;
    }

    public void decreaseQty(java.math.BigDecimal q) {
        this.remainingQty = this.remainingQty.subtract(q);
    }

    public boolean isFullyFilled() { return remainingQty.signum() <= 0; }

    public String orderId() { return orderId; }
    public java.math.BigDecimal remainingQty() { return remainingQty; }
    public long enqueuedAtNanos() { return enqueuedAtNanos; }
}

// PriceLevel.java
import java.util.*;
import java.math.BigDecimal;

public final class PriceLevel {
    private final BigDecimal price;
    private final ArrayDeque<BookEntry> entries = new ArrayDeque<>();

    public PriceLevel(BigDecimal price) { this.price = price; }

    public void add(BookEntry e) { entries.addLast(e); }

    public boolean removeOrder(String orderId) {
        return entries.removeIf(e -> e.orderId().equals(orderId));
    }

    public BigDecimal totalQuantity() {
        return entries.stream().map(BookEntry::remainingQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BookEntry peekFirst() { return entries.peekFirst(); }
    public void pollFirstIfFilled() {
        while (!entries.isEmpty() && entries.peekFirst().isFullyFilled()) entries.pollFirst();
    }

    public boolean isEmpty() {
        pollFirstIfFilled();
        return entries.isEmpty();
    }

    public BigDecimal price() { return price; }
}
```

---

### Order Book — Limit Matching (Price-Time Priority)

```java
// OrderBook.java — single-symbol in-memory book
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBook {
    private final String symbol;
    // Bids: high price first → descending key order
    private final NavigableMap<BigDecimal, PriceLevel> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    // Asks: low price first → ascending (natural BigDecimal order)
    private final NavigableMap<BigDecimal, PriceLevel> asks = new TreeMap<>();

    public OrderBook(String symbol) { this.symbol = symbol; }

    public Optional<BigDecimal> bestBid() {
        for (BigDecimal p : bids.navigableKeySet()) {
            PriceLevel l = bids.get(p);
            if (l != null && !l.isEmpty()) return Optional.of(p);
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> bestAsk() {
        for (BigDecimal p : asks.navigableSet()) {
            PriceLevel l = asks.get(p);
            if (l != null && !l.isEmpty()) return Optional.of(p);
        }
        return Optional.empty();
    }

    /**
     * Match incoming aggressive limit order against resting opposite side.
     * Resting orders are maker — trade at resting price.
     */
    public List<Trade> matchIncomingLimit(String aggressiveOrderId, OrderSide side,
                                          BigDecimal limitPrice, BigDecimal qty,
                                          String buyOrderIdResolved, String sellOrderIdResolved) {
        List<Trade> trades = new ArrayList<>();
        BigDecimal remaining = qty;

        NavigableMap<BigDecimal, PriceLevel> book = (side == OrderSide.BUY) ? asks : bids;
        Iterator<Map.Entry<BigDecimal, PriceLevel>> it = book.entrySet().iterator();

        while (remaining.signum() > 0 && it.hasNext()) {
            Map.Entry<BigDecimal, PriceLevel> e = it.next();
            BigDecimal levelPrice = e.getKey();
            if (side == OrderSide.BUY && levelPrice.compareTo(limitPrice) > 0) break;
            if (side == OrderSide.SELL && levelPrice.compareTo(limitPrice) < 0) break;

            PriceLevel level = e.getValue();
            while (remaining.signum() > 0 && !level.isEmpty()) {
                BookEntry resting = level.peekFirst();
                BigDecimal take = resting.remainingQty().min(remaining);
                resting.decreaseQty(take);
                remaining = remaining.subtract(take);

                String buyId = (side == OrderSide.BUY) ? aggressiveOrderId : resting.orderId();
                String sellId = (side == OrderSide.SELL) ? aggressiveOrderId : resting.orderId();
                trades.add(new Trade(UUID.randomUUID().toString(), symbol, buyId, sellId,
                        take, levelPrice, System.currentTimeMillis(), "INTERNAL"));

                level.pollFirstIfFilled();
            }
            if (level.isEmpty()) it.remove();
        }
        return trades;
    }

    public void addRestingLimit(String orderId, OrderSide side, BigDecimal price, BigDecimal qty) {
        NavigableMap<BigDecimal, PriceLevel> book = (side == OrderSide.BUY) ? bids : asks;
        PriceLevel level = book.computeIfAbsent(price, PriceLevel::new);
        level.add(new BookEntry(orderId, qty, System.nanoTime()));
    }

    public boolean cancelResting(String orderId, OrderSide side, BigDecimal price) {
        NavigableMap<BigDecimal, PriceLevel> book = (side == OrderSide.BUY) ? bids : asks;
        PriceLevel level = book.get(price);
        if (level == null) return false;
        boolean removed = level.removeOrder(orderId);
        if (level.isEmpty()) book.remove(price);
        return removed;
    }

    public String symbol() { return symbol; }
}
```

---

### Stop Trigger Tracker (Simplified)

```java
// StopTriggerTracker.java — evaluate on each last-sale tick
import java.math.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StopTriggerTracker {
    public record StopResting(String orderId, OrderSide side, BigDecimal stopPrice,
                              boolean stopLimit, BigDecimal limitPriceIfAny) {}

    private final Map<String, List<StopResting>> bySymbol = new ConcurrentHashMap<>();

    public void addStop(StopResting s, String symbol) {
        bySymbol.computeIfAbsent(symbol, k -> new ArrayList<>()).add(s);
    }

    /**
     * Returns stops that triggered on this tick (caller converts to market/limit flow).
     * Convention: SELL stop triggers when last <= stop; BUY stop triggers when last >= stop.
     */
    public List<StopResting> triggered(String symbol, BigDecimal last) {
        List<StopResting> list = bySymbol.getOrDefault(symbol, List.of());
        List<StopResting> out = new ArrayList<>();
        for (StopResting s : list) {
            if (s.side() == OrderSide.SELL && last.compareTo(s.stopPrice()) <= 0) out.add(s);
            if (s.side() == OrderSide.BUY && last.compareTo(s.stopPrice()) >= 0) out.add(s);
        }
        return out;
    }
}
```

---

### Matching Engine Orchestration (Conceptual Single Symbol)

```java
// MatchingEngine.java — interview sketch: wire risk + ledger + book
import java.math.*;
import java.util.*;

public class MatchingEngine {
    private final OrderBook book;
    private final StopTriggerTracker stops = new StopTriggerTracker();
    private final Map<String, WorkingOrder> working = new HashMap<>();

    public MatchingEngine(String symbol) {
        this.book = new OrderBook(symbol);
    }

    /** Minimal working order for demo */
    public static class WorkingOrder {
        String id;
        OrderSide side;
        OrderType type;
        BigDecimal qty;
        BigDecimal limit;
        OrderStatus status = OrderStatus.NEW;
    }

    public synchronized SubmitResult submitLimit(WorkingOrder o, PreTradeRiskService risk,
                                                   LedgerReservationService ledger) {
        var rc = risk.validate(o);
        if (!rc.ok()) return new SubmitResult(false, o.id, rc.reason(), List.of());

        ledger.reserve(o); // throws if insufficient BP / shares

        List<Trade> trades = book.matchIncomingLimit(o.id, o.side, o.limit, o.qty, o.id, o.id);
        BigDecimal filled = trades.stream().map(Trade::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rem = o.qty.subtract(filled);

        ledger.applyTrades(trades, o);
        if (rem.signum() > 0 && o.tif != TimeInForce.IOC && o.tif != TimeInForce.FOK) {
            book.addRestingLimit(o.id, o.side, o.limit, rem);
            working.put(o.id, o);
        } else if (rem.signum() > 0 && o.tif == TimeInForce.FOK) {
            ledger.release(o.id);
            return new SubmitResult(false, o.id, "FOK not fully fillable", trades);
        }
        return new SubmitResult(true, o.id, null, trades);
    }

    public synchronized void onTradeTick(BigDecimal last) {
        for (StopTriggerTracker.StopResting s : stops.triggered(book.symbol(), last)) {
            // Activate: submit market or limit child — production removes from stop map first
        }
    }
}

// Risk / Ledger stubs for compilation narrative
interface PreTradeRiskService {
    record RiskResult(boolean ok, String reason) {}
    RiskResult validate(MatchingEngine.WorkingOrder o);
}

interface LedgerReservationService {
    void reserve(MatchingEngine.WorkingOrder o);
    void release(String orderId);
    void applyTrades(List<Trade> trades, MatchingEngine.WorkingOrder aggressor);
}
```

---

### Order State Transitions (Interview Narrative)

```text
PENDING_NEW ──validate/risk──► REJECTED (terminal)
            └──ok────────────► NEW
NEW ──partial fill───────────► PARTIALLY_FILLED
PARTIALLY_FILLED ──more fill► PARTIALLY_FILLED | FILLED
NEW / PARTIALLY_FILLED ─cancel─► CANCELLED
NEW ──DAY expiry────────────► EXPIRED

Invariant: filledQty + remainingQty == originalQty (at all times after acceptance)
```

---

### Demo Flow (Pseudo)

```java
// StockBrokerageDemo.java
public class StockBrokerageDemo {
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine("AAPL");
        // Wire mock risk + ledger that always pass
        // 1. User A: limit sell 10 @ 150
        // 2. User B: limit buy 10 @ 150 → instant trade @ 150, both FILLED
        // 3. Publish trade to market-data bus + outbox for notifications
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **State** | `OrderStatus` transitions | Illegal transitions rejected at domain layer |
| **Strategy** | `MatchingPolicy`, `FeeSchedule`, `SessionEligibilityRule` | Exchange-specific rules without forking core engine |
| **Repository** | `OrderRepository`, `PositionRepository` | Persistence abstraction; replay-friendly tests |
| **Saga / Outbox** | Reservation → match → settle → notify | Cross-service consistency with retries |
| **Observer / Pub-Sub** | Trades → market data, charts, alerts | Decouple matching from fan-out |
| **Aggregate** | `BrokerageAccount` + ledger + positions | Single consistency boundary for financial state |
| **Event Sourcing** (optional) | Order lifecycle stream | Regulatory replay and forensic audit |
| **Chain of Responsibility** | `RiskCheckPipeline` | Ordered pre-trade checks (halt, BP, limits, velocity) |
| **Idempotent Consumer** | Notifications | Safe retries on fill events |

---

## 🔒 Step 8: Concurrency Handling (14–20 minutes)

### Problem
Thousands of clients submit orders for the same **hot symbol** while **ticks** update stops; **two threads** must not **double-match** the same resting quantity or **corrupt** buying power.

### Goals
- **Exactly-once effect** per accepted order from the **client’s perspective** (`clientOrderId` uniqueness per account).
- **Serializable outcome** for the **order book** + **fills** per symbol.
- **High read throughput** for quotes without blocking the **matching thread**.

### Techniques

1. **Single writer per symbol partition**  
   All `submit/cancel` for `symbol` routed to **one queue** / **actor** / **partition leader** — simplest strong correctness story for interviews.

2. **Database isolation**  
   If book is in DB: `SELECT ... FOR UPDATE` on **price level rows** or **symbol row**; **high contention** — usually avoid for hot path.

3. **Optimistic versioning**  
   `Position.version` or `Account.version` on settlement updates; retry on conflict for **low-frequency** admin corrections.

4. **Idempotency store**  
   `UNIQUE(account_id, client_order_id)` + return stored `SubmitResult` on duplicate POST.

5. **Ledger double-entry in one transaction**  
   `BEGIN; insert trades; insert ledger_lines; update positions; update reservations; COMMIT` — **all-or-nothing** per batch of fills from one match event.

6. **Read-your-writes**  
   After submit, client reads order status from **same region** replica that’s **synced** within SLA or polls **order id** on writer.

7. **Market data fan-out**  
   Matching publishes to **Kafka** / **NATS** topic `trades.{symbol}`; **WebSocket** layer is **stateless** and **subscribes**.

8. **Stop activation**  
   **Ticks** also go to symbol partition **in order** to avoid **race** between stop trigger and cancel.

9. **Distributed matching**  
   If symbol moves partitions: **drain** book, **pause** intake, **hand off** snapshot — rare ops procedure.

### What to say in interview
*"I serialize all mutations for a symbol on one partition so price-time priority is deterministic. Money and positions update in the same database transaction as trade inserts. Client order ids make retries safe. Real-time quotes are read-only replicas fed by an async bus so matching never waits on WebSocket fan-out."*

---

## 📊 Step 9: Database Schema (14–20 minutes)

### Core tables (relational sketch)

**users** (`user_id`, `email_hash`, `phone_hash`, `created_at`, `status`)

**kyc_profiles** (`user_id`, `tier`, `verified_at`, `documents_ref`)

**brokerage_accounts** (`account_id`, `user_id`, `type` [CASH|MARGIN], `status`, `base_currency`, `pattern_day_trader_flag`, `created_at`)

**instruments** (`symbol` PK, `exchange`, `tick_size`, `lot_size`, `asset_class`, `shortable`, `halted`)

**orders** (`order_id`, `account_id`, `client_order_id`, `symbol`, `side`, `type`, `status`, `qty`, `filled_qty`, `limit_price`, `stop_price`, `tif`, `session_flags`, `created_at`, `updated_at`, `version`, UNIQUE(`account_id`, `client_order_id`))

**order_events** (`event_id`, `order_id`, `type`, `payload_json`, `created_at`) — append-only

**trades** (`trade_id`, `symbol`, `price`, `qty`, `buy_order_id`, `sell_order_id`, `executed_at`, `venue`, `trade_day`)

**positions** (`account_id`, `symbol`, `qty`, `avg_cost`, `settled_qty`, `updated_at`, `version`)

**ledger_accounts** (`ledger_account_id`, `brokerage_account_id`, `kind` [CASH_SETTLED|CASH_UNSETTLED|MARGIN|FEE|...])

**ledger_entries** (`entry_id`, `ledger_account_id`, `amount`, `currency`, `trade_id` NULL, `transfer_id` NULL, `narration`, `posted_at`, `idempotency_key` UNIQUE)

**reservations** (`reservation_id`, `order_id`, `account_id`, `cash_reserved` NULL, `shares_reserved` NULL, `symbol` NULL, `status` [ACTIVE|RELEASED|CONSUMED])

**settlement_batches** (`batch_id`, `settlement_date`, `status`)

**settlement_items** (`batch_id`, `trade_id`, `status`)

**watchlists** (`watchlist_id`, `user_id`, `name`)

**watchlist_symbols** (`watchlist_id`, `symbol`, `sort_order`)

**price_alert_rules** (`rule_id`, `user_id`, `symbol`, `op` [CROSS_ABOVE|CROSS_BELOW], `threshold`, `status`)

**tax_lots** (`lot_id`, `account_id`, `symbol`, `qty_remaining`, `cost_per_share`, `opened_at`, `buy_trade_id`)

**lot_consumptions** (`consumption_id`, `lot_id`, `sell_trade_id`, `qty`, `realized_pnl`)

**trading_halts** (`symbol`, `reason`, `starts_at`, `ends_at`)

**audit_events** (`id`, `actor`, `action`, `entity_type`, `entity_id`, `payload_json`, `created_at`)

**notification_outbox** (`id`, `dedupe_key` UNIQUE, `user_id`, `channel`, `template`, `payload_json`, `status`, `created_at`)

**market_sessions_calendar** (`exchange`, `date`, `pre_open`, `regular_open`, `regular_close`, `after_close`, `is_early_close`)

### Helpful indexes
- `orders(account_id, status, created_at DESC)`
- `orders(symbol, status)` for **resting** order management
- `trades(symbol, executed_at DESC)` — partition by **month**
- `ledger_entries(ledger_account_id, posted_at DESC)`
- `positions(account_id)` PK (`account_id`, `symbol`)

---

## 💡 Step 10: Interview Discussion Points (18–28 minutes)

### 1. Price-time priority
**Same price, earlier timestamp first** — implemented as **FIFO deque** per `PriceLevel`. Pro-rata matching is an **exchange-specific** alternative.

### 2. Stop order semantics
Must fix **trigger reference** (last, bid, ask, NBBO). **Gap risk** on stop-market: client may fill **far** below stop in crash — disclose; **stop-limit** caps price but **may not fill**.

### 3. Internalization vs exchange
**Internal crossing** only with **best-ex** policy and **audit**; otherwise **route** to **exchange** and mirror **external fills** into the same `Trade` model.

### 4. Market order in empty book
Reject, **partial** with remainder cancelled, or convert to **limit** at **protection price** — **product** decision; must be **consistent** with disclosures.

### 5. Transactional boundaries
**Match** produces `List<Trade>`; **persist** trades + **update** positions + **ledger** + **order status** in **one** transaction; publish to bus **after** commit (outbox).

### 6. Buying power math (simplified)
`BP = max(0, equity - maintenance_margin) - open_buy_reserves` for margin; **Reg T** 50% initial on stocks (conceptual). **Unsettled cash** may **block** **withdrawals** but **allow** trading per **policy**.

### 7. Day-trading / PDT
Track **same-day** round trips; if **≥ 4** in **5** days and **< $25k** equity (US rule of thumb), **restrict** to **closing-only** — configurable.

### 8. Circuit breakers
**LULD** bands and **market-wide** halts ingested as **halts** table; matching **rejects** new risk-increasing orders; **cancels** optional for **IOC**-like behavior.

### 9. Real-time charts
**Trade stream** → **windowing** job (Flink / Kafka Streams) → **OHLCV** table; **WebSocket** serves **latest bar** + **tick** for **last** price.

### 10. Tax lots
**FIFO** default on sell; **specific identification** requires **user selection** before **settlement** cutoff; **wash sale** rules (US) need **adjusted cost** — compliance extension.

### 11. Fractional shares
Track **precision** (e.g., **6** dp); **internal** ledger uses **integer micro-units** to avoid float error.

### 12. Testing matching
Property-based: **conservation of shares** — sum of position changes + fills = 0 across paired buy/sell; **no negative** qty.

---

## ✅ Step 11: SOLID Principles Verification

### Single Responsibility
- `MatchingEngine` owns **crossing** logic only  
- `PreTradeRiskService` owns **risk** only  
- `LedgerReservationService` owns **money invariants** only  
- `MarketDataGateway` owns **feed normalization** only  

### Open/Closed
- New **order type** (e.g., **trailing stop**): extend `Order` + add **handler** without changing **price-level** structure  

### Liskov Substitution
- Any `SessionEligibilityRule` implementation must be **pure** and **deterministic** given calendar + order flags  

### Interface Segregation
- Split `ReadableOrderBook` (depth UI) from `WritableOrderBook` (matching) if exposing to **queries**  

### Dependency Inversion
- Core depends on `TradePublisher`, `Clock`, `CalendarProvider` abstractions — not Kafka / AWS specifics  

---

## 📈 Step 12: Complexity Analysis

| Operation | Time | Notes |
|-----------|------|-------|
| Insert resting limit | O(log P) + O(1) | P = distinct prices; deque append |
| Cancel resting by orderId | O(P) worst | Interview: index `orderId → (side, price)` for **O(1)** locate |
| Match aggressive order | O(F + k) | F = filled shares steps; k = price levels touched |
| Best bid/ask | O(1) amortized | With lazy cleanup of empty levels |
| Stop trigger scan | O(S) | S = stops per symbol; production uses **sorted triggers** or **price indexes** |
| Risk pipeline | O(R) | R = number of checks (small constant) |
| Ledger post per fill batch | O(1) DB round-trip | Batched statements |
| Watchlist alert eval | O(W) per tick | W = rules for symbol; **shard** rules by symbol |

**Scaling mantra:** **Partition by symbol** for matching; **never** share mutable book across threads; **separate** **OLTP** from **market-data fan-out**.

---

## 🎓 Step 13: Key Takeaways

1. **Matching engine** correctness rests on **price-time priority**, **single-writer per symbol**, and **atomic** persistence of **trades + ledger + positions**.
2. **Order types** decompose into **resting limit workflow** + **stop trigger** layer that **activates** child orders on **ordered ticks**.
3. **Buying power** and **reservations** prevent **overspending** and **overselling**; **unsettled cash** is a separate **policy** dimension from **matching**.
4. **Real-time data** is **eventually consistent** at the UI unless you **co-locate** read models; **matching** uses **in-memory** books fed by **deterministic** commands.
5. **Settlement** is **asynchronous** (T+n) but **trade date** economics should be **clear** in **ledger** accounts for **regulatory** reporting.
6. **Circuit breakers** and **halts** are **first-class** **pre-trade** inputs, not afterthoughts.
7. **Compliance** requires **immutable** **order events**, **tax lots**, and **idempotent** **notifications**.
8. **Sessions** (pre / regular / after) gate **eligibility** and **liquidity expectations** — surface explicitly in **API** and **UI**.

**Interview success formula:** Clarify order types + session rules → **order state machine** → **order book + matching** → **reservations + ledger** → **settlement** → **market data path** → **risk halts** → **audit & tax lots** → **scale by symbol partition**.

---

**Stock Brokerage / Trading System LLD — Hard difficulty — ready for review.**
