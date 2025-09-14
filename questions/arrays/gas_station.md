# Gas Station Problem

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
You are given two integer arrays of length `n`:

- `gas[i]` → amount of gas available at station `i`.
- `cost[i]` → gas required to travel from station `i` to `(i + 1) % n`.

A car with an **unlimited gas tank** starts at one of the gas stations.  
Return the starting station index from which the car can **complete a full circuit**, or `-1` if it is impossible.

---

## 🔹 Logic & Intuition
- If the **total gas < total cost**, completing the circuit is impossible.
- Otherwise, there will always be **exactly one valid starting station**.
- Use a **greedy approach**:
    - Keep a running balance `tank += gas[i] - cost[i]`.
    - If `tank` becomes negative, reset the start index to the next station and reset `tank = 0`.
- This works because all stations before the reset point cannot be valid starting stations.

---

## 🔹 Approaches

### 1. Brute Force
- Try starting at each station.
- Simulate the full journey to check if it completes.

**Time Complexity:** O(n²)  
**Space Complexity:** O(1)

---

### 2. Greedy (Optimal)
- Traverse the array once.
- Track total difference and running `tank`.
- Reset starting index whenever `tank` < 0.
- At the end, if `totalGas >= 0`, return the valid start.

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

---

## 🔹 Java Code

```java
public class GasStation {

    // Brute force approach
    public static int canCompleteCircuitBrute(int[] gas, int[] cost) {
        int n = gas.length;
        for (int start = 0; start < n; start++) {
            int tank = 0;
            int count = 0;
            int i = start;
            while (count < n) {
                tank += gas[i] - cost[i];
                if (tank < 0) break;
                i = (i + 1) % n;
                count++;
            }
            if (count == n) return start;
        }
        return -1;
    }

    // Greedy optimal solution
    public static int canCompleteCircuitGreedy(int[] gas, int[] cost) {
        int totalGas = 0, tank = 0, start = 0;
        
        for (int i = 0; i < gas.length; i++) {
            int diff = gas[i] - cost[i];
            tank += diff;
            totalGas += diff;
            
            if (tank < 0) {
                start = i + 1;
                tank = 0;
            }
        }
        
        return totalGas >= 0 ? start : -1;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach    | Time Complexity | Space Complexity |
|-------------|-----------------|------------------|
| Brute Force | O(n²)           | O(1)             |
| Greedy      | O(n)            | O(1)             |

---

## 🔹 Edge Cases
- Gas equals cost at all stations → any station is valid.
- Only one station has `gas[i] < cost[i]`.
- Total gas < total cost → impossible to complete the journey.

---

## 🔹 Follow-Up Questions
1. How would you solve the problem if you were allowed to **modify the input arrays**?
2. Can this approach be extended if there were **multiple cars** with different fuel tanks?
3. How would you handle it if stations also had **different fuel prices** and you wanted to minimize cost?


