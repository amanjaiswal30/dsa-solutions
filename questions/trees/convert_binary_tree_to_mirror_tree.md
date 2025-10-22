# 🔹 Problem: Convert a Given Binary Tree into its Mirror Tree

**Difficulty:** Easy 🌱

---

## 🔹 Problem Statement
Given the root of a **binary tree**, convert it into its **mirror tree**.  
In the mirror tree, for every node, the **left and right children are swapped**.

**Formally:**  
For each node in the tree:

swap(node.left, node.right)


---

## 🔹 Intuition
- The mirror of a tree is obtained by **swapping left and right subtrees** for every node.
- The operation is **recursive** in nature.
- The problem can be solved using:
    1. **Recursion** (simple DFS)
    2. **Iterative approach** using a queue (BFS level order)

---

## 🔹 Approaches

### 1. Recursive Approach
- For each node:
    - Swap its left and right children.
    - Recur for both subtrees.
- Base case: if node is `null`, return.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

---

### 2. Iterative Approach (Using Queue)
- Perform **level order traversal**.
- For every node popped from the queue:
    - Swap its left and right children.
    - Push the children into the queue.
- Avoid recursion stack overflow for large trees.

**Time Complexity:** O(n)  
**Space Complexity:** O(w) — width of the tree

---

## 🔹 Java Code (All Approaches)

```java
import java.util.*;

class Node {
    int val;
    Node left, right;
    Node(int val) {
        this.val = val;
    }
}

public class MirrorBinaryTree {

    // 1. Recursive Approach
    public static Node mirrorRecursive(Node root) {
        if (root == null) return null;

        // Swap left and right
        Node temp = root.left;
        root.left = root.right;
        root.right = temp;

        // Recur for subtrees
        mirrorRecursive(root.left);
        mirrorRecursive(root.right);

        return root;
    }

    // 2. Iterative Approach using Queue (Level Order)
    public static Node mirrorIterative(Node root) {
        if (root == null) return null;

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node curr = queue.poll();

            // Swap left and right children
            Node temp = curr.left;
            curr.left = curr.right;
            curr.right = temp;

            if (curr.left != null) queue.add(curr.left);
            if (curr.right != null) queue.add(curr.right);
        }

        return root;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach             | Time Complexity | Space Complexity | Remarks                    |
|----------------------|----------------|-----------------|----------------------------|
| Recursive (DFS)      | O(n)           | O(h)             | Each node visited once; recursion stack depends on tree height |
| Iterative (BFS)      | O(n)           | O(w)             | Queue stores nodes level-wise; w = max width of tree |

---

## 🔹 Key Observations
- **Mirror of mirror = original tree** — applying the mirror operation twice restores the original tree.
- Works for **any binary tree**, not just BSTs.
- **In-place operation** — no new nodes are created; only pointers are swapped.
- Order of traversal (preorder, postorder, or level order) doesn’t affect correctness as long as all nodes are processed.

---

## 🔹 Edge Cases
- **Empty tree** → return `null`
- **Single node** → remains unchanged
- **Skewed tree** → left-skewed becomes right-skewed and vice versa
- **Large tree** → iterative BFS may be preferred to avoid stack overflow

---

## 🔹 Follow-Up Questions
1. Can you write a function to **check if two trees are mirror images** of each other?
2. How can you mirror the tree **in place** during level-order traversal?
3. What changes would be needed for an **N-ary tree**?
4. How can you **mirror only a specific subtree** given a target node?
5. Can you **perform the mirroring iteratively using a stack** instead of a queue?