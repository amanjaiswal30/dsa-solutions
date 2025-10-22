# 🔹 Problem: Check if a Binary Tree is BST

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, determine whether it is a **Binary Search Tree (BST)**.

A **BST** is defined as a binary tree where:
- The **left subtree** of a node contains only nodes with values **less than** the node’s value.
- The **right subtree** contains only nodes with values **greater than** the node’s value.
- Both left and right subtrees must also be **BSTs**.

---

## 🔹 Intuition
- The property must hold **for every node**, not just its immediate children.
- For each node, ensure:

min(left subtree) < node.val < max(right subtree)

- There are multiple ways to verify this:
1. **Recursive range checking** — pass valid value ranges down the tree.
2. **Inorder traversal** — values of a BST are always in **sorted order**.

---

## 🔹 Approaches

### 1. Recursive Range Check (Optimized)
- Pass a `min` and `max` range for each node.
- A node’s value must satisfy: `min < node.val < max`.
- Recur for left and right children with updated bounds.

**Time Complexity:** O(n)  
**Space Complexity:** O(h)

---

### 2. Inorder Traversal Check
- Perform inorder traversal (Left → Root → Right).
- The values should appear in **strictly increasing order**.
- If any value violates the sorted order → not a BST.

**Time Complexity:** O(n)  
**Space Complexity:** O(h)

---

### 3. Naïve Approach (Using Subtree Min/Max)
- For each node:
- Compute `max` in left subtree and `min` in right subtree.
- Check if node’s value is within that range.
- Repeated subtree scans → **O(n²)**.

**Time Complexity:** O(n²)  
**Space Complexity:** O(h)

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

public class CheckBST {

  // 1. Recursive Range Check (Optimized)
  public static boolean isBST(Node root) {
      return isBSTUtil(root, Long.MIN_VALUE, Long.MAX_VALUE);
  }

  private static boolean isBSTUtil(Node node, long min, long max) {
      if (node == null) return true;

      if (node.val <= min || node.val >= max)
          return false;

      return isBSTUtil(node.left, min, node.val)
          && isBSTUtil(node.right, node.val, max);
  }

  // 2. Inorder Traversal Check
  static long prev = Long.MIN_VALUE;

  public static boolean isBSTInorder(Node root) {
      if (root == null) return true;

      if (!isBSTInorder(root.left)) return false;

      if (root.val <= prev) return false;
      prev = root.val;

      return isBSTInorder(root.right);
  }

  // 3. Naïve Recursive Approach (O(n²))
  public static boolean isBSTNaive(Node root) {
      if (root == null) return true;

      if (root.left != null && maxValue(root.left) >= root.val)
          return false;

      if (root.right != null && minValue(root.right) <= root.val)
          return false;

      return isBSTNaive(root.left) && isBSTNaive(root.right);
  }

  private static int minValue(Node node) {
      int minv = node.val;
      while (node.left != null) {
          node = node.left;
          minv = node.val;
      }
      return minv;
  }

  private static int maxValue(Node node) {
      int maxv = node.val;
      while (node.right != null) {
          node = node.right;
          maxv = node.val;
      }
      return maxv;
  }
}
```
---

## 🔹 Complexity Analysis

| Approach                | Time Complexity | Space Complexity | Remarks               |
|-------------------------|----------------|------------------|-----------------------|
| Range Check (Optimized) | O(n)           | O(h)             | Efficient & preferred |
| Inorder Traversal       | O(n)           | O(h)             | Easy to understand    |
| Naïve (Subtree Min/Max) | O(n²)          | O(h)             | Inefficient approach  |

---

## 🔹 Edge Cases
- **Empty tree** → return `true`
- **Single node** → return `true`
- **Duplicate values** → violate strict BST rule (no `<=` or `>=`)
- **Skewed tree** (increasing or decreasing order) → valid BST
- **Unbalanced but valid** → still BST
- **Deep violation** (not at immediate child level) → must detect and return `false`

---

## 🔹 Follow-Up Questions
1. How can you modify the function to **return the first node** where the BST property fails?
2. What if **duplicate values** are allowed — how will you define the BST rule?
3. Can you implement the **inorder traversal iteratively** using a stack?
4. How would you **validate a BST represented as an array** (like in heap form)?
5. How could you **convert a non-BST tree into a BST** by rearranging node values?

---
