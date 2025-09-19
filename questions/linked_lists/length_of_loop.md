# 🔹 Find the Length of Loop in a Linked List

---

## 📌 Problem Statement
Given a linked list that contains a **loop**, determine the **number of nodes present inside the loop**.  
If there is no loop, return `0`.

---

## 💡 Logic & Intuition
We use **Floyd’s Cycle Detection Algorithm** (Tortoise and Hare):

### Step 1: Detect the Loop
- Use two pointers:
    - `slow` → moves one step
    - `fast` → moves two steps
- If they meet → a loop exists.

### Step 2: Measure the Loop
- Once a meeting point is found inside the loop:
    - Keep one pointer fixed.
    - Move the other pointer until it returns to the fixed one.
    - Count the number of steps taken → that’s the **loop length**.

---

## 🔹 Java Code

```java
public class LoopLength {

  static class ListNode {
    int val;
    ListNode next;
    ListNode(int val) {
      this.val = val;
    }
  }

  public static int countLoopLength(ListNode head) {
    ListNode slow = head, fast = head;

    // Step 1: Detect loop using Floyd's algorithm
    while (fast != null && fast.next != null) {
      slow = slow.next;
      fast = fast.next.next;

      if (slow == fast) { // Loop detected
        int count = 1;
        slow = slow.next;

        // Step 2: Count nodes in the loop
        while (slow != fast) {
          slow = slow.next;
          count++;
        }

        return count;
      }
    }

    return 0; // No loop
  }
}
```
---

## 🔹 Complexity Analysis

| Step               | Time Complexity | Space Complexity |
|--------------------|-----------------|------------------|
| Detecting loop     | O(n)            | O(1)             |
| Counting loop size | O(k)            | O(1)             |
| **Overall**        | **O(n)**        | **O(1)**         |

---

## 🔹 Edge Cases
- No loop → function returns **0**.
- Loop starts at the **head** node.
- Entire linked list is a **cycle**.
- Single node pointing to itself (loop length = 1).

---

## 🔹 Follow-Up Questions
1. Can you detect and count the loop length **without modifying** the list?
2. How would the logic change if this were a **doubly linked list**?
3. Could you extend this to also find the **starting node of the loop** along with its length?  

