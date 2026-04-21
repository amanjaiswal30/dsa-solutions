# Low-Level Design: Tic Tac Toe Game

**Difficulty:** Medium ⚡

**Interview Duration:** 45-60 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a Tic Tac Toe game system that supports multiple game modes, AI players, and online multiplayer functionality."*

### Clarifying Questions to Ask:

1. **Q:** What game modes should we support?  
   **A:** Human vs Human (local), Human vs AI, AI vs AI, Online multiplayer.

2. **Q:** What board sizes should we support?  
   **A:** Start with 3×3, but design should support N×N boards.

3. **Q:** What AI difficulty levels?  
   **A:** Easy (random moves), Medium (basic strategy), Hard (Minimax algorithm).

4. **Q:** Should we track game history?  
   **A:** Yes, store completed games and allow replay.

5. **Q:** What about player profiles?  
   **A:** Yes, track wins/losses/draws, player stats, ratings.

6. **Q:** Should we support tournaments?  
   **A:** Yes, round-robin or knockout tournaments.

7. **Q:** What about move validation?  
   **A:** Validate legal moves, prevent overwriting occupied cells.

8. **Q:** Should we support undo/redo?  
   **A:** Yes, undo last move (for practice mode).

9. **Q:** What about time limits?  
   **A:** Optional timer per move (configurable: 10s, 30s, no limit).

10. **Q:** Should we support spectators?  
    **A:** Yes, for online games, allow others to watch.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

1. System should support player registration with username
2. Players should have profiles with stats (wins, losses, draws, rating)
3. System should support creating new games
4. Game should support different modes:
   - Human vs Human (local)
   - Human vs AI
   - AI vs AI (simulation)
   - Online multiplayer
5. Game should support different board sizes (3×3, 4×4, 5×5)
6. For 3×3: 3-in-a-row wins
7. For N×N: N-in-a-row wins
8. Players should alternate turns (X goes first)
9. System should validate moves (cell empty, within bounds, correct turn)
10. System should detect win conditions:
    - Horizontal line
    - Vertical line
    - Diagonal line
11. System should detect draw (board full, no winner)
12. System should support AI with difficulty levels:
    - Easy: Random valid moves
    - Medium: Block opponent wins + random
    - Hard: Minimax with alpha-beta pruning
13. System should support undo/redo moves
14. System should maintain move history
15. System should support game replay
16. System should track game state (WAITING, IN_PROGRESS, COMPLETED)
17. System should support optional move timer
18. For online games:
    - Match players via lobby
    - Real-time move synchronization
    - Handle disconnections (forfeit after 30s)
19. System should support tournaments (round-robin, knockout)
20. System should allow spectators for online games

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many concurrent games?"
- 10,000 concurrent online games
- 100,000 registered players
- 1M completed games in history

**Deduced NFRs:**
- ✅ In-memory game state for active games
- ✅ Database for player profiles and history
- ✅ Horizontal scaling for game servers
- ✅ Session affinity (player stays on same server)

---

#### 2. **Consistency Analysis**

**Think:** "What must be accurate?"
- Move sequence (no simultaneous moves)
- Win detection (accurate)
- Player stats (reliable)
- Game history (immutable)

**Deduced NFRs:**
- ✅ Atomic move operations
- ✅ Server-side move validation
- ✅ Optimistic locking for player stats
- ✅ Idempotent move requests

---

#### 3. **Performance Analysis**

**Think:** "Response time expectations?"
- Move processing < 100ms
- AI move (Minimax) < 2 seconds
- Win detection < 50ms
- Game creation < 500ms

**Deduced NFRs:**
- ✅ O(N) win detection (check row/col/diag)
- ✅ Alpha-beta pruning for AI (reduce search space)
- ✅ Memoization for AI game states
- ✅ Real-time updates via WebSocket

---

#### 4. **Availability Analysis**

**Think:** "Acceptable downtime?"
- Casual game - lower expectations
- But online multiplayer needs reliability

**Deduced NFRs:**
- ✅ 99% availability (acceptable for gaming)
- ✅ Graceful degradation (AI mode if matchmaking fails)
- ✅ Auto-save game state (resume after disconnect)

---

#### 5. **Usability Analysis**

**Think:** "User experience?"
- Intuitive UI
- Visual feedback
- Error messages
- Game controls

**Deduced NFRs:**
- ✅ Clear win/draw messages
- ✅ Move highlighting
- ✅ Undo button for practice
- ✅ Chat for online games (optional)

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Player registration" | Player |
| "Player profiles with stats" | Profile, Stats, Rating |
| "Create new games" | Game |
| "Game modes" | GameMode |
| "Board sizes" | Board, Cell |
| "Players alternate turns" | Turn |
| "Make moves" | Move |
| "Detect win conditions" | Win Condition |
| "AI difficulty levels" | AI, Difficulty |
| "Undo/redo moves" | Move History |
| "Game state" | Game State |
| "Move timer" | Timer |
| "Online games lobby" | Lobby |
| "Tournaments" | Tournament |
| "Spectators" | Spectator |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Player | ✅ YES | Has attributes, behavior, lifecycle |
| Profile | ❌ NO | Part of Player |
| Stats | ❌ NO | Attributes of Player |
| Rating | ❌ NO | Attribute of Player |
| Game | ✅ YES | Core entity with lifecycle |
| GameMode | ✅ YES (enum) | Type classification |
| Board | ✅ YES | Complex structure with behavior |
| Cell | ✅ YES | Individual board position |
| Turn | ❌ NO | Game attribute |
| Move | ✅ YES | Action record with history |
| WinCondition | ❌ NO | Algorithm/logic |
| AI | ✅ YES | Player subtype with strategy |
| Difficulty | ✅ YES (enum) | AI configuration |
| MoveHistory | ❌ NO | List of moves |
| GameState | ✅ YES (enum) | Lifecycle states |
| Timer | ✅ YES | Time management |
| Lobby | ✅ YES | Matchmaking container |
| Tournament | ✅ YES | Competition structure |
| Spectator | ✅ YES | Observer role |

### Final Entity List

**Core Entities:**
1. **Player** (abstract) - Game participant
2. **HumanPlayer** - Human participant
3. **AIPlayer** - Computer player with strategy
4. **Game** - Game instance
5. **Board** - Game board (N×N)
6. **Cell** - Individual position
7. **Move** - Player action
8. **GameMode** - Enum (LOCAL, ONLINE, AI)
9. **GameState** - Enum (WAITING, IN_PROGRESS, COMPLETED)
10. **GameResult** - Enum (X_WINS, O_WINS, DRAW)
11. **Symbol** - Enum (X, O, EMPTY)
12. **AIStrategy** - Interface for AI algorithms
13. **Difficulty** - Enum (EASY, MEDIUM, HARD)
14. **Timer** - Move time tracking
15. **Tournament** - Competition management
16. **Spectator** - Observer

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Inheritance Hierarchies

**Player Hierarchy:**
```
      Player (abstract)
        /    \
  HumanPlayer  AIPlayer
```

**Why?** HumanPlayer gets input from UI, AIPlayer computes moves algorithmically.

**AIStrategy Hierarchy:**
```
     AIStrategy (interface)
       /     |      \
   Random  Medium  MinimaxStrategy
```

---

### Pass 2: Ownership Relationships

#### Game ↔ Board
**Conclusion:** **Composition** (board can't exist without game)
```
Game ◆────→ Board [1]
```

#### Board ↔ Cell
**Conclusion:** **Composition** (cells part of board)
```
Board ◆────→ Cell [N×N]
```

#### Game ↔ Player
**Conclusion:** **Aggregation** (players exist independently)
```
Game ◇────→ Player [2]
```

#### Game ↔ Move
**Conclusion:** **Composition** (moves belong to game)
```
Game ◆────→ Move [0..N×N]
```

#### Tournament ↔ Game
**Conclusion:** **Composition** (tournament owns games)
```
Tournament ◆────→ Game [1..*]
```

---

### Pass 3: Cardinality

| Relationship | Cardinality | Reasoning |
|--------------|-------------|-----------|
| Game → Player | 1:2 | Two players per game |
| Game → Board | 1:1 | One board per game |
| Board → Cell | 1:N² | N×N cells |
| Game → Move | 1:0..N² | Up to N² moves |
| Player → Game | 1:N | Player plays many games |
| Tournament → Game | 1:N | Tournament has many games |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>> │  │ <<enumeration>> │
│     Symbol      │  │   GameState     │  │   GameResult    │
├─────────────────┤  ├─────────────────┤  ├─────────────────┤
│ X               │  │ WAITING         │  │ X_WINS          │
│ O               │  │ IN_PROGRESS     │  │ O_WINS          │
│ EMPTY           │  │ COMPLETED       │  │ DRAW            │
└─────────────────┘  │ PAUSED          │  │ ABANDONED       │
                     └─────────────────┘  └─────────────────┘

┌─────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>> │
│    GameMode     │  │   Difficulty    │
├─────────────────┤  ├─────────────────┤
│ LOCAL           │  │ EASY            │
│ ONLINE          │  │ MEDIUM          │
│ AI              │  │ HARD            │
└─────────────────┘  └─────────────────┘
```

---

### Class Diagram 2: Board & Cell

```
┌──────────────────────────────────────────────┐
│               Board                          │
├──────────────────────────────────────────────┤
│ - size: int                                  │
│ - cells: Cell[][]        ◆───────────────────┼──→ Cell [N×N]
├──────────────────────────────────────────────┤
│ + Board(size: int)                           │
│ + makeMove(row: int, col: int, symbol: Symbol): boolean │
│ + isCellEmpty(row: int, col: int): boolean   │
│ + isFull(): boolean                          │
│ + checkWinner(): Symbol                      │
│ + getAvailableMoves(): List<Position>        │
│ + reset(): void                              │
│ + clone(): Board                             │
└──────────────────────────────────────────────┘

┌────────────────────────────────┐
│            Cell                │
├────────────────────────────────┤
│ - row: int                     │
│ - col: int                     │
│ - symbol: Symbol               │
├────────────────────────────────┤
│ + Cell(row, col)               │
│ + isEmpty(): boolean           │
│ + mark(symbol: Symbol): void   │
│ + getSymbol(): Symbol          │
└────────────────────────────────┘
```

---

### Class Diagram 3: Player Hierarchy

```
┌──────────────────────────────────────────────┐
│          Player (abstract)                   │
├──────────────────────────────────────────────┤
│ # id: String                                 │
│ # name: String                               │
│ # symbol: Symbol                             │
│ # stats: PlayerStats                         │
├──────────────────────────────────────────────┤
│ + Player(name, symbol)                       │
│ + abstract makeMove(board: Board): Position  │
│ + updateStats(result: GameResult): void      │
│ + getStats(): PlayerStats                    │
└──────────────────────────────────────────────┘
                    △
                    │
         ┌──────────┴──────────┐
         │                     │
┌────────────────────┐  ┌─────────────────────────────┐
│   HumanPlayer      │  │      AIPlayer               │
├────────────────────┤  ├─────────────────────────────┤
│                    │  │ - strategy: AIStrategy      │
│                    │  │ - difficulty: Difficulty    │
├────────────────────┤  ├─────────────────────────────┤
│ + makeMove():      │  │ + makeMove(board): Position │
│   Position         │  │ + setStrategy(strat): void  │
│   (gets from UI)   │  └─────────────────────────────┘
└────────────────────┘

┌──────────────────────────────┐
│       PlayerStats            │
├──────────────────────────────┤
│ - gamesPlayed: int           │
│ - wins: int                  │
│ - losses: int                │
│ - draws: int                 │
│ - rating: double             │
├──────────────────────────────┤
│ + recordWin(): void          │
│ + recordLoss(): void         │
│ + recordDraw(): void         │
│ + getWinRate(): double       │
└──────────────────────────────┘
```

---

### Class Diagram 4: AIStrategy Pattern

```
┌──────────────────────────────────────┐
│   <<interface>>                      │
│      AIStrategy                      │
├──────────────────────────────────────┤
│ + getBestMove(board: Board,          │
│               symbol: Symbol):        │
│               Position                │
└──────────────────────────────────────┘
                △
                │
    ┌───────────┼───────────┐
    │           │           │
┌───────────┐ ┌──────────┐ ┌────────────────────┐
│  Random   │ │  Medium  │ │   Minimax          │
│ Strategy  │ │ Strategy │ │   Strategy         │
├───────────┤ ├──────────┤ ├────────────────────┤
│ Returns   │ │ 1. Block │ │ - maxDepth: int    │
│ random    │ │ opponent │ │ - memo: Map<State, │
│ valid     │ │ 2. Take  │ │        Score>      │
│ move      │ │ center   │ ├────────────────────┤
│           │ │ 3. Random│ │ + minimax(): int   │
│           │ │          │ │ + alphaBeta(): int │
└───────────┘ └──────────┘ └────────────────────┘
```

---

### Class Diagram 5: Game

```
┌──────────────────────────────────────────────────────────┐
│                    Game                                  │
├──────────────────────────────────────────────────────────┤
│ - gameId: String                                         │
│ - board: Board               ◆───────────────────────────┼──→ Board [1]
│ - player1: Player            ◇───────────────────────────┼──→ Player
│ - player2: Player            ◇───────────────────────────┼──→ Player
│ - currentPlayer: Player                                  │
│ - moves: List<Move>          ◆───────────────────────────┼──→ Move [0..*]
│ - state: GameState                                       │
│ - result: GameResult                                     │
│ - mode: GameMode                                         │
│ - timer: Timer                                           │
│ - spectators: Set<Spectator>                             │
│ - createdAt: LocalDateTime                               │
├──────────────────────────────────────────────────────────┤
│ + Game(player1, player2, mode, boardSize)                │
│ + start(): void                                          │
│ + makeMove(player: Player, position: Position): boolean  │
│ + undo(): boolean                                        │
│ + getWinner(): Player                                    │
│ + isDraw(): boolean                                      │
│ + isGameOver(): boolean                                  │
│ + switchTurn(): void                                     │
│ + addSpectator(spectator: Spectator): void               │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────┐
│           Move               │
├──────────────────────────────┤
│ - player: Player             │
│ - position: Position         │
│ - symbol: Symbol             │
│ - timestamp: LocalDateTime   │
├──────────────────────────────┤
│ + Move(player, position)     │
└──────────────────────────────┘

┌──────────────────────────────┐
│         Position             │
├──────────────────────────────┤
│ - row: int                   │
│ - col: int                   │
├──────────────────────────────┤
│ + Position(row, col)         │
│ + equals(obj: Object): bool  │
└──────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums

```java
// Symbol.java
public enum Symbol {
    X('X'),
    O('O'),
    EMPTY('-');
    
    private final char displayChar;
    
    Symbol(char displayChar) {
        this.displayChar = displayChar;
    }
    
    public char getDisplayChar() {
        return displayChar;
    }
    
    public Symbol getOpponent() {
        if (this == X) return O;
        if (this == O) return X;
        return EMPTY;
    }
}

// GameState.java
public enum GameState {
    WAITING, IN_PROGRESS, COMPLETED, PAUSED, ABANDONED
}

// GameResult.java
public enum GameResult {
    X_WINS, O_WINS, DRAW, ABANDONED
}

// GameMode.java
public enum GameMode {
    LOCAL, ONLINE, AI, AI_VS_AI
}

// Difficulty.java
public enum Difficulty {
    EASY("Random moves"),
    MEDIUM("Basic strategy"),
    HARD("Minimax algorithm");
    
    private final String description;
    
    Difficulty(String description) {
        this.description = description;
    }
}
```

---

### Board & Cell

```java
// Position.java
public class Position {
    private final int row;
    private final int col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}

// Cell.java
public class Cell {
    private final int row;
    private final int col;
    private Symbol symbol;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.symbol = Symbol.EMPTY;
    }
    
    public boolean isEmpty() {
        return symbol == Symbol.EMPTY;
    }
    
    public void mark(Symbol symbol) {
        if (!isEmpty()) {
            throw new IllegalStateException("Cell already occupied");
        }
        this.symbol = symbol;
    }
    
    public void clear() {
        this.symbol = Symbol.EMPTY;
    }
    
    public Symbol getSymbol() {
        return symbol;
    }
}

// Board.java
public class Board {
    private final int size;
    private final Cell[][] cells;
    
    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size][size];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }
    
    public boolean makeMove(int row, int col, Symbol symbol) {
        if (!isValidPosition(row, col)) {
            return false;
        }
        
        if (!cells[row][col].isEmpty()) {
            return false;
        }
        
        cells[row][col].mark(symbol);
        return true;
    }
    
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }
    
    public boolean isCellEmpty(int row, int col) {
        return cells[row][col].isEmpty();
    }
    
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public Symbol checkWinner() {
        // Check rows
        for (int i = 0; i < size; i++) {
            Symbol first = cells[i][0].getSymbol();
            if (first == Symbol.EMPTY) continue;
            
            boolean rowWin = true;
            for (int j = 1; j < size; j++) {
                if (cells[i][j].getSymbol() != first) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) return first;
        }
        
        // Check columns
        for (int j = 0; j < size; j++) {
            Symbol first = cells[0][j].getSymbol();
            if (first == Symbol.EMPTY) continue;
            
            boolean colWin = true;
            for (int i = 1; i < size; i++) {
                if (cells[i][j].getSymbol() != first) {
                    colWin = false;
                    break;
                }
            }
            if (colWin) return first;
        }
        
        // Check main diagonal (top-left to bottom-right)
        Symbol first = cells[0][0].getSymbol();
        if (first != Symbol.EMPTY) {
            boolean diagWin = true;
            for (int i = 1; i < size; i++) {
                if (cells[i][i].getSymbol() != first) {
                    diagWin = false;
                    break;
                }
            }
            if (diagWin) return first;
        }
        
        // Check anti-diagonal (top-right to bottom-left)
        first = cells[0][size - 1].getSymbol();
        if (first != Symbol.EMPTY) {
            boolean antiDiagWin = true;
            for (int i = 1; i < size; i++) {
                if (cells[i][size - 1 - i].getSymbol() != first) {
                    antiDiagWin = false;
                    break;
                }
            }
            if (antiDiagWin) return first;
        }
        
        return Symbol.EMPTY;
    }
    
    public List<Position> getAvailableMoves() {
        List<Position> moves = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isEmpty()) {
                    moves.add(new Position(i, j));
                }
            }
        }
        return moves;
    }
    
    public void reset() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j].clear();
            }
        }
    }
    
    public Board clone() {
        Board cloned = new Board(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!cells[i][j].isEmpty()) {
                    cloned.cells[i][j].mark(cells[i][j].getSymbol());
                }
            }
        }
        return cloned;
    }
    
    public void display() {
        System.out.println();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(" " + cells[i][j].getSymbol().getDisplayChar() + " ");
                if (j < size - 1) System.out.print("|");
            }
            System.out.println();
            if (i < size - 1) {
                for (int j = 0; j < size; j++) {
                    System.out.print("---");
                    if (j < size - 1) System.out.print("+");
                }
                System.out.println();
            }
        }
        System.out.println();
    }
    
    public int getSize() { return size; }
    public Cell getCell(int row, int col) { return cells[row][col]; }
}
```

---

### Player Hierarchy

```java
// Player.java (abstract)
public abstract class Player {
    protected String id;
    protected String name;
    protected Symbol symbol;
    protected PlayerStats stats;
    
    public Player(String name, Symbol symbol) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.symbol = symbol;
        this.stats = new PlayerStats();
    }
    
    public abstract Position makeMove(Board board);
    
    public void updateStats(GameResult result) {
        stats.gamesPlayed++;
        
        if (result == GameResult.DRAW) {
            stats.draws++;
        } else if ((result == GameResult.X_WINS && symbol == Symbol.X) ||
                   (result == GameResult.O_WINS && symbol == Symbol.O)) {
            stats.wins++;
            stats.rating += 10;
        } else {
            stats.losses++;
            stats.rating -= 5;
        }
    }
    
    public String getName() { return name; }
    public Symbol getSymbol() { return symbol; }
    public PlayerStats getStats() { return stats; }
}

// HumanPlayer.java
public class HumanPlayer extends Player {
    private Scanner scanner;
    
    public HumanPlayer(String name, Symbol symbol) {
        super(name, symbol);
        this.scanner = new Scanner(System.in);
    }
    
    @Override
    public Position makeMove(Board board) {
        System.out.println(name + " (" + symbol + "), enter your move (row col): ");
        
        while (true) {
            try {
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                
                Position pos = new Position(row, col);
                
                if (!board.isValidPosition(row, col)) {
                    System.out.println("Invalid position. Try again.");
                    continue;
                }
                
                if (!board.isCellEmpty(row, col)) {
                    System.out.println("Cell already occupied. Try again.");
                    continue;
                }
                
                return pos;
                
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Enter numbers only.");
                scanner.nextLine(); // Clear buffer
            }
        }
    }
}

// AIPlayer.java
public class AIPlayer extends Player {
    private AIStrategy strategy;
    private Difficulty difficulty;
    
    public AIPlayer(String name, Symbol symbol, Difficulty difficulty) {
        super(name, symbol);
        this.difficulty = difficulty;
        this.strategy = createStrategy(difficulty);
    }
    
    private AIStrategy createStrategy(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return new RandomStrategy();
            case MEDIUM: return new MediumStrategy();
            case HARD: return new MinimaxStrategy();
            default: return new RandomStrategy();
        }
    }
    
    @Override
    public Position makeMove(Board board) {
        System.out.println(name + " (AI-" + difficulty + ") is thinking...");
        
        try {
            Thread.sleep(500); // Simulate thinking
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Position move = strategy.getBestMove(board, symbol);
        System.out.println(name + " plays: (" + move.getRow() + ", " + move.getCol() + ")");
        
        return move;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.strategy = createStrategy(difficulty);
    }
}

// PlayerStats.java
public class PlayerStats {
    public int gamesPlayed;
    public int wins;
    public int losses;
    public int draws;
    public double rating;
    
    public PlayerStats() {
        this.rating = 1000.0; // Starting ELO rating
    }
    
    public double getWinRate() {
        return gamesPlayed == 0 ? 0 : (wins * 100.0) / gamesPlayed;
    }
    
    @Override
    public String toString() {
        return String.format("Stats: %d games, %d wins, %d losses, %d draws (%.1f%% win rate, Rating: %.0f)",
            gamesPlayed, wins, losses, draws, getWinRate(), rating);
    }
}
```

---

### AI Strategies

```java
// AIStrategy.java (interface)
public interface AIStrategy {
    Position getBestMove(Board board, Symbol aiSymbol);
}

// RandomStrategy.java (Easy)
public class RandomStrategy implements AIStrategy {
    private Random random = new Random();
    
    @Override
    public Position getBestMove(Board board, Symbol aiSymbol) {
        List<Position> available = board.getAvailableMoves();
        
        if (available.isEmpty()) {
            return null;
        }
        
        return available.get(random.nextInt(available.size()));
    }
}

// MediumStrategy.java
public class MediumStrategy implements AIStrategy {
    @Override
    public Position getBestMove(Board board, Symbol aiSymbol) {
        Symbol opponent = aiSymbol.getOpponent();
        
        // 1. Check if AI can win in next move
        Position winMove = findWinningMove(board, aiSymbol);
        if (winMove != null) return winMove;
        
        // 2. Block opponent's winning move
        Position blockMove = findWinningMove(board, opponent);
        if (blockMove != null) return blockMove;
        
        // 3. Take center if available (best strategic position)
        int center = board.getSize() / 2;
        if (board.isCellEmpty(center, center)) {
            return new Position(center, center);
        }
        
        // 4. Take corners
        Position corner = findCorner(board);
        if (corner != null) return corner;
        
        // 5. Random move
        List<Position> available = board.getAvailableMoves();
        return available.get(new Random().nextInt(available.size()));
    }
    
    private Position findWinningMove(Board board, Symbol symbol) {
        for (Position pos : board.getAvailableMoves()) {
            Board copy = board.clone();
            copy.makeMove(pos.getRow(), pos.getCol(), symbol);
            
            if (copy.checkWinner() == symbol) {
                return pos;
            }
        }
        return null;
    }
    
    private Position findCorner(Board board) {
        int n = board.getSize();
        int[][] corners = {{0, 0}, {0, n-1}, {n-1, 0}, {n-1, n-1}};
        
        for (int[] corner : corners) {
            if (board.isCellEmpty(corner[0], corner[1])) {
                return new Position(corner[0], corner[1]);
            }
        }
        return null;
    }
}

// MinimaxStrategy.java (Hard - Optimal play)
public class MinimaxStrategy implements AIStrategy {
    private static final int MAX_DEPTH = 9;
    private Map<String, Integer> memo;
    
    public MinimaxStrategy() {
        this.memo = new HashMap<>();
    }
    
    @Override
    public Position getBestMove(Board board, Symbol aiSymbol) {
        Position bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Position pos : board.getAvailableMoves()) {
            Board copy = board.clone();
            copy.makeMove(pos.getRow(), pos.getCol(), aiSymbol);
            
            int score = minimax(copy, 0, false, aiSymbol, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = pos;
            }
        }
        
        return bestMove;
    }
    
    private int minimax(Board board, int depth, boolean isMaximizing, 
                       Symbol aiSymbol, int alpha, int beta) {
        
        Symbol winner = board.checkWinner();
        
        // Terminal states
        if (winner == aiSymbol) return 10 - depth;
        if (winner == aiSymbol.getOpponent()) return depth - 10;
        if (board.isFull()) return 0;
        
        // Check memo
        String state = boardToString(board);
        if (memo.containsKey(state)) {
            return memo.get(state);
        }
        
        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            
            for (Position pos : board.getAvailableMoves()) {
                Board copy = board.clone();
                copy.makeMove(pos.getRow(), pos.getCol(), aiSymbol);
                
                int score = minimax(copy, depth + 1, false, aiSymbol, alpha, beta);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                
                if (beta <= alpha) {
                    break; // Alpha-beta pruning
                }
            }
            
            memo.put(state, maxScore);
            return maxScore;
            
        } else {
            int minScore = Integer.MAX_VALUE;
            
            for (Position pos : board.getAvailableMoves()) {
                Board copy = board.clone();
                copy.makeMove(pos.getRow(), pos.getCol(), aiSymbol.getOpponent());
                
                int score = minimax(copy, depth + 1, true, aiSymbol, alpha, beta);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                
                if (beta <= alpha) {
                    break; // Alpha-beta pruning
                }
            }
            
            memo.put(state, minScore);
            return minScore;
        }
    }
    
    private String boardToString(Board board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                sb.append(board.getCell(i, j).getSymbol().getDisplayChar());
            }
        }
        return sb.toString();
    }
}
```

---

### Game

```java
// Move.java
import java.time.LocalDateTime;

public class Move {
    private final Player player;
    private final Position position;
    private final Symbol symbol;
    private final LocalDateTime timestamp;
    
    public Move(Player player, Position position) {
        this.player = player;
        this.position = position;
        this.symbol = player.getSymbol();
        this.timestamp = LocalDateTime.now();
    }
    
    public Player getPlayer() { return player; }
    public Position getPosition() { return position; }
    public Symbol getSymbol() { return symbol; }
}

// Game.java
import java.time.LocalDateTime;
import java.util.*;

public class Game {
    private final String gameId;
    private final Board board;
    private final Player player1;
    private final Player player2;
    private Player currentPlayer;
    private final List<Move> moves;
    private GameState state;
    private GameResult result;
    private final GameMode mode;
    private final LocalDateTime createdAt;
    
    public Game(Player player1, Player player2, GameMode mode, int boardSize) {
        this.gameId = UUID.randomUUID().toString();
        this.board = new Board(boardSize);
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1; // X always starts
        this.moves = new ArrayList<>();
        this.state = GameState.WAITING;
        this.mode = mode;
        this.createdAt = LocalDateTime.now();
    }
    
    public void start() {
        this.state = GameState.IN_PROGRESS;
        System.out.println("\n🎮 Game Started!");
        System.out.println("Player 1: " + player1.getName() + " (" + player1.getSymbol() + ")");
        System.out.println("Player 2: " + player2.getName() + " (" + player2.getSymbol() + ")");
        System.out.println();
    }
    
    public boolean makeMove(Player player, Position position) {
        if (state != GameState.IN_PROGRESS) {
            System.out.println("❌ Game is not in progress");
            return false;
        }
        
        if (!player.equals(currentPlayer)) {
            System.out.println("❌ Not your turn!");
            return false;
        }
        
        if (!board.makeMove(position.getRow(), position.getCol(), player.getSymbol())) {
            System.out.println("❌ Invalid move");
            return false;
        }
        
        // Record move
        moves.add(new Move(player, position));
        
        // Check game over
        Symbol winner = board.checkWinner();
        if (winner != Symbol.EMPTY) {
            endGame(winner);
            return true;
        }
        
        if (board.isFull()) {
            endGame(Symbol.EMPTY); // Draw
            return true;
        }
        
        // Switch turn
        switchTurn();
        return true;
    }
    
    private void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
    
    private void endGame(Symbol winner) {
        this.state = GameState.COMPLETED;
        
        if (winner == Symbol.EMPTY) {
            this.result = GameResult.DRAW;
            System.out.println("\n🤝 Game ended in a DRAW!");
            player1.updateStats(GameResult.DRAW);
            player2.updateStats(GameResult.DRAW);
        } else {
            this.result = (winner == Symbol.X) ? GameResult.X_WINS : GameResult.O_WINS;
            Player winningPlayer = (winner == player1.getSymbol()) ? player1 : player2;
            
            System.out.println("\n🎉 " + winningPlayer.getName() + " WINS!");
            
            player1.updateStats(result);
            player2.updateStats(result);
        }
    }
    
    public boolean undo() {
        if (moves.isEmpty()) {
            return false;
        }
        
        // Remove last move
        Move lastMove = moves.remove(moves.size() - 1);
        Position pos = lastMove.getPosition();
        board.getCell(pos.getRow(), pos.getCol()).clear();
        
        // Switch turn back
        switchTurn();
        
        System.out.println("↩️  Undo: removed move at (" + pos.getRow() + ", " + pos.getCol() + ")");
        return true;
    }
    
    public void replay() {
        System.out.println("\n📺 Replaying game...\n");
        Board replayBoard = new Board(board.getSize());
        
        for (Move move : moves) {
            replayBoard.makeMove(move.getPosition().getRow(), 
                               move.getPosition().getCol(), 
                               move.getSymbol());
            
            System.out.println(move.getPlayer().getName() + " plays " + 
                             move.getSymbol() + " at " + move.getPosition());
            replayBoard.display();
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public boolean isGameOver() {
        return state == GameState.COMPLETED;
    }
    
    public Player getCurrentPlayer() { return currentPlayer; }
    public Board getBoard() { return board; }
    public GameState getState() { return state; }
    public GameResult getResult() { return result; }
    public List<Move> getMoves() { return new ArrayList<>(moves); }
}
```

---

### Game Service & Demo

```java
// GameService.java (Singleton)
public class GameService {
    private static GameService instance;
    private final Map<String, Game> activeGames;
    private final Map<String, Player> players;
    
    private GameService() {
        this.activeGames = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
    }
    
    public static synchronized GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }
    
    public Player registerPlayer(String name, boolean isAI, Difficulty difficulty) {
        Player player;
        
        if (isAI) {
            player = new AIPlayer(name, Symbol.X, difficulty);
        } else {
            player = new HumanPlayer(name, Symbol.X);
        }
        
        players.put(player.getName(), player);
        return player;
    }
    
    public Game createGame(Player player1, Player player2, GameMode mode, int boardSize) {
        // Assign symbols
        if (player1 instanceof HumanPlayer) {
            player1 = new HumanPlayer(player1.getName(), Symbol.X);
        } else {
            player1 = new AIPlayer(player1.getName(), Symbol.X, 
                                  ((AIPlayer) player1).difficulty);
        }
        
        if (player2 instanceof HumanPlayer) {
            player2 = new HumanPlayer(player2.getName(), Symbol.O);
        } else {
            player2 = new AIPlayer(player2.getName(), Symbol.O, 
                                  ((AIPlayer) player2).difficulty);
        }
        
        Game game = new Game(player1, player2, mode, boardSize);
        activeGames.put(game.gameId, game);
        
        return game;
    }
    
    public void playGame(Game game) {
        game.start();
        
        while (!game.isGameOver()) {
            game.getBoard().display();
            
            Player current = game.getCurrentPlayer();
            Position move = current.makeMove(game.getBoard());
            
            game.makeMove(current, move);
        }
        
        game.getBoard().display();
        
        // Show final stats
        System.out.println("\n📊 Final Stats:");
        System.out.println(game.player1.getName() + ": " + game.player1.getStats());
        System.out.println(game.player2.getName() + ": " + game.player2.getStats());
    }
}

// TicTacToeDemo.java
public class TicTacToeDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   TIC TAC TOE GAME SYSTEM DEMONSTRATION      ");
        System.out.println("═══════════════════════════════════════════════\n");
        
        GameService gameService = GameService.getInstance();
        
        // ===== SCENARIO 1: Human vs Human =====
        System.out.println("═══ SCENARIO 1: Human vs Human (Local) ═══\n");
        
        // Note: In real demo, this would use Scanner for input
        // For demonstration, we'll skip to AI games
        
        // ===== SCENARIO 2: Human vs AI (Easy) =====
        System.out.println("═══ SCENARIO 2: Human vs AI (Easy) ═══\n");
        
        Player alice = new HumanPlayer("Alice", Symbol.X);
        Player aiEasy = new AIPlayer("Bot-Easy", Symbol.O, Difficulty.EASY);
        
        // Simulated game (predefined moves)
        Game game1 = new Game(alice, aiEasy, GameMode.AI, 3);
        simulateGame(game1, new int[][]{{0,0}, {1,1}, {0,1}, {2,2}, {0,2}}); // Alice wins
        
        // ===== SCENARIO 3: AI vs AI (Medium vs Hard) =====
        System.out.println("\n═══ SCENARIO 3: AI vs AI (Medium vs Hard) ═══\n");
        
        Player aiMedium = new AIPlayer("Bot-Medium", Symbol.X, Difficulty.MEDIUM);
        Player aiHard = new AIPlayer("Bot-Hard", Symbol.O, Difficulty.HARD);
        
        Game game2 = new Game(aiMedium, aiHard, GameMode.AI_VS_AI, 3);
        gameService.playGame(game2);
        
        // ===== SCENARIO 4: 4×4 Board =====
        System.out.println("\n═══ SCENARIO 4: Larger Board (4×4) ═══\n");
        
        Player bob = new AIPlayer("Bob", Symbol.X, Difficulty.MEDIUM);
        Player charlie = new AIPlayer("Charlie", Symbol.O, Difficulty.MEDIUM);
        
        Game game3 = new Game(bob, charlie, GameMode.AI_VS_AI, 4);
        gameService.playGame(game3);
        
        // ===== SCENARIO 5: Undo Feature =====
        System.out.println("\n═══ SCENARIO 5: Undo Feature ═══\n");
        
        Game game4 = new Game(
            new HumanPlayer("Player1", Symbol.X),
            new AIPlayer("Bot", Symbol.O, Difficulty.EASY),
            GameMode.AI, 3
        );
        
        game4.start();
        game4.makeMove(game4.getCurrentPlayer(), new Position(0, 0));
        game4.getBoard().display();
        
        game4.makeMove(game4.getCurrentPlayer(), new Position(1, 1));
        game4.getBoard().display();
        
        System.out.println("Undoing last move...");
        game4.undo();
        game4.getBoard().display();
        
        // ===== SCENARIO 6: Game Replay =====
        System.out.println("\n═══ SCENARIO 6: Game Replay ═══\n");
        game1.replay();
        
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("         DEMO COMPLETED                        ");
        System.out.println("═══════════════════════════════════════════════");
    }
    
    private static void simulateGame(Game game, int[][] moves) {
        game.start();
        
        for (int[] move : moves) {
            Position pos = new Position(move[0], move[1]);
            game.makeMove(game.getCurrentPlayer(), pos);
            game.getBoard().display();
        }
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** AI strategies  
**Why:** Multiple AI algorithms (Random, Medium, Minimax)  
**Interview Justification:** "Strategy pattern allows switching AI difficulty at runtime without changing AIPlayer class. Each strategy encapsulates a different algorithm."

---

### Pattern 2: Template Method Pattern
**Where:** Player.makeMove() flow  
**Why:** Common flow with strategy-specific implementation  
**Interview Justification:** "Both HumanPlayer and AIPlayer follow same flow: validate → get move → return position. Template Method defines skeleton."

---

### Pattern 3: Singleton Pattern
**Where:** GameService  
**Why:** Single game manager instance  
**Interview Justification:** "GameService manages all active games. Singleton ensures single point of coordination."

---

### Pattern 4: Command Pattern
**Where:** Move history for undo/redo  
**Why:** Encapsulate moves as objects  
**Interview Justification:** "Each Move is a Command that can be undone. Enables undo/redo functionality cleanly."

---

### Pattern 5: Observer Pattern
**Where:** Spectators watching game  
**Why:** Multiple observers need updates  
**Interview Justification:** "When game state changes, notify all spectators. Observer pattern decouples game logic from notification logic."

---

## 💡 Step 8: Interview Discussion Points

### 1. Minimax Algorithm Complexity

**Interviewer:** "What's the time complexity of Minimax?"

**Answer:**
"For 3×3 Tic Tac Toe:
- **Without pruning:** O(b^d) where b = avg branching factor (~5), d = depth (~9)
  - ~O(5^9) = ~2 million states
- **With Alpha-Beta pruning:** Reduces to O(b^(d/2)) = ~O(5^4.5) = ~3000 states
- **With memoization:** Cache visited states, avoid recomputation

**First move analysis:**
- 9 possible positions
- Each explores ~362,880 game trees (9!)
- With pruning + memo: < 5000 states evaluated
- Completes in ~100ms

**Optimization:** For 3×3, can pre-compute optimal move table (opening book)."

---

### 2. Win Detection Optimization

**Interviewer:** "Can you optimize win detection?"

**Answer:**
"**Current:** O(N) per move check (check row, col, diagonals)

**Optimization:** **Count-based approach:**
```java
class Board {
    private int[] rowCounts = new int[size];     // Count per row
    private int[] colCounts = new int[size];     // Count per col
    private int diagCount = 0;                   // Main diagonal
    private int antiDiagCount = 0;               // Anti-diagonal
    
    public boolean makeMove(int row, int col, Symbol symbol) {
        int value = (symbol == Symbol.X) ? 1 : -1;
        
        rowCounts[row] += value;
        colCounts[col] += value;
        
        if (row == col) diagCount += value;
        if (row + col == size - 1) antiDiagCount += value;
        
        // Win if any count reaches ±size
        if (Math.abs(rowCounts[row]) == size ||
            Math.abs(colCounts[col]) == size ||
            Math.abs(diagCount) == size ||
            Math.abs(antiDiagCount) == size) {
            return true; // Winner!
        }
        
        return false;
    }
}
```

**Complexity:** O(1) win detection per move (vs O(N))  
**Trade-off:** Slightly more complex, but constant time"

---

### 3. Scaling to N×N Boards

**Interviewer:** "How does Minimax scale to larger boards?"

**Answer:**
"**Complexity explosion:**
- 3×3: ~5,478 states total
- 4×4: ~10^13 states (infeasible)
- 5×5: Astronomical

**Solutions for N > 3:**

1. **Depth-limited search:** Limit Minimax to depth 3-4
2. **Evaluation function:** Heuristic scoring instead of exhaustive search
3. **Monte Carlo Tree Search (MCTS):** Random simulations (used in AlphaGo)
4. **Opening book:** Pre-compute strong opening moves
5. **Pruning heuristics:** Order moves by likelihood (center, corners first)

**For interview:** Mention trade-off between optimality and speed. Perfect play possible for 3×3, but larger boards need heuristics."

---

### 4. Online Multiplayer Synchronization

**Interviewer:** "How do you handle online games?"

**Answer:**
"**WebSocket-based architecture:**

```
Player A ──┐
Player B ──┼──→ Game Server ──→ Database
Spectators─┘        ↓
                WebSocket
```

**Implementation:**
1. Player A makes move → sends to server
2. Server validates move → updates game state
3. Server broadcasts to Player B + Spectators via WebSocket
4. Player B sees board update in real-time

**Handling disconnects:**
- If player disconnects → wait 30 seconds
- If no reconnect → forfeit game
- Store game state in Redis for reconnection

**Race condition:** Two players send moves simultaneously
- Server processes sequentially (queue)
- Second move rejected if not their turn"

---

### 5. Tournament Implementation

**Interviewer:** "How would you implement a tournament?"

**Answer:**
"**Two tournament types:**

**Round-Robin (everyone plays everyone):**
```java
class Tournament {
    private List<Player> players;
    private List<Game> games;
    
    public void generateMatches() {
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                games.add(new Game(players.get(i), players.get(j), ...));
            }
        }
        // Total games: C(n, 2) = n(n-1)/2
    }
    
    public Player getWinner() {
        Map<Player, Integer> points = new HashMap<>();
        // Win = 3 points, Draw = 1 point, Loss = 0
        // Sort by points
        return topPlayer;
    }
}
```

**Knockout (Single elimination):**
```
Round 1: 8 players → 4 games → 4 winners
Round 2: 4 players → 2 games → 2 winners  
Final:   2 players → 1 game → 1 winner
```

Requires balanced bracket (2^n players)."

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `Board`: Manages game grid
- `AIStrategy`: Computes moves
- `Game`: Orchestrates gameplay
- `Player`: Represents participant

### Open/Closed ✅
```java
// New AI strategy without modifying existing code
public class NeuralNetworkStrategy implements AIStrategy {
    @Override
    public Position getBestMove(Board board, Symbol symbol) {
        // ML-based move selection
    }
}
```

### Liskov Substitution ✅
```java
public void playTurn(Player player, Board board) {
    Position move = player.makeMove(board); // Works for Human or AI
}
```

### Interface Segregation ✅
```java
interface AIStrategy { Position getBestMove(...); }
interface Undoable { boolean undo(); }
interface Replayable { void replay(); }
```

### Dependency Inversion ✅
```java
public class AIPlayer extends Player {
    private AIStrategy strategy; // Depends on abstraction
    
    public AIPlayer(AIStrategy strategy) {
        this.strategy = strategy; // Injected
    }
}
```

---

## 🎯 Key Takeaways

### Core Patterns
- ✅ **Strategy Pattern** - AI difficulty levels
- ✅ **Template Method** - Player move flow
- ✅ **Singleton Pattern** - GameService
- ✅ **Command Pattern** - Move history
- ✅ **Observer Pattern** - Spectators

### Algorithms
- ✅ **Minimax** - Optimal AI play
- ✅ **Alpha-Beta Pruning** - Performance optimization
- ✅ **Memoization** - State caching
- ✅ **Win Detection** - O(N) or O(1) with counters

### System Features
- ✅ **Extensible board size** - Works for 3×3, 4×4, N×N
- ✅ **Multiple game modes** - Local, AI, Online
- ✅ **Undo/Redo** - Command pattern
- ✅ **Game replay** - Move history
- ✅ **Player statistics** - Win/loss tracking
- ✅ **AI difficulty levels** - Easy to Hard

---

**Total: 136 DSA + 8 LLD Problems**

All changes ready for review!
