# Largest Number Formed from an Array

---

## 🔹 Problem Statement
Given a list of non-negative integers, arrange them such that they form the **largest possible number**.

---

## 🔹 Logic & Intuition
- To form the largest number, compare numbers based on their **concatenation order** rather than their numeric value.
- For example, compare `X + Y` and `Y + X` (strings concatenated) — whichever is **lexicographically larger** should come first.
- Sort the array based on this **custom comparator** and join the numbers.

---

## 🔹 Approaches

### 1. Brute Force (All Permutations)
- Generate all possible permutations and find the largest concatenated number.
- **Time Complexity:** O(n!) — impractical for large input.
- **Space Complexity:** O(n!)

---

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

    public static String largestNumber(int[] nums) {
        // Convert to String array
        String[] strNums = new String[nums.length];
        for (int i = 0; i < nums.length; i++) {
            strNums[i] = String.valueOf(nums[i]);
        }

        // Sort using custom comparator
        Arrays.sort(strNums, (a, b) -> {
            String order1 = a + b;
            String order2 = b + a;
            return order2.compareTo(order1); // descending order
        });

        // If highest number is "0", result is "0"
        if (strNums[0].equals("0")) {
            return "0";
        }

        // Build the largest number
        StringBuilder largestNumber = new StringBuilder();
        for (String num : strNums) {
            largestNumber.append(num);
        }

        return largestNumber.toString();
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
