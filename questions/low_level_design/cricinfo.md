# Low-Level Design: Cricinfo (Cricket Score Tracking & Live Updates)

**Difficulty:** Hard 🔥

**Interview Duration:** 75–120 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting, with emphasis on **real-time delivery**, **append-only ball history**, and **format-specific cricket rules** (Test / ODI / T20).

---

## 🎯 Step 1: Understand the Problem (8–12 minutes)

### What the Interviewer Says:
*"Design a system like Cricinfo: live cricket scores, ball-by-ball commentary, player and team profiles, tournaments, historical records, notifications, and search—at scale for millions of concurrent viewers."*

### Clarifying Questions to Ask:

1. **Q:** Which match formats must we support first?  
   **A:** Test, ODI, and T20; extensible to T10 and domestic variants via configuration.

2. **Q:** Who enters live data—official scorers only or crowd-sourced?  
   **A:** Primary path is **authorized scorers**; optional delayed public feeds; strong audit for corrections.

3. **Q:** How correct must the live scoreboard be relative to TV?  
   **A:** Sub-second to a few seconds for premium users; **strong ordering per match** (ball sequence never reordered); occasional correction events allowed.

4. **Q:** Do we need DRS, umpire reviews, or third-party video sync?  
   **A:** Out of scope for core scoring; may store **review outcome** as metadata on a delivery if product requires it.

5. **Q:** Rain interruptions and DLS?  
   **A:** Model **interruptions**, revised targets, and **DLS par scores**; exact DLS tables can be a pluggable calculator service.

6. **Q:** Multi-innings Test rules—follow-on, declarations, new ball?  
   **A:** Yes: enforce or at least record **declaration**, **forfeiture**, **follow-on offered/taken**, **new ball** events.

7. **Q:** Scale expectations?  
   **A:** Popular matches: millions of concurrent readers; thousands of notification subscribers per wicket; write load is **low** (one match, few scorers) but **burst** on boundaries.

8. **Q:** Search scope?  
   **A:** Players, teams, matches, series/tournaments; fuzzy name matching; filters by season, format, venue.

9. **Q:** Historical stats—how deep?  
   **A:** Career aggregates, format splits, venue/opp splits, records (highest scores, best bowling); derived from **ball facts** + periodic rollups.

10. **Q:** Compliance and rights?  
    **A:** Commentary text may be licensed; store **provenance**; rate-limit APIs; geo restrictions as policy flags.

---

## 🔹 Step 2: Gather Requirements (10–15 minutes)

### Functional Requirements

#### Match & Format Management (FR1–FR8)
1. System shall create and schedule matches with format (**TEST**, **ODI**, **T20**, extensible enum), venue, timezone, and participating teams
2. System shall configure **innings count**, **overs per innings** (unlimited for Test), and **powerplay** segments for white-ball formats
3. System shall support **toss** outcome and **elected** option (bat/field)
4. System shall manage playing **XI** per innings (substitutes, concussion replacements as events)
5. System shall transition match through lifecycle: `SCHEDULED` → `LIVE` → `INNINGS_BREAK` → … → `COMPLETED` / `ABANDONED` / `NO_RESULT`
6. System shall support **super over** (one or more tie-break innings) as nested innings or linked match segment
7. System shall record **strategic breaks**, **drinks**, **stumps** (Test), and **session** boundaries where applicable
8. System shall support **interruptions** (rain, bad light) and attach **DLS snapshot** / revised target metadata to the match state

#### Live Score & Scoreboard (FR9–FR18)
9. System shall maintain authoritative **scoreboard**: runs, wickets, legal balls bowled, **display overs** (e.g., 19.4), extras breakdown
10. System shall track **batting pair**: striker, non-striker, individual runs, balls faced, strike rate (derived)
11. System shall track **current bowler**, **over** sequence, **maiden** detection, **economy** (derived)
12. System shall maintain **partnership** runs, balls, and boundary counts until wicket or innings end
13. System shall expose **required rate** and **projected score** (white-ball) from current state + configuration
14. System shall recompute **team totals** and **fall of wickets** table from ball stream (or cache with validation)
15. System shall support **undo/correct** last delivery with compensating events (audit mandatory)
16. System shall support **free hit** flag on next delivery after certain no-ball types
17. System shall enforce **maximum fielders outside circle** during powerplays (validation rules per format)
18. System shall support **team review** / **DRS outcome** as optional annotations (if product requires)

#### Ball-by-Ball & Commentary (FR19–FR28)
19. System shall accept **Delivery** events: outcome (runs, wicket, dot), **extras** type, **wicket** type, fielders involved
20. System shall distinguish **legal ball** vs **wide** / **no-ball** and adjust **balls remaining** in over accordingly
21. System shall append **commentary** lines (text, language, author) tied to `sequenceNumber` / delivery id
22. System shall emit **rich events**: boundaries (4/6), milestones (50/100), **five-for**, **hat-trick** detection (derived)
23. System shall support **secondary audio/graphic** metadata (sponsor tags—policy layer)
24. System shall allow **ball revision**: insert “correction” event that references prior `deliveryId`
25. System shall generate **over summaries** and **worm** chart inputs from ordered deliveries
26. System shall support **live analysis** snippets (AI or editor) attached to over or session
27. System shall retain **full immutable history** of deliveries for a match (append-only)
28. System shall support **multiple concurrent matches** with isolated streams

#### Player, Team, Tournament (FR29–FR38)
29. System shall maintain **player profiles**: name, role, batting/bowling style, DOB, country, photo
30. System shall compute and store **career statistics** (batting avg, strike rate, 100s/50s; bowling avg, economy, strike rate, wickets)
31. System shall support **format splits** (Test/ODI/T20) and **venue/opposition** splits (materialized or query-time)
32. System shall manage **teams** (national, franchise) and **squad** lists per series
33. System shall model **series** and **tournaments** (league tables, points, NRR) where applicable
34. System shall link matches to **season**, **competition**, and **stage** (group, semi, final)
35. System shall support **player of the match** / **series** awards as post-match entities
36. System shall track **rankings** as snapshots (external feed or internal job)—optional
37. System shall handle **retired hurt**, **retired out** as specific wicket/batsman events
38. System shall support **captain** / **wicket-keeper** role markers per innings

#### Historical Records & Search (FR39–FR44)
39. System shall maintain **record boards** (highest individual score, best bowling, partnership records) with effective dates
40. System shall support **full-text and faceted search** across players, teams, matches, tournaments
41. System shall provide **autocomplete** APIs with debouncing and ranking (recent + popularity)
42. System shall allow **filter queries**: date range, format, venue, result margin, player involvement
43. System shall expose **read-optimized** views (leaderboards) refreshed asynchronously
44. System shall archive cold matches to cheaper storage while keeping **query path** consistent

#### Notifications & Real-Time (FR45–FR50)
45. Users shall subscribe to **match**, **team**, or **player** alerts
46. System shall push **real-time notifications** for wickets, milestones, innings break, result (channel: push, email, SMS—adapter layer)
47. System shall respect **user preferences** (wickets only, all events, digest mode)
48. Live clients shall receive **ordered event stream** per match (gap detection and catch-up)
49. System shall support **fan-out** to millions via **edge caching** + **pub/sub** (see Step 8)
50. System shall provide **live score API** with **ETag** / version for efficient polling fallback

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. Scalability Analysis
**Think:** "Readers vs writers; hot matches."
- Writers: 1–3 scorers per match + automation; **~1–10 deliveries/minute** average, bursts on spinners vs pace differ
- Readers: **millions** simultaneous; global audience; spike on last over
- Historical queries: heavy scan for analytics jobs; isolate to **OLAP** or **pre-aggregates**

**Deduced NFRs:**
- ✅ **CQRS**: command path (score ingestion) separate from read models (scoreboard projection)
- ✅ **Partition by `matchId`** for streams and caches
- ✅ **Horizontal read scaling** (replicas, CDN for static assets)
- ✅ **Backpressure** on commentary publish if downstream lagging

---

#### 2. Consistency Analysis
**Think:** "What must be strictly ordered? What can be eventual?"
- **Ball order** per match: **total order** required (sequence monotonic)
- Scoreboard: must reflect **causal** order of accepted deliveries
- Search index: **eventually consistent** (seconds acceptable)
- Notifications: **at-least-once** acceptable with idempotency on client

**Deduced NFRs:**
- ✅ **Strong consistency** on primary match aggregate version (optimistic concurrency per match)
- ✅ **Single-writer** or **serialized writes** per `matchId` (actor partition / DB row lock)
- ✅ **Append-only event log** as source of truth; projections rebuildable
- ✅ **Idempotent** ingestion (`scorerClientEventId` unique)

---

#### 3. Availability Analysis
**Think:** "World Cup final—cannot go dark."
- Live read path should survive **ingestion degradation** (serve last known good snapshot)
- Scoring path highly available for scorers (primary region); readers multi-region

**Deduced NFRs:**
- ✅ **99.95%+** for read APIs during live events
- ✅ **Graceful degradation**: stale score cap (e.g., max 30s) with banner
- ✅ **Multi-AZ** for core DB; **cross-region read replicas**
- ✅ **Chaos**: match partition failover without duplicate sequence (fencing—see Step 8)

---

#### 4. Maintainability Analysis
**Think:** "Law changes, format tweaks, new tournament types."
- Rules engine or **strategy per format** (`ScoringRules` interface)
- Feature flags for DLS version, super-over variants

**Deduced NFRs:**
- ✅ **Configurable formats** (YAML/DB): overs, powerplays, max bouncers (Test), etc.
- ✅ **Audit log** for every state change and correction
- ✅ **Replay tool** to rebuild scoreboard from event log (debug disputes)
- ✅ **Observability**: lag metrics between ingestion and fan-out

---

#### 5. Performance Analysis
**Think:** "Latency expectations."
- Scorer submit acknowledgment: **< 200ms** p99
- Fan-out to subscribed clients: **< 1s** p99 under load
- Search autocomplete: **< 100ms** p95
- Player career page (cached): **< 50ms** p95

**Deduced NFRs:**
- ✅ **In-memory read model** per hot match at edge + periodic DB persist
- ✅ **O(1)** append per delivery amortized (partitioned log)
- ✅ **Batch analytics** off critical path
- ✅ **Rate limiting** public APIs per API key / IP

---

#### 6. Security Analysis
**Think:** "Who can change the score?"
- Scorer accounts, role-based access, **2FA** for official feeds
- Prevent **replay attacks** on scorer API with nonce + signing (optional enterprise)
- **PII** for players minimal; GDPR delete/anonymize flows

**Deduced NFRs:**
- ✅ **RBAC**: scorer, editor, analyst, admin
- ✅ **mTLS** or signed JWT between scoring app and backend
- ✅ **WORM** or tamper-evident storage for legal disputes (optional)
- ✅ **DDoS** protection at edge; **bot** detection on HTML scraping routes

---

## 🧩 Step 3: Identify Core Entities (12–18 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Match formats Test/ODI/T20" | Match, MatchFormat, InningsRuleSet |
| "Ball-by-ball" | Delivery, BallOutcome, Over |
| "Scoreboard" | Scoreboard, Partnership, FallOfWicket |
| "Wicket types" | Wicket, DismissalType |
| "Extras" | ExtraType, Byes, LegByes, Wide, NoBall |
| "Player stats" | Player, BattingStats, BowlingStats, CareerAggregate |
| "Team, tournament" | Team, Squad, Series, Tournament, Season |
| "Commentary" | CommentaryLine, Analyst |
| "Notifications" | Subscription, Notification, UserPreferences |
| "Search" | SearchIndex, Query, Facet |
| "Real-time" | MatchEvent, EventStream, Projection |
| "DLS, rain" | Interruption, RevisedTarget, DLSCalculator |
| "Corrections" | CorrectionEvent, AuditEntry |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| BallOutcome | ❌ NO | Value object on Delivery |
| ExtraType | ❌ NO | Enum |
| FallOfWicket | ✅ YES | First-class row for scorecard table |
| DLSCalculator | ❌ NO | External service / strategy |
| Query | ❌ NO | DTO |
| EventStream | ❌ NO | Infrastructure concern |
| Partnership | ✅ YES | Mutable until break; identifiable |

### Final Entity List

**Match domain:**  
1. **Match** – root aggregate for a fixture  
2. **Innings** – batting side segment  
3. **Over** – grouping of deliveries (six legal balls typical)  
4. **Delivery** – atomic scoring event (append-only)  
5. **DeliveryCorrection** – compensating reference to prior delivery  
6. **ScoreboardSnapshot** – materialized view (cache/DB)  
7. **Partnership** – active pair aggregate stats  
8. **FallOfWicket** – score at dismissal  

**Participants:**  
9. **Team**  
10. **Player**  
11. **MatchSquad** – players available for match  
12. **PlayingXI** – selected 11 per innings  
13. **MatchOfficial** – umpires, scorers (optional)  

**Competition:**  
14. **Tournament** / **Series**  
15. **Season**  
16. **LeagueTableEntry** (derived/cached)  

**Content & engagement:**  
17. **CommentaryLine**  
18. **AnalysisSnippet**  
19. **UserSubscription**  
20. **Notification** (outbox)  

**History & search:**  
21. **PlayerCareerStats** (rollup)  
22. **RecordEntry**  
23. **SearchDocument** (index DTO)  

**Rules & config:**  
24. **FormatConfig** – overs, powerplays, follow-on margin  
25. **ScoringRules** – strategy interface implementation per format  

**Enums (first-class in model):**  
- `MatchFormat`, `MatchStatus`, `InningsStatus`, `DismissalType`, `ExtraKind`, `WicketBallType` (legal vs wide vs no-ball), `MatchPhase` (powerplay1/2/3)

---

## 🔗 Step 4: Establish Relationships (14–20 minutes)

### Pass 1: Core Match Graph

#### Match ↔ Innings
**Conclusion:** **Composition** (innings don't exist without match)
```
Match ◆────→ Innings [1..*]
```

#### Innings ↔ Over
**Conclusion:** **Composition**
```
Innings ◆────→ Over [1..*]
```

#### Over ↔ Delivery
**Conclusion:** **Composition** (delivery belongs to one over context; sequence global per innings also stored)
```
Over ◆────→ Delivery [0..6+]   // 6 legal; extras may add rows without advancing legal count appropriately
```

*Note:* Many systems model **Delivery** directly under **Innings** with `overNumber` + `ballInOver` for simpler ordering; both valid—pick one and stay consistent.

#### Delivery ↔ Player (Bowler, Batsman, Fielders)
**Conclusion:** **Association**
```
Delivery ────→ Player (bowler, striker, dismissedBatsman?, assistFielder?)
```

---

### Pass 2: Teams & Squads

#### Match ↔ Team
**Conclusion:** **Association** (home/away or team1/team2)
```
Match ────→ Team [2]
```

#### MatchSquad ↔ Player
**Conclusion:** **Association** (M:N with match context)
```
MatchSquad ────→ Player [15–20 typical]
```

---

### Pass 3: Tournament & Stats

#### Tournament ↔ Match
**Conclusion:** **Aggregation**
```
Tournament ◇────→ Match [1..*]
```

#### Player ↔ PlayerCareerStats
**Conclusion:** **Association** (rollup owned by stats service)
```
Player ────→ PlayerCareerStats [0..1 per slice key: format, season]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Match → Innings | 1:N | Composition |
| Innings → Over | 1:N | Composition |
| Over → Delivery | 1:N | Composition |
| Match → Team | N:2 | Association |
| Innings → PlayingXI | 1:2 teams worth | Association |
| Delivery → CommentaryLine | 1:N | Aggregation |
| User → UserSubscription | 1:N | Composition |
| Tournament → Match | 1:N | Aggregation |

---

## 📐 Step 5: Design Class Diagrams (18–25 minutes)

### Class Diagram 1: Enums

```
┌──────────────────┐  ┌──────────────────┐  ┌─────────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │  │ <<enumeration>>       │
│   MatchFormat    │  │   MatchStatus    │  │   DismissalType      │
├──────────────────┤  ├──────────────────┤  ├─────────────────────┤
│ TEST             │  │ SCHEDULED        │  │ BOWLED               │
│ ODI              │  │ TOSS_COMPLETED   │  │ CAUGHT               │
│ T20              │  │ LIVE             │  │ LBW                  │
│ T10              │  │ INNINGS_BREAK    │  │ RUN_OUT              │
└──────────────────┘  │ DRINKS           │  │ STUMPED              │
                      │ STRATEGIC_BREAK  │  │ HIT_WICKET           │
                      │ SUPER_OVER       │  │ RETIRED_HURT         │
                      │ COMPLETED        │  │ RETIRED_OUT          │
                      │ ABANDONED        │  │ OBSTRUCTING_FIELD    │
                      │ NO_RESULT        │  │ HANDLED_BALL         │
                      └──────────────────┘  │ HIT_BALL_TWICE       │
                                            │ TIMED_OUT            │
                                            └─────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>     │  │ <<enumeration>>     │
│   ExtraKind         │  │   DeliveryLegality  │
├─────────────────────┤  ├─────────────────────┤
│ NONE                │  │ LEGAL_GOOD          │
│ WIDE                │  │ LEGAL_DEAD_BALL     │
│ NO_BALL             │  │ WIDE                │
│ BYE                 │  │ NO_BALL             │
│ LEG_BYE             │  │ NO_BALL_WITH_BAT    │
│ PENALTY_RUNS        │  └─────────────────────┘
└─────────────────────┘
```

---

### Class Diagram 2: Match Aggregate Root

```
┌────────────────────────────────────────────────────────────────────┐
│                           Match                                     │
├────────────────────────────────────────────────────────────────────┤
│ - matchId: String                                                   │
│ - format: MatchFormat                                               │
│ - status: MatchStatus                                               │
│ - venueId: String                                                   │
│ - scheduledStart: Instant                                           │
│ - tossWinnerTeamId: String                                          │
│ - tossDecision: BAT | FIELD                                         │
│ - formatConfig: FormatConfig                                        │
│ - rules: ScoringRules                                               │
│ - version: long                          // optimistic lock         │
├────────────────────────────────────────────────────────────────────┤
│ + recordDelivery(cmd: DeliveryCommand): DeliveryRecordedEvent       │
│ + applyInterruption(cmd: InterruptionCommand): void                 │
│ + declareInnings(cmd: DeclarationCommand): void                     │
│ + offerFollowOn(): void                                             │
│ + completeMatch(): void                                             │
│ + currentScoreboard(): ScoreboardSnapshot                           │
└────────────────────────────────────────────────────────────────────┘
         │                              △
         │ uses                         │ implements
         ▼                              │
┌──────────────────┐            ┌──────┴───────────────┐
│  FormatConfig    │            │ <<interface>>         │
├──────────────────┤            │ ScoringRules         │
│ - oversPerInnings│            ├──────────────────────┤
│ - maxInnings     │            │ + validateDelivery() │
│ - powerplays     │            │ + runsAddedForExtra()│
│ - followOnMargin │            │ + isFreeHitNext()    │
│ - superOverRules │            └──────────────────────┘
└──────────────────┘                     △
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
             ┌──────┴──────┐     ┌──────┴──────┐     ┌──────┴──────┐
             │ TestRules   │     │ OdiRules    │     │ T20Rules    │
             └─────────────┘     └─────────────┘     └─────────────┘
```

---

### Class Diagram 3: Innings, Over, Delivery

```
┌────────────────────────────────────────────────────────────────────┐
│                          Innings                                    │
├────────────────────────────────────────────────────────────────────┤
│ - inningsId: String                                                 │
│ - matchId: String                                                   │
│ - battingTeamId: String                                               │
│ - bowlingTeamId: String                                               │
│ - index: int                     // 1st, 2nd ...                    │
│ - status: InningsStatus                                             │
│ - totalRuns: int                                                    │
│ - wickets: int                                                      │
│ - legalBallsBowled: int                                             │
│ - penaltyRunsAwarded: int                                           │
│ - startedAt / endedAt: Instant                                      │
├────────────────────────────────────────────────────────────────────┤
│ + apply(d: Delivery): void         // domain invariants             │
└────────────────────────────────────────────────────────────────────┘
         ◆───────────────────────────────────────┐
                                                 │
         ┌───────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────────┐
│                           Delivery                                  │
├────────────────────────────────────────────────────────────────────┤
│ - deliveryId: String                                                │
│ - matchId, inningsId: String                                        │
│ - sequenceNumber: long            // global monotonic per match      │
│ - overNumber: int                                                   │
│ - ballInOver: int                // display helper                   │
│ - legality: DeliveryLegality                                        │
│ - runsOffBat: int                                                   │
│ - extraKind: ExtraKind                                              │
│ - extraRuns: int                                                    │
│ - totalRunsOnDelivery: int                                            │
│ - isWicket: boolean                                                 │
│ - dismissal: DismissalDetails?                                      │
│ - freeHitActive: boolean                                            │
│ - strikerId, nonStrikerId, bowlerId: String                         │
│ - timestamp: Instant                                                │
│ - scorerEventId: String            // idempotency                   │
├────────────────────────────────────────────────────────────────────┤
│ // DismissalDetails: type, dismissedPlayerId, fielderId, ...      │
└────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Projections & Commentary

```
┌────────────────────────────────────────────────────────────────────┐
│                    ScoreboardProjection                               │
├────────────────────────────────────────────────────────────────────┤
│ - matchId                                                           │
│ - inningsId                                                         │
│ - teamTotalRuns, wickets, oversDisplay: String                      │
│ - strikerStats, nonStrikerStats: BatsmanLiveStats                   │
│ - bowlerStats: BowlerLiveStats                                      │
│ - partnership: PartnershipStats                                     │
│ - lastWickets: List<FallOfWicket>                                   │
│ - recentDeliveries: Deque<DeliveryDTO>   // bounded for UI          │
│ - revision: long                                                    │
├────────────────────────────────────────────────────────────────────┤
│ + applyEvent(e: MatchEvent): void                                   │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                     CommentaryLine                                  │
├────────────────────────────────────────────────────────────────────┤
│ - lineId                                                            │
│ - matchId                                                           │
│ - sequenceNumber / deliveryId                                       │
│ - authorId, locale                                                  │
│ - text: String                                                      │
│ - createdAt                                                         │
└────────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Notifications & Search (Supporting)

```
┌─────────────────────────────┐       ┌─────────────────────────────┐
│      UserSubscription       │       │      NotificationOutbox     │
├─────────────────────────────┤       ├─────────────────────────────┤
│ - userId                    │       │ - id                        │
│ - targetType: MATCH|PLAYER  │       │ - userId                    │
│ - targetId                  │       │ - templateId                │
│ - channels: Set<Channel>    │       │ - payloadJson             │
│ - filters: SubscriptionRule │       │ - dedupeKey UNIQUE        │
└─────────────────────────────┘       │ - status                    │
                                      └─────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│ <<interface>> SearchQueryService                                     │
├────────────────────────────────────────────────────────────────────┤
│ + searchPlayers(q, facets): Page<PlayerSearchHit>                    │
│ + searchMatches(q, facets): Page<MatchSearchHit>                    │
└────────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (35–50 minutes)

### 6.1 Domain: Applying a Delivery (Simplified Java)

```java
// DeliveryCommand.java — what scorer sends
public class DeliveryCommand {
    private final String matchId;
    private final String inningsId;
    private final String scorerClientEventId; // UUID from device — idempotency
    private final String bowlerId;
    private final String strikerId;
    private final String nonStrikerId;
    private final DeliveryLegality legality;
    private final int runsOffBat;
    private final ExtraKind extraKind;
    private final int extraRuns; // excluding runs off bat if counted separately
    private final DismissalDetails dismissal; // nullable
    private final boolean freeHitActive; // scorer indicates UI state
}

// Innings.java — core invariant sketch
public class Innings {
    private int totalRuns;
    private int wickets;
    private int legalBallsBowled;
    private int ballsThisOver; // legal deliveries completed in current over
    private boolean pendingFreeHit;

    public DeliveryRecorded applyDelivery(DeliveryCommand cmd, ScoringRules rules) {
        rules.validate(cmd, this);

        int runsTotal = cmd.getRunsOffBat() + cmd.getExtraRuns();
        // Wide/no-ball: typically increment total, may not increment legal count
        boolean countsAsLegalBall = rules.countsTowardOver(cmd);

        if (countsAsLegalBall) {
            legalBallsBowled++;
            ballsThisOver++;
        }

        totalRuns += runsTotal;

        if (cmd.getDismissal() != null) {
            wickets++;
            ballsThisOver = resetIfOverComplete(rules.ballsPerOver());
        } else {
            ballsThisOver = advanceStrikeAndOver(cmd, rules);
        }

        pendingFreeHit = rules.isFreeHitNext(cmd);

        return new DeliveryRecorded(/* map fields */);
    }

    private int advanceStrikeAndOver(DeliveryCommand cmd, ScoringRules rules) {
        // On odd runs or boundary with strike change rules, swap striker/non-striker
        // If over complete, rotate strike automatically
        // ...format-specific logic...
        int perOver = rules.ballsPerOver();
        if (ballsThisOver >= perOver) {
            ballsThisOver = 0;
            return 0;
        }
        return ballsThisOver;
    }
}
```

---

### 6.2 Scoring Rules Strategy (T20 vs Test excerpt)

```java
public interface ScoringRules {
    int ballsPerOver();
    void validate(DeliveryCommand cmd, Innings innings);
    boolean countsTowardOver(DeliveryCommand cmd);
    boolean isFreeHitNext(DeliveryCommand cmd);
}

public class T20Rules implements ScoringRules {
    @Override
    public int ballsPerOver() { return 6; }

    @Override
    public void validate(DeliveryCommand cmd, Innings innings) {
        if (cmd.getLegality() == DeliveryLegality.NO_BALL && cmd.getExtraRuns() < 1) {
            throw new DomainException("No-ball must award at least 1 run penalty");
        }
        // Powerplay fielding restrictions: enforced using match phase from over number
    }

    @Override
    public boolean countsTowardOver(DeliveryCommand cmd) {
        return cmd.getLegality() == DeliveryLegality.LEGAL_GOOD
            || cmd.getLegality() == DeliveryLegality.LEGAL_DEAD_BALL; // if counted per laws configured
    }

    @Override
    public boolean isFreeHitNext(DeliveryCommand cmd) {
        return cmd.getLegality() == DeliveryLegality.NO_BALL
            || cmd.getLegality() == DeliveryLegality.NO_BALL_WITH_BAT;
    }
}

public class TestRules implements ScoringRules {
    @Override
    public int ballsPerOver() { return 6; }

    @Override
    public void validate(DeliveryCommand cmd, Innings innings) {
        // New ball available after 80 overs — tracked as match-level event, not here
    }

    @Override
    public boolean countsTowardOver(DeliveryCommand cmd) {
        return cmd.getLegality() == DeliveryLegality.LEGAL_GOOD;
    }

    @Override
    public boolean isFreeHitNext(DeliveryCommand cmd) {
        return false; // free hit applies to ODIs/T20s for front-foot no-ball etc.—configure per product
    }
}
```

---

### 6.3 Match Service: Serialize Writes + Publish Event

```java
@Service
public class LiveScoringService {
    private final MatchRepository matches;
    private final DeliveryEventLog eventLog;
    private final MatchEventPublisher publisher;

    @Transactional
    public DeliveryRecorded recordDelivery(DeliveryCommand cmd) {
        if (eventLog.existsByScorerClientId(cmd.getScorerClientEventId())) {
            return eventLog.findRecorded(cmd.getScorerClientEventId()); // idempotent replay
        }

        Match match = matches.findLocked(cmd.getMatchId()); // SELECT ... FOR UPDATE
        Innings innings = match.currentInnings();

        DeliveryRecorded recorded = innings.applyDelivery(cmd, match.getRules());
        recorded.assignSequence(eventLog.nextSequence(cmd.getMatchId()));

        eventLog.append(recorded);
        matches.save(match);

        publisher.publish(recorded); // Kafka / Redis pub / WebSocket bridge
        return recorded;
    }
}
```

---

### 6.4 Projection Consumer (Read Model)

```java
@Component
public class ScoreboardProjectionHandler {

    public void on(DeliveryRecorded e) {
        ScoreboardProjection p = cache.get(e.getMatchId(), ScoreboardProjection::new);
        p.applyDelivery(e);
        if (e.isWicket()) {
            p.appendFallOfWicket(buildFow(e));
            detectMilestones(p, e); // e.g., century → enqueue NotificationOutbox
        }
        cache.put(e.getMatchId(), p);
        searchIndexer.maybeIndex(e); // lightweight, async
    }
}
```

---

### 6.5 Stats Rollup (Async Job Sketch)

```java
// Runs nightly or on match complete — materialize PlayerCareerStats
public class CareerStatsRollupJob {
    public void recomputeForPlayer(String playerId) {
        List<Delivery> balls = warehouse.fetchBattingDeliveries(playerId);
        BattingAggregate agg = new BattingAggregate();
        for (Delivery d : balls) {
            if (d.getStrikerId().equals(playerId)) {
                agg.add(d.getRunsOffBat(), d.countsAsBallFaced());
            }
        }
        agg.setAverageAndStrikeRate();
        statsRepository.upsert(playerId, MatchFormat.ODI, agg);
    }
}
```

---

### 6.6 Demo Wiring (Pseudo)

```java
public class CricinfoDemo {
    public static void main(String[] args) {
        // 1. Schedule T20 match, set squads, toss
        // 2. Start innings — open WebSocket room match:{id}
        // 3. Record dot, wide+1, wicket caught, boundary 6 with free hit chain
        // 4. Show scoreboard projection lag < 1s
        // 5. Issue correction event on last ball
        // 6. Fire notification on 50 for striker
        // 7. Search autocomplete "Koh" → player hits
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `ScoringRules` per `MatchFormat` | Isolate law/format differences without giant switch |
| **Aggregate + DDD** | `Match` / `Innings` | Enforce invariants; single consistency boundary per lock |
| **Event Sourcing (optional)** | `DeliveryEventLog` append-only | Replay projections; audit; dispute resolution |
| **CQRS** | Write: `LiveScoringService`; Read: `ScoreboardProjection` | Scale reads independently; optimize hot path |
| **Observer / Pub-Sub** | `MatchEventPublisher` → WebSocket, notifications, search | Decouple core scoring from fan-out |
| **Saga (soft)** | Interruption + DLS recalculation + revised target | Multi-step workflow with compensations |
| **Repository** | `MatchRepository`, `PlayerRepository` | Testability; swap OLTP store |
| **Template Method** | `AbstractStatsRollup` with format hooks | Shared pipeline for career stats |
| **Factory** | `ScoringRulesFactory.from(MatchFormat)` | Centralize rule wiring |
| **Idempotent Consumer** | Notification worker with `dedupeKey` | At-least-once messaging safe |

---

## 🔒 Step 8: Concurrency Handling (14–20 minutes)

### Problem
Concurrent **readers** are massive; **writers** are few but must never produce **duplicate sequence numbers** or **inconsistent totals**. Caching layers can serve **stale** data; scorers on flaky networks **retry** the same delivery.

### Goals
- **Total order** of deliveries per `matchId` (monotonic `sequenceNumber`)
- **Idempotent** ingestion (`scorerClientEventId` UNIQUE)
- **Linearizable** scoreboard updates per match from scorer perspective
- **Parallel** processing across **different** matches

### Techniques

1. **Single-row lock per match (pessimistic)**  
   `SELECT * FROM matches WHERE match_id=? FOR UPDATE` then append delivery + commit. Simple, sufficient for one active scorer; if two scorers, partition by **role** (primary/secondary) or use **operational transform** queue.

2. **Partition single writer (Kafka partition key = matchId)**  
   All commands for a match go to one partition; consumer updates aggregate. Natural ordering.

3. **Optimistic locking on Match.version**  
   `UPDATE matches SET version=version+1 WHERE match_id=? AND version=?` — retry on conflict for low-contention admin edits.

4. **Sequence generation**  
   Use **DB sequence** per match (`match_delivery_seq` table) or `MAX(sequence)+1` under row lock—avoid UUID-only ordering for UX.

5. **Fencing token for cache primary**  
   Projection stores `lastSequenceApplied`. Ignore out-of-order events below watermark unless marked **correction**.

6. **Correction events**  
   Never delete deliveries; append `DeliveryCorrection` referencing `deliveryId` and delta; projection applies delta or rebuilds from snapshot + tail replay.

7. **Read-your-writes for scorers**  
   Route scorer client to same region; use **sticky session** or **version** in response for UI sync.

8. **WebSocket fan-out**  
   **Ephemeral** state at edge; source of truth remains OLTP + log. Use **Redis Pub/Sub** or **Kafka** to edge workers; avoid DB connection per fan-out thread.

9. **Thundering herd on wicket**  
   **Coalesce** push notifications in 200ms window; sample template rendering; prioritize paying subscribers if tiered.

### What to say in interview
*"I serialize mutations per match—either a row lock or single Kafka partition. Every scorer event carries a client UUID for idempotency. The UI reads a projection that applies events in sequence; corrections are append-only. Fan-out is async so a million subscribers don't block the transaction that records the ball."*

---

## 📊 Step 9: Database Schema (14–20 minutes)

### Core tables (relational sketch)

**venues** (`venue_id`, `name`, `country`, `timezone`, …)

**teams** (`team_id`, `name`, `short_code`, `type` [NATIONAL|FRANCHISE], …)

**players** (`player_id`, `full_name`, `dob`, `country_id`, `batting_hand`, `bowling_style`, …)

**series** (`series_id`, `name`, `season_year`, `tournament_id` NULL, …)

**tournaments** (`tournament_id`, `name`, `format_default`, …)

**matches** (`match_id`, `format`, `venue_id`, `team_home_id`, `team_away_id`, `scheduled_start`, `status`, `toss_winner_team_id`, `toss_decision`, `version`, `updated_at`)

**match_squads** (`match_id`, `player_id`, `team_id`, `role`, `is_playing_xi` bool per innings optional)

**innings** (`innings_id`, `match_id`, `index`, `batting_team_id`, `bowling_team_id`, `status`, `total_runs`, `wickets`, `legal_balls`, `started_at`, `ended_at`)

**deliveries** — append-only  
(`delivery_id`, `match_id`, `innings_id`, `sequence_number` UNIQUE per match, `over_number`, `ball_in_over`, `legality`, `runs_off_bat`, `extra_kind`, `extra_runs`, `total_runs`, `wicket_flag`, `dismissal_type`, `dismissed_player_id`, `bowler_id`, `striker_id`, `non_striker_id`, `fielder_assist_id`, `scorer_client_event_id` UNIQUE, `created_at`)

**delivery_corrections** (`correction_id`, `references_delivery_id`, `reason`, `delta_json`, `created_at`)

**interruptions** (`interruption_id`, `match_id`, `started_at`, `ended_at`, `type` [RAIN|LIGHT|OTHER], `dls_version`, `revised_target_json`)

**commentary_lines** (`line_id`, `match_id`, `sequence_number`, `delivery_id` NULL, `author_id`, `locale`, `text`, `created_at`)

**scoreboard_snapshots** (`match_id`, `innings_id`, `payload_json`, `revision`, `updated_at`) — optional cache table

**player_career_stats** (`player_id`, `format`, `innings_batted`, `runs`, `balls_faced`, `outs`, `avg`, `strike_rate`, `wickets`, `balls_bowled`, `runs_conceded`, `bowling_avg`, `economy`, `bowling_sr`, `updated_at`)

**user_subscriptions** (`user_id`, `target_type`, `target_id`, `channels_json`, `rules_json`)

**notification_outbox** (`id`, `user_id`, `dedupe_key` UNIQUE, `template`, `payload_json`, `status`, `created_at`)

**search_documents** (or delegate to Elasticsearch index: `id`, `type`, `title`, `body`, `attrs_json`)

**audit_log** (`id`, `entity`, `entity_id`, `actor`, `action`, `payload_json`, `ts`)

### Helpful indexes
- `deliveries(match_id, sequence_number)` UNIQUE  
- `deliveries(innings_id, over_number, ball_in_over)`  
- `players(full_name)` trigram / full-text  
- `matches(status, scheduled_start)` for live listings  

---

## 💡 Step 10: Interview Discussion Points (16–24 minutes)

### 1. Ordering vs human corrections
Never reorder rows in UI storage; publish **correction events** so clients can animate reversals. Legal disputes need **immutable audit**.

### 2. DLS and revised targets
Treat DLS as **pure function** `(state, interruption tables) → target` executed in **isolated service**; persist inputs/outputs snapshot for reproducibility.

### 3. Super over
Model as **extra innings** with `innings.index` continuation or `phase=SUPER_OVER` and separate **target chase** rules in `FormatConfig`.

### 4. Partnership and FOW
**Partnership** resets on wicket; **FOW** stores score string like `120/3`; both derivable from stream but cached for latency.

### 5. Ball-facing stats
Byes/leg-byes: striker may not face a ball; **balls faced** increments only when `countsAsBallFaced` in rules (depends on legality).

### 6. Free hit
After most no-balls in white-ball, only **run out** (some modes), **handled ball**, **obstructing**, **hit wicket** (rare laws) apply—product usually simplifies with `dismissalAllowedOnFreeHit` table in rules.

### 7. Real-time at scale
**Poll + ETag** for low-end devices; **SSE/WebSocket** for live; **CDN** for static assets; **gRPC** for official apps if needed.

### 8. Search ranking
Blend **text score**, **recency**, **international caps** weight; cache popular prefixes.

### 9. Multi-language commentary
Store `locale` on lines; **fallback chain** en-US → en → neutral machine translation optional.

### 10. Testing
Property-based tests: sum of delivery `totalRuns` equals innings total after replay; parallel commands same `scorerClientEventId` → single row.

---

## ✅ Step 11: SOLID Principles Verification

### Single Responsibility
- `LiveScoringService` orchestrates transaction boundaries only  
- `ScoringRules` encapsulates law differences  
- `ScoreboardProjection` handles read shaping only  

### Open/Closed
- Add **The Hundred** rules: new `ScoringRules` implementation + config row, no change to ingestion pipeline  

### Liskov Substitution
- Any `ScoringRules` must preserve `validate` pre/post expectations used by `Innings`  

### Interface Segregation
- Split `NotificationSender` (push) from `EmailSender` if templates diverge  

### Dependency Inversion
- `LiveScoringService` depends on `MatchRepository` interface, not concrete JDBC  

---

## 📈 Step 12: Complexity Analysis

| Operation | Time | Notes |
|-----------|------|-------|
| Record delivery (txn) | O(1) | One match row lock + one insert + publish |
| Apply projection event | O(1) amortized | Update fixed-size structures; trim deque |
| Load scoreboard (cache hit) | O(1) | Keyed by `matchId` |
| Load full ball list for match | O(B) | B = deliveries; paginate by sequence |
| Search autocomplete | O(log N) + O(K) | N index size, K suggestions |
| Career stats rollup (full) | O(Bp) | Bp = career balls for player; batch incremental preferred |
| Notification fan-out M users | O(M) work | Parallel workers; bounded by infrastructure |
| Replay match from log | O(B) | Rebuild projections for dispute |

**Scaling mantra:** **Partition everything by `matchId` for writes**; **shard projections and caches**; **never couple fan-out to the DB transaction commit**.

---

## 🎓 Step 13: Key Takeaways

1. **Delivery log is the source of truth** — scoreboard, partnerships, and FOW are **projections** (rebuildable).
2. **Per-match serialization** — sequence monotonicity beats clever distributed locks without a single choke point.
3. **Format complexity belongs in `ScoringRules`**, not scattered `if (t20)` logic.
4. **Corrections are events**, not silent row overwrites — mandatory for trust and analytics.
5. **Real-time scale** requires **async fan-out**; the scoring transaction stays tiny.
6. **Idempotency** (`scorerClientEventId`) is non-negotiable for mobile scorers.
7. **DLS / super over / follow-on** are **configuration + workflow**, not one-off hacks.
8. **Search and stats** are **eventually consistent** services feeding off the same event stream.

**Interview success formula:** Clarify formats → delivery invariants → ordering/idempotency → projections → WebSocket fan-out → corrections → DLS edge case → stats rollup → search.

---

**Cricinfo-style Cricket Score Tracking & Live Updates LLD — Hard difficulty — ready for review.**
