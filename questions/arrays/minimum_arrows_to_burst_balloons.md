# Minimum Number of Arrows to Burst Balloons

![](../../assets/images/dynamic_programming/burst_balloons.svg)


**Difficulty:** Medium ⚡

**Category:** Arrays / Greedy / Interval Scheduling

---

## 🔹 Problem Statement

There are some spherical balloons taped onto a flat wall that represents the XY-plane. The balloons are represented as a 2D integer array `points` where `points[i] = [xstart, xend]` denotes a balloon whose horizontal diameter stretches between `xstart` and `xend`. You do not know the exact y-coordinates of the balloons.

Arrows can be shot up directly vertically (in the positive y-direction) from different points along the x-axis. A balloon with `xstart` and `xend` is burst by an arrow shot at `x` if `xstart <= x <= xend`. There is no limit to the number of arrows that can be shot. A shot arrow keeps traveling up infinitely, bursting any balloons in its path.

Given the array `points`, return the **minimum number of arrows** that must be shot to burst all balloons.

---

## 🔹 Examples

**Example 1:**  
Input: `points = [[10,16],[2,8],[1,6],[7,12]]`  
Output: `2`  
Explanation: The balloons can be burst by 2 arrows:
- Shoot an arrow at x = 6, bursting balloons [2,8] and [1,6]
- Shoot an arrow at x = 11, bursting balloons [10,16] and [7,12]

**Example 2:**  
Input: `points = [[1,2],[3,4],[5,6],[7,8]]`  
Output: `4`  
Explanation: One arrow is needed for each balloon (no overlaps).

**Example 3:**  
Input: `points = [[1,2],[2,3],[3,4],[4,5]]`  
Output: `2`  
Explanation:
- Shoot an arrow at x = 2, bursting balloons [1,2] and [2,3]
- Shoot an arrow at x = 4, bursting balloons [3,4] and [4,5]

**Example 4:**  
Input: `points = [[1,10],[3,9],[4,11],[6,7],[7,12]]`  
Output: `2`

---

## 🔹 Constraints
- `1 <= points.length <= 10^5`
- `points[i].length == 2`
- `-2^31 <= xstart < xend <= 2^31 - 1`

---

## 🔹 Logic & Intuition

This is a classic **Interval Scheduling** problem that can be solved with a **Greedy Algorithm**.

### Key Insight:
- If we sort balloons by their **end position**, we can shoot arrows optimally.
- When balloons overlap, we can burst them with a single arrow.
- The arrow should be shot at the **rightmost starting point** or **leftmost ending point** of overlapping balloons.

### Greedy Strategy:
1. **Sort balloons by end position** (ascending).
2. Shoot the first arrow at the end of the first balloon.
3. For each subsequent balloon:
   - If it starts **after** the current arrow position → need a new arrow
   - If it **overlaps** with current arrow position → same arrow works

### Why Sort by End Position?
- Sorting by end position ensures we shoot arrows as late as possible.
- This maximizes the chance of hitting future balloons with the same arrow.

---

## 🔹 Approaches

### 1. Brute Force (Try All Arrow Positions)
- Try every possible combination of arrow positions.
- Check which combination bursts all balloons with minimum arrows.

**Time Complexity:** Exponential  
**Space Complexity:** O(1)  
**Issue:** Too slow for large inputs.

---

### 2. Greedy Algorithm (Optimal)
- Sort balloons by end position.
- Use a greedy approach to place arrows optimally.
- Track the position of the last arrow shot.

**Time Complexity:** O(n log n) - due to sorting  
**Space Complexity:** O(1) or O(n) depending on sorting implementation

---

## 🔹 Java Code

```java
import java.util.Arrays;
import java.util.Comparator;

public class BurstBalloons {

    // Approach 1: Greedy - Sort by End Position
    public static int findMinArrowShots(int[][] points) {
        if (points == null || points.length == 0) {
            return 0;
        }
        
        // Sort by end position (ascending)
        Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));
        
        int arrows = 1;  // At least one arrow needed
        int arrowPos = points[0][1];  // Shoot first arrow at end of first balloon
        
        for (int i = 1; i < points.length; i++) {
            // If current balloon starts after the arrow position
            if (points[i][0] > arrowPos) {
                // Need a new arrow
                arrows++;
                arrowPos = points[i][1];  // Shoot at end of current balloon
            }
            // Else: Current balloon overlaps with arrow, no new arrow needed
        }
        
        return arrows;
    }

    // Approach 2: Alternative - Track Overlapping Range
    public static int findMinArrowShotsAlternative(int[][] points) {
        if (points == null || points.length == 0) {
            return 0;
        }
        
        // Sort by start position, then by end position
        Arrays.sort(points, (a, b) -> {
            if (a[0] != b[0]) {
                return Integer.compare(a[0], b[0]);
            }
            return Integer.compare(a[1], b[1]);
        });
        
        int arrows = 1;
        int end = points[0][1];  // Track the rightmost end of overlapping balloons
        
        for (int i = 1; i < points.length; i++) {
            if (points[i][0] <= end) {
                // Balloons overlap, update the minimum end
                end = Math.min(end, points[i][1]);
            } else {
                // No overlap, need a new arrow
                arrows++;
                end = points[i][1];
            }
        }
        
        return arrows;
    }

    // Helper method to print the sorted balloons
    public static void printBalloons(int[][] points) {
        System.out.println("Balloons (sorted by end):");
        for (int[] balloon : points) {
            System.out.println("[" + balloon[0] + ", " + balloon[1] + "]");
        }
    }

    // Detailed solution with arrow positions
    public static int findMinArrowShotsWithPositions(int[][] points) {
        if (points == null || points.length == 0) {
            return 0;
        }
        
        Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));
        
        int arrows = 1;
        int arrowPos = points[0][1];
        System.out.println("Arrow 1 at position: " + arrowPos);
        
        for (int i = 1; i < points.length; i++) {
            if (points[i][0] > arrowPos) {
                arrows++;
                arrowPos = points[i][1];
                System.out.println("Arrow " + arrows + " at position: " + arrowPos);
            } else {
                System.out.println("  Balloon [" + points[i][0] + "," + points[i][1] + 
                                 "] burst by arrow at " + arrowPos);
            }
        }
        
        return arrows;
    }

    public static void main(String[] args) {
        // Test Case 1
        int[][] points1 = {{10,16},{2,8},{1,6},{7,12}};
        System.out.println("Input: [[10,16],[2,8],[1,6],[7,12]]");
        System.out.println("Min Arrows: " + findMinArrowShots(points1));
        System.out.println("Expected: 2\n");

        // Test Case 2
        int[][] points2 = {{1,2},{3,4},{5,6},{7,8}};
        System.out.println("Input: [[1,2],[3,4],[5,6],[7,8]]");
        System.out.println("Min Arrows: " + findMinArrowShots(points2));
        System.out.println("Expected: 4\n");

        // Test Case 3
        int[][] points3 = {{1,2},{2,3},{3,4},{4,5}};
        System.out.println("Input: [[1,2],[2,3],[3,4],[4,5]]");
        System.out.println("Min Arrows: " + findMinArrowShots(points3));
        System.out.println("Expected: 2\n");

        // Test Case 4 - Detailed
        int[][] points4 = {{1,10},{3,9},{4,11},{6,7},{7,12}};
        System.out.println("Input: [[1,10],[3,9],[4,11],[6,7],[7,12]]");
        System.out.println("Detailed Solution:");
        int result = findMinArrowShotsWithPositions(points4);
        System.out.println("Total Arrows: " + result);
        System.out.println("Expected: 2\n");

        // Test Case 5 - Edge case with large numbers
        int[][] points5 = {{Integer.MIN_VALUE, Integer.MAX_VALUE}};
        System.out.println("Input: [[MIN_VALUE, MAX_VALUE]]");
        System.out.println("Min Arrows: " + findMinArrowShots(points5));
        System.out.println("Expected: 1\n");

        // Test Case 6 - Overlapping at boundaries
        int[][] points6 = {{1,2},{2,3},{3,4}};
        System.out.println("Input: [[1,2],[2,3],[3,4]]");
        System.out.println("Min Arrows: " + findMinArrowShots(points6));
        System.out.println("Expected: 2\n");
    }
}
```

---

## 🔹 Complexity Analysis

| Approach | Time Complexity | Space Complexity |
|----------|----------------|------------------|
| Brute Force | Exponential | O(1) |
| **Greedy (Sort by End)** | **O(n log n)** | **O(1)** |
| Greedy (Sort by Start) | O(n log n) | O(1) |

**Bottleneck:** Sorting takes O(n log n) time.

---

## 🔹 Step-by-Step Example

**Input:** `points = [[10,16],[2,8],[1,6],[7,12]]`

**Step 1: Sort by End Position**
```
Original: [[10,16], [2,8], [1,6], [7,12]]
Sorted:   [[1,6], [2,8], [7,12], [10,16]]
```

**Step 2: Greedy Arrow Placement**
```
Balloon [1,6]:  Shoot arrow at position 6
                arrows = 1, arrowPos = 6

Balloon [2,8]:  Start = 2 <= 6 (overlaps)
                No new arrow needed

Balloon [7,12]: Start = 7 > 6 (no overlap)
                Shoot new arrow at position 12
                arrows = 2, arrowPos = 12

Balloon [10,16]: Start = 10 <= 12 (overlaps)
                 No new arrow needed

Total: 2 arrows
```

---

## 🔹 Key Insights

### 1. Interval Scheduling Problem
This is a classic greedy interval scheduling problem similar to:
- Activity Selection Problem
- Non-overlapping Intervals
- Meeting Rooms II

### 2. Why Sort by End Position?
Sorting by end position is optimal because:
- It allows us to shoot arrows as late as possible
- Maximizes the chance of hitting future balloons
- Greedy choice is locally and globally optimal

### 3. Overlapping Condition
Two balloons overlap if: `balloon2.start <= arrow_position`
- If `balloon2.start > arrow_position` → need new arrow
- Otherwise → same arrow works

### 4. Edge Cases
- Single balloon: Return 1
- No balloons: Return 0
- All balloons overlap: Return 1
- No balloons overlap: Return n (number of balloons)
- Balloons touching at endpoints: Count as overlapping

---

## 🔹 Edge Cases

| Case | Input | Output | Explanation |
|------|-------|--------|-------------|
| Single balloon | `[[1,10]]` | `1` | One arrow needed |
| No overlap | `[[1,2],[3,4],[5,6]]` | `3` | Each needs separate arrow |
| Complete overlap | `[[1,10],[2,8],[3,7]]` | `1` | All overlap, one arrow |
| Touch at boundary | `[[1,2],[2,3]]` | `1` | Touching counts as overlap |
| Large numbers | `[[MIN_VALUE,MAX_VALUE]]` | `1` | Handle integer limits |
| Same start different end | `[[1,5],[1,10]]` | `1` | Both overlap |

---

## 🔹 Common Mistakes

1. **Wrong Sort Comparator:**
   - Using `a[1] - b[1]` causes overflow for large integers
   - Use `Integer.compare(a[1], b[1])` instead

2. **Overlap Condition:**
   - Using `>=` instead of `>` for non-overlapping check
   - Correct: `points[i][0] > arrowPos` (strictly greater)

3. **Not Handling Integer Overflow:**
   - Test with `Integer.MIN_VALUE` and `Integer.MAX_VALUE`

4. **Sorting by Start vs End:**
   - Sorting by start requires tracking minimum end
   - Sorting by end is simpler and more intuitive

---

## 🔹 Follow-Up Questions

### 1. Maximum Balloons with K Arrows
**Q:** What's the maximum number of balloons you can burst with exactly K arrows?  
**A:** Sort by end, greedily select K positions to maximize coverage.

### 2. Weighted Balloons
**Q:** Each balloon has a weight. Minimize cost (arrows × weight)?  
**A:** Use weighted interval scheduling with DP.

### 3. 2D Balloons
**Q:** Balloons have both x and y ranges `[[x1,x2],[y1,y2]]`?  
**A:** Becomes a 2D interval problem, much more complex.

### 4. Arrow Cost Varies
**Q:** Arrows at different positions have different costs?  
**A:** Use DP to minimize total cost.

---

## 🔹 Related Problems

- **Non-overlapping Intervals** (LeetCode 435) - Remove minimum intervals
- **Meeting Rooms II** (LeetCode 253) - Minimum meeting rooms needed
- **Merge Intervals** (LeetCode 56) - Merge overlapping intervals
- **Insert Interval** (LeetCode 57) - Insert and merge
- **Activity Selection Problem** - Classic greedy problem
- **Interval List Intersections** (LeetCode 986)

---

## 🔹 Pattern Recognition

**Greedy Interval Problems Pattern:**

1. **Sort intervals** (by start, end, or duration)
2. **Track current state** (end position, count, etc.)
3. **Make greedy choice** for each interval
4. **Update state** based on overlap/non-overlap

**Common Interval Problem Types:**
- Scheduling (maximize activities)
- Coverage (minimize resources)
- Merging (combine overlaps)
- Intersection (find common ranges)

---

## 🔹 Optimization Tips

### 1. In-Place Sorting
Java's `Arrays.sort()` for primitive arrays uses Dual-Pivot Quicksort (O(n log n) average, O(n²) worst case). For better worst-case, use TimSort by converting to object array.

### 2. Custom Comparator
```java
// Using lambda (modern, clean)
Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));

// Using Comparator.comparingInt (Java 8+)
Arrays.sort(points, Comparator.comparingInt(a -> a[1]));
```

### 3. Early Termination
If you need exactly K arrows, stop when arrows == K.

---

## 🔹 Visualization

```
Balloons (sorted by end position):
[1,6]   ----●
[2,8]     ------●
[7,12]           -------●
[10,16]              --------●

Arrow 1: x=6   (bursts [1,6] and [2,8])
         ↑
Arrow 2: x=12  (bursts [7,12] and [10,16])
               ↑

Total: 2 arrows
```

---

## 🔹 Interview Tips

1. **Clarify the problem:**
   - Can balloons have the same coordinates? (Yes)
   - Are coordinates sorted? (No, need to sort)
   - Do touching balloons count as overlapping? (Yes, `start <= x <= end`)

2. **Start with examples:**
   - Draw the intervals on paper
   - Identify overlaps visually

3. **Explain greedy choice:**
   - "Sort by end position to shoot arrows as late as possible"
   - "This maximizes coverage of future balloons"

4. **Handle edge cases:**
   - Empty array
   - Single balloon
   - Integer overflow in comparator

5. **Analyze complexity:**
   - Sorting: O(n log n)
   - Single pass: O(n)
   - Total: O(n log n)

6. **Compare approaches:**
   - Sorting by end (simpler)
   - Sorting by start (need to track minimum end)

---

## 🔹 Alternative Solutions

### Sort by Start Position
```java
public static int findMinArrowShotsByStart(int[][] points) {
    Arrays.sort(points, (a, b) -> Integer.compare(a[0], b[0]));
    
    int arrows = 1;
    int minEnd = points[0][1];
    
    for (int i = 1; i < points.length; i++) {
        if (points[i][0] <= minEnd) {
            // Overlapping, update minimum end
            minEnd = Math.min(minEnd, points[i][1]);
        } else {
            // Not overlapping, need new arrow
            arrows++;
            minEnd = points[i][1];
        }
    }
    
    return arrows;
}
```

This approach works but is slightly more complex because we need to track the minimum end position of overlapping balloons.

---

This is a **medium-level greedy problem** frequently asked at companies like Amazon, Google, and Microsoft!
