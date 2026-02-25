# Subarray Sum Equals K

![](../../assets/images/arrays/subarray_sum_equals_k.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array of integers `nums` and an integer `k`, return the total number of continuous subarrays whose sum equals to `k`.

---

## 🔹 Logic & Intuition
- Brute force: check all possible subarrays and calculate their sums.
- Optimized: Use **prefix sum + HashMap**.
    - Keep running sum (`prefixSum`).
    - At each step, check if `(prefixSum - k)` exists in the map → this means a subarray ending at current index has sum = `k`.
    - Count all such occurrences.

---

## 🔹 Approaches

### 1. Brute Force
- Generate all subarrays `(i, j)` and compute their sum.
- If sum = `k`, increment count.

**Time Complexity:** O(n²)  
**Space Complexity:** O(1)

---

### 2. Prefix Sum + HashMap (Optimal)
- Maintain `prefixSum` while iterating.
- Use HashMap to store frequency of each prefix sum.
- If `(prefixSum - k)` is in map → add its frequency to count.
- Update map with current `prefixSum`.

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

## 🔹 Java Code

```java
import java.util.HashMap;

public class SubarraySumEqualsK {

    // Brute force
    public static int subarraySumBrute(int[] nums, int k) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            int sum = 0;
            for (int j = i; j < nums.length; j++) {
                sum += nums[j];
                if (sum == k) {
                    count++;
                }
            }
        }
        return count;
    }

    // Optimal: Prefix Sum + HashMap
    public static int subarraySumOptimal(int[] nums, int k) {
        HashMap<Integer, Integer> map = new HashMap<>();
        map.put(0, 1); // base case: sum=0 seen once

        int prefixSum = 0, count = 0;

        for (int num : nums) {
            prefixSum += num;

            if (map.containsKey(prefixSum - k)) {
                count += map.get(prefixSum - k);
            }

            map.put(prefixSum, map.getOrDefault(prefixSum, 0) + 1);
        }
        return count;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                 | Time Complexity | Space Complexity |
|--------------------------|-----------------|------------------|
| Brute Force              | O(n²)           | O(1)             |
| Prefix Sum + HashMap     | O(n)            | O(n)             |

---

## 🔹 Edge Cases
- `nums = [1], k = 0` → result = 0
- Negative numbers in array (works fine with prefix sums).
- Large arrays (must use O(n) approach).

---

## 🔹 Follow-Up Questions
1. How would you solve it if the array contained **only positive numbers**? (Sliding window possible).
2. Can this logic be extended to count subarrays with sum **less than or equal to k**?
3. How to return the actual **subarrays**, not just the count?
