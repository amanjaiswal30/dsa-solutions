import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Group {
    private final String groupId;
    private final String groupName;
    private final List<User> users;
    private final List<Expense> expenses;

    public Group(String groupName, List<User> users) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("groupName cannot be empty.");
        }
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("Group must have at least one user.");
        }

        this.groupId = UUID.randomUUID().toString();
        this.groupName = groupName;
        this.users = new ArrayList<>(users);
        this.expenses = new ArrayList<>();

        for (User user : this.users) {
            if (user == null) {
                throw new IllegalArgumentException("Group contains null user.");
            }
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public boolean containsUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        for (User user : users) {
            if (userId.equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    public void addExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("expense cannot be null.");
        }
        expenses.add(expense);
    }
}
