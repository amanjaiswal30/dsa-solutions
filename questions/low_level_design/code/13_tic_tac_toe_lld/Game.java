/**
 * Main game orchestrator for Tic Tac Toe
 * Manages game flow, players, board state, and win/draw conditions
 */
public class Game {
    private final Board board;
    private final Player playerX;
    private final Player playerO;
    private Player currentPlayer;
    private GameState gameState;
    private Player winner;

    /**
     * Constructor for Game with two players
     * @param playerX The player with X symbol
     * @param playerO The player with O symbol
     */
    public Game(Player playerX, Player playerO) {
        if (playerX.getSymbol() != Symbol.X || playerO.getSymbol() != Symbol.O) {
            throw new IllegalArgumentException("playerX must have X symbol and playerO must have O symbol");
        }
        this.board = new Board();
        this.playerX = playerX;
        this.playerO = playerO;
        this.gameState = GameState.NOT_STARTED;
        this.winner = null;
        this.currentPlayer = null;
    }

    /**
     * Constructor for Game with custom board size
     * @param playerX The player with X symbol
     * @param playerO The player with O symbol
     * @param boardSize The size of the board
     */
    public Game(Player playerX, Player playerO, int boardSize) {
        if (playerX.getSymbol() != Symbol.X || playerO.getSymbol() != Symbol.O) {
            throw new IllegalArgumentException("playerX must have X symbol and playerO must have O symbol");
        }
        this.board = new Board(boardSize);
        this.playerX = playerX;
        this.playerO = playerO;
        this.gameState = GameState.NOT_STARTED;
        this.winner = null;
        this.currentPlayer = null;
    }

    /**
     * Start the game
     */
    public void startGame() {
        this.currentPlayer = playerX; // X always goes first
        this.gameState = GameState.IN_PROGRESS;
        this.winner = null;
        System.out.println("Game started! Player X goes first.");
    }

    /**
     * Execute a move by the current player
     * @param row Row index
     * @param col Column index
     * @return true if move was successful, false otherwise
     */
    public boolean playMove(int row, int col) {
        if (gameState != GameState.IN_PROGRESS) {
            System.out.println("Game is not in progress. Current state: " + gameState);
            return false;
        }

        try {
            if (!board.isValidMove(row, col)) {
                System.out.println("Invalid move! Cell at (" + row + ", " + col + ") is either out of bounds or already occupied.");
                return false;
            }

            board.makeMove(row, col, currentPlayer.getSymbol());
            System.out.println(currentPlayer.getId() + " placed " + currentPlayer.getSymbol() + " at (" + row + ", " + col + ")");

            // Check for winner
            if (checkWinner()) {
                gameState = GameState.WIN;
                winner = currentPlayer;
                System.out.println(currentPlayer.getId() + " wins!");
                return true;
            }

            // Check for draw
            if (checkDraw()) {
                gameState = GameState.DRAW;
                System.out.println("Game is a draw!");
                return true;
            }

            // Switch player
            switchPlayer();
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current player has won
     * @return true if current player has won, false otherwise
     */
    private boolean checkWinner() {
        Symbol symbol = currentPlayer.getSymbol();
        int size = board.getSize();

        // Check rows
        for (int row = 0; row < size; row++) {
            boolean rowWin = true;
            for (int col = 0; col < size; col++) {
                if (board.getCell(row, col).getSymbol() != symbol) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) return true;
        }

        // Check columns
        for (int col = 0; col < size; col++) {
            boolean colWin = true;
            for (int row = 0; row < size; row++) {
                if (board.getCell(row, col).getSymbol() != symbol) {
                    colWin = false;
                    break;
                }
            }
            if (colWin) return true;
        }

        // Check main diagonal (top-left to bottom-right)
        boolean mainDiagWin = true;
        for (int i = 0; i < size; i++) {
            if (board.getCell(i, i).getSymbol() != symbol) {
                mainDiagWin = false;
                break;
            }
        }
        if (mainDiagWin) return true;

        // Check anti-diagonal (top-right to bottom-left)
        boolean antiDiagWin = true;
        for (int i = 0; i < size; i++) {
            if (board.getCell(i, size - 1 - i).getSymbol() != symbol) {
                antiDiagWin = false;
                break;
            }
        }
        if (antiDiagWin) return true;

        return false;
    }

    /**
     * Check if the game is a draw
     * @return true if board is full and no winner, false otherwise
     */
    private boolean checkDraw() {
        return board.isBoardFull() && winner == null;
    }

    /**
     * Switch to the other player
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == playerX) ? playerO : playerX;
    }

    /**
     * Get the current player
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Get the winner of the game
     * @return The winning player, or null if no winner
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Get the current game state
     * @return The current game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Get the board
     * @return The board instance
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Check if game is over
     * @return true if game is not in progress, false otherwise
     */
    public boolean isGameOver() {
        return gameState == GameState.WIN || gameState == GameState.DRAW;
    }

    /**
     * Reset the game for a new round
     */
    public void resetGame() {
        board.reset();
        currentPlayer = playerX;
        gameState = GameState.IN_PROGRESS;
        winner = null;
        System.out.println("Game has been reset.");
    }

    /**
     * Display the current board
     */
    public void displayBoard() {
        System.out.println("\n" + board.toString());
    }

    /**
     * Display game info
     */
    public void displayGameInfo() {
        System.out.println("========== Game Info ==========");
        System.out.println("Current Player: " + currentPlayer.getId() + " (" + currentPlayer.getSymbol() + ")");
        System.out.println("Game State: " + gameState.getDescription());
        if (winner != null) {
            System.out.println("Winner: " + winner.getId());
        }
        System.out.println("Empty Cells: " + board.getEmptyCellsCount());
        System.out.println("===============================\n");
    }

    /**
     * Get player X
     * @return Player X
     */
    public Player getPlayerX() {
        return playerX;
    }

    /**
     * Get player O
     * @return Player O
     */
    public Player getPlayerO() {
        return playerO;
    }
}
