# 🔹 Problem: Left View of a Binary Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return the **left view** of the tree.

The **left view** of a binary tree contains the nodes that are **visible when the tree is viewed from the left side**. In other words, it contains the **first node of each level** of the tree.

---

## 🔹 Intuition
- The left view can be obtained by **level order traversal** or **recursive DFS**.
- **Observation:** At each level, the **first node encountered** is part of the left view.
- Two common approaches:
    1. **Level Order Traversal (BFS)** — track first node at each level
    2. **DFS (Preorder with Level Tracking)** — track maximum level visited

---

## 🔹 Approaches

### 1. Level Order Traversal (BFS)
- Use a queue to traverse the tree level by level.
- At each level, pick the **first node** and add it to the result.

**Time Complexity:** O(n)  
**Space Complexity:** O(w) — width of the tree (queue)

---

### 2. Recursive DFS (Preorder with Level Tracking)
- Traverse **root → left → right**.
- Maintain the **current level** and **maximum level visited**.
- If current level > maximum level visited → add node to result.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack (h = height of tree)

---

## 🔹 Java Code (Both Approaches)

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

public class LeftViewBinaryTree {

    // 1. Level Order Traversal (BFS)
    public static List<Integer> leftViewBFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                if (i == 0) result.add(node.val); // first node of this level
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
        }

        return result;
    }

    // 2. Recursive DFS (Preorder with Level Tracking)
    public static List<Integer> leftViewDFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        leftViewHelper(root, result, 1, new int[]{0});
        return result;
    }

    private static void leftViewHelper(TreeNode node, List<Integer> result, int level, int[] maxLevel) {
        if (node == null) return;

        if (level > maxLevel[0]) {
            result.add(node.val);
            maxLevel[0] = level;
        }

        leftViewHelper(node.left, result, level + 1, maxLevel);
        leftViewHelper(node.right, result, level + 1, maxLevel);
    }
}
```

---

## 🔹 Complexity Analysis

| Approach            | Time Complexity | Space Complexity |
|---------------------|-----------------|------------------|
| BFS (Level Order)   | O(n)            | O(w)             |
| DFS (Preorder)      | O(n)            | O(h)             |

---

## 🔹 Edge Cases
- Empty tree → returns empty list `[]`
- Single node → returns `[root.val]`
- Skewed tree (all left/right) → returns all nodes for left skew, only leftmost for right skew
- Balanced tree → first node of each level included

---

## 🔹 Follow-Up Questions
1. Can you compute the **right view** similarly?
2. Can you compute **top view** and **bottom view** using BFS?
3. How would you modify DFS to find **right view** instead of left view?
