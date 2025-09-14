# First Missing Positive

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

### 3. Optimal (In-place Hashing)
- Use the array itself to mark presence by swapping elements to their correct position.
- After placing numbers in their right positions, iterate to find the first index where `nums[i] != i+1`.
- **Time Complexity:** O(n)
- **Space Complexity:** O(1)

---

## 🔹 Java Code

```java
public class FirstMissingPositive {

    public static int firstMissingPositive(int[] nums) {
        int n = nums.length;

        // Place each number in its right place if possible
        for (int i = 0; i < n; i++) {
            while (nums[i] > 0 && nums[i] <= n && nums[nums[i] - 1] != nums[i]) {
                swap(nums, i, nums[i] - 1);
            }
        }

        // Find first place where index+1 != number
        for (int i = 0; i < n; i++) {
            if (nums[i] != i + 1) {
                return i + 1;
            }
        }

        return n + 1;
    }

    private static void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                 | Time Complexity | Space Complexity |
|--------------------------|-----------------|------------------|
| Brute Force              | O(n²)           | O(1)             |
| HashSet / Extra Space    | O(n)            | O(n)             |
| Optimal In-place Hashing | O(n)            | O(1)             |

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
