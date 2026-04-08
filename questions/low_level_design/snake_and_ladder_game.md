# Low-Level Design: Snake and Ladder Game

**Difficulty:** Medium ⭐⭐

**Interview Duration:** 45-60 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a Snake and Ladder game system that supports multiple players, configurable boards, dice rules, game modes, AI opponents, and statistics—while keeping the design extensible for custom rules and board sizes."*

### Clarifying Questions to Ask:

1. **Q:** What board sizes should we support?  
   **A:** Classic is 10×10 (cells 1–100). Design for arbitrary N×N or linear 1..M cells via configuration.

2. **Q:** How are snakes and ladders defined?  
   **A:** Pairs (head, tail) for snakes and (bottom, top) for ladders; validate no overlaps/conflicts per ruleset.

3. **Q:** How many players?  
   **A:** Typically 2–4; enforce min/max via `GameConfig`.

4. **Q:** One die or multiple?  
   **A:** Support 1..K dice; sum (or max, depending on variation) drives movement—make pluggable.

5. **Q:** Exact win rule?  
   **A:** Land exactly on last cell, or allow bounce-back if overshoot—configurable `WinRule`.

6. **Q:** Turn order?  
   **A:** Round-robin; optional skip/extra-turn rules in variations.

7. **Q:** Do we need persistence?  
   **A:** In-memory for interview; `GameHistory` interface allows future DB/replay.

8. **Q:** AI difficulty?  
   **A:** Strategy interface: random rolls are fair; optional greedy/heuristic for harder AI.

9. **Q:** Timed mode?  
   **A:** Per-turn or total game clock; timeout can forfeit turn or lose.

10. **Q:** Concurrency?  
    **A:** Single game session is single-threaded logical model; lock if multiple UI clients later.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Board & Setup (FR1-FR6)
1. System shall initialize a board from configurable dimensions or total cell count
2. System shall place snakes and ladders from configuration (start → end pairs)
3. System shall validate board graph (e.g., no immediate cycles, bounds, optional uniqueness rules)
4. System shall support loading preset boards (classic 1–100) and custom layouts
5. System shall expose cell queries: next position after jump from a cell (if any)
6. System shall allow `BoardBuilder` / factory for test-friendly boards

#### Players & Turns (FR7-FR12)
7. System shall register 2–4 players with unique ids, names, and optional `PlayerType` (HUMAN, AI)
8. System shall maintain current player index and advance turn after a legal move completes
9. System shall support turn skip or extra turn when a variation rule triggers
10. System shall not allow inactive players to roll or move (game over or eliminated rules)
11. System shall assign each player a token starting at cell 0 (off-board) or 1 per config
12. System shall support adding/removing players only before game start

#### Dice & Movement (FR13-FR18)
13. System shall roll one or more dice via a `DiceRoller` abstraction
14. System shall compute proposed landing cell from current position + roll total
15. System shall validate moves: cannot move beyond last cell unless `WinRule` allows overshoot handling
16. System shall apply snake descent when landing on a snake head
17. System shall apply ladder climb when landing on a ladder bottom
18. System shall chain jumps if variations allow (usually single jump per landing)

#### Game State & Win (FR19-FR23)
19. System shall maintain immutable snapshots or observable `GameState` (positions, turn, phase)
20. System shall detect win when a player satisfies `WinRule` (typically exact landing on last cell)
21. System shall transition to `FINISHED` with winner id(s); support draw if tie-break rules exist
22. System shall reject operations when state is not `IN_PROGRESS` (except reset/history read)
23. System shall emit events or records for each turn for history and UI

#### History & Modes (FR24-FR30)
24. System shall append each completed action to `GameHistory` (roll, move, jumps, turn change)
25. System shall support game mode `CLASSIC` (standard rules)
26. System shall support `TIMED` mode with per-turn or global time limits
27. System shall support `VARIATION` mode driven by a `RuleSet` (double six, no snakes, etc.)
28. System shall allow switching `RuleSet` only before match starts
29. System shall record statistics per game and aggregate per player profile (optional service)
30. AI players shall obtain decisions through `AIPlayerStrategy` without special-casing core engine

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "Many concurrent games? Huge boards?"
- Single JVM: thousands of idle games possible; active simulations are CPU-light
- Board size may be 10×10 to 20×20+ for variants
- History growth: bounded by turns × players

**Deduced NFRs:**
- ✅ O(1) snake/ladder lookup per cell (hash map)
- ✅ Config-driven behavior avoids code forks for new modes
- ✅ Optional history truncation or external store for long matches

---

#### 2. **Consistency Analysis**

**Think:** "What must stay consistent?"
- One current player at a time (in standard mode)
- Positions updated atomically per move transaction
- Turn order must match history

**Deduced NFRs:**
- ✅ Single-writer model per `GameSession` (synchronized or actor)
- ✅ `MoveResult` bundles position updates + events for replay consistency
- ✅ Validation before mutation (no partial updates on illegal roll)

---

#### 3. **Availability Analysis**

**Think:** "Uptime?"
- Casual game: best-effort; no strict HA requirement in LLD scope
- Timed mode: clock source should be monotonic

**Deduced NFRs:**
- ✅ Graceful handling of timer drift (use `System.nanoTime()` or injected `Clock`)
- ✅ Serializable state for resume (optional checkpoint)

---

#### 4. **Maintainability Analysis**

**Think:** "New rules weekly?"
- New board layouts, new modes, new AI

**Deduced NFRs:**
- ✅ **Strategy + RuleSet** for variations
- ✅ **Builder** for boards and games
- ✅ **Event-sourced history** for debugging and replay UI

---

#### 5. **Performance Analysis**

**Think:** "Hot paths?"
- Roll + move + jump is O(1) with hash maps
- Win check O(P) for P players each turn (P ≤ 4)

**Deduced NFRs:**
- ✅ No graph search per move unless custom rules require it
- ✅ Statistics as O(1) updates per event

---

#### 6. **Security Analysis**

**Think:** "Cheating?"
- Server-authoritative rolls in online play (out of scope: document extension point)
- Validate all inputs against board bounds

**Deduced NFRs:**
- ✅ Pluggable `DiceRoller` (secure random server-side in production)
- ✅ No trust in client-supplied dice values in competitive mode

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Configurable board" | Board, Cell, Dimension |
| "Snake/ladder placement" | Snake, Ladder, JumpLink |
| "2-4 players" | Player, PlayerType |
| "Dice rolling" | Dice, Die, DiceRoller, RollResult |
| "Movement" | Move, Position, MoveValidator |
| "Turn management" | TurnManager, GamePhase |
| "Win condition" | WinRule, Winner |
| "Game state" | GameState, GameSession |
| "History" | GameHistory, GameEvent |
| "Game modes" | GameMode, RuleSet, Timer |
| "AI" | AIPlayerStrategy |
| "Statistics" | GameStatistics, PlayerStats |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Cell | ✅ YES | Addressable position on linearized board |
| Board | ✅ YES | Aggregates cells, jumps, last cell index |
| Snake / Ladder | ❌ NO | Instances of `JumpLink` with type enum |
| JumpLink | ✅ YES | Directed teleport (head→tail, bottom→top) |
| Player | ✅ YES | Identity, type, position |
| Dice / Die | ❌ NO | Rolled by `DiceRoller`; not stored as entity |
| DiceRoller | ✅ YES | Strategy for randomness and dice count |
| Move | ❌ NO | Operation result / value object |
| MoveValidator | ✅ YES | Encapsulates overshoot and legality |
| TurnManager | ✅ YES | Owns current player index |
| WinRule | ✅ YES | Strategy for winning |
| GameState | ✅ YES | Snapshot of positions + phase + turn |
| GameSession | ✅ YES | Orchestrates flow |
| GameHistory | ✅ YES | Ordered events |
| GameEvent | ✅ YES | Record for replay |
| GameMode | ✅ YES | Enum + associated `RuleSet` |
| RuleSet | ✅ YES | Composable rules |
| Timer | ✅ YES | For timed mode (optional composition) |
| AIPlayerStrategy | ✅ YES | Decides when AI “commits” to roll |
| GameStatistics | ✅ YES | Counters derived from events |

### Final Entity List

**Board & Jumps:**
1. **Board** - Linear cells 1..N, jump map
2. **Cell** - Index, optional metadata (theme)
3. **JumpLink** - fromCell, toCell, JumpType (SNAKE, LADDER)
4. **BoardLayout** - Immutable definition used to build `Board`
5. **BoardBuilder** - Validates and constructs `Board`

**Players & Play:**
6. **Player** - id, name, type, currentCellIndex
7. **PlayerType** - Enum (HUMAN, AI)
8. **TurnManager** - current player rotation, skip/extra logic hooks

**Dice & Rules:**
9. **DiceRoller** - Interface; **StandardDiceRoller**, **LoadedDiceRoller** (tests)
10. **RollResult** - values[], sum
11. **MoveValidator** - Validates proposed index against `WinRule` and board end
12. **WinRule** - Interface; **ExactEndWinRule**, **BounceBackWinRule**

**Game Core:**
13. **GamePhase** - Enum (WAITING, IN_PROGRESS, FINISHED)
14. **GameState** - Immutable or copy-on-write snapshot
15. **GameSession** - Facade: roll, move pipeline, win check
16. **GameConfig** - players count bounds, dice count, mode, rule set id

**History & Observability:**
17. **GameHistory** - append/list events
18. **GameEvent** - type, payload, timestamp, seq
19. **GameStatistics** - rolls, snakes hit, ladders hit, turns, duration

**Modes & AI:**
20. **GameMode** - CLASSIC, TIMED, VARIATION
21. **RuleSet** - Interface; composable predicates/post-actions
22. **TurnTimer** - Optional per-turn deadline
23. **AIPlayerStrategy** - Interface for AI turn automation

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Core Relationships

#### GameSession ↔ Board
**Conclusion:** **Association** (session plays on a board)
```
GameSession ─────→ Board [1]
```

#### Board ↔ JumpLink
**Conclusion:** **Composition** (board owns jump map)
```
Board ◆────→ Map<Integer, JumpLink> [0..*]
```

#### GameSession ↔ Player
**Conclusion:** **Aggregation** (players join session)
```
GameSession ◇────→ Player [2..4]
```

---

### Pass 2: Rules & Strategies

#### GameSession ↔ DiceRoller
**Conclusion:** **Association** (injectable)
```
GameSession ─────→ DiceRoller [1]
```

#### GameSession ↔ WinRule / MoveValidator
**Conclusion:** **Association**
```
GameSession ─────→ WinRule [1]
GameSession ─────→ MoveValidator [1]
```

#### Player (AI) ↔ AIPlayerStrategy
**Conclusion:** **Association**
```
Player (type AI) ─────→ AIPlayerStrategy [0..1]
```

---

### Pass 3: History & Statistics

#### GameSession ↔ GameHistory
**Conclusion:** **Composition**
```
GameSession ◆────→ GameHistory [1]
```

#### GameSession ↔ GameStatistics
**Conclusion:** **Composition** (updated on events)
```
GameSession ◆────→ GameStatistics [1]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| GameSession → Board | 1:1 | Association |
| Board → JumpLink | 1:N | Composition (map) |
| GameSession → Player | 1:2..4 | Aggregation |
| GameSession → DiceRoller | 1:1 | Association |
| GameSession → WinRule | 1:1 | Association |
| GameSession → GameHistory | 1:1 | Composition |
| Player → AIPlayerStrategy | 0..1:1 | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>> │
│   PlayerType    │  │    JumpType      │  │   GamePhase     │
├─────────────────┤  ├──────────────────┤  ├─────────────────┤
│ HUMAN           │  │ SNAKE            │  │ WAITING         │
│ AI              │  │ LADDER           │  │ IN_PROGRESS     │
└─────────────────┘  └──────────────────┘  │ FINISHED        │
                                            └─────────────────┘

┌─────────────────┐
│ <<enumeration>> │
│   GameMode      │
├─────────────────┤
│ CLASSIC         │
│ TIMED           │
│ VARIATION       │
└─────────────────┘

┌─────────────────┐
│ <<enumeration>> │
│  GameEventType  │
├─────────────────┤
│ GAME_STARTED    │
│ ROLL            │
│ MOVE            │
│ JUMP            │
│ TURN_CHANGED    │
│ WIN             │
│ TIMER_WARNING   │
└─────────────────┘
```

---

### Class Diagram 2: Board & Jumps

```
┌───────────────────────────────────────────────────────────┐
│                      Board                                │
├───────────────────────────────────────────────────────────┤
│ - lastCellIndex: int                                      │
│ - jumps: Map<Integer, JumpLink>   ◆────────────────┐    │
├───────────────────────────────────────────────────────────┤
│ + Board(lastCell, jumps: Map)                             │
│ + getLastCellIndex(): int                                 │
│ + getJumpFrom(cell: int): Optional<JumpLink>              │
│ + applyJump(cell: int): int   // returns final cell       │
└───────────────────────────────────────────────────────────┘
                          △
                          │ builds
┌───────────────────────────────────────────────────────────┐
│                   BoardBuilder                            │
├───────────────────────────────────────────────────────────┤
│ - lastCellIndex: int                                      │
│ - jumps: Map<Integer, JumpLink>                           │
├───────────────────────────────────────────────────────────┤
│ + setLastCellIndex(n: int): BoardBuilder                  │
│ + addSnake(head: int, tail: int): BoardBuilder            │
│ + addLadder(bottom: int, top: int): BoardBuilder          │
│ + validate(layoutValidator: BoardLayoutValidator): void   │
│ + build(): Board                                          │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                     JumpLink                              │
├───────────────────────────────────────────────────────────┤
│ - fromCell: int                                           │
│ - toCell: int                                             │
│ - type: JumpType                                          │
├───────────────────────────────────────────────────────────┤
│ + JumpLink(from, to, type)                                │
│ + getDestination(): int                                   │
└───────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Player, Dice, Movement

```
┌───────────────────────────────────────────────────────────┐
│                      Player                               │
├───────────────────────────────────────────────────────────┤
│ - id: String                                              │
│ - name: String                                            │
│ - type: PlayerType                                        │
│ - currentCellIndex: int   // 0 = off board before start   │
│ - aiStrategy: AIPlayerStrategy?                           │
├───────────────────────────────────────────────────────────┤
│ + moveTo(cell: int): void                                 │
│ + isOnBoard(): boolean                                    │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│              <<interface>>                               │
│                 DiceRoller                                │
├───────────────────────────────────────────────────────────┤
│ + roll(numDice: int, sides: int): RollResult              │
└───────────────────────────────────────────────────────────┘
                        △
                        │
            ┌───────────┴────────────┐
            │                        │
┌───────────────────────┐  ┌────────────────────────┐
│  StandardDiceRoller   │  │ SecureDiceRoller       │
│  (local Random)       │  │ (injected RNG/CSPRNG)  │
└───────────────────────┘  └────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                    RollResult                             │
├───────────────────────────────────────────────────────────┤
│ - values: int[]                                           │
│ - sum: int                                                │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                  MoveValidator                            │
├───────────────────────────────────────────────────────────┤
│ - winRule: WinRule                                        │
├───────────────────────────────────────────────────────────┤
│ + computeTargetCell(from: int, delta: int, last: int):    │
│       MoveValidationResult                                │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│              <<interface>>                                │
│                  WinRule                                  │
├───────────────────────────────────────────────────────────┤
│ + hasWon(cellIndex: int, lastCell: int): boolean          │
└───────────────────────────────────────────────────────────┘
            △                        △
            │                        │
┌───────────────────────┐  ┌────────────────────────┐
│  ExactEndWinRule      │  │ BounceBackWinRule      │
└───────────────────────┘  └────────────────────────┘
```

---

### Class Diagram 4: Game Session, Turn, History

```
┌───────────────────────────────────────────────────────────┐
│                    GameSession                            │
├───────────────────────────────────────────────────────────┤
│ - config: GameConfig                                      │
│ - board: Board                                            │
│ - players: List<Player>                                   │
│ - turnManager: TurnManager                                │
│ - diceRoller: DiceRoller                                  │
│ - moveValidator: MoveValidator                            │
│ - phase: GamePhase                                        │
│ - history: GameHistory           ◆──────────┐             │
│ - statistics: GameStatistics     ◆──────────┼─┐           │
│ - ruleSet: RuleSet               ─────────────┼─┐         │
│ - turnTimer: TurnTimer?          ─────────────┼─┼─┐       │
├───────────────────────────────────────────────────────────┤
│ + start(): void                                           │
│ + rollDice(): MoveResult   // human or unified API        │
│ + executeAITurns(): void   // loop while current is AI    │
│ + getState(): GameState                                   │
│ + getWinner(): Optional<Player>                           │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                   TurnManager                             │
├───────────────────────────────────────────────────────────┤
│ - players: List<Player>                                   │
│ - currentIndex: int                                       │
├───────────────────────────────────────────────────────────┤
│ + currentPlayer(): Player                                 │
│ + advanceAfterTurn(ruleSet: RuleSet): void                │
│ + grantExtraTurn(): void                                  │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                   GameHistory                             │
├───────────────────────────────────────────────────────────┤
│ - events: List<GameEvent>                                 │
├───────────────────────────────────────────────────────────┤
│ + append(e: GameEvent): void                              │
│ + getEvents(): List<GameEvent>                            │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                    GameEvent                              │
├───────────────────────────────────────────────────────────┤
│ - seq: long                                               │
│ - type: GameEventType                                     │
│ - playerId: String                                        │
│ - payload: Map<String,Object>                             │
│ - timestampMs: long                                       │
└───────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Modes, Rules, AI, Statistics

```
┌───────────────────────────────────────────────────────────┐
│              <<interface>>                               │
│                  RuleSet                                  │
├───────────────────────────────────────────────────────────┤
│ + onRollCompleted(ctx: RuleContext): TurnAdjustment       │
│ + allowJump(link: JumpLink): boolean                      │
│ + sidesPerDie(): int                                      │
│ + diceCount(): int                                        │
└───────────────────────────────────────────────────────────┘
            △
            │
┌───────────────────────┐  ┌────────────────────────┐
│  ClassicRuleSet       │  │ NoSnakesRuleSet        │
└───────────────────────┘  └────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│              <<interface>>                               │
│              AIPlayerStrategy                             │
├───────────────────────────────────────────────────────────┤
│ + shouldRoll(state: GameState): boolean                   │
└───────────────────────────────────────────────────────────┘
            △
            │
┌───────────────────────┐  ┌────────────────────────┐
│  RandomAI             │  │ AggressiveAI (heuristic) │
└───────────────────────┘  └────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                 GameStatistics                            │
├───────────────────────────────────────────────────────────┤
│ - totalTurns: int                                         │
│ - rollsByPlayer: Map<String,Integer>                      │
│ - snakesHit: Map<String,Integer>                            │
│ - laddersHit: Map<String,Integer>                         │
│ - gameStartMs: long                                       │
│ - gameEndMs: long                                         │
├───────────────────────────────────────────────────────────┤
│ + onEvent(e: GameEvent): void                             │
│ + summary(): StatisticsSummary                            │
└───────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums & Value Objects

```java
// PlayerType.java
public enum PlayerType { HUMAN, AI }

// JumpType.java
public enum JumpType { SNAKE, LADDER }

// GamePhase.java
public enum GamePhase { WAITING, IN_PROGRESS, FINISHED }

// GameMode.java
public enum GameMode { CLASSIC, TIMED, VARIATION }

// GameEventType.java
public enum GameEventType {
    GAME_STARTED, ROLL, MOVE, JUMP, TURN_CHANGED, WIN, TIMER_WARNING, FORFEIT_TURN
}
```

```java
// RollResult.java
public final class RollResult {
    private final int[] values;
    private final int sum;

    public RollResult(int[] values) {
        this.values = values.clone();
        int s = 0;
        for (int v : this.values) s += v;
        this.sum = s;
    }
    public int[] getValues() { return values.clone(); }
    public int getSum() { return sum; }
}
```

```java
// JumpLink.java
public final class JumpLink {
    private final int fromCell;
    private final int toCell;
    private final JumpType type;

    public JumpLink(int fromCell, int toCell, JumpType type) {
        if (fromCell == toCell) throw new IllegalArgumentException("Degenerate jump");
        this.fromCell = fromCell;
        this.toCell = toCell;
        this.type = type;
    }
    public int getFromCell() { return fromCell; }
    public int getToCell() { return toCell; }
    public JumpType getType() { return type; }
}
```

---

### Board & Builder

```java
// Board.java
import java.util.*;

public class Board {
    private final int lastCellIndex;
    private final Map<Integer, JumpLink> jumps;

    public Board(int lastCellIndex, Map<Integer, JumpLink> jumps) {
        if (lastCellIndex < 1) throw new IllegalArgumentException("Invalid board size");
        this.lastCellIndex = lastCellIndex;
        this.jumps = Collections.unmodifiableMap(new HashMap<>(jumps));
    }

    public int getLastCellIndex() { return lastCellIndex; }

    public Optional<JumpLink> getJumpFrom(int cell) {
        return Optional.ofNullable(jumps.get(cell));
    }

    /** Follow at most one jump from landing cell (classic rule). */
    public int applyJumpIfAny(int landingCell) {
        return jumps.containsKey(landingCell)
            ? jumps.get(landingCell).getToCell()
            : landingCell;
    }
}
```

```java
// BoardBuilder.java
import java.util.*;

public class BoardBuilder {
    private int lastCellIndex = 100;
    private final Map<Integer, JumpLink> jumps = new HashMap<>();

    public BoardBuilder lastCell(int n) {
        this.lastCellIndex = n;
        return this;
    }

    public BoardBuilder addSnake(int head, int tail) {
        validatePair(head, tail);
        if (head <= tail) throw new IllegalArgumentException("Snake head must be above tail");
        jumps.put(head, new JumpLink(head, tail, JumpType.SNAKE));
        return this;
    }

    public BoardBuilder addLadder(int bottom, int top) {
        validatePair(bottom, top);
        if (bottom >= top) throw new IllegalArgumentException("Ladder bottom must be below top");
        jumps.put(bottom, new JumpLink(bottom, top, JumpType.LADDER));
        return this;
    }

    private void validatePair(int a, int b) {
        if (a < 1 || b < 1 || a > lastCellIndex || b > lastCellIndex) {
            throw new IllegalArgumentException("Jump outside board");
        }
        if (jumps.containsKey(a)) throw new IllegalArgumentException("Duplicate jump source: " + a);
    }

    public Board build() {
        return new Board(lastCellIndex, jumps);
    }
}
```

---

### Win Rule & Move Validation

```java
// WinRule.java
public interface WinRule {
    boolean hasWon(int cellIndex, int lastCell);
}

// ExactEndWinRule.java
public class ExactEndWinRule implements WinRule {
    @Override
    public boolean hasWon(int cellIndex, int lastCell) {
        return cellIndex == lastCell;
    }
}
```

```java
// MoveValidationResult.java
public final class MoveValidationResult {
    private final int targetCell;
    private final boolean moved; // false if overshoot disallowed and roll wasted

    public MoveValidationResult(int targetCell, boolean moved) {
        this.targetCell = targetCell;
        this.moved = moved;
    }
    public int getTargetCell() { return targetCell; }
    public boolean isMoved() { return moved; }
}

// MoveValidator.java
public class MoveValidator {
    private final boolean exactLandingRequired;

    public MoveValidator(boolean exactLandingRequired) {
        this.exactLandingRequired = exactLandingRequired;
    }

    /**
     * Classic exact end: if from + delta > last, stay put (no move).
     */
    public MoveValidationResult computeTarget(int from, int delta, int lastCell) {
        int proposed = from + delta;
        if (proposed > lastCell) {
            if (exactLandingRequired) {
                return new MoveValidationResult(from, false);
            }
            // Variation: bounce back example
            int overshoot = proposed - lastCell;
            return new MoveValidationResult(lastCell - overshoot, true);
        }
        return new MoveValidationResult(proposed, true);
    }
}
```

---

### Dice

```java
// DiceRoller.java
public interface DiceRoller {
    RollResult roll(int numDice, int sides);
}

// StandardDiceRoller.java
import java.util.concurrent.ThreadLocalRandom;

public class StandardDiceRoller implements DiceRoller {
    @Override
    public RollResult roll(int numDice, int sides) {
        int[] v = new int[numDice];
        for (int i = 0; i < numDice; i++) {
            v[i] = 1 + ThreadLocalRandom.current().nextInt(sides);
        }
        return new RollResult(v);
    }
}
```

---

### Player, Turn, History, Statistics

```java
// Player.java
public class Player {
    private final String id;
    private final String name;
    private final PlayerType type;
    private int currentCellIndex; // 0 = off board
    private AIPlayerStrategy aiStrategy;

    public Player(String id, String name, PlayerType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.currentCellIndex = 0;
    }

    public void moveTo(int cell) { this.currentCellIndex = cell; }
    public String getId() { return id; }
    public String getName() { return name; }
    public PlayerType getType() { return type; }
    public int getCurrentCellIndex() { return currentCellIndex; }
    public boolean isOnBoard() { return currentCellIndex >= 1; }

    public void setAiStrategy(AIPlayerStrategy s) { this.aiStrategy = s; }
    public AIPlayerStrategy getAiStrategy() { return aiStrategy; }
}
```

```java
// TurnManager.java
import java.util.*;

public class TurnManager {
    private final List<Player> players;
    private int currentIndex;
    private boolean extraTurnPending;

    public TurnManager(List<Player> players) {
        if (players.size() < 2 || players.size() > 4) {
            throw new IllegalArgumentException("2-4 players required");
        }
        this.players = new ArrayList<>(players);
        this.currentIndex = 0;
    }

    public Player currentPlayer() {
        return players.get(currentIndex);
    }

    public void grantExtraTurn() {
        this.extraTurnPending = true;
    }

    public void advance() {
        if (extraTurnPending) {
            extraTurnPending = false;
            return;
        }
        currentIndex = (currentIndex + 1) % players.size();
    }
}
```

```java
// GameHistory.java & GameEvent.java
import java.util.*;

public class GameHistory {
    private final List<GameEvent> events = new ArrayList<>();
    private long seq = 0;

    public synchronized GameEvent append(GameEventType type, String playerId, Map<String, Object> payload) {
        GameEvent e = new GameEvent(++seq, type, playerId, payload, System.currentTimeMillis());
        events.add(e);
        return e;
    }

    public List<GameEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }
}

public final class GameEvent {
    private final long seq;
    private final GameEventType type;
    private final String playerId;
    private final Map<String, Object> payload;
    private final long timestampMs;

    public GameEvent(long seq, GameEventType type, String playerId,
                     Map<String, Object> payload, long timestampMs) {
        this.seq = seq;
        this.type = type;
        this.playerId = playerId;
        this.payload = Collections.unmodifiableMap(new HashMap<>(payload));
        this.timestampMs = timestampMs;
    }
    public long getSeq() { return seq; }
    public GameEventType getType() { return type; }
    public String getPlayerId() { return playerId; }
    public Map<String, Object> getPayload() { return payload; }
    public long getTimestampMs() { return timestampMs; }
}
```

```java
// GameStatistics.java
import java.util.*;

public class GameStatistics {
    private int totalTurns;
    private final Map<String, Integer> rollsByPlayer = new HashMap<>();
    private final Map<String, Integer> snakesHit = new HashMap<>();
    private final Map<String, Integer> laddersHit = new HashMap<>();
    private long gameStartMs;
    private Long gameEndMs;

    public void onGameStart() {
        this.gameStartMs = System.currentTimeMillis();
    }

    public void onEvent(GameEvent e) {
        switch (e.getType()) {
            case ROLL -> rollsByPlayer.merge(e.getPlayerId(), 1, Integer::sum);
            case JUMP -> {
                String jt = String.valueOf(e.getPayload().getOrDefault("jumpType", ""));
                if ("SNAKE".equals(jt)) snakesHit.merge(e.getPlayerId(), 1, Integer::sum);
                if ("LADDER".equals(jt)) laddersHit.merge(e.getPlayerId(), 1, Integer::sum);
            }
            case TURN_CHANGED -> totalTurns++;
            case WIN -> gameEndMs = System.currentTimeMillis();
            default -> { }
        }
    }

    public long durationMs() {
        long end = gameEndMs != null ? gameEndMs : System.currentTimeMillis();
        return end - gameStartMs;
    }
}
```

---

### RuleSet & AI

```java
// TurnAdjustment.java — result of rule hooks
public final class TurnAdjustment {
    private final boolean grantExtraTurn;
    public TurnAdjustment(boolean grantExtraTurn) { this.grantExtraTurn = grantExtraTurn; }
    public boolean isGrantExtraTurn() { return grantExtraTurn; }
}

// RuleContext.java (minimal)
public final class RuleContext {
    private final RollResult roll;
    public RuleContext(RollResult roll) { this.roll = roll; }
    public RollResult getRoll() { return roll; }
}

// RuleSet.java
public interface RuleSet {
    default TurnAdjustment onRollCompleted(RuleContext ctx) {
        return new TurnAdjustment(false);
    }
    default boolean allowJump(JumpLink link) { return true; }
    default int sidesPerDie() { return 6; }
    default int diceCount() { return 1; }
}

// ClassicRuleSet.java
public class ClassicRuleSet implements RuleSet { }

// AIPlayerStrategy.java
public interface AIPlayerStrategy {
    boolean shouldRoll(GameStateView state);
}

// RandomAI.java
public class RandomAI implements AIPlayerStrategy {
    @Override
    public boolean shouldRoll(GameStateView state) {
        return true; // always proceed; real games may add delay/UI
    }
}

/** Narrow interface to avoid circular deps */
public interface GameStateView {
    String currentPlayerId();
}
```

---

### GameState & Session (core flow)

```java
// GameState.java
import java.util.*;

public final class GameState {
    private final GamePhase phase;
    private final String currentPlayerId;
    private final Map<String, Integer> positions;

    public GameState(GamePhase phase, String currentPlayerId, Map<String, Integer> positions) {
        this.phase = phase;
        this.currentPlayerId = currentPlayerId;
        this.positions = Collections.unmodifiableMap(new HashMap<>(positions));
    }
    public GamePhase getPhase() { return phase; }
    public String getCurrentPlayerId() { return currentPlayerId; }
    public Map<String, Integer> getPositions() { return positions; }
}
```

```java
// MoveResult.java — outcome of a turn action
public final class MoveResult {
    private final RollResult roll;
    private final int beforeCell;
    private final int afterCell;
    private final boolean won;

    public MoveResult(RollResult roll, int beforeCell, int afterCell, boolean won) {
        this.roll = roll;
        this.beforeCell = beforeCell;
        this.afterCell = afterCell;
        this.won = won;
    }
    public RollResult getRoll() { return roll; }
    public int getAfterCell() { return afterCell; }
    public boolean isWon() { return won; }
}
```

```java
// GameSession.java
import java.util.*;
import java.util.stream.Collectors;

public class GameSession implements GameStateView {
    private final Board board;
    private final List<Player> players;
    private final TurnManager turnManager;
    private final DiceRoller diceRoller;
    private final MoveValidator moveValidator;
    private final WinRule winRule;
    private final GameHistory history;
    private final GameStatistics statistics;
    private final RuleSet ruleSet;

    private GamePhase phase = GamePhase.WAITING;
    private Player winner;

    public GameSession(Board board, List<Player> players, DiceRoller diceRoller,
                       MoveValidator moveValidator, WinRule winRule, RuleSet ruleSet) {
        this.board = board;
        this.players = new ArrayList<>(players);
        this.turnManager = new TurnManager(this.players);
        this.diceRoller = diceRoller;
        this.moveValidator = moveValidator;
        this.winRule = winRule;
        this.history = new GameHistory();
        this.statistics = new GameStatistics();
        this.ruleSet = ruleSet;
    }

    public void start() {
        if (phase != GamePhase.WAITING) throw new IllegalStateException("Already started");
        phase = GamePhase.IN_PROGRESS;
        statistics.onGameStart();
        record(GameEventType.GAME_STARTED, "SYSTEM", Map.of(
            "lastCell", board.getLastCellIndex(),
            "players", players.stream().map(Player::getId).toList()
        ));
    }

    private void record(GameEventType type, String playerId, Map<String, Object> payload) {
        GameEvent e = history.append(type, playerId, payload);
        statistics.onEvent(e);
    }

    /** Unified roll + move for current player. */
    public synchronized MoveResult rollDice() {
        if (phase != GamePhase.IN_PROGRESS) throw new IllegalStateException("Not in progress");
        Player p = turnManager.currentPlayer();

        RollResult roll = diceRoller.roll(ruleSet.diceCount(), ruleSet.sidesPerDie());
        record(GameEventType.ROLL, p.getId(), Map.of("values", Arrays.toString(roll.getValues()), "sum", roll.getSum()));

        int from = p.getCurrentCellIndex();
        MoveValidationResult mv = moveValidator.computeTarget(from, roll.getSum(), board.getLastCellIndex());
        if (!mv.isMoved()) {
            record(GameEventType.MOVE, p.getId(), Map.of("from", from, "to", from, "skipped", true));
            applyTurnRules(roll);
            turnManager.advance();
            record(GameEventType.TURN_CHANGED, turnManager.currentPlayer().getId(), Map.of());
            return new MoveResult(roll, from, from, false);
        }

        int landing = mv.getTargetCell();
        record(GameEventType.MOVE, p.getId(), Map.of("from", from, "to", landing));

        Optional<JumpLink> jump = board.getJumpFrom(landing);
        int finalCell = landing;
        if (jump.isPresent() && ruleSet.allowJump(jump.get())) {
            finalCell = board.applyJumpIfAny(landing);
            record(GameEventType.JUMP, p.getId(), Map.of(
                "from", landing, "to", finalCell, "jumpType", jump.get().getType().name()
            ));
        }

        p.moveTo(finalCell);

        boolean won = winRule.hasWon(finalCell, board.getLastCellIndex());
        if (won) {
            phase = GamePhase.FINISHED;
            winner = p;
            record(GameEventType.WIN, p.getId(), Map.of("cell", finalCell));
        } else {
            applyTurnRules(roll);
            turnManager.advance();
            record(GameEventType.TURN_CHANGED, turnManager.currentPlayer().getId(), Map.of());
        }

        return new MoveResult(roll, from, finalCell, won);
    }

    private void applyTurnRules(RollResult roll) {
        TurnAdjustment adj = ruleSet.onRollCompleted(new RuleContext(roll));
        if (adj.isGrantExtraTurn()) {
            turnManager.grantExtraTurn();
        }
    }

    public GameState getState() {
        Map<String, Integer> pos = players.stream()
            .collect(Collectors.toMap(Player::getId, Player::getCurrentCellIndex));
        return new GameState(phase, turnManager.currentPlayer().getId(), pos);
    }

    @Override
    public String currentPlayerId() {
        return turnManager.currentPlayer().getId();
    }

    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    public GameHistory getHistory() { return history; }
    public GameStatistics getStatistics() { return statistics; }

    /** Run consecutive AI turns until human's turn or game over. */
    public void executeAITurns() {
        while (phase == GamePhase.IN_PROGRESS &&
               turnManager.currentPlayer().getType() == PlayerType.AI) {
            AIPlayerStrategy s = turnManager.currentPlayer().getAiStrategy();
            if (s != null && !s.shouldRoll(getState())) break;
            rollDice();
        }
    }
}
```

---

### Demo

```java
// SnakeLadderDemo.java
import java.util.*;

public class SnakeLadderDemo {
    public static void main(String[] args) {
        Board board = new BoardBuilder()
            .lastCell(100)
            .addLadder(1, 38)
            .addSnake(99, 54)
            .addLadder(5, 14)
            .build();

        List<Player> players = List.of(
            new Player("p1", "Alice", PlayerType.HUMAN),
            new Player("p2", "Bot", PlayerType.AI)
        );
        players.get(1).setAiStrategy(new RandomAI());

        GameSession game = new GameSession(
            board,
            players,
            new StandardDiceRoller(),
            new MoveValidator(true),
            new ExactEndWinRule(),
            new ClassicRuleSet()
        );

        game.start();

        // Simulate until someone wins (cap iterations for demo)
        for (int i = 0; i < 500 && game.getState().getPhase() == GamePhase.IN_PROGRESS; i++) {
            game.executeAITurns();
            if (game.getState().getPhase() != GamePhase.IN_PROGRESS) break;
            game.rollDice(); // human
        }

        game.getWinner().ifPresent(w ->
            System.out.println("Winner: " + w.getName())
        );
        System.out.println("Events: " + game.getHistory().getEvents().size());
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** `DiceRoller`, `WinRule`, `RuleSet`, `AIPlayerStrategy`  
**Why:** Swap randomness source, win semantics, house rules, and AI behavior without changing `GameSession`.  
**Interview Justification:** "New corporate-branded boards often need custom rules; strategies keep the engine stable."

---

### Pattern 2: Builder Pattern
**Where:** `BoardBuilder`  
**Why:** Construct valid boards step-by-step; centralize validation.  
**Interview Justification:** "Classic 100-cell preset and custom 8×8 variants share the same construction pipeline."

---

### Pattern 3: Template Method (lightweight)
**Where:** `GameSession.rollDice()` defines the turn pipeline; `RuleSet` hooks customize branches.  
**Why:** Fixed order: roll → validate → move → jump → win → turn advance.  
**Interview Justification:** "Interviewers want to see one clear orchestration method."

---

### Pattern 4: Observer / Listener (extension)
**Where:** `GameHistory` + `GameStatistics` (wire `history.registerListener(stats)`)  
**Why:** Decouple analytics from core logic.  
**Interview Justification:** "Replay UI and leaderboards subscribe to the same event stream."

---

## 💡 Step 8: Interview Discussion Points

### 1. Extensibility: Custom Board Sizes and Graphs

**Interviewer:** "How do you support non-classic boards?"

**Answer:**
"- Represent the play track as **1..N** linear cells regardless of visual grid; N = rows × cols.  
- **BoardBuilder** accepts arbitrary jumps; optional `BoardLayoutValidator` checks:  
  - unique jump sources  
  - no ladder/snake pointing outside bounds  
  - optional policy: forbid jumps into another jump cell (configurable).  
- **WinRule** and **MoveValidator** only depend on `lastCellIndex`, not geometry."

---

### 2. Overshoot Rules: Exact vs Bounce

**Interviewer:** "What if roll overshoots 100?"

**Answer:**
"**Exact (classic):** player stays; turn ends. Implemented in `MoveValidator` with `moved=false`.  
**Bounce-back variation:** reflect overshoot: `target = last - (proposed - last)`.  
**WinRule** stays separate: bounce may still need exact end depending on product.  
Keeping these in two classes avoids `if (mode == …)` scattered in the session."

---

### 3. Multiple Dice and Variations

**Interviewer:** "Two dice, or sum of max of two?"

**Answer:**
"`RuleSet.diceCount()` and optional `RollAggregator` interface if product needs **sum**, **max**, or **min**.  
`DiceRoller` only returns faces; aggregation is a small strategy between roll and move.  
Example: **Double-six bonus** implemented in `onRollCompleted` → `grantExtraTurn()`."

---

### 4. Timed Mode

**Interviewer:** "How does timed mode fit?"

**Answer:**
"Compose `TurnTimer` into `GameSession`. On `startTurn`, schedule deadline; on timeout, append `FORFEIT_TURN` or `TIMER_WARNING` events, then `turnManager.advance()`.  
Use injected `Clock` for testability.  
**Statistics** track `durationMs` and count forfeits."

---

### 5. AI: Fairness vs Strength

**Interviewer:** "How do you implement AI?"

**Answer:**
"**RandomAI:** always roll—fair, good default.  
**Heuristic AI:** score moves by distance to end minus expected snakes; requires Monte Carlo or shallow search—keep behind `AIPlayerStrategy`.  
Never embed AI branches inside `rollDice()`; the session only checks `PlayerType` in `executeAITurns()`."

---

### 6. History, Replay, and Cheating in Online Play

**Interviewer:** "Can players cheat?"

**Answer:**
"For online, **server-owned** `GameSession` + `SecureDiceRoller` (CSPRNG). Clients send **intent** (e.g., roll request), not outcomes.  
`GameHistory` is append-only and can be signed or hashed for audit.  
Replay = re-apply events deterministically with same RNG seeds if recorded."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `Board`: topology and jumps only
- `TurnManager`: rotation only
- `MoveValidator`: overshoot policy only
- `GameSession`: orchestration only

### Open/Closed ✅
```java
public class DoubleSixRuleSet implements RuleSet {
    @Override
    public TurnAdjustment onRollCompleted(RuleContext ctx) {
        int[] v = ctx.getRoll().getValues();
        if (v.length == 2 && v[0] == 6 && v[1] == 6) {
            return new TurnAdjustment(true);
        }
        return new TurnAdjustment(false);
    }
}
```

### Liskov Substitution ✅
- Any `DiceRoller` / `WinRule` / `RuleSet` implementation usable without changing callers

### Interface Segregation ✅
- `GameStateView` for AI; narrow surface instead of full `GameSession`

### Dependency Inversion ✅
```java
public GameSession(Board board, List<Player> players,
                   DiceRoller diceRoller, WinRule winRule, RuleSet ruleSet) {
    // depend on abstractions
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Linearized board** with O(1) jump map for any grid size
- ✅ **RuleSet + Strategy** for modes (classic, timed, variations)
- ✅ **MoveValidator + WinRule** split overshoot vs victory
- ✅ **Event log** for history, replay, and statistics
- ✅ **2–4 players** with pluggable AI

### Game Flow
- ✅ Roll → validate (no illegal overshoot) → move → optional jump → win check → rules → next turn

### Extensibility
- ✅ Custom snakes/ladders, dice count, aggregators, timers, and AI without rewriting core session

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
