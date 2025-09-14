# 🔹 Remove N-th Node from End of a Linked List

---

## 📌 Problem Statement
Given the head of a linked list, remove the **n-th node from the end** and return the head of the modified list.

---

## 💡 Logic & Intuition
- A classic linked list problem, commonly solved with **two-pointer technique**.
- Idea:
    - Move `fast` pointer **n steps ahead** first.
    - Then move both `fast` and `slow` until `fast` reaches the end.
    - The `slow` pointer will be just before the node to remove.
- **Edge Case:** if `n` equals the length of the list → remove the **head**.

---

## 🔹 Approaches

### 1. Brute Force (Two Passes)
- Traverse once to **count the total number of nodes**.
- Calculate the position from the start: `(length - n)`.
- Traverse again to that node and delete the **next node**.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

### 2. Optimal (One Pass using Two Pointers + Dummy Node)
- Use a **dummy node** before head (handles head deletion cleanly).
- Move `fast` pointer **n+1 steps ahead**.
- Then move both pointers until `fast` reaches the end.
- `slow` will now be right before the target node → delete it.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 💻 Java Code (Both Approaches)

```java
public class RemoveNthFromEnd {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Approach 1: Brute Force (Two Passes)
    public static ListNode removeNthFromEndBrute(ListNode head, int n) {
        int length = 0;
        ListNode temp = head;

        // First pass: count nodes
        while (temp != null) {
            length++;
            temp = temp.next;
        }

        if (n == length) {
            // Remove head
            return head.next;
        }

        int pos = length - n;
        temp = head;

        // Second pass: reach (length - n - 1)th node
        for (int i = 1; i < pos; i++) {
            temp = temp.next;
        }

        temp.next = temp.next.next;
        return head;
    }

    // Approach 2: Optimal (Single Pass with Dummy Node)
    public static ListNode removeNthFromEndOptimal(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        ListNode first = dummy;
        ListNode second = dummy;

        // Move first ahead by n+1 steps
        for (int i = 0; i <= n; i++) {
            first = first.next;
        }

        // Move both until first reaches the end
        while (first != null) {
            first = first.next;
            second = second.next;
        }

        // Delete target node
        second.next = second.next.next;

        return dummy.next;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                | Time Complexity | Space Complexity |
|-------------------------|-----------------|------------------|
| Brute Force (Two Pass)  | O(n)            | O(1)             |
| Optimal (Two Pointers)  | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- **n = length of list** → remove head.
- **n = 1** → remove last node.
- **Empty list** (`head = null`) → return `null`.
- **Single node list** with `n = 1` → result should be `null`.

---

## 🔹 Follow-Up Questions
1. How would you solve this **without using a dummy node**?
2. Can you solve this using **recursion** instead of iteration?
3. How would the solution change if it was a **doubly linked list**?  
