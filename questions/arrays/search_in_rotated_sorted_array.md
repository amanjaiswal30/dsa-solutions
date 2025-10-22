# 🔹 Problem: Search in Rotated Sorted Array

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array `nums` sorted in **ascending order** but **rotated at some pivot unknown to you**, and a `target` value, return its **index** if found, otherwise `-1`.

- You must write an algorithm with **O(log n)** runtime complexity.
- Example of rotation: `[0,1,2,4,5,6,7]` rotated at pivot 3 → `[4,5,6,7,0,1,2]`.

---

## 🔹 Intuition
- A rotated sorted array is composed of **two sorted subarrays**.
- Use **modified binary search**:
    1. Check if **mid element is target**.
    2. Determine which half (left or right) is sorted.
    3. If target lies within the sorted half, continue search there.
    4. Otherwise, search in the other half.

---

## 🔹 Approaches

### 1. Modified Binary Search
- Iteratively perform binary search with extra checks:
    - If `nums[left] <= nums[mid]` → left half sorted.
    - Else → right half sorted.
- Compare target with the sorted half to decide the next range.

**Time Complexity:** O(log n)  
**Space Complexity:** O(1)

### 2. Alternative Approach (Pivot + Binary Search)
- First, find **pivot (smallest element index)** in O(log n).
- Then, decide which subarray (left or right of pivot) to search using standard binary search.
- Also O(log n), slightly more steps.

---

## 🔹 Java Code (Modified Binary Search)

```java
public class SearchRotatedSortedArray {

    public static int search(int[] nums, int target) {
        if (nums == null || nums.length == 0) return -1;

        int left = 0, right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] == target) return mid;

            // Left half is sorted
            if (nums[left] <= nums[mid]) {
                if (nums[left] <= target && target < nums[mid]) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } 
            // Right half is sorted
            else {
                if (nums[mid] < target && target <= nums[right]) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }

        return -1;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                     | Time Complexity | Space Complexity | Remarks |
|-------------------------------|----------------|-----------------|---------|
| Modified Binary Search        | O(log n)       | O(1)            | Classic approach |
| Pivot + Standard Binary Search| O(log n)       | O(1)            | Two-step binary search |

---

## 🔹 Edge Cases
- **Empty array** → return `-1`
- **Single element array** → check if it equals target
- **No rotation (normal sorted array)** → works as normal binary search
- **All elements identical except target** → ensures binary search checks correctly
- **Target not present** → return `-1`

---

## 🔹 Follow-Up Questions
1. Can you implement **recursive version** of modified binary search?
2. How to handle **duplicates** in rotated sorted array?
3. Can you **find the number of rotations** from array first element?
4. How to find **minimum element** efficiently?
5. Can you adapt the solution for **searching in descending rotated sorted array**?
