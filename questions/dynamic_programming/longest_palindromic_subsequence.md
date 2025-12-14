# Longest Palindromic Subsequence – Detailed Explanation and Approaches

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given a string `s`, find the **longest palindromic subsequence's length** in `s`.

A **subsequence** is a sequence that can be derived from another sequence by deleting some or no elements without changing the order of the remaining elements.

---

## 🔹 Examples

**Example 1:**  
Input: `s = "bbbab"`  
Output: `4`  
Explanation: LPS is "bbbb"

**Example 2:**  
Input: `s = "cbbd"`  
Output: `2`  
Explanation: LPS is "bb"

**Example 3:**  
Input: `s = "racecar"`  
Output: `7`  
Explanation: LPS is "racecar" (entire string)

**Example 4:**  
Input: `s = "abcde"`  
Output: `1`  
Explanation: Any single character (a, b, c, d, or e)

---

## 🔹 Constraints
- `1 <= s.length <= 1000`
- `s` consists only of lowercase English letters

---

## 🔹 Core Intuition

**Key Insight:** LPS(s) = LCS(s, reverse(s))

But we can solve it more elegantly using interval DP:

**If characters match at boundaries:**
```
dp[i][j] = 2 + dp[i+1][j-1]
```

**If characters don't match:**
```
dp[i][j] = max(dp[i+1][j], dp[i][j-1])
```

**Base Cases:**
```
dp[i][i] = 1  // Single character
dp[i][i+1] = 2 if s[i]==s[i+1], else 1
```

---

## 1️⃣ Recursive Approach (Brute Force)

### Explanation
Check characters at both ends, recurse on middle portion.

### Code (Java)

```java
public class LongestPalindromicSubsequence {
    
    // Approach 1: Recursive
    public int longestPalindromeSubseq(String s) {
        return lpsRecursive(s, 0, s.length() - 1);
    }
    
    private int lpsRecursive(String s, int left, int right) {
        // Base cases
        if (left > right) return 0;
        if (left == right) return 1;
        
        // If characters match
        if (s.charAt(left) == s.charAt(right)) {
            return 2 + lpsRecursive(s, left + 1, right - 1);
        }
        
        // If don't match, try excluding from either end
        return Math.max(
            lpsRecursive(s, left + 1, right),
            lpsRecursive(s, left, right - 1)
        );
    }
}
```

### Complexity
- **Time:** O(2^n) — exponential
- **Space:** O(n) — recursion depth

---

## 2️⃣ Recursive with Memoization (Top-Down DP)

### Explanation
Cache results for each (left, right) pair.

### Code (Java)

```java
public class LongestPalindromicSubsequence {
    
    // Approach 2: Memoization
    public int longestPalindromeSubseqMemo(String s) {
        int n = s.length();
        int[][] memo = new int[n][n];
        
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        
        return lpsMemo(s, 0, n - 1, memo);
    }
    
    private int lpsMemo(String s, int left, int right, int[][] memo) {
        if (left > right) return 0;
        if (left == right) return 1;
        
        if (memo[left][right] != -1) {
            return memo[left][right];
        }
        
        if (s.charAt(left) == s.charAt(right)) {
            memo[left][right] = 2 + lpsMemo(s, left + 1, right - 1, memo);
        } else {
            memo[left][right] = Math.max(
                lpsMemo(s, left + 1, right, memo),
                lpsMemo(s, left, right - 1, memo)
            );
        }
        
        return memo[left][right];
    }
}
```

### Complexity
- **Time:** O(n²)
- **Space:** O(n²)

---

## 3️⃣ Dynamic Programming – Bottom-Up (Tabulation)

### Explanation
Build DP table diagonally, starting from small substrings to larger ones.

### Code (Java)

```java
public class LongestPalindromicSubsequence {
    
    // Approach 3: Bottom-Up DP
    public int longestPalindromeSubseqDP(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        
        // Base case: single characters
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }
        
        // Fill table for substrings of increasing length
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                
                if (s.charAt(i) == s.charAt(j)) {
                    dp[i][j] = 2 + (len == 2 ? 0 : dp[i + 1][j - 1]);
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[0][n - 1];
    }
}
```

### DP Table Example

For `s = "bbbab"`:

|  | b | b | b | a | b |
|---|---|---|---|---|---|
| **b** | 1 | 2 | 3 | 3 | **4** |
| **b** |   | 1 | 2 | 2 | 3 |
| **b** |   |   | 1 | 1 | 3 |
| **a** |   |   |   | 1 | 1 |
| **b** |   |   |   |   | 1 |

**Answer:** dp[0][4] = **4**

### Complexity
- **Time:** O(n²)
- **Space:** O(n²)

---

## 4️⃣ Using LCS (Alternative Approach)

### Explanation
LPS(s) = LCS(s, reverse(s))

### Code (Java)

```java
public class LongestPalindromicSubsequence {
    
    // Approach 4: Using LCS
    public int longestPalindromeSubseqLCS(String s) {
        String rev = new StringBuilder(s).reverse().toString();
        return lcs(s, rev);
    }
    
    private int lcs(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
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

### Complexity
- **Time:** O(n²)
- **Space:** O(n²)

---

## 🎯 Printing the LPS

To find the actual palindromic subsequence:

### Code (Java)

```java
public class LongestPalindromicSubsequence {
    
    // Print actual LPS
    public String printLPS(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }
        
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                
                if (s.charAt(i) == s.charAt(j)) {
                    dp[i][j] = 2 + (len == 2 ? 0 : dp[i + 1][j - 1]);
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
        }
        
        // Backtrack to find LPS
        return backtrack(s, dp, 0, n - 1);
    }
    
    private String backtrack(String s, int[][] dp, int i, int j) {
        if (i > j) return "";
        if (i == j) return String.valueOf(s.charAt(i));
        
        if (s.charAt(i) == s.charAt(j)) {
            return s.charAt(i) + backtrack(s, dp, i + 1, j - 1) + s.charAt(j);
        }
        
        if (dp[i + 1][j] > dp[i][j - 1]) {
            return backtrack(s, dp, i + 1, j);
        } else {
            return backtrack(s, dp, i, j - 1);
        }
    }
}
```

---

## 🔄 Variations with Code

### Variation 1: Minimum Insertions to Make Palindrome

**Problem:** Minimum characters to insert to make string palindrome.

**Formula:** `n - LPS(s)`

#### Code (Java)

```java
public class LPSVariations {
    
    // Variation 1: Minimum Insertions
    public int minInsertions(String s) {
        int lps = longestPalindromeSubseqDP(s);
        return s.length() - lps;
    }
}
```

**Example:** s = "abcde" → LPS = 1 → Insertions = 5-1 = 4  
Result: "edcbabcde"

---

### Variation 2: Minimum Deletions to Make Palindrome

**Problem:** Minimum characters to delete to make string palindrome.

**Formula:** `n - LPS(s)`

#### Code (Java)

```java
public class LPSVariations {
    
    // Variation 2: Minimum Deletions
    public int minDeletions(String s) {
        int lps = longestPalindromeSubseqDP(s);
        return s.length() - lps;
    }
}
```

**Example:** s = "abcde" → LPS = 1 → Deletions = 5-1 = 4

---

### Variation 3: Count Palindromic Subsequences

**Problem:** Count all distinct palindromic subsequences (mod 10^9+7).

#### Code (Java)

```java
public class LPSVariations {
    
    // Variation 3: Count Palindromic Subsequences
    private static final int MOD = 1000000007;
    
    public int countPalindromicSubsequences(String s) {
        int n = s.length();
        long[][] dp = new long[n][n];
        
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }
        
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                
                if (s.charAt(i) == s.charAt(j)) {
                    int left = i + 1;
                    int right = j - 1;
                    
                    while (left <= right && s.charAt(left) != s.charAt(i)) left++;
                    while (left <= right && s.charAt(right) != s.charAt(i)) right--;
                    
                    if (left > right) {
                        dp[i][j] = (2 * dp[i + 1][j - 1] + 2) % MOD;
                    } else if (left == right) {
                        dp[i][j] = (2 * dp[i + 1][j - 1] + 1) % MOD;
                    } else {
                        dp[i][j] = (2 * dp[i + 1][j - 1] - dp[left + 1][right - 1] + MOD) % MOD;
                    }
                } else {
                    dp[i][j] = (dp[i + 1][j] + dp[i][j - 1] - dp[i + 1][j - 1] + MOD) % MOD;
                }
            }
        }
        
        return (int)dp[0][n - 1];
    }
}
```

---

### Variation 4: Longest Palindromic Substring

**Problem:** Find longest palindrome that is a substring (consecutive characters).

#### Code (Java)

```java
public class LPSVariations {
    
    // Variation 4: Longest Palindromic Substring
    public String longestPalindrome(String s) {
        if (s == null || s.length() < 1) return "";
        
        int start = 0, end = 0;
        
        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i);      // Odd length
            int len2 = expandAroundCenter(s, i, i + 1);  // Even length
            int len = Math.max(len1, len2);
            
            if (len > end - start) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }
        
        return s.substring(start, end + 1);
    }
    
    private int expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return right - left - 1;
    }
    
    // DP Approach
    public String longestPalindromeDP(String s) {
        int n = s.length();
        boolean[][] dp = new boolean[n][n];
        int start = 0, maxLen = 1;
        
        // Single characters
        for (int i = 0; i < n; i++) {
            dp[i][i] = true;
        }
        
        // Two characters
        for (int i = 0; i < n - 1; i++) {
            if (s.charAt(i) == s.charAt(i + 1)) {
                dp[i][i + 1] = true;
                start = i;
                maxLen = 2;
            }
        }
        
        // Longer substrings
        for (int len = 3; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                
                if (s.charAt(i) == s.charAt(j) && dp[i + 1][j - 1]) {
                    dp[i][j] = true;
                    start = i;
                    maxLen = len;
                }
            }
        }
        
        return s.substring(start, start + maxLen);
    }
}
```

---

### Variation 5: Palindrome Partitioning II

**Problem:** Minimum cuts needed to partition string into palindromes.

#### Code (Java)

```java
public class LPSVariations {
    
    // Variation 5: Palindrome Partitioning II
    public int minCut(String s) {
        int n = s.length();
        boolean[][] isPalin = new boolean[n][n];
        int[] cuts = new int[n];
        
        // Build palindrome table
        for (int i = 0; i < n; i++) {
            int minCut = i;
            
            for (int j = 0; j <= i; j++) {
                if (s.charAt(j) == s.charAt(i) && (i - j <= 1 || isPalin[j + 1][i - 1])) {
                    isPalin[j][i] = true;
                    minCut = (j == 0) ? 0 : Math.min(minCut, cuts[j - 1] + 1);
                }
            }
            
            cuts[i] = minCut;
        }
        
        return cuts[n - 1];
    }
}
```

---

### Variation 6: Palindromic Substrings Count

**Problem:** Count all palindromic substrings.

#### Code (Java)

```java
public class LPSVariations {
    
    // Variation 6: Count Palindromic Substrings
    public int countSubstrings(String s) {
        int n = s.length();
        int count = 0;
        
        for (int i = 0; i < n; i++) {
            // Odd length palindromes
            count += expandAndCount(s, i, i);
            // Even length palindromes
            count += expandAndCount(s, i, i + 1);
        }
        
        return count;
    }
    
    private int expandAndCount(String s, int left, int right) {
        int count = 0;
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            count++;
            left--;
            right++;
        }
        return count;
    }
}
```

---

## 📊 Complexity Comparison

| Approach | Time | Space | Recommended |
|----------|------|-------|-------------|
| Recursive | O(2^n) | O(n) | ❌ Never |
| Memoization | O(n²) | O(n²) | ✅ Good |
| Tabulation | O(n²) | O(n²) | ⭐ Best |
| LCS Method | O(n²) | O(n²) | ✅ Alternative |

---

## 🎓 Complete Implementation

```java
import java.util.*;

public class LPSComplete {
    
    // Optimal DP solution
    public int longestPalindromeSubseq(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }
        
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                
                if (s.charAt(i) == s.charAt(j)) {
                    dp[i][j] = 2 + (len == 2 ? 0 : dp[i + 1][j - 1]);
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[0][n - 1];
    }
    
    public static void main(String[] args) {
        LPSComplete lps = new LPSComplete();
        
        String s = "bbbab";
        System.out.println("LPS Length: " + lps.longestPalindromeSubseq(s));
    }
}
```

---

## 🎯 Key Takeaways

1. **Interval DP:** Process from ends inward
2. **Match:** Include both characters, recurse on middle
3. **No Match:** Try excluding from either end
4. **Alternative:** Can use LCS with reverse
5. **Many Applications:** Insertions, deletions, partitioning

---

## 💡 Pattern Recognition

**LPS pattern applies to:**
- Minimum insertions/deletions
- Palindrome partitioning
- Count palindromic subsequences
- Making string palindrome with min operations

---

## 🌟 Interview Tips

1. **Explain interval DP** approach clearly
2. **Compare with LCS** method
3. **Know formula** for min insertions/deletions
4. **Distinguish:** Subsequence vs substring
5. **DP table** helps visualize
6. **Space optimization** possible but complex

**LPS is a classic interval DP problem** — master the pattern! 🚀
