# Alien Dictionary

**Difficulty:** Hard 🔥

---

## 🔹 Problem Statement
There is a new alien language that uses the English alphabet. However, the order among the letters is unknown to you.

You are given a list of strings `words` from the alien language's dictionary, where the strings in `words` are **sorted lexicographically** by the rules of this new language.

Return *a string of the unique letters in the new alien language sorted in **lexicographically increasing order** by the new language's rules*. If there is no solution, return `""`. If there are multiple solutions, return **any of them**.

---

## 🔹 Examples

**Example 1:**  
Input: `words = ["wrt","wrf","er","ett","rftt"]`  
Output: `"wertf"`  
Explanation: From the given words, we can derive the ordering: w < e < r < t < f

**Example 2:**  
Input: `words = ["z","x"]`  
Output: `"zx"`  
Explanation: From the given words, we can derive the ordering: z < x

**Example 3:**  
Input: `words = ["z","x","z"]`  
Output: `""`  
Explanation: The order is invalid, so return "".

**Example 4:**  
Input: `words = ["abc","ab"]`  
Output: `""`  
Explanation: Invalid because "abc" cannot come before "ab" if 'c' > 'b'.

---

## 🔹 Constraints
- `1 <= words.length <= 100`
- `1 <= words[i].length <= 100`
- `words[i]` consists of only lowercase English letters
- The words are sorted lexicographically according to alien language rules

---

## 🔹 Intuition & Logic
This problem is essentially **finding the topological order** of characters in a directed graph.

**Key Steps:**
1. **Build directed graph** from adjacent word pairs
2. **Find character dependencies** by comparing adjacent words
3. **Apply topological sorting** to get the lexicographic order
4. **Detect cycles** - if cycle exists, no valid ordering possible

**Graph Construction:**
- For each pair of adjacent words, find the first differing character
- Add directed edge from first character to second character
- This represents the ordering constraint in alien language

**Invalid Cases:**
- **Cycle detected** → No valid topological order
- **Prefix violation** → Longer word comes before its prefix (e.g., "abc" before "ab")

---

## 🔹 Approaches

### 1. Kahn's Algorithm (BFS Topological Sort)
- Build graph and calculate indegrees
- Use BFS to process nodes with indegree 0
- If all nodes processed → valid order, else cycle exists

**Time Complexity:** O(C) where C = total number of characters in all words  
**Space Complexity:** O(1) since we have at most 26 characters

---

### 2. DFS-based Topological Sort
- Build graph from word comparisons  
- Use DFS with three states (white, gray, black)
- Gray state detection indicates cycle
- Post-order traversal gives topological order

**Time Complexity:** O(C)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.*;

public class AlienDictionary {
    
    // 1. Kahn's Algorithm (BFS)
    public static String alienOrderKahn(String[] words) {
        // Step 1: Build graph and indegree map
        Map<Character, Set<Character>> graph = new HashMap<>();
        Map<Character, Integer> indegree = new HashMap<>();
        
        // Initialize all characters
        for (String word : words) {
            for (char c : word.toCharArray()) {
                graph.putIfAbsent(c, new HashSet<>());
                indegree.putIfAbsent(c, 0);
            }
        }
        
        // Step 2: Build edges from adjacent word pairs
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            
            // Check for prefix violation (longer word before shorter)
            if (word1.length() > word2.length() && word1.startsWith(word2)) {
                return ""; // Invalid case
            }
            
            // Find first differing character
            for (int j = 0; j < Math.min(word1.length(), word2.length()); j++) {
                char c1 = word1.charAt(j);
                char c2 = word2.charAt(j);
                
                if (c1 != c2) {
                    // Add edge c1 → c2 if not already exists
                    if (!graph.get(c1).contains(c2)) {
                        graph.get(c1).add(c2);
                        indegree.put(c2, indegree.get(c2) + 1);
                    }
                    break; // Only first differing character matters
                }
            }
        }
        
        // Step 3: Kahn's algorithm (BFS topological sort)
        Queue<Character> queue = new ArrayDeque<>();
        for (char c : indegree.keySet()) {
            if (indegree.get(c) == 0) {
                queue.offer(c);
            }
        }
        
        StringBuilder result = new StringBuilder();
        while (!queue.isEmpty()) {
            char current = queue.poll();
            result.append(current);
            
            for (char neighbor : graph.get(current)) {
                indegree.put(neighbor, indegree.get(neighbor) - 1);
                if (indegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // Step 4: Check if all characters are processed (no cycle)
        return result.length() == indegree.size() ? result.toString() : "";
    }
    
    // 2. DFS-based Topological Sort
    public static String alienOrderDFS(String[] words) {
        Map<Character, Set<Character>> graph = new HashMap<>();
        
        // Initialize all characters
        for (String word : words) {
            for (char c : word.toCharArray()) {
                graph.putIfAbsent(c, new HashSet<>());
            }
        }
        
        // Build graph from adjacent words
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            
            // Check prefix violation
            if (word1.length() > word2.length() && word1.startsWith(word2)) {
                return "";
            }
            
            // Add edge for first differing character
            for (int j = 0; j < Math.min(word1.length(), word2.length()); j++) {
                char c1 = word1.charAt(j);
                char c2 = word2.charAt(j);
                if (c1 != c2) {
                    graph.get(c1).add(c2);
                    break;
                }
            }
        }
        
        // DFS with three states: 0=white, 1=gray, 2=black
        Map<Character, Integer> color = new HashMap<>();
        for (char c : graph.keySet()) {
            color.put(c, 0);
        }
        
        Stack<Character> stack = new Stack<>();
        for (char c : graph.keySet()) {
            if (color.get(c) == 0) {
                if (!dfs(graph, color, c, stack)) {
                    return ""; // Cycle detected
                }
            }
        }
        
        StringBuilder result = new StringBuilder();
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }
        
        return result.toString();
    }
    
    private static boolean dfs(Map<Character, Set<Character>> graph, 
                              Map<Character, Integer> color, 
                              char node, Stack<Character> stack) {
        color.put(node, 1); // Mark as gray (being processed)
        
        for (char neighbor : graph.get(node)) {
            if (color.get(neighbor) == 1) {
                return false; // Cycle detected (gray neighbor)
            }
            if (color.get(neighbor) == 0 && !dfs(graph, color, neighbor, stack)) {
                return false;
            }
        }
        
        color.put(node, 2); // Mark as black (completely processed)
        stack.push(node);
        return true;
    }
    
    public static void main(String[] args) {
        // Test case 1: Valid ordering
        String[] words1 = {"wrt", "wrf", "er", "ett", "rftt"};
        System.out.println("Test 1 - Kahn: " + alienOrderKahn(words1)); // "wertf"
        System.out.println("Test 1 - DFS: " + alienOrderDFS(words1));   // "wertf"
        
        // Test case 2: Simple case
        String[] words2 = {"z", "x"};
        System.out.println("Test 2 - Kahn: " + alienOrderKahn(words2)); // "zx"
        
        // Test case 3: Invalid - cycle
        String[] words3 = {"z", "x", "z"};
        System.out.println("Test 3 - Invalid: " + alienOrderKahn(words3)); // ""
        
        // Test case 4: Invalid - prefix violation
        String[] words4 = {"abc", "ab"};
        System.out.println("Test 4 - Prefix violation: " + alienOrderDFS(words4)); // ""
        
        // Test case 5: Single character
        String[] words5 = {"a", "b", "c"};
        System.out.println("Test 5 - Simple: " + alienOrderKahn(words5)); // "abc"
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|----------|-----------------|------------------|
| Kahn's Algorithm | O(C) | O(1) |
| DFS Topological Sort | O(C) | O(1) |

*Where C = total number of characters in all words. Space is O(1) since we have at most 26 lowercase English letters.*

---

## 🔹 Edge Cases
- **Empty words array** → Return empty string
- **Single word** → Return all unique characters in any order
- **All same characters** → Return single character
- **Prefix violation** → "abc" before "ab" is invalid
- **Cycle in dependencies** → No valid ordering exists
- **Identical adjacent words** → Skip, no ordering information

---

## 🔹 Key Insights

### 1. **Graph Construction**
Only the **first differing character** between adjacent words gives ordering information.

### 2. **Prefix Handling**
If word A is a prefix of word B, then A must come before B. If B comes before A in input, it's invalid.

### 3. **Cycle Detection**
If there's a cycle in character dependencies, no valid alien dictionary order exists.

### 4. **Multiple Solutions**
The problem allows multiple valid topological orders - return any one.

---

## 🔹 Follow-Up Questions
1. What if the alien language uses **different characters** (not just English lowercase)?
2. How would you handle **case sensitivity** in the alien dictionary?
3. Can you **find all possible valid orderings** instead of just one?
4. How would you **verify if a given ordering** is valid for the input words?
5. What if words can have **equal precedence** (multiple characters at same level)?
6. How would you handle **streaming input** where words arrive one by one?
