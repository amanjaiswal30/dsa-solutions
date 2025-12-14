# Word Break – Detailed Explanation

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given a string `s` and a dictionary of strings `wordDict`, return `true` if `s` can be segmented into a space-separated sequence of one or more dictionary words.

**Note:** The same word in the dictionary may be reused multiple times in the segmentation.

---

## 🔹 Examples

**Example 1:**  
Input: `s = "leetcode", wordDict = ["leet", "code"]`  
Output: `true`  
Explanation: "leetcode" can be segmented as "leet code"

**Example 2:**  
Input: `s = "applepenapple", wordDict = ["apple", "pen"]`  
Output: `true`  
Explanation: "applepenapple" can be segmented as "apple pen apple"

**Example 3:**  
Input: `s = "catsandog", wordDict = ["cats", "dog", "sand", "and", "cat"]`  
Output: `false`

---

## 🔹 Core Intuition

**Recurrence:**
```
dp[i] = can segment s[0..i-1]?

dp[i] = true if there exists j < i such that:
  - dp[j] is true
  - s[j..i-1] is in wordDict
```

**Base Case:**
```
dp[0] = true (empty string)
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class WordBreak {
    
    public boolean wordBreakRecursive(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        return canBreak(s, 0, dict);
    }
    
    private boolean canBreak(String s, int start, Set<String> dict) {
        if (start == s.length()) {
            return true;
        }
        
        for (int end = start + 1; end <= s.length(); end++) {
            if (dict.contains(s.substring(start, end)) && 
                canBreak(s, end, dict)) {
                return true;
            }
        }
        
        return false;
    }
}
```

---

## 2️⃣ Memoization (Top-Down DP)

### Code (Java)

```java
public class WordBreak {
    
    public boolean wordBreakMemo(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        Boolean[] memo = new Boolean[s.length()];
        return canBreakMemo(s, 0, dict, memo);
    }
    
    private boolean canBreakMemo(String s, int start, Set<String> dict, Boolean[] memo) {
        if (start == s.length()) {
            return true;
        }
        
        if (memo[start] != null) {
            return memo[start];
        }
        
        for (int end = start + 1; end <= s.length(); end++) {
            if (dict.contains(s.substring(start, end)) && 
                canBreakMemo(s, end, dict, memo)) {
                memo[start] = true;
                return true;
            }
        }
        
        memo[start] = false;
        return false;
    }
}
```

---

## 3️⃣ Dynamic Programming – Bottom-Up ⭐ OPTIMAL

### Code (Java)

```java
public class WordBreak {
    
    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;
        
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && dict.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        
        return dp[n];
    }
}
```

### Example Walkthrough

For `s = "leetcode"`, `wordDict = ["leet", "code"]`:

| i | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|---|
| **char** | - | l | e | e | t | c | o | d | e |
| **dp** | T | F | F | F | T | F | F | F | T |

**Answer:** dp[8] = **true**

### Complexity
- **Time:** O(n² × m) where m = avg word length
- **Space:** O(n)

---

## 4️⃣ Optimized with Trie

### Code (Java)

```java
public class WordBreak {
    
    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isWord = false;
    }
    
    public boolean wordBreakTrie(String s, List<String> wordDict) {
        TrieNode root = buildTrie(wordDict);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;
        
        for (int i = 0; i < n; i++) {
            if (!dp[i]) continue;
            
            TrieNode node = root;
            for (int j = i; j < n; j++) {
                char c = s.charAt(j);
                
                if (!node.children.containsKey(c)) {
                    break;
                }
                
                node = node.children.get(c);
                
                if (node.isWord) {
                    dp[j + 1] = true;
                }
            }
        }
        
        return dp[n];
    }
    
    private TrieNode buildTrie(List<String> words) {
        TrieNode root = new TrieNode();
        
        for (String word : words) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);
            }
            node.isWord = true;
        }
        
        return root;
    }
}
```

---

## 🔄 Variations

### Variation 1: Word Break II (Return All Sentences)

**Problem:** Return all possible sentences.

```java
public class WordBreakVariations {
    
    public List<String> wordBreakII(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        Map<Integer, List<String>> memo = new HashMap<>();
        return backtrack(s, 0, dict, memo);
    }
    
    private List<String> backtrack(String s, int start, Set<String> dict, 
                                   Map<Integer, List<String>> memo) {
        if (memo.containsKey(start)) {
            return memo.get(start);
        }
        
        List<String> result = new ArrayList<>();
        
        if (start == s.length()) {
            result.add("");
            return result;
        }
        
        for (int end = start + 1; end <= s.length(); end++) {
            String word = s.substring(start, end);
            
            if (dict.contains(word)) {
                List<String> sublist = backtrack(s, end, dict, memo);
                
                for (String sub : sublist) {
                    result.add(word + (sub.isEmpty() ? "" : " " + sub));
                }
            }
        }
        
        memo.put(start, result);
        return result;
    }
}
```

### Variation 2: Concatenated Words

**Problem:** Find all words that can be formed by concatenating other words.

```java
public List<String> findAllConcatenatedWordsInADict(String[] words) {
    Set<String> dict = new HashSet<>(Arrays.asList(words));
    List<String> result = new ArrayList<>();
    
    for (String word : words) {
        dict.remove(word);
        
        if (canBreak(word, dict)) {
            result.add(word);
        }
        
        dict.add(word);
    }
    
    return result;
}

private boolean canBreak(String s, Set<String> dict) {
    if (s.isEmpty()) return false;
    
    int n = s.length();
    boolean[] dp = new boolean[n + 1];
    dp[0] = true;
    
    for (int i = 1; i <= n; i++) {
        for (int j = 0; j < i; j++) {
            if (dp[j] && dict.contains(s.substring(j, i))) {
                dp[i] = true;
                break;
            }
        }
    }
    
    return dp[n];
}
```

### Variation 3: Word Break with Wildcards

```java
public boolean wordBreakWildcard(String s, List<String> wordDict) {
    Set<String> dict = new HashSet<>(wordDict);
    int n = s.length();
    boolean[] dp = new boolean[n + 1];
    dp[0] = true;
    
    for (int i = 1; i <= n; i++) {
        for (int j = 0; j < i; j++) {
            if (dp[j]) {
                String substr = s.substring(j, i);
                
                if (dict.contains(substr) || matchesWildcard(substr, dict)) {
                    dp[i] = true;
                    break;
                }
            }
        }
    }
    
    return dp[n];
}

private boolean matchesWildcard(String s, Set<String> dict) {
    for (String word : dict) {
        if (matches(s, word)) {
            return true;
        }
    }
    return false;
}

private boolean matches(String s, String pattern) {
    if (s.length() != pattern.length()) return false;
    
    for (int i = 0; i < s.length(); i++) {
        if (pattern.charAt(i) != '*' && s.charAt(i) != pattern.charAt(i)) {
            return false;
        }
    }
    return true;
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^n) | O(n) |
| Memoization | O(n²×m) | O(n) |
| DP Bottom-Up | O(n²×m) | O(n) ⭐ |
| Trie | O(n²) | O(total chars) |

where m = average word length

---

## 🎯 Key Takeaways

1. **DP pattern:** Try all possible word breaks
2. **HashSet lookup:** O(1) dictionary check
3. **Optimization:** Trie for faster prefix matching
4. **Variations:** Return all sentences, concatenated words
5. **Applications:** NLP, text segmentation, spell checking

**Classic string DP problem!** 🚀
