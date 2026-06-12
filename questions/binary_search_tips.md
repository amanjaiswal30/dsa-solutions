# Binary search — loop condition tips

Quick reference for **`while (low < high)`** vs **`while (low <= high)`**. Pick one template per problem and keep `mid`, `low`, and `high` updates consistent with it.

![](../assets/images/patterns/binary_search_decision.svg)

---

## The two templates

| Template | Loop | On match / shrink | Returns |
|----------|------|-------------------|---------|
| **Half-open interval** | `while (low < high)` | Often `low = mid + 1` or `high = mid` (not `mid - 1`) | `low` (or `high` when they meet) |
| **Closed interval** | `while (low <= high)` | `low = mid + 1` or `high = mid - 1` | `low` if found, else `-1` |

**Invariant (half-open):** answer is always in `[low, high)` or `[low, high]` with **one** index left when loop ends.

**Invariant (closed):** answer is in `[low, high]`; loop stops when interval is empty.

---

## When to use `low < high`

Use when the answer is a **boundary** (first / last / min / max) or you **binary search on the answer** (feasibility).

| Signal in problem | Update pattern | Example |
|-------------------|----------------|---------|
| Find **minimum** that works (first feasible) | `if (feasible(mid)) high = mid` else `low = mid + 1` | Min capacity to ship, Koko eating bananas |
| Find **maximum** that works (last feasible) | `if (feasible(mid)) low = mid` else `high = mid - 1` | Max minimum distance, split array largest sum |
| Min in **rotated sorted** array | `if (nums[mid] > nums[right]) low = mid + 1` else `high = mid` | [Minimum in rotated sorted array](arrays/minimum_in_rotated_sorted_array.md) |
| **First** index where condition holds | `if (condition(mid)) high = mid` else `low = mid + 1` | First bad version, lower bound |
| **Last** index where condition holds | `if (condition(mid)) low = mid` else `high = mid - 1` | Upper bound (still often `low < high` with care) |

**Why not `<=` here:** you often keep `mid` as a candidate (`high = mid`), so `low` and `high` can become equal with that index still valid — `low <= high` would loop forever unless you exit early.

```java
// Template: first position where condition is true (lower bound)
int low = 0, high = n; // sometimes high = n for "insert position"
while (low < high) {
    int mid = low + (high - low) / 2;
    if (condition(mid)) high = mid;
    else low = mid + 1;
}
return low;
```

---

## When to use `low <= high`

Use for **exact search** in a sorted array: target exists or not, return index or `-1`.

| Signal | Update pattern | Example |
|--------|----------------|---------|
| Find **exact** value in sorted array | `if (nums[mid] == target) return mid`; else shrink both sides | Classic binary search |
| Search in sorted matrix (row/col) | Same closed-interval logic on index | [Search a 2D matrix](arrays/search_in_2d_matrix.md) |

```java
int low = 0, high = n - 1;
while (low <= high) {
    int mid = low + (high - low) / 2;
    if (nums[mid] == target) return mid;
    else if (nums[mid] < target) low = mid + 1;
    else high = mid - 1;
}
return -1;
```

---

## Decision cheat sheet

See diagram at top. Quick text version:

- **Exact target in sorted data?** → `low <= high`, exclude mid with `mid ± 1`
- **First/last occurrence, boundary, min/max feasible?** → `low < high`, often `high = mid` or `low = mid`
- **BS on answer:** minimize → `high = mid` when feasible; maximize → `low = mid` when feasible

---

## Common mistakes

| Mistake | Fix |
|---------|-----|
| Mix templates (`low < high` but `high = mid - 1` everywhere) | Stick to one invariant per problem |
| `mid = (low + high) / 2` when `low = mid` possible | Use `low + (high - low) / 2` to avoid infinite loop |
| `high = mid - 1` with `low < high` for lower bound | Use `high = mid` for lower bound style |
| Off-by-one on `high = n` vs `n - 1` | Insert position / first true often needs `high = n` |

---

## Related problems in this repo

- [Minimum in rotated sorted array](arrays/minimum_in_rotated_sorted_array.md) — `low < high`, `high = mid`
- [Search in rotated sorted array](arrays/search_in_rotated_sorted_array.md) — boundary / two-phase BS
- [First and last position in sorted array](arrays/first_and_last_position_in_sorted_array.md) — lower / upper bound
- [Peak element](arrays/peak_element.md) — `low < high`
- [Median of two sorted arrays](arrays/median_of_two_sorted_arrays.md) — BS on partition
