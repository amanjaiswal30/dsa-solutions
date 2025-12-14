# Fibonacci Number – Detailed Explanation and Approaches

**Difficulty:** Easy ✅

---

## 🔹 Problem Statement

The **Fibonacci numbers**, commonly denoted `F(n)`, form a sequence called the **Fibonacci sequence**, such that each number is the sum of the two preceding ones, starting from `0` and `1`.

That is:
```
F(0) = 0, F(1) = 1
F(n) = F(n - 1) + F(n - 2), for n > 1
```

Given `n`, calculate `F(n)`.

---

## 🔹 Examples

**Example 1:**  
Input: `n = 2`  
Output: `1`  
Explanation: F(2) = F(1) + F(0) = 1 + 0 = 1

**Example 2:**  
Input: `n = 3`  
Output: `2`  
Explanation: F(3) = F(2) + F(1) = 1 + 1 = 2

**Example 3:**  
Input: `n = 4`  
Output: `3`  
Explanation: F(4) = F(3) + F(2) = 2 + 1 = 3

**Example 4:**  
Input: `n = 10`  
Output: `55`  
Explanation: The 10th Fibonacci number is 55

---

## 🔹 Constraints
- `0 <= n <= 30`

---

## 🔹 Core Intuition

The Fibonacci sequence is defined recursively:
- **Base cases:** F(0) = 0, F(1) = 1
- **Recurrence relation:** F(n) = F(n-1) + F(n-2)

This is the **classic example** to introduce Dynamic Programming concepts:
1. **Overlapping Subproblems** - Same values computed multiple times
2. **Optimal Substructure** - Solution built from smaller subproblems

---

## 🔹 Fibonacci Sequence

| n | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|---|---|-----|
| F(n) | 0 | 1 | 1 | 2 | 3 | 5 | 8 | 13 | 21 | 34 | 55 |

---

## 1️⃣ Recursive Approach (Brute Force)

### Explanation
Direct implementation of the mathematical definition.  
For each `F(n)`, recursively compute `F(n-1)` and `F(n-2)`, then add them.

**Base cases:**
- `F(0) = 0`
- `F(1) = 1`

### Code (Java)

```java
public class FibonacciNumber {
    
    // Approach 1: Recursive (Brute Force)
    public int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }
}
```

### Drawbacks
- **Time Complexity:** O(2^n) — exponential growth, extremely slow
- Each call spawns two more recursive calls
- **Massive redundant computation** of same values
- **Space Complexity:** O(n) due to recursion call stack depth

### Visualization - Recursion Tree for F(5)
```
                    fib(5)
                   /      \
              fib(4)        fib(3)
             /     \        /     \
        fib(3)   fib(2)  fib(2)  fib(1)
        /    \   /    \  /    \
    fib(2) fib(1) fib(1) fib(0) fib(1) fib(0)
    /   \
fib(1) fib(0)

Notice: fib(3) computed 2 times, fib(2) computed 3 times!
```

### Time Complexity Analysis
For F(n), the number of recursive calls grows exponentially:
- F(5): ~15 calls
- F(10): ~177 calls
- F(20): ~21,891 calls
- F(30): ~2,692,537 calls 😱

---

## 2️⃣ Recursive with Memoization (Top-Down DP)

### Explanation
Cache computed results in a `memo` array to avoid recomputation.  
Before computing `F(n)`, check if it's already in the cache.

This transforms the exponential algorithm into linear time!

### Code (Java)

```java
public class FibonacciNumber {
    
    // Approach 2: Memoization (Top-Down DP)
    public int fib(int n) {
        int[] memo = new int[n + 1];
        return fibMemo(n, memo);
    }
    
    private int fibMemo(int n, int[] memo) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        if (memo[n] != 0) {
            return memo[n];  // Return cached result
        }
        
        memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        return memo[n];
    }
}
```

### Complexity
- **Time Complexity:** O(n) — each F(i) computed exactly once
- **Space Complexity:** O(n) for memo array + O(n) for recursion stack = O(n)

### Memoization Table Example (n=6)
| Index | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
|-------|---|---|---|---|---|---|---|
| memo[i] | 0 | 1 | 1 | 2 | 3 | 5 | 8 |

After computing F(6), all previous values are cached!

---

## 3️⃣ Dynamic Programming – Bottom-Up Approach (Tabulation)

### Explanation
Build solution **iteratively** from bottom up.  
Start with base cases and compute each Fibonacci number in sequence.

No recursion needed - pure iteration!

**Formula:**
```
dp[i] = dp[i-1] + dp[i-2]
```

**Initialization:**
```
dp[0] = 0
dp[1] = 1
```

### Code (Java)

```java
public class FibonacciNumber {
    
    // Approach 3: Bottom-Up DP (Tabulation)
    public int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        
        return dp[n];
    }
}
```

### Complexity
- **Time Complexity:** O(n)
- **Space Complexity:** O(n) for dp array

### DP Table Example (n=10)
| i | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|---|---|-----|
| dp[i] | 0 | 1 | 1 | 2 | 3 | 5 | 8 | 13 | 21 | 34 | 55 |

---

## 4️⃣ Dynamic Programming – Space Optimized ⭐

### Explanation
**Key Observation:** To compute F(n), we only need F(n-1) and F(n-2).  
No need to store entire array!

Use just **two variables** to track the last two Fibonacci numbers.

**This is the OPTIMAL solution!**

### Code (Java)

```java
public class FibonacciNumber {
    
    // Approach 4: Space Optimized DP (OPTIMAL)
    public int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int prev2 = 0;  // F(0)
        int prev1 = 1;  // F(1)
        int current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
}
```

### Complexity
- **Time Complexity:** O(n)
- **Space Complexity:** O(1) ⭐ **Constant space!**

### Evolution of Variables (n=6)
| Iteration | i | prev2 | prev1 | current |
|-----------|---|-------|-------|---------|
| Initial | - | 0 | 1 | 0 |
| Loop 1 | 2 | 1 | 1 | 1 |
| Loop 2 | 3 | 1 | 2 | 2 |
| Loop 3 | 4 | 2 | 3 | 3 |
| Loop 4 | 5 | 3 | 5 | 5 |
| Loop 5 | 6 | 5 | 8 | **8** |

---

## 5️⃣ Matrix Exponentiation (Advanced) 🚀

### Explanation
Use **matrix multiplication** to compute Fibonacci in **O(log n)** time!

The Fibonacci recurrence can be represented as:
```
[F(n+1)]   [1  1]^n   [1]
[F(n)  ] = [1  0]   × [0]
```

Using **fast matrix exponentiation** (like binary exponentiation), we can compute this in O(log n).

### Code (Java)

{% raw %}
```java
public class FibonacciNumber {
    
    // Approach 5: Matrix Exponentiation
    public int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int[][] result = matrixPower(new int[][]{{1, 1}, {1, 0}}, n - 1);
        return result[0][0];
    }
    
    private int[][] matrixPower(int[][] matrix, int n) {
        if (n == 1) {
            return matrix;
        }
        
        int[][] half = matrixPower(matrix, n / 2);
        int[][] result = matrixMultiply(half, half);
        
        if (n % 2 != 0) {
            result = matrixMultiply(result, matrix);
        }
        
        return result;
    }
    
    private int[][] matrixMultiply(int[][] a, int[][] b) {
        int[][] result = new int[2][2];
        result[0][0] = a[0][0] * b[0][0] + a[0][1] * b[1][0];
        result[0][1] = a[0][0] * b[0][1] + a[0][1] * b[1][1];
        result[1][0] = a[1][0] * b[0][0] + a[1][1] * b[1][0];
        result[1][1] = a[1][0] * b[0][1] + a[1][1] * b[1][1];
        return result;
    }
}
```
{% endraw %}

### Complexity
- **Time Complexity:** O(log n) ⚡
- **Space Complexity:** O(log n) for recursion stack

---

## 6️⃣ Binet's Formula (Mathematical) 📐

### Explanation
**Closed-form mathematical formula** to compute Fibonacci directly!

```
F(n) = (φ^n - ψ^n) / √5
```

Where:
- φ (phi) = (1 + √5) / 2 ≈ 1.618 (Golden Ratio)
- ψ (psi) = (1 - √5) / 2 ≈ -0.618

### Code (Java)

```java
public class FibonacciNumber {
    
    // Approach 6: Binet's Formula (Mathematical)
    public int fib(int n) {
        double phi = (1 + Math.sqrt(5)) / 2;
        double psi = (1 - Math.sqrt(5)) / 2;
        
        return (int) Math.round((Math.pow(phi, n) - Math.pow(psi, n)) / Math.sqrt(5));
    }
}
```

### Complexity
- **Time Complexity:** O(1) ⚡⚡ **Constant time!**
- **Space Complexity:** O(1)

### Limitations
- **Floating-point precision issues** for large n
- Not suitable for very large Fibonacci numbers
- Works well for n ≤ 30 (problem constraint)

---

## 📊 Complexity Comparison

| Approach | Time | Space | Recommended | Notes |
|----------|------|-------|-------------|-------|
| Recursive (Brute Force) | O(2^n) | O(n) | ❌ Never | Exponentially slow |
| Memoization (Top-Down) | O(n) | O(n) | ✅ Good | Easy to implement |
| Tabulation (Bottom-Up) | O(n) | O(n) | ✅ Good | No recursion overhead |
| Space Optimized | O(n) | O(1) | ⭐ Best | Most practical |
| Matrix Exponentiation | O(log n) | O(log n) | ⚡ Fast | For very large n |
| Binet's Formula | O(1) | O(1) | ⚡⚡ Fastest | Precision issues |

---

## 🎯 Extra Insights

### The Golden Ratio Connection
The ratio of consecutive Fibonacci numbers approaches the **Golden Ratio (φ)**:
```
F(n+1) / F(n) → φ ≈ 1.618033988749...
```

**Examples:**
- F(10)/F(9) = 55/34 ≈ 1.6176
- F(20)/F(19) = 6765/4181 ≈ 1.6180
- F(30)/F(29) = 832040/514229 ≈ 1.6180339

### Applications of Fibonacci Numbers
1. **Algorithm Analysis** - Worst-case scenarios
2. **Nature** - Spirals in shells, flower petals, pine cones
3. **Financial Markets** - Fibonacci retracement levels
4. **Computer Science** - Fibonacci heap data structure
5. **Art & Architecture** - Golden ratio in design

### Fundamental DP Lessons
This problem teaches:
1. **Recognizing Overlapping Subproblems** - Key to applying DP
2. **Memoization vs Tabulation** - Top-down vs bottom-up
3. **Space Optimization** - Reducing memory usage
4. **Alternative Solutions** - Matrix exponentiation, closed-form formulas

---

## 🔄 Additional Variations with Code

### Variation 1: Nth Tribonacci Number

**Problem:** Similar to Fibonacci, but sum of previous **three** numbers.
```
T(0) = 0, T(1) = 1, T(2) = 1
T(n) = T(n-1) + T(n-2) + T(n-3)
```

#### Code (Java)

```java
public class FibonacciVariations {
    
    // Variation 1: Tribonacci Number
    public int tribonacci(int n) {
        if (n == 0) return 0;
        if (n == 1 || n == 2) return 1;
        
        int prev3 = 0;  // T(0)
        int prev2 = 1;  // T(1)
        int prev1 = 1;  // T(2)
        int current = 0;
        
        for (int i = 3; i <= n; i++) {
            current = prev1 + prev2 + prev3;
            prev3 = prev2;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
    
    // With DP array
    public int tribonacciDP(int n) {
        if (n == 0) return 0;
        if (n == 1 || n == 2) return 1;
        
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        dp[2] = 1;
        
        for (int i = 3; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2] + dp[i - 3];
        }
        
        return dp[n];
    }
}
```

**Sequence:** 0, 1, 1, 2, 4, 7, 13, 24, 44, 81, 149...

**Complexity:**
- Time: O(n)
- Space: O(1) optimized, O(n) with DP array

---

### Variation 2: Fibonacci with Modulo

**Problem:** Return F(n) modulo a number (to prevent overflow for large n).

#### Code (Java)

```java
public class FibonacciVariations {
    
    // Variation 2: Fibonacci with Modulo
    public int fibMod(int n, int mod) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        long prev2 = 0;
        long prev1 = 1;
        long current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = (prev1 + prev2) % mod;
            prev2 = prev1;
            prev1 = current;
        }
        
        return (int) current;
    }
}
```

**Use Case:** Computing F(1000) % 1000000007 (common in competitive programming)

---

### Variation 3: Sum of First N Fibonacci Numbers

**Problem:** Find the sum of F(0) + F(1) + F(2) + ... + F(n).

**Mathematical Insight:** Sum(F(0) to F(n)) = F(n+2) - 1

#### Code (Java)

```java
public class FibonacciVariations {
    
    // Variation 3: Sum of First N Fibonacci Numbers
    public int fibSum(int n) {
        // Sum of F(0) to F(n) = F(n+2) - 1
        return fib(n + 2) - 1;
    }
    
    // Alternative: Direct computation
    public int fibSumDirect(int n) {
        if (n == 0) return 0;
        
        int prev2 = 0, prev1 = 1;
        int sum = 1;  // F(0) + F(1)
        
        for (int i = 2; i <= n; i++) {
            int current = prev1 + prev2;
            sum += current;
            prev2 = prev1;
            prev1 = current;
        }
        
        return sum;
    }
}
```

**Example:** Sum(F(0) to F(5)) = 0+1+1+2+3+5 = 12 = F(7) - 1 = 13 - 1 ✓

---

### Variation 4: Fibonacci with Different Starting Values

**Problem:** Generalized Fibonacci with custom F(0) and F(1) values.

#### Code (Java)

```java
public class FibonacciVariations {
    
    // Variation 4: Generalized Fibonacci
    public int generalizedFib(int n, int a, int b) {
        // F(0) = a, F(1) = b
        // F(n) = F(n-1) + F(n-2)
        
        if (n == 0) return a;
        if (n == 1) return b;
        
        int prev2 = a;
        int prev1 = b;
        int current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
}
```

**Examples:**
- a=0, b=1 → Standard Fibonacci: 0, 1, 1, 2, 3, 5, 8...
- a=2, b=1 → Lucas Numbers: 2, 1, 3, 4, 7, 11, 18...
- a=0, b=2 → Even Fibonacci: 0, 2, 2, 4, 6, 10, 16...

---

### Variation 5: Nth Fibonacci Digit

**Problem:** Find the last digit (or last k digits) of F(n) for very large n.

#### Code (Java)

```java
public class FibonacciVariations {
    
    // Variation 5: Last Digit of Nth Fibonacci
    public int fibLastDigit(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int prev2 = 0;
        int prev1 = 1;
        int current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = (prev1 + prev2) % 10;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
    
    // Last K Digits using Pisano Period (Advanced)
    public long fibLastKDigits(int n, int k) {
        long mod = (long) Math.pow(10, k);
        
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        long prev2 = 0;
        long prev1 = 1;
        long current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = (prev1 + prev2) % mod;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
}
```

**Use Case:** F(10000) is huge, but last digit follows pattern (Pisano Period = 60)

---

### Variation 6: Check if Number is Fibonacci

**Problem:** Given a number, check if it's a Fibonacci number.

**Mathematical Property:** A number n is Fibonacci if and only if one of (5×n² + 4) or (5×n² - 4) is a perfect square.

#### Code (Java)

```java
public class FibonacciVariations {
    
    // Variation 6: Check if Number is Fibonacci
    public boolean isFibonacci(int num) {
        // A number is Fibonacci if one of these is a perfect square:
        // 5*n^2 + 4 or 5*n^2 - 4
        
        return isPerfectSquare(5 * num * num + 4) || 
               isPerfectSquare(5 * num * num - 4);
    }
    
    private boolean isPerfectSquare(long n) {
        long sqrt = (long) Math.sqrt(n);
        return sqrt * sqrt == n;
    }
    
    // Alternative: Generate and Check
    public boolean isFibonacciGenerate(int num) {
        if (num < 0) return false;
        if (num == 0 || num == 1) return true;
        
        int prev2 = 0, prev1 = 1;
        
        while (prev1 < num) {
            int current = prev1 + prev2;
            if (current == num) return true;
            prev2 = prev1;
            prev1 = current;
        }
        
        return prev1 == num;
    }
}
```

**Examples:**
- 8 is Fibonacci ✓ (5×64+4 = 324 = 18²)
- 10 is not Fibonacci ✗

---

## 🎓 Complete Implementation with All Approaches

```java
import java.util.*;

public class FibonacciComplete {
    
    // ==================== MAIN PROBLEM ====================
    
    // 1. Recursive (Brute Force)
    public int fibRecursive(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fibRecursive(n - 1) + fibRecursive(n - 2);
    }
    
    // 2. Memoization (Top-Down DP)
    public int fibMemo(int n) {
        int[] memo = new int[n + 1];
        return fibMemoHelper(n, memo);
    }
    
    private int fibMemoHelper(int n, int[] memo) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        if (memo[n] != 0) return memo[n];
        memo[n] = fibMemoHelper(n - 1, memo) + fibMemoHelper(n - 2, memo);
        return memo[n];
    }
    
    // 3. Tabulation (Bottom-Up DP)
    public int fibDP(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        
        return dp[n];
    }
    
    // 4. Space Optimized (BEST for practical use)
    public int fibOptimized(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int prev2 = 0, prev1 = 1, current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
    
    // 5. Matrix Exponentiation (O(log n))
    public int fibMatrix(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        int[][] result = matrixPower(new int[][]{{1, 1}, {1, 0}}, n - 1);
        return result[0][0];
    }
    
    private int[][] matrixPower(int[][] matrix, int n) {
        if (n == 1) return matrix;
        
        int[][] half = matrixPower(matrix, n / 2);
        int[][] result = matrixMultiply(half, half);
        
        if (n % 2 != 0) {
            result = matrixMultiply(result, matrix);
        }
        
        return result;
    }
    
    private int[][] matrixMultiply(int[][] a, int[][] b) {
        int[][] result = new int[2][2];
        result[0][0] = a[0][0] * b[0][0] + a[0][1] * b[1][0];
        result[0][1] = a[0][0] * b[0][1] + a[0][1] * b[1][1];
        result[1][0] = a[1][0] * b[0][0] + a[1][1] * b[1][0];
        result[1][1] = a[1][0] * b[0][1] + a[1][1] * b[1][1];
        return result;
    }
    
    // 6. Binet's Formula (O(1))
    public int fibBinet(int n) {
        double phi = (1 + Math.sqrt(5)) / 2;
        double psi = (1 - Math.sqrt(5)) / 2;
        return (int) Math.round((Math.pow(phi, n) - Math.pow(psi, n)) / Math.sqrt(5));
    }
    
    // ==================== VARIATIONS ====================
    // (All variation codes from above sections)
    
    public static void main(String[] args) {
        FibonacciComplete fib = new FibonacciComplete();
        
        System.out.println("=== Fibonacci Calculations ===");
        int n = 10;
        
        System.out.println("F(" + n + ") using different approaches:");
        System.out.println("Optimized: " + fib.fibOptimized(n));
        System.out.println("Matrix: " + fib.fibMatrix(n));
        System.out.println("Binet: " + fib.fibBinet(n));
        
        System.out.println("\n=== First 15 Fibonacci Numbers ===");
        for (int i = 0; i <= 15; i++) {
            System.out.print(fib.fibOptimized(i) + " ");
        }
        System.out.println();
    }
}
```

---

## 🎯 Key Takeaways

1. **Start Simple:** Understand the recursive definition first
2. **Optimize Gradually:** Recursion → Memoization → Tabulation → Space Optimization
3. **Know Alternatives:** Matrix exponentiation and Binet's formula for advanced scenarios
4. **Understand Trade-offs:** Time vs Space vs Precision
5. **Master Variations:** Tribonacci, modulo arithmetic, generalized sequences
6. **Real-World Applications:** Algorithm analysis, nature patterns, financial models

---

## 🌟 Performance Comparison for F(30)

| Approach | Approximate Time | Operations |
|----------|------------------|------------|
| Recursive | ~2 seconds | 2,692,537 calls |
| Memoization | ~0.00003 seconds | 30 calls |
| Tabulation | ~0.00003 seconds | 30 iterations |
| Space Optimized | ~0.00003 seconds | 30 iterations |
| Matrix Exponentiation | ~0.000005 seconds | ~5 operations |
| Binet's Formula | ~0.000001 seconds | 1 operation |

---

## 💡 When to Use Which Approach?

| Scenario | Best Approach | Reason |
|----------|---------------|--------|
| Learning DP | Memoization | Shows DP transition clearly |
| Production Code | Space Optimized | Best balance of speed and simplicity |
| Very Large n (n > 10^6) | Matrix Exponentiation | O(log n) time |
| Small n, need speed | Binet's Formula | O(1) constant time |
| Need exact large values | Space Optimized with BigInteger | Handles overflow |
| Competitive Programming | Space Optimized with Modulo | Fast and memory efficient |

---

This problem is the **quintessential example** of Dynamic Programming and serves as the foundation for understanding optimization techniques! 🚀
