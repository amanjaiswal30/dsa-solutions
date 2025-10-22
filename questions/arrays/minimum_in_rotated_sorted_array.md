# 🔹 Problem: Find Minimum in Rotated Sorted Array

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array `nums` sorted in **ascending order** but **rotated at some pivot unknown to you**, return the **minimum element** in the array.

- You must write an algorithm with **O(log n)** runtime complexity.
- Example: `[4,5,6,7,0,1,2]` → minimum is `0`.

---

## 🔹 Intuition
- The array is rotated; thus it consists of **two sorted subarrays**.
- Use **modified binary search** to find the **inflection point** where rotation occurs:
    1. If the middle element is greater than the rightmost element, the minimum is in the **right half**.
    2. Else, minimum is in the **left half** (including mid).
- Repeat until the search space is one element → that is the minimum.

---

## 🔹 Approaches

### 1. Binary Search for Minimum
- Iteratively narrow down the range based on comparison with `right`:
    - If `nums[mid] > nums[right]` → minimum is right of mid.
    - Else → minimum is at mid or left of mid.

**Time Complexity:** O(log n)  
**Space Complexity:** O(1)

### 2. Linear Scan (Not Recommended)
- Traverse the array to find minimum.
- O(n), defeats the purpose of binary search.

---

## 🔹 Java Code (Binary Search)

```java
public class FindMinRotatedArray {

    public static int findMin(int[] nums) {
        if (nums == null || nums.length == 0) throw new IllegalArgumentException("Array is empty");

        int left = 0, right = nums.length - 1;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] > nums[right]) {
                // Minimum is in right half
                left = mid + 1;
            } else {
                // Minimum is at mid or left half
                right = mid;
            }
        }

        return nums[left];
    }
}
```
---

## 🔹 Complexity Analysis

| Approach               | Time Complexity | Space Complexity | Remarks |
|------------------------|----------------|-----------------|---------|
| Binary Search          | O(log n)       | O(1)            | Optimal |
| Linear Scan            | O(n)           | O(1)            | Not recommended |

---

## 🔹 Edge Cases
- **Empty array** → throw exception or return sentinel
- **Single element array** → return that element
- **No rotation (sorted array)** → first element is minimum
- **All elements identical** → works, returns first element
- **Array with two elements** → correctly returns min

---

## 🔹 Follow-Up Questions
1. Can you implement **recursive binary search** version?
2. How to handle **duplicates** in rotated sorted array?
3. Can you **find the rotation count** using minimum index?
4. Can this approach be adapted to **descending rotated array**?
5. How to **find maximum element** in rotated array efficiently?
