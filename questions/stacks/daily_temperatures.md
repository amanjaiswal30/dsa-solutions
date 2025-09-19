# 🔹 Daily Temperatures

---

## 📌 Problem Statement
Given an array of integers `temperatures` representing daily temperatures, return an array `answer` such that `answer[i]` is the **number of days** you have to wait after the `i`-th day to get a warmer temperature.  
If there is no future day for which this is possible, put `0` instead.

**Example Inputs & Outputs**  
Input: `[73, 74, 75, 71, 69, 72, 76, 73]` → Output: `[1, 1, 4, 2, 1, 1, 0, 0]`

Input: `[30, 40, 50, 60]` → Output: `[1, 1, 1, 0]`

Input: `[30, 60, 90]` → Output: `[1, 1, 0]`

---

## 💡 Logic & Intuition
- For each day, we want the **next warmer day**.
- Three main approaches:
    1. **Brute Force:** check all future days for each day.
    2. **Stack-Based (Optimal):** use a monotonic decreasing stack.
    3. **DP / Next-Greater-Index:** traverse backward using previously computed answers.

---

## 💻 Java Code (All Approaches Together)

```java
import java.util.Stack;

public class DailyTemperatures {

    // Approach 1: Brute Force
    public int[] dailyTemperaturesBruteForce(int[] temperatures) {
        int n = temperatures.length;
        int[] answer = new int[n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (temperatures[j] > temperatures[i]) {
                    answer[i] = j - i;
                    break;
                }
            }
        }

        return answer;
    }

    // Approach 2: Stack-Based (Optimal)
    public int[] dailyTemperaturesStack(int[] temperatures) {
        int n = temperatures.length;
        int[] answer = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                int idx = stack.pop();
                answer[idx] = i - idx;
            }
            stack.push(i);
        }

        return answer;
    }

    // Approach 3: DP / Next-Greater-Index (Backward Traversal)
    public int[] dailyTemperaturesDP(int[] temperatures) {
        int n = temperatures.length;
        int[] answer = new int[n];

        for (int i = n - 2; i >= 0; i--) {
            int j = i + 1;
            while (j < n && temperatures[j] <= temperatures[i]) {
                if (answer[j] > 0) {
                    j += answer[j];
                } else {
                    j = n; // no warmer day ahead
                }
            }
            if (j < n) answer[i] = j - i;
        }

        return answer;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                | Time Complexity | Space Complexity |
|-------------------------|-----------------|------------------|
| Brute Force             | O(n²)           | O(n)             |
| Stack-Based (Optimal)   | O(n)            | O(n)             |
| DP / Backward Traversal | O(n)            | O(n)             |

---

## 🔹 Edge Cases
1. All temperatures the same → all `0`s.
2. Strictly decreasing temperatures → all `0`s.
3. Single temperature → `[0]`.
4. Very large array → stack-based or DP solution required for efficiency.

---

## 🔹 Follow-Up Questions
1. How to modify for **next colder day** instead?
2. Can we **reduce space usage** if we are allowed to modify input array?
3. How to **stream temperatures one by one** and still find next warmer day efficiently?

---
