# Longest Common Subsequence (LCS) – Detailed Explanation and Approaches

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given two strings `text1` and `text2`, return the **length of their longest common subsequence**. If there is no common subsequence, return `0`.

A **subsequence** of a string is a new string generated from the original string with some characters (can be none) deleted without changing the relative order of the remaining characters.

**Example:** "ace" is a subsequence of "abcde"

A **common subsequence** of two strings is a subsequence that is common to both strings.

---

## 🔹 Examples

**Example 1:**  
Input: `text1 = "abcde", text2 = "ace"`  
Output: `3`  
Explanation: The longest common subsequence is "ace" with length 3

**Example 2:**  
Input: `text1 = "abc", text2 = "abc"`  
Output: `3`  
Explanation: The longest common subsequence is "abc" with length 3

**Example 3:**  
Input: `text1 = "abc", text2 = "def"`  
Output: `0`  
Explanation: There is no common subsequence

**Example 4:**  
Input: `text1 = "abcdef", text2 = "fedcba"`  
Output: `1`  
Explanation: Multiple LCS of length 1 exist (a, b, c, d, e, or f)

**Example 5:**  
Input: `text1 = "programming", text2 = "gaming"`  
Output: `6`  
Explanation: LCS is "gaming"

---

## 🔹 Constraints
- `1 <= text1.length, text2.length <= 1000`
- `text1` and `text2` consist of only lowercase English characters

---

## 🔹 Core Intuition

Compare characters from both strings:

**If characters match:**
```
Include this character in LCS
dp[i][j] = 1 + dp[i-1][j-1]
```

**If characters don't match:**
```
Take max of excluding from either string
dp[i][j] = max(dp[i-1][j], dp[i][j-1])
```

**Base Case:**
```
dp[0][j] = 0  // Empty first string
dp[i][0] = 0  // Empty second string
```

---

## 1️⃣ Recursive Approach (Brute Force)

### Explanation
For each position, try matching characters:
- If match → include and recurse on remaining strings
- If no match → try excluding from either string, take max

### Code (Java)

```java
public class LongestCommonSubsequence {
    
    // Approach 1: Recursive (Brute Force)
    public int longestCommonSubsequence(String text1, String text2) {
        return lcsRecursive(text1, text2, text1.length(), text2.length());
    }
    
    private int lcsRecursive(String s1, String s2, int m, int n) {
        // Base case: empty string
        if (m == 0 || n == 0) {
            return 0;
        }
        
        // If last characters match
        if (s1.charAt(m - 1) == s2.charAt(n - 1)) {
            return 1 + lcsRecursive(s1, s2, m - 1, n - 1);
        }
        
        // If don't match, try excluding from either string
        return Math.max(
            lcsRecursive(s1, s2, m - 1, n),
            lcsRecursive(s1, s2, m, n - 1)
        );
    }
}
```

### Complexity
- **Time:** O(2^(m+n)) — exponential branching
- **Space:** O(m + n) — recursion depth

---

## 2️⃣ Recursive with Memoization (Top-Down DP)

### Explanation
Cache results in 2D memo array to avoid recomputation.

### Code (Java)

```java
public class LongestCommonSubsequence {
    
    // Approach 2: Memoization
    public int longestCommonSubsequenceMemo(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] memo = new int[m + 1][n + 1];
        
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        
        return lcsMemo(text1, text2, m, n, memo);
    }
    
    private int lcsMemo(String s1, String s2, int m, int n, int[][] memo) {
        if (m == 0 || n == 0) {
            return 0;
        }
        
        if (memo[m][n] != -1) {
            return memo[m][n];
        }
        
        if (s1.charAt(m - 1) == s2.charAt(n - 1)) {
            memo[m][n] = 1 + lcsMemo(s1, s2, m - 1, n - 1, memo);
        } else {
            memo[m][n] = Math.max(
                lcsMemo(s1, s2, m - 1, n, memo),
                lcsMemo(s1, s2, m, n - 1, memo)
            );
        }
        
        return memo[m][n];
    }
}
```

### Complexity
- **Time:** O(m × n)
- **Space:** O(m × n)

---

## 3️⃣ Dynamic Programming – Bottom-Up (Tabulation)

### Explanation
Build 2D DP table iteratively from base cases.

### Code (Java)

```java
public class LongestCommonSubsequence {
    
    // Approach 3: Bottom-Up DP (RECOMMENDED)
    public int longestCommonSubsequenceDP(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        // Base case: dp[0][*] = 0 and dp[*][0] = 0 (already initialized)
        
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
}
```

### DP Table Example

For `text1 = "abcde"`, `text2 = "ace"`:

|   |   | a | c | e |
|---|---|---|---|---|
|   | 0 | 0 | 0 | 0 |
| **a** | 0 | 1 | 1 | 1 |
| **b** | 0 | 1 | 1 | 1 |
| **c** | 0 | 1 | 2 | 2 |
| **d** | 0 | 1 | 2 | 2 |
| **e** | 0 | 1 | 2 | **3** |

### Complexity
- **Time:** O(m × n)
- **Space:** O(m × n)

---

## 4️⃣ Space Optimized (1D Array) ⭐

### Explanation
Only need current and previous row to compute DP values.

### Code (Java)

```java
public class LongestCommonSubsequence {
    
    // Approach 4: Space Optimized
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

### Complexity
- **Time:** O(m × n)
- **Space:** O(n) ⭐

---

## 🎯 Printing the LCS

To find the actual LCS string (not just length):

### Code (Java)

```java
public class LongestCommonSubsequence {
    
    // Print the actual LCS
    public String printLCS(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        // Build DP table
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        // Backtrack to find LCS
        StringBuilder lcs = new StringBuilder();
        int i = m, j = n;
        
        while (i > 0 && j > 0) {
            if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                lcs.append(text1.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        
        return lcs.reverse().toString();
    }
}
```

---

## 🔄 Variations with Code

### Variation 1: Shortest Common Supersequence

**Problem:** Find length of shortest string that has both strings as subsequences.

**Formula:** `length = m + n - LCS(s1, s2)`

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 1: Shortest Common Supersequence Length
    public int shortestCommonSupersequence(String str1, String str2) {
        int lcs = longestCommonSubsequenceDP(str1, str2);
        return str1.length() + str2.length() - lcs;
    }
    
    // Print actual SCS
    public String printSCS(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        StringBuilder scs = new StringBuilder();
        int i = m, j = n;
        
        while (i > 0 && j > 0) {
            if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                scs.append(str1.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                scs.append(str1.charAt(i - 1));
                i--;
            } else {
                scs.append(str2.charAt(j - 1));
                j--;
            }
        }
        
        while (i > 0) {
            scs.append(str1.charAt(i - 1));
            i--;
        }
        
        while (j > 0) {
            scs.append(str2.charAt(j - 1));
            j--;
        }
        
        return scs.reverse().toString();
    }
}
```

---

### Variation 2: Minimum Insertions/Deletions to Convert

**Problem:** Minimum operations to convert str1 to str2.

**Formula:**
- Deletions = `m - LCS`
- Insertions = `n - LCS`

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 2: Min Insertions and Deletions
    public int[] minOperations(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        int lcs = longestCommonSubsequenceDP(str1, str2);
        
        int deletions = m - lcs;
        int insertions = n - lcs;
        
        return new int[]{deletions, insertions};
    }
}
```

---

### Variation 3: Longest Repeating Subsequence

**Problem:** Find length of longest subsequence that repeats (appears twice).

**Key:** Use LCS on string with itself, but don't match same index.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 3: Longest Repeating Subsequence
    public int longestRepeatingSubsequence(String str) {
        int n = str.length();
        int[][] dp = new int[n + 1][n + 1];
        
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                // Match characters only if indices are different
                if (str.charAt(i - 1) == str.charAt(j - 1) && i != j) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[n][n];
    }
}
```

**Example:** str = "aabebcdd" → LRS = "abd" (length 3)

---

### Variation 4: LCS of Three Strings

**Problem:** Find LCS of three strings.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 4: LCS of Three Strings
    public int lcsOfThree(String s1, String s2, String s3) {
        int m = s1.length();
        int n = s2.length();
        int o = s3.length();
        
        int[][][] dp = new int[m + 1][n + 1][o + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                for (int k = 1; k <= o; k++) {
                    if (s1.charAt(i - 1) == s2.charAt(j - 1) && 
                        s2.charAt(j - 1) == s3.charAt(k - 1)) {
                        dp[i][j][k] = 1 + dp[i - 1][j - 1][k - 1];
                    } else {
                        dp[i][j][k] = Math.max(
                            dp[i - 1][j][k],
                            Math.max(dp[i][j - 1][k], dp[i][j][k - 1])
                        );
                    }
                }
            }
        }
        
        return dp[m][n][o];
    }
}
```

### Complexity
- **Time:** O(m × n × o)
- **Space:** O(m × n × o)

---

### Variation 5: Sequence Pattern Matching

**Problem:** Check if string A is subsequence of string B.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 5: Is Subsequence
    public boolean isSubsequence(String s, String t) {
        int lcs = longestCommonSubsequenceDP(s, t);
        return lcs == s.length();
    }
    
    // Optimized Two Pointer Approach
    public boolean isSubsequenceOptimized(String s, String t) {
        int i = 0, j = 0;
        
        while (i < s.length() && j < t.length()) {
            if (s.charAt(i) == t.charAt(j)) {
                i++;
            }
            j++;
        }
        
        return i == s.length();
    }
}
```

---

### Variation 6: Diff Utility (Find Differences)

**Problem:** Given two strings, find which characters to delete/insert.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 6: Diff Utility
    public List<String> findDiff(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        List<String> diff = new ArrayList<>();
        int i = m, j = n;
        
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && str1.charAt(i - 1) == str2.charAt(j - 1)) {
                diff.add("  " + str1.charAt(i - 1));  // Common
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                diff.add("+ " + str2.charAt(j - 1));  // Insert
                j--;
            } else if (i > 0) {
                diff.add("- " + str1.charAt(i - 1));  // Delete
                i--;
            }
        }
        
        Collections.reverse(diff);
        return diff;
    }
}
```

---

## 📊 Complexity Comparison

| Approach | Time | Space | Recommended |
|----------|------|-------|-------------|
| Recursive | O(2^(m+n)) | O(m+n) | ❌ Never |
| Memoization | O(m×n) | O(m×n) | ✅ Good |
| Tabulation | O(m×n) | O(m×n) | ⭐ Best |
| Space Optimized | O(m×n) | O(n) | ⭐ Best for length only |

---

## 🎓 Complete Implementation

```java
import java.util.*;

public class LCSComplete {
    
    // Main LCS function
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
    
    public static void main(String[] args) {
        LCSComplete lcs = new LCSComplete();
        
        String text1 = "abcde";
        String text2 = "ace";
        
        System.out.println("LCS Length: " + lcs.longestCommonSubsequence(text1, text2));
    }
}
```

---

## 🎯 Key Takeaways

1. **Match:** Include character, move both pointers
2. **No Match:** Try excluding from either, take max
3. **Foundation:** LCS is basis for many string DP problems
4. **Applications:** Diff tools, DNA sequencing, plagiarism detection
5. **Related:** Edit distance uses similar pattern

---

## 💡 Real-World Applications

### 1. **Version Control (Git Diff)**
Show differences between file versions

### 2. **DNA Sequence Alignment**
Find similarity between genetic sequences

### 3. **Plagiarism Detection**
Measure text similarity

### 4. **File Comparison Tools**
Identify common content

### 5. **Spell Checkers**
Find closest matching words

---

## 🌟 Interview Tips

1. **Draw DP table** for small examples
2. **Explain recurrence** clearly
3. **Know variations** (SCS, min insertions/deletions)
4. **Space optimization** shows advanced understanding
5. **Backtracking** to print actual LCS
6. **Connection to Edit Distance**

**LCS is a fundamental DP pattern** — master it to solve many related problems! 🚀
