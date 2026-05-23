import java.time.LocalDateTime;

public class TransactionRecord {
    private final TransactionType type;
    private final double amount;
    private final String message;
    private final LocalDateTime timestamp;

    public TransactionRecord(TransactionType type, double amount, String message) {
        this.type = type;
        this.amount = amount;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return timestamp + " | " + type + " | amount=" + amount + " | " + message;
    }
}
