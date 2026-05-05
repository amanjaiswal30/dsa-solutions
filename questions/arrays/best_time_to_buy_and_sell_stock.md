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

    // Same logic as above: fold state with Math.min / Math.max (no if/else needed).
    // After updating minPrice, price - minPrice is 0 on a new low—so maxProfit unchanged.
    public static int maxProfitExplicit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;

        for (int price : prices) {
            minPrice = Math.min(minPrice, price);
            maxProfit = Math.max(maxProfit, price - minPrice);
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
**A:** Keep `minPrice[j]` and `maxProfit[j]` for each transaction leg `j = 0 … k-1`, and update them each day (same recurrence as “at most two transactions,” generalized); `O(k·n)` time, `O(k)` space.

### 3. With Transaction Fee
**Q:** What if each transaction has a fee?  
**A:** Track **`cash`** (best profit, **no** shares) vs **`hold`** (best profit, **one** share, buy cost folded in); subtract **`fee`** on each sell (LeetCode **714**); see **Transaction fee** under Variations below.

### 4. With Cooldown
**Q:** What if you must wait 1 day after selling before buying again?  
**A:** Three states **`buy`**, **`holding`**, **`cooldown`** (see **Cooldown** below): **`dp[day][3]`** or three rolling ints.

---

## 🔹 Related Problems

- **Best Time to Buy and Sell Stock II** (Multiple Transactions)
- **Best Time to Buy and Sell Stock III** (At Most 2 Transactions)
- **Best Time to Buy and Sell Stock IV** (At Most K Transactions)
- **Best Time to Buy and Sell Stock with Cooldown**
- **Best Time to Buy and Sell Stock with Transaction Fee**
- **Maximum Subarray** (Similar single-pass pattern)

---

## 🔹 Variations — classic variants, cooldown, and transaction fee

The first four map to LeetCode **121**, **122**, **123**, and **188**. Same input `prices[]`; only the **transaction limit** changes. **Cooldown** is **309**: unlimited trades, but **no buy on the calendar day immediately after a sell**. **Transaction fee** is **714**: unlimited trades and a fixed **`fee`** deducted every time you **sell**.

| Variant | Rule | Typical approach |
|---------|------|------------------|
| **I** | At most **one** buy and one sell | Single pass: min price so far |
| **II** | **Unlimited** transactions (no overlap) | Greedy: sum all upward day-to-day moves |
| **III** | At most **2** complete round-trips | Extend variant I: `minPrice1`/`maxProfit1`, then `minPrice2`/`maxProfit2` |
| **IV** | At most **k** transactions | Same recurrence as III: `minPrice[j]` / `maxProfit[j]` for `j = 0..k-1` |
| **Cooldown** | Unlimited trades + **1-day buy freeze** after each sell | Three states **`buy` / `holding` / `cooldown`** (`dp[i][3]` or rolling) |
| **Transaction fee** | Unlimited trades + **`fee` per sell** | Two states: **`cash`** (no stock), **`hold`** (long 1 share) |

**Same folding style:** Variants **I–IV** use `Math.min` / `Math.max` as above. **Cooldown:** only **`buy − price`** enters **`holding`** (no buy the day after a close); **`holding + price`** enters **`cooldown`**; **`buy`** becomes **`max(buy, cooldown)`** so the freeze can end. **Transaction fee:** **`cash`** / **`hold`**; the buy step uses **`cashBeforeSell`** (same “read **old** values, then assign **next**” order as rolling cooldown).

---

### Variant I — at most one transaction (covered above)

See **Optimized (Single Pass)** under Approaches above.  
**Bonus — also return buy/sell indices:**

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

---

### Variant II — unlimited transactions

**Rule:** You may buy and sell many times; only one share at a time; must sell before buying again.

**Idea:** Every increasing adjacent pair `prices[i] > prices[i-1]` is profit you can lock in; summing them equals the best total.

```java
/** Best Time to Buy and Sell Stock II — O(n) time, O(1) space */
public static int maxProfitUnlimited(int[] prices) {
    int profit = 0;
    for (int i = 1; i < prices.length; i++) {
        profit += Math.max(0, prices[i] - prices[i - 1]);
    }
    return profit;
}
```

---

### Variant III — at most two transactions

**Rule:** At most **two** non-overlapping buy–sell pairs.

**Idea:** Same recurrence as variant **I**, twice. First leg: `minPrice1` / `maxProfit1`. Second leg: treat `p - maxProfit1` as the effective cost basis for buying again—track its minimum as `minPrice2`, then `maxProfit2 = max(maxProfit2, p - minPrice2)`.

```java
/** Best Time to Buy and Sell Stock III — O(n) time, O(1) space */
public static int maxProfitAtMostTwo(int[] prices) {
    int minPrice1 = Integer.MAX_VALUE;
    int maxProfit1 = 0;
    int minPrice2 = Integer.MAX_VALUE;
    int maxProfit2 = 0;
    for (int p : prices) {
        minPrice1 = Math.min(minPrice1, p);
        maxProfit1 = Math.max(maxProfit1, p - minPrice1);
        minPrice2 = Math.min(minPrice2, p - maxProfit1);
        maxProfit2 = Math.max(maxProfit2, p - minPrice2);
    }
    return maxProfit2;
}
```

---

### Variant IV — at most k transactions

**Rule:** At most **k** complete buy–sell pairs.

**Idea:** Generalize variant **III**: for each transaction index `j` (0-based), keep `minPrice[j]` (best effective buy cost for leg `j+1`) and `maxProfit[j]` (best profit after completing leg `j+1`). For each price, loop **`j` from `0` to `k-1`** so `maxProfit[j-1]` already reflects today before updating leg `j`:

- `minPrice[j] = min(minPrice[j], price - (j == 0 ? 0 : maxProfit[j-1]))`
- `maxProfit[j] = max(maxProfit[j], price - minPrice[j])`

Same logic as variant **III** when `k == 2`. Two length-`k` arrays only; time `O(k·n)`, space `O(k)`.

```java
/** Best Time to Buy and Sell Stock IV — O(k·n) time, O(k) space */
public static int maxProfitKTransactions(int k, int[] prices) {
    int n = prices.length;
    if (n <= 1 || k == 0) {
        return 0;
    }

    int[] minPriceK = new int[k];
    int[] maxProfitK = new int[k];
    for (int i = 0; i < k; i++) {
        minPriceK[i] = Integer.MAX_VALUE;
    }

    for (int price : prices) {
        for (int j = 0; j < k; j++) {
            minPriceK[j] = Math.min(minPriceK[j], price - (j == 0 ? 0 : maxProfitK[j - 1]));
            maxProfitK[j] = Math.max(maxProfitK[j], price - minPriceK[j]);
        }
    }
    return maxProfitK[k - 1];
}
```

---

### Cooldown — unlimited trades with 1-day freeze after sell

**Rule (LeetCode 309):** Unlimited round-trips, **at most one share** at a time. If you **sell** on day `i`, you **may not buy** on day `i + 1`. You **may** buy again on day `i + 2` and later.

**Three states**

Each is **best profit at end of that day** in that state:

| Index | Variable | Meaning |
|:-----:|----------|---------|
| **0** | **`buy`** | No stock; **you may buy tomorrow.** |
| **1** | **`holding`** | You **own one share** (open long). |
| **2** | **`cooldown`** | No stock; you **sold today** → **no buy tomorrow**. |

**Transitions** for `prices[i]` (read **yesterday**, write **today**):

| | Formula |
|--|---------|
| **`buy`** | `max(buy, cooldown)` |
| **`holding`** | `max(holding, buy − price)` |
| **`cooldown`** | `holding + price` |

**Day 0:** `buy = 0`, `holding = −prices[0]`, `cooldown = 0`.

**Return:** `max(buy, cooldown)` — not **`holding`** (still in a position until you exit).

**Example:** `prices = [1,2,3,0,2]` → **3** (e.g. buy 1 → sell 2 → skip one buy day → buy 0 → sell 2).

**1) Easiest to read — `dp[day][state]` (O(n) time, O(n) space)**

Each row is one day; you literally fill the table.

```java
/** Cooldown — explicit DP table (LeetCode 309), O(n) time / O(n) space */
public static int maxProfitWithCooldownTable(int[] prices) {
    if (prices == null || prices.length == 0) {
        return 0;
    }
    final int BUY = 0, HOLDING = 1, COOLDOWN = 2;
    int n = prices.length;
    int[][] dp = new int[n][3];

    dp[0][BUY] = 0;
    dp[0][HOLDING] = -prices[0];
    dp[0][COOLDOWN] = 0;

    for (int i = 1; i < n; i++) {
        int p = prices[i];
        dp[i][BUY] = Math.max(dp[i - 1][BUY], dp[i - 1][COOLDOWN]);
        dp[i][HOLDING] = Math.max(dp[i - 1][HOLDING], dp[i - 1][BUY] - p);
        dp[i][COOLDOWN] = dp[i - 1][HOLDING] + p;
    }
    return Math.max(dp[n - 1][BUY], dp[n - 1][COOLDOWN]);
}
```

**2) Same math, O(1) space — rolling**

```java
/** Cooldown — rolling: buy / holding / cooldown (LeetCode 309), O(n) time / O(1) space */
public static int maxProfitWithCooldown(int[] prices) {
    if (prices == null || prices.length == 0) {
        return 0;
    }

    int buy = 0;
    int holding = -prices[0];
    int cooldown = 0;

    for (int i = 1; i < prices.length; i++) {
        int price = prices[i];

        int nextBuy = Math.max(buy, cooldown);
        int nextHolding = Math.max(holding, buy - price);
        int nextCooldown = holding + price;

        buy = nextBuy;
        holding = nextHolding;
        cooldown = nextCooldown;
    }
    return Math.max(buy, cooldown);
}
```

**`BUY` / `HOLDING` / `COOLDOWN`** in the table are the same three slots as **`buy` / `holding` / `cooldown`** here.

---

### Transaction fee — unlimited trades, fee on each sell

**Rule:** Unlimited buys/sells (one share at a time). Each **sell** pays a fixed **`fee`** (LeetCode **714**; fee comes out of sale proceeds).

**Idea:** Like variant **II**, but two scalars — **camelCase state nouns**; use **`cashBeforeSell`** when the buy transition must see **`cash`** before the sell transition updates it:

| Variable | Meaning |
|----------|---------|
| **`cash`** | Best profit with **zero** shares (fully liquid; you may buy). |
| **`hold`** | Best profit while **holding exactly one** share (buy cost is folded into this balance). |

Each day at **`price`**, set **`cashBeforeSell = cash`**, then:

1. **Sell (optional):** **`cash = max(cash, hold + price − fee)`** — stay flat, or sell and pay **`fee`**.
2. **Buy (optional):** **`hold = max(hold, cashBeforeSell − price)`** — keep the position, or buy using **`cash`** from **before** step 1 (same snapshot pattern as rolling **`buy` / `holding` / `cooldown`**).

Seed **`hold = cash − prices[0]`** with **`cash = 0`**. Loop **`i = 1 … n−1`**. Return **`cash`** (best answer ends with no open long).

**Example:** `prices = [1, 3, 2, 8, 4, 9]`, `fee = 2` → **8** (LeetCode sample).

```java
/** Transaction fee (LeetCode 714) — O(n) time, O(1) space */
public static int maxProfitWithFee(int[] prices, int fee) {
    if (prices == null || prices.length == 0) {
        return 0;
    }

    int cash = 0;
    int hold = cash - prices[0];

    for (int i = 1; i < prices.length; i++) {
        int price = prices[i];
        int cashBeforeSell = cash;
        cash = Math.max(cash, hold + price - fee);
        hold = Math.max(hold, cashBeforeSell - price);
    }
    return cash;
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
