# Interleaving String – Detailed Explanation

![](../../assets/images/dynamic_programming/interleaving_string.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given strings `s1`, `s2`, and `s3`, find whether `s3` is formed by an **interleaving** of `s1` and `s2`.

An **interleaving** of two strings `s` and `t` is a configuration where they are divided into `n` and `m` substrings respectively, and the substrings are merged alternately to form `s3`.

---

## 🔹 Examples

**Example 1:**  
Input: `s1 = "aabcc", s2 = "dbbca", s3 = "aadbbcbcac"`  
Output: `true`  
Explanation: Interleaving: "aa" + "dbbc" + "bc" + "a" + "c"

**Example 2:**  
Input: `s1 = "aabcc", s2 = "dbbca", s3 = "aadbbbaccc"`  
Output: `false`

**Example 3:**  
Input: `s1 = "", s2 = "", s3 = ""`  
Output: `true`

---

## 🔹 Core Intuition

**Recurrence:**
```
dp[i][j] = can form s3[0..i+j-1] using s1[0..i-1] and s2[0..j-1]

dp[i][j] = true if:
  - (dp[i-1][j] && s1[i-1] == s3[i+j-1]) OR
  - (dp[i][j-1] && s2[j-1] == s3[i+j-1])
```

**Base Case:**
```
dp[0][0] = true (empty strings)
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class InterleavingString {
    
    public boolean isInterleaveRecursive(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) return false;
        
        return helper(s1, s2, s3, 0, 0, 0);
    }
    
    private boolean helper(String s1, String s2, String s3, int i, int j, int k) {
        if (k == s3.length()) {
            return i == s1.length() && j == s2.length();
        }
        
        boolean useS1 = false, useS2 = false;
        
        if (i < s1.length() && s1.charAt(i) == s3.charAt(k)) {
            useS1 = helper(s1, s2, s3, i + 1, j, k + 1);
        }
        
        if (j < s2.length() && s2.charAt(j) == s3.charAt(k)) {
            useS2 = helper(s1, s2, s3, i, j + 1, k + 1);
        }
        
        return useS1 || useS2;
    }
}
```

---

## 2️⃣ Memoization (Top-Down DP)

### Code (Java)

```java
public class InterleavingString {
    
    public boolean isInterleaveMemo(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) return false;
        
        Boolean[][] memo = new Boolean[s1.length() + 1][s2.length() + 1];
        return helperMemo(s1, s2, s3, 0, 0, memo);
    }
    
    private boolean helperMemo(String s1, String s2, String s3, int i, int j, Boolean[][] memo) {
        if (i + j == s3.length()) {
            return true;
        }
        
        if (memo[i][j] != null) {
            return memo[i][j];
        }
        
        boolean result = false;
        
        if (i < s1.length() && s1.charAt(i) == s3.charAt(i + j)) {
            result = helperMemo(s1, s2, s3, i + 1, j, memo);
        }
        
        if (!result && j < s2.length() && s2.charAt(j) == s3.charAt(i + j)) {
            result = helperMemo(s1, s2, s3, i, j + 1, memo);
        }
        
        memo[i][j] = result;
        return result;
    }
}
```

---

## 3️⃣ Dynamic Programming – Bottom-Up

### Code (Java)

```java
public class InterleavingString {
    
    public boolean isInterleaveDP(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) return false;
        
        int m = s1.length();
        int n = s2.length();
        boolean[][] dp = new boolean[m + 1][n + 1];
        
        dp[0][0] = true;
        
        // Fill first column (using only s1)
        for (int i = 1; i <= m; i++) {
            dp[i][0] = dp[i - 1][0] && s1.charAt(i - 1) == s3.charAt(i - 1);
        }
        
        // Fill first row (using only s2)
        for (int j = 1; j <= n; j++) {
            dp[0][j] = dp[0][j - 1] && s2.charAt(j - 1) == s3.charAt(j - 1);
        }
        
        // Fill rest of table
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = (dp[i - 1][j] && s1.charAt(i - 1) == s3.charAt(i + j - 1)) ||
                           (dp[i][j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1));
            }
        }
        
        return dp[m][n];
    }
}
```

### DP Table Example

For `s1 = "aabcc"`, `s2 = "dbbca"`, `s3 = "aadbbcbcac"`:

|  |  | d | b | b | c | a |
|---|---|---|---|---|---|---|
|  | ✓ | F | F | F | F | F |
| **a** | ✓ | F | F | F | F | F |
| **a** | ✓ | ✓ | ✓ | ✓ | ✓ | F |
| **b** | F | ✓ | ✓ | F | ✓ | F |
| **c** | F | F | ✓ | ✓ | ✓ | ✓ |
| **c** | F | F | F | ✓ | F | **✓** |

**Answer:** dp[5][5] = **true**

---

## 4️⃣ Space Optimized – O(n) ⭐ OPTIMAL

### Code (Java)

```java
public class InterleavingString {
    
    public boolean isInterleave(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) return false;
        
        int m = s1.length();
        int n = s2.length();
        
        boolean[] dp = new boolean[n + 1];
        
        dp[0] = true;
        
        for (int j = 1; j <= n; j++) {
            dp[j] = dp[j - 1] && s2.charAt(j - 1) == s3.charAt(j - 1);
        }
        
        for (int i = 1; i <= m; i++) {
            dp[0] = dp[0] && s1.charAt(i - 1) == s3.charAt(i - 1);
            
            for (int j = 1; j <= n; j++) {
                dp[j] = (dp[j] && s1.charAt(i - 1) == s3.charAt(i + j - 1)) ||
                        (dp[j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1));
            }
        }
        
        return dp[n];
    }
}
```

---

## 🔄 Variations

### Variation 1: Count All Interleaving Ways

```java
public int countInterleavings(String s1, String s2, String s3) {
    if (s1.length() + s2.length() != s3.length()) return 0;
    
    int m = s1.length();
    int n = s2.length();
    int[][] dp = new int[m + 1][n + 1];
    
    dp[0][0] = 1;
    
    for (int i = 1; i <= m; i++) {
        if (s1.charAt(i - 1) == s3.charAt(i - 1)) {
            dp[i][0] = dp[i - 1][0];
        }
    }
    
    for (int j = 1; j <= n; j++) {
        if (s2.charAt(j - 1) == s3.charAt(j - 1)) {
            dp[0][j] = dp[0][j - 1];
        }
    }
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            int ways = 0;
            
            if (s1.charAt(i - 1) == s3.charAt(i + j - 1)) {
                ways += dp[i - 1][j];
            }
            
            if (s2.charAt(j - 1) == s3.charAt(i + j - 1)) {
                ways += dp[i][j - 1];
            }
            
            dp[i][j] = ways;
        }
    }
    
    return dp[m][n];
}
```

### Variation 2: Scramble String

```java
public boolean isScramble(String s1, String s2) {
    if (s1.equals(s2)) return true;
    if (s1.length() != s2.length()) return false;
    
    int n = s1.length();
    boolean[][][] dp = new boolean[n][n][n + 1];
    
    // Base case: length 1
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            dp[i][j][1] = s1.charAt(i) == s2.charAt(j);
        }
    }
    
    // Fill for lengths 2 to n
    for (int len = 2; len <= n; len++) {
        for (int i = 0; i <= n - len; i++) {
            for (int j = 0; j <= n - len; j++) {
                for (int k = 1; k < len; k++) {
                    if ((dp[i][j][k] && dp[i + k][j + k][len - k]) ||
                        (dp[i][j + len - k][k] && dp[i + k][j][len - k])) {
                        dp[i][j][len] = true;
                        break;
                    }
                }
            }
        }
    }
    
    return dp[0][0][n];
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^(m+n)) | O(m+n) |
| Memoization | O(m×n) | O(m×n) |
| DP 2D | O(m×n) | O(m×n) |
| Space Optimized | O(m×n) | O(n) ⭐ |

---

## 🎯 Key Takeaways

1. **Length check:** Essential first step
2. **Two choices:** Take from s1 or s2
3. **2D DP:** Track positions in both strings
4. **Space optimization:** Only need one row
5. **Similar problems:** Edit distance, scramble string

**Classic 2D DP string matching problem!** 🚀
