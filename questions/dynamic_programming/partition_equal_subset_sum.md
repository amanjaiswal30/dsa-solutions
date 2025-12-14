# Partition Equal Subset Sum – Detailed Explanation

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given a **non-empty** array `nums` containing **only positive integers**, find if the array can be partitioned into two subsets such that the sum of elements in both subsets is equal.

---

## 🔹 Examples

**Example 1:**  
Input: `nums = [1,5,11,5]`  
Output: `true`  
Explanation: [1,5,5] and [11] have equal sum = 11

**Example 2:**  
Input: `nums = [1,2,3,5]`  
Output: `false`  
Explanation: Cannot partition into equal sum subsets

**Example 3:**  
Input: `nums = [1,2,5]`  
Output: `false`

---

## 🔹 Core Intuition

**Key Insight:** This reduces to the **Subset Sum Problem**.

If total sum is **odd** → impossible to partition equally.  
If total sum is **even** → find subset with sum = totalSum / 2.

**This is a 0/1 Knapsack variant!**

**Recurrence:**
```
dp[i][sum] = can make sum using first i elements?

dp[i][sum] = dp[i-1][sum] || dp[i-1][sum - nums[i]]
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class PartitionEqualSubsetSum {
    
    public boolean canPartitionRecursive(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        if (totalSum % 2 != 0) return false;
        
        return canMakeSum(nums, nums.length - 1, totalSum / 2);
    }
    
    private boolean canMakeSum(int[] nums, int index, int sum) {
        if (sum == 0) return true;
        if (index < 0 || sum < 0) return false;
        
        return canMakeSum(nums, index - 1, sum) ||  // Exclude
               canMakeSum(nums, index - 1, sum - nums[index]);  // Include
    }
}
```

---

## 2️⃣ Memoization (Top-Down DP)

### Code (Java)

```java
public class PartitionEqualSubsetSum {
    
    public boolean canPartitionMemo(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        if (totalSum % 2 != 0) return false;
        
        int target = totalSum / 2;
        Boolean[][] memo = new Boolean[nums.length][target + 1];
        
        return canMakeSumMemo(nums, nums.length - 1, target, memo);
    }
    
    private boolean canMakeSumMemo(int[] nums, int index, int sum, Boolean[][] memo) {
        if (sum == 0) return true;
        if (index < 0 || sum < 0) return false;
        
        if (memo[index][sum] != null) {
            return memo[index][sum];
        }
        
        memo[index][sum] = canMakeSumMemo(nums, index - 1, sum, memo) ||
                           canMakeSumMemo(nums, index - 1, sum - nums[index], memo);
        
        return memo[index][sum];
    }
}
```

---

## 3️⃣ Dynamic Programming – 2D Array

### Code (Java)

```java
public class PartitionEqualSubsetSum {
    
    public boolean canPartitionDP(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        if (totalSum % 2 != 0) return false;
        
        int target = totalSum / 2;
        int n = nums.length;
        boolean[][] dp = new boolean[n + 1][target + 1];
        
        // Base case: sum 0 is always possible
        for (int i = 0; i <= n; i++) {
            dp[i][0] = true;
        }
        
        for (int i = 1; i <= n; i++) {
            for (int sum = 1; sum <= target; sum++) {
                dp[i][sum] = dp[i - 1][sum];  // Exclude
                
                if (sum >= nums[i - 1]) {
                    dp[i][sum] = dp[i][sum] || dp[i - 1][sum - nums[i - 1]];  // Include
                }
            }
        }
        
        return dp[n][target];
    }
}
```

---

## 4️⃣ Space Optimized – 1D Array ⭐ OPTIMAL

### Code (Java)

```java
public class PartitionEqualSubsetSum {
    
    public boolean canPartition(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        if (totalSum % 2 != 0) return false;
        
        int target = totalSum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
        
        for (int num : nums) {
            // Traverse right to left to avoid using updated values
            for (int sum = target; sum >= num; sum--) {
                dp[sum] = dp[sum] || dp[sum - num];
            }
        }
        
        return dp[target];
    }
}
```

### Example Walkthrough

For `nums = [1,5,11,5]`:
- totalSum = 22, target = 11

DP array evolution:
```
Initial: [T, F, F, F, F, F, F, F, F, F, F, F]
After 1: [T, T, F, F, F, F, F, F, F, F, F, F]
After 5: [T, T, F, F, F, T, T, F, F, F, F, F]
After 11:[T, T, F, F, F, T, T, F, F, F, F, T]
After 5: [T, T, F, F, F, T, T, F, F, F, T, T]
```

**Answer:** dp[11] = **true**

### Complexity
- **Time:** O(n × sum)
- **Space:** O(sum) ⭐

---

## 🔄 Variations

### Variation 1: Partition with Minimum Difference

**Problem:** Minimize |sum1 - sum2|.

```java
public class PartitionVariations {
    
    public int minimumDifference(int[] nums) {
        int totalSum = 0;
        for (int num : nums) {
            totalSum += num;
        }
        
        int target = totalSum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
        
        for (int num : nums) {
            for (int sum = target; sum >= num; sum--) {
                dp[sum] = dp[sum] || dp[sum - num];
            }
        }
        
        // Find maximum sum <= target that is achievable
        int maxSum = 0;
        for (int sum = target; sum >= 0; sum--) {
            if (dp[sum]) {
                maxSum = sum;
                break;
            }
        }
        
        return totalSum - 2 * maxSum;
    }
}
```

### Variation 2: Count Subsets with Given Difference

**Problem:** Count pairs of subsets with difference D.

```java
public int countSubsetsWithDiff(int[] nums, int diff) {
    int totalSum = 0;
    for (int num : nums) {
        totalSum += num;
    }
    
    if ((diff + totalSum) % 2 != 0) return 0;
    
    int target = (diff + totalSum) / 2;
    
    int[] dp = new int[target + 1];
    dp[0] = 1;
    
    for (int num : nums) {
        for (int sum = target; sum >= num; sum--) {
            dp[sum] += dp[sum - num];
        }
    }
    
    return dp[target];
}
```

### Variation 3: Partition into K Equal Sum Subsets

```java
public boolean canPartitionKSubsets(int[] nums, int k) {
    int totalSum = 0;
    for (int num : nums) {
        totalSum += num;
    }
    
    if (totalSum % k != 0) return false;
    
    int target = totalSum / k;
    Arrays.sort(nums);
    
    int n = nums.length;
    if (nums[n - 1] > target) return false;
    
    boolean[] visited = new boolean[n];
    return backtrack(nums, visited, 0, k, 0, target);
}

private boolean backtrack(int[] nums, boolean[] visited, int start, int k, int currentSum, int target) {
    if (k == 1) return true;
    
    if (currentSum == target) {
        return backtrack(nums, visited, 0, k - 1, 0, target);
    }
    
    for (int i = start; i < nums.length; i++) {
        if (visited[i] || currentSum + nums[i] > target) continue;
        
        visited[i] = true;
        if (backtrack(nums, visited, i + 1, k, currentSum + nums[i], target)) {
            return true;
        }
        visited[i] = false;
    }
    
    return false;
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^n) | O(n) |
| Memoization | O(n×sum) | O(n×sum) |
| DP 2D | O(n×sum) | O(n×sum) |
| Space Optimized | O(n×sum) | O(sum) ⭐ |

---

## 🎯 Key Takeaways

1. **Reduce to subset sum:** Find subset with sum = totalSum/2
2. **Odd total sum:** Impossible to partition equally
3. **0/1 Knapsack pattern:** Include or exclude each element
4. **Right to left traversal:** Ensures each element used once
5. **Space optimization:** Use 1D array instead of 2D

**Classic 0/1 Knapsack application!** 🚀
