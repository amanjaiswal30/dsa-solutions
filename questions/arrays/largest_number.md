# Largest Number Formed from an Array

**Difficulty:** Medium-Hard 🔥

---

## 🔹 Problem Statement
Given a list of non-negative integers, arrange them such that they form the **largest possible number**.

---

## 🔹 Logic & Intuition
- To form the largest number, compare numbers based on their **concatenation order** rather than numeric value.
- For example, compare `X + Y` and `Y + X` (strings concatenated) — whichever is **lexicographically larger** should come first.
- Sort the array based on this **custom comparator** and join the numbers.

---

## 🔹 Approaches

### 1. Brute Force (All Permutations)
- Generate all possible **permutations** of the array.
- Convert each permutation to a concatenated number (as string).
- Track the **largest number** among all permutations.
- **Time Complexity:** O(n!) — impractical for large arrays.
- **Space Complexity:** O(n!) for storing permutations.

### 2. Custom Sort (Optimal)
- Convert numbers to strings.
- Sort strings using a **custom comparator** based on concatenation.
- Join the sorted strings to form the final largest number.
- **Time Complexity:** O(n log n × k), where k = max number of digits.
- **Space Complexity:** O(n)

---

## 🔹 Java Code

```java
import java.util.Arrays;
import java.util.Comparator;

public class LargestNumber {

    // Brute force using all permutations
    public static String largestNumberBrute(int[] nums) {
        return permuteAndFindLargest(nums, 0, nums.length - 1, "");
    }

    private static String permuteAndFindLargest(int[] nums, int l, int r, String max) {
        if (l == r) {
            StringBuilder sb = new StringBuilder();
            for (int num : nums) sb.append(num);
            String current = sb.toString();
            if (max.equals("") || current.compareTo(max) > 0) {
                max = current;
            }
            return max;
        }
        for (int i = l; i <= r; i++) {
            swap(nums, l, i);
            max = permuteAndFindLargest(nums, l + 1, r, max);
            swap(nums, l, i); // backtrack
        }
        return max;
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    // Optimal solution using custom sort
    public static String largestNumberOptimal(int[] nums) {
        String[] strNums = new String[nums.length];
        for (int i = 0; i < nums.length; i++) strNums[i] = String.valueOf(nums[i]);

        Arrays.sort(strNums, (a, b) -> (b + a).compareTo(a + b));

        if (strNums[0].equals("0")) return "0";

        StringBuilder sb = new StringBuilder();
        for (String s : strNums) sb.append(s);

        return sb.toString();
    }
}
```

---

## 🔹 Complexity Analysis

| Approach    | Time Complexity | Space Complexity |
|-------------|-----------------|------------------|
| Brute Force | O(n!)           | O(n!)            |
| Custom Sort | O(n log n × k)  | O(n)             |
*where k is the maximum number of digits in any number*

---

## 🔹 Edge Cases
- All elements are `0` → should return `"0"`.
- Array contains single element → return that element.
- Numbers with different lengths (e.g., `[3, 30, 34, 5, 9]`).

---

## 🔹 Follow-Up Questions
1. Can you solve this without converting numbers to strings?
2. How would you handle extremely large arrays or numbers with hundreds of digits?
3. What if negative numbers were allowed? How would the comparator change?

---
