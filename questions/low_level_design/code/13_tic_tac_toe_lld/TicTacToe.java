import java.util.Scanner;

/**
 * Main class to run the Tic Tac Toe game
 * Provides an interactive command-line interface for playing the game
 */
public class TicTacToe {

    public static void main(String[] args) {
        System.out.println("========== Tic Tac Toe Game ==========");

        // Create players
        Player playerX = new Player("Player 1", Symbol.X, PlayerType.HUMAN);
        Player playerO = new Player("Player 2", Symbol.O, PlayerType.HUMAN);

        // Create game
        Game game = new Game(playerX, playerO);

        // Start game
        game.startGame();

        // Display initial board
        game.displayBoard();

        // Game loop
        playGame(game);

        System.out.println("Thanks for playing!");
    }

    /**
     * Main game loop - handles user input and game flow
     * @param game The game instance
     */
    private static void playGame(Game game) {
        Scanner scanner = new Scanner(System.in);

        while (!game.isGameOver()) {
            game.displayGameInfo();

            // Get player input
            int row = -1;
            int col = -1;
            boolean validInput = false;

            while (!validInput) {
                try {
                    System.out.print(game.getCurrentPlayer().getId() + ", enter row (0-" + (game.getBoard().getSize() - 1) + "): ");
                    row = scanner.nextInt();

                    System.out.print(game.getCurrentPlayer().getId() + ", enter column (0-" + (game.getBoard().getSize() - 1) + "): ");
                    col = scanner.nextInt();

                    // Validate input bounds
                    if (row < 0 || row >= game.getBoard().getSize() || col < 0 || col >= game.getBoard().getSize()) {
                        System.out.println("Invalid input! Please enter values between 0 and " + (game.getBoard().getSize() - 1) + ".");
                        continue;
                    }

                    validInput = true;
                } catch (Exception e) {
                    System.out.println("Invalid input! Please enter integers only.");
                    scanner.nextLine(); // Clear the input buffer
                }
            }

            // Play the move
            boolean moveSuccess = game.playMove(row, col);

            if (moveSuccess) {
                game.displayBoard();
            } else {
                System.out.println("Move failed. Try again.\n");
            }
        }

        // Game over
        scanner.close();
        displayGameResult(game);

        // Ask to play again
        playAgain(game);
    }

    /**
     * Display the final game result
     * @param game The game instance
     */
    private static void displayGameResult(Game game) {
        System.out.println("\n========== Game Over ==========");
        if (game.getGameState() == GameState.WIN) {
            System.out.println("🎉 " + game.getWinner().getId() + " (" + game.getWinner().getSymbol() + ") wins! 🎉");
        } else if (game.getGameState() == GameState.DRAW) {
            System.out.println("🤝 The game is a draw! 🤝");
        }
        System.out.println("Final Board:");
        game.displayBoard();
        System.out.println("==============================\n");
    }

    /**
     * Ask user if they want to play again
     * @param game The game instance
     */
    private static void playAgain(Game game) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Do you want to play again? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("yes") || response.equals("y")) {
            game.resetGame();
            game.displayBoard();
            playGame(game);
        } else {
            System.out.println("Goodbye!");
        }

        scanner.close();
    }
}
