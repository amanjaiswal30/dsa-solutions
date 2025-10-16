# 🔹 Problem: Merge K Sorted Arrays

**Difficulty:** Hard ⚡

---

## 🔹 Problem Statement
Given `k` sorted integer arrays, merge them into a **single sorted array**.

**Example:**  
Input: `arrays = [[1,4,5],[1,3,4],[2,6]]`  
Output: `[1,1,2,3,4,4,5,6]`

Explanation:
- Merge all arrays while maintaining sorted order.

---

## 🔹 Intuition
- Use a **min-heap (priority queue)** to always pick the **smallest current element** from the k arrays.
- Keep track of **array index and element index** in the heap.
- Pop from heap → add next element from the same array → repeat until heap is empty.

---

## 🔹 Approaches

### 1. Flatten + Sort
- Combine all arrays into a single array.
- Sort the combined array.

**Time Complexity:** O(N log N) → N = total number of elements  
**Space Complexity:** O(N)

---

### 2. Min-Heap (Optimal)
- Push the first element of each array into a min-heap along with its array index and element index.
- While heap is not empty:
    1. Pop the smallest element.
    2. Add it to result.
    3. If the array has more elements, push the next element into the heap.

**Time Complexity:** O(N log k) → N = total elements, k = number of arrays  
**Space Complexity:** O(k) → heap size

---

### 3. Divide & Conquer
- Pairwise merge arrays using **merge two sorted arrays** recursively.
- Similar to merge sort approach.

**Time Complexity:** O(N log k)  
**Space Complexity:** O(N) → for merged arrays

---

## 🔹 Java Code

```java
import java.util.*;

public class MergeKSortedArrays {

    static class Element {
        int value, arrayIndex, elementIndex;
        public Element(int value, int arrayIndex, int elementIndex) {
            this.value = value;
            this.arrayIndex = arrayIndex;
            this.elementIndex = elementIndex;
        }
    }

    public static List<Integer> mergeKSortedArrays(List<List<Integer>> arrays) {
        PriorityQueue<Element> minHeap = new PriorityQueue<>(Comparator.comparingInt(e -> e.value));
        List<Integer> result = new ArrayList<>();

        // Add first element of each array to heap
        for (int i = 0; i < arrays.size(); i++) {
            if (!arrays.get(i).isEmpty()) {
                minHeap.offer(new Element(arrays.get(i).get(0), i, 0));
            }
        }

        while (!minHeap.isEmpty()) {
            Element curr = minHeap.poll();
            result.add(curr.value);

            int nextIndex = curr.elementIndex + 1;
            if (nextIndex < arrays.get(curr.arrayIndex).size()) {
                minHeap.offer(new Element(arrays.get(curr.arrayIndex).get(nextIndex), curr.arrayIndex, nextIndex));
            }
        }

        return result;
    }

    public static void main(String[] args) {
        List<List<Integer>> arrays = Arrays.asList(
            Arrays.asList(1,4,5),
            Arrays.asList(1,3,4),
            Arrays.asList(2,6)
        );

        System.out.println(mergeKSortedArrays(arrays)); // [1,1,2,3,4,4,5,6]
    }
}
```
---

## 🔹 Complexity Analysis

| Approach           | Time Complexity | Space Complexity |
|-------------------|----------------|-----------------|
| Flatten + Sort     | O(N log N)     | O(N)            |
| Min-Heap           | O(N log k)     | O(k)            |
| Divide & Conquer   | O(N log k)     | O(N)            |

---

## 🔹 Edge Cases
- Empty arrays → return `[]`
- Arrays of different lengths → handled naturally
- Single array → return that array
- All arrays empty → return `[]`

---

## 🔹 Follow-Up Questions
1. How would you merge **K sorted linked lists** instead of arrays?
2. Can you merge **in-place** if arrays are stored consecutively?
3. How would you optimize for **streaming arrays** arriving one by one?
