# Minimum Number of Meeting Rooms

**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array of meeting time intervals `intervals[i] = [start_i, end_i]`, find the **minimum number of conference rooms required**.

---

## 🔹 Logic & Intuition
- A meeting room is needed if **meetings overlap**.
- Sort the intervals by **start time**.
- Track ongoing meetings:
    - **Brute Force:** Count overlaps for each interval.
    - **Min-Heap:** Track earliest ending meeting.
    - **Two Pointers:** Separate start and end times, count overlaps using two pointers.

---

## 🔹 Approaches

### 1. Brute Force
- For each interval, count how many intervals overlap with it.
- Maximum overlap count = minimum rooms required.
- **Time Complexity:** O(n²)
- **Space Complexity:** O(1)

---

### 2. Min-Heap (PriorityQueue)
- Sort intervals by start time.
- Use a min-heap to track end times of ongoing meetings.
- For each meeting:
    - If `interval.start >= heap.peek()`, pop from heap (reuse room).
    - Push `interval.end` to heap (allocate room).
- Heap size = minimum rooms required.
- **Time Complexity:** O(n log n) → sorting + heap operations
- **Space Complexity:** O(n) → heap may store all intervals in worst case

---

### 3. Two-Pointer Approach (Optimal without heap)
- Separate **start times** and **end times** arrays.
- Sort both arrays.
- Use two pointers `i` (start) and `j` (end) and a `rooms` counter:
    - If `starts[i] < ends[j]` → need a new room → increment rooms.
    - Else → meeting ended → move end pointer `j++`.
- Maximum rooms counted during traversal = minimum rooms required.
- **Time Complexity:** O(n log n) → sorting
- **Space Complexity:** O(n)

---

## 🔹 Java Code

```java
import java.util.*;

public class MeetingRooms {

    // Brute Force Approach
    public static int minMeetingRoomsBruteForce(int[][] intervals) {
        int n = intervals.length;
        int maxRooms = 0;

        for (int i = 0; i < n; i++) {
            int rooms = 1;
            for (int j = 0; j < n; j++) {
                if (i != j && intervals[i][0] < intervals[j][1] && intervals[j][0] < intervals[i][1]) {
                    rooms++;
                }
            }
            maxRooms = Math.max(maxRooms, rooms);
        }

        return maxRooms;
    }

    // Min-Heap Approach
    public static int minMeetingRoomsHeap(int[][] intervals) {
        if (intervals == null || intervals.length == 0) return 0;

        Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        heap.add(intervals[0][1]);

        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] >= heap.peek()) {
                heap.poll(); // reuse room
            }
            heap.add(intervals[i][1]);
        }

        return heap.size();
    }

    // Two-Pointer Approach
    public static int minMeetingRoomsTwoPointer(int[][] intervals) {
        if (intervals == null || intervals.length == 0) return 0;

        int n = intervals.length;
        int[] starts = new int[n];
        int[] ends = new int[n];

        for (int i = 0; i < n; i++) {
            starts[i] = intervals[i][0];
            ends[i] = intervals[i][1];
        }

        Arrays.sort(starts);
        Arrays.sort(ends);

        int rooms = 0, endPtr = 0;
        for (int i = 0; i < n; i++) {
            if (starts[i] < ends[endPtr]) {
                rooms++; // need new room
            } else {
                endPtr++; // reuse room
            }
        }

        return rooms;
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                 | Time Complexity | Space Complexity |
|--------------------------|-----------------|------------------|
| Brute Force              | O(n²)           | O(1)             |
| Min-Heap (PriorityQueue) | O(n log n)      | O(n)             |
| Two-Pointer              | O(n log n)      | O(n)             |

---

## 🔹 Edge Cases
- No meetings → return 0.
- All meetings **non-overlapping** → 1 room.
- All meetings **fully overlapping** → `n` rooms.
- Meetings with same start and end times.
- Single meeting → 1 room.

---

## 🔹 Examples

**Example 1:**  
Input: `[[0,30],[5,10],[15,20]]`  
Output: `2`

**Example 2:**  
Input: `[[7,10],[2,4]]`  
Output: `1`

**Example 3:**  
Input: `[[1,5],[2,6],[3,7]]`  
Output: `3`

---

## 🔹 Follow-Up Questions
1. How would you handle **real-time meeting scheduling**?  
   → Use a dynamic data structure like **TreeMap** for start/end times.
2. Can you optimize further if meetings are **already sorted by start time**?  
   → Use the **two-pointer approach** without a heap.
