# 🔹 Clone a Linked List with Next and Random Pointer

---

## 📌 Problem Statement
You have a linked list where each node has two pointers:
- `next` → points to the next node in the list
- `random` → points to any node in the list or `null`

Your task is to create a **deep copy** of this list. The cloned list should have exactly the same structure and random pointer connections, but **all nodes should be newly created**.

---

## 💡 Logic & Intuition
- Each node needs a **new instance** to ensure deep copy.
- The `next` pointers maintain list order.
- The `random` pointers can point anywhere in the list, so we need a way to track the mapping from original → cloned nodes.
- Two main approaches:
    1. Use a **HashMap** to map original nodes to their clones.
    2. Use **in-place interleaving** to avoid extra space.

---

## 🔹 Approaches

### 1. Using HashMap (Brute Force)
- Map each original node to its cloned node using a `HashMap`.
- Steps:
    1. Traverse the original list and create a cloned node for each original node.
    2. Store mapping `original → clone` in a `HashMap`.
    3. Traverse again and set `next` and `random` pointers of cloned nodes using the map.
    4. Return the cloned head from the map.

**Time Complexity:** O(n)  
**Space Complexity:** O(n) (for the HashMap)

---

### 2. In-place Interleaving (Optimized)
- Insert cloned nodes **between original nodes** to avoid extra space.
- Steps:
    1. Traverse the original list and insert cloned nodes right after their originals.
    2. Set `random` pointers of cloned nodes using original nodes’ random pointers.
    3. Separate the original and cloned lists to restore the original list and extract the clone.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 💻 Java Code (All Approaches)

```java
import java.util.HashMap;
import java.util.Map;

public class CloneLinkedList {

    static class Node {
        int val;
        Node next, random;
        Node(int val) { this.val = val; }
    }

    // Approach 1: Using HashMap
    public static Node copyRandomListWithMap(Node head) {
        if (head == null) return null;

        Map<Node, Node> map = new HashMap<>();
        Node curr = head;

        // Step 1: Clone nodes and store in map
        while (curr != null) {
            map.put(curr, new Node(curr.val));
            curr = curr.next;
        }

        // Step 2: Set next and random pointers
        curr = head;
        while (curr != null) {
            Node clone = map.get(curr);
            clone.next = map.get(curr.next);
            clone.random = map.get(curr.random);
            curr = curr.next;
        }

        // Step 3: Return cloned head
        return map.get(head);
    }

    // Approach 2: In-place Interleaving
    public static Node copyRandomList(Node head) {
        if (head == null) return null;

        Node curr = head;

        // Step 1: Insert cloned nodes after original nodes
        while (curr != null) {
            Node clone = new Node(curr.val);
            clone.next = curr.next;
            curr.next = clone;
            curr = clone.next;
        }

        // Step 2: Set random pointers for cloned nodes
        curr = head;
        while (curr != null) {
            if (curr.random != null) {
                curr.next.random = curr.random.next;
            }
            curr = curr.next.next;
        }

        // Step 3: Separate original and cloned lists
        Node dummy = new Node(0);
        Node copyCurr = dummy;
        curr = head;

        while (curr != null) {
            copyCurr.next = curr.next;
            curr.next = curr.next.next;

            copyCurr = copyCurr.next;
            curr = curr.next;
        }

        return dummy.next;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                          | Time Complexity | Space Complexity |
|-----------------------------------|-----------------|------------------|
| HashMap (Brute Force)             | O(n)            | O(n)             |
| In-place Interleaving (Optimized) | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Empty list → return `null`.
- All nodes’ `random` pointers are `null`.
- Random pointers form cycles.
- Single node → copy node correctly.

---

## 🔹 Follow-Up Questions
1. Can you **restore the original list** after cloning (without extra space)?
2. How would you handle **deep copy for a doubly linked list with random pointers**?
3. Can this be adapted for **streaming input** where nodes are generated one by one?
