# 🔹 Problem: Determine Height of a Binary Tree

**Difficulty:** Easy 🌱

---

## 🔹 Problem Statement
Given the root of a **binary tree**, determine its **height** (also known as the **maximum depth**).

The height of a binary tree is the **number of nodes along the longest path** from the root node down to the **farthest leaf node**.  
If the tree is empty, its height is 0.

---

## 🔹 Intuition
- The height of a tree can be determined **recursively**:
    - Height of a node = `1 + max(height of left subtree, height of right subtree)`
- Base case: if the node is `null`, return `0`.
- This uses a **post-order traversal** since we need information from both subtrees first.

---

## 🔹 Approaches

### 1. Recursive (DFS)
- Recursively compute the height of the left and right subtrees.
- Return `1 + max(leftHeight, rightHeight)`.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — due to recursion stack (h = height of tree)

---

### 2. Level Order (BFS)
- Use a **queue** to traverse level by level.
- Count the number of levels — that’s the height of the tree.

**Time Complexity:** O(n)  
**Space Complexity:** O(n) — for queue storage

---

## 🔹 Java Code (Both Approaches)

```java
// Definition for a binary tree node.
class TreeNode {
    int val;
    TreeNode left, right;

    TreeNode(int val) {
        this.val = val;
        left = right = null;
    }
}

public class HeightOfBinaryTree {

    // 1. Recursive (DFS)
    public static int heightDFS(TreeNode root) {
        if (root == null) return 0;
        int left = heightDFS(root.left);
        int right = heightDFS(root.right);
        return 1 + Math.max(left, right);
    }

    // 2. Level Order (BFS)
    public static int heightBFS(TreeNode root) {
        if (root == null) return 0;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int height = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
            height++;
        }
        return height;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach          | Time Complexity | Space Complexity |
|-------------------|-----------------|------------------|
| Recursive (DFS)   | O(n)            | O(h)             |
| Level Order (BFS) | O(n)            | O(n)             |

---

## 🔹 Edge Cases
- Empty tree (`root = null`) → height = 0
- Single node → height = 1
- Completely skewed tree (all left or all right) → height = n
- Perfectly balanced tree → height = log₂(n + 1)

---

## 🔹 Follow-Up Questions
1. Can you compute the **height iteratively using a stack** (DFS without recursion)?
2. How would you modify the function to return **diameter** (longest path between two leaves)?
3. Can you determine if the tree is **balanced** using the height function?
