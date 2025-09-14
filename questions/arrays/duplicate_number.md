# Find the Duplicate Number

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array of `n + 1` integers where each integer is between `1` and `n` (inclusive), prove that at least one duplicate number must exist.  
Find the duplicate number **without modifying the array** and using only **constant extra space**.

---

## 🔹 Logic & Intuition
- The array can be viewed as a **linked list** where each index points to the value at that index.
- Because of the duplicate, the linked list will always contain a **cycle**.
- Use **Floyd’s Tortoise and Hare (Cycle Detection)** algorithm to detect this cycle and find the duplicate.

---

## 🔹 Approaches

### 1. Brute Force
- Compare every element against all others.
- Return the duplicate if found.

**Time Complexity:** O(n²)  
**Space Complexity:** O(1)

---

### 2. Sorting
- Sort the array.
- Check consecutive elements for duplicates.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(1) (if in-place) or O(n) (depending on sorting algorithm).

---

### 3. Hash Set
- Maintain a hash set of seen numbers.
- If a number repeats → return it.

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

### 4. Floyd’s Cycle Detection (Optimal)
- Use two pointers:
    - **slow** moves by 1 step.
    - **fast** moves by 2 steps.
- Detect cycle intersection, then reset one pointer to start.
- The meeting point is the duplicate.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.HashSet;
import java.util.Set;

public class FindDuplicateNumber {

    // Brute force approach
    public static int findDuplicateBrute(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] == nums[j]) {
                    return nums[i];
                }
            }
        }
        return -1; // no duplicate found
    }

    // Using HashSet
    public static int findDuplicateHashSet(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int num : nums) {
            if (seen.contains(num)) {
                return num;
            }
            seen.add(num);
        }
        return -1;
    }

    // Floyd’s Tortoise and Hare (Cycle Detection)
    public static int findDuplicateFloyd(int[] nums) {
        int slow = nums[0];
        int fast = nums[0];
        
        // Phase 1: Detect intersection point
        do {
            slow = nums[slow];
            fast = nums[nums[fast]];
        } while (slow != fast);

        // Phase 2: Find entrance to the cycle (duplicate)
        slow = nums[0];
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }
        return slow;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                  | Time Complexity | Space Complexity |
|---------------------------|-----------------|------------------|
| Brute Force               | O(n²)           | O(1)             |
| Sorting                   | O(n log n)      | O(1) / O(n)      |
| Hash Set                  | O(n)            | O(n)             |
| Floyd’s Cycle Detection   | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Array contains only **one duplicate number** multiple times.
- Minimum input size: `n = 1`.
- Duplicate appears at the **start** or **end** of the array.

---

## 🔹 Follow-Up Questions
1. How would you modify the algorithm if **multiple duplicates** were possible?
2. How would you solve it if modifying the array was **allowed** (e.g., marking visited indices negative)?

---
