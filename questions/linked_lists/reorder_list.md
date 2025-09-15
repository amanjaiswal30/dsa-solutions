# 🔹 Reorder List

---

## 📌 Problem Statement
Given a singly linked list:

L1 → L2 → … → Ln

Reorder it to:

L1 → Ln → L2 → Ln-1 → L3 → Ln-2 → …


Rearrange the nodes **in-place** without altering the node values.

---

## 💡 Logic & Intuition
- The problem can be broken down into **three subproblems**:
    1. Find the **middle node** of the list using fast and slow pointers.
    2. **Reverse the second half** of the list.
    3. **Merge the two halves** alternately to achieve the required order.
- This ensures nodes are reordered **in-place** with **O(1) extra space**.

---

## 🔹 Approach (Optimized)
1. Use two pointers (`slow` and `fast`) to find the middle node.
2. Reverse the second half of the list starting from the middle.
3. Merge nodes from the first half and the reversed second half alternately.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 💻 Java Code

```java
public static class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
        this.val = val;
    }
}

public static void reorderList(ListNode head) {
    if (head == null || head.next == null) return;

    // Step 1: Find the middle of the list
    ListNode slow = head, fast = head;
    while (fast.next != null && fast.next.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }

    // Step 2: Reverse second half
    ListNode second = reverseList(slow.next);
    slow.next = null;  // Split the list into two halves

    // Step 3: Merge two halves
    ListNode first = head;
    while (second != null) {
        ListNode temp1 = first.next;
        ListNode temp2 = second.next;

        first.next = second;
        second.next = temp1;

        first = temp1;
        second = temp2;
    }
}

// Helper method to reverse a linked list
private static ListNode reverseList(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode nextTemp = curr.next;
        curr.next = prev;
        prev = curr;
        curr = nextTemp;
    }
    return prev;
}
```

---

## 🔹 Complexity Analysis

| Step                  | Time Complexity | Space Complexity |
|-----------------------|-----------------|------------------|
| Finding middle        | O(n)            | O(1)             |
| Reversing second half | O(n)            | O(1)             |
| Merging two halves    | O(n)            | O(1)             |
| **Overall**           | **O(n)**        | **O(1)**         |

---

## 🔹 Edge Cases
- Empty list → no changes.
- Single node → list remains the same.
- Two nodes → simply swap order if needed.
- Odd vs even length lists → works for both.

---

## 🔹 Follow-Up Questions
1. How would you **reorder a doubly linked list**?
2. Can this be done **recursively** without using extra space?
3. How would you **reorder only a sublist** from position m to n?


