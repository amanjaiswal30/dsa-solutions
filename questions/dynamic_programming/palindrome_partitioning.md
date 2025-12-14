# Palindrome Partitioning II (Min Cuts) – Detailed Explanation

**Difficulty:** Hard 🔥

---

## 🔹 Problem Statement

Given a string `s`, partition `s` such that every substring of the partition is a **palindrome**.

Return the **minimum cuts** needed for a palindrome partitioning of `s`.

---

## 🔹 Examples

**Example 1:**  
Input: `s = "aab"`  
Output: `1`  
Explanation: Partition into ["aa", "b"]

**Example 2:**  
Input: `s = "a"`  
Output: `0`  
Explanation: Already a palindrome

**Example 3:**  
Input: `s = "ab"`  
Output: `1`  
Explanation: Partition into ["a", "b"]

**Example 4:**  
Input: `s = "aabbaa"`  
Output: `0`  
Explanation: Entire string is palindrome

---

## 🔹 Core Intuition

**Two-step approach:**
1. Build palindrome table: `isPalin[i][j]` = is substring [i..j] palindrome?
2. Build DP for min cuts: `cuts[i]` = min cuts for s[0..i]

**Recurrence:**
```
cuts[i] = minimum cuts for s[0..i]

If s[0..i] is palindrome: cuts[i] = 0
Else: cuts[i] = min(cuts[j] + 1) where s[j+1..i] is palindrome
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class PalindromePartitioning {
    
    public int minCutRecursive(String s) {
        return minCutHelper(s, 0, s.length() - 1);
    }
    
    private int minCutHelper(String s, int start, int end) {
        if (start >= end || isPalindrome(s, start, end)) {
            return 0;
        }
        
        int minCuts = Integer.MAX_VALUE;
        
        for (int k = start; k < end; k++) {
            if (isPalindrome(s, start, k)) {
                minCuts = Math.min(minCuts, 1 + minCutHelper(s, k + 1, end));
            }
        }
        
        return minCuts;
    }
    
    private boolean isPalindrome(String s, int i, int j) {
        while (i < j) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }
}
```

---

## 2️⃣ Dynamic Programming – O(n²) ⭐ OPTIMAL

### Code (Java)

```java
public class PalindromePartitioning {
    
    public int minCut(String s) {
        int n = s.length();
        
        // Build palindrome table
        boolean[][] isPalin = new boolean[n][n];
        
        for (int i = 0; i < n; i++) {
            isPalin[i][i] = true;
        }
        
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                
                if (s.charAt(i) == s.charAt(j)) {
                    isPalin[i][j] = (len == 2) || isPalin[i + 1][j - 1];
                }
            }
        }
        
        // Build cuts array
        int[] cuts = new int[n];
        
        for (int i = 0; i < n; i++) {
            if (isPalin[0][i]) {
                cuts[i] = 0;
            } else {
                cuts[i] = i;  // Max cuts possible
                
                for (int j = 0; j < i; j++) {
                    if (isPalin[j + 1][i]) {
                        cuts[i] = Math.min(cuts[i], cuts[j] + 1);
                    }
                }
            }
        }
        
        return cuts[n - 1];
    }
}
```

### Example Walkthrough

For `s = "aab"`:

Palindrome table:
```
    a  a  b
a [ T  T  F ]
a [    T  F ]
b [       T ]
```

Cuts array:
- cuts[0] = 0 (a is palindrome)
- cuts[1] = 0 (aa is palindrome)
- cuts[2] = 1 (aa | b)

**Answer:** **1**

### Complexity
- **Time:** O(n²)
- **Space:** O(n²)

---

## 3️⃣ Optimized DP (Expand Around Center)

### Code (Java)

```java
public class PalindromePartitioning {
    
    public int minCutOptimized(String s) {
        int n = s.length();
        int[] cuts = new int[n];
        
        for (int i = 0; i < n; i++) {
            cuts[i] = i;  // Max cuts
        }
        
        for (int center = 0; center < n; center++) {
            // Odd length palindromes
            expandAndUpdate(s, center, center, cuts);
            
            // Even length palindromes
            expandAndUpdate(s, center, center + 1, cuts);
        }
        
        return cuts[n - 1];
    }
    
    private void expandAndUpdate(String s, int left, int right, int[] cuts) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            int newCut = (left == 0) ? 0 : cuts[left - 1] + 1;
            cuts[right] = Math.min(cuts[right], newCut);
            left--;
            right++;
        }
    }
}
```

---

## 🔄 Variations

### Variation 1: Palindrome Partitioning I (All Partitions)

**Problem:** Return all possible palindrome partitioning.

```java
public class PalindromePartitioningVariations {
    
    public List<List<String>> partition(String s) {
        List<List<String>> result = new ArrayList<>();
        backtrack(s, 0, new ArrayList<>(), result);
        return result;
    }
    
    private void backtrack(String s, int start, List<String> current, List<List<String>> result) {
        if (start == s.length()) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int end = start; end < s.length(); end++) {
            if (isPalindrome(s, start, end)) {
                current.add(s.substring(start, end + 1));
                backtrack(s, end + 1, current, result);
                current.remove(current.size() - 1);
            }
        }
    }
    
    private boolean isPalindrome(String s, int i, int j) {
        while (i < j) {
            if (s.charAt(i) != s.charAt(j)) return false;
            i++;
            j--;
        }
        return true;
    }
}
```

### Variation 2: Palindrome Partitioning III (K Partitions with Changes)

```java
public int palindromePartition(String s, int k) {
    int n = s.length();
    
    // cost[i][j] = min changes to make s[i..j] palindrome
    int[][] cost = new int[n][n];
    
    for (int len = 2; len <= n; len++) {
        for (int i = 0; i <= n - len; i++) {
            int j = i + len - 1;
            cost[i][j] = cost[i + 1][j - 1] + (s.charAt(i) == s.charAt(j) ? 0 : 1);
        }
    }
    
    // dp[i][p] = min cost to partition s[0..i] into p palindromes
    int[][] dp = new int[n][k + 1];
    
    for (int[] row : dp) {
        Arrays.fill(row, n);
    }
    
    for (int i = 0; i < n; i++) {
        dp[i][1] = cost[0][i];
        
        for (int p = 2; p <= Math.min(i + 1, k); p++) {
            for (int j = p - 2; j < i; j++) {
                dp[i][p] = Math.min(dp[i][p], dp[j][p - 1] + cost[j + 1][i]);
            }
        }
    }
    
    return dp[n - 1][k];
}
```

### Variation 3: Longest Palindromic Decomposition

```java
public int longestDecomposition(String text) {
    int n = text.length();
    
    return decompose(text, 0, n - 1);
}

private int decompose(String s, int left, int right) {
    if (left > right) return 0;
    
    for (int len = 1; len <= (right - left + 1) / 2; len++) {
        if (s.substring(left, left + len).equals(s.substring(right - len + 1, right + 1))) {
            return 2 + decompose(s, left + len, right - len);
        }
    }
    
    return 1;
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^n) | O(n) |
| DP with Palindrome Table | O(n²) | O(n²) ⭐ |
| Expand Around Center | O(n²) | O(n) ⭐ |

---

## 🎯 Key Takeaways

1. **Two-step DP:** Build palindrome table first
2. **Expand around center:** Space optimization
3. **Min cuts formula:** Try all possible partitions
4. **Variations:** All partitions, K partitions with changes
5. **Applications:** Text segmentation, pattern matching

**Classic interval DP with palindrome checking!** 🚀
