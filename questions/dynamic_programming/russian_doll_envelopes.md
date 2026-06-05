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

### 1. DP on Heights — O(n²)
- Sort as above; read heights in-place as `envelopes[i][1]` (no extra height array).
- Classic LIS DP: `dp[i] = 1 + max(dp[j])` for `j < i` and `envelopes[j][1] < envelopes[i][1]`.

**Time:** O(n²)  
**Space:** O(n)

---

### 2. Patience Sorting + lower_bound — O(n log n) ⭐
- Same sort; scan `envelopes[i][1]` directly.
- Maintain `sorted` list (smallest ending height for each chain length) — [MAZHARMIK LIS Approach-4](https://github.com/MAZHARMIK/Interview_DS_Algo/blob/master/DP/LIS%20%26%20Variants/Longest%20Increasing%20Subsequence.cpp).
- For each height, use **lower_bound** (hand-written binary search): first index where `sorted[i] >= height`.
  - If at end → append (extend chain).
  - Else → replace (keep smallest tail for that length).

**Time:** O(n log n)  
**Space:** O(n)

#### Why each step?

| Step | What we do | Why |
|------|------------|-----|
| **1. Sort width ↑, height ↓** | `Arrays.sort` with custom comparator | Width must strictly increase in any valid chain, so we process envelopes left-to-right by width. Equal widths cannot nest, so we sort heights **descending** — only the first (tallest) of each width group can extend an increasing height subsequence. |
| **2. Reduce to LIS on heights** | After sort, scan only `e[1]` | Width order is already fixed; nesting now depends only on strictly increasing heights. This turns a 2D problem into classic LIS. |
| **3. Maintain `sorted` list** | `sorted[i]` = smallest ending height of any chain of length `i + 1` | Smaller tails leave more room for future envelopes. We never need to store every chain — only the best tail per length. |
| **4. `lower_bound` on each height** | Binary search for first index where `sorted[i] >= h` | Finds the **shortest** chain that can accept `h` (replace tail) or signals that `h` starts a new longest chain (append). Using `>=` (not `>`) prevents equal heights from stacking in the same chain. |
| **5. Replace or append** | `set(index, h)` or `add(h)` | Replace when a chain of that length can end with a **smaller** height (better for future envelopes). Append when `h` is larger than every tail — the maximum chain length grows by one. |
| **6. Return `sorted.size()`** | Length of the list | Each index in `sorted` represents one chain length; final size equals the longest strictly increasing height subsequence = max nesting count. |

**Walkthrough** on `[[5,4],[6,4],[6,7],[2,3]]` after sort → `[[2,3],[5,4],[6,7],[6,4]]`:

| Height | `sorted` after | Action | Why |
|--------|----------------|--------|-----|
| 3 | `[3]` | append | first envelope |
| 4 | `[3, 4]` | append | 4 > 3, new chain length |
| 7 | `[3, 4, 7]` | append | 7 > 4, extend again |
| 4 | `[3, 4, 7]` | replace index 1 (`4 → 4`) | same height; `lower_bound` finds index 1, replace keeps tail minimal (no length change) |

Answer: `sorted.size() = 3`.

---

## 🔹 Java Code

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RussianDollEnvelopes {

    /** O(n²) LIS DP on heights after sorting by width ↑, height ↓. */
    public int maxEnvelopesDp(int[][] envelopes) {
        sortEnvelopes(envelopes);
        int n = envelopes.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        int best = 1;

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (envelopes[j][1] < envelopes[i][1]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    /**
     * Returns the maximum number of nested envelopes in O(n log n) time.
     * After sorting, the problem becomes LIS on heights using patience sorting.
     */
    public int maxEnvelopes(int[][] envelopes) {
        sortEnvelopes(envelopes);
        List<Integer> sorted = new ArrayList<>();

        for (int[] e : envelopes) {
            int h = e[1];
            int index = lowerBound(sorted, h);

            if (index == sorted.size()) {
                sorted.add(h);           // h beats every tail → chain grows by 1
            } else {
                sorted.set(index, h);    // swap in a smaller tail for this chain length
            }
        }
        return sorted.size();
    }

    /**
     * First index i where sorted[i] >= target (lower_bound).
     * Equal values replace instead of extend, keeping the subsequence strictly increasing.
     */
    private int lowerBound(List<Integer> sorted, int target) {
        int left = 0;
        int right = sorted.size();
        int result = sorted.size();

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (sorted.get(mid) < target) {
                left = mid + 1;
            } else {
                result = mid;
                right = mid;
            }
        }
        return result;
    }

    /** Width ascending; height descending on ties (same width cannot both appear in a chain). */
    private void sortEnvelopes(int[][] envelopes) {
        Arrays.sort(envelopes, (a, b) -> {
            if (a[0] != b[0]) {
                return Integer.compare(a[0], b[0]);
            }
            return Integer.compare(b[1], a[1]); // height descending
        });
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time | Space |
|----------|------|-------|
| DP LIS on heights | O(n²) | O(n) |
| Patience sorting + lower_bound | O(n log n) | O(n) |

Sorting costs O(n log n) in both cases.

---

## 🔹 Edge Cases
1. **All same size** — answer `1`.
2. **Same width, different heights** — only one per width in a chain (handled by descending height sort).
3. **Already sorted by width** — still need height-desc tie-break.
4. **Single envelope** — return `1`.

---

## 🔹 Follow-Up Questions
1. **Print the actual chain of envelopes** — track parents during LIS DP, or reconstruct from patience-sort indices.
2. **3D boxes (width, height, depth)** — sort by two keys, run LIS on the third (extension of this pattern).
3. **Maximum envelopes with non-strict inequality** — problem definition changes; sorting tie-break rules change too.

---

## 🔗 Related

- [Longest Increasing Subsequence](longest_increasing_subsequence.md) — core pattern; this problem is **Variation 5** there.
- [Minimum Arrows to Burst Balloons](../arrays/minimum_arrows_to_burst_balloons.md) — another interval / greedy sorting problem.
