# Next Permutation

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

### 1. Brute Force
- Generate all permutations of the array and pick the next greater one.
- **Time Complexity:** O(n!) → not feasible for large n.
- **Space Complexity:** O(n!)

---

### 2. Optimal (In-place)
- Apply the pivot-suffix logic above.
- Rearrange in **O(n) time** and **O(1) space**.

---

## 🔹 Java Code

```java
public class NextPermutation {

    public static void nextPermutation(int[] nums) {
        int i = nums.length - 2;

        // Find first decreasing element from right
        while (i >= 0 && nums[i] >= nums[i + 1]) {
            i--;
        }

        if (i >= 0) {
            // Find element just larger than nums[i]
            int j = nums.length - 1;
            while (nums[j] <= nums[i]) {
                j--;
            }
            swap(nums, i, j);
        }

        // Reverse the suffix
        reverse(nums, i + 1, nums.length - 1);
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    private static void reverse(int[] nums, int start, int end) {
        while (start < end) {
            swap(nums, start, end);
            start++;
            end--;
        }
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
