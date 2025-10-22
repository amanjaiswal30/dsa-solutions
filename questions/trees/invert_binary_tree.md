# 🔹 Problem: Invert a Binary Tree

**Difficulty:** Easy 🌱

---

## 🔹 Problem Statement
Given the root of a **binary tree**, invert the tree such that the **left and right children of all nodes are swapped**.

**Formally:**  
For each node in the tree:

swap(node.left, node.right)


---

## 🔹 Intuition
- Inverting a tree is equivalent to **mirroring it**.
- Can be solved recursively or iteratively:
    1. **Recursive DFS** — swap children at each node, then recur.
    2. **Iterative BFS** — level order traversal and swap children at each node.

---

## 🔹 Approaches

### 1. Recursive Approach
- Base case: if node is `null` → return.
- Swap left and right children.
- Recur for left and right subtrees.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

---

### 2. Iterative Approach (Using Queue)
- Use a queue for **level-order traversal**.
- For each node popped:
    - Swap its left and right children.
    - Enqueue non-null children.

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

public class InvertBinaryTree {

    // 1. Recursive Approach
    public static Node invertTreeRecursive(Node root) {
        if (root == null) return null;

        Node temp = root.left;
        root.left = root.right;
        root.right = temp;

        invertTreeRecursive(root.left);
        invertTreeRecursive(root.right);

        return root;
    }

    // 2. Iterative Approach using Queue
    public static Node invertTreeIterative(Node root) {
        if (root == null) return null;

        Queue<Node> q = new LinkedList<>();
        q.add(root);

        while (!q.isEmpty()) {
            Node curr = q.poll();

            // Swap left and right
            Node temp = curr.left;
            curr.left = curr.right;
            curr.right = temp;

            if (curr.left != null) q.add(curr.left);
            if (curr.right != null) q.add(curr.right);
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

## 🔹 Edge Cases
- **Empty tree** → return `null`
- **Single node** → remains unchanged
- **Left-skewed tree** → becomes right-skewed
- **Right-skewed tree** → becomes left-skewed

---

## 🔹 Follow-Up Questions
1. Can you **invert the tree in-place during tree construction**?
2. How would you **invert an N-ary tree**?
3. Can you **perform inversion using a stack** instead of a queue?
4. How does **mirror vs invert** differ conceptually?
5. Can you **invert only a specific subtree** given a target node?
