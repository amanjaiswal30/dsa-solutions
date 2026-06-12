# Largest Island

![](../../assets/images/graphs/largest_island.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
You are given an `m x n` binary matrix `grid` where:
- `1` represents **land**
- `0` represents **water**

An **island** is a group of `1`s connected **4-directionally** (up, down, left, right).

Return the **area of the largest island** in the grid. If there is no land, return `0`.

The **area** of an island is the number of land cells in that connected component.

---

## 🔹 Examples

**Input:**
```
grid = [
  [0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0],
  [0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0],
  [0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0],
  [0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0],
  [0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0],
  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0],
  [0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0],
  [0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0]
]
```

**Output:** `6`

**Explanation:** The largest island (highlighted below) has area `6`.

```
  . . 1 . . . . 1 . . . . .
  . . . . . . . 1 1 1 . . .
  . 1 1 . 1 . . . . . . . .
  . 1 . . 1 1 . . 1 . 1 . .
  . 1 . . 1 1 . . 1 1 1 . .   ← this component has 6 land cells
  . . . . . . . . . . 1 . .
  . . . . . . . 1 1 1 . . .
  . . . . . . . 1 1 . . . .
```

---

**Input:**
```
grid = [[0, 0, 0], [0, 0, 0], [0, 0, 0]]
```

**Output:** `0`

**Explanation:** No land cells.

---

## 🔹 Constraints
- `m == grid.length`
- `n == grid[i].length`
- `1 <= m, n <= 50`
- `grid[i][j]` is `0` or `1`

---

## 🔹 Intuition & Logic
This is a direct extension of [Number of Islands](number_of_islands.md):
- **Number of Islands** → count how many connected `'1'` components exist.
- **Largest Island** → track the **maximum size** among those components.

For each unvisited land cell, run DFS/BFS and **count cells** in that component. Update a running `maxArea`.

Same grid-as-graph model: each `1` is a node; edges connect 4-neighbors that are also `1`.

---

## 🔹 Approaches

### 1. DFS
- Scan the grid.
- When `grid[i][j] == 1` and not yet visited, DFS and return the component size.
- Update `maxArea = max(maxArea, size)`.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n) (`visited` + recursion stack)

---

### 2. BFS
- Same logic with a queue; increment an area counter as you dequeue land cells.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n) (`visited` + queue)

---

### 3. In-Place Sink (DFS)
- Mark visited land by flipping `1 → 0` during traversal (mutates input).
- Avoids extra `visited[][]` at the cost of modifying the grid.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n) recursion stack only

---

## 🔹 Java Code (DFS, BFS, In-Place DFS)

```java
import java.util.*;

public class LargestIsland {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // 1. DFS with visited[]
    public static int maxAreaOfIslandDFS(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int m = grid.length, n = grid[0].length;
        boolean[][] visited = new boolean[m][n];
        int maxArea = 0;

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 1 && !visited[r][c]) {
                    maxArea = Math.max(maxArea, dfs(grid, visited, r, c));
                }
            }
        }
        return maxArea;
    }

    private static int dfs(int[][] grid, boolean[][] visited, int r, int c) {
        int m = grid.length, n = grid[0].length;
        if (r < 0 || c < 0 || r >= m || c >= n || grid[r][c] != 1 || visited[r][c])
            return 0;

        visited[r][c] = true;
        int area = 1;
        for (int[] d : DIRS) {
            area += dfs(grid, visited, r + d[0], c + d[1]);
        }
        return area;
    }

    // 2. BFS with visited[]
    public static int maxAreaOfIslandBFS(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int m = grid.length, n = grid[0].length;
        boolean[][] visited = new boolean[m][n];
        int maxArea = 0;
        Queue<int[]> queue = new ArrayDeque<>();

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] != 1 || visited[r][c]) continue;

                visited[r][c] = true;
                queue.add(new int[]{r, c});
                int area = 0;

                while (!queue.isEmpty()) {
                    int[] cell = queue.poll();
                    area++;
                    int cr = cell[0], cc = cell[1];

                    for (int[] d : DIRS) {
                        int nr = cr + d[0], nc = cc + d[1];
                        if (nr >= 0 && nc >= 0 && nr < m && nc < n
                                && grid[nr][nc] == 1 && !visited[nr][nc]) {
                            visited[nr][nc] = true;
                            queue.add(new int[]{nr, nc});
                        }
                    }
                }
                maxArea = Math.max(maxArea, area);
            }
        }
        return maxArea;
    }

    // 3. In-place DFS (sink land to 0)
    public static int maxAreaOfIslandSink(int[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int m = grid.length, n = grid[0].length;
        int maxArea = 0;

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == 1) {
                    maxArea = Math.max(maxArea, sinkDfs(grid, r, c));
                }
            }
        }
        return maxArea;
    }

    private static int sinkDfs(int[][] grid, int r, int c) {
        int m = grid.length, n = grid[0].length;
        if (r < 0 || c < 0 || r >= m || c >= n || grid[r][c] != 1) return 0;

        grid[r][c] = 0;
        int area = 1;
        for (int[] d : DIRS) {
            area += sinkDfs(grid, r + d[0], c + d[1]);
        }
        return area;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach        | Time Complexity | Space Complexity              |
|-----------------|-----------------|-------------------------------|
| DFS + visited   | O(m × n)        | O(m × n) visited + stack      |
| BFS + visited   | O(m × n)        | O(m × n) visited + queue      |
| In-place DFS    | O(m × n)        | O(m × n) recursion stack      |

Each land cell is visited at most once.

---

## 🔹 Edge Cases
- All water → return `0`
- All land → return `m × n`
- Single-cell island → return `1`
- Multiple islands of equal max size → return that shared max area
- One large island plus many tiny ones → return size of the large one

---

## 🔹 Follow-Up Questions
1. How does this differ from [Number of Islands](number_of_islands.md)?
2. What if you may **flip one `0` to `1`** to maximize island size? (LeetCode 827 — Making A Large Island)
3. Can you return the **coordinates** of the largest island?
4. How would you solve it with **Union-Find**?
