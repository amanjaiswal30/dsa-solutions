# 🔹 Length of Longest Valid Parentheses

---

## 📌 Problem Statement
Given a string `s` containing just the characters `'('` and `')'`, find the length of the **longest valid (well-formed) parentheses substring**.

---

## 🧩 Example Inputs & Outputs
| Input        | Output |
|--------------|--------|
| `"(()"`      | `2`    |
| `")()())"`   | `4`    |
| `""`         | `0`    |
| `"()(()"`    | `2`    |
| `"((())())"` | `8`    |

---

## 💡 Logic & Intuition
- A **valid substring** requires matching `(` and `)` in the correct order.
- We want the **longest continuous segment** that satisfies this.
- Approaches:
    1. **Brute Force** → Generate all substrings and validate each.
    2. **Stack** → Track indices of unmatched parentheses.
    3. **Two-Pass Scan** → Use counters from both directions (no stack).

---

## 🔹 Approaches & Java Code

```java
import java.util.Stack;

public class LongestValidParentheses {

    // Helper function for Brute Force
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(') stack.push(c);
            else {
                if (stack.isEmpty()) return false;
                stack.pop();
            }
        }
        return stack.isEmpty();
    }

    // Approach 1: Brute Force
    public int longestValidParenthesesBruteForce(String s) {
        int max = 0;
        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 2; j <= s.length(); j += 2) {
                if (isValid(s.substring(i, j))) {
                    max = Math.max(max, j - i);
                }
            }
        }
        return max;
    }

    // Approach 2: Stack-Based
    public int longestValidParenthesesStack(String s) {
        Stack<Integer> stack = new Stack<>();
        stack.push(-1); // base for first valid substring
        int max = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                stack.push(i);
            } else {
                stack.pop();
                if (stack.isEmpty()) {
                    stack.push(i); // reset base
                } else {
                    max = Math.max(max, i - stack.peek());
                }
            }
        }
        return max;
    }

    // Approach 3: Two-Pass Linear Scan
    public int longestValidParenthesesTwoPass(String s) {
        int left = 0, right = 0, max = 0;

        // Left to Right
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') left++;
            else right++;

            if (left == right) max = Math.max(max, 2 * right);
            else if (right > left) left = right = 0;
        }

        // Right to Left
        left = right = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '(') left++;
            else right++;

            if (left == right) max = Math.max(max, 2 * left);
            else if (left > right) left = right = 0;
        }

        return max;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                          | Time Complexity | Space Complexity |
|-----------------------------------|-----------------|------------------|
| Brute Force (Generate Substrings) | O(n³)           | O(n)             |
| Stack-Based                       | O(n)            | O(n)             |
| Two-Pass Linear Scan              | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Empty string → result `0`.
- String with no valid pairs → result `0`.
- Entire string valid (e.g., `"((()))"`) → return full length.
- Multiple valid substrings → only count the **longest** one.

---

## 🔹 Follow-Up Questions
1. Can you extend this to return the **substring itself**, not just its length?
2. How would you adapt this for multiple types of brackets like `{}`, `[]`?
3. Can you solve this in **one-pass** without using extra stack memory?

---
