# Low-Level Design: Text Editor

**Difficulty:** Medium ⚡ → Hard 🔥 (with undo/redo and piece table)

**Interview Duration:** 45–75 minutes

---

## 📋 Interview Approach

Classic LLD: **cursor**, **insert/delete**, optional **selection**, **copy/paste**, **undo/redo**. Interviewers often start with a **gap buffer** or **rope**; advanced follow-up is **piece table** (used in real editors).

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says

*"Design a text editor’s core: move cursor, insert and delete characters, and support undo/redo. How would you represent the buffer for good performance on large files?"*

### Clarifying Questions

1. **Unicode:** Code points vs UTF-16 code units (Java `String`)?  
2. **Large files:** Multi-MB documents — avoid O(n) per keystroke?  
3. **Undo:** Linear undo vs grouped typing bursts?  
4. **Concurrency:** Single-threaded UI model (typical)?  
5. **Clipboard:** OS clipboard integration — treat as adapter?

---

## 🔹 Step 2: Requirements

### Functional

1. **Cursor** at index 0…length (gap between characters).  
2. **Insert** string at cursor; cursor advances.  
3. **Delete** backspace (before cursor) and delete (after cursor).  
4. **Selection** optional: anchor + active end; replace selection on insert.  
5. **Undo / redo** stack of **commands** (text + cursor moves optional).  
6. **Copy / paste** via internal clipboard + optional system bridge.

### Non-functional

- **Large edits:** Sub-linear or amortized cost per operation where possible.  
- **Memory:** Avoid copying entire buffer on each insert for huge files.

---

## 🔹 Step 3: Representation options

| Approach | Insert (avg) | Notes |
|----------|----------------|-------|
| **Array / StringBuilder** | O(n) | Simple; poor for large n |
| **Gap buffer** | O(1) amortized at gap; O(n) worst gap move | Great for local edits |
| **Rope / balanced tree** | O(log n) | Heavier implementation |
| **Piece table** | O(1) append to piece list; edit = new spans | Used in VS Code / Word |

### Gap buffer (interview default)

- One `char[]` with a **gap** at cursor.  
- Insert: if gap full, resize; move gap to cursor; copy chars into gap.  
- Delete: extend gap over deleted range.

### Piece table (follow-up)

- Original buffer + append-only **add** buffer.  
- **Piece:** pointer to buffer + start + length.  
- Linked list of pieces; split piece on edit; merge adjacent when possible.

---

## 🔹 Step 4: Command pattern for undo

```java
interface EditCommand {
    void execute(EditorModel model);
    void undo(EditorModel model);
}

final class InsertCommand implements EditCommand {
    private final int offset;
    private final String text;
    // execute inserts; undo deletes same range
}
```

- **Undo stack** and **redo stack**; new edit after undo clears redo.  
- **Macro:** merge consecutive inserts into one command for undo granularity.

---

## 🔹 Step 5: Core interfaces

```java
public interface TextBuffer {
    void moveGapTo(int logicalIndex);
    void insert(int index, String s);
    void deleteRange(int start, int end);
    int length();
    String substring(int start, int end);
}

public final class EditorModel {
    private TextBuffer buffer;
    private int cursor;
    private Selection selection; // optional
}
```

---

## 🔹 Step 6: Testing angles

- Insert at start / middle / end; surrogate pairs (emoji).  
- Undo restores cursor position if spec requires.  
- Stress: many inserts at same spot (gap stays local).

---

## 🔹 Follow-ups

- **Collaborative editing:** OT or CRDT — separate hard round.  
- **Line/column** display: maintain line index cache invalidated on newline edits.  
- **Syntax highlighting:** async lexer; piece boundaries help incremental lex.
