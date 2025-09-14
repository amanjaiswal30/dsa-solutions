# Second-Largest Number in an Array

**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
Given an array of integers, find the **second-largest element** in the array.  
Return `Integer.MIN_VALUE` if the second largest does not exist.

---

## 🔹 Logic & Intuition
- The **largest number** is the maximum in the array.
- The **second largest** is the largest number smaller than the maximum.
- Special cases:
    - All elements are the same → no second largest.
    - Array has fewer than 2 elements → no second largest.

---

## 🔹 Approaches

### 1. Brute Force (Sorting)
- Sort the array in descending order.
- Traverse to find the first number smaller than the maximum.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(1) (in-place) or O(n) if using extra array.

---

### 2. Two-Pass Maximum Approach
- **First pass:** find the maximum element.
- **Second pass:** find the largest element smaller than the maximum.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

### 3. Single-Pass Optimal
- Maintain **two variables**: `first` (largest) and `second` (second largest).
- Traverse the array once:
    - If `num > first` → update `second = first`, `first = num`.
    - Else if `num < first` and `num > second` → update `second = num`.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecondLargestNumber {

    // -----------------------
    // 1. Brute Force (Sorting)
    // -----------------------
    public static int secondLargestSort(int[] nums) {
        int n = nums.length;
        if (n < 2) return Integer.MIN_VALUE;

        // Sort array in ascending order
        Arrays.sort(nums);

        // Traverse from second last element
        for (int i = n - 2; i >= 0; i--) {
            if (nums[i] < nums[n - 1]) return nums[i];
        }

        return Integer.MIN_VALUE;
    }

    // -----------------------
    // 2. Two-Pass Maximum Approach
    // -----------------------
    public static int secondLargestTwoPass(int[] nums) {
        int n = nums.length;
        if (n < 2) return Integer.MIN_VALUE;

        // First pass: find maximum
        int max = Integer.MIN_VALUE;
        for (int num : nums) {
            if (num > max) max = num;
        }

        // Second pass: find largest smaller than max
        int second = Integer.MIN_VALUE;
        for (int num : nums) {
            if (num < max && num > second) second = num;
        }

        return second;
    }

    // -----------------------
    // 3. Single-Pass Optimal
    // -----------------------
    public static int secondLargestSinglePass(int[] nums) {
        int first = Integer.MIN_VALUE;
        int second = Integer.MIN_VALUE;

        for (int num : nums) {
            if (num > first) {
                second = first;  // update second largest
                first = num;     // update largest
            } else if (num < first && num > second) {
                second = num;    // update second largest
            }
        }

        return second;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach              | Time Complexity | Space Complexity |
|-----------------------|-----------------|------------------|
| Brute Force (Sorting) | O(n log n)      | O(1)             |
| Two-Pass Maximum      | O(n)            | O(1)             |
| Single-Pass Optimal   | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Array has **all identical elements** → no second largest (`Integer.MIN_VALUE`).
- Array has **only one element** → no second largest.
- Array with **negative numbers** → works correctly.
- Array with **duplicates** → correctly identifies the second largest distinct number.

---

## 🔹 Examples

**Example 1:**  
Input: `[3, 5, 2, 4, 5]`  
Output: `4`

**Example 2:**  
Input: `[10]`  
Output: `Integer.MIN_VALUE`

**Example 3:**  
Input: `[7, 7, 7]`  
Output: `Integer.MIN_VALUE`

**Example 4:**  
Input: `[-3, -1, -2]`  
Output: `-2`

---

## 🔹 Follow-Up Questions
1. How would you find the **k-th largest element** efficiently?  
   → Use **Min Heap** of size k or **QuickSelect** algorithm.

2. Can this be done in **one traversal** without using extra space?  
   → Yes, see **Single-Pass Optimal** solution.

3. How would you handle **arrays with very large size**?  
   → Single-pass approach is best for O(n) time and O(1) space.
