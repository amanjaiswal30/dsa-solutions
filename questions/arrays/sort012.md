# Sort an Array of 0s, 1s, and 2s

---

## 🔹 Problem Statement
Given an array consisting only of `0s`, `1s`, and `2s`, sort the array in linear time without using extra space.

---

## 🔹 Logic & Intuition
- This is the famous **Dutch National Flag problem**.
- Use **three pointers**:
    - `low` → next position for `0`
    - `high` → next position for `2`
    - `mid` → scans the array
- Swap elements in-place so that `0s` go to the front and `2s` go to the end.

---

## 🔹 Approaches

### 1. Brute Force (Sorting)
- Use built-in sorting.
- Time: **O(n log n)**
- Space: **O(1)**

---

### 2. Counting Sort
- Count the number of `0s`, `1s`, and `2s`.
- Overwrite the array with the counted values.
- Time: **O(n)**
- Space: **O(1)**

---

### 3. Dutch National Flag Algorithm (Optimal)
- Maintain three pointers (`low`, `mid`, `high`).
- Swap elements in-place to group values.
- Time: **O(n)**
- Space: **O(1)**

---

## 🔹 Java Code

```java
public class SortColors {

    // Brute force - built-in sort
    public static void sortColorsBrute(int[] nums) {
        java.util.Arrays.sort(nums);
    }

    // Counting sort approach
    public static void sortColorsCounting(int[] nums) {
        int count0 = 0, count1 = 0, count2 = 0;
        for (int num : nums) {
            if (num == 0) count0++;
            else if (num == 1) count1++;
            else count2++;
        }
        int i = 0;
        while (count0-- > 0) nums[i++] = 0;
        while (count1-- > 0) nums[i++] = 1;
        while (count2-- > 0) nums[i++] = 2;
    }

    // Dutch National Flag Algorithm (Optimal)
    public static void sortColorsDutchFlag(int[] nums) {
        int low = 0, mid = 0, high = nums.length - 1;
        while (mid <= high) {
            switch (nums[mid]) {
                case 0:
                    swap(nums, low++, mid++);
                    break;
                case 1:
                    mid++;
                    break;
                case 2:
                    swap(nums, mid, high--);
                    break;
            }
        }
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i]; 
        nums[i] = nums[j]; 
        nums[j] = temp;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                      | Time Complexity | Space Complexity |
|-------------------------------|-----------------|------------------|
| Brute Force (Sorting)         | O(n log n)      | O(1)             |
| Counting Sort                 | O(n)            | O(1)             |
| Dutch National Flag (Optimal) | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Array already sorted (e.g., `[0,0,1,1,2,2]`).
- Array with all same elements (e.g., `[1,1,1,1]`).
- Array with only two numbers (e.g., `[0,2,2,0,0]`).
- Very small arrays (size 1 or 2).

---

## 🔹 Follow-Up Questions
1. Can you solve this problem if the array contained more than three unique values?
    - Hint: A **generalized partitioning approach** or **counting sort** may be needed.

2. Why is the Dutch National Flag algorithm considered optimal?
    - Single-pass solution, in-place, and constant space.

---
