import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankAccount {
    private final String accountNumber;
    private final String customerId;
    private double balance;
    private final List<TransactionRecord> history = new ArrayList<>();

    public BankAccount(String accountNumber, String customerId, double openingBalance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = openingBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getBalance() {
        return balance;
    }

    public void debit(double amount, String message) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (balance < amount) {
            throw new IllegalStateException("Insufficient account balance");
        }
        balance -= amount;
        history.add(new TransactionRecord(TransactionType.WITHDRAWAL, amount, message));
    }


    public void addRecord(TransactionType type, String message) {
        history.add(new TransactionRecord(type, 0, message));
    }

    public List<TransactionRecord> getMiniStatement(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        int fromIndex = Math.max(0, history.size() - count);
        return Collections.unmodifiableList(history.subList(fromIndex, history.size()));
    }
}
