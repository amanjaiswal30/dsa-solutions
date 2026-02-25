# 🔹 Problem: Search in a 2D Matrix

![](../../assets/images/arrays/search_in_2d_matrix.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an `m x n` matrix with the following properties:

1. Each row is sorted in ascending order from left to right.
2. The first integer of each row is greater than the last integer of the previous row.

Write an efficient algorithm to **search for a target value** in the matrix.  
Return `true` if the target exists, otherwise `false`.

---

## 🔹 Intuition
- The matrix can be treated as a **flattened sorted array** of size `m * n`.
- Use **binary search** on this virtual array:
    - `midIndex = mid / n` → row
    - `mid % n` → column
- Alternatively, **start from top-right corner** and move left/down based on comparison:
    - If current > target → move left
    - If current < target → move down

---

## 🔹 Approaches

### 1. Binary Search on Flattened Matrix
- Treat matrix as 1D array of length `m*n`.
- Map `index` → `matrix[index / n][index % n]`.
- Perform standard binary search.

**Time Complexity:** O(log (m*n)) = O(log m + log n)  
**Space Complexity:** O(1)

### 2. Top-Right Corner Approach
- Start at `matrix[0][n-1]`.
- Move left if greater than target, move down if less than target.
- Stop when found or out of bounds.

**Time Complexity:** O(m + n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code (Top-Right Approach)

```java
public class Search2DMatrix {

    public static boolean searchMatrix(int[][] matrix, int target) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) return false;

        int m = matrix.length;
        int n = matrix[0].length;

        int row = 0, col = n - 1;

        while (row < m && col >= 0) {
            if (matrix[row][col] == target) {
                return true;
            } else if (matrix[row][col] > target) {
                col--;
            } else {
                row++;
            }
        }

        return false;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                  | Time Complexity | Space Complexity | Remarks |
|----------------------------|----------------|-----------------|---------|
| Binary Search Flattened    | O(log (m*n))   | O(1)            | Treat as 1D array |
| Top-Right Corner           | O(m + n)       | O(1)            | Often simpler in practice |

---

## 🔹 Edge Cases
- **Empty matrix** → return `false`
- **Single row/column** → handled by algorithm
- **Target smaller than smallest** → return `false`
- **Target larger than largest** → return `false`
- **All elements equal** → works correctly

---

## 🔹 Follow-Up Questions
1. Can you implement **binary search row first, then column**?
2. What if **rows are not sorted** but columns are?
3. How would this change for **matrix with duplicates**?
4. Can you adapt to **search in 3D matrix** efficiently?
5. How to **find all occurrences** of the target?
