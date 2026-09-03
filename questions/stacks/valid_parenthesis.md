# Valid Parentheses

![](../../assets/images/stacks/valid_parenthesis.svg)


**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
Given a string `s` consisting of the characters `'('`, `')'`, `'{'`, `'}'`, `'['`, and `']'`, determine if the string is **valid**.

A string is valid if:
1. Open brackets are closed by the same type of bracket.
2. Open brackets are closed in the correct order.
3. Every closing bracket has a corresponding open bracket before it.

### Variant: Parentheses With Noise Characters
A common interview twist: the string contains **arbitrary extra characters** (letters, digits, symbols) mixed in with just `'('` and `')'`. Ignore every non-parenthesis character and check whether the `(`/`)` that remain are **balanced and correctly ordered**.

---

## 🔹 Examples
| Input      | Output  |
|------------|---------|
| `"()[]{}"` | `true`  |
| `"([{}])"` | `true`  |
| `"(]"`     | `false` |
| `"([)]"`   | `false` |
| `"("`      | `false` |

**Variant (with noise characters, only `(` / `)` matter):**

| Input                     | Output  | Why |
|---------------------------|---------|-----|
| `"(())"`                  | `true`  | Balanced, noise-free |
| `"((Sha#1*)()()(((()"`    | `false` | 8 `(` vs. only 4 `)` — 4 unmatched opens remain |
| `"a(b)c"`                 | `true`  | Letters ignored, `()` balanced |
| `"(a))"`                  | `false` | Extra unmatched `)` |

---

## 🔹 Intuition & Logic
- Brackets must **match in type** and follow **LIFO order**.
- A **stack** is the natural choice.
- Approaches:
    1. **Brute Force (Replace method)**
    2. **Stack-based (Standard)**
    3. **Stack + HashMap (Cleaner)**
    4. **Variant: Stack, ignoring non-`()` characters**

For the noise-character variant, the logic is the same LIFO check as approach 2, restricted to `'('`/`')'` — any other character is simply **skipped**, not pushed:
- See `'('` → push it.
- See `')'` → if the stack is empty, unmatched close → invalid; otherwise pop.
- See anything else → ignore.
- At the end, valid only if the stack is **empty** (no unmatched opens).

---

## 💻 Java Code (All Approaches Together)

```java
import java.util.*;

public class ValidParentheses {

    // Approach 1: Brute Force (Replace Method)
    public boolean isValidBruteForce(String s) {
        int length;
        do {
            length = s.length();
            s = s.replace("()", "")
                 .replace("{}", "")
                 .replace("[]", "");
        } while (s.length() != length);

        return s.isEmpty();
    }

    // Approach 2: Stack-Based (Standard)
    public boolean isValidStack(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) return false;
                char top = stack.pop();
                if ((c == ')' && top != '(') ||
                    (c == '}' && top != '{') ||
                    (c == ']' && top != '[')) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    // Approach 3: Stack with HashMap (Cleaner)
    public boolean isValidHashMap(String s) {
        Map<Character, Character> map = new HashMap<>();
        map.put(')', '(');
        map.put('}', '{');
        map.put(']', '[');

        Stack<Character> stack = new Stack<>();

        for (char c : s.toCharArray()) {
            if (map.containsKey(c)) {
                char top = stack.isEmpty() ? '#' : stack.pop();
                if (top != map.get(c)) return false;
            } else {
                stack.push(c);
            }
        }
        return stack.isEmpty();
    }

    // Variant: only '(' and ')' matter, every other character is ignored
    public boolean isValidIgnoringNoise(String s) {
        int openCount = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') {
                openCount++;
            } else if (c == ')') {
                if (openCount == 0) return false; // unmatched close
                openCount--;
            }
            // any other character: skip
        }
        return openCount == 0; // no unmatched opens left
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                          | Time Complexity | Space Complexity |
|-----------------------------------|-----------------|------------------|
| Brute Force (Replace)             | O(n²)           | O(1)             |
| Stack (Standard)                  | O(n)            | O(n)             |
| Stack + HashMap (Cleaner)         | O(n)            | O(n)             |
| Variant (ignore noise, counter)   | O(n)            | O(1)             |

The noise variant only tracks one bracket type, so an `int` counter replaces the stack — no need to store the actual open brackets, just how many are unmatched.

---

## 🔹 Edge Cases
- Empty string → valid (`true`).
- Single unmatched bracket → invalid.
- Nested structures like `"([{}])"` → valid.
- Incorrect order like `"([)]"` → invalid.
- **Noise variant:** string with no parentheses at all (e.g. `"abc"`) → valid (nothing to balance).
- **Noise variant:** a `')'` appearing before any `'('` → invalid immediately.
- **Noise variant:** more `'('` than `')'` at the end (e.g. `"((Sha#1*)()()(((()"`) → invalid, unmatched opens remain.

---

## 🔹 Follow-Up Questions
1. Can this be extended to handle **other paired symbols** (e.g., `< >`)?
2. How would you optimize for **huge inputs (millions of chars)**?
3. Can it be solved **without a stack**, only using counters?
4. **Noise variant:** if the string could mix `()`, `{}`, and `[]` *along with* noise characters, would the counter trick still work, or do you need a stack again? (Answer: back to a stack — multiple bracket types require tracking *which* type is open, not just a count.)

---
