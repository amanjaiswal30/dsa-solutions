# 🔹 Merge K Sorted Linked Lists

---

## 📌 Problem Statement
Given **K sorted linked lists**, merge them into **one sorted linked list**.

**Example:**  
Input:  
List 1: 1 → 4 → 5  
List 2: 1 → 3 → 4  
List 3: 2 → 6

Output:  
1 → 1 → 2 → 3 → 4 → 4 → 5 → 6

---

## 💡 Logic & Intuition
- We need to combine **multiple sorted lists** into a single sorted list.
- Approaches vary from **brute force collection** to **optimized heap or divide-and-conquer methods**.
- The goal is to maintain **sorted order** efficiently.

---

## 🔹 Approaches

### 1. Brute Force (Collect and Sort)
- Extract all elements from all lists into one array or list.
- Sort the array.
- Build a new linked list from the sorted array.

**Time Complexity:** O(N log N), where N is total number of nodes  
**Space Complexity:** O(N) for storing values

---

### 2. Sequential Merge (Merge Two Lists Repeatedly)
- Start with the first list.
- Merge it with the second list to get a sorted list.
- Merge the result with the third list, and so on.

**Time Complexity:** O(kN), where k = number of lists, N = total nodes  
**Space Complexity:** O(1)

---

### 3. Min Heap / Priority Queue (Optimized)
- Use a min-heap to keep track of the smallest current nodes from each list.
- Extract the smallest node and add its next node to the heap.
- Continue until the heap is empty.

**Time Complexity:** O(N log k)  
**Space Complexity:** O(k) for the heap

---

### 4. Divide and Conquer
- Pair lists and merge each pair, reducing the total number of lists by half each iteration.
- Repeat until only one list remains.

**Time Complexity:** O(N log k)  
**Space Complexity:** O(1)

---

## 💻 Java Code

```java
import java.util.*;

public class MergeKSortedLists {

    // Definition for singly-linked list
    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    // Approach 1: Brute Force
    public static ListNode mergeKListsBruteForce(ListNode[] lists) {
        List<Integer> values = new ArrayList<>();
        for (ListNode head : lists) {
            while (head != null) {
                values.add(head.val);
                head = head.next;
            }
        }
        Collections.sort(values);

        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }
        return dummy.next;
    }

    // Helper to merge two sorted lists
    public static ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                current.next = l1;
                l1 = l1.next;
            } else {
                current.next = l2;
                l2 = l2.next;
            }
            current = current.next;
        }
        current.next = (l1 != null) ? l1 : l2;
        return dummy.next;
    }

    // Approach 2: Sequential Merge
    public static ListNode mergeKListsSequential(ListNode[] lists) {
        if (lists.length == 0) return null;
        ListNode merged = lists[0];
        for (int i = 1; i < lists.length; i++) {
            merged = mergeTwoLists(merged, lists[i]);
        }
        return merged;
    }

    // Approach 3: Min Heap / Priority Queue
    public static ListNode mergeKListsHeap(ListNode[] lists) {
        PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
        for (ListNode node : lists) if (node != null) pq.add(node);

        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        while (!pq.isEmpty()) {
            ListNode smallest = pq.poll();
            current.next = smallest;
            current = current.next;
            if (smallest.next != null) pq.add(smallest.next);
        }
        return dummy.next;
    }

    // Approach 4: Divide and Conquer
    public static ListNode mergeKListsDivideAndConquer(ListNode[] lists) {
        if (lists == null || lists.length == 0) return null;
        int interval = 1, n = lists.length;

        while (interval < n) {
            for (int i = 0; i + interval < n; i += interval * 2) {
                lists[i] = mergeTwoLists(lists[i], lists[i + interval]);
            }
            interval *= 2;
        }
        return lists[0];
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                     | Time Complexity | Space Complexity |
|------------------------------|-----------------|------------------|
| Brute Force (Collect & Sort) | O(N log N)      | O(N)             |
| Sequential Merge             | O(kN)           | O(1)             |
| Min Heap / Priority Queue    | O(N log k)      | O(k)             |
| Divide and Conquer           | O(N log k)      | O(1)             |

---

## 🔹 Edge Cases
- Input array is empty → return `null`.
- Some lists are `null`.
- All lists are empty.
- Single list only → return that list.
- Lists contain duplicate values.

---

## 🔹 Follow-Up Questions
1. Can you **merge lists in-place** without creating new nodes?
2. How would you **merge streams of sorted data** instead of lists in memory?
3. Can you **adapt the heap approach** for external memory (very large k and N)?
4. What if the lists are **doubly linked**? Does the logic change?
