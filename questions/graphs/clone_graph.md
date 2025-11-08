# Clone Graph

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
You are given a reference to a node in a **connected undirected graph**.  
Each node contains a value (`int val`) and a list of its neighbors.

Return a **deep copy (clone)** of the graph.

The cloned graph should have:
- The **same structure** as the original graph.
- Each node and edge **copied exactly once** (no shared references).

---

## 🔹 Examples

**Example 1:**  
Input: `adjList = [[2,4],[1,3],[2,4],[1,3]]`  
Output: `[[2,4],[1,3],[2,4],[1,3]]`  
Explanation: Node 1 is connected to 2 and 4, Node 2 is connected to 1 and 3, etc. The structure remains identical in the clone.

**Example 2:**  
Input: `adjList = [[]]`  
Output: `[[]]`  
Explanation: Single node with no neighbors.

**Example 3:**  
Input: `adjList = []`  
Output: `[]`  
Explanation: Empty graph.

---

## 🔹 Constraints
- The number of nodes in the given graph is in the range `[0, 100]`
- `1 <= Node.val <= 100`
- `Node.val` is unique for each node
- There are no repeated edges and no self-loops in the graph
- The Graph is connected and all nodes can be visited starting from the given node

---

## 🔹 Intuition & Logic
We must **copy all nodes and their connections** without duplicating any node reference.  
This can be seen as a **graph traversal + mapping** problem.

Steps:
1. For each node, create a **new copy**.
2. Maintain a **mapping** of `originalNode → clonedNode` to avoid re-creation.
3. Recursively or iteratively connect all neighbors using DFS or BFS.

Approaches:
- **DFS (recursive)** — easy and elegant.
- **BFS (iterative)** — avoids recursion stack overflow on large graphs.

---

## 🔹 Approaches

### 1. DFS Approach (Recursive)
- If a node is already cloned (in map), return it.
- Otherwise:
    - Clone the node.
    - Recursively clone all its neighbors.
    - Link them in the new graph.

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V) (recursion + hashmap)

---

### 2. BFS Approach (Iterative)
- Use a queue to traverse graph level by level.
- Clone nodes as they are encountered.
- For each node, clone all its neighbors if not already cloned.

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V)

---

## 🔹 Java Code (DFS and BFS)

```java
class Node {
    public int val;
    public List<Node> neighbors;

    public Node() {
        neighbors = new ArrayList<>();
    }

    public Node(int val) {
        this.val = val;
        neighbors = new ArrayList<>();
    }

    public Node(int val, List<Node> neighbors) {
        this.val = val;
        this.neighbors = neighbors;
    }
}

public class CloneGraph {

    // 1. DFS Approach
    private Map<Node, Node> map = new HashMap<>();

    public Node cloneGraphDFS(Node node) {
        if (node == null) return null;
        if (map.containsKey(node)) return map.get(node);

        Node clone = new Node(node.val);
        map.put(node, clone);

        for (Node neighbor : node.neighbors) {
            clone.neighbors.add(cloneGraphDFS(neighbor));
        }

        return clone;
    }

    // 2. BFS Approach
    public Node cloneGraphBFS(Node node) {
        if (node == null) return null;

        Map<Node, Node> map = new HashMap<>();
        Queue<Node> queue = new ArrayDeque<>();

        Node clone = new Node(node.val);
        map.put(node, clone);
        queue.add(node);

        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            for (Node neighbor : curr.neighbors) {
                if (!map.containsKey(neighbor)) {
                    map.put(neighbor, new Node(neighbor.val));
                    queue.add(neighbor);
                }
                map.get(curr).neighbors.add(map.get(neighbor));
            }
        }

        return clone;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|-----------|-----------------|------------------|
| DFS       | O(V + E)        | O(V)             |
| BFS       | O(V + E)        | O(V)             |

---

## 🔹 Edge Cases
- `node = null` → return `null`
- Graph with only one node
- Graph containing self-loops
- Graph with multiple edges or shared neighbors

---

## 🔹 Follow-Up Questions
1. How would you modify this to **clone a directed graph**?
2. How can you handle a **disconnected graph** efficiently?
3. Can you perform the cloning without using extra space (in-place marking)?
4. How can you extend this logic to **weighted or labeled graphs**?
