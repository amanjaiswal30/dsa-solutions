# Spiral Matrix

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
- Keep a boolean visited matrix.
- At each step, mark the cell as visited and move in the spiral direction.
- Turn direction when you hit a boundary or a visited cell.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(m × n)

---

### 2. Optimized Boundary Tracking (Optimal)
- Maintain **four pointers**: `top`, `bottom`, `left`, `right`.
- Traverse boundaries and shrink them after each pass.
- Avoids extra space usage.

**Time Complexity:** O(m × n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.ArrayList;
import java.util.List;

public class SpiralMatrix {

    public static List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0) return result;

        int top = 0, bottom = matrix.length - 1;
        int left = 0, right = matrix[0].length - 1;

        while (top <= bottom && left <= right) {
            // Traverse from left to right
            for (int col = left; col <= right; col++) {
                result.add(matrix[top][col]);
            }
            top++;

            // Traverse downwards
            for (int row = top; row <= bottom; row++) {
                result.add(matrix[row][right]);
            }
            right--;

            if (top <= bottom) {
                // Traverse from right to left
                for (int col = right; col >= left; col--) {
                    result.add(matrix[bottom][col]);
                }
                bottom--;
            }

            if (left <= right) {
                // Traverse upwards
                for (int row = bottom; row >= top; row--) {
                    result.add(matrix[row][left]);
                }
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
