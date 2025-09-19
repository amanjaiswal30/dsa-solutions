# 🔹 Trapping Rain Water

---

## 📌 Problem Statement
Given an array of heights representing elevation maps, compute how much water can be **trapped after raining**.

---

## 📊 Example Inputs & Outputs

**Input:** `[0,1,0,2,1,0,1,3,2,1,2,1]`  
**Output:** `6`

---

## 💻 Java Code (All Approaches)

```java
public class TrappingRainWater {

    // Approach 1: Brute Force
    public int trapBruteForce(int[] height) {
        int n = height.length;
        int totalWater = 0;

        for (int i = 0; i < n; i++) {
            int leftMax = 0, rightMax = 0;

            for (int j = 0; j <= i; j++) {
                leftMax = Math.max(leftMax, height[j]);
            }

            for (int j = i; j < n; j++) {
                rightMax = Math.max(rightMax, height[j]);
            }

            totalWater += Math.min(leftMax, rightMax) - height[i];
        }

        return totalWater;
    }

    // Approach 2: Prefix and Suffix Arrays
    public int trapWithArrays(int[] height) {
        int n = height.length;
        int[] leftMax = new int[n];
        int[] rightMax = new int[n];
        int totalWater = 0;

        leftMax[0] = height[0];
        for (int i = 1; i < n; i++) {
            leftMax[i] = Math.max(leftMax[i - 1], height[i]);
        }

        rightMax[n - 1] = height[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i + 1], height[i]);
        }

        for (int i = 0; i < n; i++) {
            totalWater += Math.min(leftMax[i], rightMax[i]) - height[i];
        }

        return totalWater;
    }

    // Approach 3: Two Pointers (Optimized)
    public int trapTwoPointers(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int totalWater = 0;

        while (left < right) {
            if (height[left] <= height[right]) {
                if (height[left] >= leftMax) {
                    leftMax = height[left];
                } else {
                    totalWater += leftMax - height[left];
                }
                left++;
            } else {
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    totalWater += rightMax - height[right];
                }
                right--;
            }
        }

        return totalWater;
    }
}
```
---

## 💡 Intuition Behind Each Approach

- **Brute Force:**  
  For each index, compute the **maximum height to the left and right**.  
  Water trapped = `min(leftMax, rightMax) - height[i]`.

- **Prefix and Suffix Arrays:**  
  Precompute `leftMax` and `rightMax` for each index to avoid recomputation in every iteration.

- **Two Pointers (Optimized):**  
  Use two pointers (`left` and `right`) moving toward the center, maintaining `leftMax` and `rightMax`.  
  Add water at each step based on the smaller of the two heights.

---

## 📊 Complexity Analysis

| Approach                 | Time Complexity | Space Complexity |
|--------------------------|-----------------|------------------|
| Brute Force              | O(n²)           | O(1)             |
| Prefix and Suffix Arrays | O(n)            | O(n)             |
| Two Pointers (Optimized) | O(n)            | O(1)             |

---

## 🔹 Edge Cases
1. **Empty array** → trapped water = 0.
2. **Single element or two elements** → trapped water = 0.
3. **All elements equal** → trapped water = 0.
4. **Strictly increasing or decreasing heights** → trapped water = 0.

---

## 🔹 Follow-Up Questions
- Can you compute **trapped water in 2D elevation maps**?
- How to modify for **streaming input** where heights arrive one by one?
- Can this be extended to **find water trapped between peaks efficiently** using stacks?

---
