# 🔹 Find the Middle Element of a Linked List

---

## 📌 Problem Statement
Given a singly linked list, find the middle node.  
If there are two middle nodes (even length), return the **second one**.

---

## 💡 Logic & Intuition
- A very common and efficient way is using **two pointers: slow and fast**.
- Move the slow pointer **one step** at a time, and the fast pointer **two steps**.
- When the fast pointer reaches the end, the slow pointer will be at the **middle**.

---

## 🔹 Approaches

### 1. Brute Force (Two Passes)
- Traverse the list to **count total nodes**.
- Traverse again till the **middle index (n / 2)**.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

### 2. Optimal (Slow & Fast Pointer - Single Pass)
- Use two pointers: **slow and fast**.
- Fast moves 2 steps, slow moves 1 step.
- When fast reaches the end, slow is at the middle.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 💻 Java Code (Both Approaches)

```java
public class MiddleOfLinkedList {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Approach 1: Brute Force (Two Passes)
    public static ListNode middleNodeBrute(ListNode head) {
        int count = 0;
        ListNode temp = head;

        // First pass: count nodes
        while (temp != null) {
            count++;
            temp = temp.next;
        }

        int mid = count / 2;
        temp = head;

        // Second pass: move to mid
        for (int i = 0; i < mid; i++) {
            temp = temp.next;
        }

        return temp;
    }

    // Approach 2: Optimal (Slow & Fast Pointers - Single Pass)
    public static ListNode middleNodeOptimal(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        // Fast moves 2 steps, slow moves 1 step
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                | Time Complexity | Space Complexity |
|-------------------------|-----------------|------------------|
| Brute Force (Two Pass)  | O(n)            | O(1)             |
| Slow & Fast Pointer     | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Empty list → return `null`.
- List with **1 node** → return head.
- List with **2 nodes** → return the **second node** (since two middles → pick second).

---

## 🔹 Follow-Up Questions
1. Can you modify this to return the **first middle node** instead of the second?
2. How would you solve this if the list was **doubly linked**?
3. Can you extend this approach to find the **k-th node from the end**?  
