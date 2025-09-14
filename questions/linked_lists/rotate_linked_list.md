# 🔹 Rotate a Linked List

---

## 📌 Problem Statement
Given the head of a linked list and an integer `k`, **rotate the list to the right by k places**.

---

## 💡 Logic & Intuition
- Rotating a linked list means moving the **last k nodes to the front**.
- Key observations:
    1. Rotating the list by its **length** results in the same list.
    2. Make the list **circular**, find the **new tail**, and then **break the circle**.

---

## 🔹 Approaches

### 1. Brute Force
- Rotate **one node at a time**, repeating k times.
- For each rotation, traverse to the **last node**.
- **Time Complexity:** O(k × n)
- **Space Complexity:** O(1)
> Inefficient for large k.

### 2. Optimal Approach
- Count the **length** of the list.
- Connect the **tail to head** (make it circular).
- Move to the node just before the **new head** (`len - k % len` steps).
- Break the circle.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 💻 Java Code (Both Approaches)

```java
public class RotateLinkedList {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Approach 1: Brute Force (Rotate one node at a time)
    public static ListNode rotateRightBrute(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) return head;

        // Count length
        int len = 0;
        ListNode temp = head;
        while (temp != null) {
            len++;
            temp = temp.next;
        }

        k = k % len;
        if (k == 0) return head;

        for (int i = 0; i < k; i++) {
            ListNode prev = null;
            ListNode curr = head;
            // Traverse to last node
            while (curr.next != null) {
                prev = curr;
                curr = curr.next;
            }
            // Move last node to front
            prev.next = null;
            curr.next = head;
            head = curr;
        }

        return head;
    }

    // Approach 2: Optimal (Circular List)
    public static ListNode rotateRightOptimal(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) return head;

        // Step 1: Count the length and find the tail
        ListNode tail = head;
        int len = 1;
        while (tail.next != null) {
            tail = tail.next;
            len++;
        }

        // Step 2: Make the list circular
        tail.next = head;

        // Step 3: Find the new tail (len - k % len - 1)th node
        int stepsToNewTail = len - k % len;
        ListNode newTail = head;
        for (int i = 1; i < stepsToNewTail; i++) {
            newTail = newTail.next;
        }

        // Step 4: Set the new head and break the loop
        ListNode newHead = newTail.next;
        newTail.next = null;

        return newHead;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach           | Time Complexity | Space Complexity |
|--------------------|-----------------|------------------|
| Brute Force        | O(k × n)        | O(1)             |
| Optimal (Readable) | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Empty list → return `null`.
- List with **1 node** → return head.
- `k = 0` → return head as is.
- `k` greater than length → use `k % length`.

---

## 🔹 Follow-Up Questions
1. How would you rotate the list to the **left** instead of the right?
2. Can you perform the rotation **without making it circular**?
3. How would you rotate a **doubly linked list** efficiently?  
