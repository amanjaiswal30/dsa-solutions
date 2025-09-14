# Merge Overlapping Intervals

---

## 🔹 Problem Statement
Given a collection of intervals, **merge all overlapping intervals**.  
Return the merged intervals as a list.

---

## 🔹 Logic & Intuition
- Sort intervals based on **start times**.
- Iterate through the intervals and compare each interval's **start** with the **end** of the last merged interval.
- If they **overlap**, merge by updating the **end** of the last interval.
- Otherwise, add the current interval as a **new interval** to the result.

---

## 🔹 Approaches

### 1. Brute Force
- For each interval, check overlap with every other interval.
- Merge overlapping intervals.
- **Time Complexity:** O(n²)
- **Space Complexity:** O(n) (for result)

---

### 2. Sorting + Merge (Optimal)
- Sort intervals by start time.
- Iterate once, merging intervals as needed.
- **Time Complexity:** O(n log n) (for sorting)
- **Space Complexity:** O(n) (for result)

---

## 🔹 Java Code

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeIntervals {

    public static int[][] merge(int[][] intervals) {
        if (intervals.length <= 1) {
            return intervals;
        }

        // Sort by start time
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> merged = new ArrayList<>();
        int[] currentInterval = intervals[0];
        merged.add(currentInterval);

        for (int[] interval : intervals) {
            if (interval[0] <= currentInterval[1]) {
                // Overlapping intervals, merge
                currentInterval[1] = Math.max(currentInterval[1], interval[1]);
            } else {
                // No overlap, add new interval
                currentInterval = interval;
                merged.add(currentInterval);
            }
        }

        return merged.toArray(new int[merged.size()][]);
    }
}
```
---

## 🔹 Complexity Analysis

| Approach        | Time Complexity | Space Complexity |
|-----------------|-----------------|------------------|
| Brute Force     | O(n²)           | O(n)             |
| Sorting + Merge | O(n log n)      | O(n)             |

---

## 🔹 Edge Cases
- Intervals are already **non-overlapping** → return as is.
- Intervals are all **completely overlapping** → return a single merged interval.
- Intervals contain **single interval only** → return the same.
- Intervals with same start and end points → merge correctly.

---

## 🔹 Follow-Up Questions
1. How would you handle intervals sorted in **descending order**?
2. How to merge intervals **in-place** without extra space?
3. How to count the **total number of merged intervals** without storing them?

---
