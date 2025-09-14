# 🔹 Intersection Point in Y-Shaped Linked List

---

## 📌 Problem Statement
Given two singly linked lists that merge at a certain point (**Y-shaped**), find the node at which they intersect.  
If there is **no intersection**, return `null`.

---

## 💡 Logic & Intuition
- When two lists intersect, the nodes **after the intersection point are common**.
- To detect the intersection, we need to align both lists properly.

---

## 🔹 Approaches

### 1. Brute Force
- For each node in **List A**, compare it with every node in **List B**.
- If they match → return intersection node.
- **Time Complexity:** O(m × n)
- **Space Complexity:** O(1)
- **Drawback:** Inefficient for large lists.

---

### 2. Hashing
- Store all nodes of **List A** in a `HashSet`.
- Traverse **List B**, the first node found in the set is the intersection.
- **Time Complexity:** O(m + n)
- **Space Complexity:** O(m)

---

### 3. Optimal (Length Difference Technique)
- Count the lengths of both lists.
- Move the longer list’s pointer ahead by the **difference in lengths**.
- Then traverse both together until they meet.
- **Time Complexity:** O(m + n)
- **Space Complexity:** O(1)

---

### 4. Alternative Optimal (Two Pointer Traversal)
- Maintain two pointers `a` and `b`.
- Traverse both lists; when a pointer reaches the end, switch it to the head of the other list.
- Eventually, they meet at the intersection or both become `null`.
- **Time Complexity:** O(m + n)
- **Space Complexity:** O(1)
- **Advantage:** Very concise and elegant solution.

---

## 💻 Java Code (All Approaches)

```java
import java.util.HashSet;
import java.util.Set;

public class IntersectionPoint {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // Approach 1: Brute Force
    public static ListNode getIntersectionBrute(ListNode headA, ListNode headB) {
        while (headA != null) {
            ListNode temp = headB;
            while (temp != null) {
                if (headA == temp) {
                    return headA;
                }
                temp = temp.next;
            }
            headA = headA.next;
        }
        return null;
    }

    // Approach 2: Hashing
    public static ListNode getIntersectionHashing(ListNode headA, ListNode headB) {
        Set<ListNode> set = new HashSet<>();
        while (headA != null) {
            set.add(headA);
            headA = headA.next;
        }
        while (headB != null) {
            if (set.contains(headB)) {
                return headB;
            }
            headB = headB.next;
        }
        return null;
    }

    // Approach 3: Length Difference Method
    public static ListNode getIntersectionLengthDiff(ListNode headA, ListNode headB) {
        int lenA = getLength(headA);
        int lenB = getLength(headB);

        while (lenA > lenB) {
            headA = headA.next;
            lenA--;
        }
        while (lenB > lenA) {
            headB = headB.next;
            lenB--;
        }

        while (headA != null && headB != null) {
            if (headA == headB) {
                return headA;
            }
            headA = headA.next;
            headB = headB.next;
        }
        return null;
    }

    private static int getLength(ListNode head) {
        int len = 0;
        while (head != null) {
            len++;
            head = head.next;
        }
        return len;
    }

    // Approach 4: Two Pointer Traversal (Elegant)
    public static ListNode getIntersectionTwoPointer(ListNode headA, ListNode headB) {
        ListNode a = headA;
        ListNode b = headB;

        while (a != b) {
            a = (a == null) ? headB : a.next;
            b = (b == null) ? headA : b.next;
        }

        return a; // Either intersection node or null
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                         | Time Complexity | Space Complexity |
|----------------------------------|-----------------|------------------|
| Brute Force                      | O(m × n)        | O(1)             |
| Hashing                          | O(m + n)        | O(m)             |
| Length Difference Technique      | O(m + n)        | O(1)             |
| Two Pointer Traversal (Elegant)  | O(m + n)        | O(1)             |

---

## 🔹 Edge Cases
- No intersection → return `null`.
- One or both lists are empty → return `null`.
- Intersection at the **head node** itself.
- Lists of **unequal lengths**.

---

## 🔹 Follow-Up Questions
1. How would you detect if the two lists are **cyclic** and intersect inside a cycle?
2. Can you solve this problem if you are **not allowed to modify the lists** (no extra space, no hashing)?
3. How would the approach change if the lists were **doubly linked**?  
