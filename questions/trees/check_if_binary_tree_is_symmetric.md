# 🔹 Problem: Check if a Binary Tree is Symmetric

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, determine whether it is **symmetric** around its center (a mirror of itself).

**Formally:**  
A tree is symmetric if the **left subtree** is a **mirror** of the **right subtree**.

---

## 🔹 Intuition
- The problem is similar to checking if two trees are mirrors.
- For a symmetric tree:
    - `root.left` and `root.right` must be mirrors of each other.
- Can be solved using:
    1. **Recursion** (DFS)
    2. **Iteration** using a queue (BFS)

---

## 🔹 Approaches

### 1. Recursive Approach
- Define a helper function `isMirror(t1, t2)`:
    - If both nodes are `null` → return `true`.
    - If one is `null` → return `false`.
    - Compare values.
    - Recur for `t1.left` & `t2.right`, and `t1.right` & `t2.left`.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

---

### 2. Iterative Approach (Using Queue)
- Use a queue to store pairs of nodes `(left, right)`.
- While the queue is not empty:
    - Pop a pair.
    - If both are `null` → continue.
    - If one is `null` or values differ → return `false`.
    - Push children in mirror order:
        - `(left.left, right.right)`
        - `(left.right, right.left)`

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

public class SymmetricBinaryTree {

    // 1. Recursive Approach
    public static boolean isSymmetric(Node root) {
        if (root == null) return true;
        return isMirror(root.left, root.right);
    }

    private static boolean isMirror(Node t1, Node t2) {
        if (t1 == null && t2 == null) return true;
        if (t1 == null || t2 == null) return false;
        if (t1.val != t2.val) return false;

        return isMirror(t1.left, t2.right) && isMirror(t1.right, t2.left);
    }

    // 2. Iterative Approach using Queue
    public static boolean isSymmetricIterative(Node root) {
        if (root == null) return true;

        Queue<Node[]> q = new LinkedList<>();
        q.add(new Node[]{root.left, root.right});

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
- **Empty tree** → return `true`
- **Single node tree** → return `true`
- **Tree with only left or right subtree** → return `false`
- **Skewed trees** → asymmetric → return `false`

---

## 🔹 Follow-Up Questions
1. Can you implement the **iterative approach using two stacks**?
2. How can you **check symmetry while constructing the tree dynamically**?
3. Can this approach be generalized for **N-ary trees**?
4. Can we **mirror the tree first** and then compare to check symmetry?
5. How would you **return the first pair of nodes violating symmetry**?
