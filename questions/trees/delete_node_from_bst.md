# 🔹 Problem: Delete a Node from a Binary Search Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **Binary Search Tree (BST)** and a value `key`, delete the node with value `key` in the BST.

- Return the **root of the updated BST**.
- BST must maintain the **inorder property**:
    - Left subtree < node < right subtree
- Three deletion cases:
    1. Node is a **leaf** → remove directly.
    2. Node has **one child** → replace node with child.
    3. Node has **two children** → replace node with **inorder successor** (smallest in right subtree) or **predecessor**.

---

## 🔹 Intuition
- Recursively search for the node to delete.
- Handle deletion cases:
    1. Leaf → return null.
    2. One child → return non-null child.
    3. Two children → replace with successor/predecessor, delete that successor/predecessor recursively.
- Return updated root at each recursion level.

---

## 🔹 Approaches

### 1. Recursive Deletion
- Recursively find node.
- Handle leaf/one-child/two-children cases.
- Return updated root.

**Time Complexity:** O(h)  
**Space Complexity:** O(h) — recursion stack

### 2. Iterative Deletion
- Use parent pointer to find node.
- Adjust child pointers according to deletion case.
- Avoid recursion stack.

**Time Complexity:** O(h)  
**Space Complexity:** O(1)

---

## 🔹 Java Code (Recursive)

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

public class DeleteBST {

    public static TreeNode deleteNode(TreeNode root, int key) {
        if (root == null) return null;

        if (key < root.val) {
            root.left = deleteNode(root.left, key);
        } else if (key > root.val) {
            root.right = deleteNode(root.right, key);
        } else {
            // Node found
            if (root.left == null) return root.right;    // One child or no child
            else if (root.right == null) return root.left;

            // Two children: find inorder successor
            TreeNode successor = findMin(root.right);
            root.val = successor.val;
            root.right = deleteNode(root.right, successor.val);
        }

        return root;
    }

    private static TreeNode findMin(TreeNode node) {
        while (node.left != null) node = node.left;
        return node;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach       | Time Complexity | Space Complexity | Remarks             |
|----------------|----------------|-----------------|-------------------|
| Recursive      | O(h)           | O(h)            | h = height of BST  |
| Iterative      | O(h)           | O(1)            | Avoid recursion    |

---

## 🔹 Edge Cases
- **Empty BST** → return null
- **Node not found** → BST remains unchanged
- **Node is leaf** → delete directly
- **Node has one child** → replace with child
- **Node has two children** → replace with inorder successor

---

## 🔹 Follow-Up Questions
1. Can you implement **iterative deletion** without recursion?
2. How to choose between **inorder successor or predecessor** for two children?
3. How to **delete all nodes with duplicates** if BST allows duplicates?
4. How does deletion affect **AVL or Red-Black Trees**?
5. Can you **delete the root node efficiently** without changing tree height drastically?
