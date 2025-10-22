# 🔹 Problem: Check if a Binary Tree is Sum Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a **binary tree**, determine whether it is a **Sum Tree**.  
A **Sum Tree** is a binary tree where the value of each non-leaf node is equal to the **sum of the values of its left and right subtrees**.

**Note:**
- An **empty tree** and a **leaf node** are considered **Sum Trees** by definition.

---

## 🔹 Intuition
- For every node, check if:

node.val == sum(left subtree) + sum(right subtree)

- A **naïve approach** would repeatedly compute subtree sums, leading to **O(n²)** time.
- Instead, use a **bottom-up postorder traversal**:
- Compute the subtree sum once.
- Return both the **sum** and **boolean flag** indicating whether it’s a Sum Tree.
- This way, we can determine in **O(n)** whether the entire tree satisfies the condition.

---

## 🔹 Approaches

### 1. Recursive (Optimized) Approach
- If the node is `null` or a leaf → return `true`.
- Recursively check for left and right subtrees.
- Compute:

totalSum = leftSum + rightSum

- If `node.val == totalSum` and both subtrees are Sum Trees → current node is valid.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

---

### 2. Naïve Recursive Approach (Recomputing Subtree Sums)
- For each node:
- Recursively calculate left and right subtree sums using a helper function.
- Check if `node.val == leftSum + rightSum`.
- Leads to **repeated sum calculations**, so it’s **O(n²)**.

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

public class SumTreeCheck {

  // 1. Optimized Recursive Approach
  static class Result {
      boolean isSumTree;
      int sum;
      Result(boolean isSumTree, int sum) {
          this.isSumTree = isSumTree;
          this.sum = sum;
      }
  }

  public static boolean isSumTree(Node root) {
      return helper(root).isSumTree;
  }

  private static Result helper(Node node) {
      if (node == null)
          return new Result(true, 0);

      if (node.left == null && node.right == null)
          return new Result(true, node.val);

      Result left = helper(node.left);
      Result right = helper(node.right);

      boolean isSum = left.isSumTree && right.isSumTree && 
                      (node.val == left.sum + right.sum);

      int totalSum = left.sum + right.sum + node.val;

      return new Result(isSum, totalSum);
  }

  // 2. Naïve Recursive Approach (O(n²))
  public static boolean isSumTreeNaive(Node root) {
      if (root == null || (root.left == null && root.right == null))
          return true;

      int leftSum = sum(root.left);
      int rightSum = sum(root.right);

      return (root.val == leftSum + rightSum) 
              && isSumTreeNaive(root.left)
              && isSumTreeNaive(root.right);
  }

  private static int sum(Node node) {
      if (node == null) return 0;
      return node.val + sum(node.left) + sum(node.right);
  }
}
```

---

## 🔹 Complexity Analysis

| Approach              | Time Complexity | Space Complexity | Remarks               |
|-----------------------|----------------|------------------|-----------------------|
| Optimized (Postorder) | O(n)           | O(h)             | Efficient & preferred |
| Naïve (Recompute)     | O(n²)          | O(h)             | Redundant sums        |

---

## 🔹 Edge Cases
- **Empty tree** → return `true`
- **Single node** → return `true`
- **Any non-leaf node** violates the sum property → return `false`
- **Negative values** → still works fine since arithmetic remains valid

---

**Step-by-step check:**
1. For node `4` and `6` → leaf nodes → valid Sum Trees.
2. Node `10`: left + right = 4 + 6 = 10 ✅
3. Node `3`: right = 3, left = null → 0 + 3 = 3 ✅
4. Root `26`: left + right = 10 + 3 + 3 = 26 ✅  
   ✅ Entire tree is a **Sum Tree**.

---

## 🔹 Follow-Up Questions
1. How would you modify the code to **return the first node** that violates the property?
2. Can this approach be adapted for an **N-ary tree**?
3. What if the tree values include **negative integers** — does the logic still hold?
4. Can we solve this **iteratively** using a stack-based postorder traversal?

---
