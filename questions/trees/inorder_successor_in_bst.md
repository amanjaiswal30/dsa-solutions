# 🔹 Problem: Inorder Successor in a Binary Search Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a **Binary Search Tree (BST)** and a node `p`, find the **inorder successor** of `p` in the BST.

- **Inorder Successor** of a node `p` is the **node with the smallest key greater than `p.val`**.
- If `p` has **no successor**, return `null`.

---

## 🔹 Intuition
- BST inorder traversal gives nodes in **sorted order**.
- **Two cases**:
    1. `p` has a **right child** → successor = **leftmost node in right subtree**.
    2. `p` has **no right child** → successor = **lowest ancestor whose left child subtree contains `p`**.

- Can be implemented **recursively** or **iteratively** using BST properties.

---

## 🔹 Approaches

### 1. Iterative Using BST Property
- Start from root.
- Compare root with `p.val`:
    - If `p.val < root.val` → root could be successor → go left.
    - Else → go right.
- Keep track of last potential successor.

**Time Complexity:** O(h)  
**Space Complexity:** O(1)

### 2. Recursive Approach
- Similar logic: traverse left/right recursively.
- Keep track of successor in recursion.

**Time Complexity:** O(h)  
**Space Complexity:** O(h) — recursion stack

---

## 🔹 Java Code (Iterative)

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

public class InorderSuccessorBST {

    public static TreeNode inorderSuccessor(TreeNode root, TreeNode p) {
        TreeNode successor = null;
        TreeNode curr = root;

        while (curr != null) {
            if (p.val < curr.val) {
                successor = curr;
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }

        return successor;
    }

    // Recursive Approach
    public static TreeNode inorderSuccessorRecursive(TreeNode root, TreeNode p) {
        if (root == null) return null;

        if (p.val >= root.val) {
            return inorderSuccessorRecursive(root.right, p);
        } else {
            TreeNode left = inorderSuccessorRecursive(root.left, p);
            return (left != null) ? left : root;
        }
    }
}
```
---

## 🔹 Complexity Analysis

| Approach       | Time Complexity | Space Complexity | Remarks             |
|----------------|----------------|-----------------|-------------------|
| Iterative      | O(h)           | O(1)            | h = height of BST  |
| Recursive      | O(h)           | O(h)            | Recursion stack    |

---

## 🔹 Edge Cases
- **Empty BST** → return null
- **Node has no right child** → successor is ancestor
- **Node is largest in BST** → return null
- **Node not in BST** → assume node exists

---

## 🔹 Follow-Up Questions
1. How to find **inorder predecessor** in BST?
2. Can you find successor **without using extra space**?
3. How to find successor for **all nodes** efficiently?
4. How to handle **BST with duplicates**?
5. Can this approach be adapted for **Binary Tree (not BST)**?
