/**
 * Enum representing the symbols that can be placed on the board
 */
public enum Symbol {
    X("X"),
    O("O"),
    EMPTY(" ");

    private final String value;

    Symbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Check if the symbol is not empty
     */
    public boolean isNotEmpty() {
        return this != EMPTY;
    }
}
