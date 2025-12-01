# Topological Sort

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a **Directed Acyclic Graph (DAG)** with `V` vertices and `E` edges, find a **topological ordering** of the vertices.

A **topological ordering** is a linear arrangement of vertices such that for every directed edge `u → v`, vertex `u` comes before vertex `v` in the ordering.

**Note:** Topological sorting is only possible for DAGs (graphs with no cycles).

---

## 🔹 Examples

**Example 1:**  
Input: `V = 6, edges = [[5,2], [5,0], [4,0], [4,1], [2,3], [3,1]]`  
Output: `[5, 4, 2, 3, 1, 0]` (one possible topological order)  
Explanation: All edges respect the ordering (u comes before v for edge u→v).

**Example 2:**  
Input: `V = 4, edges = [[0,1], [1,2], [2,3]]`  
Output: `[0, 1, 2, 3]`  
Explanation: Linear chain has unique topological order.

**Example 3:**  
Input: `V = 2, edges = [[1,0]]`  
Output: `[1, 0]`  
Explanation: Simple directed edge from 1 to 0.

---

## 🔹 Constraints
- `1 <= V <= 10^5`
- `0 <= E <= 10^5`
- The graph is a **DAG (no cycles)**
- Vertices are labeled from `0` to `V-1`

---

## 🔹 Intuition & Logic
**Topological Sort** gives us a **dependency resolution order**. If A depends on B, then B must come before A in the topological order.

**Key Applications:**
- **Course Prerequisites** (Course Schedule problem)
- **Build Dependencies** (makefile, package managers)
- **Task Scheduling** with dependencies
- **Compiler** symbol resolution

**Two Main Approaches:**
1. **Kahn's Algorithm (BFS)** - Remove vertices with indegree 0
2. **DFS-based** - Use post-order traversal with stack

---

## 🔹 Approaches

### 1. Kahn's Algorithm (BFS-based)
- Calculate indegree for all vertices
- Add all vertices with indegree 0 to queue
- Process queue: remove vertex, decrease neighbors' indegrees
- Add neighbors with indegree 0 to queue
- Result is topological order

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V)

---

### 2. DFS-based Approach
- Perform DFS from each unvisited vertex
- Use post-order traversal (add to result after processing all neighbors)
- Reverse the final result to get topological order
- Alternative: Use stack during DFS

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V)

---

## 🔹 Java Code

```java
import java.util.*;

public class TopologicalSort {
    
    // 1. Kahn's Algorithm (BFS-based)
    public static List<Integer> topologicalSortKahn(int V, List<List<Integer>> adj) {
        int[] indegree = new int[V];
        
        // Calculate indegrees
        for (int i = 0; i < V; i++) {
            for (int neighbor : adj.get(i)) {
                indegree[neighbor]++;
            }
        }
        
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < V; i++) {
            if (indegree[i] == 0) {
                queue.offer(i);
            }
        }
        
        List<Integer> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            int node = queue.poll();
            result.add(node);
            
            // Reduce indegree of neighbors
            for (int neighbor : adj.get(node)) {
                indegree[neighbor]--;
                if (indegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // Check for cycle (if result size != V, cycle exists)
        if (result.size() != V) {
            return new ArrayList<>(); // Cycle detected, no topological order
        }
        
        return result;
    }
    
    // 2. DFS-based Approach (using Stack)
    public static List<Integer> topologicalSortDFS(int V, List<List<Integer>> adj) {
        boolean[] visited = new boolean[V];
        Stack<Integer> stack = new Stack<>();
        
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                dfs(adj, visited, stack, i);
            }
        }
        
        List<Integer> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        
        return result;
    }
    
    private static void dfs(List<List<Integer>> adj, boolean[] visited, Stack<Integer> stack, int node) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                dfs(adj, visited, stack, neighbor);
            }
        }
        
        stack.push(node); // Add to stack after processing all neighbors
    }
    
    // 3. DFS-based Approach (using List + Reverse)
    public static List<Integer> topologicalSortDFSReverse(int V, List<List<Integer>> adj) {
        boolean[] visited = new boolean[V];
        List<Integer> result = new ArrayList<>();
        
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                dfsReverse(adj, visited, result, i);
            }
        }
        
        Collections.reverse(result);
        return result;
    }
    
    private static void dfsReverse(List<List<Integer>> adj, boolean[] visited, List<Integer> result, int node) {
        visited[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                dfsReverse(adj, visited, result, neighbor);
            }
        }
        
        result.add(node); // Post-order: add after processing neighbors
    }
    
    // Helper method to build adjacency list
    public static List<List<Integer>> buildGraph(int V, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            adj.get(edge[0]).add(edge[1]);
        }
        return adj;
    }
    
    // Utility to print topological order
    public static void printTopologicalOrder(List<Integer> order) {
        if (order.isEmpty()) {
            System.out.println("No topological order possible (cycle detected)");
        } else {
            System.out.println("Topological Order: " + order);
        }
    }
    
    public static void main(String[] args) {
        // Test case 1: Standard DAG
        int[][] edges1 = {{5,2}, {5,0}, {4,0}, {4,1}, {2,3}, {3,1}};
        List<List<Integer>> adj1 = buildGraph(6, edges1);
        
        System.out.println("=== Test Case 1 ===");
        printTopologicalOrder(topologicalSortKahn(6, adj1));
        printTopologicalOrder(topologicalSortDFS(6, adj1));
        
        // Test case 2: Linear chain
        int[][] edges2 = {{0,1}, {1,2}, {2,3}};
        List<List<Integer>> adj2 = buildGraph(4, edges2);
        
        System.out.println("\n=== Test Case 2 ===");
        printTopologicalOrder(topologicalSortKahn(4, adj2));
        printTopologicalOrder(topologicalSortDFSReverse(4, adj2));
        
        // Test case 3: Single edge
        int[][] edges3 = {{1,0}};
        List<List<Integer>> adj3 = buildGraph(2, edges3);
        
        System.out.println("\n=== Test Case 3 ===");
        printTopologicalOrder(topologicalSortKahn(2, adj3));
        
        // Test case 4: Disconnected components
        int[][] edges4 = {{0,1}, {2,3}};
        List<List<Integer>> adj4 = buildGraph(4, edges4);
        
        System.out.println("\n=== Test Case 4 ===");
        printTopologicalOrder(topologicalSortDFS(4, adj4));
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|----------|-----------------|------------------|
| Kahn's Algorithm | O(V + E) | O(V) |
| DFS-based | O(V + E) | O(V) |

---

## 🔹 Edge Cases
- **Empty graph** → Any order of isolated vertices
- **Single vertex** → [vertex] is the only topological order
- **Linear chain** → Unique topological order
- **Multiple components** → Each component sorted independently
- **Graph with cycle** → No topological order possible

---

## 🔹 Applications

### 1. **Course Prerequisites**
Determine order to take courses given prerequisites.

### 2. **Build Systems**
Compile dependencies in correct order (makefiles, package managers).

### 3. **Task Scheduling**
Schedule tasks respecting dependencies.

### 4. **Compiler Design**  
Symbol table construction, dependency resolution.

### 5. **Spreadsheet Calculations**
Calculate cells in correct order based on formula dependencies.

---

## 🔹 Follow-Up Questions
1. How would you **find all possible topological orders** of a DAG?
2. What if you needed the **lexicographically smallest** topological order?
3. How would you **detect if cycles exist** during topological sorting?
4. Can you find the **longest path** in a DAG using topological sort?
5. How would you handle **parallel processing** of independent tasks?
6. What's the **minimum time** to complete all tasks given dependencies and processing times?
