/**
 * Represents the Tic Tac Toe game board
 * Manages board state and cell operations
 */
public class Board {
    private final int size;
    private final Cell[][] cells;

    /**
     * Constructor for Board with default size 3x3
     */
    public Board() {
        this(3);
    }

    /**
     * Constructor for Board with custom size
     * @param size The size of the board (e.g., 3 for 3x3 board)
     */
    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size][size];
        initializeBoard();
    }

    /**
     * Initialize the board with empty cells
     */
    private void initializeBoard() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    /**
     * Get the size of the board
     * @return Board size
     */
    public int getSize() {
        return size;
    }

    /**
     * Make a move on the board by placing a symbol at given position
     * @param row Row index
     * @param col Column index
     * @param symbol The symbol to place
     * @return true if move was successful, false otherwise
     * @throws IllegalArgumentException if move is invalid
     */
    public boolean makeMove(int row, int col, Symbol symbol) {
        if (!isValidMove(row, col)) {
            throw new IllegalArgumentException("Invalid move at (" + row + ", " + col + ")");
        }
        cells[row][col].setSymbol(symbol);
        return true;
    }

    /**
     * Check if a move is valid (within bounds and cell is empty)
     * @param row Row index
     * @param col Column index
     * @return true if move is valid, false otherwise
     */
    public boolean isValidMove(int row, int col) {
        // Check bounds
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        // Check if cell is empty
        return cells[row][col].isEmpty();
    }

    /**
     * Get a cell at given position
     * @param row Row index
     * @param col Column index
     * @return The Cell object
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException("Position (" + row + ", " + col + ") is out of bounds");
        }
        return cells[row][col];
    }

    /**
     * Check if the board is full
     * @return true if all cells are filled, false otherwise
     */
    public boolean isBoardFull() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (cells[row][col].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Count empty cells on the board
     * @return Number of empty cells
     */
    public int getEmptyCellsCount() {
        int count = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (cells[row][col].isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Reset the board to initial state (all cells empty)
     */
    public void reset() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cells[row][col].reset();
            }
        }
    }

    /**
     * Get the 2D array of cells
     * @return The cells array
     */
    public Cell[][] getCells() {
        return cells;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                sb.append(" ").append(cells[row][col].getSymbol().getValue()).append(" ");
                if (col < size - 1) {
                    sb.append("|");
                }
            }
            sb.append("\n");
            if (row < size - 1) {
                sb.append("-----------\n");
            }
        }
        return sb.toString();
    }
}
