# 🔹 Problem: Vertical Order Traversal of a Binary Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return the **vertical order traversal** of its nodes' values.

**Rules:**
1. Nodes are grouped by **vertical columns** (x-coordinate).
2. Columns are ordered from **leftmost to rightmost**.
3. Within a column, nodes are ordered from **top to bottom**.
4. If two nodes share the same row and column, order them by **node value**.

---

## 🔹 Intuition
- Assign **horizontal distance (HD)** to each node:
    - Root → HD = 0
    - Left child → HD = parent HD - 1
    - Right child → HD = parent HD + 1
- Use **level-order traversal (BFS)** to maintain top-to-bottom order.
- Group nodes by HD in a **map** (TreeMap to keep sorted order).

---

## 🔹 Approaches

### 1. BFS with Map
- Use a `TreeMap<Integer, List<Integer>>` to store nodes by HD.
- Use a queue to traverse tree **level by level**, storing `(node, HD)`.
- Add nodes to corresponding HD list in map.
- Collect map values for final vertical order.

**Time Complexity:** O(n log k)
- n = number of nodes
- k = number of columns (TreeMap insertion)

**Space Complexity:** O(n)

---

### 2. DFS with Map
- Perform **preorder traversal**.
- Pass **HD** and **level** in recursion.
- Use `TreeMap<HD, List<Pair<level, value>>>` to store nodes.
- Sort nodes in each column by level (and value if tie).

**Time Complexity:** O(n log n)  
**Space Complexity:** O(n)

---

## 🔹 Java Code (BFS Approach)

```java
import java.util.*;

class Node {
    int val;
    Node left, right;
    Node(int val) { this.val = val; }
}

public class VerticalOrderTraversal {

    public static List<List<Integer>> verticalOrder(Node root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        // TreeMap to store nodes by horizontal distance
        TreeMap<Integer, List<Integer>> map = new TreeMap<>();
        Queue<Pair<Node, Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(root, 0));

        while (!queue.isEmpty()) {
            Pair<Node, Integer> p = queue.poll();
            Node node = p.getKey();
            int hd = p.getValue();

            map.putIfAbsent(hd, new ArrayList<>());
            map.get(hd).add(node.val);

            if (node.left != null) queue.add(new Pair<>(node.left, hd - 1));
            if (node.right != null) queue.add(new Pair<>(node.right, hd + 1));
        }

        for (List<Integer> list : map.values()) {
            result.add(list);
        }

        return result;
    }

    // Simple Pair class for convenience
    static class Pair<K,V> {
        private K key;
        private V value;
        public Pair(K k, V v) { key = k; value = v; }
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity | Remarks |
|----------|----------------|-----------------|---------|
| BFS + TreeMap | O(n log k) | O(n) | n = number of nodes, k = number of vertical columns |
| DFS + TreeMap | O(n log n) | O(n) | Sort nodes by level and value in each column |

---

## 🔹 Edge Cases
- **Empty tree** → return `[]`
- **Single node tree** → return `[[root.val]]`
- **Left-skewed tree** → single node per column
- **Right-skewed tree** → single node per column
- **Multiple nodes in same position** → sort by value if using DFS approach

---

## 🔹 Follow-Up Questions
1. Can you **print bottom-up vertical order**?
2. How would you modify for **top view or bottom view** of a tree?
3. Can you solve **without using TreeMap**, just using min/max HD and a list of lists?
4. How would you handle **N-ary trees**?
5. Can this be optimized to **O(n)** time using BFS only?
