# 🔹 Problem: Region in a Binary Matrix

**Difficulty:** Medium 🌿

---

## 🔹 Problem Statement
You are given a **binary matrix** of size `n x m` (containing only `0`s and `1`s).  
A **region** is defined as a group of `1`s connected **horizontally, vertically, or diagonally**.

You must determine the **number of distinct regions** and the **size of the largest region**.

---

## 🔹 Example

**Input:**

matrix = [

[0, 0, 1, 1],


[1, 1, 1, 0],

[0, 0, 1, 0],

[1, 0, 0, 0]

]


**Output:**

Number of Regions: 2

Largest Region Size: 6


**Explanation:**
- The first region contains 6 connected `1`s (top-left cluster).
- The second region contains a single `1` (bottom-left corner).

---

## 🔹 Intuition
This is a **connected component** problem in a 2D grid, similar to **Number of Islands** but with **8-directional connectivity** (including diagonals).

We can solve it using either:
- **DFS (Depth-First Search)**
- **BFS (Breadth-First Search)**

Each time we encounter a `1` that hasn’t been visited, we:
1. Start a DFS/BFS from that cell.
2. Mark all reachable `1`s as visited.
3. Count them as one region and track its size.

---

## 🔹 Approach

### DFS Approach
1. Iterate through every cell of the matrix.
2. When a `1` is found that hasn’t been visited:
    - Increment the region count.
    - Run a DFS to explore all 8 directions.
    - Track the size of the region.
3. Maintain a global maximum region size.

**Directions:**

dx = [-1, -1, -1, 0, 0, 1, 1, 1]

dy = [-1, 0, 1, -1, 1,-1, 0, 1]


**Time Complexity:** O(n × m)  
**Space Complexity:** O(n × m)

---

### BFS Approach
Same logic as DFS, but using a queue instead of recursion.  
Helps avoid stack overflow for large grids.

**Time Complexity:** O(n × m)  
**Space Complexity:** O(n × m)

---

## 🔹 Java Code (DFS and BFS)

```java
public class RegionInBinaryMatrix {

    private static final int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

    // 1. DFS Approach
    public static int[] countRegionsDFS(int[][] matrix) {
        int n = matrix.length, m = matrix[0].length;
        boolean[][] visited = new boolean[n][m];
        int regions = 0, maxSize = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (matrix[i][j] == 1 && !visited[i][j]) {
                    int size = dfs(matrix, visited, i, j, n, m);
                    maxSize = Math.max(maxSize, size);
                    regions++;
                }
            }
        }

        return new int[]{regions, maxSize};
    }

    private static int dfs(int[][] matrix, boolean[][] visited, int x, int y, int n, int m) {
        visited[x][y] = true;
        int size = 1;

        for (int dir = 0; dir < 8; dir++) {
            int nx = x + dx[dir];
            int ny = y + dy[dir];

            if (nx >= 0 && ny >= 0 && nx < n && ny < m &&
                matrix[nx][ny] == 1 && !visited[nx][ny]) {
                size += dfs(matrix, visited, nx, ny, n, m);
            }
        }
        return size;
    }

    // 2. BFS Approach
    public static int[] countRegionsBFS(int[][] matrix) {
        int n = matrix.length, m = matrix[0].length;
        boolean[][] visited = new boolean[n][m];
        int regions = 0, maxSize = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (matrix[i][j] == 1 && !visited[i][j]) {
                    regions++;
                    int size = bfs(matrix, visited, i, j, n, m);
                    maxSize = Math.max(maxSize, size);
                }
            }
        }

        return new int[]{regions, maxSize};
    }

    private static int bfs(int[][] matrix, boolean[][] visited, int x, int y, int n, int m) {
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{x, y});
        visited[x][y] = true;
        int size = 0;

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            size++;

            for (int dir = 0; dir < 8; dir++) {
                int nx = cell[0] + dx[dir];
                int ny = cell[1] + dy[dir];

                if (nx >= 0 && ny >= 0 && nx < n && ny < m &&
                    matrix[nx][ny] == 1 && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        return size;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|-----------|-----------------|------------------|
| DFS       | O(n × m)        | O(n × m)         |
| BFS       | O(n × m)        | O(n × m)         |

---

## 🔹 Edge Cases
- Matrix contains all `0`s → regions = 0, largest = 0
- Matrix contains all `1`s → one region, largest = n × m
- Single cell grid (1×1)
- Only diagonal connections → counts as one region (since diagonals are valid connections)
- Large matrices → prefer BFS to avoid stack overflow due to deep recursion

---

## 🔹 Follow-Up Questions
1. How would you handle **4-directional connectivity** instead of 8-directional?
2. How can you return **all region sizes** instead of just the largest?
3. Can this be optimized for **sparse matrices** using adjacency mapping?
4. How would you modify the algorithm if you had to find **regions above a given threshold** (e.g., connected cells > 5)?
