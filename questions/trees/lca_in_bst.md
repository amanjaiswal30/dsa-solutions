# 🔹 Problem: Lowest Common Ancestor in a Binary Search Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a **Binary Search Tree (BST)** and two nodes `p` and `q`, find their **Lowest Common Ancestor (LCA)**.

- **LCA** of two nodes `p` and `q` in a BST is defined as the **lowest node** in the tree that has both `p` and `q` as descendants (a node can be a descendant of itself).

**BST Property:**
- Left subtree < node < right subtree

---

## 🔹 Intuition
- Start from root.
- If both `p` and `q` are **less than root**, LCA lies in left subtree.
- If both are **greater than root**, LCA lies in right subtree.
- Otherwise, current node is **split point** → LCA.

---

## 🔹 Approaches

### 1. Recursive
- Compare `p` and `q` with root.
- Recur into left or right subtree until split point.

**Time Complexity:** O(h)
**Space Complexity:** O(h) — recursion stack

### 2. Iterative
- Start from root.
- Move left/right based on `p` and `q`.
- Stop when split point found.

**Time Complexity:** O(h)
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

public class LCAinBST {

    // Recursive Approach
    public static TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null) return null;

        if (p.val < root.val && q.val < root.val) {
            return lowestCommonAncestor(root.left, p, q);
        } else if (p.val > root.val && q.val > root.val) {
            return lowestCommonAncestor(root.right, p, q);
        } else {
            return root; // split point
        }
    }

    // Iterative Approach
    public static TreeNode lowestCommonAncestorIterative(TreeNode root, TreeNode p, TreeNode q) {
        TreeNode curr = root;

        while (curr != null) {
            if (p.val < curr.val && q.val < curr.val) {
                curr = curr.left;
            } else if (p.val > curr.val && q.val > curr.val) {
                curr = curr.right;
            } else {
                return curr; // split point
            }
        }

        return null;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach       | Time Complexity | Space Complexity | Remarks             |
|----------------|----------------|-----------------|-------------------|
| Recursive      | O(h)           | O(h)            | h = height of BST  |
| Iterative      | O(h)           | O(1)            | Preferred for large trees |

---

## 🔹 Edge Cases
- **Empty BST** → return null
- **One node is ancestor of the other** → return ancestor node
- **Both nodes are same** → return that node
- **Nodes not in BST** → behavior undefined (assume both exist)

---

## 🔹 Follow-Up Questions
1. How to find LCA in a **Binary Tree (not BST)**?
2. Can you implement **iterative LCA** for Binary Tree?
3. How to find LCA **without recursion and parent pointers**?
4. How to find LCA for **multiple queries efficiently**?
5. Can this approach be adapted for **BST with duplicates**?
