# 0/1 Knapsack Problem – Detailed Explanation and Approaches

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

You are given **N items**, each with a **weight** and a **value**. You have a knapsack with a maximum **weight capacity W**.

Your goal is to select items to put in the knapsack such that:
1. The total weight does not exceed W
2. The total value is **maximized**

**Constraint:** Each item can be taken **0 or 1 time** (cannot take fractional parts or multiple copies).

---

## 🔹 Examples

**Example 1:**  
Input: 
```
weights = [2, 3, 4, 5]
values = [3, 4, 5, 6]
W = 5
```
Output: `7`  
Explanation: Take items with weight 2 and 3 (value = 3 + 4 = 7)

**Example 2:**  
Input:
```
weights = [1, 3, 4, 5]
values = [1, 4, 5, 7]
W = 7
```
Output: `9`  
Explanation: Take items with weight 3 and 4 (value = 4 + 5 = 9)

**Example 3:**  
Input:
```
weights = [10, 20, 30]
values = [60, 100, 120]
W = 50
```
Output: `220`  
Explanation: Take items with weight 20 and 30 (value = 100 + 120 = 220)

---

## 🔹 Constraints
- `1 <= N <= 1000`
- `1 <= W <= 1000`
- `1 <= weights[i] <= W`
- `1 <= values[i] <= 1000`

---

## 🔹 Core Intuition

For each item, we have **two choices**:
1. **Include** the item (if weight allows)
2. **Exclude** the item

**Recurrence Relation:**
```
dp[i][w] = maximum value using first i items with capacity w

If weight[i-1] > w:
    dp[i][w] = dp[i-1][w]  // Can't include, too heavy
Else:
    dp[i][w] = max(
        dp[i-1][w],                          // Exclude item i
        dp[i-1][w - weight[i-1]] + value[i-1] // Include item i
    )
```

**Base Case:**
```
dp[0][w] = 0  // No items, value = 0
dp[i][0] = 0  // No capacity, value = 0
```

---

## 1️⃣ Recursive Approach (Brute Force)

### Explanation
For each item, recursively explore both choices:
- Include it and solve for remaining capacity
- Exclude it and solve for same capacity

Return the maximum of both choices.

### Code (Java)

```java
public class Knapsack01 {
    
    // Approach 1: Recursive (Brute Force)
    public int knapsackRecursive(int W, int[] weights, int[] values, int n) {
        // Base case: no items or no capacity
        if (n == 0 || W == 0) {
            return 0;
        }
        
        // If weight of nth item is more than capacity, skip it
        if (weights[n - 1] > W) {
            return knapsackRecursive(W, weights, values, n - 1);
        }
        
        // Return max of two cases:
        // 1. nth item included
        // 2. nth item excluded
        return Math.max(
            values[n - 1] + knapsackRecursive(W - weights[n - 1], weights, values, n - 1),
            knapsackRecursive(W, weights, values, n - 1)
        );
    }
}
```

### Drawbacks
- **Time Complexity:** O(2^n) — exponential, tries all combinations
- **Space Complexity:** O(n) — recursion stack depth
- Recomputes same subproblems multiple times

### Recursion Tree Example
```
knapsack(W=5, items=[2,3,4], values=[3,4,5], n=3)
                    /                      \
         Include item 3                Exclude item 3
         (w=4, v=5)                    (w=5, v=0)
        /           \                  /           \
   Include 2    Exclude 2        Include 2    Exclude 2
   ...           ...              ...           ...
```

---

## 2️⃣ Recursive with Memoization (Top-Down DP)

### Explanation
Cache results using a 2D array `memo[i][w]`:
- `i` = number of items considered
- `w` = remaining capacity

Before solving a subproblem, check if already computed.

### Code (Java)

```java
public class Knapsack01 {
    
    // Approach 2: Memoization (Top-Down DP)
    public int knapsackMemo(int W, int[] weights, int[] values, int n) {
        int[][] memo = new int[n + 1][W + 1];
        
        // Initialize memo with -1 (uncomputed)
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        
        return knapsackMemoHelper(W, weights, values, n, memo);
    }
    
    private int knapsackMemoHelper(int W, int[] weights, int[] values, int n, int[][] memo) {
        // Base case
        if (n == 0 || W == 0) {
            return 0;
        }
        
        // Check if already computed
        if (memo[n][W] != -1) {
            return memo[n][W];
        }
        
        // If weight of nth item is more than capacity
        if (weights[n - 1] > W) {
            memo[n][W] = knapsackMemoHelper(W, weights, values, n - 1, memo);
        } else {
            // Max of including or excluding nth item
            memo[n][W] = Math.max(
                values[n - 1] + knapsackMemoHelper(W - weights[n - 1], weights, values, n - 1, memo),
                knapsackMemoHelper(W, weights, values, n - 1, memo)
            );
        }
        
        return memo[n][W];
    }
}
```

### Complexity
- **Time Complexity:** O(N × W) — each state computed once
- **Space Complexity:** O(N × W) for memo + O(N) for recursion stack

---

## 3️⃣ Dynamic Programming – Bottom-Up Approach (Tabulation)

### Explanation
Build a 2D DP table iteratively:
- **Rows** represent items (0 to N)
- **Columns** represent capacity (0 to W)
- **dp[i][w]** = maximum value using first i items with capacity w

Fill table row by row using the recurrence relation.

### Code (Java)

```java
public class Knapsack01 {
    
    // Approach 3: Bottom-Up DP (Tabulation)
    public int knapsackDP(int W, int[] weights, int[] values, int n) {
        int[][] dp = new int[n + 1][W + 1];
        
        // Build table bottom-up
        for (int i = 0; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                if (i == 0 || w == 0) {
                    dp[i][w] = 0;  // Base case
                } else if (weights[i - 1] <= w) {
                    // Max of including or excluding current item
                    dp[i][w] = Math.max(
                        values[i - 1] + dp[i - 1][w - weights[i - 1]],
                        dp[i - 1][w]
                    );
                } else {
                    // Can't include (too heavy)
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        
        return dp[n][W];
    }
}
```

### Complexity
- **Time Complexity:** O(N × W)
- **Space Complexity:** O(N × W)

### DP Table Example

For `weights=[2,3,4]`, `values=[3,4,5]`, `W=5`:

|  | w=0 | w=1 | w=2 | w=3 | w=4 | w=5 |
|---|-----|-----|-----|-----|-----|-----|
| **i=0** | 0 | 0 | 0 | 0 | 0 | 0 |
| **i=1** (w=2,v=3) | 0 | 0 | 3 | 3 | 3 | 3 |
| **i=2** (w=3,v=4) | 0 | 0 | 3 | 4 | 4 | 7 |
| **i=3** (w=4,v=5) | 0 | 0 | 3 | 4 | 5 | 7 |

**Answer:** dp[3][5] = **7**

---

## 4️⃣ Dynamic Programming – Space Optimized (1D Array) ⭐

### Explanation
**Observation:** To compute dp[i][w], we only need values from dp[i-1][*].

We can use a **single 1D array** and update it **right to left** to avoid overwriting values we still need.

**This is the OPTIMAL solution!**

### Code (Java)

```java
public class Knapsack01 {
    
    // Approach 4: Space Optimized DP (OPTIMAL)
    public int knapsackOptimized(int W, int[] weights, int[] values, int n) {
        int[] dp = new int[W + 1];
        
        // Process each item
        for (int i = 0; i < n; i++) {
            // Traverse from right to left to avoid overwriting
            for (int w = W; w >= weights[i]; w--) {
                dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
            }
        }
        
        return dp[W];
    }
}
```

### Why Right to Left?
If we go left to right, we might use updated values from current iteration, effectively allowing multiple copies of same item (becomes unbounded knapsack).

Going **right to left** ensures we use values from previous iteration only.

### Complexity
- **Time Complexity:** O(N × W)
- **Space Complexity:** O(W) ⭐

---

## 📊 Complexity Comparison

| Approach | Time | Space | Recommended |
|----------|------|-------|-------------|
| Recursive (Brute Force) | O(2^n) | O(n) | ❌ Never |
| Memoization (Top-Down) | O(N×W) | O(N×W) | ✅ Good |
| Tabulation (Bottom-Up) | O(N×W) | O(N×W) | ✅ Good |
| Space Optimized | O(N×W) | O(W) | ⭐ Best |

---

## 🎯 Finding Which Items to Include

To track which items were selected, we can backtrack through the DP table:

### Code (Java)

```java
public class Knapsack01 {
    
    // Find which items are included
    public List<Integer> findItems(int W, int[] weights, int[] values, int n) {
        int[][] dp = new int[n + 1][W + 1];
        
        // Fill DP table
        for (int i = 0; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                if (i == 0 || w == 0) {
                    dp[i][w] = 0;
                } else if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(
                        values[i - 1] + dp[i - 1][w - weights[i - 1]],
                        dp[i - 1][w]
                    );
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        
        // Backtrack to find items
        List<Integer> items = new ArrayList<>();
        int w = W;
        
        for (int i = n; i > 0 && w > 0; i--) {
            // If value comes from including item i
            if (dp[i][w] != dp[i - 1][w]) {
                items.add(i - 1);  // Add item index
                w -= weights[i - 1];  // Reduce capacity
            }
        }
        
        Collections.reverse(items);
        return items;
    }
}
```

---

## 🔄 Additional Variations with Code

### Variation 1: Subset Sum Problem

**Problem:** Given a set of integers, determine if there exists a subset with sum equal to target.

**Relation to Knapsack:** weights = values = array elements, W = target sum

#### Code (Java)

```java
public class KnapsackVariations {
    
    // Variation 1: Subset Sum
    public boolean subsetSum(int[] nums, int target) {
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;  // Sum 0 is always possible (empty subset)
        
        for (int num : nums) {
            for (int sum = target; sum >= num; sum--) {
                dp[sum] = dp[sum] || dp[sum - num];
            }
        }
        
        return dp[target];
    }
    
    // With items tracking
    public List<Integer> findSubsetSum(int[] nums, int target) {
        boolean[][] dp = new boolean[nums.length + 1][target + 1];
        
        for (int i = 0; i <= nums.length; i++) {
            dp[i][0] = true;
        }
        
        for (int i = 1; i <= nums.length; i++) {
            for (int sum = 1; sum <= target; sum++) {
                dp[i][sum] = dp[i - 1][sum];  // Exclude
                if (sum >= nums[i - 1]) {
                    dp[i][sum] = dp[i][sum] || dp[i - 1][sum - nums[i - 1]];  // Include
                }
            }
        }
        
        if (!dp[nums.length][target]) {
            return new ArrayList<>();  // No solution
        }
        
        // Backtrack
        List<Integer> subset = new ArrayList<>();
        int sum = target;
        for (int i = nums.length; i > 0; i--) {
            if (!dp[i - 1][sum]) {  // Item was included
                subset.add(nums[i - 1]);
                sum -= nums[i - 1];
            }
        }
        
        return subset;
    }
}
```

**Example:** nums=[3,34,4,12,5,2], target=9 → Output: [4,5] or [3,4,2]

---

### Variation 2: Equal Sum Partition

**Problem:** Determine if array can be partitioned into two subsets with equal sum.

**Relation:** Find if subset with sum = totalSum/2 exists.

#### Code (Java)

```java
public class KnapsackVariations {
    
    // Variation 2: Equal Sum Partition
    public boolean canPartition(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        // If odd sum, can't partition equally
        if (totalSum % 2 != 0) {
            return false;
        }
        
        int target = totalSum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
        
        for (int num : nums) {
            for (int sum = target; sum >= num; sum--) {
                dp[sum] = dp[sum] || dp[sum - num];
            }
        }
        
        return dp[target];
    }
}
```

**Example:** nums=[1,5,11,5] → true (partition: [1,5,5] and [11])

---

### Variation 3: Count of Subsets with Given Sum

**Problem:** Count how many subsets have sum equal to target.

#### Code (Java)

```java
public class KnapsackVariations {
    
    // Variation 3: Count Subsets with Sum
    public int countSubsetsWithSum(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;  // One way to make sum 0 (empty subset)
        
        for (int num : nums) {
            for (int sum = target; sum >= num; sum--) {
                dp[sum] += dp[sum - num];
            }
        }
        
        return dp[target];
    }
    
    // 2D DP version for clarity
    public int countSubsetsWithSum2D(int[] nums, int target) {
        int n = nums.length;
        int[][] dp = new int[n + 1][target + 1];
        
        for (int i = 0; i <= n; i++) {
            dp[i][0] = 1;  // One way to make sum 0
        }
        
        for (int i = 1; i <= n; i++) {
            for (int sum = 0; sum <= target; sum++) {
                dp[i][sum] = dp[i - 1][sum];  // Exclude
                if (sum >= nums[i - 1]) {
                    dp[i][sum] += dp[i - 1][sum - nums[i - 1]];  // Include
                }
            }
        }
        
        return dp[n][target];
    }
}
```

**Example:** nums=[2,3,5,6,8,10], target=10 → Output: 3 (subsets: [2,3,5], [2,8], [10])

---

### Variation 4: Minimum Subset Sum Difference

**Problem:** Partition array into two subsets such that absolute difference of their sums is minimized.

#### Code (Java)

```java
public class KnapsackVariations {
    
    // Variation 4: Minimum Subset Sum Difference
    public int minSubsetSumDiff(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        // Find all possible sums up to totalSum/2
        boolean[] dp = new boolean[totalSum / 2 + 1];
        dp[0] = true;
        
        for (int num : nums) {
            for (int sum = totalSum / 2; sum >= num; sum--) {
                dp[sum] = dp[sum] || dp[sum - num];
            }
        }
        
        // Find maximum sum <= totalSum/2
        int maxSum = 0;
        for (int sum = totalSum / 2; sum >= 0; sum--) {
            if (dp[sum]) {
                maxSum = sum;
                break;
            }
        }
        
        // Difference = |sum1 - sum2| = |maxSum - (totalSum - maxSum)|
        return totalSum - 2 * maxSum;
    }
}
```

**Example:** nums=[1,6,11,5] → Output: 1 (partition: [1,5,6] sum=12 and [11] sum=11, diff=1)

---

### Variation 5: Target Sum (Add +/- to Reach Target)

**Problem:** Assign +/- to each number to reach a target sum.

**Conversion:** This becomes: Find subset P with sum = (target + totalSum) / 2

#### Code (Java)

```java
public class KnapsackVariations {
    
    // Variation 5: Target Sum
    public int findTargetSumWays(int[] nums, int target) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        // Check if solution exists
        if (target > totalSum || target < -totalSum || (target + totalSum) % 2 != 0) {
            return 0;
        }
        
        int sum = (target + totalSum) / 2;
        return countSubsetsWithSum(nums, sum);
    }
    
    private int countSubsetsWithSum(int[] nums, int sum) {
        int[] dp = new int[sum + 1];
        dp[0] = 1;
        
        for (int num : nums) {
            for (int s = sum; s >= num; s--) {
                dp[s] += dp[s - num];
            }
        }
        
        return dp[sum];
    }
}
```

**Example:** nums=[1,1,1,1,1], target=3 → Output: 5 ways

---

### Variation 6: Bounded Knapsack (Multiple Copies)

**Problem:** Each item has a limited quantity (not just 0/1).

#### Code (Java)

```java
public class KnapsackVariations {
    
    // Variation 6: Bounded Knapsack
    public int boundedKnapsack(int W, int[] weights, int[] values, int[] quantities, int n) {
        int[] dp = new int[W + 1];
        
        for (int i = 0; i < n; i++) {
            // For each item, try all its copies
            for (int copy = 0; copy < quantities[i]; copy++) {
                for (int w = W; w >= weights[i]; w--) {
                    dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
                }
            }
        }
        
        return dp[W];
    }
}
```

---

## 🎓 Complete Implementation

```java
import java.util.*;

public class Knapsack01Complete {
    
    // ==================== MAIN PROBLEM ====================
    
    // 1. Recursive
    public int knapsackRecursive(int W, int[] weights, int[] values, int n) {
        if (n == 0 || W == 0) return 0;
        if (weights[n - 1] > W) {
            return knapsackRecursive(W, weights, values, n - 1);
        }
        return Math.max(
            values[n - 1] + knapsackRecursive(W - weights[n - 1], weights, values, n - 1),
            knapsackRecursive(W, weights, values, n - 1)
        );
    }
    
    // 2. Memoization
    public int knapsackMemo(int W, int[] weights, int[] values, int n) {
        int[][] memo = new int[n + 1][W + 1];
        for (int[] row : memo) Arrays.fill(row, -1);
        return knapsackMemoHelper(W, weights, values, n, memo);
    }
    
    private int knapsackMemoHelper(int W, int[] weights, int[] values, int n, int[][] memo) {
        if (n == 0 || W == 0) return 0;
        if (memo[n][W] != -1) return memo[n][W];
        
        if (weights[n - 1] > W) {
            memo[n][W] = knapsackMemoHelper(W, weights, values, n - 1, memo);
        } else {
            memo[n][W] = Math.max(
                values[n - 1] + knapsackMemoHelper(W - weights[n - 1], weights, values, n - 1, memo),
                knapsackMemoHelper(W, weights, values, n - 1, memo)
            );
        }
        return memo[n][W];
    }
    
    // 3. Tabulation
    public int knapsackDP(int W, int[] weights, int[] values, int n) {
        int[][] dp = new int[n + 1][W + 1];
        
        for (int i = 0; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                if (i == 0 || w == 0) {
                    dp[i][w] = 0;
                } else if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(
                        values[i - 1] + dp[i - 1][w - weights[i - 1]],
                        dp[i - 1][w]
                    );
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        return dp[n][W];
    }
    
    // 4. Space Optimized (BEST)
    public int knapsackOptimized(int W, int[] weights, int[] values, int n) {
        int[] dp = new int[W + 1];
        
        for (int i = 0; i < n; i++) {
            for (int w = W; w >= weights[i]; w--) {
                dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
            }
        }
        return dp[W];
    }
    
    public static void main(String[] args) {
        Knapsack01Complete kp = new Knapsack01Complete();
        
        int[] weights = {2, 3, 4, 5};
        int[] values = {3, 4, 5, 6};
        int W = 5;
        int n = weights.length;
        
        System.out.println("=== 0/1 Knapsack ===");
        System.out.println("Maximum value: " + kp.knapsackOptimized(W, weights, values, n));
    }
}
```

---

## 🎯 Key Takeaways

1. **Identify Choices:** Each item can be included or excluded
2. **State Definition:** dp[i][w] = max value with first i items and capacity w
3. **Space Optimization:** Use 1D array, traverse right to left
4. **Backtracking:** Track included items through DP table
5. **Master Variations:** Subset sum, partition, target sum all reduce to knapsack

---

## 💡 Pattern Recognition

**0/1 Knapsack Pattern applies to:**
- Subset Sum
- Equal Partition
- Minimum Subset Difference
- Count Subsets with Sum
- Target Sum (with +/-)
- Any "choose or don't choose" optimization problem

This is one of the **most important DP patterns** for interviews! 🚀
