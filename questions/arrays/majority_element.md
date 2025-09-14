# Majority Element

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array of size `n`, find the majority element.  
The majority element is the element that appears more than ⌊n/2⌋ times.

---

## 🔹 Examples
**Example 1:**  
Input: `nums = [3,2,3]`  
Output: `3`

**Example 2:**  
Input: `nums = [2,2,1,1,1,2,2]`  
Output: `2`

---

## 🔹 Constraints
- `1 <= n <= 5 * 10^4`
- `-10^9 <= nums[i] <= 10^9`
- The majority element always exists in the array.

---

## 🔹 Logic & Intuition
- The **majority element** appears more than half the time.
- If you **sort the array**, the middle element is guaranteed to be the majority.
- A **hash map** can count frequencies in one pass.
- The **Boyer-Moore Voting Algorithm** finds majority in O(n) time and O(1) space:
    - Keep a `candidate` and a `count`.
    - Increment if element equals candidate, decrement otherwise.
    - If count hits zero, change candidate.
    - At the end, candidate is the majority.

---

## 🔹 Approaches

### 1. Brute Force (Sorting)
- Sort the array.
- Return the element at index `n/2`.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(1)

---

### 2. Hash Map Counting
- Count frequency of each element.
- Return the element with count > n/2.

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

### 3. Boyer-Moore Voting Algorithm (Optimal)
- Maintain a candidate and count.
- Iterate and update count accordingly.
- Candidate at the end is guaranteed to be the majority.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class MajorityElement {

    // Brute force using sorting
    public static int majorityElementSort(int[] nums) {
        Arrays.sort(nums);
        return nums[nums.length / 2];
    }

    // Hash map counting
    public static int majorityElementHashMap(int[] nums) {
        Map<Integer, Integer> countMap = new HashMap<>();
        int majorityCount = nums.length / 2;
        for (int num : nums) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
            if (countMap.get(num) > majorityCount) {
                return num;
            }
        }
        return -1; // no majority element found
    }

    // Boyer-Moore Voting Algorithm
    public static int majorityElementBoyerMoore(int[] nums) {
        int count = 0, candidate = -1;
        for (int num : nums) {
            if (count == 0) {
                candidate = num;
            }
            count += (num == candidate) ? 1 : -1;
        }
        return candidate;
    }
}
```
---

## 🔹 Complexity Analysis

| Approach                       | Time Complexity | Space Complexity |
|--------------------------------|-----------------|------------------|
| Sorting                        | O(n log n)      | O(1)             |
| HashMap Counting               | O(n)            | O(n)             |
| Boyer-Moore Voting Algorithm   | O(n)            | O(1)             |

---

## 🔹 Edge Cases
1. Array of size 1 → element itself is the majority.  
   Example: `[5] → 5`

2. Multiple elements, but only one can be majority.  
   Example: `[3,3,4] → 3`

3. Large array with clear majority.  
   Example: `[2,2,1,1,1,2,2] → 2`

---

## 🔹 Interviewer Follow-ups
- What if there is **no majority element**?
    - By definition, in this problem statement, majority **always exists**.
    - But if not guaranteed, Boyer-Moore result should be verified with a second pass.

- Can you extend this for **n/3 majority element(s)?**
    - Yes, use a **modified Boyer-Moore** keeping track of **two candidates**.

- How would you solve it in a **streaming scenario** (real-time input)?
    - Apply Boyer-Moore Voting Algorithm while streaming elements.

---

## 🔹 Key Takeaways
- Sorting works but is not optimal.
- HashMap gives linear time but extra space.
- Boyer-Moore is the **best choice** (O(n), O(1)).
- Be ready to **extend Boyer-Moore** for variations like n/3 majority.  
