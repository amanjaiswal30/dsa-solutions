/**
 * Enum representing the type of player
 */
public enum PlayerType {
    HUMAN("Human"),
    COMPUTER("Computer/AI");

    private final String description;

    PlayerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
