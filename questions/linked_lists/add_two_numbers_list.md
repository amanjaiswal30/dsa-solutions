# 🔹 Add Two Numbers Represented by Linked Lists

---

## 📌 Problem Statement
You are given two non-empty linked lists representing two non-negative integers.
- The digits are stored in **reverse order**.
- Each node contains a single digit.

Add the two numbers and return the **sum as a linked list**.

---

### Example

**Input:**  
List 1: 2 → 4 → 3  (represents 342)  
List 2: 5 → 6 → 4  (represents 465)

**Output:**  
7 → 0 → 8 (represents 807 = 342 + 465)

---

## 💡 Logic & Intuition
- Each node represents a digit; the least significant digit comes first.
- Two approaches:
    1. **Brute Force:** Convert lists to integers, sum, then convert back.
    2. **Optimized:** Add digit-by-digit with a carry, creating new nodes for the sum.

---

## 🔹 Approaches

### 1. Brute Force (Convert to Number)
**Intuition:**
- Convert linked lists to integers.
- Add the integers.
- Convert the sum back to a linked list.

**Downsides:**
- Can cause **overflow** for very large numbers.
- Not suitable for long lists.

**Time Complexity:** O(m + n)  
**Space Complexity:** O(m + n) (for the output list)

---

### 2. Optimized (Digit-by-Digit Addition with Carry)
**Intuition:**
- Traverse both lists simultaneously.
- Add corresponding digits plus any **carry**.
- Create new nodes for each digit in the sum.
- If a **carry remains** after traversal, add a new node.

**Time Complexity:** O(max(m, n))  
**Space Complexity:** O(max(m, n)) (for the output list)

---

## 💻 Java Code (All Approaches)

```java
// Approach 1: Brute Force
public static ListNode addTwoNumbersBruteForce(ListNode l1, ListNode l2) {
    long num1 = 0, num2 = 0;
    long multiplier = 1;

    while (l1 != null) {
        num1 += l1.val * multiplier;
        multiplier *= 10;
        l1 = l1.next;
    }

    multiplier = 1;
    while (l2 != null) {
        num2 += l2.val * multiplier;
        multiplier *= 10;
        l2 = l2.next;
    }

    long sum = num1 + num2;

    if (sum == 0) return new ListNode(0);

    ListNode dummy = new ListNode(0);
    ListNode current = dummy;
    while (sum > 0) {
        current.next = new ListNode((int)(sum % 10));
        sum /= 10;
        current = current.next;
    }

    return dummy.next;
}

// Approach 2: Optimized
public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0);
    ListNode current = dummy;

    int carry = 0;
    while (l1 != null || l2 != null || carry != 0) {
        int sum = carry;

        if (l1 != null) {
            sum += l1.val;
            l1 = l1.next;
        }

        if (l2 != null) {
            sum += l2.val;
            l2 = l2.next;
        }

        carry = sum / 10;
        current.next = new ListNode(sum % 10);
        current = current.next;
    }

    return dummy.next;
}
```

---

## 🔹 Complexity Analysis

| Approach    | Time Complexity | Space Complexity |
|-------------|-----------------|------------------|
| Brute Force | O(m + n)        | O(m + n)         |
| Optimized   | O(max(m, n))    | O(max(m, n))     |

---

## 🔹 Edge Cases
- Lists of **different lengths**.
- **Carry** at the end (e.g., 9 → 9 + 1 → 0).
- One list is **null** → return the other list.
- Both lists are **null** → return null.

---

## 🔹 Follow-Up Questions
1. How would you **do this in-place** without creating extra nodes?
2. Can you extend this to **numbers stored in forward order**?
3. How would you handle **very large numbers** that cannot fit in primitive types?


