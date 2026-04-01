# Best Time to Buy and Sell Stock

![](../../assets/images/arrays/best_time_to_buy_and_sell_stock.svg)


**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
You are given an array `prices` where `prices[i]` is the price of a given stock on the `i-th` day.

You want to maximize your profit by choosing a **single day** to buy one stock and choosing a **different day in the future** to sell that stock.

Return the **maximum profit** you can achieve from this transaction. If you cannot achieve any profit, return `0`.

---

## 🔹 Examples
**Example 1:**  
Input: `prices = [7, 1, 5, 3, 6, 4]`  
Output: `5`  
Explanation: Buy on day 2 (price = 1) and sell on day 5 (price = 6), profit = 6 - 1 = 5.

**Example 2:**  
Input: `prices = [7, 6, 4, 3, 1]`  
Output: `0`  
Explanation: In this case, no transactions are done and the max profit = 0.

**Example 3:**  
Input: `prices = [2, 4, 1]`  
Output: `2`  
Explanation: Buy on day 1 (price = 2) and sell on day 2 (price = 4), profit = 4 - 2 = 2.

---

## 🔹 Constraints
- `1 <= prices.length <= 10^5`
- `0 <= prices[i] <= 10^4`

---

## 🔹 Logic & Intuition
- We need to find the **maximum difference** between two prices where the buying price comes before the selling price.
- **Key Insight:** For each day, we want to know the minimum price seen so far (best buying opportunity).
- Then calculate potential profit if we sell on the current day.
- Track the maximum profit seen throughout.

**Why this works:**
- As we iterate, we keep track of the lowest price we've seen (best time to buy).
- For each price, we calculate profit if we sell at current price after buying at the lowest.
- We update our maximum profit if current profit is better.

---

## 🔹 Approaches

### 1. Brute Force
- Check all pairs (i, j) where i < j.
- Calculate `prices[j] - prices[i]` for each pair.
- Track the maximum profit.

**Time Complexity:** O(n²)  
**Space Complexity:** O(1)

---

### 2. Optimized (Single Pass - Kadane's Algorithm Variant)
- Keep track of the **minimum price** seen so far.
- At each step, calculate profit if we sell at current price.
- Update maximum profit if current profit is better.
- Update minimum price if current price is lower.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
public class BestTimeToBuyAndSellStock {

    // Brute force approach - Check all pairs
    public static int maxProfitBrute(int[] prices) {
        int maxProfit = 0;
        
        for (int i = 0; i < prices.length; i++) {
            for (int j = i + 1; j < prices.length; j++) {
                int profit = prices[j] - prices[i];
                maxProfit = Math.max(maxProfit, profit);
            }
        }
        
        return maxProfit;
    }

    // Optimized approach - Single pass with tracking minimum
    public static int maxProfit(int[] prices) {
        if (prices == null || prices.length == 0) {
            return 0;
        }
        
        int minPrice = prices[0];
        int maxProfit = 0;
        
        for (int i = 1; i < prices.length; i++) {
            // Calculate profit if we sell at current price
            int profit = prices[i] - minPrice;
            
            // Update maximum profit
            maxProfit = Math.max(maxProfit, profit);
            
            // Update minimum price seen so far
            minPrice = Math.min(minPrice, prices[i]);
        }
        
        return maxProfit;
    }

    // Alternative: More explicit approach
    public static int maxProfitExplicit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;
        
        for (int price : prices) {
            if (price < minPrice) {
                minPrice = price;  // Update minimum buying price
            } else if (price - minPrice > maxProfit) {
                maxProfit = price - minPrice;  // Update max profit
            }
        }
        
        return maxProfit;
    }

    public static void main(String[] args) {
        // Test Case 1
        int[] prices1 = {7, 1, 5, 3, 6, 4};
        System.out.println("Prices: [7,1,5,3,6,4]");
        System.out.println("Max Profit (Brute): " + maxProfitBrute(prices1));
        System.out.println("Max Profit (Optimized): " + maxProfit(prices1));
        System.out.println("Expected: 5\n");

        // Test Case 2
        int[] prices2 = {7, 6, 4, 3, 1};
        System.out.println("Prices: [7,6,4,3,1]");
        System.out.println("Max Profit: " + maxProfit(prices2));
        System.out.println("Expected: 0\n");

        // Test Case 3
        int[] prices3 = {2, 4, 1};
        System.out.println("Prices: [2,4,1]");
        System.out.println("Max Profit: " + maxProfit(prices3));
        System.out.println("Expected: 2\n");

        // Test Case 4
        int[] prices4 = {3, 2, 6, 5, 0, 3};
        System.out.println("Prices: [3,2,6,5,0,3]");
        System.out.println("Max Profit: " + maxProfit(prices4));
        System.out.println("Expected: 4\n");
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|----------|----------------|------------------|
| Brute Force | O(n²) | O(1) |
| **Optimized (Single Pass)** | **O(n)** | **O(1)** |

---

## 🔹 Key Insights

1. **One Transaction Rule:** You can only buy once and sell once.
2. **Order Matters:** You must buy before you sell (can't sell before buying).
3. **Greedy Approach:** Always try to buy at the lowest price seen so far.
4. **Running Maximum:** Track the best profit possible at each step.

---

## 🔹 Edge Cases

| Case | Input | Output | Explanation |
|------|-------|--------|-------------|
| Decreasing prices | `[7,6,4,3,1]` | `0` | No profitable transaction |
| Single element | `[5]` | `0` | Need at least 2 days |
| All same price | `[3,3,3,3]` | `0` | No price difference |
| Two elements | `[1,5]` | `4` | Buy at 1, sell at 5 |
| Min at end | `[5,4,3,2,1]` | `0` | Can't sell before buying |
| Max profit at end | `[1,2,3,4,5]` | `4` | Buy at 1, sell at 5 |

---

## 🔹 Common Mistakes

1. **Selling before buying:** Ensure buy index < sell index.
2. **Not handling empty array:** Check for null/empty input.
3. **Using wrong initial values:** Initialize `minPrice` with `prices[0]` or `Integer.MAX_VALUE`.
4. **Forgetting to return 0:** When no profit is possible.

---

## 🔹 Follow-Up Questions

### 1. Multiple Transactions
**Q:** What if you could make multiple transactions (buy and sell multiple times)?  
**A:** This becomes "Best Time to Buy and Sell Stock II" - use a greedy approach to capture every upward price movement.

### 2. At Most K Transactions
**Q:** What if you could make at most k transactions?  
**A:** Use dynamic programming with state: `dp[i][k]` = max profit on day i with at most k transactions.

### 3. With Transaction Fee
**Q:** What if each transaction has a fee?  
**A:** Modify the profit calculation to subtract the fee when selling.

### 4. With Cooldown
**Q:** What if you must wait 1 day after selling before buying again?  
**A:** Use DP with states: bought, sold, cooldown.

---

## 🔹 Related Problems

- **Best Time to Buy and Sell Stock II** (Multiple Transactions)
- **Best Time to Buy and Sell Stock III** (At Most 2 Transactions)
- **Best Time to Buy and Sell Stock IV** (At Most K Transactions)
- **Best Time to Buy and Sell Stock with Cooldown**
- **Best Time to Buy and Sell Stock with Transaction Fee**
- **Maximum Subarray** (Similar single-pass pattern)

---

## 🔹 Variations

### Variation 1: Return the Buy and Sell Days
```java
public static int[] maxProfitWithDays(int[] prices) {
    int minPrice = prices[0];
    int maxProfit = 0;
    int buyDay = 0, sellDay = 0;
    int tempBuyDay = 0;
    
    for (int i = 1; i < prices.length; i++) {
        int profit = prices[i] - minPrice;
        
        if (profit > maxProfit) {
            maxProfit = profit;
            buyDay = tempBuyDay;
            sellDay = i;
        }
        
        if (prices[i] < minPrice) {
            minPrice = prices[i];
            tempBuyDay = i;
        }
    }
    
    return new int[]{buyDay, sellDay, maxProfit};
}
```

### Variation 2: Multiple Transactions (Unlimited)
```java
public static int maxProfitMultiple(int[] prices) {
    int maxProfit = 0;
    
    for (int i = 1; i < prices.length; i++) {
        // Capture every upward movement
        if (prices[i] > prices[i - 1]) {
            maxProfit += prices[i] - prices[i - 1];
        }
    }
    
    return maxProfit;
}
```

---

## 🔹 Pattern Recognition

This problem belongs to the **"Kadane's Algorithm"** family:
- Track a running minimum/maximum
- Calculate result based on current value and running value
- Update both result and running value

**Similar Pattern Problems:**
- Maximum Subarray Sum (Kadane's Algorithm)
- Maximum Product Subarray
- House Robber (DP with running max)
