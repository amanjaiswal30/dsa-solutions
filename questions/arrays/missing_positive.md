# First Missing Positive

![](../../assets/images/arrays/missing_positive.svg)


**Difficulty:** Hard 🔥

---

## 🔹 Problem Statement
Given an unsorted integer array, find the **smallest missing positive integer**.

---

## 🔹 Logic & Intuition
- The first missing positive integer must be in the range **[1, n+1]**, where `n` is the length of the array.
- Use the array **indices** to mark the presence of elements.
- Place each number `x` at index `x-1` if possible.
- Then, the first index where `nums[i] != i + 1` is the **missing positive**.

---

## 🔹 Approaches

### 1. Brute Force
- Check for every number starting from 1 if it exists in the array.
- **Time Complexity:** O(n²)
- **Space Complexity:** O(1)

---

### 2. HashSet or Extra Space
- Use extra space to record seen numbers.
- Iterate from 1 to n+1 to find the first missing positive.
- **Time Complexity:** O(n)
- **Space Complexity:** O(n)

---

### 3. Optimal (In-place Hashing — cyclic swaps)
- Use the array itself to mark presence by swapping elements to their correct position.
- After placing numbers in their right positions, iterate to find the first index where `nums[i] != i+1`.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

### 4. Optimal (Three-pass index marking)
- **Pass 1 — bound:** The answer lies in **[1, n + 1]** for `n = nums.length`. Anything **≤ 0** or **> n** cannot be that missing number, so rewrite those cells to **`n + 1`** (a sentinel **strictly larger** than the largest value we still care about, namely **`n`**).
- **Pass 2 — mark:** For each `x = |nums[i]|`, if **`1 ≤ x ≤ n`**, treat **`x`** as “seen” by making **`nums[x - 1]`** negative (use **`Math.abs`** when flipping so duplicates do not flip a mark back to positive).
- **Pass 3 — scan:** The first index **`i`** with **`nums[i] > 0`** means **`i + 1`** was never marked → **first missing positive**. If every slot is negative, return **`n + 1`**.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.HashSet;
import java.util.Set;

public class FirstMissingPositive {

    // -----------------------
    // 1. Brute Force Approach
    // -----------------------
    public static int firstMissingPositiveBrute(int[] nums) {
        int n = nums.length;

        // Check numbers from 1 to n+1
        for (int i = 1; i <= n + 1; i++) {
            boolean found = false;

            // Scan entire array to see if i exists
            for (int num : nums) {
                if (num == i) {
                    found = true;
                    break;
                }
            }

            // i not found → return as missing
            if (!found) return i;
        }

        return n + 1; // default case
    }

    // -----------------------
    // 2. HashSet Approach
    // -----------------------
    public static int firstMissingPositiveHashSet(int[] nums) {
        int n = nums.length;
        Set<Integer> set = new HashSet<>();

        // Add all positive numbers to set
        for (int num : nums) {
            if (num > 0) set.add(num);
        }

        // Find the first missing positive
        for (int i = 1; i <= n + 1; i++) {
            if (!set.contains(i)) return i;
        }

        return n + 1; // default case
    }

    // -----------------------
    // 3. Optimal In-place Hashing
    // -----------------------
    public static int firstMissingPositiveOptimal(int[] nums) {
        int n = nums.length;

        // Place each number at its correct index (1 → index 0, 2 → index 1, etc.)
        for (int i = 0; i < n; i++) {
            while (nums[i] > 0 && nums[i] <= n && nums[nums[i] - 1] != nums[i]) {
                swap(nums, i, nums[i] - 1);
            }
        }

        // The first index where index+1 != number is the missing positive
        for (int i = 0; i < n; i++) {
            if (nums[i] != i + 1) {
                return i + 1;
            }
        }

        // All numbers in place → missing positive is n+1
        return n + 1;
    }

    // -----------------------
    // 4. Three-pass index marking (sign flip)
    // -----------------------
    public static int firstMissingPositiveThreePass(int[] nums) {
        int n = nums.length;

        // Pass 1: irrelevant values → n + 1 (sentinel above largest useful positive n)
        for (int i = 0; i < n; i++) {
            if (nums[i] <= 0 || nums[i] > n) {
                nums[i] = n + 1;
            }
        }

        // Pass 2: mark that x exists by negating nums[x - 1]
        for (int i = 0; i < n; i++) {
            int x = Math.abs(nums[i]);
            if (x >= 1 && x <= n) {
                nums[x - 1] = -Math.abs(nums[x - 1]);
            }
        }

        // Pass 3: first index still positive → missing is i + 1
        for (int i = 0; i < n; i++) {
            if (nums[i] > 0) {
                return i + 1;
            }
        }
        return n + 1;
    }

    // -----------------------
    // Helper Method: Swap
    // -----------------------
    private static void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                       | Time Complexity | Space Complexity |
|--------------------------------|-----------------|------------------|
| Brute Force                    | O(n²)           | O(1)             |
| HashSet / Extra Space          | O(n)            | O(n)             |
| Optimal — cyclic swaps         | O(n)            | O(1)             |
| Optimal — three-pass marking   | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Array contains **all negative numbers** → missing positive is 1.
- Array contains **consecutive positive numbers starting from 1** → missing positive is n+1.
- Array contains **duplicates** → still works with in-place hashing.
- Array contains **single element** → returns 1 if element ≠ 1, otherwise 2.

---

## 🔹 Follow-Up Questions
1. Can this algorithm be extended to find the **first k missing positive numbers**?
2. How would you handle **very large arrays** efficiently?
3. Can this be adapted to work for **negative numbers included** while still finding the first missing positive?

---
