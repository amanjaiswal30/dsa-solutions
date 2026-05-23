# Low-Level Design: Text Editor

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

### 1.1 Edit document

1. User moves **cursor**; inserts/deletes text in **buffer**.
2. Optional **selection** for bulk delete/replace.
3. **Undo/redo** via command stack (Command pattern).
4. Copy/paste uses clipboard buffer.

---

## 2. Entities & relationships

_Deduced from the flows above — each entity should appear in at least one step._

| Entity | Responsibility | Key fields / collaborators |
|--------|----------------|----------------------------|
| **EditorModel** | Facade | cursor, selection, buffer |
| **TextBuffer** | Storage | gap buffer / rope / piece table |
| **Command** | Undo unit | execute, unexecute |
| **UndoManager** | History | undo stack, redo stack |

### Relationships

- EditorModel **1—1** TextBuffer
- Commands mutate buffer; UndoManager stacks Command objects

---

## 3. Reference implementation (Java)


