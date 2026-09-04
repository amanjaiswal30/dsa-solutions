# Russian Doll Envelopes

![](../../assets/images/dynamic_programming/longest_increasing_subsequence.svg)


**Difficulty:** Hard 🔥

---

## 🔹 Problem Statement

You are given a 2D array of integers `envelopes` where `envelopes[i] = [w_i, h_i]` represents the width and height of an envelope.

One envelope can be placed inside another if and only if **both** the width and height of one envelope are **strictly greater** than the width and height of the other.

Return the **maximum number of envelopes** you can Russian doll (i.e., nest one inside another).

---

## 🔹 Examples

**Example 1:**  
Input: `envelopes = [[5,4],[6,4],[6,7],[2,3]]`  
Output: `3`  
Explanation: Nest `[2,3]` → `[5,4]` → `[6,7]` (order by increasing width and height).

**Example 2:**  
Input: `envelopes = [[1,1],[1,1],[1,1]]`  
Output: `1`  
Explanation: Equal dimensions cannot nest strictly.

**Example 3:**  
Input: `envelopes = [[4,5],[4,6],[6,7],[2,3],[1,1]]`  
Output: `4`  
Explanation: One chain is `[1,1]` → `[2,3]` → `[4,5]` → `[6,7]`.

---

## 🔹 Constraints
- `1 <= envelopes.length <= 10^5`
- `envelopes[i].length == 2`
- `1 <= w_i, h_i <= 10^5`

---

## 🔹 Intuition & Logic

Naively checking all pairs is O(n²). The key reduction is **2D longest increasing subsequence (LIS)**:

1. **Sort** envelopes by **width ascending**. For equal widths, sort **height descending** (so only one envelope per width can appear in a valid chain).
2. After sorting, finding the longest nesting chain equals **LIS on the height array**.
3. Use **patience sorting + lower_bound** (MAZHARMIK LIS Approach-4) on heights — O(n log n).

**Why height descending for equal width?**  
If two envelopes share width `w`, you cannot nest one inside the other (width must be strictly greater). Descending heights prevent both from contributing to the same increasing height subsequence.

---

## 🔹 Approaches

### 1. Brute-Force DP on Pairs — O(n²)
- Sort by **width ascending only** — no tie-break needed.
- Classic LIS-style DP: `dp[i] = 1 + max(dp[j])` for every `j < i` where `envelopes[i][0] != envelopes[j][0]` **and** `envelopes[i][1] > envelopes[j][1]`.
- The explicit `width != width` check does the job that the height-descending tie-break does in Approach 2 — either works, this one just checks it directly per pair instead of baking it into the sort order.

**Time:** O(n²)  
**Space:** O(n)

---

### 2. Binary Search on Smallest Ending Heights (Patience Sorting) — O(n log n) ⭐
- Sort width **ascending**, height **descending** on ties (so two envelopes with the same width can never both extend the same height chain).
- Maintain a fixed-size array `smallestEndingHeight`, where `smallestEndingHeight[k]` = the smallest tail height of any valid chain of length `k + 1` — same idea as [MAZHARMIK LIS Approach-4](https://github.com/MAZHARMIK/Interview_DS_Algo/blob/master/DP/LIS%20%26%20Variants/Longest%20Increasing%20Subsequence.cpp), implemented with a plain array instead of a list.
- For each envelope's height, binary search (inline, no separate helper) for the **first index** where `smallestEndingHeight[index] >= currentHeight`.
  - If that index equals `bestLisSize` (past every filled slot) → it beats every tail, so **extend**: write it in and grow `bestLisSize`.
  - Otherwise → **replace** the tail at that index with the smaller height.

**Time:** O(n log n)  
**Space:** O(n)

#### Why each step?

| Step | What we do | Why |
|------|------------|-----|
| **1. Sort width ↑, height ↓** | `Arrays.sort` with custom comparator | Width must strictly increase in any valid chain, so we process envelopes left-to-right by width. Equal widths cannot nest, so we sort heights **descending** — only the first (tallest) of each width group can extend an increasing height subsequence. |
| **2. Reduce to LIS on heights** | After sort, scan only `envelopes[i][1]` | Width order is already fixed; nesting now depends only on strictly increasing heights. This turns a 2D problem into classic LIS. |
| **3. Maintain `smallestEndingHeight`** | `smallestEndingHeight[k]` = smallest ending height of any chain of length `k + 1` | Smaller tails leave more room for future envelopes. We never need to store every chain — only the best tail per length. |
| **4. Binary search each height** | `left`/`right`/`mid` loop for first index where `smallestEndingHeight[mid] >= currentHeight` | Finds the **shortest** chain that can accept `currentHeight` (replace tail) or signals that it starts a new longest chain (extend). Using `>=` (not `>`) prevents equal heights from stacking in the same chain. |
| **5. Replace or extend** | `smallestEndingHeight[left] = currentHeight`, then grow `bestLisSize` only if `left == bestLisSize` | Replace when a chain of that length can end with a **smaller** height (better for future envelopes). Extend when `currentHeight` is larger than every tail — the maximum chain length grows by one. |
| **6. Return `bestLisSize`** | Count of filled slots | Each index represents one chain length; final count equals the longest strictly increasing height subsequence = max nesting count. |

**Walkthrough** on `[[5,4],[6,4],[6,7],[2,3]]` after sort → `[[2,3],[5,4],[6,7],[6,4]]`:

| Height | `smallestEndingHeight` after | Action | Why |
|--------|-------------------------------|--------|-----|
| 3 | `[3]` | extend | first envelope |
| 4 | `[3, 4]` | extend | 4 > 3, new chain length |
| 7 | `[3, 4, 7]` | extend | 7 > 4, extend again |
| 4 | `[3, 4, 7]` | replace index 1 (`4 → 4`) | same height; binary search finds index 1, replace keeps tail minimal (no length change) |

Answer: `bestLisSize = 3`.

---

## 🔹 Java Code

```java
import java.util.Arrays;

public class RussianDollEnvelopes {

    /**
     * Brute-force O(n²) DP. Sort by width ascending only — the pairwise check
     * (`width` differs AND `height` is greater) enforces strict nesting directly,
     * so no height tie-break is needed at sort time.
     */
    public int maxEnvelopesDp(int[][] envelopes) {
        int n = envelopes.length;
        Arrays.sort(envelopes, (a, b) -> a[0] - b[0]);

        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        int result = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (envelopes[i][0] != envelopes[j][0] && envelopes[i][1] > envelopes[j][1]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            result = Math.max(result, dp[i]);
        }
        return result;
    }

    /**
     * Optimized O(n log n). Sort width ascending, height descending on ties
     * (so equal-width envelopes can't both extend the same height chain), then
     * run patience sorting / binary search directly on a fixed-size array —
     * `smallestEndingHeight[k]` is the smallest tail height of any chain of length k + 1.
     */
    public int maxEnvelopes(int[][] envelopes) {
        int n = envelopes.length;
        Arrays.sort(envelopes, (a, b) -> a[0] == b[0] ? b[1] - a[1] : a[0] - b[0]);

        int[] smallestEndingHeight = new int[n];
        int bestLisSize = 0;

        for (int i = 0; i < n; i++) {
            int currentHeight = envelopes[i][1];
            int left = 0;
            int right = bestLisSize;

            while (left < right) {
                int mid = left + (right - left) / 2;
                if (smallestEndingHeight[mid] < currentHeight) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }

            smallestEndingHeight[left] = currentHeight; // extend or replace tail for this chain length
            if (left == bestLisSize) {
                bestLisSize++; // currentHeight beat every existing tail → chain grows by 1
            }
        }
        return bestLisSize;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time | Space |
|----------|------|-------|
| Brute-force DP on pairs | O(n²) | O(n) |
| Binary search / patience sorting | O(n log n) | O(n) |

Sorting costs O(n log n) in both cases.

---

## 🔹 Edge Cases
1. **All same size** — answer `1`.
2. **Same width, different heights** — only one per width in a chain (brute force checks `width != width` explicitly per pair; the optimized approach handles it via descending height sort).
3. **Already sorted by width** — optimized approach still needs the height-desc tie-break; brute force doesn't depend on sort order for correctness at all.
4. **Single envelope** — return `1`.

---

## 🔹 Follow-Up Questions
1. **Print the actual chain of envelopes** — track parents during LIS DP, or reconstruct from `smallestEndingHeight` indices.
2. **3D boxes (width, height, depth)** — sort by two keys, run LIS on the third (extension of this pattern).
3. **Maximum envelopes with non-strict inequality** — problem definition changes; sorting tie-break rules change too.

---

## 🔗 Related

- [Longest Increasing Subsequence](longest_increasing_subsequence.md) — core pattern; this problem is **Variation 5** there.
- [Minimum Arrows to Burst Balloons](../arrays/minimum_arrows_to_burst_balloons.md) — another interval / greedy sorting problem.
