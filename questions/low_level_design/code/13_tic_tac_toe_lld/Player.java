/**
 * Represents a player in the Tic Tac Toe game
 */
public class Player {
    private final String id;
    private final Symbol symbol;
    private final PlayerType playerType;

    /**
     * Constructor for Player
     * @param id Unique identifier for the player
     * @param symbol The symbol for this player (X or O)
     * @param playerType Type of player (HUMAN or COMPUTER)
     */
    public Player(String id, Symbol symbol, PlayerType playerType) {
        if (symbol == Symbol.EMPTY) {
            throw new IllegalArgumentException("Player cannot have EMPTY symbol");
        }
        this.id = id;
        this.symbol = symbol;
        this.playerType = playerType;
    }

    /**
     * Get the player's unique identifier
     * @return Player ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the player's symbol
     * @return Player's symbol (X or O)
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Get the player's type
     * @return Player type (HUMAN or COMPUTER)
     */
    public PlayerType getPlayerType() {
        return playerType;
    }

    /**
     * Check if this player is a human player
     * @return true if player is human, false otherwise
     */
    public boolean isHuman() {
        return playerType == PlayerType.HUMAN;
    }

    /**
     * Check if this player is a computer player
     * @return true if player is computer, false otherwise
     */
    public boolean isComputer() {
        return playerType == PlayerType.COMPUTER;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", symbol=" + symbol +
                ", playerType=" + playerType +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return id.equals(player.id) && symbol == player.symbol;
    }

    @Override
    public int hashCode() {
        return id.hashCode() * 31 + symbol.hashCode();
    }
}
