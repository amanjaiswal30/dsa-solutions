# 🔹 Min Stack

---

## 📌 Problem Statement
Design a stack that supports `push`, `pop`, `top`, and retrieving the **minimum element** in constant time.

**Operations to Support:**
- `push(x)` — Push element `x` onto stack.
- `pop()` — Removes the element on top of the stack.
- `top()` — Get the top element.
- `getMin()` — Retrieve the minimum element in the stack.

---

## 💻 Java Code (All Approaches)

```java
import java.util.Stack;

public class MinStackSolutions {

    // Approach 1: Brute Force
    static class MinStackBrute {
        private Stack<Integer> stack;

        public MinStackBrute() {
            stack = new Stack<>();
        }

        public void push(int x) {
            stack.push(x);
        }

        public void pop() {
            if (!stack.isEmpty()) stack.pop();
        }

        public int top() {
            return stack.peek();
        }

        public int getMin() {
            int min = Integer.MAX_VALUE;
            for (int val : stack) {
                if (val < min) min = val;
            }
            return min;
        }
    }

    // Approach 2: Two Stacks (Optimized)
    static class MinStackTwoStacks {
        private Stack<Integer> stack;
        private Stack<Integer> minStack;

        public MinStackTwoStacks() {
            stack = new Stack<>();
            minStack = new Stack<>();
        }

        public void push(int x) {
            stack.push(x);
            if (minStack.isEmpty() || x <= minStack.peek()) {
                minStack.push(x);
            }
        }

        public void pop() {
            if (!stack.isEmpty()) {
                int val = stack.pop();
                if (val == minStack.peek()) minStack.pop();
            }
        }

        public int top() {
            return stack.peek();
        }

        public int getMin() {
            return minStack.peek();
        }
    }

    // Approach 3: One Stack with Encoded Values (Optimized)
    static class MinStackEncoded {
        private Stack<Long> stack;
        private long min;

        public MinStackEncoded() {
            stack = new Stack<>();
        }

        public void push(int x) {
            if (stack.isEmpty()) {
                stack.push(0L);
                min = x;
            } else {
                stack.push(x - min);
                if (x < min) min = x;
            }
        }

        public void pop() {
            long top = stack.pop();
            if (top < 0) min = min - top;
        }

        public int top() {
            long top = stack.peek();
            if (top > 0) return (int)(top + min);
            else return (int)min;
        }

        public int getMin() {
            return (int)min;
        }
    }
}
```
---

## 💡 Intuition Behind Each Approach

- **Brute Force:**  
  Maintain all elements in a stack. On `getMin()`, iterate through the stack to find the minimum. Simple but inefficient (O(n) for `getMin()`).

- **Two Stacks (Optimized):**  
  Use a secondary stack to keep track of the minimum value at each push. Ensures O(1) for all operations, as the top of the min stack is always the current minimum.

- **One Stack with Encoded Values (Optimized):**  
  Store the difference between the current value and the current minimum.  
  Negative values indicate that the minimum has changed. This approach saves space and maintains O(1) operations.

---

## 📊 Complexity Analysis

| Approach                      | push | pop  | top  | getMin | Space Complexity |
|-------------------------------|------|------|------|--------|------------------|
| Brute Force                   | O(1) | O(1) | O(1) | O(n)   | O(n)             |
| Two Stacks (Optimized)        | O(1) | O(1) | O(1) | O(1)   | O(n)             |
| One Stack with Encoded Values | O(1) | O(1) | O(1) | O(1)   | O(n)             |

---

## 🔹 Edge Cases
1. Calling `getMin()` on an **empty stack** → should handle exceptions.
2. All elements are the **same** → min remains constant.
3. Negative numbers → encoded approach handles properly.
4. Single element stack → min = that element.

---

## 🔹 Follow-Up Questions
- Can we **reduce space further** for `getMin` without a second stack?
- How to adapt this approach for a **Max Stack**?
- Can we implement this efficiently for **streaming input** where the stack is huge?

---
