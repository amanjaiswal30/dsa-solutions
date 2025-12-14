# House Robber (Max Sum Without Adjacent) – Detailed Explanation

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

You are a professional robber planning to rob houses along a street. Each house has a certain amount of money stashed. Adjacent houses have security systems connected that will automatically contact the police if two adjacent houses were broken into on the same night.

Given an integer array `nums` representing the amount of money of each house, return the **maximum amount of money** you can rob tonight **without alerting the police**.

---

## 🔹 Examples

**Example 1:**  
Input: `nums = [1,2,3,1]`  
Output: `4`  
Explanation: Rob house 1 (money = 1) and house 3 (money = 3), total = 4

**Example 2:**  
Input: `nums = [2,7,9,3,1]`  
Output: `12`  
Explanation: Rob house 1 (money = 2), house 3 (money = 9) and house 5 (money = 1), total = 12

**Example 3:**  
Input: `nums = [2,1,1,2]`  
Output: `4`  
Explanation: Rob house 1 and house 4

---

## 🔹 Core Intuition

For each house, decide:
1. **Rob it:** Take current + max from (i-2)
2. **Skip it:** Take max from (i-1)

**Recurrence:**
```
dp[i] = max(nums[i] + dp[i-2], dp[i-1])
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class HouseRobber {
    
    public int robRecursive(int[] nums) {
        return robHelper(nums, 0);
    }
    
    private int robHelper(int[] nums, int index) {
        if (index >= nums.length) return 0;
        
        int rob = nums[index] + robHelper(nums, index + 2);
        int skip = robHelper(nums, index + 1);
        
        return Math.max(rob, skip);
    }
}
```

---

## 2️⃣ Dynamic Programming – O(n)

### Code (Java)

```java
public class HouseRobber {
    
    public int robDP(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        if (nums.length == 1) return nums[0];
        
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        
        for (int i = 2; i < nums.length; i++) {
            dp[i] = Math.max(nums[i] + dp[i - 2], dp[i - 1]);
        }
        
        return dp[nums.length - 1];
    }
}
```

### DP Array Example

For `nums = [2,7,9,3,1]`:

| i | nums[i] | dp[i] | Explanation |
|---|---------|-------|-------------|
| 0 | 2 | 2 | Only one house |
| 1 | 7 | 7 | Max(2, 7) |
| 2 | 9 | 11 | Max(9+2, 7) |
| 3 | 3 | 11 | Max(3+7, 11) |
| 4 | 1 | **12** | Max(1+11, 11) |

**Answer:** **12**

---

## 3️⃣ Space Optimized – O(1) ⭐ OPTIMAL

### Code (Java)

```java
public class HouseRobber {
    
    public int rob(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        if (nums.length == 1) return nums[0];
        
        int prev2 = 0;
        int prev1 = 0;
        
        for (int num : nums) {
            int current = Math.max(num + prev2, prev1);
            prev2 = prev1;
            prev1 = current;
        }
        
        return prev1;
    }
}
```

---

## 🔄 Variations

### Variation 1: House Robber II (Circular)

**Problem:** Houses are arranged in a circle (first and last are adjacent).

```java
public class HouseRobberII {
    
    public int rob(int[] nums) {
        if (nums.length == 1) return nums[0];
        
        // Case 1: Rob houses 0 to n-2
        int max1 = robLinear(nums, 0, nums.length - 2);
        
        // Case 2: Rob houses 1 to n-1
        int max2 = robLinear(nums, 1, nums.length - 1);
        
        return Math.max(max1, max2);
    }
    
    private int robLinear(int[] nums, int start, int end) {
        int prev2 = 0, prev1 = 0;
        
        for (int i = start; i <= end; i++) {
            int current = Math.max(nums[i] + prev2, prev1);
            prev2 = prev1;
            prev1 = current;
        }
        
        return prev1;
    }
}
```

### Variation 2: House Robber III (Binary Tree)

```java
public class HouseRobberIII {
    
    public int rob(TreeNode root) {
        int[] result = robHelper(root);
        return Math.max(result[0], result[1]);
    }
    
    private int[] robHelper(TreeNode node) {
        if (node == null) return new int[]{0, 0};
        
        int[] left = robHelper(node.left);
        int[] right = robHelper(node.right);
        
        // [rob this node, don't rob this node]
        int rob = node.val + left[1] + right[1];
        int notRob = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);
        
        return new int[]{rob, notRob};
    }
}
```

### Variation 3: Delete and Earn

```java
public int deleteAndEarn(int[] nums) {
    int maxNum = 0;
    for (int num : nums) {
        maxNum = Math.max(maxNum, num);
    }
    
    int[] sums = new int[maxNum + 1];
    for (int num : nums) {
        sums[num] += num;
    }
    
    return rob(sums);  // Apply house robber logic
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^n) | O(n) |
| DP Array | O(n) | O(n) |
| Space Optimized | O(n) | O(1) ⭐ |

---

## 🎯 Key Takeaways

1. **Classic DP:** Max of rob vs skip
2. **No adjacent:** Can't take consecutive elements
3. **Space optimization:** Only need last two values
4. **Variations:** Circular, binary tree, delete and earn

**Essential DP pattern for non-adjacent selection!** 🚀
