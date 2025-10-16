# 🔹 Problem: Minimum Window Substring

**Difficulty:** Hard ⚡

---

## 🔹 Problem Statement
Given two strings `s` and `t`, find the **minimum window in `s` which contains all the characters of `t`** (including duplicates).  
If no such window exists, return an empty string `""`.

**Example:**  
Input: `s = "ADOBECODEBANC"`, `t = "ABC"`  
Output: `"BANC"`

Explanation:
- The substring `"BANC"` contains all characters `A`, `B`, and `C` and is the smallest such window.

---

## 🔹 Intuition
- Use the **sliding window technique** with two pointers.
- Maintain a **frequency map** for characters in `t`.
- Expand the right pointer to include characters until all required characters are included.
- Shrink the left pointer to minimize the window while still satisfying the condition.
- Keep track of the **minimum window length** and its indices.

---

## 🔹 Approaches

### 1. Brute Force
- Check all substrings of `s`.
- For each substring, check if it contains all characters of `t`.
- Track the smallest valid substring.

**Time Complexity:** O(n² * m) → n = length of s, m = length of t  
**Space Complexity:** O(1) for frequency (constant alphabet)

---

### 2. Sliding Window + HashMap (Optimal)
- Use a **map** to count characters in `t`.
- Use a **sliding window** [left, right] over `s`.
- Expand right pointer:
    1. Include current character in a window count.
    2. If character satisfies requirement, increment `formed` count.
- Shrink left pointer while `formed == required`:
    1. Update minimum window if smaller.
    2. Remove `s[left]` from window count and adjust `formed`.
- Continue until the end of `s`.

**Time Complexity:** O(n + m)  
**Space Complexity:** O(|T| + |S|) → frequency maps

---

## 🔹 Java Code

```java
import java.util.*;

public class MinimumWindowSubstring {

    public static String minWindow(String s, String t) {
        if (s == null || t == null || s.length() < t.length()) return "";

        Map<Character, Integer> tCount = new HashMap<>();
        for (char c : t.toCharArray()) tCount.put(c, tCount.getOrDefault(c, 0) + 1);

        Map<Character, Integer> windowCount = new HashMap<>();
        int left = 0, right = 0;
        int required = tCount.size();
        int formed = 0;
        int minLen = Integer.MAX_VALUE;
        int start = 0;

        while (right < s.length()) {
            char c = s.charAt(right);
            windowCount.put(c, windowCount.getOrDefault(c, 0) + 1);

            if (tCount.containsKey(c) && windowCount.get(c).intValue() == tCount.get(c).intValue()) {
                formed++;
            }

            while (left <= right && formed == required) {
                if (right - left + 1 < minLen) {
                    minLen = right - left + 1;
                    start = left;
                }

                char leftChar = s.charAt(left);
                windowCount.put(leftChar, windowCount.get(leftChar) - 1);
                if (tCount.containsKey(leftChar) && windowCount.get(leftChar).intValue() < tCount.get(leftChar).intValue()) {
                    formed--;
                }
                left++;
            }

            right++;
        }

        return minLen == Integer.MAX_VALUE ? "" : s.substring(start, start + minLen);
    }

    public static void main(String[] args) {
        String s = "ADOBECODEBANC";
        String t = "ABC";
        System.out.println(minWindow(s, t)); // Output: BANC
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                   | Time Complexity | Space Complexity |
|----------------------------|----------------|-----------------|
| Brute Force                | O(n² * m)      | O(1)            |
| Sliding Window + HashMap   | O(n + m)       | O(|S| + |T|)    |

---

## 🔹 Edge Cases
- `s = ""` or `t = ""` → Output `""`
- `s` shorter than `t` → Output `""`
- No valid window → Output `""`
- `s` and `t` with duplicate characters → handled by frequency map

---

## 🔹 Follow-Up Questions
1. Can you find the **number of minimum windows** containing all characters?
2. How would you modify the solution if `t` is very large compared to `s`?
3. Can this be adapted for **streaming input**, where `s` arrives character by character?
