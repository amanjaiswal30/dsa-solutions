# Flood Fill

![](../../assets/images/graphs/flood_fill.svg)


**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
You are given an `m x n` integer matrix `image` representing a pixel grid, a starting pixel `(sr, sc)`, and a new color `color`.

Perform a **flood fill** starting from `(sr, sc)`:
- Let `originalColor = image[sr][sc]`.
- Change the color of `(sr, sc)` and **all connected pixels** that currently have `originalColor` to `color`.
- Two pixels are connected if they share an edge (**4-directional**: up, down, left, right).

Return the modified image.

This is the classic **paint bucket** operation on a 2D grid.

---

## 🔹 Examples

**Input:**
```
image = [
  [1, 1, 1],
  [1, 1, 0],
  [1, 0, 1]
]
sr = 1, sc = 1, color = 2
```

**Output:**
```
[
  [2, 2, 2],
  [2, 2, 0],
  [2, 0, 1]
]
```

**Explanation:** Starting at `(1,1)` (value `1`), all 4-directionally connected `1`s become `2`. The `0` and isolated `1` at `(2,2)` stay unchanged.

---

**Input:**
```
image = [[0, 0, 0], [0, 0, 0]]
sr = 0, sc = 0, color = 0
```

**Output:** `[[0, 0, 0], [0, 0, 0]]`

**Explanation:** New color equals original color — no change needed.

---

## 🔹 Constraints
- `m == image.length`
- `n == image[i].length`
- `1 <= m, n <= 50`
- `0 <= image[i][j], color < 2^16`
- `0 <= sr < m`, `0 <= sc < n`

---

## 🔹 Intuition & Logic
Treat the grid as an implicit graph: each cell is a node; edges connect 4-neighbors with the **same color**.

From `(sr, sc)`, explore every reachable cell with value `originalColor` and recolor them.

This is the same core idea as **Number of Islands**, but instead of counting components you **mutate** all cells in one component.

Key early exit:
```java
if (originalColor == color) return image; // nothing to do
```

---

## 🔹 Approaches

### 1. DFS (Recursive)
- Save `originalColor`.
- DFS from `(sr, sc)`: if out of bounds, wrong color, or already recolored → return.
- Set `image[r][c] = color`, recurse on 4 neighbors.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n) recursion stack worst case

---

### 2. BFS (Iterative)
- Same logic, use a queue to avoid deep recursion on large connected regions.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n) queue worst case

---

### 3. DFS (Iterative with stack)
- Explicit stack — useful when recursion depth is a concern.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n)

---

## 🔹 Java Code (DFS, BFS, Iterative DFS)

```java
import java.util.*;

public class FloodFill {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // 1. Recursive DFS
    public static int[][] floodFillDFS(int[][] image, int sr, int sc, int color) {
        int original = image[sr][sc];
        if (original == color) return image;
        dfs(image, sr, sc, original, color);
        return image;
    }

    private static void dfs(int[][] image, int r, int c, int original, int color) {
        int m = image.length, n = image[0].length;
        if (r < 0 || c < 0 || r >= m || c >= n || image[r][c] != original) return;

        image[r][c] = color;
        for (int[] d : DIRS) {
            dfs(image, r + d[0], c + d[1], original, color);
        }
    }

    // 2. BFS
    public static int[][] floodFillBFS(int[][] image, int sr, int sc, int color) {
        int original = image[sr][sc];
        if (original == color) return image;

        int m = image.length, n = image[0].length;
        Queue<int[]> queue = new ArrayDeque<>();
        image[sr][sc] = color;
        queue.add(new int[]{sr, sc});

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0], c = cell[1];

            for (int[] d : DIRS) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nc >= 0 && nr < m && nc < n && image[nr][nc] == original) {
                    image[nr][nc] = color;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        return image;
    }

    // 3. Iterative DFS (explicit stack)
    public static int[][] floodFillIterativeDFS(int[][] image, int sr, int sc, int color) {
        int original = image[sr][sc];
        if (original == color) return image;

        int m = image.length, n = image[0].length;
        Deque<int[]> stack = new ArrayDeque<>();
        image[sr][sc] = color;
        stack.push(new int[]{sr, sc});

        while (!stack.isEmpty()) {
            int[] cell = stack.pop();
            int r = cell[0], c = cell[1];

            for (int[] d : DIRS) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nc >= 0 && nr < m && nc < n && image[nr][nc] == original) {
                    image[nr][nc] = color;
                    stack.push(new int[]{nr, nc});
                }
            }
        }
        return image;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach        | Time Complexity | Space Complexity        |
|-----------------|-----------------|-------------------------|
| DFS (recursive) | O(m × n)        | O(m × n) recursion stack |
| BFS             | O(m × n)        | O(m × n) queue          |
| Iterative DFS   | O(m × n)        | O(m × n) stack          |

Each cell is visited at most once because we recolor immediately (`image[r][c] = color`).

---

## 🔹 Edge Cases
- `originalColor == color` → return image unchanged (avoid infinite loop)
- Single cell grid
- Start pixel is already isolated (no neighbors with same color)
- Entire grid is one color → all cells recolored
- Large uniform region → prefer BFS / iterative DFS over deep recursion

---

## 🔹 Follow-Up Questions
1. How does this relate to [Number of Islands](number_of_islands.md)?
2. What changes for **8-directional** connectivity?
3. How would you count **how many pixels** were recolored?
4. Can you implement flood fill **without modifying** the input (using a `visited` array)?
