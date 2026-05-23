import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Transaction {
    private final Product product;
    private final List<Money> insertedMoney = new ArrayList<>();
    private TransactionStatus status = TransactionStatus.INITIATED;

    public Transaction(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public List<Money> getInsertedMoney() {
        return Collections.unmodifiableList(insertedMoney);
    }

    public int getInsertedAmount() {
        int total = 0;
        for (Money money : insertedMoney) {
            total += money.getDenominationInCents();
        }
        return total;
    }

    public void addMoney(Money money) {
        insertedMoney.add(money);
        status = TransactionStatus.WAITING_FOR_PAYMENT;
    }

    public void markReadyToDispense() {
        status = TransactionStatus.READY_TO_DISPENSE;
    }

    public void markCompleted() {
        status = TransactionStatus.COMPLETED;
    }

    public void markRefunded() {
        status = TransactionStatus.REFUNDED;
    }

    public void markFailed() {
        status = TransactionStatus.FAILED;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}
