# 🔹 Problem: No Adjacent Characters Together

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a string `s` consisting of only lowercase letters, **rearrange the characters** so that **no two adjacent characters are the same**.  
Return **any valid rearranged string**, or an empty string `""` if not possible.

**Example:**  
Input: `s = "aab"`  
Output: `"aba"`

Input: `s = "aaab"`  
Output: `""`

Explanation:
- `"aab"` can be rearranged as `"aba"` where no two same letters are adjacent.
- `"aaab"` cannot be rearranged to satisfy the condition.

---

## 🔹 Intuition
- Use a **max-heap (priority queue)** to always place the character with **highest remaining count**.
- Place characters one by one, keeping track of the **previous character** to avoid consecutive repeats.
- If the most frequent character cannot be placed, return `""`.

---

## 🔹 Approaches

### 1. Count + Sorting
- Count frequency of each character.
- Sort characters by frequency.
- Try to place characters alternatively in the string.
- If at any point a character cannot be placed without repeating → impossible.

**Time Complexity:** O(n log n)  
**Space Complexity:** O(26) → for frequency array

---

### 2. Max Heap / Priority Queue (Optimal)
- Count frequency of each character.
- Push all characters into a **max-heap** by frequency.
- Initialize `prevChar` with frequency 0.
- While heap is not empty:
    1. Pop character `curr` from heap.
    2. Append `curr` to result.
    3. Decrease its frequency.
    4. Push `prevChar` back if frequency > 0.
    5. Update `prevChar = curr`.
- If successfully placed all characters → return string; else → `""`.

**Time Complexity:** O(n log 26) → O(n) effectively  
**Space Complexity:** O(26 + n) → heap + result

---

## 🔹 Java Code

```java
import java.util.*;

public class NoAdjacentCharacters {

    public static String rearrangeString(String s) {
        int[] freq = new int[26];
        for (char c : s.toCharArray()) freq[c - 'a']++;

        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((a,b) -> b[1] - a[1]);
        for (int i = 0; i < 26; i++) {
            if (freq[i] > 0) maxHeap.offer(new int[]{i, freq[i]});
        }

        StringBuilder result = new StringBuilder();
        int[] prev = {-1, 0}; // {charIndex, remainingCount}

        while (!maxHeap.isEmpty()) {
            int[] curr = maxHeap.poll();
            result.append((char) (curr[0] + 'a'));
            curr[1]--;

            if (prev[1] > 0) {
                maxHeap.offer(prev);
            }

            prev = curr;
        }

        return result.length() == s.length() ? result.toString() : "";
    }

    public static void main(String[] args) {
        String s1 = "aab";
        String s2 = "aaab";

        System.out.println(rearrangeString(s1)); // Output: "aba"
        System.out.println(rearrangeString(s2)); // Output: ""
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                  | Time Complexity | Space Complexity |
|---------------------------|----------------|-----------------|
| Count + Sorting           | O(n log n)     | O(26)           |
| Max Heap / Priority Queue | O(n)           | O(26 + n)       |

---

## 🔹 Edge Cases
- Single character → always valid
- All characters same → impossible → return `""`
- Already valid string → no rearrangement needed
- Large frequency of one character → check if possible (`maxFreq <= (n+1)/2`)

---

## 🔹 Follow-Up Questions
1. How would you handle **uppercase and lowercase letters together**?
2. Can you **return all possible valid rearrangements**?
3. How would you modify the solution for **streaming input**, where characters arrive one by one?
