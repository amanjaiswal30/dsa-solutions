# 🔹 Problem: Tic Tac Toe Winner Detection

**Difficulty:** Easy ⚡

---

## 🔹 Problem Statement
You are given a 3x3 Tic Tac Toe board represented as a 2D character array `board`, where each cell contains:
- `'X'` → Player X's move
- `'O'` → Player O's move
- `' '` (space or empty) → Empty cell

Your task is to **determine the winner** of the game.

Return:
- `"X"` if Player X has won
- `"O"` if Player O has won
- `"Draw"` if all cells are filled and no winner
- `"Pending"` if the game is not yet finished (some empty cells remain and no winner yet)

---

## 🔹 Intuition
A Tic Tac Toe winner occurs when **three same symbols** (`X` or `O`) appear:
- In **any row**
- In **any column**
- In **either of the two diagonals**

We just need to check all 8 possible winning lines.

---

## 🔹 Approaches

### 1. Brute Force
- Check every row, column, and diagonal manually.
- If any has all `'X'` → X wins.
- If any has all `'O'` → O wins.
- Else, if no empty cells → Draw.
- Else → Pending.

**Time Complexity:** O(1) (since board is fixed 3x3)  
**Space Complexity:** O(1)

---

### 2. Generic N x N Winner Check (Scalable)
- Works for any `n x n` Tic Tac Toe.
- For each row & column, maintain count of X’s and O’s.
- Track both diagonals.
- If any count reaches `n` or `-n`, we found a winner.

**Time Complexity:** O(n²)  
**Space Complexity:** O(n)

---

## 🔹 Java Code (Both Approaches)

```java
import java.util.*;

public class TicTacToeWinner {

    // 1. Simple 3x3 Brute Force Check
    public static String findWinner(char[][] board) {
        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            // Row check
            if (board[i][0] != ' ' &&
                board[i][0] == board[i][1] && board[i][1] == board[i][2])
                return String.valueOf(board[i][0]);

            // Column check
            if (board[0][i] != ' ' &&
                board[0][i] == board[1][i] && board[1][i] == board[2][i])
                return String.valueOf(board[0][i]);
        }

        // Diagonal check
        if (board[0][0] != ' ' &&
            board[0][0] == board[1][1] && board[1][1] == board[2][2])
            return String.valueOf(board[0][0]);

        // Anti-diagonal check
        if (board[0][2] != ' ' &&
            board[0][2] == board[1][1] && board[1][1] == board[2][0])
            return String.valueOf(board[0][2]);

        // Check for empty spaces
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[i][j] == ' ')
                    return "Pending";

        return "Draw";
    }

    // 2. Generic N x N Tic Tac Toe
    public static String findWinnerNbyN(char[][] board) {
        int n = board.length;
        int[] rows = new int[n];
        int[] cols = new int[n];
        int diag = 0, antiDiag = 0;
        boolean emptyCell = false;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                char c = board[i][j];
                if (c == ' ') {
                    emptyCell = true;
                    continue;
                }

                int val = (c == 'X') ? 1 : -1;

                rows[i] += val;
                cols[j] += val;
                if (i == j) diag += val;
                if (i + j == n - 1) antiDiag += val;

                if (rows[i] == n || cols[j] == n || diag == n || antiDiag == n)
                    return "X";
                if (rows[i] == -n || cols[j] == -n || diag == -n || antiDiag == -n)
                    return "O";
            }
        }

        return emptyCell ? "Pending" : "Draw";
    }
}
```

---

## 🔹 Complexity Analysis

| Approach                    | Time Complexity | Space Complexity | Scalable to N x N |
|-----------------------------|-----------------|------------------|-------------------|
| Brute Force (3x3 only)      | O(1)            | O(1)             | ❌                 |
| Generic Counter-Based Check | O(n²)           | O(n)             | ✅                 |

---

## 🔹 Edge Cases
- All cells empty → `"Pending"`
- Full board, no winner → `"Draw"`
- Early win (row of X’s) → `"X"`
- Partial filled, no winner yet → `"Pending"`

---

## 🔹 Example

**Input:**

X O X

O X O

O X X


**Output:**

X

**Explanation:**  
Player `X` wins diagonally: `(0,0), (1,1), (2,2)`

---

## 🔹 Follow-Up Questions
1. How would you modify this to detect **the earliest move** when a player wins?
2. How would you design a **Tic Tac Toe class** with a `move(int row, int col, char player)` method?
3. How would you implement an **AI opponent** (e.g., using the Minimax algorithm)?


