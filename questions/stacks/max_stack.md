# 🔹 Max Stack

![](../../assets/images/stacks/max_stack.svg)


---

## 📌 Problem Statement
Design a stack that supports `push`, `pop`, `top`, retrieving the **maximum element** (`peekMax`), and **removing the maximum element** (`popMax`) — all while preserving the relative order of the remaining elements.

**Operations to Support:**
- `push(x)` — Push element `x` onto the stack.
- `pop()` — Remove and return the element on top of the stack.
- `top()` — Get the top element.
- `peekMax()` — Retrieve the maximum element currently in the stack.
- `popMax()` — Remove the maximum element and return it. If there are duplicates, remove the one closest to the top.

---

## 💻 Java Code (All Approaches)

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

public class MaxStackSolutions {

    // Approach 1: Two Stacks (popMax is O(n), everything else O(1))
    static class MaxStackTwoStacks {
        private Stack<Integer> stack;
        private Stack<Integer> maxStack;

        public MaxStackTwoStacks() {
            stack = new Stack<>();
            maxStack = new Stack<>();
        }

        public void push(int x) {
            int currentMax = maxStack.isEmpty() ? x : Math.max(x, maxStack.peek());
            stack.push(x);
            maxStack.push(currentMax);
        }

        public int pop() {
            maxStack.pop();
            return stack.pop();
        }

        public int top() {
            return stack.peek();
        }

        public int peekMax() {
            return maxStack.peek();
        }

        public int popMax() {
            int max = maxStack.peek();
            Stack<Integer> buffer = new Stack<>();

            // Buffer everything above the max element
            while (stack.peek() != max) {
                buffer.push(pop());
            }
            pop(); // discard the max itself

            // Push the buffered elements back (recomputes maxStack correctly)
            while (!buffer.isEmpty()) {
                push(buffer.pop());
            }

            return max;
        }
    }

    // Approach 2: Doubly Linked List + TreeMap (Optimized)
    static class MaxStackOptimized {
        private static class Node {
            int val;
            Node prev, next;
            Node(int val) { this.val = val; }
        }

        private final Node head = new Node(0); // sentinel
        private final Node tail = new Node(0); // sentinel
        private final TreeMap<Integer, List<Node>> map = new TreeMap<>();

        public MaxStackOptimized() {
            head.next = tail;
            tail.prev = head;
        }

        public void push(int x) {
            Node node = new Node(x);
            Node prevTail = tail.prev;
            prevTail.next = node;
            node.prev = prevTail;
            node.next = tail;
            tail.prev = node;

            map.computeIfAbsent(x, k -> new ArrayList<>()).add(node);
        }

        public int pop() {
            Node node = tail.prev;
            unlink(node);
            removeFromMap(node);
            return node.val;
        }

        public int top() {
            return tail.prev.val;
        }

        public int peekMax() {
            return map.lastKey();
        }

        public int popMax() {
            int maxVal = map.lastKey();
            List<Node> bucket = map.get(maxVal);
            Node node = bucket.remove(bucket.size() - 1);
            if (bucket.isEmpty()) map.remove(maxVal);

            unlink(node);
            return maxVal;
        }

        private void removeFromMap(Node node) {
            List<Node> bucket = map.get(node.val);
            bucket.remove(bucket.size() - 1);
            if (bucket.isEmpty()) map.remove(node.val);
        }

        private void unlink(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }
}
```
---

## 💡 Intuition Behind Each Approach

- **Two Stacks:**  
  Mirror the Min Stack trick: a secondary stack tracks the running maximum at every push, giving O(1) `peekMax`. The catch is `popMax` — the maximum may be buried anywhere in the stack, not just at the top. To remove it, pop elements into a temporary buffer until the max is exposed, discard it, then push the buffer back (which naturally recomputes the running max as each element is re-pushed). This makes `popMax` O(n) in the worst case, while every other operation stays O(1).

- **Doubly Linked List + TreeMap (Optimized):**  
  Represent the stack as a doubly linked list so any node can be unlinked in O(1) given a reference to it — this is what makes removing a buried max element cheap. A `TreeMap<Integer, List<Node>>` indexes nodes by value, sorted, so the current maximum value is always `map.lastKey()` in O(log n). Each value's bucket is used purely as a stack (`add` to append, `remove(size() - 1)` to pop the most recent) — no need for `Deque`. Because nodes are only ever appended/removed at the **end** of their own value's bucket, and the physically most-recent node in the list is always the most-recent node for its own value too, both `pop()` and `popMax()` can safely take the last entry of the relevant bucket without ambiguity. This brings `push`, `pop`, and `popMax` down to O(log n), while `top` and `peekMax` remain O(1).

---

## 📊 Complexity Analysis

| Approach                      | push     | pop      | top  | peekMax | popMax   | Space Complexity |
|--------------------------------|----------|----------|------|---------|----------|-------------------|
| Two Stacks                    | O(1)     | O(1)     | O(1) | O(1)    | O(n)     | O(n)              |
| Doubly Linked List + TreeMap  | O(log n) | O(log n) | O(1) | O(1)    | O(log n) | O(n)              |

---

## 🔹 Edge Cases
1. **Duplicate max values** → `popMax` removes only the most recently pushed occurrence; the rest remain in place.
2. **`pop`/`popMax`/`top`/`peekMax` on an empty stack** → should be guarded against (calling `peek`/`pop` on an empty structure throws).
3. **All elements equal** → `peekMax` always returns that value; `popMax` behaves like a normal `pop` from the top.
4. **Single element stack** → `popMax` removes it and the stack becomes empty.
5. **Interleaved push/popMax calls** → verify relative order of the remaining elements is preserved after a `popMax` removes a non-top element.

---

## 🔹 Follow-Up Questions
- [Min Stack](min_stack.md) mirrors both approaches here to add its own `popMin()` — same buffer trick for the two-stack version, same `TreeMap` (keyed by `firstKey()` instead of `lastKey()`) for the optimized version.
- Can you support `getKthMax()` efficiently with the TreeMap-based design?
- What breaks if two threads call `push`/`popMax` concurrently — where exactly would you add synchronization?

---
