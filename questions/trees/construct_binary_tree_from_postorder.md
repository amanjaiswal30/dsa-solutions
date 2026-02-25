# 🔹 Problem: Construct Binary Tree from Inorder and Postorder Traversal

![](../../assets/images/trees/construct_binary_tree_from_postorder.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given two arrays:
1. `inorder` — the inorder traversal of a binary tree
2. `postorder` — the postorder traversal of the same tree

Construct and return the **binary tree**.

**Notes:**
- Postorder: Left → Right → Root
- Inorder: Left → Root → Right

---

## 🔹 Intuition
- The **last element of postorder** is always the **root**.
- Find the root's index in inorder array → splits inorder into left and right subtrees.
- Recur for **left and right subtrees** with appropriate slices of inorder and postorder arrays.
- Keep a **postIndex** starting from the end of postorder array.

---

## 🔹 Approaches

### 1. Recursive with Index Mapping
- Create a **map of inorder values to indices** for O(1) lookup.
- Maintain a **postorder index** pointer starting at last element.
- Recur for **right subtree first**, then left subtree (since postorder is Left → Right → Root).

**Time Complexity:** O(n)  
**Space Complexity:** O(n) — for hashmap + recursion stack

### 2. Iterative (Advanced / Optional)
- Less common; uses stack to simulate postorder construction.

---

## 🔹 Java Code (Recursive Approach)

```java
import java.util.*;

class Node {
    int val;
    Node left, right;
    Node(int val) { this.val = val; }
}

public class BuildTreeInPost {

    private static int postIndex;

    public static Node buildTree(int[] inorder, int[] postorder) {
        postIndex = postorder.length - 1;
        Map<Integer, Integer> inorderMap = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            inorderMap.put(inorder[i], i);
        }
        return build(postorder, inorderMap, 0, inorder.length - 1);
    }

    private static Node build(int[] postorder, Map<Integer, Integer> inorderMap, int inStart, int inEnd) {
        if (inStart > inEnd) return null;

        int rootVal = postorder[postIndex--];
        Node root = new Node(rootVal);

        int inIndex = inorderMap.get(rootVal);

        // Recur for right first, then left
        root.right = build(postorder, inorderMap, inIndex + 1, inEnd);
        root.left = build(postorder, inorderMap, inStart, inIndex - 1);

        return root;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach               | Time Complexity | Space Complexity | Remarks |
|------------------------|----------------|-----------------|---------|
| Recursive with Map      | O(n)           | O(n)             | Map for inorder indices + recursion stack |
| Iterative (Advanced)    | O(n)           | O(n)             | Uses stack |

---

## 🔹 Edge Cases
- **Empty arrays** → return `null`
- **Arrays of length 1** → single node tree
- **Invalid arrays** → not a valid binary tree → may throw exception or return partial tree
- **All nodes in one side (skewed tree)** → recursion still works

---

## 🔹 Follow-Up Questions
1. Can you construct from **preorder and inorder** instead?
2. Can this be implemented **iteratively**?
3. How to handle **duplicate values**?
4. Can you **construct tree with O(1) extra space** if recursion stack allowed?
5. How to **validate inorder and postorder arrays** before construction?
