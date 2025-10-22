# 🔹 Problem: Maximum Path Sum from Any Node to Any Node

**Difficulty:** Hard ⚡⚡

---

## 🔹 Problem Statement
Given a **binary tree**, find the **maximum path sum** for paths starting and ending at **any two nodes**.

**Notes:**
- The path can start and end at **any node** in the tree.
- The path may pass through the **root or any intermediate nodes**.
- Nodes in the path must be **connected via parent-child links**.

---

## 🔹 Intuition
- For each node, compute the **maximum root-to-node path sum** for left and right subtrees.
- Maximum path **through current node** = `leftMax + node.val + rightMax`
- Update **global maximum** with this value.
- Return **max path sum ending at this node** to parent:  
  `node.val + max(leftMax, rightMax)`
- Use **post-order traversal** to compute left and right before current.

---

## 🔹 Approaches

### 1. Recursive Postorder with Global Variable
- Use a global variable `maxSum` to track maximum path sum.
- For each node:
    - Compute `leftMax` and `rightMax` recursively.
    - Update `maxSum = max(maxSum, leftMax + node.val + rightMax)`.
    - Return `node.val + max(leftMax, rightMax)` to parent.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

### 2. Recursive Using Pair Class (Optional)
- Return a pair of `(maxPathSum, maxRootToNodeSum)` from each recursion.
- Avoid global variable.

### 3. Iterative DFS (Advanced, Optional)
- Use a stack to simulate post-order traversal.
- Maintain maps for leftMax/rightMax.
- Update global max during node processing.

---

## 🔹 Java Code (Recursive Postorder)

```java
class Node {
    int val;
    Node left, right;
    Node(int val) { this.val = val; }
}

public class MaxAnyNodeToNodePathSum {

    private static int maxSum;

    public static int maxPathSum(Node root) {
        maxSum = Integer.MIN_VALUE;
        maxPathDown(root);
        return maxSum;
    }

    private static int maxPathDown(Node node) {
        if (node == null) return 0;

        int leftMax = Math.max(0, maxPathDown(node.left));   // ignore negative paths
        int rightMax = Math.max(0, maxPathDown(node.right));

        maxSum = Math.max(maxSum, leftMax + node.val + rightMax);

        return node.val + Math.max(leftMax, rightMax);
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                   | Time Complexity | Space Complexity | Remarks |
|-----------------------------|----------------|-----------------|---------|
| Recursive Postorder         | O(n)           | O(h)             | h = tree height |
| Recursive with Pair Class   | O(n)           | O(h)             | Avoids global variable |
| Iterative DFS (Optional)    | O(n)           | O(n)             | Stack + map simulation |

---

## 🔹 Edge Cases
- **Empty tree** → return `Integer.MIN_VALUE`
- **Single node tree** → path sum = node value
- **Negative values** → handled by ignoring negative contributions
- **Tree with multiple negative nodes** → path may include single node only
- **All nodes negative** → max path = max node value

---

## 🔹 Follow-Up Questions
1. How would you **return the actual path** along with sum?
2. Can you adapt it for **N-ary trees**?
3. How to implement **iteratively without recursion**?
4. How to handle **very large trees** efficiently?
5. How does handling **negative values** change the return value?
