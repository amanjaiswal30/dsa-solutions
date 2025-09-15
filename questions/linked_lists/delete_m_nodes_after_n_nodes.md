# 🔹 Delete N Nodes After M Nodes of a Linked List

---

## 📌 Problem Statement
Given a linked list, **repeatedly skip M nodes and then delete N nodes**, continuing until the end of the list.

**Example:**  
Input:  

1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10
M = 2, N = 3

Output:  

1 → 2 → 6 → 7

**Explanation:**
- Skip first 2 nodes (1, 2), delete next 3 nodes (3, 4, 5)
- Skip next 2 nodes (6, 7), delete next 3 nodes (8, 9, 10)

---

## 💡 Logic & Intuition
- Traverse the list while **skipping M nodes**.
- Delete the next N nodes by **adjusting the next pointer**.
- Repeat until the list ends.
- Can be implemented **iteratively, recursively, or using two pointers**.

---

## 🔹 Approaches

### 1. Iterative (Brute Force)
- Use a single pointer to traverse the list.
- Skip M nodes, then delete N nodes by adjusting `next`.
- Repeat until the end.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

### 2. Recursive
- Skip M nodes recursively.
- Delete next N nodes recursively.
- Connect and recurse until end.

**Time Complexity:** O(n)  
**Space Complexity:** O(n/M) (recursion stack)

---

### 3. Two Pointers Variant
- Use one pointer to skip M nodes.
- Use another pointer to delete N nodes.
- Adjust pointers and repeat.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 💻 Java Code (All Approaches)

```java
public static class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

// Approach 1: Iterative
public static ListNode deleteNAfterM(ListNode head, int M, int N) {
    if (head == null || N == 0) return head;

    ListNode current = head;
    while (current != null) {
        // Skip M nodes
        for (int count = 1; count < M && current != null; count++) {
            current = current.next;
        }
        if (current == null) break;

        // Delete next N nodes
        ListNode temp = current.next;
        for (int count = 0; count < N && temp != null; count++) {
            temp = temp.next;
        }

        // Link Mth node to the node after deleted nodes
        current.next = temp;
        current = temp;
    }
    return head;
}

// Approach 2: Recursive
public static ListNode deleteNAfterMRecursive(ListNode head, int M, int N) {
    if (head == null) return null;

    ListNode current = head;
    // Skip M nodes
    for (int i = 1; i < M && current != null; i++) {
        current = current.next;
    }

    if (current == null) return head;

    // Delete next N nodes
    ListNode temp = current.next;
    for (int i = 0; i < N && temp != null; i++) {
        temp = temp.next;
    }

    current.next = deleteNAfterMRecursive(temp, M, N);
    return head;
}

// Approach 3: Two Pointers Variant
public static ListNode deleteNAfterMTwoPointers(ListNode head, int M, int N) {
    ListNode current = head;

    while (current != null) {
        for (int i = 1; i < M && current != null; i++) {
            current = current.next;
        }
        if (current == null) return head;

        ListNode delPtr = current.next;
        for (int i = 0; i < N && delPtr != null; i++) {
            delPtr = delPtr.next;
        }
        current.next = delPtr;
        current = delPtr;
    }
    return head;
}
```

---

## 🔹 Complexity Analysis

| Approach             | Time Complexity | Space Complexity |
|----------------------|-----------------|------------------|
| Iterative            | O(n)            | O(1)             |
| Recursive            | O(n)            | O(n/M)           |
| Two Pointers Variant | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Empty list → return `null`.
- N = 0 → list remains unchanged.
- M = 0 → delete first N nodes, then repeat.
- N or M larger than remaining nodes → handle gracefully.

---

## 🔹 Follow-Up Questions
1. Can you **delete nodes in-place while also keeping a count of deleted nodes**?
2. How would you **adapt this for a doubly linked list**?
3. Can this be done **using a single pass without nested loops**?
