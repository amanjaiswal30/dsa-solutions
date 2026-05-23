public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final String fromWalletId;
    private final String toWalletId;
    private final double amount;
    private final TransactionStatus status;
    private final String note;
    private final long timestamp;

    public Transaction(String transactionId,
                       TransactionType type,
                       String fromWalletId,
                       String toWalletId,
                       double amount,
                       TransactionStatus status,
                       String note,
                       long timestamp) {
        this.transactionId = transactionId;
        this.type = type;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.status = status;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public String getFromWalletId() {
        return fromWalletId;
    }

    public String getToWalletId() {
        return toWalletId;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
