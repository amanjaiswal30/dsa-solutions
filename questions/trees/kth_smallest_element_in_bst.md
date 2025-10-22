# 🔹 Problem: Kth Smallest Element in a Binary Search Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **Binary Search Tree (BST)** and an integer `k`, return the **kth smallest element** in the BST.

- BST property ensures that **inorder traversal** produces elements in **ascending order**.

---

## 🔹 Intuition
- Perform **inorder traversal** to get sorted order.
- Keep a **counter** to track how many nodes have been visited.
- Return the **kth visited node**.

- Can be implemented **recursively** or **iteratively using stack**.

---

## 🔹 Approaches

### 1. Recursive Inorder Traversal
- Traverse left → visit node → traverse right.
- Keep count and return when kth node is reached.

**Time Complexity:** O(h + k)  
**Space Complexity:** O(h) — recursion stack

### 2. Iterative Inorder Traversal
- Use **stack** to simulate recursion.
- Pop nodes from stack, count visits.
- Stop when kth node is reached.

**Time Complexity:** O(h + k)  
**Space Complexity:** O(h) — stack

### 3. Augmented BST (Follow-Up)
- Store **size of subtree** at each node.
- At each node, determine whether kth element is in left subtree, current node, or right subtree.

**Time Complexity:** O(h)  
**Space Complexity:** O(1) extra (if sizes maintained)

---

## 🔹 Java Code (Recursive & Iterative)

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

public class KthSmallestBST {

    private static int count = 0;
    private static int result = 0;

    // Recursive Approach
    public static int kthSmallestRecursive(TreeNode root, int k) {
        count = 0;
        result = 0;
        inorder(root, k);
        return result;
    }

    private static void inorder(TreeNode node, int k) {
        if (node == null) return;

        inorder(node.left, k);

        count++;
        if (count == k) {
            result = node.val;
            return;
        }

        inorder(node.right, k);
    }

    // Iterative Approach
    public static int kthSmallestIterative(TreeNode root, int k) {
        Stack<TreeNode> stack = new Stack<>();
        TreeNode curr = root;
        int count = 0;

        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }

            curr = stack.pop();
            count++;
            if (count == k) return curr.val;

            curr = curr.right;
        }

        return -1; // k is invalid
    }
}
```
---

## 🔹 Complexity Analysis

| Approach       | Time Complexity | Space Complexity | Remarks                     |
|----------------|----------------|-----------------|-----------------------------|
| Recursive      | O(h + k)       | O(h)            | h = height of BST           |
| Iterative      | O(h + k)       | O(h)            | Stack simulates recursion   |
| Augmented BST  | O(h)           | O(1) extra      | Needs size info at each node |

---

## 🔹 Edge Cases
- **Empty BST** → return -1 or error
- **k > number of nodes** → invalid input → return -1
- **k = 1** → smallest element (leftmost node)
- **k = n** → largest element (rightmost node)
- **Skewed BST** → behaves like sorted array traversal

---

## 🔹 Follow-Up Questions
1. How to find **kth largest element**?
2. Can you maintain **running kth smallest** for dynamic inserts/deletes?
3. How to optimize if **multiple kth queries** are given?
4. Can we implement **O(1) space solution** using Morris Traversal?
5. How to extend for **duplicates** in BST?
