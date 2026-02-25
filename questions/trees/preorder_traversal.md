# 🔹 Problem: Preorder Traversal of a Binary Tree

![](../../assets/images/trees/preorder_traversal.svg)


**Difficulty:** Easy 🌱

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return the **preorder traversal** of its nodes’ values.

In a preorder traversal, you visit:
1. Root node
2. Left subtree
3. Right subtree

---

## 🔹 Intuition
- The **preorder traversal** is useful when you need to **copy** or **serialize** a tree, because it visits the root before its children.
- There are multiple ways to perform preorder traversal:
    1. **Recursive**
    2. **Iterative** using a stack
    3. **Morris Traversal** (O(1) space)

---

## 🔹 Approaches

### 1. Recursive Approach
- Visit the root node.
- Recursively visit the left subtree.
- Recursively visit the right subtree.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack (h = height of the tree)

---

### 2. Iterative Approach (Using Stack)
- Use a stack to simulate recursion.
- Push right child first, then left child (so left is processed first).

**Time Complexity:** O(n)  
**Space Complexity:** O(h)

---

### 3. Morris Traversal (Without Stack or Recursion)
- Use threaded binary tree technique.
- Connect the predecessor’s right pointer to the current node.
- Restore the links while traversing.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code (All 3 Approaches)

```java
import java.util.*;

class TreeNode {
    int val;
    TreeNode left, right;

    TreeNode(int val) {
        this.val = val;
        left = right = null;
    }
}

public class PreorderTraversal {

    // 1. Recursive Approach
    public static List<Integer> preorderRecursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        preorderHelper(root, result);
        return result;
    }

    private static void preorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        result.add(node.val);
        preorderHelper(node.left, result);
        preorderHelper(node.right, result);
    }

    // 2. Iterative Approach (Using Stack)
    public static List<Integer> preorderIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            result.add(node.val);

            if (node.right != null) stack.push(node.right);
            if (node.left != null) stack.push(node.left);
        }

        return result;
    }

    // 3. Morris Traversal (O(1) space)
    public static List<Integer> preorderMorris(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        TreeNode curr = root;

        while (curr != null) {
            if (curr.left == null) {
                result.add(curr.val);
                curr = curr.right;
            } else {
                TreeNode pre = curr.left;
                while (pre.right != null && pre.right != curr) {
                    pre = pre.right;
                }

                if (pre.right == null) {
                    result.add(curr.val); // Add before creating the thread
                    pre.right = curr;
                    curr = curr.left;
                } else {
                    pre.right = null;
                    curr = curr.right;
                }
            }
        }

        return result;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach            | Time Complexity | Space Complexity |
|---------------------|-----------------|------------------|
| Recursive           | O(n)            | O(h)             |
| Iterative (Stack)   | O(n)            | O(h)             |
| Morris Traversal    | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Empty tree → returns empty list `[]`
- Single node → returns `[root.val]`
- Skewed tree (all left/right) → linear traversal
- Balanced tree → typical preorder sequence

---

## 🔹 Follow-Up Questions
1. How does **preorder** differ from **inorder** and **postorder** traversal?
2. Can you use preorder traversal to **serialize and deserialize** a binary tree?
3. Can you write a **postorder traversal** using just one stack (iteratively)?
