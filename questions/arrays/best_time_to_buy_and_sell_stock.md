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
**A:** Keep two bests—**holding** vs **flat cash**—and subtract **`fee` on each sell** (LeetCode assumes fee on sell); see **Transaction fee** under Variations below.

### 4. With Cooldown
**Q:** What if you must wait 1 day after selling before buying again?  
**A:** Model three profits per day—**holding** stock, **just sold** (next day is frozen for buys), **resting** (flat, allowed to buy)—and sweep with `Math.max` only; see **Cooldown** under Variations below.

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
| **Cooldown** | Unlimited trades + **1-day buy freeze** after each sell | Three-state DP: `hold`, `sold`, `rest` |
| **Transaction fee** | Unlimited trades + **`fee` per sell** | Two states: `hold`, `cash` (flat) |

**Same folding style:** Variants **I–IV** use `Math.min` / `Math.max` as above. **Cooldown** uses three scalars—buy only from **rest**, sell only from **hold**, **rest** absorbs yesterday’s **sold**. **Transaction fee** uses two scalars—buy from **`cash`**, sell adds **`price - fee`** into **`cash`**; snapshot **`prevCash`** before updating **`cash`** so **`hold`** stays consistent with variant **II** logic plus the fee on exit.

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

**Rule:** As many buy/sell rounds as you want (one share at a time). After any **sell**, you **cannot buy** on the **next** day; later days are fine.

**Idea:** Track best profit in three situations:

| State | Meaning |
|-------|--------|
| `hold` | Own a share after today’s actions |
| `sold` | End today **right after selling** (tomorrow you must not buy from this path) |
| `rest` | No share and **allowed to buy** tomorrow (were flat, or cooldown just ended) |

For each price `p`, save `prevSold`, then:

- `sold = hold + p` — sell today (must have been holding).
- `hold = max(hold, rest - p)` — keep holding, or **buy today** only from `rest` (not from a path that sold yesterday).
- `rest = max(rest, prevSold)` — stay flat, or enter rest coming **from** a sale **yesterday** (cooldown cleared).

Before the loop, seed **`hold`** with the **same rule as inside the loop**: you may only buy from **`rest`**, so after day 0 use **`hold = rest - prices[0]`** (with **`rest == 0`**, that is spending `prices[0]` to hold one share—reads as “pay from flat cash,” not a bare negation). Run the three transitions for **`i = 1 … n-1`**; later **`hold = max(hold, rest - prices[i])`** can replace that first buy with a better entry day. Return **`max(sold, rest)`**.

**Example:** `prices = [1,2,3,0,2]` → **3** (e.g. buy 1 sell 2, skip buy while cooldown, buy 0 sell 2).

```java
/** Best Time to Buy and Sell Stock with Cooldown — O(n) time, O(1) space (LeetCode 309) */
public static int maxProfitWithCooldown(int[] prices) {
    if (prices == null || prices.length == 0) {
        return 0;
    }

    int rest = 0;
    int sold = 0;
    int hold = rest - prices[0]; // same as loop body: buy from rest → equity -= price

    for (int i = 1; i < prices.length; i++) {
        int p = prices[i];
        int prevSold = sold;
        sold = hold + p;
        hold = Math.max(hold, rest - p);
        rest = Math.max(rest, prevSold);
    }
    return Math.max(sold, rest);
}
```

---

### Transaction fee — unlimited trades, fee on each sell

**Rule:** Unlimited buys/sells (one share at a time). Each **sell** pays a fixed **`fee`** (LeetCode **714**; fee comes out of sale proceeds).

**Idea:** Like variant **II**, but only two tracked bests:

| State | Meaning |
|-------|--------|
| `cash` | Max profit when **not** holding (liquid; may buy tomorrow). |
| `hold` | Max profit when **holding** one share (paid `price` when bought). |

Each day at price `p`, save **`prevCash`**, then:

- **`cash = max(cash, hold + p - fee)`** — stay flat, or **sell** today and pay **`fee`**.
- **`hold = max(hold, prevCash - p)`** — keep holding, or **buy** using yesterday’s flat balance (**not** today’s updated `cash`, same pattern as cooldown’s `prevSold`).

Seed **`hold = cash - prices[0]`** with **`cash == 0`** so day 0 matches “buy from flat.” Loop **`i = 1 … n-1`**. Answer **`cash`** (optimal schedule ends flat after a sell; holding alone never beats realizing).

**Example:** `prices = [1, 3, 2, 8, 4, 9]`, `fee = 2` → **8** (LeetCode sample).

```java
/** Best Time to Buy and Sell Stock with Transaction Fee — O(n) time, O(1) space (LeetCode 714) */
public static int maxProfitWithFee(int[] prices, int fee) {
    if (prices == null || prices.length == 0) {
        return 0;
    }

    int cash = 0;
    int hold = cash - prices[0]; // buy on day 0: equity -= prices[0]

    for (int i = 1; i < prices.length; i++) {
        int p = prices[i];
        int prevCash = cash;
        cash = Math.max(cash, hold + p - fee);
        hold = Math.max(hold, prevCash - p);
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
