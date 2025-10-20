# 🔹 Problem: Populating Next Right Pointers in Each Node

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a **perfect binary tree** (all leaves are on the same level, and every parent has two children), populate each node's **next** pointer to point to its **next right node**.
- If there is no next right node, the next pointer should be set to `null`.
- Initially, all next pointers are `null`.

---

## 🔹 Intuition
- Since it’s a **perfect binary tree**, we can connect nodes **level by level**.
- For each node:
    1. Connect `node.left.next = node.right`
    2. Connect `node.right.next = node.next.left` (if `node.next` exists)
- Can be done **recursively** or **iteratively** using level pointers.

---

## 🔹 Approaches

### 1. Recursive Approach
- Start from the root.
- Connect left and right children.
- Connect right child to `node.next.left` if `node.next` exists.
- Recur for left and right subtrees.

**Time Complexity:** O(n)  
**Space Complexity:** O(h) — recursion stack

### 2. Iterative Approach (Using Level Pointers)
- Use the already established next pointers to traverse each level.
- Start from the leftmost node of each level.
- Connect children nodes without using extra queue space.

**Time Complexity:** O(n)  
**Space Complexity:** O(1) — constant space

### 3. BFS Using Queue
- Use a queue to perform **level order traversal**.
- Connect nodes in the same level using queue order.
- Useful for general binary trees as well.

**Time Complexity:** O(n)  
**Space Complexity:** O(w) — queue width

---

## 🔹 Java Code (All Approaches)

```java
import java.util.*;

class Node {
    public int val;
    public Node left;
    public Node right;
    public Node next;

    public Node() {}
    public Node(int val) { this.val = val; }
    public Node(int val, Node left, Node right, Node next) {
        this.val = val;
        this.left = left;
        this.right = right;
        this.next = next;
    }
}

public class ConnectNextRightPointers {

    // 1. Recursive Approach
    public static Node connectRecursive(Node root) {
        if (root == null) return null;

        if (root.left != null) {
            root.left.next = root.right;
            if (root.next != null) {
                root.right.next = root.next.left;
            }
        }

        connectRecursive(root.left);
        connectRecursive(root.right);

        return root;
    }

    // 2. Iterative Approach using Level Pointers
    public static Node connectIterative(Node root) {
        if (root == null) return null;

        Node leftMost = root;

        while (leftMost.left != null) {
            Node head = leftMost;

            while (head != null) {
                // Connect left → right
                head.left.next = head.right;

                // Connect right → next left
                if (head.next != null) {
                    head.right.next = head.next.left;
                }

                head = head.next;
            }

            leftMost = leftMost.left;
        }

        return root;
    }

    // 3. BFS Using Queue (General Approach)
    public static Node connectBFS(Node root) {
        if (root == null) return null;

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            Node prev = null;

            for (int i = 0; i < levelSize; i++) {
                Node curr = queue.poll();

                if (prev != null) {
                    prev.next = curr;
                }
                prev = curr;

                if (curr.left != null) queue.add(curr.left);
                if (curr.right != null) queue.add(curr.right);
            }
            prev.next = null; // Last node of level points to null
        }

        return root;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach           | Time Complexity | Space Complexity |
|-------------------|----------------|-----------------|
| Recursive          | O(n)           | O(h)            |
| Iterative (Level)  | O(n)           | O(1)            |
| BFS (Queue)        | O(n)           | O(w)            |

---

## 🔹 Edge Cases
- Empty tree → return `null`
- Single node → `next` pointer remains `null`
- Perfect binary tree → all levels except last are fully connected

---

## 🔹 Follow-Up Questions
1. How would you modify the solution for a **general binary tree** (not perfect)?
2. Can this be solved **iteratively without using extra queue space**?
3. How does using the `next` pointer help in level order traversal without a queue?
