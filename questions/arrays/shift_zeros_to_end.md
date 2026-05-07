# Shift All Zeros to the End

![](../../assets/images/arrays/shift_zeros_to_end.svg)


**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
Given an integer array `nums`, move all `0`s to the end of the array **in-place** while maintaining the **relative order** of the non-zero elements.

---

## 🔹 Examples

**Example 1:**  
Input: `nums = [0,1,0,3,12]`  
Output: `[1,3,12,0,0]`  
Explanation: Non-zero values stay in order `1, 3, 12`; zeros are shifted right.

**Example 2:**  
Input: `nums = [0]`  
Output: `[0]`

**Example 3:**  
Input: `nums = [1,2,3]`  
Output: `[1,2,3]`  
Explanation: No zeros; array unchanged.

---

## 🔹 Constraints
- `1 <= nums.length <= 10^4`
- `-2^31 <= nums[i] <= 2^31 - 1` (typically `-10^4 <= nums[i] <= 10^4` in interview variants)

---

## 🔹 Intuition & Logic
- You need a **stable partition**: all non-zero elements first (in original order), then all zeros.
- A **write pointer** tracks where the next non-zero should go.
- Either **swap** non-zeros into place as you scan, or **compact** non-zeros left then **fill** the suffix with zeros.

---

## 🔹 Approaches

### 1. Extra Array
- Copy non-zeros to a new array in order, append zeros, copy back.
- **Time:** O(n)  
- **Space:** O(n)

---

### 2. Two Pass — Compact + Fill
- First pass: overwrite from the left with non-zero values (or collect indices).
- Second pass: set remaining positions to `0`.
- **Time:** O(n)  
- **Space:** O(1)

---

### 3. One Pass — Swap (Optimal)
- Maintain `k` = index for the next non-zero.
- For each `i`, if `nums[i] != 0`, swap `nums[k]` and `nums[i]`, then `k++`.
- **Time:** O(n)  
- **Space:** O(1)

---

## 🔹 Java Code

```java
public class ShiftZerosToEnd {

    /** Extra array — O(n) space */
    public static void moveZeroesExtraArray(int[] nums) {
        int n = nums.length;
        int[] tmp = new int[n];
        int j = 0;
        for (int v : nums) {
            if (v != 0) tmp[j++] = v;
        }
        System.arraycopy(tmp, 0, nums, 0, n);
    }

    /** Two passes — O(1) space */
    public static void moveZeroesCompact(int[] nums) {
        int k = 0;
        for (int v : nums) {
            if (v != 0) nums[k++] = v;
        }
        while (k < nums.length) nums[k++] = 0;
    }

    /** One pass with swaps — O(1) space */
    public static void moveZeroesSwap(int[] nums) {
        int k = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                swap(nums, k++, i);
            }
        }
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach        | Time Complexity | Space Complexity |
|-----------------|-----------------|------------------|
| Extra Array     | O(n)            | O(n)             |
| Compact + Fill  | O(n)            | O(1)             |
| Swap (one pass) | O(n)            | O(1)             |

---

## 🔹 Edge Cases
1. All zeros → output unchanged order (all zeros).
2. No zeros → array unchanged.
3. Single element.
4. Many zeros interleaved with non-zeros.

---

## 🔹 Follow-Up Questions
1. **What if you must shift all `0`s to the front instead?**  
   → Scan from the right or run the same logic with reversed roles.
2. **What if the array contains only `0` and `1`?**  
   → Related to **sort colors / Dutch flag** with two partitions.
3. **Move zeros to end but also return the count of zeros?**  
   → `n - k` after compact, where `k` is the number of non-zeros.
