# Edit Distance (Levenshtein Distance) – Detailed Explanation and Approaches

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given two strings `word1` and `word2`, return the **minimum number of operations** required to convert `word1` to `word2`.

You have the following **three operations** permitted on a word:
1. **Insert** a character
2. **Delete** a character
3. **Replace** a character

This is also known as the **Levenshtein Distance**.

---

## 🔹 Examples

**Example 1:**  
Input: `word1 = "horse", word2 = "ros"`  
Output: `3`  
Explanation:
- horse → rorse (replace 'h' with 'r')
- rorse → rose (remove 'r')
- rose → ros (remove 'e')

**Example 2:**  
Input: `word1 = "intention", word2 = "execution"`  
Output: `5`  
Explanation:
- intention → inention (delete 't')
- inention → enention (replace 'i' with 'e')
- enention → exention (replace 'n' with 'x')
- exention → exection (replace 'n' with 'c')
- exection → execution (insert 'u')

**Example 3:**  
Input: `word1 = "abc", word2 = "abc"`  
Output: `0`  
Explanation: Strings are already equal

**Example 4:**  
Input: `word1 = "", word2 = "abc"`  
Output: `3`  
Explanation: Insert 3 characters

---

## 🔹 Constraints
- `0 <= word1.length, word2.length <= 500`
- `word1` and `word2` consist of lowercase English letters

---

## 🔹 Core Intuition

For each character position, we have multiple choices based on whether characters match:

**If characters match:**
```
No operation needed, move to next characters
dp[i][j] = dp[i-1][j-1]
```

**If characters don't match, try all 3 operations:**
```
1. Replace: dp[i-1][j-1] + 1
2. Delete from word1: dp[i-1][j] + 1
3. Insert into word1: dp[i][j-1] + 1

dp[i][j] = 1 + min(replace, delete, insert)
```

**Base Cases:**
```
dp[0][j] = j  // Insert j characters
dp[i][0] = i  // Delete i characters
```

---

## 1️⃣ Recursive Approach (Brute Force)

### Explanation
Compare characters from end of both strings:
- If match → move both pointers back
- If don't match → try all 3 operations, take minimum

### Code (Java)

```java
public class EditDistance {
    
    // Approach 1: Recursive (Brute Force)
    public int minDistanceRecursive(String word1, String word2) {
        return helper(word1, word2, word1.length(), word2.length());
    }
    
    private int helper(String word1, String word2, int i, int j) {
        // Base cases
        if (i == 0) return j;  // Insert all characters of word2
        if (j == 0) return i;  // Delete all characters of word1
        
        // If last characters match
        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
            return helper(word1, word2, i - 1, j - 1);
        }
        
        // If don't match, try all 3 operations
        int insert = helper(word1, word2, i, j - 1);      // Insert
        int delete = helper(word1, word2, i - 1, j);      // Delete
        int replace = helper(word1, word2, i - 1, j - 1); // Replace
        
        return 1 + Math.min(insert, Math.min(delete, replace));
    }
}
```

### Drawbacks
- **Time Complexity:** O(3^(m+n)) — exponential branching
- **Space Complexity:** O(m + n) — recursion depth
- Massive redundant computation for large strings

---

## 2️⃣ Recursive with Memoization (Top-Down DP)

### Explanation
Cache results in a 2D memo table:
- `memo[i][j]` = min operations to convert first i chars of word1 to first j chars of word2

### Code (Java)

```java
public class EditDistance {
    
    // Approach 2: Memoization (Top-Down DP)
    public int minDistanceMemo(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] memo = new int[m + 1][n + 1];
        
        // Initialize with -1 (uncomputed)
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        
        return helperMemo(word1, word2, m, n, memo);
    }
    
    private int helperMemo(String word1, String word2, int i, int j, int[][] memo) {
        // Base cases
        if (i == 0) return j;
        if (j == 0) return i;
        
        // Check cache
        if (memo[i][j] != -1) {
            return memo[i][j];
        }
        
        // If characters match
        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
            memo[i][j] = helperMemo(word1, word2, i - 1, j - 1, memo);
        } else {
            int insert = helperMemo(word1, word2, i, j - 1, memo);
            int delete = helperMemo(word1, word2, i - 1, j, memo);
            int replace = helperMemo(word1, word2, i - 1, j - 1, memo);
            
            memo[i][j] = 1 + Math.min(insert, Math.min(delete, replace));
        }
        
        return memo[i][j];
    }
}
```

### Complexity
- **Time Complexity:** O(m × n) — each state computed once
- **Space Complexity:** O(m × n) for memo + O(m + n) for recursion

---

## 3️⃣ Dynamic Programming – Bottom-Up Approach (Tabulation)

### Explanation
Build a 2D DP table where:
- **Rows** represent characters of word1 (0 to m)
- **Columns** represent characters of word2 (0 to n)
- **dp[i][j]** = min operations to convert first i chars of word1 to first j chars of word2

### Code (Java)

```java
public class EditDistance {
    
    // Approach 3: Bottom-Up DP (Tabulation)
    public int minDistanceDP(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        // Base cases: first row and first column
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;  // Delete all characters
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;  // Insert all characters
        }
        
        // Fill the DP table
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    // Characters match, no operation needed
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Try all 3 operations, take minimum
                    int insert = dp[i][j - 1];      // Insert
                    int delete = dp[i - 1][j];      // Delete
                    int replace = dp[i - 1][j - 1]; // Replace
                    
                    dp[i][j] = 1 + Math.min(insert, Math.min(delete, replace));
                }
            }
        }
        
        return dp[m][n];
    }
}
```

### Complexity
- **Time Complexity:** O(m × n)
- **Space Complexity:** O(m × n)

### DP Table Example

For `word1 = "horse"`, `word2 = "ros"`:

|   |   | r | o | s |
|---|---|---|---|---|
|   | 0 | 1 | 2 | 3 |
| **h** | 1 | 1 | 2 | 3 |
| **o** | 2 | 2 | 1 | 2 |
| **r** | 3 | 2 | 2 | 2 |
| **s** | 4 | 3 | 3 | 2 |
| **e** | 5 | 4 | 4 | **3** |

**Answer:** dp[5][3] = **3**

---

## 4️⃣ Dynamic Programming – Space Optimized ⭐

### Explanation
**Observation:** To compute dp[i][j], we only need current row and previous row.

Use **two 1D arrays** instead of 2D table to save space.

### Code (Java)

```java
public class EditDistance {
    
    // Approach 4: Space Optimized (Two Arrays)
    public int minDistanceOptimized(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        
        // Initialize first row
        for (int j = 0; j <= n; j++) {
            prev[j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            curr[0] = i;  // First column (delete operations)
            
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    curr[j] = 1 + Math.min(
                        curr[j - 1],      // Insert
                        Math.min(
                            prev[j],       // Delete
                            prev[j - 1]    // Replace
                        )
                    );
                }
            }
            
            // Swap arrays
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return prev[n];
    }
}
```

### Complexity
- **Time Complexity:** O(m × n)
- **Space Complexity:** O(n) ⭐ (only two arrays of size n+1)

---

## 📊 Complexity Comparison

| Approach | Time | Space | Recommended |
|----------|------|-------|-------------|
| Recursive (Brute Force) | O(3^(m+n)) | O(m+n) | ❌ Never |
| Memoization (Top-Down) | O(m×n) | O(m×n) | ✅ Good |
| Tabulation (Bottom-Up) | O(m×n) | O(m×n) | ✅ Good |
| Space Optimized | O(m×n) | O(min(m,n)) | ⭐ Best |

---

## 🎯 Reconstructing the Edit Sequence

To find the actual operations performed:

### Code (Java)

```java
public class EditDistance {
    
    // Find actual edit operations
    public List<String> getEditOperations(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        // Build DP table
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        dp[i][j - 1],      // Insert
                        Math.min(dp[i - 1][j], dp[i - 1][j - 1])
                    );
                }
            }
        }
        
        // Backtrack to find operations
        List<String> operations = new ArrayList<>();
        int i = m, j = n;
        
        while (i > 0 && j > 0) {
            char c1 = word1.charAt(i - 1);
            char c2 = word2.charAt(j - 1);
            
            if (c1 == c2) {
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j - 1] + 1) {
                operations.add("Replace '" + c1 + "' with '" + c2 + "'");
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j] + 1) {
                operations.add("Delete '" + c1 + "'");
                i--;
            } else {
                operations.add("Insert '" + c2 + "'");
                j--;
            }
        }
        
        while (i > 0) {
            operations.add("Delete '" + word1.charAt(i - 1) + "'");
            i--;
        }
        
        while (j > 0) {
            operations.add("Insert '" + word2.charAt(j - 1) + "'");
            j--;
        }
        
        Collections.reverse(operations);
        return operations;
    }
}
```

---

## 🔄 Additional Variations with Code

### Variation 1: Edit Distance with Custom Costs

**Problem:** Different operations have different costs.

#### Code (Java)

```java
public class EditDistanceVariations {
    
    // Variation 1: Custom Operation Costs
    public int minDistanceWithCosts(String word1, String word2, 
                                     int insertCost, int deleteCost, int replaceCost) {
        int m = word1.length();
        int n = word2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        // Base cases with custom costs
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * deleteCost;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * insertCost;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1] + insertCost;
                    int delete = dp[i - 1][j] + deleteCost;
                    int replace = dp[i - 1][j - 1] + replaceCost;
                    
                    dp[i][j] = Math.min(insert, Math.min(delete, replace));
                }
            }
        }
        
        return dp[m][n];
    }
}
```

---

### Variation 2: One Edit Distance

**Problem:** Check if two strings are exactly one edit distance apart.

#### Code (Java)

```java
public class EditDistanceVariations {
    
    // Variation 2: One Edit Distance
    public boolean isOneEditDistance(String s, String t) {
        int m = s.length();
        int n = t.length();
        
        // Length difference should be at most 1
        if (Math.abs(m - n) > 1) return false;
        
        // If same length, check for exactly one replacement
        if (m == n) {
            int diffCount = 0;
            for (int i = 0; i < m; i++) {
                if (s.charAt(i) != t.charAt(i)) {
                    diffCount++;
                    if (diffCount > 1) return false;
                }
            }
            return diffCount == 1;
        }
        
        // If different length, check for exactly one insert/delete
        String longer = m > n ? s : t;
        String shorter = m > n ? t : s;
        
        int i = 0, j = 0;
        boolean foundDiff = false;
        
        while (i < shorter.length() && j < longer.length()) {
            if (shorter.charAt(i) != longer.charAt(j)) {
                if (foundDiff) return false;
                foundDiff = true;
                j++;  // Skip character in longer string
            } else {
                i++;
                j++;
            }
        }
        
        return true;
    }
}
```

---

### Variation 3: Delete Operation for Two Strings

**Problem:** Find minimum deletions to make two strings equal.

**Insight:** This is Edit Distance with only delete operations (no insert/replace).

#### Code (Java)

```java
public class EditDistanceVariations {
    
    // Variation 3: Minimum Deletions to Make Equal
    public int minDeleteOperations(String word1, String word2) {
        // Find LCS (Longest Common Subsequence)
        int lcs = longestCommonSubsequence(word1, word2);
        
        // Delete characters not in LCS from both strings
        return (word1.length() - lcs) + (word2.length() - lcs);
    }
    
    private int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
    
    // Direct DP approach
    public int minDeleteOperationsDirect(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Only delete operations allowed
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
}
```

**Example:** word1="sea", word2="eat" → Delete 's' from word1, delete 't' from word2 = 2 operations

---

### Variation 4: Wildcard Pattern Matching

**Problem:** Match string with pattern containing '?' (any single char) and '*' (any sequence).

#### Code (Java)

```java
public class EditDistanceVariations {
    
    // Variation 4: Wildcard Matching
    public boolean isMatch(String s, String p) {
        int m = s.length();
        int n = p.length();
        
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;  // Empty matches empty
        
        // Handle patterns like "***" matching empty string
        for (int j = 1; j <= n; j++) {
            if (p.charAt(j - 1) == '*') {
                dp[0][j] = dp[0][j - 1];
            }
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char sc = s.charAt(i - 1);
                char pc = p.charAt(j - 1);
                
                if (pc == '*') {
                    // '*' matches empty or any sequence
                    dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
                } else if (pc == '?' || sc == pc) {
                    // '?' matches any single char, or exact match
                    dp[i][j] = dp[i - 1][j - 1];
                }
            }
        }
        
        return dp[m][n];
    }
}
```

**Example:** s="adceb", p="*a*b" → true

---

### Variation 5: Regular Expression Matching

**Problem:** Pattern with '.' (any char) and '*' (zero or more of previous char).

#### Code (Java)

```java
public class EditDistanceVariations {
    
    // Variation 5: Regular Expression Matching
    public boolean isMatchRegex(String s, String p) {
        int m = s.length();
        int n = p.length();
        
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;
        
        // Handle patterns like "a*b*c*" matching empty
        for (int j = 2; j <= n; j++) {
            if (p.charAt(j - 1) == '*') {
                dp[0][j] = dp[0][j - 2];
            }
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char sc = s.charAt(i - 1);
                char pc = p.charAt(j - 1);
                
                if (pc == '.' || sc == pc) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else if (pc == '*') {
                    char prevPattern = p.charAt(j - 2);
                    
                    // Two cases for '*'
                    // 1. Match zero occurrences
                    dp[i][j] = dp[i][j - 2];
                    
                    // 2. Match one or more occurrences
                    if (prevPattern == '.' || prevPattern == sc) {
                        dp[i][j] = dp[i][j] || dp[i - 1][j];
                    }
                }
            }
        }
        
        return dp[m][n];
    }
}
```

**Example:** s="aab", p="c*a*b" → true

---

### Variation 6: Longest Common Subsequence

**Problem:** Find length of longest subsequence common to both strings.

**Relation:** Related to edit distance. Edit distance uses LCS in some solutions.

#### Code (Java)

```java
public class EditDistanceVariations {
    
    // Variation 6: Longest Common Subsequence
    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
    
    // Space optimized version
    public int longestCommonSubsequenceOptimized(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    curr[j] = 1 + prev[j - 1];
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return prev[n];
    }
}
```

**Connection to Edit Distance:**
```
Edit Distance = (m - LCS) + (n - LCS)
```

---

## 🎓 Complete Implementation

```java
import java.util.*;

public class EditDistanceComplete {
    
    // ==================== MAIN PROBLEM ====================
    
    // 1. Recursive
    public int minDistanceRecursive(String word1, String word2) {
        return helper(word1, word2, word1.length(), word2.length());
    }
    
    private int helper(String word1, String word2, int i, int j) {
        if (i == 0) return j;
        if (j == 0) return i;
        
        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
            return helper(word1, word2, i - 1, j - 1);
        }
        
        return 1 + Math.min(
            helper(word1, word2, i, j - 1),      // Insert
            Math.min(
                helper(word1, word2, i - 1, j),   // Delete
                helper(word1, word2, i - 1, j - 1) // Replace
            )
        );
    }
    
    // 2. Memoization
    public int minDistanceMemo(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] memo = new int[m + 1][n + 1];
        for (int[] row : memo) Arrays.fill(row, -1);
        return helperMemo(word1, word2, m, n, memo);
    }
    
    private int helperMemo(String word1, String word2, int i, int j, int[][] memo) {
        if (i == 0) return j;
        if (j == 0) return i;
        if (memo[i][j] != -1) return memo[i][j];
        
        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
            memo[i][j] = helperMemo(word1, word2, i - 1, j - 1, memo);
        } else {
            memo[i][j] = 1 + Math.min(
                helperMemo(word1, word2, i, j - 1, memo),
                Math.min(
                    helperMemo(word1, word2, i - 1, j, memo),
                    helperMemo(word1, word2, i - 1, j - 1, memo)
                )
            );
        }
        return memo[i][j];
    }
    
    // 3. Tabulation (RECOMMENDED)
    public int minDistanceDP(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i][j - 1], 
                                   Math.min(dp[i - 1][j], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[m][n];
    }
    
    // 4. Space Optimized (BEST)
    public int minDistanceOptimized(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        
        for (int j = 0; j <= n; j++) prev[j] = j;
        
        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    curr[j] = 1 + Math.min(curr[j - 1], 
                                  Math.min(prev[j], prev[j - 1]));
                }
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[n];
    }
    
    public static void main(String[] args) {
        EditDistanceComplete ed = new EditDistanceComplete();
        
        String word1 = "horse";
        String word2 = "ros";
        
        System.out.println("=== Edit Distance ===");
        System.out.println("Minimum operations: " + ed.minDistanceDP(word1, word2));
    }
}
```

---

## 🎯 Key Takeaways

1. **Three Operations:** Insert, Delete, Replace — understand what each means
2. **State Definition:** dp[i][j] = min ops to convert first i chars to first j chars
3. **Character Match:** If match, no operation needed, take diagonal
4. **Character Mismatch:** Try all 3 operations, take minimum + 1
5. **Applications:** Spell checkers, DNA sequence alignment, diff tools

---

## 💡 Real-World Applications

### 1. **Spell Checkers**
Find closest matching words in dictionary based on edit distance.

### 2. **DNA Sequence Alignment**
Compare genetic sequences to find similarity and mutations.

### 3. **Diff Tools (Git, Version Control)**
Show minimum changes between file versions.

### 4. **Auto-Correction Systems**
Suggest corrections based on minimum edits needed.

### 5. **Plagiarism Detection**
Measure similarity between texts.

---

## 🌟 Interview Tips

1. **Draw the DP table:** Helps visualize state transitions
2. **Explain 3 operations:** Make sure interviewer understands each choice
3. **Start with recursion:** Show the thought process
4. **Optimize gradually:** Recursion → Memo → DP → Space optimized
5. **Know variations:** One edit distance, delete-only, pattern matching
6. **Handle edge cases:** Empty strings, identical strings, single character

This is one of the **most classic DP problems** and appears in many forms! 🚀
