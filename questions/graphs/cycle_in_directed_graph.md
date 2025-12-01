# Detect Cycle in Directed Graph

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a directed graph with `V` vertices and `E` edges, check whether the graph contains any cycle or not.

A cycle exists in a directed graph if there is a path from a vertex back to itself following the direction of edges.

---

## 🔹 Examples

**Example 1:**  
Input: `V = 4, edges = [[0,1], [1,2], [2,3]]`  
Output: `false`  
Explanation: No cycle exists (DAG - Directed Acyclic Graph).

**Example 2:**  
Input: `V = 4, edges = [[0,1], [1,2], [2,3], [3,1]]`  
Output: `true`  
Explanation: There is a cycle 1 → 2 → 3 → 1.

**Example 3:**  
Input: `V = 3, edges = [[0,1], [1,2], [2,0]]`  
Output: `true`  
Explanation: There is a cycle 0 → 1 → 2 → 0.

---

## 🔹 Constraints
- `1 <= V <= 10^5`
- `0 <= E <= 10^5`
- Graph doesn't contain self-loops
- The graph may not be strongly connected

---

## 🔹 Intuition & Logic
In a **directed graph**, cycle detection is more complex than undirected graphs because we need to track the **current path** being explored.

**Key Insight:** A cycle exists if during DFS, we encounter a vertex that is **currently being processed** (part of current recursion stack).

We need **three states** for each vertex:
- **White (0)**: Unvisited
- **Gray (1)**: Currently being processed (in recursion stack)  
- **Black (2)**: Completely processed

**Approaches:**
1. **DFS with Recursion Stack** - Track vertices in current path
2. **Kahn's Algorithm (Topological Sort)** - If topological sort is possible, no cycle exists
3. **Strongly Connected Components** - Advanced approach using Tarjan's/Kosaraju's algorithm

---

## 🔹 Approaches

### 1. DFS with Recursion Stack (White-Gray-Black)
- Maintain color array: White(0), Gray(1), Black(2)
- During DFS, mark vertex as Gray (being processed)
- If we encounter Gray vertex → cycle found
- After processing, mark vertex as Black

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V)

---

### 2. Kahn's Algorithm (Topological Sort)
- If graph is acyclic, we can perform complete topological sort
- Count vertices processed during topological sort
- If count < V → cycle exists

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V)

---

### 3. DFS with Visited + Recursion Stack Arrays
- Use two arrays: visited[] and recStack[]
- recStack[] tracks current path being explored
- More intuitive than color-based approach

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V)

---

## 🔹 Java Code

```java
import java.util.*;

public class CycleDirectedGraph {
    
    // 1. DFS with Recursion Stack (White-Gray-Black)
    public static boolean hasCycleDFS(int V, List<List<Integer>> adj) {
        int[] color = new int[V]; // 0: White, 1: Gray, 2: Black
        
        for (int i = 0; i < V; i++) {
            if (color[i] == 0) { // White (unvisited)
                if (dfs(adj, color, i)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean dfs(List<List<Integer>> adj, int[] color, int node) {
        color[node] = 1; // Mark as Gray (being processed)
        
        for (int neighbor : adj.get(node)) {
            if (color[neighbor] == 1) {
                // Gray neighbor means cycle (back edge to ancestor)
                return true;
            }
            if (color[neighbor] == 0 && dfs(adj, color, neighbor)) {
                return true;
            }
        }
        
        color[node] = 2; // Mark as Black (completely processed)
        return false;
    }
    
    // 2. Kahn's Algorithm (Topological Sort)
    public static boolean hasCycleKahn(int V, List<List<Integer>> adj) {
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
        
        int count = 0;
        while (!queue.isEmpty()) {
            int node = queue.poll();
            count++;
            
            for (int neighbor : adj.get(node)) {
                indegree[neighbor]--;
                if (indegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        return count != V; // If count < V, cycle exists
    }
    
    // 3. DFS with Visited + RecStack Arrays
    public static boolean hasCycleDFSRecStack(int V, List<List<Integer>> adj) {
        boolean[] visited = new boolean[V];
        boolean[] recStack = new boolean[V];
        
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                if (dfsRecStack(adj, visited, recStack, i)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean dfsRecStack(List<List<Integer>> adj, boolean[] visited, 
                                     boolean[] recStack, int node) {
        visited[node] = true;
        recStack[node] = true;
        
        for (int neighbor : adj.get(node)) {
            if (!visited[neighbor]) {
                if (dfsRecStack(adj, visited, recStack, neighbor)) {
                    return true;
                }
            } else if (recStack[neighbor]) {
                // Found back edge to vertex in current recursion stack
                return true;
            }
        }
        
        recStack[node] = false; // Remove from recursion stack
        return false;
    }
    
    // Helper method to build adjacency list from edges
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
    
    public static void main(String[] args) {
        // Test case 1: No cycle (DAG)
        int[][] edges1 = {{0,1}, {1,2}, {2,3}};
        List<List<Integer>> adj1 = buildGraph(4, edges1);
        System.out.println("Test 1 - DFS: " + hasCycleDFS(4, adj1)); // false
        System.out.println("Test 1 - Kahn: " + hasCycleKahn(4, adj1)); // false
        
        // Test case 2: Has cycle
        int[][] edges2 = {{0,1}, {1,2}, {2,3}, {3,1}};
        List<List<Integer>> adj2 = buildGraph(4, edges2);
        System.out.println("Test 2 - DFS: " + hasCycleDFS(4, adj2)); // true
        System.out.println("Test 2 - RecStack: " + hasCycleDFSRecStack(4, adj2)); // true
        
        // Test case 3: Self-cycle
        int[][] edges3 = {{0,1}, {1,2}, {2,0}};
        List<List<Integer>> adj3 = buildGraph(3, edges3);
        System.out.println("Test 3 - Kahn: " + hasCycleKahn(3, adj3)); // true
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|----------|-----------------|------------------|
| DFS (White-Gray-Black) | O(V + E) | O(V) |
| Kahn's Algorithm | O(V + E) | O(V) |
| DFS (Visited + RecStack) | O(V + E) | O(V) |

---

## 🔹 Edge Cases
- Single vertex → No cycle possible
- No edges → No cycle (isolated vertices)
- Self-loop → Cycle exists (if allowed)
- Strongly connected graph → Definitely has cycles
- Tree structure → No cycles (DAG)

---

## 🔹 Follow-Up Questions
1. How would you **find and print all cycles** in the directed graph?
2. Can you **find the shortest cycle** in the graph?
3. How would you detect cycles in a **weighted directed graph**?
4. What's the difference between **strongly connected components** and cycle detection?
5. How would you handle **online cycle detection** when edges are added dynamically?
6. Can you **find the longest path** in a DAG (no cycles)?
