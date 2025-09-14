# Next Permutation

**Difficulty:** Medium-Hard 🔥

---

## 🔹 Problem Statement
Given an array of integers, rearrange them to the **lexicographically next greater permutation** of numbers.  
If such arrangement is not possible, rearrange it as the **lowest possible order** (sorted ascending).

---

## 🔹 Logic & Intuition
- Find the **longest non-increasing suffix** from the end of the array.
- Identify the **pivot** just before the suffix.
- Swap the pivot with the **smallest element in the suffix larger than the pivot**.
- Reverse the suffix to get the **next permutation**.

---

## 🔹 Approaches

### 1. Brute Force (All Permutations)
- Generate all permutations of the array.
- Sort the permutations lexicographically.
- Return the **next permutation** after the current array (wrap to first if at last permutation).
- **Time Complexity:** O(n!)
- **Space Complexity:** O(n!)

### 2. Optimal (In-place)
- Apply the **pivot-suffix logic** above.
- Rearrange in **O(n) time** and **O(1) space**.

---

## 🔹 Java Code

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NextPermutation {

    // Brute force using all permutations
    public static int[] nextPermutationBrute(int[] nums) {
        List<int[]> permutations = new ArrayList<>();
        generatePermutations(nums, 0, permutations);

        Arrays.sort(permutations.toArray(new int[0][0]), (a, b) -> {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) return a[i] - b[i];
            }
            return 0;
        });

        for (int i = 0; i < permutations.size(); i++) {
            if (Arrays.equals(permutations.get(i), nums)) {
                return permutations.get((i + 1) % permutations.size()); // next permutation
            }
        }
        return nums;
    }

    private static void generatePermutations(int[] nums, int l, List<int[]> res) {
        if (l == nums.length) {
            res.add(nums.clone());
            return;
        }
        for (int i = l; i < nums.length; i++) {
            swap(nums, l, i);
            generatePermutations(nums, l + 1, res);
            swap(nums, l, i); // backtrack
        }
    }

    // Optimal in-place solution
    public static void nextPermutationOptimal(int[] nums) {
        int i = nums.length - 2;

        while (i >= 0 && nums[i] >= nums[i + 1]) i--;

        if (i >= 0) {
            int j = nums.length - 1;
            while (nums[j] <= nums[i]) j--;
            swap(nums, i, j);
        }

        reverse(nums, i + 1, nums.length - 1);
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    private static void reverse(int[] nums, int start, int end) {
        while (start < end) swap(nums, start++, end--);
    }
}
```

---

## 🔹 Complexity Analysis

| Approach         | Time Complexity | Space Complexity |
|------------------|-----------------|------------------|
| Brute Force      | O(n!)           | O(n!)            |
| Optimal In-place | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Array is already in **descending order** → result should be sorted ascending.
- Array contains **all identical elements** → no change.
- Array contains only **one element** → no change.

---

## 🔹 Follow-Up Questions
1. How would you modify the algorithm to find the **k-th next permutation** efficiently?
2. Can this approach be extended to work on **strings** or other comparable objects?
3. How to generate the **previous permutation** instead of next?

---
