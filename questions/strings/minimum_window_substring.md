# 🔹 Problem: Minimum Window Substring

![](../../assets/images/strings/minimum_window_substring.svg)


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
- **Sliding window** `[left, right]` on `s`: expand `right` until the window **covers** `t` (with multiplicities), then shrink `left` while still valid to minimize length.
- **Fast bookkeeping (ASCII):** Use **`int[128]`** (or `256` if needed) for **`need[c]`** (from `t`) and **`win[c]`** (current window). Avoids `HashMap` boxing and resizing — same **O(|s| + |t|)** time, better constants.
- **`missing` counter:** **`missing`** = how many **instances** from `t` are still **under-covered** in the window (initially **`|t|`**). Before **`win[c]++`**, if **`need[c] > 0`** and **`win[c] < need[c]`**, **`missing--`**. Before **`win[lc]--`**, if **`need[lc] > 0`** and **`win[lc] <= need[lc]`**, **`missing++`**. Valid window ⇔ **`missing == 0`**. Same invariant as **`formed == required`** over distinct chars, but **one int** and **no second map**.
- **Unicode / large alphabet:** Keep **`HashMap<Character, Integer>`** for `need` / `win`.

---

## 🔹 `missing` vs `formed` / `required`

| | **`missing`** | **`formed` / `required`** |
|--|----------------|---------------------------|
| **Counts** | **Occurrences** of `t` still under-covered in the window (starts at **\|t\|**). | **Distinct** letters of `t` that are fully satisfied: **`required`** = number of keys in `t`’s map with positive count; **`formed`** = how many of those keys have `windowCount == need` right now. |
| **Valid window** | **`missing == 0`** | **`formed == required`** |
| **Typical pairing** | **`int[128]`** `need` / `win` (ASCII) | **`HashMap`** counts |
| **When to prefer** | Bounded alphabet, fewer allocations, one extra **`int`** besides arrays. | Large / sparse alphabet, or you already think in “how many distinct constraints are satisfied?” |

Same sliding-window idea and **O(\|s\| + \|t\|)** time; pick one style and stay consistent in the shrink loop.

---

## 🔹 Approaches

### 1. Brute Force
- Generate every substring `s[left..right]` (outer loop on `left`, inner loop on `right`).
- For each candidate, call `containsAll(window, t)`:
  - Build a fresh frequency map of `t`, and set `remaining = t.length()`.
  - Walk the window character by character. Whenever a character is still needed (`map.get(c) > 0`), **decrement** its count in the map **and decrement `remaining`**.
  - The window is valid once `remaining == 0` — every occurrence required by `t` has been matched.
- The map/`remaining` pair is rebuilt **from scratch for every candidate window** — that repeated, wasted recomputation is exactly what the optimized approaches below eliminate.
- Minor but natural optimization: for a fixed `left`, once a `right` makes `remaining == 0`, stop expanding — any larger `right` only produces a longer (never shorter) valid window for that `left`.

**Time Complexity:** O(n² · m) → n = length of `s`, m = length of `t` (rebuilding the map + scanning the window for every one of the O(n²) candidate substrings)
**Space Complexity:** O(m) for the per-call frequency map

**Bridge to the optimized solution:** `remaining` here is already the same idea as **`missing`** in Approach 3 — "how many required occurrences are still unmatched in the current window." The only difference is *how* it's maintained: brute force **resets it to `|t|` and rebuilds the map for every substring**, while the sliding window **keeps one map and one counter alive across the whole scan**, nudging them by ±1 as `right` expands and `left` shrinks. Removing the "rebuild from scratch" step is what collapses O(n² · m) down to O(n + m).

---

### 2. Sliding window + HashMap (`formed` / `required`)
- Count `t` in a map; maintain window counts; **`formed`** = number of distinct keys whose window count **equals** target count.
- Same structure as §3: **outer `for` over `right`**, **inner `while`** to shrink **`left`** while **`formed == required`**.
- **Time:** O(|s| + |t|) · **Space:** O(|Σ|) hash overhead per access.

---

### 3. Sliding window + `int[128]` + `missing` (recommended on ASCII)
- **`need[c]`** from `t`, **`win[c]`** in window, **`missing`** starts at **`|t|`**.
- **Outer `for (right ...)`** expands the window; **inner `while (missing == 0)`** shrinks **`left`** while the window stays valid.
- Single **`char[]`** for `s` avoids repeated **`charAt`** in the hot path (minor).
- **Time:** O(|s| + |t|) · **Space:** O(1) extra if alphabet size is constant (128).

---

## 🔹 Java Code

```java
import java.util.*;

public class MinimumWindowSubstring {

    /** Brute force: try every substring, check it against a freshly-built need map each time */
    public static String minWindowBruteForce(String s, String t) {
        if (t.isEmpty() || s.length() < t.length()) return "";

        int n = s.length();
        int minLen = Integer.MAX_VALUE;
        int start = -1;

        for (int left = 0; left < n; left++) {
            for (int right = left; right < n; right++) {
                String window = s.substring(left, right + 1);
                if (containsAll(window, t)) {
                    if (window.length() < minLen) {
                        minLen = window.length();
                        start = left;
                    }
                    break; // longer windows from this `left` can't beat the one we just found
                }
            }
        }

        return start == -1 ? "" : s.substring(start, start + minLen);
    }

    /** Rebuilds a need map from `t` and consumes `window` against it, tracking a remaining count. */
    private static boolean containsAll(String window, String t) {
        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) {
            need.put(c, need.getOrDefault(c, 0) + 1);
        }

        int remaining = t.length();
        for (char c : window.toCharArray()) {
            Integer count = need.get(c);
            if (count != null && count > 0) {
                need.put(c, count - 1); // decrement: one fewer occurrence of `c` still owed
                remaining--;            // decrement: one fewer character owed overall
            }
        }
        return remaining == 0;
    }

    /** Optimized: ASCII counts + missing counter — fewer allocations, same asymptotics */
    public static String minWindow(String s, String t) {
        if (t.isEmpty() || s.length() < t.length()) return "";

        int[] need = new int[128];
        for (int i = 0; i < t.length(); i++) {
            need[t.charAt(i)]++;
        }

        int missing = t.length();
        int[] win = new int[128];
        char[] arr = s.toCharArray();

        int left = 0;
        int start = 0;
        int minLen = Integer.MAX_VALUE;

        for (int right = 0; right < arr.length; right++) {
            char c = arr[right];
            if (need[c] > 0 && win[c] < need[c]) {
                missing--;
            }
            win[c]++;

            while (missing == 0) {
                if (right - left + 1 < minLen) {
                    minLen = right - left + 1;
                    start = left;
                }
                char lc = arr[left];
                if (need[lc] > 0 && win[lc] <= need[lc]) {
                    missing++;
                }
                win[lc]--;
                left++;
            }
        }

        return minLen == Integer.MAX_VALUE ? "" : s.substring(start, start + minLen);
    }

    /** HashMap variant — use when alphabet is not bounded ASCII */
    public static String minWindowHashMap(String s, String t) {
        if (s == null || t == null || s.length() < t.length()) return "";

        Map<Character, Integer> tCount = new HashMap<>();
        for (char c : t.toCharArray()) {
            tCount.put(c, tCount.getOrDefault(c, 0) + 1);
        }

        Map<Character, Integer> windowCount = new HashMap<>();
        int left = 0;
        int required = tCount.size();
        int formed = 0;
        int minLen = Integer.MAX_VALUE;
        int start = 0;

        for (int right = 0; right < s.length(); right++) {
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
                if (tCount.containsKey(leftChar)
                        && windowCount.get(leftChar).intValue() < tCount.get(leftChar).intValue()) {
                    formed--;
                }
                left++;
            }
        }

        return minLen == Integer.MAX_VALUE ? "" : s.substring(start, start + minLen);
    }

    public static void main(String[] args) {
        String s = "ADOBECODEBANC";
        String t = "ABC";
        System.out.println(minWindowBruteForce(s, t)); // BANC
        System.out.println(minWindow(s, t));            // BANC
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                          | Time Complexity | Space Complexity        |
|-----------------------------------|----------------|-------------------------|
| Brute Force                       | O(n² · m)      | O(m) per-call need map  |
| Sliding window + HashMap          | O(n + m)       | O(|Σ|) map buckets      |
| Sliding window + `int[]` + missing | O(n + m)    | O(1) for fixed Σ e.g. 128 |

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
