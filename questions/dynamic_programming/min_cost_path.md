# Minimum Cost Path in Grid – Detailed Explanation

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given a `m x n` grid filled with non-negative numbers, find a path from top-left to bottom-right, which minimizes the sum of all numbers along its path.

**Note:** You can only move either **down** or **right** at any point in time.

---

## 🔹 Examples

**Example 1:**  
Input:
```
grid = [
  [1,3,1],
  [1,5,1],
  [4,2,1]
]
```
Output: `7`  
Explanation: Path 1→3→1→1→1 minimizes the sum

**Example 2:**  
Input:
```
grid = [
  [1,2,3],
  [4,5,6]
]
```
Output: `12`  
Explanation: Path 1→2→3→6

**Example 3:**  
Input: `grid = [[1]]`  
Output: `1`

---

## 🔹 Core Intuition

**Recurrence:**
```
dp[i][j] = minimum cost to reach cell (i,j)

dp[i][j] = grid[i][j] + min(dp[i-1][j], dp[i][j-1])
```

**Base Case:**
```
dp[0][0] = grid[0][0]
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class MinCostPath {
    
    public int minPathSumRecursive(int[][] grid) {
        return minCost(grid, grid.length - 1, grid[0].length - 1);
    }
    
    private int minCost(int[][] grid, int i, int j) {
        if (i == 0 && j == 0) {
            return grid[0][0];
        }
        
        if (i < 0 || j < 0) {
            return Integer.MAX_VALUE;
        }
        
        return grid[i][j] + Math.min(
            minCost(grid, i - 1, j),
            minCost(grid, i, j - 1)
        );
    }
}
```

### Complexity
- **Time:** O(2^(m+n)) — exponential
- **Space:** O(m + n) — recursion depth

---

## 2️⃣ Memoization (Top-Down DP)

### Code (Java)

```java
public class MinCostPath {
    
    public int minPathSumMemo(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int[][] memo = new int[m][n];
        
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        
        return minCostMemo(grid, m - 1, n - 1, memo);
    }
    
    private int minCostMemo(int[][] grid, int i, int j, int[][] memo) {
        if (i == 0 && j == 0) {
            return grid[0][0];
        }
        
        if (i < 0 || j < 0) {
            return Integer.MAX_VALUE;
        }
        
        if (memo[i][j] != -1) {
            return memo[i][j];
        }
        
        memo[i][j] = grid[i][j] + Math.min(
            minCostMemo(grid, i - 1, j, memo),
            minCostMemo(grid, i, j - 1, memo)
        );
        
        return memo[i][j];
    }
}
```

---

## 3️⃣ Dynamic Programming – Bottom-Up

### Code (Java)

```java
public class MinCostPath {
    
    public int minPathSumDP(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int[][] dp = new int[m][n];
        
        dp[0][0] = grid[0][0];
        
        // Fill first row
        for (int j = 1; j < n; j++) {
            dp[0][j] = dp[0][j - 1] + grid[0][j];
        }
        
        // Fill first column
        for (int i = 1; i < m; i++) {
            dp[i][0] = dp[i - 1][0] + grid[i][0];
        }
        
        // Fill rest of table
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = grid[i][j] + Math.min(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        
        return dp[m - 1][n - 1];
    }
}
```

### DP Table Example

For grid:
```
[1,3,1]
[1,5,1]
[4,2,1]
```

DP table:
```
[1, 4, 5]
[2, 7, 6]
[6, 8, 7]
```

**Answer:** dp[2][2] = **7**

---

## 4️⃣ Space Optimized – O(n) ⭐ OPTIMAL

### Code (Java)

```java
public class MinCostPath {
    
    public int minPathSum(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int[] dp = new int[n];
        
        dp[0] = grid[0][0];
        
        // Initialize first row
        for (int j = 1; j < n; j++) {
            dp[j] = dp[j - 1] + grid[0][j];
        }
        
        // Process remaining rows
        for (int i = 1; i < m; i++) {
            dp[0] += grid[i][0];
            
            for (int j = 1; j < n; j++) {
                dp[j] = grid[i][j] + Math.min(dp[j], dp[j - 1]);
            }
        }
        
        return dp[n - 1];
    }
}
```

### Complexity
- **Time:** O(m × n)
- **Space:** O(n) ⭐

---

## 🔄 Variations

### Variation 1: Min Cost with 4 Directions

**Problem:** Can move in all 4 directions (up, down, left, right).

```java
public class MinCostPathVariations {
    
    private int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
    
    public int minCostPath4Directions(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        int[][] dist = new int[m][n];
        
        for (int[] row : dist) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        
        pq.offer(new int[]{grid[0][0], 0, 0});
        dist[0][0] = grid[0][0];
        
        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int cost = curr[0], i = curr[1], j = curr[2];
            
            if (i == m - 1 && j == n - 1) {
                return cost;
            }
            
            for (int[] dir : dirs) {
                int ni = i + dir[0];
                int nj = j + dir[1];
                
                if (ni >= 0 && ni < m && nj >= 0 && nj < n) {
                    int newCost = cost + grid[ni][nj];
                    
                    if (newCost < dist[ni][nj]) {
                        dist[ni][nj] = newCost;
                        pq.offer(new int[]{newCost, ni, nj});
                    }
                }
            }
        }
        
        return -1;
    }
}
```

### Variation 2: Path with Maximum Gold

```java
public int getMaximumGold(int[][] grid) {
    int m = grid.length;
    int n = grid[0].length;
    int maxGold = 0;
    
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (grid[i][j] != 0) {
                maxGold = Math.max(maxGold, dfs(grid, i, j));
            }
        }
    }
    
    return maxGold;
}

private int dfs(int[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length || grid[i][j] == 0) {
        return 0;
    }
    
    int gold = grid[i][j];
    grid[i][j] = 0;  // Mark visited
    
    int maxPath = 0;
    maxPath = Math.max(maxPath, dfs(grid, i + 1, j));
    maxPath = Math.max(maxPath, dfs(grid, i - 1, j));
    maxPath = Math.max(maxPath, dfs(grid, i, j + 1));
    maxPath = Math.max(maxPath, dfs(grid, i, j - 1));
    
    grid[i][j] = gold;  // Backtrack
    
    return gold + maxPath;
}
```

### Variation 3: Unique Paths (Count Ways)

```java
public int uniquePaths(int m, int n) {
    int[] dp = new int[n];
    Arrays.fill(dp, 1);
    
    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            dp[j] += dp[j - 1];
        }
    }
    
    return dp[n - 1];
}
```

### Variation 4: Unique Paths with Obstacles

```java
public int uniquePathsWithObstacles(int[][] obstacleGrid) {
    int m = obstacleGrid.length;
    int n = obstacleGrid[0].length;
    
    if (obstacleGrid[0][0] == 1) return 0;
    
    int[] dp = new int[n];
    dp[0] = 1;
    
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (obstacleGrid[i][j] == 1) {
                dp[j] = 0;
            } else if (j > 0) {
                dp[j] += dp[j - 1];
            }
        }
    }
    
    return dp[n - 1];
}
```

### Variation 5: Dungeon Game

```java
public int calculateMinimumHP(int[][] dungeon) {
    int m = dungeon.length;
    int n = dungeon[0].length;
    int[][] dp = new int[m][n];
    
    dp[m - 1][n - 1] = Math.max(1, 1 - dungeon[m - 1][n - 1]);
    
    // Fill last row
    for (int j = n - 2; j >= 0; j--) {
        dp[m - 1][j] = Math.max(1, dp[m - 1][j + 1] - dungeon[m - 1][j]);
    }
    
    // Fill last column
    for (int i = m - 2; i >= 0; i--) {
        dp[i][n - 1] = Math.max(1, dp[i + 1][n - 1] - dungeon[i][n - 1]);
    }
    
    // Fill rest (bottom-up, right to left)
    for (int i = m - 2; i >= 0; i--) {
        for (int j = n - 2; j >= 0; j--) {
            int minHealthNeeded = Math.min(dp[i + 1][j], dp[i][j + 1]);
            dp[i][j] = Math.max(1, minHealthNeeded - dungeon[i][j]);
        }
    }
    
    return dp[0][0];
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^(m+n)) | O(m+n) |
| Memoization | O(m×n) | O(m×n) |
| DP 2D | O(m×n) | O(m×n) |
| Space Optimized | O(m×n) | O(n) ⭐ |

---

## 🎯 Key Takeaways

1. **Classic grid DP:** Build from top-left to bottom-right
2. **Two choices:** Come from top or left
3. **Space optimization:** Only need previous row
4. **Variations:** Count paths, obstacles, 4 directions
5. **Applications:** Robot navigation, game paths

**Essential grid DP pattern!** 🚀
