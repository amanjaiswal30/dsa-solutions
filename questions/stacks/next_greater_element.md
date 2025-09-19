# 🔹 Next Greater Element

---

## 📌 Problem Statement
Given an array of integers, for each element, find the next greater element to its right.  
If no such element exists, assign `-1`.

### Example Inputs & Outputs
**Input:**  `[4, 5, 2, 25]`  
**Output:** `[5, 25, 25, -1]`

**Input:**  `[13, 7, 6, 12]`  
**Output:** `[-1, 12, 12, -1]`

---

## 🛠️ Java Solutions

```java
// ✅ Approach 1: Brute Force
public int[] nextGreaterElements(int[] nums) {
    int n = nums.length;
    int[] result = new int[n];

    for (int i = 0; i < n; i++) {
        result[i] = -1;
        for (int j = i + 1; j < n; j++) {
            if (nums[j] > nums[i]) {
                result[i] = nums[j];
                break;
            }
        }
    }

    return result;
}

// ✅ Approach 2: Stack-Based (Efficient)
public int[] nextGreaterElements(int[] nums) {
    int n = nums.length;
    int[] result = new int[n];
    Stack<Integer> stack = new Stack<>();

    for (int i = n - 1; i >= 0; i--) {
        while (!stack.isEmpty() && stack.peek() <= nums[i]) {
            stack.pop();
        }

        result[i] = stack.isEmpty() ? -1 : stack.peek();
        stack.push(nums[i]);
    }

    return result;
}

// ✅ Approach 3: Circular Array Variant (LeetCode 503)
public int[] nextGreaterElements(int[] nums) {
    int n = nums.length;
    int[] result = new int[n];
    Stack<Integer> stack = new Stack<>();

    for (int i = 2 * n - 1; i >= 0; i--) {
        while (!stack.isEmpty() && stack.peek() <= nums[i % n]) {
            stack.pop();
        }

        if (i < n) {
            result[i] = stack.isEmpty() ? -1 : stack.peek();
        }

        stack.push(nums[i % n]);
    }

    return result;
}
```
---

## 💡 Intuition Behind Each Approach

- **Brute Force:**  
  For every element, linearly scan to the right until you find a greater element. Straightforward but inefficient.

- **Stack-Based:**  
  Use a stack to track candidates while traversing from right to left. Efficient since each element is pushed and popped at most once.

- **Circular Array Variant:**  
  Same stack approach but loop through the array twice to simulate circular behavior. Useful for problems like **LeetCode 503**.

---

## 📊 Complexity Analysis

| Approach                  | Time Complexity | Space Complexity |
|---------------------------|-----------------|------------------|
| Brute Force               | O(n²)           | O(n)             |
| Stack-Based               | O(n)            | O(n)             |
| Circular Array (Variant)  | O(n)            | O(n)             |

---

## 🔹 Edge Cases
1. **All elements in descending order** → result = all `-1`.
2. **All elements equal** → result = all `-1`.
3. **Single element array** → result = `[-1]`.
4. **Circular case**: ensures wrap-around elements are considered in Approach 3.

---

## 🔹 Follow-Up Questions
- How would you adapt this for **Next Smaller Element** instead of Next Greater?
- Can this be extended to work with a **stream of numbers** (online processing)?
- How would you handle a **dynamic array** where elements can be inserted/deleted?

---
