# Low-Level Design: Chess Game

**Difficulty:** Hard 🔥  
**Interview duration:** 60–75 min  

---

## How to present in an interview

Present in this order — interviewers expect **flow first**, then **model**, then **code**:

1. **Core flow** — main use cases as numbered steps (happy path + key branches).
2. **Entities & relationships** — nouns and verbs from the flow; who owns whom.
3. **Reference implementation** — classes that map 1:1 to the model (only after flow is agreed).

Do not open with a class diagram or code dumps before the flow is clear.

---

## 1. Core flow

### 1.1 Play game

1. Two players join; **board** initialized.
2. Active player submits **move**; engine validates legality.
3. Board updates; check checkmate/stalemate/draw rules.
4. Optional: undo/redo via **command** stack.

---

## 2. Entities & relationships

_Deduced from the flows above — each entity should appear in at least one step._

| Entity | Responsibility | Key fields / collaborators |
|--------|----------------|----------------------------|
| **Game** | Session orchestrator | board, players, status, turn |
| **Board** | 8×8 state | squares, pieces |
| **Piece / Move** | Rules + application | from, to, capture, special moves |
| **Player** | Human or AI | color, clock |
| **GameStatus** | Lifecycle | IN_PROGRESS, CHECKMATE, STALEMATE, … |

### Relationships

- Game **1—1** Board; Game **2—** Player
- MoveCommand applies to Board (Command pattern for undo)

---

## 3. Reference implementation (Java)


