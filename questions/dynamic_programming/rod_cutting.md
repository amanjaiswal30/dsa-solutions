# Rod Cutting Problem – Detailed Explanation

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement

Given a rod of length `n` and an array `prices` where `prices[i]` denotes the price of a rod of length `i+1`, determine the maximum revenue obtainable by cutting up the rod and selling the pieces.

**Note:** You can make any number of cuts (including zero cuts).

---

## 🔹 Examples

**Example 1:**  
Input: `prices = [1, 5, 8, 9, 10, 17, 17, 20]`, `n = 8`  
Output: `22`  
Explanation: Cut into pieces of length 2 and 6 (price = 5 + 17 = 22)

**Example 2:**  
Input: `prices = [3, 5, 8, 9, 10, 17, 17, 20]`, `n = 8`  
Output: `24`  
Explanation: Cut into 8 pieces of length 1 (price = 8×3 = 24)

**Example 3:**  
Input: `prices = [1, 5, 8, 9]`, `n = 4`  
Output: `10`  
Explanation: Cut into 2 pieces of length 2 (price = 5 + 5 = 10)

---

## 🔹 Core Intuition

**This is an Unbounded Knapsack problem!**

For each length, try all possible cuts and take maximum revenue.

**Recurrence:**
```
dp[n] = maximum revenue for rod of length n

dp[n] = max(prices[i] + dp[n-i-1]) for i in [0, n-1]
```

**Base Case:**
```
dp[0] = 0 (no rod, no revenue)
```

---

## 1️⃣ Recursive Approach

### Code (Java)

```java
public class RodCutting {
    
    public int cutRodRecursive(int[] prices, int n) {
        if (n == 0) return 0;
        
        int maxRevenue = Integer.MIN_VALUE;
        
        for (int i = 0; i < n; i++) {
            maxRevenue = Math.max(maxRevenue, 
                                  prices[i] + cutRodRecursive(prices, n - i - 1));
        }
        
        return maxRevenue;
    }
}
```

### Complexity
- **Time:** O(2^n) — exponential
- **Space:** O(n) — recursion depth

---

## 2️⃣ Memoization (Top-Down DP)

### Code (Java)

```java
public class RodCutting {
    
    public int cutRodMemo(int[] prices, int n) {
        int[] memo = new int[n + 1];
        Arrays.fill(memo, -1);
        return cutRodMemoHelper(prices, n, memo);
    }
    
    private int cutRodMemoHelper(int[] prices, int n, int[] memo) {
        if (n == 0) return 0;
        
        if (memo[n] != -1) {
            return memo[n];
        }
        
        int maxRevenue = Integer.MIN_VALUE;
        
        for (int i = 0; i < n; i++) {
            maxRevenue = Math.max(maxRevenue, 
                                  prices[i] + cutRodMemoHelper(prices, n - i - 1, memo));
        }
        
        memo[n] = maxRevenue;
        return maxRevenue;
    }
}
```

---

## 3️⃣ Dynamic Programming – Bottom-Up ⭐ OPTIMAL

### Code (Java)

```java
public class RodCutting {
    
    public int cutRod(int[] prices, int n) {
        int[] dp = new int[n + 1];
        dp[0] = 0;
        
        for (int length = 1; length <= n; length++) {
            int maxRevenue = Integer.MIN_VALUE;
            
            for (int i = 0; i < length; i++) {
                maxRevenue = Math.max(maxRevenue, prices[i] + dp[length - i - 1]);
            }
            
            dp[length] = maxRevenue;
        }
        
        return dp[n];
    }
}
```

### Example Walkthrough

For `prices = [1, 5, 8, 9, 10, 17, 17, 20]`, `n = 8`:

| Length | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|--------|---|---|---|---|---|---|---|---|---|
| **DP** | 0 | 1 | 5 | 8 | 10 | 13 | 17 | 18 | **22** |

**Answer:** **22**

### Complexity
- **Time:** O(n²)
- **Space:** O(n)

---

## 4️⃣ Print Cuts (Reconstruction)

### Code (Java)

```java
public class RodCutting {
    
    public List<Integer> cutRodWithSolution(int[] prices, int n) {
        int[] dp = new int[n + 1];
        int[] cuts = new int[n + 1];  // Track first cut length
        
        dp[0] = 0;
        
        for (int length = 1; length <= n; length++) {
            int maxRevenue = Integer.MIN_VALUE;
            
            for (int i = 0; i < length; i++) {
                if (prices[i] + dp[length - i - 1] > maxRevenue) {
                    maxRevenue = prices[i] + dp[length - i - 1];
                    cuts[length] = i + 1;  // Store cut length
                }
            }
            
            dp[length] = maxRevenue;
        }
        
        // Reconstruct solution
        List<Integer> solution = new ArrayList<>();
        int remaining = n;
        
        while (remaining > 0) {
            int cutLength = cuts[remaining];
            solution.add(cutLength);
            remaining -= cutLength;
        }
        
        return solution;
    }
}
```

---

## 🔄 Variations

### Variation 1: Rod Cutting with Cost

**Problem:** Each cut has a cost. Maximize revenue - total cut cost.

```java
public class RodCuttingVariations {
    
    public int cutRodWithCost(int[] prices, int n, int cutCost) {
        int[] dp = new int[n + 1];
        dp[0] = 0;
        
        for (int length = 1; length <= n; length++) {
            dp[length] = prices[length - 1];  // No cut option
            
            for (int i = 1; i < length; i++) {
                dp[length] = Math.max(dp[length], 
                                     dp[i] + dp[length - i] - cutCost);
            }
        }
        
        return dp[n];
    }
}
```

### Variation 2: Rod Cutting with Limited Cuts

**Problem:** Can make at most K cuts.

```java
public int cutRodKCuts(int[] prices, int n, int k) {
    // dp[len][cuts] = max revenue for length len with cuts made
    int[][] dp = new int[n + 1][k + 2];
    
    for (int[] row : dp) {
        Arrays.fill(row, Integer.MIN_VALUE);
    }
    
    for (int cuts = 0; cuts <= k + 1; cuts++) {
        dp[0][cuts] = 0;
    }
    
    for (int len = 1; len <= n; len++) {
        for (int cuts = 0; cuts <= k + 1; cuts++) {
            // No cut
            if (len <= prices.length) {
                dp[len][cuts] = Math.max(dp[len][cuts], prices[len - 1]);
            }
            
            // Make a cut
            if (cuts > 0) {
                for (int i = 1; i < len; i++) {
                    if (dp[i][cuts - 1] != Integer.MIN_VALUE && 
                        dp[len - i][cuts - 1] != Integer.MIN_VALUE) {
                        dp[len][cuts] = Math.max(dp[len][cuts], 
                                                dp[i][cuts - 1] + dp[len - i][cuts - 1]);
                    }
                }
            }
        }
    }
    
    int maxRevenue = Integer.MIN_VALUE;
    for (int cuts = 0; cuts <= k + 1; cuts++) {
        maxRevenue = Math.max(maxRevenue, dp[n][cuts]);
    }
    
    return maxRevenue;
}
```

### Variation 3: Minimize Cuts (Instead of Maximize Revenue)

```java
public int minCuts(int[] lengths, int totalLength) {
    int[] dp = new int[totalLength + 1];
    Arrays.fill(dp, Integer.MAX_VALUE);
    dp[0] = 0;
    
    for (int len = 1; len <= totalLength; len++) {
        for (int cutLen : lengths) {
            if (cutLen <= len && dp[len - cutLen] != Integer.MAX_VALUE) {
                dp[len] = Math.min(dp[len], dp[len - cutLen] + 1);
            }
        }
    }
    
    return dp[totalLength] == Integer.MAX_VALUE ? -1 : dp[totalLength];
}
```

### Variation 4: Perfect Squares (Similar Problem)

```java
public int numSquares(int n) {
    int[] dp = new int[n + 1];
    Arrays.fill(dp, Integer.MAX_VALUE);
    dp[0] = 0;
    
    for (int i = 1; i <= n; i++) {
        for (int j = 1; j * j <= i; j++) {
            dp[i] = Math.min(dp[i], dp[i - j * j] + 1);
        }
    }
    
    return dp[n];
}
```

---

## 📊 Complexity

| Approach | Time | Space |
|----------|------|-------|
| Recursive | O(2^n) | O(n) |
| Memoization | O(n²) | O(n) |
| DP Bottom-Up | O(n²) | O(n) ⭐ |

---

## 🎯 Key Takeaways

1. **Unbounded knapsack:** Can use same length multiple times
2. **Try all cuts:** For each length, try all possible first cuts
3. **Optimal substructure:** Max revenue = best first cut + max revenue for remaining
4. **Reconstruction:** Track cuts to print solution
5. **Variations:** With cost, limited cuts, minimize cuts

**Classic unbounded knapsack problem!** 🚀
