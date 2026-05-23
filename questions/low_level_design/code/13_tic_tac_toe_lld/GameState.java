/**
 * Enum representing the different states of the game
 */
public enum GameState {
    NOT_STARTED("Game has not started"),
    IN_PROGRESS("Game is in progress"),
    WIN("A player has won"),
    DRAW("Game is a draw");

    private final String description;

    GameState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
