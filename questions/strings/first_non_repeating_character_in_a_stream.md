# 🔹 Problem: First Non-Repeating Character in a Stream

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given a stream of lowercase characters, one by one, print the **first non-repeating character** at each point in the stream.

**Example:**  
Input: `stream = "aabc"`  
Output: `["a", "-1", "b", "b"]`

Explanation:
- After first `a` → first non-repeating is `a`
- After second `a` → `a` repeats → output `-1`
- After third `b` → first non-repeating is `b`
- After fourth `c` → first non-repeating is still `b`

---

## 🔹 Intuition
- Keep track of **frequency** of each character.
- Maintain the **order of characters** using a queue.
- The **front of the queue** always holds the first non-repeating character.
- Remove characters from the queue if they repeat.

---

## 🔹 Approaches

### 1. Brute Force
- For each character in the stream:
    1. Count frequency of all characters seen so far.
    2. Traverse from start to find the first character with frequency 1.
- Output that character, or `-1` if none exists.

**Time Complexity:** O(n²)  
**Space Complexity:** O(1) (frequency array only)

---

### 2. Queue + Frequency Map (Optimal)
- Use a **Queue** to maintain order of characters.
- Use a **frequency array/map** to store count of each character.
- For each new character:
    1. Increment its frequency.
    2. Add it to the queue.
    3. Remove characters from the front if their frequency > 1.
- The front of the queue (if any) is the first non-repeating character; else output `-1`.

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

## 🔹 Java Code

```java
import java.util.*;

public class FirstNonRepeatingCharacter {

    public static void printFirstNonRepeating(String stream) {
        int[] freq = new int[26]; // frequency array
        Queue<Character> q = new LinkedList<>();

        for (char ch : stream.toCharArray()) {
            freq[ch - 'a']++;
            q.add(ch);

            // Remove repeating characters from front
            while (!q.isEmpty() && freq[q.peek() - 'a'] > 1) {
                q.poll();
            }

            if (q.isEmpty()) {
                System.out.print("-1 ");
            } else {
                System.out.print(q.peek() + " ");
            }
        }
    }

    public static void main(String[] args) {
        String stream = "aabc";
        printFirstNonRepeating(stream);
    }
}
```

---

## 🔹 Complexity Analysis

| Approach              | Time Complexity | Space Complexity |
|-----------------------|-----------------|------------------|
| Brute Force           | O(n²)           | O(1)             |
| Queue + Frequency Map | O(n)            | O(n)             |

---

## 🔹 Edge Cases
- Empty stream → Output `[]`
- All repeating characters → Output `-1` for all except first
- Stream with only unique characters → Output all characters as-is

---

## 🔹 Follow-Up Questions
1. How would you extend this to handle **uppercase and lowercase** letters?
2. How would you handle a **continuous infinite stream** without storing all characters?
3. Can you modify it to **return the index** of the first non-repeating character at each step?
