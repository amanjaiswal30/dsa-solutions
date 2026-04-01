# 🔹 Problem: Find First and Last Position of Element in Sorted Array

![](../../assets/images/arrays/first_and_last_position_in_sorted_array.svg)


**Difficulty:** Medium ⚡

---

## 🔹 Problem Statement
Given an array of integers `nums` sorted in **non-decreasing order** and a target value `target`, return the **starting and ending position** of the target in the array.

- If the target is not found, return `[-1, -1]`.
- You must write an algorithm with **O(log n)** runtime complexity.

---

## 🔹 Intuition
- Since the array is **sorted**, **binary search** can be used.
- Perform **two binary searches**:
    1. First to find the **leftmost (first) occurrence**.
    2. Second to find the **rightmost (last) occurrence**.
- Classic modification of binary search to find boundaries instead of any occurrence.

---

## 🔹 Approaches

### 1. Two-pass Binary Search
- First binary search → find first occurrence.
- Second binary search → find last occurrence.
- Each binary search O(log n).

**Time Complexity:** O(log n)  
**Space Complexity:** O(1)

### 2. One-pass Binary Search (Optional)
- Use a modified binary search that searches for first and last positions in a single pass using recursion or iterative approach.
- Slightly trickier, same O(log n).

---

## 🔹 Java Code (Two-pass Binary Search with findLeft and findRight)

```java
public class FirstLastPosition {

    public static int[] searchRange(int[] nums, int target) {
        int[] result = new int[]{-1, -1};
        
        if (nums == null || nums.length == 0) {
            return result;
        }
        
        // Find first (leftmost) occurrence
        int first = findLeft(nums, target);
        
        // If target not found, return [-1, -1]
        if (first == -1) {
            return result;
        }
        
        // Find last (rightmost) occurrence
        int last = findRight(nums, target);
        
        result[0] = first;
        result[1] = last;
        
        return result;
    }
    
    // Helper method to find leftmost (first) occurrence
    private static int findLeft(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (nums[mid] == target) {
                result = mid;           // Found target, but keep searching left
                right = mid - 1;        // Continue searching in left half
            } else if (nums[mid] < target) {
                left = mid + 1;         // Search in right half
            } else {
                right = mid - 1;        // Search in left half
            }
        }
        
        return result;
    }
    
    // Helper method to find rightmost (last) occurrence
    private static int findRight(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (nums[mid] == target) {
                result = mid;           // Found target, but keep searching right
                left = mid + 1;         // Continue searching in right half
            } else if (nums[mid] < target) {
                left = mid + 1;         // Search in right half
            } else {
                right = mid - 1;        // Search in left half
            }
        }
        
        return result;
    }

    public static void main(String[] args) {
        // Test Case 1
        int[] nums1 = {5, 7, 7, 8, 8, 10};
        int target1 = 8;
        int[] result1 = searchRange(nums1, target1);
        System.out.println("Input: [5,7,7,8,8,10], target = 8");
        System.out.println("Output: [" + result1[0] + ", " + result1[1] + "]");
        System.out.println("Expected: [3, 4]\n");

        // Test Case 2
        int[] nums2 = {5, 7, 7, 8, 8, 10};
        int target2 = 6;
        int[] result2 = searchRange(nums2, target2);
        System.out.println("Input: [5,7,7,8,8,10], target = 6");
        System.out.println("Output: [" + result2[0] + ", " + result2[1] + "]");
        System.out.println("Expected: [-1, -1]\n");

        // Test Case 3
        int[] nums3 = {};
        int target3 = 0;
        int[] result3 = searchRange(nums3, target3);
        System.out.println("Input: [], target = 0");
        System.out.println("Output: [" + result3[0] + ", " + result3[1] + "]");
        System.out.println("Expected: [-1, -1]\n");

        // Test Case 4: All elements are target
        int[] nums4 = {1, 1, 1, 1, 1};
        int target4 = 1;
        int[] result4 = searchRange(nums4, target4);
        System.out.println("Input: [1,1,1,1,1], target = 1");
        System.out.println("Output: [" + result4[0] + ", " + result4[1] + "]");
        System.out.println("Expected: [0, 4]\n");

        // Test Case 5: Single element
        int[] nums5 = {1};
        int target5 = 1;
        int[] result5 = searchRange(nums5, target5);
        System.out.println("Input: [1], target = 1");
        System.out.println("Output: [" + result5[0] + ", " + result5[1] + "]");
        System.out.println("Expected: [0, 0]\n");
    }
}
```

---

## 🔹 Complexity Analysis

| Approach               | Time Complexity | Space Complexity | Remarks |
|------------------------|----------------|-----------------|---------|
| Two-pass Binary Search  | O(log n)       | O(1)            | Classic approach |
| One-pass Binary Search  | O(log n)       | O(1)            | Slightly more complex |

---

## 🔹 Edge Cases
- **Empty array** → return `[-1, -1]`
- **Target not present** → return `[-1, -1]`
- **All elements are target** → return `[0, n-1]`
- **Single element array** → check if equals target
- **Array with duplicates** → correctly finds first and last positions

---

## 🔹 Follow-Up Questions
1. Can you implement using **recursion** instead of iteration?
2. Can you find first and last positions **in one pass**?
3. How would this change for **unsorted arrays**?
4. Can you generalize this for **floating point numbers** with precision issues?
5. Can you find **k-th occurrence** of the target efficiently?
