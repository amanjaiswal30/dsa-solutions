# Kth Largest Element in an Array

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an integer array `nums` and an integer `k`, return the **kth largest element** in the array.  
Note that it is the **kth largest element in sorted order**, not the kth distinct element.

## 🔹 Examples

**Example 1:**  
Input: `nums = [3,2,1,5,6,4]`, `k = 2`  
Output: `5`  
Explanation: The 2nd largest element is 5.

**Example 2:**  
Input: `nums = [3,2,3,1,2,4,5,5,6]`, `k = 4`  
Output: `4`  
Explanation: The 4th largest element is 4.

---

## 🔹 Constraints
- `1 <= k <= nums.length <= 10^5`
- `-10^4 <= nums[i] <= 10^4`

---

## 🔹 Intuition & Logic
- Sorting is straightforward → kth largest = `nums[n-k]`.
- Use a **min-heap of size k** to optimize for large arrays.
- QuickSelect (partition-based) can find kth largest in average O(n) time.

---

## 🔹 Approaches

### 1. Sorting
- Sort array in ascending order.
- Return `nums[nums.length - k]`.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(1) or O(n) depending on sort implementation

---

### 2. Min-Heap (Optimal for streaming or large data)
- Maintain a **min-heap of size k**.
- Iterate through elements:
    1. Add element to heap.
    2. If heap size > k → remove min element.
- Top of heap after iteration = kth largest.

**Time Complexity:** O(n log k)  
**Space Complexity:** O(k)

---

### 3. QuickSelect (Average O(n))
- Use partition similar to QuickSort.
- Find the position `pos = n - k` in sorted order.
- Recurse on left/right partition depending on pivot.

**Time Complexity:** O(n) average, O(n²) worst-case  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.*;

public class KthLargestElement {

    // Min-Heap Approach
    public static int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int num : nums) {
            minHeap.offer(num);
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.peek();
    }

    public static void main(String[] args) {
        int[] nums1 = {3,2,1,5,6,4};
        int k1 = 2;
        System.out.println(findKthLargest(nums1, k1)); // 5

        int[] nums2 = {3,2,3,1,2,4,5,5,6};
        int k2 = 4;
        System.out.println(findKthLargest(nums2, k2)); // 4
    }
}
```
---

## 🔹 Complexity Analysis

| Approach         | Time Complexity | Space Complexity |
|-----------------|----------------|-----------------|
| Sorting          | O(n log n)     | O(1) / O(n)     |
| Min-Heap         | O(n log k)     | O(k)            |
| QuickSelect      | O(n) avg       | O(1)            |

---

## 🔹 Edge Cases
- `k = 1` → largest element
- `k = nums.length` → smallest element
- Array with duplicates → handled naturally
- Single element array → return that element

---

## 🔹 Follow-Up Questions
1. How would you **find kth smallest element** instead?
2. Can you handle a **stream of numbers**, finding kth largest efficiently?
3. How would you modify QuickSelect to **avoid worst-case O(n²)**?  
