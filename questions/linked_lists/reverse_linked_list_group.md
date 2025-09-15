# 🔹 Reverse Linked List in Groups of Size K

---

## 📌 Problem Statement
Given a linked list, reverse the nodes of the list in groups of size `k` and return the modified list.  
If the number of nodes is **not a multiple of k**, then the last remaining nodes should remain as is.

---

## 💡 Logic & Intuition
- Instead of reversing the entire list at once, we reverse it in **chunks of size k**.
- Steps:
    1. Traverse and check if there are at least `k` nodes to reverse.
    2. Reverse those `k` nodes.
    3. Recur (or iterate) for the remaining nodes.
    4. Connect the reversed block with the result of the remaining list.

---

## 🔹 Approaches

### 1. Recursive (Optimal & Clean)
- Reverse the **first k nodes**.
- Recur for the remaining list.
- Connect the **current reversed group** with the head of the next reversed group.
- **Time Complexity:** O(n)
- **Space Complexity:** O(n/k) (recursive stack)

---

### 2. Iterative (Advanced)
- Use a **dummy node** before head for easy handling.
- Reverse nodes in chunks of size `k` using multiple pointers.
- After each reversal, connect the groups.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 💻 Java Code (Both Approaches)

```java
public class ReverseKGroup {

    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) {
            this.val = val;
        }
    }

    // Approach 1: Recursive
    public static ListNode reverseKGroupRecursive(ListNode head, int k) {
        // Step 1: Check if we have at least k nodes
        ListNode temp = head;
        int count = 0;
        while (temp != null && count < k) {
            temp = temp.next;
            count++;
        }

        if (count < k) return head; // not enough nodes

        // Step 2: Reverse first k nodes
        ListNode prev = null, curr = head, next = null;
        count = 0;
        while (curr != null && count < k) {
            next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
            count++;
        }

        // Step 3: Connect with the next reversed part
        head.next = reverseKGroupRecursive(curr, k);

        return prev; // new head of this group
    }

    // Approach 2: Iterative
    public static ListNode reverseKGroupIterative(ListNode head, int k) {
        if (head == null || k == 1) return head;

        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode prev = dummy, curr = dummy, next = dummy;

        // Step 1: Count total nodes
        int count = 0;
        while (curr.next != null) {
            curr = curr.next;
            count++;
        }

        // Step 2: Reverse in groups of k
        while (count >= k) {
            curr = prev.next;
            next = curr.next;
            for (int i = 1; i < k; i++) {
                curr.next = next.next;
                next.next = prev.next;
                prev.next = next;
                next = curr.next;
            }
            prev = curr;
            count -= k;
        }

        return dummy.next;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach   | Time Complexity | Space Complexity |
|------------|-----------------|------------------|
| Recursive  | O(n)            | O(n/k)           |
| Iterative  | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- **k = 1** → List remains unchanged.
- **Length of list < k** → Return original list.
- **Empty list (`head = null`)** → Return `null`.
- **Last group smaller than k** → Keep as is.

---

## 🔹 Follow-Up Questions
1. How would the solution change if the list were **doubly linked**?
2. Can we solve it using a **stack-based approach** for educational purposes?
3. How would you optimize this for **very large k values**?
4. Can you modify the code to **restore the original list** after reversing in groups?

---
