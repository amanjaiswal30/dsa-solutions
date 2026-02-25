# Merge Overlapping Intervals

![](../../assets/images/arrays/merge_intervals.svg)


**Difficulty:** Medium-Hard 🔥

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
- Merge overlapping intervals repeatedly until no overlaps remain.
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

    // Brute Force Approach
    public static int[][] mergeBrute(int[][] intervals) {
        List<int[]> result = new ArrayList<>(Arrays.asList(intervals));

        boolean mergedAny;
        do {
            mergedAny = false;
            for (int i = 0; i < result.size(); i++) {
                int[] a = result.get(i);
                for (int j = i + 1; j < result.size(); j++) {
                    int[] b = result.get(j);
                    if (isOverlap(a, b)) {
                        // Merge a and b
                        int start = Math.min(a[0], b[0]);
                        int end = Math.max(a[1], b[1]);
                        result.set(i, new int[]{start, end});
                        result.remove(j);
                        mergedAny = true;
                        break; // Restart inner loop
                    }
                }
                if (mergedAny) break; // Restart outer loop
            }
        } while (mergedAny);

        return result.toArray(new int[result.size()][]);
    }

    private static boolean isOverlap(int[] a, int[] b) {
        return a[0] <= b[1] && b[0] <= a[1];
    }

    // Optimal Approach
    public static int[][] mergeOptimal(int[][] intervals) {
        if (intervals.length <= 1) return intervals;

        Arrays.sort(intervals, (x, y) -> Integer.compare(x[0], y[0]));

        List<int[]> merged = new ArrayList<>();
        int[] current = intervals[0];
        merged.add(current);

        for (int[] interval : intervals) {
            if (interval[0] <= current[1]) {
                current[1] = Math.max(current[1], interval[1]);
            } else {
                current = interval;
                merged.add(current);
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
