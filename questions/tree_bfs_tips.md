# Tree BFS — when to use `level` (per-level loop)

BFS on trees always uses a **queue**. The choice is whether to process **one level at a time** (`levelSize = queue.size()`) or **one node at a time** (simple `while (!queue.isEmpty())`).

![](../assets/images/patterns/tree_bfs_decision.svg)

---

## Two BFS shapes

### A · With level snapshot (inner `for` loop)

```java
while (!queue.isEmpty()) {
    int levelSize = queue.size();   // freeze count for THIS level only
    for (int i = 0; i < levelSize; i++) {
        TreeNode node = queue.poll();
        // process node; enqueue children for NEXT level
    }
}
```

### B · Without level (single dequeue per iteration)

```java
while (!queue.isEmpty()) {
    TreeNode node = queue.poll();
    // process node; enqueue children
}
```

---

## When to USE level (`levelSize` loop)

| Problem type | Why level matters |
|--------------|-------------------|
| **Level order traversal** (return `List<List<Integer>>`) | Each inner loop = one row in output |
| **Shortest path / min depth** (unweighted) | Depth increases exactly once per level; first leaf = answer |
| **Right view / left view** | Only **last** (or **first**) node per level counts |
| **Zigzag level order** | Reverse every other level list |
| **Average of levels** | Sum nodes per level, divide by `levelSize` |
| **Largest value per level** | Compare only within same `levelSize` batch |
| **Connect nodes at same level** (perfect tree) | Wire pointers at end of each level pass |
| **Check if tree is complete** | Index / slot math is level-based |

**Rule:** if the answer depends on **which horizontal layer** a node is on, use the level snapshot.

---

## When NOT to use level

| Problem type | What to do instead |
|--------------|-------------------|
| **Visit every node** (count, sum, find any target) | Simple BFS; level adds no value |
| **Serialize / deserialize** (general) | Often level BFS for level-order encoding; preorder/inorder use DFS instead |
| **Track depth per node** | Push `(node, depth)` or `(node, parent)`; depth is data on the queue entry |
| **Multi-source BFS on grid** | Push all sources with `0`, then `(r,c,dist+1)` — distance on entry, not `queue.size()` |
| **BFS with early exit** (e.g. path exists) | Stop when target found; level boundary optional |
| **Graph BFS** (not tree) | Same as multi-source: state + distance on queue; level loop only if you need layer output |

**Rule:** if you only need **reachability** or **global aggregate**, skip the level loop.

---

## Level BFS vs DFS with depth parameter

| Use level BFS | Consider DFS + `depth` |
|---------------|-------------------------|
| Output grouped by level | Path sum, diameter, LCA (often DFS) |
| Min depth / shortest path to leaf | Max depth (height) — either works |
| Views (left/right) | Inorder / preorder traversals |

DFS with `depth` is not the same as level BFS: DFS depth follows **branch**, not **tree layer** unless you pass level explicitly.

---

## Decision flow

See diagram at top. Quick text version:

- **Output/logic needs all nodes at depth d?** → `levelSize` inner loop
- **Shortest steps to a node (unweighted tree)?** → `levelSize` (each pass = +1 distance)
- **Only visit every node once?** → simple queue poll loop
- **Need depth per node?** → queue stores `(node, depth)` — level loop optional

---

## Pitfalls

| Pitfall | Fix |
|---------|-----|
| Using `queue.size()` inside `for` without snapshot | Save `int sz = queue.size()` before inner loop |
| Level loop on **wrong** problem (path sum) | Path = root-to-leaf DFS; level = horizontal slice |
| Forgetting children enqueued in same level poll | Children always belong to **next** level — enqueue after processing current node |
| Min depth: checking `node.left == null` only | A leaf is **both** children null |

---

## Related problems in this repo

- [Level order traversal](trees/level_order_traversal.md) — classic level loop
- [Left view of tree](trees/left_view_of_tree.md) — first per level
- [Bottom view of binary tree](trees/bottom_view_of_binary_tree.md) — level + horizontal index
- [Populating next right pointer](trees/populating_next_right_pointer.md) — level wiring
- [Diameter of tree](trees/diameter_of_tree.md) — DFS, not level BFS
- [Rotting oranges](graphs/rotting_oranges.md) — grid multi-source BFS with time = levels
