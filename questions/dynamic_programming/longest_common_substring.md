# Longest Common Substring – Detailed Explanation and Approaches

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given two strings `text1` and `text2`, return the **length of their longest common substring**.

A **substring** is a contiguous sequence of characters within a string. Unlike subsequence, we cannot skip characters.

If there is no common substring, return `0`.

---

## 🔹 Examples

**Example 1:**  
Input: `text1 = "abcde", text2 = "abfce"`  
Output: `2`  
Explanation: LCS substring is "ab"

**Example 2:**  
Input: `text1 = "abc", text2 = "bcd"`  
Output: `2`  
Explanation: LCS substring is "bc"

**Example 3:**  
Input: `text1 = "abc", text2 = "def"`  
Output: `0`  
Explanation: No common substring

**Example 4:**  
Input: `text1 = "ABCDGH", text2 = "ACDGHR"`  
Output: `4`  
Explanation: LCS substring is "CDGH"

---

## 🔹 Constraints
- `1 <= text1.length, text2.length <= 1000`
- `text1` and `text2` consist of lowercase/uppercase English letters

---

## 🔹 Core Intuition

**Key Difference from LCS (Subsequence):**
- Subsequence: Can skip characters → dp[i][j] considers max of excluding
- Substring: Must be contiguous → dp[i][j] resets to 0 if mismatch

**If characters match:**
```
dp[i][j] = 1 + dp[i-1][j-1]
```

**If characters don't match:**
```
dp[i][j] = 0  // Reset for substring
```

**Answer:** `max(dp[i][j])` for all i, j

---

## 1️⃣ Dynamic Programming – O(m×n) Solution

### Explanation
Build 2D DP table where `dp[i][j]` = length of common substring ending at `text1[i-1]` and `text2[j-1]`.

### Code (Java)

```java
public class LongestCommonSubstring {
    
    // Approach 1: DP O(m×n)
    public int longestCommonSubstr(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        int maxLen = 0;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                    maxLen = Math.max(maxLen, dp[i][j]);
                } else {
                    dp[i][j] = 0;  // Key difference from LCS
                }
            }
        }
        
        return maxLen;
    }
}
```

### DP Table Example

For `text1 = "abcde"`, `text2 = "abfce"`:

|   |   | a | b | f | c | e |
|---|---|---|---|---|---|---|
|   | 0 | 0 | 0 | 0 | 0 | 0 |
| **a** | 0 | 1 | 0 | 0 | 0 | 0 |
| **b** | 0 | 0 | **2** | 0 | 0 | 0 |
| **c** | 0 | 0 | 0 | 0 | 1 | 0 |
| **d** | 0 | 0 | 0 | 0 | 0 | 0 |
| **e** | 0 | 0 | 0 | 0 | 0 | 1 |

**Answer:** max(dp) = **2** ("ab")

### Complexity
- **Time:** O(m × n)
- **Space:** O(m × n)

---

## 2️⃣ Space Optimized – O(n) Space ⭐

### Explanation
Only need previous row to compute current row.

### Code (Java)

```java
public class LongestCommonSubstring {
    
    // Approach 2: Space Optimized
    public int longestCommonSubstrOptimized(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        int maxLen = 0;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    curr[j] = 1 + prev[j - 1];
                    maxLen = Math.max(maxLen, curr[j]);
                } else {
                    curr[j] = 0;
                }
            }
            
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return maxLen;
    }
}
```

### Complexity
- **Time:** O(m × n)
- **Space:** O(n) ⭐

---

## 3️⃣ Printing the Longest Common Substring

### Code (Java)

```java
public class LongestCommonSubstring {
    
    // Print actual substring
    public String printLCS(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        int maxLen = 0;
        int endIndex = 0;  // Ending index in text1
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                    
                    if (dp[i][j] > maxLen) {
                        maxLen = dp[i][j];
                        endIndex = i;
                    }
                }
            }
        }
        
        if (maxLen == 0) return "";
        
        return text1.substring(endIndex - maxLen, endIndex);
    }
}
```

---

## 4️⃣ Suffix Array Approach (Advanced)

### Explanation
Build suffix array and find LCP (Longest Common Prefix).

### Code (Java)

```java
public class LongestCommonSubstring {
    
    // Approach 4: Using Suffix Array (Advanced)
    public int longestCommonSubstrSuffixArray(String s1, String s2) {
        String combined = s1 + "#" + s2 + "$";
        int n = combined.length();
        
        // Build suffix array
        String[] suffixes = new String[n];
        for (int i = 0; i < n; i++) {
            suffixes[i] = combined.substring(i);
        }
        
        Arrays.sort(suffixes);
        
        int maxLen = 0;
        int len1 = s1.length();
        
        // Find LCP of adjacent suffixes from different strings
        for (int i = 0; i < n - 1; i++) {
            int idx1 = n - suffixes[i].length();
            int idx2 = n - suffixes[i + 1].length();
            
            // Check if from different strings
            if ((idx1 < len1 && idx2 > len1) || (idx1 > len1 && idx2 < len1)) {
                int lcp = getLCP(suffixes[i], suffixes[i + 1]);
                maxLen = Math.max(maxLen, lcp);
            }
        }
        
        return maxLen;
    }
    
    private int getLCP(String s1, String s2) {
        int len = 0;
        int minLen = Math.min(s1.length(), s2.length());
        
        for (int i = 0; i < minLen; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                len++;
            } else {
                break;
            }
        }
        
        return len;
    }
}
```

### Complexity
- **Time:** O((m+n) log(m+n))
- **Space:** O(m + n)

---

## 🔄 Variations with Code

### Variation 1: All Longest Common Substrings

**Problem:** Find all LCS (in case of ties).

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 1: All Longest Common Substrings
    public List<String> allLongestCommonSubstrings(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        int maxLen = 0;
        List<String> result = new ArrayList<>();
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                    
                    if (dp[i][j] > maxLen) {
                        maxLen = dp[i][j];
                        result.clear();
                        result.add(text1.substring(i - maxLen, i));
                    } else if (dp[i][j] == maxLen) {
                        String substr = text1.substring(i - maxLen, i);
                        if (!result.contains(substr)) {
                            result.add(substr);
                        }
                    }
                }
            }
        }
        
        return result;
    }
}
```

---

### Variation 2: K Common Substrings

**Problem:** Find longest substring common to K strings.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 2: LCS of K strings
    public int longestCommonSubstringK(String[] strings) {
        if (strings == null || strings.length == 0) return 0;
        if (strings.length == 1) return strings[0].length();
        
        String first = strings[0];
        int maxLen = 0;
        
        // Check all substrings of first string
        for (int i = 0; i < first.length(); i++) {
            for (int j = i + 1; j <= first.length(); j++) {
                String substr = first.substring(i, j);
                
                boolean foundInAll = true;
                for (int k = 1; k < strings.length; k++) {
                    if (!strings[k].contains(substr)) {
                        foundInAll = false;
                        break;
                    }
                }
                
                if (foundInAll) {
                    maxLen = Math.max(maxLen, substr.length());
                }
            }
        }
        
        return maxLen;
    }
}
```

---

### Variation 3: Longest Repeating Substring

**Problem:** Find longest substring that appears at least twice.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 3: Longest Repeating Substring
    public int longestRepeatingSubstring(String s) {
        int n = s.length();
        int[][] dp = new int[n + 1][n + 1];
        int maxLen = 0;
        
        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                if (s.charAt(i - 1) == s.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                    maxLen = Math.max(maxLen, dp[i][j]);
                }
            }
        }
        
        return maxLen;
    }
    
    // With non-overlapping constraint
    public int longestRepeatingNonOverlapping(String s) {
        int n = s.length();
        int[][] dp = new int[n + 1][n + 1];
        int maxLen = 0;
        
        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                if (s.charAt(i - 1) == s.charAt(j - 1) && dp[i - 1][j - 1] < (j - i)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                    maxLen = Math.max(maxLen, dp[i][j]);
                }
            }
        }
        
        return maxLen;
    }
}
```

---

### Variation 4: Longest Common Prefix

**Problem:** Find longest common prefix of all strings in array.

#### Code (Java)

```java
public class LCSVariations {
    
    // Variation 4: Longest Common Prefix
    public String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        
        String prefix = strs[0];
        
        for (int i = 1; i < strs.length; i++) {
            while (strs[i].indexOf(prefix) != 0) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        
        return prefix;
    }
    
    // Trie-based approach
    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }
    
    public String longestCommonPrefixTrie(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        
        TrieNode root = new TrieNode();
        
        // Insert first string
        for (char c : strs[0].toCharArray()) {
            root.children.put(c, new TrieNode());
            root = root.children.get(c);
        }
        
        // Check prefix with other strings
        root = new TrieNode();
        for (char c : strs[0].toCharArray()) {
            root = root.children.get(c);
        }
        
        // Find LCP length
        int len = 0;
        root = new TrieNode();
        
        for (int i = 0; i < strs[0].length(); i++) {
            char c = strs[0].charAt(i);
            boolean found = true;
            
            for (int j = 1; j < strs.length; j++) {
                if (i >= strs[j].length() || strs[j].charAt(i) != c) {
                    found = false;
                    break;
                }
            }
            
            if (found) {
                len++;
            } else {
                break;
            }
        }
        
        return strs[0].substring(0, len);
    }
}
```

---

## 📊 Complexity Comparison

| Approach | Time | Space | Recommended |
|----------|------|-------|-------------|
| DP 2D | O(m×n) | O(m×n) | ✅ Standard |
| DP Optimized | O(m×n) | O(n) | ⭐ Best |
| Suffix Array | O((m+n) log(m+n)) | O(m+n) | ⚠️ Advanced |

---

## 🎓 Complete Implementation

```java
import java.util.*;

public class LCSComplete {
    
    // Optimal solution
    public int longestCommonSubstr(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        int maxLen = 0;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    curr[j] = 1 + prev[j - 1];
                    maxLen = Math.max(maxLen, curr[j]);
                } else {
                    curr[j] = 0;
                }
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return maxLen;
    }
    
    public static void main(String[] args) {
        LCSComplete lcs = new LCSComplete();
        
        String text1 = "abcde";
        String text2 = "abfce";
        
        System.out.println("LCS Length: " + lcs.longestCommonSubstr(text1, text2));
    }
}
```

---

## 🎯 Key Takeaways

1. **Substring:** Must be contiguous (unlike subsequence)
2. **Reset to 0:** On mismatch (key difference from LCS)
3. **Track max:** Answer is max of all dp values
4. **Space optimization:** Easy with 1D array
5. **Applications:** Plagiarism, DNA matching, diff tools

---

## 💡 LCS vs LCS Substring

| Feature | Subsequence | Substring |
|---------|-------------|-----------|
| **Contiguous** | No | Yes |
| **On Mismatch** | Max of exclude | Reset to 0 |
| **Answer** | dp[m][n] | Max of all dp[i][j] |
| **Example** | "ace" from "abcde" | "ab" from "abcde" |

---

## 🌟 Interview Tips

1. **Clarify:** Subsequence vs substring
2. **Key difference:** Reset to 0 on mismatch
3. **Track maximum:** Not just final cell
4. **Space optimization:** Show 1D array
5. **Print substring:** Track ending index
6. **Know variations:** Repeating, K strings, prefix

**Understand the subtle difference from LCS (subsequence)!** 🚀
