# 🔹 Problem: Top K Frequent Elements

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an integer array `nums` and an integer `k`, return the **k most frequent elements**.  
You may return the answer in **any order**.

**Example:**  
Input: `nums = [1,1,1,2,2,3]`, `k = 2`  
Output: `[1,2]`

Input: `nums = [1]`, `k = 1`  
Output: `[1]`

---

## 🔹 Intuition
- Count frequency of each element.
- Use a **heap** or **bucket sort** to efficiently find the top k elements.
- Max-heap keeps highest frequency at top.
- Bucket sort arranges elements by frequency.

---

## 🔹 Approaches

### 1. HashMap + Sorting
- Count frequency using `Map<Integer, Integer>`.
- Sort entries by frequency in descending order.
- Return first k keys.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(n)

---

### 2. Min-Heap of size k (Optimal for large n)
- Count frequency using `Map`.
- Maintain a **min-heap of size k** based on frequency.
- If heap size > k → remove min frequency.
- Heap top after processing → k most frequent elements.

**Time Complexity:** O(n log k)  
**Space Complexity:** O(n)

---

### 3. Bucket Sort (Optimal if k ≈ n)
- Count frequency.
- Create **buckets**: array of lists where index = frequency.
- Iterate buckets from high to low frequency, collect top k elements.

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

## 🔹 Java Code

```java
import java.util.*;

public class TopKFrequentElements {

    public static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int num : nums) freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);

        PriorityQueue<Integer> minHeap = new PriorityQueue<>(
            (a, b) -> freqMap.get(a) - freqMap.get(b)
        );

        for (int num : freqMap.keySet()) {
            minHeap.offer(num);
            if (minHeap.size() > k) minHeap.poll();
        }

        int[] result = new int[k];
        for (int i = k - 1; i >= 0; i--) result[i] = minHeap.poll();
        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {1,1,1,2,2,3};
        int k1 = 2;
        System.out.println(Arrays.toString(topKFrequent(nums1, k1))); // [1,2]

        int[] nums2 = {1};
        int k2 = 1;
        System.out.println(Arrays.toString(topKFrequent(nums2, k2))); // [1]
    }
}
```

---

## 🔹 Complexity Analysis

| Approach          | Time Complexity | Space Complexity |
|------------------|----------------|-----------------|
| HashMap + Sorting | O(n log n)     | O(n)            |
| Min-Heap          | O(n log k)     | O(n)            |
| Bucket Sort       | O(n)           | O(n)            |

---

## 🔹 Edge Cases
- `k = 1` → most frequent element
- Array with all elements unique → return any k elements
- Multiple elements with same frequency → any order is fine
- Single element array → return that element

---

## 🔹 Follow-Up Questions
1. How would you **return elements in exact frequency order**?
2. Can you solve it for a **stream of numbers** efficiently?
3. How would you modify Bucket Sort to **handle large frequency ranges**?
