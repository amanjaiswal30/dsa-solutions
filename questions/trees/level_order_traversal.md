# 🔹 Problem: Level Order Traversal of a Binary Tree

**Difficulty:** Easy 🌱

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return the **level order traversal** of its nodes’ values.

In a level order traversal, nodes are visited **level by level from top to bottom** and **left to right** at each level.

---

## 🔹 Intuition
- Level order traversal is essentially **Breadth-First Search (BFS)** on a binary tree.
- Use a **queue** to keep track of nodes at the current level.
- Can also be used to solve problems like **left/right view**, **zigzag traversal**, or **height of a tree**.

---

## 🔹 Approaches

### 1. Iterative BFS Using Queue
- Initialize a queue and add the root.
- While the queue is not empty:
    - Iterate over nodes of the current level (queue size at the start of iteration).
    - Add children of nodes to the queue for the next level.

**Time Complexity:** O(n)  
**Space Complexity:** O(w) — width of the tree (queue)

### 2. Recursive DFS (Level Tracking)
- Traverse the tree recursively.
- Maintain a **list of lists**, each representing a level.
- Add nodes to their corresponding level list.

**Time Complexity:** O(n)  
**Space Complexity:** O(h + n) — recursion stack + output

---

## 🔹 Java Code (Iterative BFS)

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

public class LevelOrderTraversal {

    // 1. Iterative BFS
    public static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Integer> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node.val);

                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }

            result.add(currentLevel);
        }

        return result;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach       | Time Complexity | Space Complexity |
|----------------|----------------|-----------------|
| BFS (Queue)    | O(n)           | O(w)            |
| DFS (Recursive)| O(n)           | O(h + n)        |

---

## 🔹 Edge Cases
- Empty tree → returns `[]`
- Single node → returns `[[root.val]]`
- Skewed tree → each level contains only one node
- Balanced tree → levels contain nodes from left to right

---

## 🔹 Follow-Up Questions
1. How would you implement **zigzag level order traversal**?
2. How can you use level order traversal to find **maximum width** of the tree?
3. Can you combine **level order** with **level sum** computation efficiently?
