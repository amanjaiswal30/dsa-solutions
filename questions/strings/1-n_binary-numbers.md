# 🔹 Problem: Generate Binary Numbers from 1 to N

**Difficulty:** Easy ⚡

---

## 🔹 Problem Statement
Given a number `n`, generate and print binary numbers from `1` to `n`.

**Example:**  
Input: `n = 5`  
Output: `["1", "10", "11", "100", "101"]`

---

## 🔹 Intuition
Binary numbers are simply base-2 representations of integers.
- One straightforward way: convert each integer `i` from 1 → n using `Integer.toBinaryString(i)`.
- But a more **interesting and interview-style approach** is to use a **Queue** (BFS-like method) to generate numbers level by level — similar to how binary trees expand.

---

## 🔹 Approaches

### 1. Direct Conversion
- For each number `i` from 1 to n:
    - Convert `i` to binary using `Integer.toBinaryString(i)`.
    - Add it to the result list.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(n)

---

### 2. Using Queue (Optimal BFS-like Approach)
- Start with `"1"` in a queue.
- Repeatedly remove the front element and:
    - Append it to the result.
    - Push `current + "0"` and `current + "1"` to the queue.
- Continue until you generate `n` binary numbers.

**Example:**  
Queue flow for `n = 5`:  

Start: ["1"]

Pop "1" → add "10", "11" → ["10", "11"]

Pop "10" → add "100", "101" → ["11", "100", "101"]

Result: ["1", "10", "11", "100", "101"]


**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

## 🔹 Java Code (Both Approaches)

```java
import java.util.*;

public class GenerateBinaryNumbers {

    // 1. Direct Conversion using Integer.toBinaryString()
    public static List<String> generateBinaryDirect(int n) {
        List<String> result = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            result.add(Integer.toBinaryString(i));
        }
        return result;
    }

    // 2. Queue-based BFS approach
    public static List<String> generateBinaryUsingQueue(int n) {
        List<String> result = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();

        queue.add("1");
        while (n-- > 0) {
            String current = queue.poll();
            result.add(current);
            queue.add(current + "0");
            queue.add(current + "1");
        }

        return result;
    }

    // For testing
    public static void main(String[] args) {
        int n = 5;
        System.out.println(generateBinaryUsingQueue(n));
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                     | Time Complexity | Space Complexity | Notes                         |
|------------------------------|-----------------|------------------|-------------------------------|
| Direct Conversion            | O(n log n)      | O(n)             | Simple, but less intuitive    |
| Queue (BFS-style generation) | O(n)            | O(n)             | More elegant, interview-style |

---

## 🔹 Edge Cases
- `n = 0` → Output: `[]`
- `n = 1` → Output: `["1"]`
- Large `n` → Should handle efficiently using Queue method.

---

## 🔹 Example

**Input:**

n = 5


**Output:**

["1", "10", "11", "100", "101"]


**Explanation:**  
Binary representations of 1 to 5 are generated in increasing order.

---

## 🔹 Follow-Up Questions
1. How would you print binary numbers **padded with leading zeros** (e.g., for 3 bits: 001, 010, 011)?
2. Can you modify this to generate **Gray Codes** instead of normal binary numbers?
3. How would you implement this in **recursive** fashion (without using queue)?
