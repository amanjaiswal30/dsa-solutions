# Missing Number in an Array

**Difficulty:** Easy ✅

---

## 🔹 Problem Statement
Given an array containing `n` distinct numbers taken from the range `0` to `n`, find the single missing number.

### Examples
**Example 1:**  
Input: `nums = [3, 0, 1]`  
Output: `2`

**Example 2:**  
Input: `nums = [0, 1]`  
Output: `2`

**Example 3:**  
Input: `nums = [9,6,4,2,3,5,7,0,1]`  
Output: `8`

### Constraints
- `n == nums.length`
- `1 <= n <= 10^4`
- `0 <= nums[i] <= n`
- All numbers in `nums` are **distinct**

---

## 🔹 Logic & Intuition
- The array contains numbers from `0` to `n` with exactly **one missing number**.
- The sum of numbers from `0` to `n` is:

`totalSum = n * (n + 1) / 2`

- The missing number can be found by subtracting the sum of elements in the array from `totalSum`:

`missing = totalSum - sum(nums)`

---

## 🔹 Approaches

### 1. Brute Force (Using Extra Space)
- Create a boolean array `visited` of size `n+1`.
- Mark indices corresponding to elements in the input array.
- The index not marked is the missing number.

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

### 2. Optimized (Using Sum Formula)
- Calculate `totalSum` from 0 to n.
- Calculate sum of array elements.
- `missing = totalSum - arrSum`

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

### 3. Optimal (Using XOR)
- XOR all numbers from 0 to n and all elements in the array.
- XOR of these two results gives the missing number.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
public class MissingNumber {

    // Brute force using boolean array
    public static int missingNumberBrute(int[] nums) {
        int n = nums.length;
        boolean[] visited = new boolean[n + 1];
        for (int num : nums) visited[num] = true;
        for (int i = 0; i <= n; i++) {
            if (!visited[i]) return i;
        }
        return -1; // should not happen
    }

    // Using sum formula
    public static int missingNumberSum(int[] nums) {
        int n = nums.length;
        int totalSum = n * (n + 1) / 2;
        int arrSum = 0;
        for (int num : nums) arrSum += num;
        return totalSum - arrSum;
    }

    // Using XOR
    public static int missingNumberXOR(int[] nums) {
        int xor = 0, n = nums.length;
        for (int i = 0; i < n; i++) {
            xor ^= i ^ nums[i];
        }
        xor ^= n;
        return xor;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach    | Time Complexity | Space Complexity |
|-------------|-----------------|------------------|
| Brute Force | O(n)            | O(n)             |
| Sum Formula | O(n)            | O(1)             |
| XOR Method  | O(n)            | O(1)             |

---

## 🔹 Edge Cases
1. nums = [0] → Missing = 1
2. nums = [1] → Missing = 0
3. Large array up to 10^5 elements

---

## 🔹 Interviewer Follow-ups
- What if **two numbers** are missing?  
  → Use **expected sum & expected square sum** to solve.
- What if **duplicates exist**?  
  → Problem changes to **"Find missing + duplicate numbers"**.

---
