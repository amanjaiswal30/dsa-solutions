# 🔹 Reverse a Linked List

---

## 📌 Problem Statement
Given the head of a singly linked list, reverse the list and return the **new head**.

---

## 💡 Logic & Intuition
- To reverse a linked list, you need to **change the direction** of each node’s `next` pointer so that it points to the **previous node**.
- Two common approaches: **iterative** and **recursive**.

---

## 🔹 Approaches

### 1. Iterative Approach (Optimal)
- Use three pointers: **prev**, **curr**, and **next**.
- Iterate through the list, and at each step:
    1. Store the next node.
    2. Point current node’s `next` to `prev`.
    3. Move `prev` and `curr` forward.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

### 2. Recursive Approach
- Reverse the rest of the list recursively.
- Set `head.next.next = head` and `head.next = null`.
- Base case: return node if it’s null or only one node.
- **Time Complexity:** O(n)
- **Space Complexity:** O(n) (due to recursion stack)

---

## 💻 Java Code (Both Approaches)

```java
public class ReverseLinkedList {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Approach 1: Iterative Method
    public static ListNode reverseListIterative(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;

        while (curr != null) {
            ListNode next = curr.next; // store next node
            curr.next = prev;          // reverse pointer
            prev = curr;               // move prev forward
            curr = next;               // move curr forward
        }

        return prev;
    }

    // Approach 2: Recursive Method
    public static ListNode reverseListRecursive(ListNode head) {
        // Base case
        if (head == null || head.next == null) return head;

        // Recursively reverse the rest
        ListNode newHead = reverseListRecursive(head.next);

        head.next.next = head; // reverse the current link
        head.next = null;      // set current node's next to null

        return newHead;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach         | Time Complexity | Space Complexity |
|------------------|-----------------|------------------|
| Iterative Method | O(n)            | O(1)             |
| Recursive Method | O(n)            | O(n)             |

---

## 🔹 Edge Cases
- Empty list → return `null`.
- List with **1 node** → return head.
- List with **2 nodes** → correctly reverse both.

---

## 🔹 Follow-Up Questions
1. Can you reverse the linked list **in groups of k nodes**?
2. How would you reverse a **doubly linked list**?
3. Can you implement a **stack using a reversed linked list** efficiently?  
