# 🔹 Problem: Person Relationship – Count Number of Groups

**Difficulty:** Medium 🌿

---

## 🔹 Problem Statement
You are given a list of people and their **relationships** represented as a matrix `relations`, where:
- `relations[i][j] = 1` means **person i and person j** are directly related (friends, connected, etc.)
- `relations[i][j] = 0` means they are **not directly related**.
- The relationship is **symmetric** (`relations[i][j] == relations[j][i]`) and **reflexive** (`relations[i][i] = 1`).

A **group** is defined as a set of people who are directly or indirectly connected through relationships.

You must determine the **number of distinct groups**.

---

## 🔹 Example

Input:

relations = [

[1,1,0],

[1,1,0],

[0,0,1]

]

Output: 2


**Explanation:**
- Person 0 and 1 are connected → Group 1
- Person 2 is alone → Group 2

---

## 🔹 Intuition
This problem is equivalent to finding the **number of connected components** in an **undirected graph**:
- Each person is a **node**.
- A relationship (`1`) is an **edge**.
- A **group** is a connected component.

We can solve this using:
- **DFS (Depth-First Search)** — explore all connected nodes recursively.
- **BFS (Breadth-First Search)** — traverse level by level using a queue.
- **Union-Find (Disjoint Set Union)** — efficiently merge and count distinct sets.

---

## 🔹 Approaches

### 1. DFS Approach
- For every person not yet visited, perform a DFS to mark all connected people.
- Increment group count each time a new DFS starts.

**Time Complexity:** O(n²)  
**Space Complexity:** O(n) — visited array + recursion stack

---

### 2. BFS Approach
- Similar to DFS but uses a queue to traverse all connected nodes iteratively.

**Time Complexity:** O(n²)  
**Space Complexity:** O(n) — visited array + queue

---

### 3. Union-Find (Disjoint Set)
- Initially, each person is their own parent (own group).
- For each connection, **union** the two sets.
- Count how many unique parents remain.

**Time Complexity:** O(n² * α(n)) ≈ O(n²)  
**Space Complexity:** O(n)

---

## 🔹 Java Code (All 3 Approaches)

```java
public class PersonGroups {

    // 1. DFS Approach
    public static int countGroupsDFS(int[][] relations) {
        int n = relations.length;
        boolean[] visited = new boolean[n];
        int groups = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfs(relations, visited, i);
                groups++;
            }
        }
        return groups;
    }

    private static void dfs(int[][] relations, boolean[] visited, int person) {
        visited[person] = true;
        for (int i = 0; i < relations.length; i++) {
            if (relations[person][i] == 1 && !visited[i]) {
                dfs(relations, visited, i);
            }
        }
    }

    // 2. BFS Approach
    public static int countGroupsBFS(int[][] relations) {
        int n = relations.length;
        boolean[] visited = new boolean[n];
        int groups = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                Queue<Integer> queue = new ArrayDeque<>();
                queue.add(i);
                visited[i] = true;

                while (!queue.isEmpty()) {
                    int person = queue.poll();
                    for (int j = 0; j < n; j++) {
                        if (relations[person][j] == 1 && !visited[j]) {
                            queue.add(j);
                            visited[j] = true;
                        }
                    }
                }
                groups++;
            }
        }
        return groups;
    }

    // 3. Union-Find (Disjoint Set)
    public static int countGroupsUnionFind(int[][] relations) {
        int n = relations.length;
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (relations[i][j] == 1) {
                    union(parent, i, j);
                }
            }
        }

        Set<Integer> uniqueParents = new HashSet<>();
        for (int i = 0; i < n; i++) {
            uniqueParents.add(find(parent, i));
        }

        return uniqueParents.size();
    }

    private static int find(int[] parent, int x) {
        if (parent[x] != x)
            parent[x] = find(parent, parent[x]); // path compression
        return parent[x];
    }

    private static void union(int[] parent, int x, int y) {
        int rootX = find(parent, x);
        int rootY = find(parent, y);
        if (rootX != rootY)
            parent[rootY] = rootX;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach             | Time Complexity | Space Complexity |
|----------------------|-----------------|------------------|
| DFS                  | O(n²)           | O(n)             |
| BFS                  | O(n²)           | O(n)             |
| Union-Find (DSU)     | O(n²)           | O(n)             |

---

## 🔹 Edge Cases
- Empty input (`relations = []`) → groups = 0
- Single person → groups = 1
- Everyone connected → groups = 1
- No one connected → groups = n
- Large fully connected matrix → ensure recursion doesn’t overflow (prefer Union-Find)

---

## 🔹 Follow-Up Questions
1. How would you handle **directed relationships** (not symmetric)?
2. Can you optimize for **sparse relationships** (use adjacency list instead of matrix)?
3. How would you detect **cycles** in the relationship graph?
4. Can you identify **largest group size** instead of just the count?
