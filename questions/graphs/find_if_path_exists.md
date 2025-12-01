# Find If Path Exists in Graph

**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
There is a **bi-directional graph** with `n` vertices, where each vertex is labeled from `0` to `n - 1` (**inclusive**). The edges in the graph are represented as a 2D integer array `edges`, where each `edges[i] = [ui, vi]` denotes a bi-directional edge between vertex `ui` and vertex `vi`. Every vertex pair is connected by **at most one edge**, and no vertex has an edge to itself.

You want to determine if there is a **valid path** that exists from vertex `source` to vertex `destination`.

Given `edges` and the integers `n`, `source`, and `destination`, return `true` *if there is a **valid path** from `source` to `destination`, or `false` otherwise*.

---

## 🔹 Examples

**Example 1:**  
Input: `n = 3, edges = [[0,1],[1,2],[2,0]], source = 0, destination = 2`  
Output: `true`  
Explanation: There are two paths from vertex 0 to vertex 2:
- 0 → 1 → 2
- 0 → 2

**Example 2:**  
Input: `n = 6, edges = [[0,1],[0,2],[3,5],[5,4],[4,3]], source = 0, destination = 5`  
Output: `false`  
Explanation: There is no path from vertex 0 to vertex 5.

**Example 3:**  
Input: `n = 1, edges = [], source = 0, destination = 0`  
Output: `true`  
Explanation: There is only one vertex, so source and destination are the same.

---

## 🔹 Constraints
- `1 <= n <= 2 * 10^5`
- `0 <= edges.length <= 2 * 10^5`
- `edges[i].length == 2`
- `0 <= ui, vi <= n - 1`
- `ui != vi`
- `0 <= source, destination <= n - 1`
- There are no duplicate edges
- There are no self edges

---

## 🔹 Intuition & Logic
This is a classic **graph connectivity** problem. We need to check if two vertices are in the same **connected component**.

Key approaches:
1. **DFS (Depth-First Search)** - Traverse from source and check if we can reach destination
2. **BFS (Breadth-First Search)** - Level-by-level exploration from source
3. **Union-Find (Disjoint Set)** - Build connected components and check if source and destination belong to same component

All approaches work well, with Union-Find being particularly efficient for multiple queries.

---

## 🔹 Approaches

### 1. DFS Approach (Recursive)
- Build adjacency list from edges
- Start DFS from source vertex
- Mark visited vertices to avoid cycles
- Return true if destination is reached

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V + E)

---

### 2. BFS Approach (Iterative)
- Build adjacency list from edges
- Use queue for level-by-level exploration
- Mark visited vertices
- Return true if destination is found

**Time Complexity:** O(V + E)  
**Space Complexity:** O(V + E)

---

### 3. Union-Find Approach (Optimal for Multiple Queries)
- Initialize each vertex as its own parent
- Union connected vertices from edges
- Check if source and destination have same root

**Time Complexity:** O(E * α(V)) where α is inverse Ackermann function  
**Space Complexity:** O(V)

---

## 🔹 Java Code

```java
import java.util.*;

public class FindPathExists {
    
    // 1. DFS Approach
    public static boolean validPathDFS(int n, int[][] edges, int source, int destination) {
        if (source == destination) return true;
        
        // Build adjacency list
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        
        for (int[] edge : edges) {
            adj.get(edge[0]).add(edge[1]);
            adj.get(edge[1]).add(edge[0]);
        }
        
        boolean[] visited = new boolean[n];
        return dfs(adj, visited, source, destination);
    }
    
    private static boolean dfs(List<List<Integer>> adj, boolean[] visited, int current, int destination) {
        if (current == destination) return true;
        visited[current] = true;
        
        for (int neighbor : adj.get(current)) {
            if (!visited[neighbor] && dfs(adj, visited, neighbor, destination)) {
                return true;
            }
        }
        return false;
    }
    
    // 2. BFS Approach
    public static boolean validPathBFS(int n, int[][] edges, int source, int destination) {
        if (source == destination) return true;
        
        // Build adjacency list
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        
        for (int[] edge : edges) {
            adj.get(edge[0]).add(edge[1]);
            adj.get(edge[1]).add(edge[0]);
        }
        
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(source);
        visited[source] = true;
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            
            for (int neighbor : adj.get(current)) {
                if (neighbor == destination) return true;
                
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.offer(neighbor);
                }
            }
        }
        
        return false;
    }
    
    // 3. Union-Find Approach
    static class UnionFind {
        int[] parent;
        int[] rank;
        
        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }
        
        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }
        
        void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            
            if (rootX != rootY) {
                // Union by rank
                if (rank[rootX] < rank[rootY]) {
                    parent[rootX] = rootY;
                } else if (rank[rootX] > rank[rootY]) {
                    parent[rootY] = rootX;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX]++;
                }
            }
        }
        
        boolean connected(int x, int y) {
            return find(x) == find(y);
        }
    }
    
    public static boolean validPathUnionFind(int n, int[][] edges, int source, int destination) {
        UnionFind uf = new UnionFind(n);
        
        // Union all connected vertices
        for (int[] edge : edges) {
            uf.union(edge[0], edge[1]);
        }
        
        return uf.connected(source, destination);
    }
    
    public static void main(String[] args) {
        // Test cases
        int[][] edges1 = {{0,1},{1,2},{2,0}};
        System.out.println(validPathDFS(3, edges1, 0, 2)); // true
        
        int[][] edges2 = {{0,1},{0,2},{3,5},{5,4},{4,3}};
        System.out.println(validPathBFS(6, edges2, 0, 5)); // false
        
        int[][] edges3 = {};
        System.out.println(validPathUnionFind(1, edges3, 0, 0)); // true
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|----------|-----------------|------------------|
| DFS      | O(V + E)       | O(V + E)         |
| BFS      | O(V + E)       | O(V + E)         |
| Union-Find | O(E * α(V))   | O(V)             |

*Where V = number of vertices, E = number of edges, α = inverse Ackermann function*

---

## 🔹 Edge Cases
- Source equals destination → Always return `true`
- Empty graph (no edges) → Only true if source == destination
- Disconnected components → Path exists only within same component
- Single vertex graph → True if source == destination
- Large graphs → Union-Find performs better for multiple path queries

---

## 🔹 Follow-Up Questions
1. How would you **find the actual path** from source to destination?
2. What if you needed to answer **multiple path existence queries** efficiently?
3. How would you modify this for a **directed graph**?
4. Can you find the **shortest path length** between source and destination?
5. How would you handle **weighted edges** in this problem?
