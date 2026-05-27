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
3. Use the **O(n log n) LIS** (patience sorting / tails + binary search) on heights.

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

### 2. LIS with Binary Search — O(n log n) ⭐
- Same sort; scan `envelopes[i][1]` directly.
- Maintain `tails[]` (smallest ending height for each subsequence length).
- For each height, **hand-written binary search** on `tails` for the insert/replace index (no `Collections`).

**Time:** O(n log n)  
**Space:** O(n)

---

## 🔹 Java Code

```java
import java.util.Arrays;

public class RussianDollEnvelopes {

    /** O(n^2) — LIS DP; only dp[] extra, heights from sorted envelopes */
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

    /** O(n log n) — tails[] + manual binary search for LIS position */
    public int maxEnvelopes(int[][] envelopes) {
        sortEnvelopes(envelopes);
        int n = envelopes.length;
        int[] tails = new int[n];
        int size = 0;

        for (int[] e : envelopes) {
            int h = e[1];
            int pos = binarySearch(tails, size, h);
            tails[pos] = h;
            if (pos == size) {
                size++;
            }
        }
        return size;
    }

    /** First index in tails[0..size) where tails[i] >= target (insert position) */
    private int binarySearch(int[] tails, int size, int target) {
        int lo = 0, hi = size;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (tails[mid] < target) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

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
| LIS + binary search | O(n log n) | O(n) |

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
