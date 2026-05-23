import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Expense {
    private final String expenseId;
    private final int amount;
    private final User paidByUser;
    private final String description;
    private final List<Split> splitList;

    public Expense(int amount, User paidByUser, String description, List<Split> splitList) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be > 0.");
        }
        if (paidByUser == null) {
            throw new IllegalArgumentException("paidByUser cannot be null.");
        }
        if (splitList == null || splitList.isEmpty()) {
            throw new IllegalArgumentException("splitList cannot be empty.");
        }

        this.expenseId = UUID.randomUUID().toString();
        this.amount = amount;
        this.paidByUser = paidByUser;
        this.description = (description == null) ? "" : description;
        this.splitList = new ArrayList<>(splitList);
    }

    public String getExpenseId() {
        return expenseId;
    }

    public int getAmount() {
        return amount;
    }

    public User getPaidByUser() {
        return paidByUser;
    }

    public String getDescription() {
        return description;
    }

    public List<Split> getSplitList() {
        return Collections.unmodifiableList(splitList);
    }
}
