# 🔹 Detect and Remove Loop in a Linked List

---

## 📌 Problem Statement
Given the head of a linked list, detect if there is a **loop (cycle)** in it.  
If a loop exists, **remove the loop** and restore the linked list.

---

## 💡 Logic & Intuition

### 🔹 Detecting the Loop
- Use **Floyd’s Cycle Detection Algorithm (Tortoise & Hare)**.
- Maintain two pointers:
    - `slow` → moves **one step** at a time.
    - `fast` → moves **two steps** at a time.
- If they **ever meet**, a loop exists.

### 🔹 Finding the Start of the Loop
- Once a loop is detected:
    - Place one pointer (`slow`) at the **head**.
    - Keep the other pointer (`fast`) at the **meeting point**.
    - Move both one step at a time.
    - The point where they meet again = **start of the loop**.

### 🔹 Removing the Loop
- To remove the loop:
    - Traverse from the loop’s start node until you reach the node whose `next` points back to the loop’s start.
    - Set that node’s `next = null`.

---

## 🔹 Approaches

### 1. Optimal (Floyd’s + Loop Removal)
- Detect loop using slow & fast pointers.
- Find the loop start.
- Break the loop by setting the last node’s `next = null`.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 💻 Java Code

```java
public class DetectAndRemoveLoop {

  static class ListNode {
    int val;
    ListNode next;
    ListNode(int val) {
      this.val = val;
    }
  }

  public static void detectAndRemoveLoop(ListNode head) {
    if (head == null || head.next == null) return;

    ListNode slow = head;
    ListNode fast = head;
    ListNode pre = null;
    ListNode ptr = head;

    while (fast != null && fast.next != null) {
      fast = fast.next.next;
      pre = slow;
      slow = slow.next;

      if (slow == fast) {  // Loop detected
        // Move ptr from head and slow from meeting point to find loop start
        while (ptr != slow) {
          ptr = ptr.next;
          pre = slow;
          slow = slow.next;
        }

        // Break the loop
        pre.next = null;
        return;
      }
    }
  }
}
```

---

## 🔹 Complexity Analysis

| Step                  | Time Complexity | Space Complexity |
|-----------------------|-----------------|------------------|
| Detecting loop        | O(n)            | O(1)             |
| Finding loop start    | O(n)            | O(1)             |
| Removing the loop     | O(n)            | O(1)             |
| **Overall**           | **O(n)**        | **O(1)**         |

---

## 🔹 Edge Cases
- No loop → return without changes.
- Loop starts at the **head**.
- Entire list is a **cycle**.
- Single node pointing to itself.

---

## 🔹 Follow-Up Questions
1. How would you **only detect** a loop without removing it?
2. Can you modify this to find the **length of the loop**?
3. What if the list is a **doubly linked list**? Would the logic change?

---
