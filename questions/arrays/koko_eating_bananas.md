# 🔹 Koko Eating Bananas

![](../../assets/images/arrays/koko_eating_bananas.svg)


---

## 📌 Problem Statement
Koko loves bananas. There are `n` piles of bananas, `piles[i]` bananas in the `i-th` pile. There are `h` hours until the guards return.

Each hour, Koko chooses **one pile** and eats `k` bananas from it. If the pile has fewer than `k` bananas, she eats all of them and does not eat any more bananas during that hour (she cannot combine multiple piles in the same hour).

Koko wants to eat all the bananas within `h` hours, as slowly as possible. Return the **minimum integer eating speed `k`** such that she can eat all the bananas within `h` hours.

---

## 📊 Example Input & Output

**Input:** `piles = [3, 6, 7, 11]`, `h = 8`
**Output:** `4`

**Input:** `piles = [30, 11, 23, 4, 20]`, `h = 5`
**Output:** `30`

**Input:** `piles = [30, 11, 23, 4, 20]`, `h = 6`
**Output:** `23`

---

## 💻 Java Code (All Approaches)

```java
import java.util.Arrays;

public class KokoEatingBananas {

    // Approach 1: Brute Force - try every speed starting from 1
    public int minEatingSpeedBruteForce(int[] piles, int h) {
        int maxPile = Arrays.stream(piles).max().getAsInt();

        for (int speed = 1; speed <= maxPile; speed++) {
            if (hoursNeeded(piles, speed) <= h) {
                return speed;
            }
        }
        return maxPile;
    }

    // Approach 2: Binary Search on Answer (Optimized)
    public int minEatingSpeed(int[] piles, int h) {
        int low = 1, high = Arrays.stream(piles).max().getAsInt();

        while (low < high) {
            int mid = low + (high - low) / 2;
            if (hoursNeeded(piles, mid) <= h) {
                high = mid;       // speed works, try to go slower
            } else {
                low = mid + 1;    // too slow, need more speed
            }
        }

        return low;
    }

    // Hours needed to finish all piles at a given eating speed
    private long hoursNeeded(int[] piles, int speed) {
        long hours = 0;
        for (int pile : piles) {
            hours += (pile + speed - 1) / speed; // ceil(pile / speed)
        }
        return hours;
    }

    public static void main(String[] args) {
        KokoEatingBananas solution = new KokoEatingBananas();
        System.out.println(solution.minEatingSpeed(new int[]{3, 6, 7, 11}, 8));          // 4
        System.out.println(solution.minEatingSpeed(new int[]{30, 11, 23, 4, 20}, 5));    // 30
        System.out.println(solution.minEatingSpeed(new int[]{30, 11, 23, 4, 20}, 6));    // 23
    }
}
```
---

## 💡 Intuition Behind Each Approach

- **Brute Force:**
  Try every possible eating speed starting from `1`. For each candidate speed, compute the total hours needed (using ceiling division per pile) and return the first speed that fits within `h` hours. Worst case checks up to `max(piles)` speeds, each requiring an O(n) feasibility check.

- **Binary Search on Answer (Optimized):**
  The feasibility function `hoursNeeded(piles, speed) <= h` is **monotonic**: if a speed `k` is fast enough, every speed greater than `k` is also fast enough (and slower speeds may or may not work). This monotonic "yes/no" boundary is exactly what binary search needs — search the speed space `[1, max(piles)]` instead of the answer space directly. At each `mid`, check feasibility; if it works, the answer could be smaller so shrink `high`, otherwise push `low` up. Converges to the minimum feasible speed in O(log(max(piles))) iterations, each doing an O(n) feasibility check.

---

## 📊 Complexity Analysis

| Approach                        | Time Complexity        | Space Complexity |
|----------------------------------|-------------------------|-------------------|
| Brute Force                     | O(n · max(piles))       | O(1)              |
| Binary Search on Answer         | O(n · log(max(piles)))  | O(1)              |

---

## 🔹 Edge Cases
1. **`h` equals the number of piles** → Koko can only eat from each pile once, so `k` must equal `max(piles)`.
2. **`h` much larger than `n`** → the minimum speed can be as low as `1`.
3. **All piles the same size** → answer depends purely on `ceil(size / (h / n))`-style division; binary search still applies directly.
4. **Large pile values** → use `long` for the accumulated hours to avoid overflow when `piles.length` and pile sizes are both large.
5. **Single pile** → answer is `ceil(pile / h)`.

---

## 🔹 Follow-Up Questions
- Can you solve the **inverse problem**: given a fixed eating speed `k`, find the minimum `h` required?
- How would the approach change if Koko could split her hourly effort **across two piles**?
- Can you generalize the feasibility check to support a **per-pile time cost** (e.g., some piles take longer to start eating from)?

---
