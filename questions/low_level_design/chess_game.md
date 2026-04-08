# Low-Level Design: Chess Game System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a chess game engine that supports full FIDE rules, human vs human or human vs AI, clocks, ratings, draw detection, and persistence."*

### Clarifying Questions to Ask:

1. **Q:** Do we need full chess rules or a simplified subset?  
   **A:** Full rules: all piece moves, castling, en passant, promotion, check/checkmate/stalemate, and standard draw rules.

2. **Q:** Is this online multiplayer, local hot-seat, or single-player vs AI?  
   **A:** Support local two-player and AI opponent; online can be a thin layer over the same core engine.

3. **Q:** How should we represent the board and moves?  
   **A:** 8×8 grid, algebraic notation for I/O; internal representation can be arrays or bitboards (discuss trade-offs).

4. **Q:** What about move validation performance?  
   **A:** Legal move generation must be fast enough for interactive play and AI search (thousands to millions of nodes).

5. **Q:** Undo/redo scope?  
   **A:** Full move history with ability to step back/forward through the game tree of applied moves.

6. **Q:** Time controls?  
   **A:** Blitz, rapid, classical presets with optional increment/delay (simplified Fischer clock).

7. **Q:** Elo — for registered users only or also guests?  
   **A:** Persisted rating per player account; optional casual games without rating update.

8. **Q:** Draw claims — automatic or player-initiated?  
   **A:** Engine auto-detects 50-move, threefold, insufficient material; stalemate/checkmate automatic; optional "offer draw" for human agreement.

9. **Q:** AI strength vs speed?  
   **A:** Minimax with alpha-beta pruning; depth configurable; optional piece-square tables / ordering heuristics.

10. **Q:** Persistence format?  
    **A:** JSON or PGN-like snapshot: FEN for position + metadata (clocks, move list, rights).

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Core Game & Board (FR1-FR8)
1. System shall initialize an 8×8 board with standard starting piece placement
2. System shall assign each piece a type (King, Queen, Rook, Bishop, Knight, Pawn) and color (White, Black)
3. System shall enforce correct movement geometry per piece type (sliding, leaping, pawn rules)
4. System shall block moves through occupied squares for sliding pieces (except knight)
5. System shall capture opponent pieces by moving to their square
6. System shall alternate turns between White and Black starting with White
7. System shall reject illegal moves and explain reason (optional: wrong turn, blocked, leaves king in check)
8. System shall update castling rights, en passant target, halfmove clock, and fullmove number per FIDE semantics

#### Special Moves (FR9-FR12)
9. System shall support kingside and queenside castling with all validity checks (not in check, path not attacked, king/rook unmoved, empty path)
10. System shall support en passant on the move immediately following a double pawn push
11. System shall support pawn promotion to Queen, Rook, Bishop, or Knight when reaching the last rank
12. System shall record special move type in history for correct undo/redo

#### Check, Checkmate, Stalemate (FR13-FR16)
13. System shall detect when a king is in check after any position change
14. System shall disallow any move that leaves the active color’s king in check (or fails to remove existing check)
15. System shall declare checkmate when side to move is in check and has no legal moves
16. System shall declare stalemate when side to move is not in check and has no legal moves

#### Move Validation & Legal Moves (FR17-FR20)
17. System shall generate all pseudo-legal moves then filter moves that leave own king in check
18. System shall expose “legal moves from square” and “all legal moves” APIs for UI and AI
19. System shall detect pinned pieces and restrict their movement along pin ray where applicable
20. System shall validate king safety efficiently (minimize full board copies where possible)

#### History & State (FR21-FR24)
21. System shall maintain ordered move history with enough state to undo/redo (including captured pieces, castling rights, EP square, clocks)
22. System shall support undo and redo stacks (or single history with cursor)
23. System shall expose current game phase: IN_PROGRESS, CHECKMATE, STALEMATE, DRAW_*, WHITE_RESIGNED, etc.
24. System shall evaluate draw conditions: 50-move rule, threefold repetition, insufficient material

#### Time & Players (FR25-FR30)
25. System shall support configurable time controls: Blitz (e.g., 3+2), Rapid (e.g., 10+0), Classical (e.g., 90+30)
26. System shall decrement the active player’s clock during their turn; switch clock on move commit
27. System shall apply increment/delay per control type after each move
28. System shall flag loss on timeout (optional: auto-draw in dead positions)
29. System shall associate games with two players (human or AI)
30. System shall update Elo ratings after rated games complete

#### AI (FR31-FR33)
31. System shall provide an AI opponent using minimax with alpha-beta pruning
32. System shall use a static evaluation function (material, piece-square, mobility optional)
33. System shall support configurable search depth and time budget per move

#### Persistence (FR34-FR36)
34. System shall save game state to storage (file/DB) including FEN + move list + metadata
35. System shall load saved games and resume from last position
36. System shall export/import PGN or JSON for interoperability

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "Many concurrent games? Leaderboards? AI load?"
- Casual scale: thousands of concurrent online games
- AI search is CPU-heavy; may offload to worker or cap depth
- Rating updates must handle concurrent finished games

**Deduced NFRs:**
- ✅ Stateless engine core where possible; scale API horizontally
- ✅ Pluggable AI (local vs remote worker)
- ✅ Efficient move generation to support deep search
- ✅ O(1) or O(n) legal move listing with small n (~20–40 typical)

---

#### 2. **Consistency Analysis**

**Think:** "What must never be wrong?"
- Illegal positions must never be committed as “official” state
- Ratings and game results must be consistent with outcome
- Clocks must not double-charge or skip turns

**Deduced NFRs:**
- ✅ **Strong consistency** for a single game session (one writer per game)
- ✅ **Atomic move application**: validate → apply → update derivatives (check, outcome) in one transaction
- ✅ **Deterministic** repetition detection given move history
- ✅ **Versioned saves** to detect stale load overwrites

---

#### 3. **Availability Analysis**

**Think:** "Chess downtime impact?"
- Mostly soft real-time; players expect low-latency move acceptance
- Persistence failures should not silently corrupt games

**Deduced NFRs:**
- ✅ Graceful degradation: AI falls back to lower depth on CPU pressure
- ✅ Retry and idempotent save operations
- ✅ Reconnection restores state from server snapshot

---

#### 4. **Maintainability Analysis**

**Think:** "Rules are complex and evolve (variants)?"
- Separate rules from UI and transport
- Unit-test per rule (castling, EP, pins, discovery)

**Deduced NFRs:**
- ✅ **Rule classes** per concern (movement, special moves, termination)
- ✅ **Comprehensive tests**: known positions from chess problem suites
- ✅ **PGN/FEN** compatibility for debugging and regression

---

#### 5. **Performance Analysis**

**Think:** "Hot paths?"
- Legal move generation every turn and at every AI node
- Check detection called heavily during filtering

**Deduced NFRs:**
- ✅ **Target:** generate legal moves in sub-millisecond on typical hardware for midgame
- ✅ **Optimized check test:** attack maps, king ray scan, or precomputed attacks
- ✅ **Alpha-beta** with move ordering (captures first, killer moves optional)
- ✅ Avoid deep cloning full board per node; use copy-make or unmake

---

#### 6. **Security Analysis**

**Think:** "Cheating, tampering?"
- Client-submitted moves must be revalidated server-side
- Save files can be edited; server authoritative for rated play

**Deduced NFRs:**
- ✅ **Server-side validation** for online rated games
- ✅ **Signed snapshots** or server-stored truth
- ✅ **Rate limiting** move submissions; detect engine assistance patterns (behavioral, out of LLD scope)

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Initialize board" | Board, Square, Piece, Game |
| "Piece movement rules" | Piece, Move, Direction, PieceType |
| "Castling, EP, promotion" | CastlingRights, EnPassantTarget, PromotionChoice |
| "Move validation" | MoveValidator, LegalMoveGenerator |
| "Check detection" | CheckDetector, King, AttackMap |
| "Checkmate, stalemate" | GameStatus, TerminalEvaluator |
| "Player turns" | Player, Color, SideToMove |
| "Undo/redo" | MoveHistory, MoveRecord, Command |
| "Time controls" | ChessClock, TimeControl |
| "Elo rating" | Rating, PlayerProfile, EloCalculator |
| "Draw rules" | DrawClaim, RepetitionTable, HalfmoveClock |
| "AI" | ChessAI, SearchEngine, Evaluator |
| "Save/load" | GameRepository, GameSnapshot |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Direction | ❌ NO | Vector (file, rank delta) or embedded in Move |
| PieceType | ✅ YES | Enum |
| Color | ✅ YES | Enum |
| Square | ✅ YES | Value object (0–63 or file/rank) |
| Piece | ✅ YES | Type, color, square (or square holds piece) |
| Board | ✅ YES | Core aggregate |
| Move | ✅ YES | From, to, promotion, flags |
| MoveRecord | ✅ YES | Move + reversible state delta |
| CastlingRights | ✅ YES | Bitmask or four booleans |
| Game | ✅ YES | Root aggregate / session |
| MoveValidator | ✅ YES | Service |
| LegalMoveGenerator | ✅ YES | Service (may merge with validator) |
| CheckDetector | ✅ YES | Service |
| GameStatus | ✅ YES | Enum |
| Player | ✅ YES | Human vs AI abstraction |
| ChessClock | ✅ YES | Per-player timers |
| TimeControl | ✅ YES | Policy (initial, increment) |
| EloRating | ❌ NO | Attribute of PlayerProfile |
| PlayerProfile | ✅ YES | User id, rating stats |
| EloCalculator | ✅ YES | Service |
| RepetitionTracker | ✅ YES | Zobrist hash history multiset |
| ChessAI | ✅ YES | Strategy / component |
| GameRepository | ✅ YES | Persistence port |

### Final Entity List

**Core Domain:**
1. **Game** — Session: board, side to move, rights, clocks, status, history pointer
2. **Board** — 8×8 placement; optional piece lists for speed
3. **Square** — Coordinate + optional reference to **Piece**
4. **Piece** — color, type; behavior delegated to **PieceMovementStrategy** (or static rules)

**Moves & Rules:**
5. **Move** — from, to, promotion piece, flags (capture, EP, castle, double push)
6. **MoveRecord** — applied **Move** + **UndoState** (captured piece, previous rights, previous EP, halfmove)
7. **CastlingRights** — KQkq flags
8. **MoveApplicationService** — apply/unapply move atomically
9. **PseudoLegalMoveGenerator** — geometry + occupancy, ignoring check
10. **LegalMoveFilter** — removes moves leaving king in check; adds castling/EP legality
11. **CheckDetector** — is square attacked? is king in check?
12. **TerminalStateEvaluator** — checkmate, stalemate, draws

**Players & Time:**
13. **Player** — id, profile, human/AI
14. **Color** — WHITE, BLACK
15. **ChessClock** — remaining time, running side, last tick timestamp
16. **TimeControl** — initial ms, increment ms, type (BLITZ, RAPID, CLASSICAL)

**Rating & Persistence:**
17. **PlayerProfile** — Elo, games played, K-factor policy
18. **RatingService** — update after game
19. **GameSnapshot** — FEN + history + clock state + metadata
20. **GameRepository** — save/load

**AI:**
21. **ChessAI** — chooses move given position and budget
22. **SearchEngine** — minimax + alpha-beta
23. **PositionEvaluator** — static score

**Draw & Repetition:**
24. **RepetitionTracker** — position hash counts (for threefold)
25. **MaterialInspector** — insufficient material

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Core Relationships

#### Game ↔ Board
**Conclusion:** **Composition** (game owns board)
```
Game ◆────→ Board [1]
```

#### Board ↔ Square / Piece
**Conclusion:** **Composition** (board owns grid); **Piece on Square**
```
Board ◆────→ Square [64]
Square ─────→ Piece [0..1]
```

---

### Pass 2: Rules & Services

#### Game ↔ MoveApplicationService
**Conclusion:** **Association** (service mutates game)
```
MoveApplicationService ─────→ Game [1]
```

#### LegalMoveFilter ↔ CheckDetector
**Conclusion:** **Dependency** (filter asks detector)
```
LegalMoveFilter ─────→ CheckDetector
```

#### ChessAI ↔ LegalMoveFilter / SearchEngine
**Conclusion:** **Uses** for move generation and tree search
```
ChessAI ─────→ SearchEngine ─────→ LegalMoveFilter
```

---

### Pass 3: Persistence & Players

#### Game ↔ Player
**Conclusion:** **Association** (white/black)
```
Game ─────→ Player [2] (white, black)
```

#### GameRepository ↔ GameSnapshot
**Conclusion:** **Dependency**
```
GameRepository ─────→ GameSnapshot
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Game → Board | 1:1 | Composition |
| Board → Square | 1:64 | Composition |
| Square → Piece | 1:0..1 | Association |
| Game → MoveRecord (history) | 1:N | Composition / list |
| Game → Player | 2:1 | Association |
| ChessAI → Game (view) | N:1 | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐
│ <<enumeration>> │  │ <<enumeration>> │  │ <<enumeration>>  │
│     Color       │  │   PieceType     │  │    GameStatus    │
├─────────────────┤  ├─────────────────┤  ├──────────────────┤
│ WHITE           │  │ KING            │  │ IN_PROGRESS      │
│ BLACK           │  │ QUEEN           │  │ WHITE_MATED      │
└─────────────────┘  │ ROOK            │  │ BLACK_MATED      │
                     │ BISHOP          │  │ STALEMATE        │
                     │ KNIGHT          │  │ DRAW_50_MOVE     │
                     │ PAWN            │  │ DRAW_REPETITION  │
                     └─────────────────┘  │ DRAW_INSUFFICIENT│
                                          │ DRAW_AGREEMENT   │
                                          │ WHITE_TIMEOUT    │
                                          │ BLACK_TIMEOUT    │
                                          └──────────────────┘

┌──────────────────┐  ┌──────────────────┐
│ <<enumeration>>  │  │ <<enumeration>>  │
│  TimeControlType │  │   SpecialMove    │
├──────────────────┤  ├──────────────────┤
│ BLITZ            │  │ NONE             │
│ RAPID            │  │ CASTLE_KINGSIDE  │
│ CLASSICAL        │  │ CASTLE_QUEENSIDE │
└──────────────────┘  │ EN_PASSANT       │
                      │ PROMOTION        │
                      └──────────────────┘
```

---

### Class Diagram 2: Board, Square, Piece

```
┌────────────────────────────────────────────────────────────┐
│                         Board                              │
├────────────────────────────────────────────────────────────┤
│ - squares: Piece?[64]   // or Square[64]                   │
│ - whiteKing: int        // square index                    │
│ - blackKing: int                                           │
├────────────────────────────────────────────────────────────┤
│ + Board() // standard setup                                │
│ + Board(fen: String)                                       │
│ + getPiece(at: int): Piece?                                │
│ + setPiece(at: int, p: Piece?): void                       │
│ + findKing(color: Color): int                              │
│ + copy(): Board                                            │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                         Piece                              │
├────────────────────────────────────────────────────────────┤
│ - color: Color                                             │
│ - type: PieceType                                          │
├────────────────────────────────────────────────────────────┤
│ + Piece(color, type)                                       │
│ + getColor(): Color                                        │
│ + getType(): PieceType                                     │
└────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Move, History, Undo

```
┌────────────────────────────────────────────────────────────┐
│                          Move                              │
├────────────────────────────────────────────────────────────┤
│ - from: int                                                │
│ - to: int                                                  │
│ - promotion: PieceType?  // null except promotion          │
│ - special: SpecialMove                                     │
├────────────────────────────────────────────────────────────┤
│ + Move(from, to, ...)                                      │
│ + equals/hash for repetition                               │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                       UndoState                            │
├────────────────────────────────────────────────────────────┤
│ - captured: Piece?  // at destination before move            │
│ - prevRights: CastlingRights                               │
│ - prevEpSquare: int  // -1 if none                         │
│ - prevHalfmove: int                                        │
│ - movedPiece: Piece  // for promotion undo                 │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                      MoveRecord                            │
├────────────────────────────────────────────────────────────┤
│ - move: Move                                               │
│ - undo: UndoState                                          │
├────────────────────────────────────────────────────────────┤
│ + MoveRecord(move, undo)                                   │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                    MoveHistory                             │
├────────────────────────────────────────────────────────────┤
│ - records: List<MoveRecord>                                │
│ - cursor: int   // for redo                                │
├────────────────────────────────────────────────────────────┤
│ + append(record): void                                     │
│ + undo(game): boolean                                      │
│ + redo(game): boolean                                      │
│ + clearFuture(): void  // on new move after undo           │
└────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Game & Clock

```
┌────────────────────────────────────────────────────────────┐
│                          Game                              │
├────────────────────────────────────────────────────────────┤
│ - board: Board                                             │
│ - sideToMove: Color                                        │
│ - castling: CastlingRights                                 │
│ - enPassantSquare: int                                     │
│ - halfmoveClock: int                                       │
│ - fullmoveNumber: int                                      │
│ - status: GameStatus                                       │
│ - history: MoveHistory                                     │
│ - clock: ChessClock                                        │
│ - whitePlayer: Player                                      │
│ - blackPlayer: Player                                      │
│ - repetition: RepetitionTracker                            │
│ - rated: boolean                                           │
├────────────────────────────────────────────────────────────┤
│ + tryMove(move: Move): MoveResult                            │
│ + getLegalMoves(): List<Move>                              │
│ + getFen(): String                                         │
│ + save(): GameSnapshot                                     │
│ + load(snapshot): void                                     │
└────────────────────────────────────────────────────────────┘
         │ uses
         ▼
┌────────────────────────────────────────────────────────────┐
│                      ChessClock                            │
├────────────────────────────────────────────────────────────┤
│ - whiteRemainingMs: long                                   │
│ - blackRemainingMs: long                                   │
│ - active: Color                                            │
│ - control: TimeControl                                     │
├────────────────────────────────────────────────────────────┤
│ + onMoveCompleted(prev: Color): void                       │
│ + tick(now: long): void  // updates active                 │
│ + isFlagged(): Color?                                      │
└────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────┐
│                     TimeControl                            │
├────────────────────────────────────────────────────────────┤
│ - type: TimeControlType                                    │
│ - initialMs: long                                          │
│ - incrementMs: long                                        │
└────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Rules Engine & AI

```
┌────────────────────────────────────────────────────────────┐
│               PseudoLegalMoveGenerator                     │
├────────────────────────────────────────────────────────────┤
│ + generate(board, side): List<Move>                        │
│ - pawnMoves(...), knightMoves(...), sliding(...)           │
└────────────────────────────────────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────┐
│                  LegalMoveFilter                           │
├────────────────────────────────────────────────────────────┤
│ - checkDetector: CheckDetector                             │
├────────────────────────────────────────────────────────────┤
│ + filterPseudoLegal(game, moves): List<Move>               │
│ + addCastlingIfLegal(game): List<Move>                     │
│ + addEnPassantIfLegal(game): List<Move>                    │
└────────────────────────────────────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────┐
│                    CheckDetector                           │
├────────────────────────────────────────────────────────────┤
│ + isInCheck(board, color): boolean                         │
│ + isSquareAttacked(board, sq, byColor): boolean            │
│ + findPinned(board, color): Map<int, PinInfo>  // optional │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                      ChessAI                               │
├────────────────────────────────────────────────────────────┤
│ - search: SearchEngine                                     │
│ - maxDepth: int                                            │
│ - timeBudgetMs: long                                       │
├────────────────────────────────────────────────────────────┤
│ + chooseMove(game): Move                                   │
└────────────────────────────────────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────┐
│                    SearchEngine                            │
├────────────────────────────────────────────────────────────┤
│ + minimaxRoot(game, depth): Move                           │
│ - alphabeta(node, α, β, depth): int                        │
└────────────────────────────────────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────┐
│                 PositionEvaluator                          │
├────────────────────────────────────────────────────────────┤
│ + evaluate(board, side): int  // centipawns, side perspective│
└────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 6: Rating & Persistence

```
┌────────────────────────────────────────────────────────────┐
│                    PlayerProfile                           │
├────────────────────────────────────────────────────────────┤
│ - userId: String                                           │
│ - elo: int                                                 │
│ - gamesPlayed: int                                         │
├────────────────────────────────────────────────────────────┤
│ + applyEloUpdate(delta: int): void                       │
└────────────────────────────────────────────────────────────┘
                    △
                    │
┌────────────────────────────────────────────────────────────┐
│               HumanPlayer          AIPlayer                │
├────────────────────────────────────────────────────────────┤
│ - profile: PlayerProfile       - difficulty: int          │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                   RatingService                            │
├────────────────────────────────────────────────────────────┤
│ + updateRatings(white: Profile, black: Profile, outcome)   │
│ - expectedScore(ra, rb): double                            │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                   GameSnapshot                             │
├────────────────────────────────────────────────────────────┤
│ - fen: String                                              │
│ - movesPgn: String                                         │
│ - clockWhiteMs: long                                       │
│ - clockBlackMs: long                                       │
│ - metadata: Map<String,String>                           │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                 <<interface>>                             │
│                 GameRepository                             │
├────────────────────────────────────────────────────────────┤
│ + save(snapshot): String  // id                            │
│ + load(id): GameSnapshot                                   │
└────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums

```java
// Color.java
public enum Color {
    WHITE, BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
```

```java
// PieceType.java
public enum PieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

    public int material() {
        return switch (this) {
            case PAWN -> 100;
            case KNIGHT, BISHOP -> 320;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 20000;
        };
    }
}
```

```java
// GameStatus.java
public enum GameStatus {
    IN_PROGRESS,
    WHITE_MATED, BLACK_MATED,
    STALEMATE,
    DRAW_50_MOVE, DRAW_REPETITION, DRAW_INSUFFICIENT,
    DRAW_AGREEMENT,
    WHITE_TIMEOUT, BLACK_TIMEOUT,
    WHITE_RESIGNED, BLACK_RESIGNED
}
```

---

### Square & Move helpers

```java
// BoardCoords.java — 0..63, a1=0, h8=63 (example mapping; pick one convention and stick to it)
public final class BoardCoords {
    private BoardCoords() {}

    public static int sq(int file, int rank) {
        return rank * 8 + file;
    }
    public static int fileOf(int sq) { return sq % 8; }
    public static int rankOf(int sq) { return sq / 8; }

    public static boolean onBoard(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }
}
```

```java
// Move.java
public final class Move {
    private final int from;
    private final int to;
    private final PieceType promotion; // nullable
    private final boolean enPassant;
    private final boolean castling;

    public Move(int from, int to, PieceType promotion, boolean enPassant, boolean castling) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.enPassant = enPassant;
        this.castling = castling;
    }

    public int from() { return from; }
    public int to() { return to; }
    public PieceType promotion() { return promotion; }
    // equals/hashCode for repetition keys if needed
}
```

---

### Castling rights

```java
// CastlingRights.java — KQkq
public final class CastlingRights {
    private boolean whiteKingSide, whiteQueenSide;
    private boolean blackKingSide, blackQueenSide;

    public CastlingRights copy() {
        CastlingRights c = new CastlingRights();
        c.whiteKingSide = whiteKingSide;
        c.whiteQueenSide = whiteQueenSide;
        c.blackKingSide = blackKingSide;
        c.blackQueenSide = blackQueenSide;
        return c;
    }

    // Update when king/rook moves or is captured — rules applied in MoveApplicator
    // getters/setters omitted for brevity
}
```

---

### Check detection (central to validation)

```java
// CheckDetector.java
public class CheckDetector {

    public boolean isInCheck(Board board, Color kingColor) {
        int kingSq = board.findKing(kingColor);
        return isSquareAttacked(board, kingSq, kingColor.opposite());
    }

    public boolean isSquareAttacked(Board board, int targetSq, Color byColor) {
        // Pawns
        int dir = byColor == Color.WHITE ? -8 : 8;
        int f = BoardCoords.fileOf(targetSq);
        int r = BoardCoords.rankOf(targetSq);
        for (int df : new int[]{-1, 1}) {
            int nf = f + df;
            int nr = r + (byColor == Color.WHITE ? -1 : 1);
            if (!BoardCoords.onBoard(nf, nr)) continue;
            int from = BoardCoords.sq(nf, nr);
            Piece p = board.getPiece(from);
            if (p != null && p.getColor() == byColor && p.getType() == PieceType.PAWN) {
                return true;
            }
        }
        // Knights: 8 offsets
        int[] kf = {1,2,2,1,-1,-2,-2,-1};
        int[] kr = {2,1,-1,-2,-2,-1,1,2};
        for (int i = 0; i < 8; i++) {
            int nf = f + kf[i], nr = r + kr[i];
            if (!BoardCoords.onBoard(nf, nr)) continue;
            Piece p = board.getPiece(BoardCoords.sq(nf, nr));
            if (p != null && p.getColor() == byColor && p.getType() == PieceType.KNIGHT) {
                return true;
            }
        }
        // King (adjacent)
        for (int df = -1; df <= 1; df++) {
            for (int dr = -1; dr <= 1; dr++) {
                if (df == 0 && dr == 0) continue;
                int nf = f + df, nr = r + dr;
                if (!BoardCoords.onBoard(nf, nr)) continue;
                Piece p = board.getPiece(BoardCoords.sq(nf, nr));
                if (p != null && p.getColor() == byColor && p.getType() == PieceType.KING) {
                    return true;
                }
            }
        }
        // Sliders: rook & bishop directions
        if (rayAttacked(board, f, r, byColor, 1, 0, PieceType.ROOK, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, -1, 0, PieceType.ROOK, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, 0, 1, PieceType.ROOK, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, 0, -1, PieceType.ROOK, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, 1, 1, PieceType.BISHOP, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, 1, -1, PieceType.BISHOP, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, -1, 1, PieceType.BISHOP, PieceType.QUEEN)) return true;
        if (rayAttacked(board, f, r, byColor, -1, -1, PieceType.BISHOP, PieceType.QUEEN)) return true;

        return false;
    }

    private boolean rayAttacked(Board board, int f, int r, Color byColor,
                                int df, int dr, PieceType a, PieceType b) {
        int nf = f + df, nr = r + dr;
        while (BoardCoords.onBoard(nf, nr)) {
            Piece p = board.getPiece(BoardCoords.sq(nf, nr));
            if (p != null) {
                return p.getColor() == byColor &&
                       (p.getType() == a || p.getType() == b);
            }
            nf += df; nr += dr;
        }
        return false;
    }
}
```

---

### Legal move filtering (pseudo-legal → legal)

```java
// LegalMoveFilter.java
public class LegalMoveFilter {
    private final CheckDetector checkDetector = new CheckDetector();
    private final MoveApplicator applicator = new MoveApplicator();

    public List<Move> legalMoves(Game game) {
        List<Move> pseudo = new PseudoLegalMoveGenerator().generate(game.getBoard(), game.getSideToMove());
        List<Move> legal = new ArrayList<>();
        for (Move m : pseudo) {
            UndoState u = applicator.apply(game, m);
            boolean stillInCheck = checkDetector.isInCheck(game.getBoard(), game.getSideToMove().opposite());
            applicator.unapply(game, m, u);
            if (!stillInCheck) {
                legal.add(m);
            }
        }
        // Castling & EP are included in pseudo generator or appended here with same filter
        return legal;
    }
}
```

**Interview note:** For performance, use “king in check” fast path: if only king moves or single escape, narrow generation; for AI, use unmake instead of full copy.

---

### Special moves — castling validation sketch

```java
// CastlingValidator.java (called before adding castle moves)
public class CastlingValidator {
    private final CheckDetector checkDetector = new CheckDetector();

    public boolean canCastleKingSide(Game g, Color c) {
        if (inCheck(g, c)) return false;
        if (!g.getCastling().allowsKingSide(c)) return false;
        Board b = g.getBoard();
        int rank = c == Color.WHITE ? 0 : 7;
        // e1, f1, g1 squares clear for white — indices depend on coord system
        if (!pathClear(b, rank, 5, 6, 7)) return false;
        if (checkDetector.isSquareAttacked(b, sq(rank, 5), c.opposite())) return false; // f1
        if (checkDetector.isSquareAttacked(b, sq(rank, 6), c.opposite())) return false; // g1
        return true;
    }

    private boolean inCheck(Game g, Color c) {
        return checkDetector.isInCheck(g.getBoard(), c);
    }

    private int sq(int rank, int file) { return BoardCoords.sq(file, rank); }

    private boolean pathClear(Board b, int rank, int kingFile, int midFile, int rookFile) {
        // king on kingFile, rook on rookFile; no pieces between
        // implement iteration
        return true;
    }
}
```

---

### Terminal state & draws

```java
// TerminalStateEvaluator.java
public class TerminalStateEvaluator {
    private final LegalMoveFilter legal = new LegalMoveFilter();

    public GameStatus evaluate(Game game) {
        List<Move> moves = legal.legalMoves(game);
        boolean inCheck = new CheckDetector().isInCheck(game.getBoard(), game.getSideToMove());

        if (moves.isEmpty()) {
            return inCheck
                ? (game.getSideToMove() == Color.WHITE ? GameStatus.BLACK_MATED : GameStatus.WHITE_MATED)
                : GameStatus.STALEMATE;
        }
        if (game.getHalfmoveClock() >= 100) { // 50 full moves = 100 half-moves
            return GameStatus.DRAW_50_MOVE;
        }
        if (game.getRepetition().countCurrentPosition() >= 3) {
            return GameStatus.DRAW_REPETITION;
        }
        if (new MaterialInspector().isInsufficient(game.getBoard())) {
            return GameStatus.DRAW_INSUFFICIENT;
        }
        return GameStatus.IN_PROGRESS;
    }
}
```

```java
// MaterialInspector.java — simplified K vs K, K+B vs K, K+N vs K, K+B vs K+B same color only, etc.
public class MaterialInspector {
    public boolean isInsufficient(Board board) {
        List<Piece> pieces = board.allPieces();
        // Count material; apply tablebase-style insufficient rules
        // ...
        return false;
    }
}
```

---

### Repetition tracking (Zobrist-style)

```java
// RepetitionTracker.java
public class RepetitionTracker {
    private final Map<Long, Integer> counts = new HashMap<>();
    private final Deque<Long> stack = new ArrayDeque<>();

    public void pushPosition(long zobristHash) {
        stack.push(zobristHash);
        counts.merge(zobristHash, 1, Integer::sum);
    }

    public void popPosition() {
        long h = stack.pop();
        int c = counts.get(h) - 1;
        if (c == 0) counts.remove(h);
        else counts.put(h, c);
    }

    public int countCurrentPosition() {
        return stack.isEmpty() ? 0 : counts.getOrDefault(stack.peek(), 0);
    }
}
```

**Note:** Hash must include side to move, castling rights, EP square, and board — same as FEN-based repetition key.

---

### Time control

```java
// TimeControl.java
public record TimeControl(TimeControlType type, long initialMs, long incrementMs) {
    public static TimeControl blitz3Plus2() {
        return new TimeControl(TimeControlType.BLITZ, 3 * 60_000L, 2_000L);
    }
    public static TimeControl rapid10() {
        return new TimeControl(TimeControlType.RAPID, 10 * 60_000L, 0);
    }
    public static TimeControl classical90d30() {
        return new TimeControl(TimeControlType.CLASSICAL, 90 * 60_000L, 30_000L);
    }
}

// ChessClock.java
public class ChessClock {
    private long whiteMs, blackMs;
    private Color active;
    private final TimeControl control;
    private long lastTick;

    public void onMoveCompleted(Color mover) {
        if (control.incrementMs() > 0) {
            if (mover == Color.WHITE) whiteMs += control.incrementMs();
            else blackMs += control.incrementMs();
        }
        active = mover.opposite();
        lastTick = System.currentTimeMillis();
    }

    public void tick(long now) {
        long delta = now - lastTick;
        lastTick = now;
        if (active == Color.WHITE) whiteMs -= delta;
        else blackMs -= delta;
    }

    public Color flaggedSide() {
        if (whiteMs <= 0) return Color.WHITE;
        if (blackMs <= 0) return Color.BLACK;
        return null;
    }
}
```

---

### Elo update

```java
// RatingService.java
public class RatingService {
    public void updateRatings(PlayerProfile white, PlayerProfile black, double whiteScore) {
        double rw = white.getElo(), rb = black.getElo();
        double ew = expected(rw, rb);
        double eb = expected(rb, rw);
        int kw = kFactor(white), kb = kFactor(black);
        white.setElo((int) Math.round(rw + kw * (whiteScore - ew)));
        black.setElo((int) Math.round(rb + kb * ((1 - whiteScore) - eb)));
    }

    private double expected(double ra, double rb) {
        return 1.0 / (1.0 + Math.pow(10, (rb - ra) / 400.0));
    }

    private int kFactor(PlayerProfile p) {
        return p.getGamesPlayed() < 30 ? 40 : 20;
    }
}
```

---

### AI: minimax + alpha-beta (negamax form)

```java
// SearchEngine.java — negamax: static evaluation is always "good for side to move"
public class SearchEngine {
    private final PositionEvaluator eval = new PositionEvaluator();
    private final LegalMoveFilter legal = new LegalMoveFilter();

    public Move minimaxRoot(Game game, int maxDepth) {
        List<Move> moves = legal.legalMoves(game);
        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        MoveApplicator app = new MoveApplicator();

        for (Move m : orderMoves(moves)) {
            UndoState u = app.apply(game, m);
            int score = -negamax(game, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            app.unapply(game, m, u);
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best;
    }

    /** Score from the perspective of the side currently to move (higher = better for them). */
    private int negamax(Game game, int depth, int alpha, int beta) {
        if (depth == 0) {
            return eval.evaluateForSideToMove(game);
        }
        List<Move> moves = legal.legalMoves(game);
        if (moves.isEmpty()) {
            return eval.terminalScoreForSideToMove(game);
        }
        MoveApplicator app = new MoveApplicator();
        int best = Integer.MIN_VALUE;
        for (Move m : orderMoves(moves)) {
            UndoState u = app.apply(game, m);
            int score = -negamax(game, depth - 1, -beta, -alpha);
            app.unapply(game, m, u);
            best = Math.max(best, score);
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break; // beta cut-off
        }
        return best;
    }

    private List<Move> orderMoves(List<Move> moves) {
        // MVV-LVA, killer moves, history — improves alpha-beta pruning
        return moves;
    }
}
```

---

### Persistence snapshot

```java
// GameSnapshot.java + save/load sketch
public record GameSnapshot(
    String fen,
    String movesUci,
    long whiteMs,
    long blackMs,
    boolean rated,
    String whiteId,
    String blackId
) {}

public class GameSerializer {
    public GameSnapshot toSnapshot(Game g) {
        return new GameSnapshot(
            g.getFen(),
            g.getHistory().asUci(),
            g.getClock().whiteRemaining(),
            g.getClock().blackRemaining(),
            g.isRated(),
            g.getWhitePlayer().getId(),
            g.getBlackPlayer().getId()
        );
    }

    public Game fromSnapshot(GameSnapshot s, GameRepository repo) {
        // parse FEN, rebuild history optional for replay
        return new Game();
    }
}
```

---

### Demo

```java
// ChessGameDemo.java
public class ChessGameDemo {
    public static void main(String[] args) {
        Game game = Game.standard();
        LegalMoveFilter legals = new LegalMoveFilter();

        Move e2e4 = MoveParser.parse("e2e4");
        game.tryMove(e2e4);

        List<Move> moves = legals.legalMoves(game);
        System.out.println("Legal moves for side to move: " + moves.size());

        ChessAI ai = new ChessAI(new SearchEngine(), 4, 2000);
        Move reply = ai.chooseMove(game);
        System.out.println("AI plays: " + MoveFormatter.toUci(reply));

        GameSnapshot snap = new GameSerializer().toSnapshot(game);
        // repository.save(snap);
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Command Pattern
**Where:** `MoveRecord`, `MoveApplicator.apply` / `unapply`  
**Why:** Uniform undo/redo and AI search unmake  
**Interview Justification:** "Every move encapsulates reversible state. The engine can push/pop commands for history and for alpha-beta without copying entire games."

---

### Pattern 2: Strategy Pattern
**Where:** `Player` implementations (`HumanPlayer`, `ChessAI`), optional `TimeControl` strategies  
**Why:** Swap human vs AI without changing `Game`  
**Interview Justification:** "The game loop asks the active player for a move; AI uses search, human uses UI."

---

### Pattern 3: State Pattern (lightweight)
**Where:** `GameStatus` drives allowed transitions (no moves after mate)  
**Why:** Prevents illegal operations after termination  
**Interview Justification:** "Once status is terminal, `tryMove` rejects new moves except load/reset."

---

### Pattern 4: Repository Pattern
**Where:** `GameRepository` + `GameSnapshot`  
**Why:** Decouple engine from storage (file, DB, cloud)  
**Interview Justification:** "FEN + move list is the portable contract; persistence is injected."

---

## 💡 Step 8: Interview Discussion Points

### 1. Move validation: pseudo-legal vs direct legal generation

**Interviewer:** "How do you generate only legal moves without trying every pseudo-legal move?"

**Answer:**
"**Two mainstream approaches:**

**A) Generate pseudo-legal, filter with make-unmake:**
- Generate moves as if check didn’t matter (including pinned piece logic optionally).
- For each candidate, apply move, test if own king is attacked, unapply.
- Simple, correct, easy to test. Cost: O(k) check tests per position.

**B) Pin-aware generation + king in check specialization:**
- If king in check, only king moves, captures of checking piece, or block moves along ray (if slider).
- If not in check, still handle pins by restricting pinned piece movement to pin line.
- Fewer false candidates; more code.

**For interviews, start with (A)** then mention (B) for optimization. Use **attack detector** shared by castling (squares not attacked), EP discovery check, and filter."

---

### 2. Check / checkmate / stalemate

**Interviewer:** "How do you detect checkmate cheaply?"

**Answer:**
"**Checkmate = in check + zero legal moves.**  
**Stalemate = not in check + zero legal moves.**

So one **legal move list** answers both. The expensive part is legal generation; terminal detection is O(1) after that.

**Optimization:** If in double check, only king moves can be legal — generate those first."

---

### 3. Castling and en passant edge cases

**Interviewer:** "What breaks castling?"

**Answer:**
"- King or relevant rook has moved (track rights).  
- King currently in check.  
- King passes through or lands on **attacked** square (FIDE).  
- Pieces between king and rook.  
- **Correct rook must exist** on original square (underpromotion edge cases rare in practice).

**En passant:**  
- Only the move immediately after double-step.  
- Resulting position must not leave own king in check (EP discovery).  
- Captured pawn square is behind destination, not destination square."

---

### 4. Threefold repetition and 50-move rule

**Interviewer:** "How do you track threefold?"

**Answer:**
"**Repetition** uses a hash of **rule-relevant state**: piece placement, side to move, castling rights, EP target.  
Maintain a **multiset or map** from hash → count for positions along the game; after each half-move, read count for current hash.

**50-move rule:** **Halfmove clock** increments on non-pawn, non-capture moves; reset on pawn move or capture. At **≥100 half-moves**, claim draw (automatic in engines)."

---

### 5. AI: alpha-beta and move ordering

**Interviewer:** "Why alpha-beta helps?"

**Answer:**
"**Without pruning:** minimax explores full tree.  
**With alpha-beta:** if a move refutes a line (β cut), skip siblings — **best case** complexity drops toward O(b^(d/2)) with perfect ordering.

**Move ordering:** captures (MVV-LVA), killer moves, history heuristic — **good ordering maximizes cuts**."

---

### 6. Elo and rated games

**Interviewer:** "When do you update Elo?"

**Answer:**
"**After terminal status** (mate, resignation, timeout, draw). Map outcome to **whiteScore** ∈ {1, 0.5, 0}. Use **expected score** formula and **K-factor** (higher for new players). Optionally use **Glicko-2** for RD — mention as extension."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `CheckDetector`: attack detection only
- `PseudoLegalMoveGenerator`: geometry only
- `MoveApplicator`: mutation + undo deltas only
- `RatingService`: Elo math only

### Open/Closed ✅
```java
public interface Player {
    Move chooseMove(Game game);
}
// New AI or remote human without changing Game core
```

### Liskov Substitution ✅
- Any `Player` implementation works in the same game loop

### Interface Segregation ✅
```java
interface MoveReadFacade { List<Move> legalMoves(); }
interface GameCommand { void tryMove(Move m); }
// UI may depend on read-only view
```

### Dependency Inversion ✅
```java
public class GameService {
    private final GameRepository repository;
    public GameService(GameRepository repository) { this.repository = repository; }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **Separate pseudo-legal generation from legal filtering** using check test + apply/unapply
- ✅ **Castling / EP** as first-class moves with dedicated validation
- ✅ **Command-style history** for undo/redo and AI
- ✅ **FEN + Zobrist** for persistence and repetition
- ✅ **Terminal rules** unified through legal move count + flags

### Rules & Correctness
- ✅ **King safety** is the backbone of validation and castling
- ✅ **Draw rules** (50-move, threefold, insufficient material) integrated after each move
- ✅ **Clocks** decoupled but updated atomically with move commit

### AI & Scale
- ✅ **Alpha-beta** with move ordering for practical depth
- ✅ **Iterative deepening** + time budget (mention as production enhancement)

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
