# Insert an Element in a Sorted Array

**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
Given a **sorted array** and a number `x`, insert `x` into the array such that it remains **sorted**.

---

## 🔹 Logic & Intuition
- Since the array is already sorted, find the **correct position** where `x` should be inserted.
- Shift all elements to the **right** from that position to make space.
- Insert `x` at the identified position.
- Return the updated array (or list).

---

## 🔹 Approaches

### 1. Brute Force
- Iterate over the array to find the first element **greater than `x`**.
- Shift elements to the right and insert `x`.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1) (if in-place)

### 2. Using Binary Search (Optimized for Position)
- Perform **binary search** to find the correct insertion index.
- Shift elements to the right and insert `x`.
- **Time Complexity:** O(log n + n) → O(n) due to shifting
- **Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.Arrays;

public class InsertInSortedArray {

    // Brute force / straightforward
    public static int[] insertElement(int[] arr, int x) {
        int n = arr.length;
        int[] result = new int[n + 1];
        int i = 0;

        // Find position to insert
        while (i < n && arr[i] < x) {
            result[i] = arr[i];
            i++;
        }

        // Insert x
        result[i] = x;

        // Copy remaining elements
        while (i < n) {
            result[i + 1] = arr[i];
            i++;
        }

        return result;
    }

    // Using binary search to find insert position
    public static int[] insertElementBinarySearch(int[] arr, int x) {
        int n = arr.length;
        int[] result = new int[n + 1];

        // Find insert index
        int left = 0, right = n - 1, pos = n;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= x) {
                pos = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        // Copy elements before position
        for (int i = 0; i < pos; i++) {
            result[i] = arr[i];
        }

        // Insert x
        result[pos] = x;

        // Copy remaining elements
        for (int i = pos; i < n; i++) {
            result[i + 1] = arr[i];
        }

        return result;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach              | Time Complexity | Space Complexity |
|-----------------------|-----------------|------------------|
| Brute Force           | O(n)            | O(n)             |
| Binary Search + Shift | O(n)            | O(n)             |

---

## 🔹 Edge Cases
- Array is empty → insert at index 0.
- `x` is smaller than all elements → insert at start.
- `x` is larger than all elements → insert at end.
- Array contains duplicates → insert before first element ≥ x.

---

## 🔹 Follow-Up Questions
1. Can this be done **in-place** without using extra array?  
   → Yes, if array has **extra space at the end**.
2. How would you **insert multiple elements** while keeping array sorted efficiently?  
   → Merge like in **merge sort**.
3. What if the array is **descending** instead of ascending?  
   → Adjust comparison accordingly.
