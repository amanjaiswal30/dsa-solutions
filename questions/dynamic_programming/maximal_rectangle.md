# Maximal Rectangle – Detailed Explanation

**Difficulty:** Hard 🔥

---

## 🔹 Problem Statement

Given a `rows x cols` binary matrix filled with `0's` and `1's`, find the largest rectangle containing only `1's` and return its area.

---

## 🔹 Examples

**Example 1:**  
Input:
```
matrix = [
  ["1","0","1","0","0"],
  ["1","0","1","1","1"],
  ["1","1","1","1","1"],
  ["1","0","0","1","0"]
]
```
Output: `6`  
Explanation: Maximum rectangle is highlighted, area = 6

**Example 2:**  
Input: `matrix = [["0"]]`  
Output: `0`  

**Example 3:**  
Input: `matrix = [["1"]]`  
Output: `1`  

---

## 🔹 Core Intuition

**Key Insight:** Reduce to "Largest Rectangle in Histogram" problem.

For each row:
1. Build histogram (heights of consecutive 1's)
2. Find max rectangle in histogram
3. Track overall maximum

---

## 1️⃣ Using Largest Rectangle in Histogram ⭐ OPTIMAL

### Code (Java)

```java
public class MaximalRectangle {
    
    public int maximalRectangle(char[][] matrix) {
        if (matrix == null || matrix.length == 0) return 0;
        
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[] heights = new int[cols];
        int maxArea = 0;
        
        for (int i = 0; i < rows; i++) {
            // Build histogram for current row
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == '1') {
                    heights[j]++;
                } else {
                    heights[j] = 0;
                }
            }
            
            // Find max rectangle in histogram
            int area = largestRectangleHistogram(heights);
            maxArea = Math.max(maxArea, area);
        }
        
        return maxArea;
    }
    
    private int largestRectangleHistogram(int[] heights) {
        Stack<Integer> stack = new Stack<>();
        int maxArea = 0;
        int n = heights.length;
        
        for (int i = 0; i <= n; i++) {
            int h = (i == n) ? 0 : heights[i];
            
            while (!stack.isEmpty() && h < heights[stack.peek()]) {
                int height = heights[stack.pop()];
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, height * width);
            }
            
            stack.push(i);
        }
        
        return maxArea;
    }
}
```

### Example Walkthrough

Matrix:
```
["1","0","1","0","0"]
["1","0","1","1","1"]
["1","1","1","1","1"]
["1","0","0","1","0"]
```

Heights per row:
- Row 0: [1, 0, 1, 0, 0] → maxArea = 1
- Row 1: [2, 0, 2, 1, 1] → maxArea = 2
- Row 2: [3, 1, 3, 2, 2] → maxArea = **6**
- Row 3: [4, 0, 0, 3, 0] → maxArea = 6

**Answer:** **6**

### Complexity
- **Time:** O(rows × cols)
- **Space:** O(cols)

---

## 2️⃣ Dynamic Programming Approach

### Code (Java)

```java
public class MaximalRectangle {
    
    public int maximalRectangleDP(char[][] matrix) {
        if (matrix == null || matrix.length == 0) return 0;
        
        int rows = matrix.length;
        int cols = matrix[0].length;
        
        int[] left = new int[cols];
        int[] right = new int[cols];
        int[] height = new int[cols];
        
        Arrays.fill(right, cols);
        
        int maxArea = 0;
        
        for (int i = 0; i < rows; i++) {
            int currLeft = 0, currRight = cols;
            
            // Update height
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == '1') {
                    height[j]++;
                } else {
                    height[j] = 0;
                }
            }
            
            // Update left
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == '1') {
                    left[j] = Math.max(left[j], currLeft);
                } else {
                    left[j] = 0;
                    currLeft = j + 1;
                }
            }
            
            // Update right
            for (int j = cols - 1; j >= 0; j--) {
                if (matrix[i][j] == '1') {
                    right[j] = Math.min(right[j], currRight);
                } else {
                    right[j] = cols;
                    currRight = j;
                }
            }
            
            // Calculate area
            for (int j = 0; j < cols; j++) {
                maxArea = Math.max(maxArea, (right[j] - left[j]) * height[j]);
            }
        }
        
        return maxArea;
    }
}
```

---

## 🔄 Variations

### Variation 1: Count Square Submatrices

```java
public int countSquares(int[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    int count = 0;
    
    int[][] dp = new int[rows][cols];
    
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (matrix[i][j] == 1) {
                if (i == 0 || j == 0) {
                    dp[i][j] = 1;
                } else {
                    dp[i][j] = Math.min(dp[i-1][j], 
                                Math.min(dp[i][j-1], dp[i-1][j-1])) + 1;
                }
                count += dp[i][j];
            }
        }
    }
    
    return count;
}
```

### Variation 2: Maximal Square

```java
public int maximalSquare(char[][] matrix) {
    if (matrix == null || matrix.length == 0) return 0;
    
    int rows = matrix.length;
    int cols = matrix[0].length;
    int maxSide = 0;
    
    int[][] dp = new int[rows + 1][cols + 1];
    
    for (int i = 1; i <= rows; i++) {
        for (int j = 1; j <= cols; j++) {
            if (matrix[i-1][j-1] == '1') {
                dp[i][j] = Math.min(dp[i-1][j], 
                           Math.min(dp[i][j-1], dp[i-1][j-1])) + 1;
                maxSide = Math.max(maxSide, dp[i][j]);
            }
        }
    }
    
    return maxSide * maxSide;
}
```

### Variation 3: Largest Plus Sign

```java
public int orderOfLargestPlusSign(int n, int[][] mines) {
    int[][] grid = new int[n][n];
    
    for (int[] row : grid) {
        Arrays.fill(row, n);
    }
    
    for (int[] mine : mines) {
        grid[mine[0]][mine[1]] = 0;
    }
    
    for (int i = 0; i < n; i++) {
        int left = 0, right = 0, up = 0, down = 0;
        
        for (int j = 0, k = n - 1; j < n; j++, k--) {
            // Left
            left = (grid[i][j] == 0) ? 0 : left + 1;
            grid[i][j] = Math.min(grid[i][j], left);
            
            // Right
            right = (grid[i][k] == 0) ? 0 : right + 1;
            grid[i][k] = Math.min(grid[i][k], right);
            
            // Up
            up = (grid[j][i] == 0) ? 0 : up + 1;
            grid[j][i] = Math.min(grid[j][i], up);
            
            // Down
            down = (grid[k][i] == 0) ? 0 : down + 1;
            grid[k][i] = Math.min(grid[k][i], down);
        }
    }
    
    int maxOrder = 0;
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            maxOrder = Math.max(maxOrder, grid[i][j]);
        }
    }
    
    return maxOrder;
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Histogram | O(rows×cols) | O(cols) ⭐ |
| DP Arrays | O(rows×cols) | O(cols) ⭐ |

---

## 🎯 Key Takeaways

1. **Reduce to histogram:** Build heights row by row
2. **Monotonic stack:** For histogram max rectangle
3. **Reset on 0:** Heights become 0
4. **Row-wise processing:** Process each row as histogram base
5. **Applications:** Image processing, VLSI design

**Combines multiple DP patterns!** 🚀
