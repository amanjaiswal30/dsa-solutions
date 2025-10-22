# 🔹 Problem: All Root to Leaf Paths

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return **all root-to-leaf paths** in the tree.

**Notes:**
- Each path should be represented as a **list of node values** from root to leaf.
- A **leaf** is a node with no children.

---

## 🔹 Intuition
- Perform **DFS traversal** from root.
- Maintain a **current path list**.
- On reaching a **leaf node**, add a copy of the current path to the result.
- Backtrack by removing the last node when returning from recursion.

---

## 🔹 Approaches

### 1. Recursive DFS
- Start from root with empty path.
- For each node:
    - Add node value to path.
    - If leaf → add path copy to result.
    - Recur for left and right children.
    - Remove node value from path (backtracking).

**Time Complexity:** O(n)
- Each node visited once.  
  **Space Complexity:** O(h + p)
- h = recursion stack height, p = space for paths.

### 2. Iterative DFS (Optional)
- Use a stack storing `(node, pathList)` pairs.
- Pop node, update path, push children with updated path.
- Leaf nodes → add path to result.

---

## 🔹 Java Code (Recursive Approach)

```java
import java.util.*;

class Node {
    int val;
    Node left, right;
    Node(int val) { this.val = val; }
}

public class AllRootToLeafPaths {

    public static List<List<Integer>> allPaths(Node root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        List<Integer> path = new ArrayList<>();
        dfs(root, path, result);
        return result;
    }

    private static void dfs(Node node, List<Integer> path, List<List<Integer>> result) {
        if (node == null) return;

        path.add(node.val);

        // If leaf node
        if (node.left == null && node.right == null) {
            result.add(new ArrayList<>(path));
        } else {
            dfs(node.left, path, result);
            dfs(node.right, path, result);
        }

        path.remove(path.size() - 1); // Backtrack
    }
}
```

---

## 🔹 Complexity Analysis

| Approach       | Time Complexity | Space Complexity | Remarks |
|----------------|----------------|-----------------|---------|
| Recursive DFS  | O(n)           | O(h + p)        | h = tree height, p = total path storage |
| Iterative DFS  | O(n)           | O(h + p)        | Stack stores node + path pairs |

---

## 🔹 Edge Cases
- **Empty tree** → return `[]`
- **Single node tree** → return `[[root.val]]`
- **Left-skewed tree** → single path
- **Right-skewed tree** → single path
- **Multiple paths** → all should be returned

---

## 🔹 Follow-Up Questions
1. How can you **return paths as strings** instead of lists?
2. Can this be adapted for **N-ary trees**?
3. How to handle **very large trees** efficiently?
4. Can you implement **iteratively using stack** without recursion?
5. How to **filter paths** based on sum or length constraints?
