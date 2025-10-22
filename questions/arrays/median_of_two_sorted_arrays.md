# 🔹 Problem: Median of Two Sorted Arrays

**Difficulty:** Hard ⚡⚡

---

## 🔹 Problem Statement
Given two **sorted arrays** `nums1` and `nums2` of size `m` and `n`, return the **median** of the two sorted arrays.

- Overall run time complexity must be **O(log (m+n))**.
- Median definition:
    - If total length odd → middle element
    - If total length even → average of two middle elements

---

## 🔹 Intuition
- **Naïve approach:** merge arrays → O(m+n) → return median.
- **Optimized approach:** use **binary search** on the smaller array to partition both arrays:
    1. Partition arrays such that **left halves** contain `k = (m+n+1)/2` elements.
    2. Ensure **all elements in left halves ≤ all elements in right halves**.
    3. Median = max(left halves) or average of max(left halves) and min(right halves) depending on total length odd/even.

---

## 🔹 Approaches

### 1. Binary Search Partition (Optimal)
- Binary search on smaller array for correct partition.
- Compare edges of partitions to ensure `maxLeftX <= minRightY` and `maxLeftY <= minRightX`.
- O(log min(m, n)) time, O(1) space.

### 2. Merge Arrays (Naïve)
- Merge both arrays into one sorted array.
- Return median.
- O(m+n) time, O(m+n) space.

---

## 🔹 Java Code (Binary Search Partition)

```java
public class MedianTwoSortedArrays {

    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if (nums1.length > nums2.length) {
            return findMedianSortedArrays(nums2, nums1); // ensure nums1 is smaller
        }

        int x = nums1.length;
        int y = nums2.length;
        int low = 0, high = x;

        while (low <= high) {
            int partitionX = (low + high) / 2;
            int partitionY = (x + y + 1) / 2 - partitionX;

            int maxLeftX = (partitionX == 0) ? Integer.MIN_VALUE : nums1[partitionX - 1];
            int minRightX = (partitionX == x) ? Integer.MAX_VALUE : nums1[partitionX];

            int maxLeftY = (partitionY == 0) ? Integer.MIN_VALUE : nums2[partitionY - 1];
            int minRightY = (partitionY == y) ? Integer.MAX_VALUE : nums2[partitionY];

            if (maxLeftX <= minRightY && maxLeftY <= minRightX) {
                if ((x + y) % 2 == 0) {
                    return ((double)Math.max(maxLeftX, maxLeftY) + Math.min(minRightX, minRightY)) / 2;
                } else {
                    return (double)Math.max(maxLeftX, maxLeftY);
                }
            } else if (maxLeftX > minRightY) {
                high = partitionX - 1;
            } else {
                low = partitionX + 1;
            }
        }

        throw new IllegalArgumentException("Input arrays are not sorted");
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                   | Time Complexity | Space Complexity | Remarks |
|-----------------------------|----------------|-----------------|---------|
| Binary Search Partition     | O(log min(m, n)) | O(1)           | Optimal |
| Merge Arrays                | O(m+n)          | O(m+n)         | Naïve, simpler to implement |

---

## 🔹 Edge Cases
- **Empty arrays** → handle as per spec
- **Arrays of unequal lengths** → works by always searching smaller array
- **All elements equal** → median same value
- **Single element arrays** → works correctly
- **Odd/even total length** → median computation differs

---

## 🔹 Follow-Up Questions
1. Can you implement **recursive version** of binary search partition?
2. How to **generalize to k-th smallest element**?
3. Can this approach handle **duplicates efficiently**?
4. How would you adapt if arrays were **descending sorted**?
5. Can you solve **without using MAX_VALUE/MIN_VALUE sentinels**?
