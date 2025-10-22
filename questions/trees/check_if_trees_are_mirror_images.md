# 🔹 Problem: Check if Two Binary Trees are Mirror Images of Each Other

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the roots of **two binary trees**, determine whether they are **mirror images** of each other.

Two trees are mirror images if:
1. Their **root values are the same**.
2. The **left subtree** of the first tree is the **mirror** of the right subtree of the second tree.
3. The **right subtree** of the first tree is the **mirror** of the left subtree of the second tree.

---

## 🔹 Intuition
- The problem can be solved using **recursion**:
    - Compare the roots.
    - Recursively check:
        - `left` of tree1 with `right` of tree2
        - `right` of tree1 with `left` of tree2
- If at any point the values differ or structure differs → not mirror images.

- Can also be solved **iteratively** using a queue for level-order comparison.

---

## 🔹 Approaches

### 1. Recursive Approach
- Base case: both nodes are `null` → return `true`.
- If one node is `null` and the other isn’t → return `false`.
- Compare current node values.
- Recur for left of first tree with right of second, and right of first with left of second.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

---

### 2. Iterative Approach (Using Queue)
- Use a queue to store pairs of nodes `(node1, node2)`.
- While queue is not empty:
    - Pop a pair.
    - If both are `null` → continue.
    - If one is `null` → return `false`.
    - Compare values; if different → return `false`.
    - Add children in **mirror order** to queue:
        - `(node1.left, node2.right)`
        - `(node1.right, node2.left)`

**Time Complexity:** O(n)  
**Space Complexity:** O(w) — maximum number of node pairs in queue

---

## 🔹 Java Code (All Approaches)

```java
class Node {
    int val;
    Node left, right;
    Node(int val) {
        this.val = val;
    }
}

public class MirrorTrees {

    // 1. Recursive Approach
    public static boolean areMirror(Node root1, Node root2) {
        if (root1 == null && root2 == null) return true;
        if (root1 == null || root2 == null) return false;
        if (root1.val != root2.val) return false;

        return areMirror(root1.left, root2.right) && areMirror(root1.right, root2.left);
    }

    // 2. Iterative Approach using Queue
    public static boolean areMirrorIterative(Node root1, Node root2) {
        Queue<Node[]> q = new LinkedList<>();
        q.add(new Node[]{root1, root2});

        while (!q.isEmpty()) {
            Node[] pair = q.poll();
            Node n1 = pair[0], n2 = pair[1];

            if (n1 == null && n2 == null) continue;
            if (n1 == null || n2 == null) return false;
            if (n1.val != n2.val) return false;

            q.add(new Node[]{n1.left, n2.right});
            q.add(new Node[]{n1.right, n2.left});
        }

        return true;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach             | Time Complexity | Space Complexity | Remarks                      |
|----------------------|----------------|-----------------|-------------------------------|
| Recursive (DFS)      | O(n)           | O(h)             | Simple & elegant              |
| Iterative (BFS)      | O(n)           | O(w)             | Queue stores node pairs       |

---

## 🔹 Edge Cases
- Both trees **empty** → return `true`
- One tree empty → return `false`
- Single-node trees with same value → return `true`
- Single-node trees with different values → return `false`
- Skewed trees → check mirror along the skewed sides

---

## 🔹 Follow-Up Questions
1. Can you modify the function to **return the first pair of nodes where the mirror property fails**?
2. How would this approach change for **N-ary trees**?
3. Can we check mirror property **iteratively using two stacks** instead of a queue?
4. How can you **generate the mirror of a tree** and then compare it to another tree?
5. Can you apply this check **while constructing the trees dynamically**?
