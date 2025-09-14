# Spiral Matrix

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an `m x n` matrix, return all elements of the matrix in spiral order.

---

## 🔹 Logic & Intuition
- Traverse the matrix **layer by layer** in a spiral order:
    - Left ➝ Right (top row)
    - Top ➝ Bottom (right column)
    - Right ➝ Left (bottom row)
    - Bottom ➝ Top (left column)
- After each direction, update the respective **boundary** (`top`, `bottom`, `left`, `right`).

---

## 🔹 Approaches

### 1. Brute Force (Visited Matrix)
- Maintain a **boolean visited matrix** of size `m x n`.
- At each step, add the current element to result and mark it visited.
- Move in the current direction; change direction when hitting a boundary or visited cell.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n)

### 2. Optimized Boundary Tracking (Optimal)
- Maintain **four pointers**: `top`, `bottom`, `left`, `right`.
- Traverse boundaries in order and shrink them after each pass.
- Avoids extra space usage.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.ArrayList;
import java.util.List;

public class SpiralMatrix {

    // Brute Force using visited matrix
    public static List<Integer> spiralOrderBrute(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0) return result;

        int m = matrix.length, n = matrix[0].length;
        boolean[][] visited = new boolean[m][n];

        int[] dr = {0, 1, 0, -1}; // direction row: right, down, left, up
        int[] dc = {1, 0, -1, 0}; // direction col: right, down, left, up
        int r = 0, c = 0, dir = 0;

        for (int i = 0; i < m * n; i++) {
            result.add(matrix[r][c]);
            visited[r][c] = true;
            int nr = r + dr[dir];
            int nc = c + dc[dir];

            if (nr >= 0 && nr < m && nc >= 0 && nc < n && !visited[nr][nc]) {
                r = nr;
                c = nc;
            } else {
                dir = (dir + 1) % 4; // change direction
                r += dr[dir];
                c += dc[dir];
            }
        }

        return result;
    }

    // Optimized approach using boundary tracking
    public static List<Integer> spiralOrderOptimized(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0) return result;

        int top = 0, bottom = matrix.length - 1;
        int left = 0, right = matrix[0].length - 1;

        while (top <= bottom && left <= right) {
            // Left -> Right
            for (int col = left; col <= right; col++) result.add(matrix[top][col]);
            top++;

            // Top -> Bottom
            for (int row = top; row <= bottom; row++) result.add(matrix[row][right]);
            right--;

            // Right -> Left
            if (top <= bottom) {
                for (int col = right; col >= left; col--) result.add(matrix[bottom][col]);
                bottom--;
            }

            // Bottom -> Top
            if (left <= right) {
                for (int row = bottom; row >= top; row--) result.add(matrix[row][left]);
                left++;
            }
        }

        return result;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                    | Time Complexity | Space Complexity |
|-----------------------------|-----------------|------------------|
| Brute Force (Visited)       | O(m × n)        | O(m × n)         |
| Optimized Boundary Tracking | O(m × n)        | O(1)             |

---

## 🔹 Edge Cases
- Empty matrix `[]`.
- Single row matrix.
- Single column matrix.
- Square vs. rectangular matrices.

---

## 🔹 Follow-Up Questions
1. How would you extend this to traverse in **reverse spiral order**?
2. How would you solve it if the matrix was **infinite stream-like** (not fully stored in memory)?

---
