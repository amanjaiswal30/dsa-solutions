# Letter Combinations of a Phone Number

![](../../assets/images/strings/letter_combinations_of_phone_number.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a string `digits` containing digits from `2` through `9` inclusive, return **all possible letter combinations** that the number could represent on a classic phone keypad.

Map digits to letters as follows:

| Digit | Letters |
|-------|---------|
| 2 | a, b, c |
| 3 | d, e, f |
| 4 | g, h, i |
| 5 | j, k, l |
| 6 | m, n, o |
| 7 | p, q, r, s |
| 8 | t, u, v |
| 9 | w, x, y, z |

**Note:** `1` and `0` do not map to any letters. The input may be empty.

---

## 🔹 Examples

**Input:** `digits = "23"`

**Output:** `["ad","ae","af","bd","be","bf","cd","ce","cf"]`

**Explanation:** Digit `2` → `abc`, digit `3` → `def`. Combine one letter from each digit in order.

---

**Input:** `digits = ""`

**Output:** `[]`

---

**Input:** `digits = "2"`

**Output:** `["a","b","c"]`

---

## 🔹 Constraints
- `0 <= digits.length <= 4`
- `digits[i]` is a digit in the range `'2'`–`'9'`

---

## 🔹 Intuition & Logic
This is a classic **backtracking / DFS** problem:
- At each position in `digits`, pick one letter from that digit's mapping.
- Append the letter, recurse on the next digit, then **backtrack** (remove the letter).
- When the current combination length equals `digits.length`, add it to the result.

Think of it as building a tree: each digit branches into 3–4 children (its letters). Every root-to-leaf path is one valid combination.

Total combinations ≈ **3^n to 4^n** (worst case when digit `7` or `9` appears often).

---

## 🔹 Approaches

### 1. Backtracking (Recursive DFS)
- Maintain a `StringBuilder` (or `char[]`) for the current path.
- For `digits[index]`, try every mapped letter, recurse with `index + 1`, then undo the last append.

**Time Complexity:** O(4^n × n) — up to 4 choices per digit, each string length n  
**Space Complexity:** O(n) recursion stack + output (not counted in some analyses)

---

### 2. Iterative (BFS / queue)
- Start with an empty string in a queue.
- For each digit, expand every string in the queue by appending each possible letter.
- After processing all digits, queue holds all combinations.

**Time Complexity:** O(4^n × n)  
**Space Complexity:** O(4^n) for the queue

---

## 🔹 Java Code (Backtracking, Iterative BFS)

```java
import java.util.*;

public class LetterCombinationsOfPhoneNumber {

    private static final Map<Character, String> MAP = new HashMap<>();

    static {
        MAP.put('2', "abc");
        MAP.put('3', "def");
        MAP.put('4', "ghi");
        MAP.put('5', "jkl");
        MAP.put('6', "mno");
        MAP.put('7', "pqrs");
        MAP.put('8', "tuv");
        MAP.put('9', "wxyz");
    }

    // 1. Backtracking (DFS)
    public static List<String> letterCombinationsDFS(String digits) {
        List<String> result = new ArrayList<>();
        if (digits == null || digits.isEmpty()) return result;

        backtrack(digits, 0, new StringBuilder(), result);
        return result;
    }

    private static void backtrack(String digits, int index, StringBuilder path, List<String> result) {
        if (index == digits.length()) {
            result.add(path.toString());
            return;
        }

        String letters = MAP.get(digits.charAt(index));
        for (char c : letters.toCharArray()) {
            path.append(c);
            backtrack(digits, index + 1, path, result);
            path.deleteCharAt(path.length() - 1); // backtrack
        }
    }

    // 2. Iterative BFS (queue expansion)
    public static List<String> letterCombinationsBFS(String digits) {
        List<String> result = new ArrayList<>();
        if (digits == null || digits.isEmpty()) return result;

        Queue<String> queue = new ArrayDeque<>();
        queue.add("");

        for (char d : digits.toCharArray()) {
            String letters = MAP.get(d);
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String prefix = queue.poll();
                for (char c : letters.toCharArray()) {
                    queue.add(prefix + c);
                }
            }
        }

        result.addAll(queue);
        return result;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach     | Time Complexity | Space Complexity        |
|--------------|-----------------|-------------------------|
| Backtracking | O(4^n × n)      | O(n) stack + output     |
| Iterative BFS| O(4^n × n)      | O(4^n) queue + output   |

Where `n = digits.length()`. Digits `7` and `9` have 4 letters each (worst-case branching).

---

## 🔹 Edge Cases
- Empty input `""` → return `[]` (not `[""]`)
- Single digit `"2"` → `["a","b","c"]`
- All same digit `"222"` → 3³ = 27 combinations
- Maximum length 4 → at most 4⁴ = 256 combinations

---

## 🔹 Follow-Up Questions
1. How would you handle digits `0` and `1` if they were included?
2. Can you generate combinations **lexicographically sorted** without sorting at the end?
3. How does this relate to other backtracking templates (subsets, generate parentheses)?
4. What changes if you need **combinations with repetition** allowed?
