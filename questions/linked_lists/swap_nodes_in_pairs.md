# 🔹 Swap Nodes in Pairs

---

## 📌 Problem Statement
Given the `head` of a singly linked list, swap every **two adjacent nodes**, and return the **new head**. You must solve it without changing values in the nodes (only **pointer** changes).

---

## 💡 Logic & Intuition
- Each swap involves **two consecutive nodes**; after swapping, advance the anchor past the pair.
- A **`dummy`** node before `head` avoids a special case for swapping the first two nodes.
- Name the two nodes **`swap1`** and **`swap2`** (`swap1 = prev.next`, `swap2 = swap1.next`), then rewire: `swap1.next = swap2.next`, `swap2.next = swap1`, `prev.next = swap2`, then move **`prev` to `swap1`** for the next pair.

---

## 🔹 Approach

### Optimal (dummy + one pass)
- **Time Complexity:** O(n)  
- **Space Complexity:** O(1)

---

## 💻 Java Code

```java
public class SwapNodesInPairs {

    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    public static ListNode swapPairs(ListNode head) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy;

        while (prev.next != null && prev.next.next != null) {
            ListNode swap1 = prev.next;
            ListNode swap2 = prev.next.next;

            swap1.next = swap2.next;
            swap2.next = swap1;
            prev.next = swap2;

            prev = swap1;
        }
        return dummy.next;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach        | Time | Space |
|-----------------|------|-------|
| Dummy + pointers | O(n) | O(1)  |

---

## 🔹 Edge Cases
- **Empty list** → `dummy.next` is `null`, return `null`.
- **Single node** → loop does not run; list unchanged.
- **Odd length** → last node stays in place.

---

## 🔹 Follow-Up Questions
1. How would you generalize to **swap every k** nodes?
2. Can you do this **recursively** with the same asymptotic cost?
