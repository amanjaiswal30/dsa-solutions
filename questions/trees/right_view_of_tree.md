# 🔹 Problem: Right View of a Binary Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return the **right view** of the tree.

The **right view** of a binary tree contains the nodes that are **visible when the tree is viewed from the right side**. In other words, it contains the **last node of each level** of the tree.

---

## 🔹 Intuition
- The right view can be obtained by **level order traversal** or **recursive DFS**.
- **Observation:** At each level, the **last node encountered** (BFS) or the **first node visited from right in DFS** is part of the right view.
- Two common approaches:
    1. **Level Order Traversal (BFS)** — track last node at each level
    2. **DFS (Preorder with Level Tracking, Right First)** — traverse **root → right → left** and track maximum level visited

---

## 🔹 Approaches

### 1. Level Order Traversal (BFS)
- Use a queue to traverse the tree level by level.
- At each level, pick the **last node** and add it to the result.

**Time Complexity:** O(n)  
**Space Complexity:** O(w) — width of the tree (queue)

---

### 2. Recursive DFS (Preorder with Level Tracking, Right First)
- Traverse **root → right → left**.
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

public class RightViewBinaryTree {

    // 1. Level Order Traversal (BFS)
    public static List<Integer> rightViewBFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                if (i == levelSize - 1) result.add(node.val); // last node of this level
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
        }

        return result;
    }

    // 2. Recursive DFS (Preorder with Level Tracking, Right First)
    public static List<Integer> rightViewDFS(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        rightViewHelper(root, result, 1, new int[]{0});
        return result;
    }

    private static void rightViewHelper(TreeNode node, List<Integer> result, int level, int[] maxLevel) {
        if (node == null) return;

        if (level > maxLevel[0]) {
            result.add(node.val);
            maxLevel[0] = level;
        }

        rightViewHelper(node.right, result, level + 1, maxLevel);
        rightViewHelper(node.left, result, level + 1, maxLevel);
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
- Skewed tree (all left/right) → returns all nodes for right skew, only rightmost for left skew
- Balanced tree → last node of each level included

---

## 🔹 Follow-Up Questions
1. Can you compute **left view** similarly?
2. Can you compute **top view** and **bottom view** using BFS?
3. How would you modify DFS to find **left view** instead of right view?
