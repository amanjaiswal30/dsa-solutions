# 🔹 Largest Rectangle in Histogram

![](../../assets/images/stacks/largest_rectangle_in_histogram.svg)


---

## 📌 Problem Statement
Given an array of integers representing the heights of bars in a histogram, find the **area of the largest rectangle** that can be formed within the bounds of the histogram.

---

## 📊 Example Input & Output

**Input:** `[2, 1, 5, 6, 2, 3]`  
**Output:** `10`

---

## 💻 Java Code (All Approaches)

```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class LargestRectangleHistogram {

    // Approach 1: Brute Force
    public int largestRectangleAreaBruteForce(int[] heights) {
        int n = heights.length;
        int maxArea = 0;

        for (int i = 0; i < n; i++) {
            int minHeight = heights[i];
            for (int j = i; j < n; j++) {
                minHeight = Math.min(minHeight, heights[j]);
                int width = j - i + 1;
                maxArea = Math.max(maxArea, minHeight * width);
            }
        }

        return maxArea;
    }

    // Approach 2: Stack-Based (Optimized)
    public int largestRectangleAreaStack(int[] heights) {
        int n = heights.length;
        Stack<Integer> stack = new Stack<>();
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            int currentHeight = (i == n) ? 0 : heights[i];

            while (!stack.isEmpty() && heights[stack.peek()] > currentHeight) {
                int height = heights[stack.pop()];
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, height * width);
            }

            stack.push(i);
        }

        return maxArea;
    }

    // Approach 3: Left and Right Boundary Arrays (Optimized, same TC/SC as Approach 2)
    public int largestRectangleAreaLeftRight(int[] heights) {
        int n = heights.length;
        int[] leftSmaller = new int[n];   // nearest index to the left with a strictly smaller bar
        int[] rightSmaller = new int[n];  // nearest index to the right with a strictly smaller bar

        Deque<Integer> stack = new ArrayDeque<>();

        // Pass 1: fill leftSmaller using a monotonic increasing stack
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && heights[stack.peek()] >= heights[i]) {
                stack.pop();
            }
            leftSmaller[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }

        stack.clear();

        // Pass 2: fill rightSmaller using a monotonic increasing stack, scanning right to left
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && heights[stack.peek()] >= heights[i]) {
                stack.pop();
            }
            rightSmaller[i] = stack.isEmpty() ? n : stack.peek();
            stack.push(i);
        }

        int maxArea = 0;
        for (int i = 0; i < n; i++) {
            int width = rightSmaller[i] - leftSmaller[i] - 1;
            maxArea = Math.max(maxArea, heights[i] * width);
        }

        return maxArea;
    }
}
```
---

## 💡 Intuition Behind Each Approach

- **Brute Force:**  
  For each bar, extend left and right until a smaller height is found.  
  Area = `height * width`. Keep track of the maximum area.

- **Stack-Based (Optimized):**  
  Maintain a stack of indices with **ascending heights**.  
  When a lower bar is encountered, pop from stack and calculate area using the popped bar as the smallest bar.  
  Width = `i - stack.peek() - 1` (or `i` if stack is empty). Update max area.

- **Left and Right Boundary Arrays (Optimized):**  
  Same underlying idea as the stack approach, just split into two explicit passes.  
  For every bar `i`, precompute `leftSmaller[i]` — the nearest index to the left with a strictly smaller height — and `rightSmaller[i]` — the nearest index to the right with a strictly smaller height. Each is computed with its own monotonic stack in one linear pass.  
  The widest rectangle with bar `i` as its limiting height then spans `rightSmaller[i] - leftSmaller[i] - 1`. Take the max of `heights[i] * width` over all `i`. Since it still uses a monotonic stack internally (twice, once per direction), it has the **same O(n) time and O(n) space** as Approach 2 — it just trades one combined sweep for two clearer, independent boundary computations.

---

## 📊 Complexity Analysis

| Approach                              | Time Complexity | Space Complexity |
|-----------------------------------------|-----------------|------------------|
| Brute Force                           | O(n²)           | O(1)             |
| Stack-Based (Optimized)               | O(n)            | O(n)             |
| Left and Right Boundary Arrays (Optimized) | O(n)       | O(n)             |

---

## 🔹 Edge Cases
1. **Empty histogram** → largest area = 0.
2. **All bars same height** → largest area = `height * n`.
3. **Strictly increasing or decreasing heights** → area depends on consecutive bars.
4. **Single bar** → largest area = height of that bar.

---

## 🔹 Follow-Up Questions
- Can this be extended to a **2D matrix of heights** (Maximal Rectangle problem)?
- How to compute largest rectangle in **streaming input**?
- Can you eliminate the explicit `leftSmaller`/`rightSmaller` arrays and merge both boundary computations into the **single combined pass** used in Approach 2?

---
