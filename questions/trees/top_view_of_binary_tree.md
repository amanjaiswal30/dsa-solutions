# 🔹 Problem: Top View of a Binary Tree

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given the root of a **binary tree**, return the **top view** of the tree.

**Top View Definition:**
- Nodes visible when the tree is viewed from **above**.
- For each vertical column (horizontal distance, HD), the **first node encountered** from top to bottom is included.

---

## 🔹 Intuition
- Assign **horizontal distance (HD)** to each node:
    - Root → HD = 0
    - Left child → HD = parent HD - 1
    - Right child → HD = parent HD + 1
- Perform **level-order traversal (BFS)** to ensure **top-most node** at each HD is captured first.
- Store nodes in a **map** keyed by HD.
- Output values sorted by HD (leftmost to rightmost).

---

## 🔹 Approaches

### 1. BFS with Map
- Use a `TreeMap<Integer, Integer>` or `HashMap<Integer, Integer>` + track min/max HD.
- Queue stores `(node, HD)` pairs.
- For each node:
    - If HD not seen before → add node value to map.
    - Enqueue children with updated HD.
- Collect map values in HD order for top view.

**Time Complexity:** O(n)  
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

public class TopViewBinaryTree {

    public static List<Integer> topView(Node root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Map<Integer, Integer> map = new HashMap<>();
        Queue<Pair<Node, Integer>> queue = new LinkedList<>();
        int minHD = 0, maxHD = 0;

        queue.add(new Pair<>(root, 0));

        while (!queue.isEmpty()) {
            Pair<Node, Integer> p = queue.poll();
            Node node = p.getKey();
            int hd = p.getValue();

            if (!map.containsKey(hd)) {
                map.put(hd, node.val);
            }

            if (node.left != null) queue.add(new Pair<>(node.left, hd - 1));
            if (node.right != null) queue.add(new Pair<>(node.right, hd + 1));

            minHD = Math.min(minHD, hd);
            maxHD = Math.max(maxHD, hd);
        }

        for (int i = minHD; i <= maxHD; i++) {
            result.add(map.get(i));
        }

        return result;
    }

    // Simple Pair class
    static class Pair<K,V> {
        private K key; private V value;
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
| BFS + Map | O(n) | O(n) | Level-order ensures first node at each HD is captured |

---

## 🔹 Edge Cases
- **Empty tree** → return `[]`
- **Single node tree** → return `[root.val]`
- **Left-skewed tree** → all nodes visible
- **Right-skewed tree** → all nodes visible
- **Nodes overlapping vertically** → only top-most node included

---

## 🔹 Follow-Up Questions
1. How would you implement **bottom view** instead?
2. Can you optimize using **TreeMap** to avoid min/max tracking?
3. How to modify for **N-ary trees**?
4. Can this be done **recursively**?
5. How to print **both top and bottom view in one traversal**?
