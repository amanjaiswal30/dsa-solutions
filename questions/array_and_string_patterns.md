# Array & string problems — pattern decision tree

Four views: **mindmap** (families at a glance), **flowchart** (decisions → pattern, with examples), **compact LR flow**, **text trees** (split ASCII + numbered + expanded branch). Many problems **combine** patterns (e.g. **window + hash map**). This is a **heuristic**, not a proof.

**See also:** [Binary search loop tips](binary_search_tips.md) · [Tree BFS level tips](tree_bfs_tips.md)

---

## 0 · Quick tips — when to use what

### Sliding window

| Use | Signals | Skip window |
|-----|---------|-------------|
| **Fixed-size window** | "subarray of size `k`", max/min in every window of length `k` | Need non-contiguous elements |
| **Variable window** | "longest/shortest substring/subarray" with a **count constraint** (at most K distinct, sum ≤ X) | Need **all** subarrays enumerated |
| **Window + deque** | Min/max **inside** each window as you slide (not just sum/count) | Constraint is on **sorted pairs**, not a segment |

**Expand / shrink rule:** grow `right` until constraint breaks → record answer → shrink `left` until valid again (variable window).

---

### Two pointers

| Variant | When | Examples |
|---------|------|----------|
| **Opposite ends** (`i`, `j` on sorted array) | Pairs/triplets, palindrome check, container area, merge two sorted | [3Sum](arrays/3-sum.md), [Container with most water](arrays/container_with_most_water.md) |
| **Same direction (slow / fast)** | In-place partition, remove duplicates, move zeros | [Shift zeros](arrays/shift_zeros_to_end.md) |
| **Two sorted arrays/lists** | Merge, intersection, median of two | [Median of two sorted arrays](arrays/median_of_two_sorted_arrays.md) |

**Use two pointers when:** array/string is **sorted** (or you sort once) and you compare/move ends toward a target — **O(n)** not **O(n²)**.

---

### Prefix sum / prefix map

| Use | Signals |
|-----|---------|
| **Prefix sum array** | Many range-sum queries `sum(i..j)` on **static** array |
| **Prefix + HashMap** | Count subarrays with sum = `K`, or "how many prefixes have sum = X" |
| **Running prefix (no array)** | Single pass subarray sum problems |

**Map keys:** often `prefixSum` → count or earliest index. Formula: `prefix[j] - prefix[i] = K` → look for `prefix[i] = prefix[j] - K`.

**Not prefix:** need **longest** substring with **character constraint** → sliding window + map, not prefix alone.

---

### HashMap / HashSet

| Use | Signals |
|-----|---------|
| **HashMap** | Count frequency, store index of last seen, complement `target - x` | [Two sum](arrays/two_sum.md) |
| **HashSet** | Duplicate detection, O(1) membership | Cycle in array, unique chars |
| **`int[26]` / `int[128]`** | Lowercase letters / small fixed alphabet | Prefer over map when bounds are tiny |

---

### Binary search (on array)

| Use | Signals |
|-----|---------|
| **BS on sorted array** | Exact find → `low <= high` | [Binary search tips](binary_search_tips.md) |
| **BS on rotated / boundary** | Min in rotated, first/last position → `low < high` | [Minimum in rotated](arrays/minimum_in_rotated_sorted_array.md) |
| **BS on answer** | "Minimize the maximum", "smallest X such that feasible(X)" | Monotonic feasibility |

---

### Other array/string tools

| Pattern | When |
|---------|------|
| **Monotonic stack** | Next greater/smaller, histogram area, daily temperatures |
| **Sort + greedy** | Intervals, meeting rooms, non-overlapping |
| **Kadane / DP** | Max subarray sum, state depends on **ending at i** not any subarray |
| **Rolling hash** | Many equal-length substring comparisons |
| **Trie** | Prefix queries over a **dictionary** of words |

---

## 1 · Mindmap (overview)

![](../assets/images/patterns/pattern_mindmap.svg)

---

## 2 · Flowchart (decisions → pattern)

Read top → bottom. **Diamond** = question; **rounded** = start; **rectangle** = pattern to use.

**Compact view** (two main branches):

![](../assets/images/patterns/pattern_overview.svg)

**Split decision tree:**

![](../assets/images/patterns/pattern_split_tree.svg)

**Contiguous branch** (sliding window family):

![](../assets/images/patterns/pattern_contiguous_branch.svg)

**Non-contiguous branch** (full array scan):

![](../assets/images/patterns/pattern_non_contiguous_branch.svg)

---

## 3 · Text tree (same logic as flowchart)

### Split diagram (two branches)

```
                    ┌─────────────────────────┐
                    │  Array / String problem │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼───────────────┐
                    │ Contiguous subarray or    │
                    │ substring?                │
                    └───────────┬───────────────┘
              ┌─────────────────┴─────────────────┐
             YES                                   NO
              │                                     │
   ┌──────────▼──────────┐            ┌───────────▼───────────┐
   │  SLIDING WINDOW     │            │  NON-CONTIGUOUS PATH    │
   │  (segment on array) │            │  (scan full problem)   │
   └──────────┬──────────┘            └───────────┬───────────┘
              │                                   │
   ┌──────────▼──────────┐            ┌───────────▼───────────┐
   │ Fixed length k?     │            │ Range sum / sum = K?  │
   └──┬──────────────┬───┘            └──┬────────────────┬───┘
     yes            no                  yes               no
      │              │                   │                 │
      ▼              ▼                   ▼                 ▼
 ┌─────────┐  ┌─────────────┐    ┌─────────────┐   ┌──────────────┐
 │ FIXED   │  │ Longest /   │    │ PREFIX SUM  │   │ Sorted or    │
 │ WINDOW  │  │ shortest +  │    │ + HashMap   │   │ sort helps?  │
 │         │  │ constraint? │    └─────────────┘   └──┬───────┬───┘
 └─────────┘  └──┬──────┬───┘                         yes      no
                 yes    no                            │        │
                  │      │                    ┌──────▼──┐  ┌──▼───────────┐
                  ▼      ▼                    │ One     │  │ O(1) lookup? │
           ┌──────────┐ ┌──────────┐         │ target? │  └──┬───────┬───┘
           │ VARIABLE │ │ MONOTONIC│         └──┬───┬──┘    yes      no
           │ WINDOW   │ │ DEQUE    │           y   n         │        │
           │ + map    │ │ min/max  │            │   │         ▼        ▼
           └──────────┘ └──────────┘            ▼   ▼    ┌───────┐ ┌──────────┐
                                               BS  2PTR   │ HASH  │ │ Next gr? │
                                               on  3Sum   │ MAP   │ └──┬───┬───┘
                                               arr pairs  └───────┘   y   n
                                                                      │   │
                                                                      ▼   ▼
                                                                   STACK  BS on answer
                                                                            │
                                                                            ▼
                                                                    … greedy · DP · trie
```

### Full decision tree (numbered)

```
Array / string
│
├─ [1] Contiguous subarray or substring?
│   ├─ YES
│   │   ├─ [1a] Fixed length k?              → FIXED SLIDING WINDOW
│   │   ├─ [1b] Longest/shortest + constraint → VARIABLE WINDOW + HashMap / int[26]
│   │   └─ [1c] Min/max inside each window    → MONOTONIC DEQUE
│   │
│   └─ NO  (answer is NOT necessarily a sliding segment)
│       ├─ [2] Range sums / subarray sum = K? → PREFIX SUM (+ HashMap)
│       ├─ [3] Sorted or sort unlocks pairs?
│       │   ├─ single target / boundary       → BINARY SEARCH
│       │   └─ pairs, triplets, intervals     → SORT + TWO POINTERS
│       ├─ [4] O(1) by value / complement?    → HashMap / HashSet
│       ├─ [5] Next greater/smaller / bars?   → MONOTONIC STACK
│       ├─ [6] Minimize max, feasible(mid)?   → BINARY SEARCH ON ANSWER
│       ├─ [7] Intervals / deadlines?         → SORT + GREEDY
│       ├─ [8] Optimal on two strings?        → DP
│       └─ [9] Else
│           ├─ many substring compares        → ROLLING HASH
│           ├─ dictionary / prefix words      → TRIE
│           └─ otherwise                      → re-read constraints
```

### Non-contiguous branch (expanded)

```
[1] NO — not a pure sliding segment
 │
 ├─ [2] Range sums / subarray sum = K? ──────────────► PREFIX SUM + HashMap
 │
 ├─ [3] Sorted or sort helps?
 │      ├─ one target / boundary ────────────────────► BINARY SEARCH
 │      └─ pairs · triplets · intervals ─────────────► SORT + TWO POINTERS
 │
 ├─ [4] O(1) lookup / complement? ───────────────────► HashMap / HashSet
 │
 ├─ [5] Next greater / smaller / histogram? ─────────► MONOTONIC STACK
 │
 ├─ [6] Minimize max, feasible(mid) monotone? ───────► BINARY SEARCH ON ANSWER
 │
 ├─ [7] Intervals / deadlines / overlap? ────────────► SORT + GREEDY
 │
 ├─ [8] Optimal on two strings? ─────────────────────► DP
 │
 └─ [9] String-specific?
        ├─ many equal-length substring compares ─────► ROLLING HASH
        ├─ dictionary / prefix word queries ─────────► TRIE
        └─ none of the above ────────────────────────► re-read constraints
```

---

## Common combos

| Combo | When |
|--------|------|
| **Sliding window + HashMap** | Longest substring with at most K distinct, min window substring |
| **Prefix + HashMap** | Subarray sum equals K; count subarrays with sum |
| **Sort + greedy + (heap)** | Meeting rooms II, task scheduler style |
| **Two pointers + hash** | Dedupe or map while moving ends |

---

## Pitfalls

- Nested “every subarray” brute → **one** scan with **window** or **prefix + map**.
- Re-sorting every step → **sort once** or heap.
- Only `a`–`z` → prefer **`int[26]`** over `HashMap` when counts fit.

Re-check **`n`**, **value bounds**, **sorted?**, **mod input allowed?** before finalizing.
