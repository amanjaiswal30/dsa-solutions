/**
 * Represents a single cell on the Tic Tac Toe board
 */
public class Cell {
    private final int row;
    private final int col;
    private Symbol symbol;

    /**
     * Constructor for Cell
     * @param row Row index of the cell
     * @param col Column index of the cell
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.symbol = Symbol.EMPTY;
    }

    /**
     * Check if the cell is empty
     * @return true if cell is empty, false otherwise
     */
    public boolean isEmpty() {
        return symbol == Symbol.EMPTY;
    }

    /**
     * Set the symbol in the cell
     * @param symbol The symbol to place (X or O)
     * @throws IllegalArgumentException if trying to place symbol in non-empty cell
     */
    public void setSymbol(Symbol symbol) {
        if (!isEmpty()) {
            throw new IllegalArgumentException("Cell already occupied at (" + row + ", " + col + ")");
        }
        if (symbol == Symbol.EMPTY) {
            throw new IllegalArgumentException("Cannot place EMPTY symbol in a cell");
        }
        this.symbol = symbol;
    }

    /**
     * Get the symbol in the cell
     * @return The current symbol
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Get the row index
     * @return Row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the column index
     * @return Column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Reset the cell to empty
     */
    public void reset() {
        this.symbol = Symbol.EMPTY;
    }

    @Override
    public String toString() {
        return symbol.getValue();
    }
}
