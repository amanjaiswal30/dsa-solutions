# Rotate Image

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
You are given an `n x n` 2D matrix representing an image.  
Rotate the image by **90 degrees clockwise**, **in-place**.

---

## 🔹 Logic & Intuition
- Rotating an image by 90° clockwise =
    1. **Transpose** the matrix (swap across diagonal).
    2. **Reverse each row**.

This avoids using extra space and works in-place.

---

## 🔹 Approaches

### 1. Brute Force (Using Extra Matrix)
- Create a new matrix of the same size.
- Place each element at its rotated position:  
  `result[j][n-1-i] = matrix[i][j]`
- Copy back into the original matrix.

**Time Complexity:** O(n²)  
**Space Complexity:** O(n²)

---

### 2. Optimal (In-place Transpose + Reverse)
- First transpose: swap `matrix[i][j]` with `matrix[j][i]`.
- Then reverse each row.

**Time Complexity:** O(n²)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
public class RotateImage {
    
    // Brute force approach
    public static void rotateBrute(int[][] matrix) {
        int n = matrix.length;
        int[][] result = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[j][n - 1 - i] = matrix[i][j];
            }
        }
        
        // Copy back into original
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = result[i][j];
            }
        }
    }

    // Optimal in-place rotation
    public static void rotateOptimal(int[][] matrix) {
        int n = matrix.length;

        // Step 1: Transpose
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }

        // Step 2: Reverse each row
        for (int i = 0; i < n; i++) {
            int left = 0, right = n - 1;
            while (left < right) {
                int temp = matrix[i][left];
                matrix[i][left] = matrix[i][right];
                matrix[i][right] = temp;
                left++;
                right--;
            }
        }
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                   | Time Complexity | Space Complexity |
|----------------------------|-----------------|------------------|
| Brute Force (Extra Matrix) | O(n²)           | O(n²)            |
| In-place (Optimal)         | O(n²)           | O(1)             |

---

## 🔹 Edge Cases
- `n = 1` → single element, no change.
- Even vs odd dimensions (works the same).
- Large matrices (must stay O(1) extra space).

---

## 🔹 Follow-Up Questions
1. How would you rotate the matrix **anticlockwise**?
2. How to rotate by **180° or 270°** efficiently?
3. Could you extend this to **non-square (m x n)** matrices?

