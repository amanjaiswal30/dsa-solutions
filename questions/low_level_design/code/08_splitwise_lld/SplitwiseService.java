import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitwiseService {
    private static final SplitwiseService INSTANCE = new SplitwiseService();

    private final Map<String, User> userMap;
    private final Map<String, Group> groupMap;

    private SplitwiseService() {
        this.userMap = new HashMap<>();
        this.groupMap = new HashMap<>();
    }

    public static SplitwiseService getInstance() {
        return INSTANCE;
    }

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null.");
        }
        userMap.put(user.getUserId(), user);
    }

    public void addGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group cannot be null.");
        }

        for (User user : group.getUsers()) {
            if (!userMap.containsKey(user.getUserId())) {
                throw new IllegalArgumentException("All group users must be registered first.");
            }
        }

        groupMap.put(group.getGroupId(), group);
    }

    public Expense addExpense(
            String groupId,
            int amount,
            User paidByUser,
            List<User> participants,
            SplitStrategy splitStrategy,
            List<Integer> customValues,
            String description
    ) {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("groupId cannot be empty.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0.");
        }
        if (paidByUser == null) {
            throw new IllegalArgumentException("paidByUser cannot be null.");
        }
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("participants cannot be empty.");
        }
        if (splitStrategy == null) {
            throw new IllegalArgumentException("splitStrategy cannot be null.");
        }

        Group group = groupMap.get(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }

        if (!group.containsUser(paidByUser.getUserId())) {
            throw new IllegalArgumentException("Payer must be a member of the group.");
        }

        // Ensure every participant belongs to the target group.
        for (User user : participants) {
            if (user == null || !group.containsUser(user.getUserId())) {
                throw new IllegalArgumentException("All participants must be members of the group.");
            }
        }

        // 1) Compute per-user split using the selected strategy (equal/exact/percentage).
        List<Split> splitList = splitStrategy.split(amount, participants, customValues);

        // 2) Defensive check: total of all split shares must match expense amount exactly.
        int splitTotal = 0;
        for (Split split : splitList) {
            splitTotal += split.getAmount();
        }
        if (splitTotal != amount) {
            throw new IllegalArgumentException("Split total must equal expense amount.");
        }

        // 3) Persist the expense in group history before balance updates.
        Expense expense = new Expense(amount, paidByUser, description, splitList);
        group.addExpense(expense);

        // 4) Update bilateral balances:
        //    - payer gets +share against each participant
        //    - participant gets -share against payer
        //    Skip payer's own split entry.
        for (Split split : splitList) {
            User participant = split.getUser();
            int share = split.getAmount();

            if (participant.getUserId().equals(paidByUser.getUserId())) {
                continue;
            }

            paidByUser.adjustBalance(participant.getUserId(), share);
            participant.adjustBalance(paidByUser.getUserId(), -share);
        }

        return expense;
    }


    public Map<String, Integer> getBalances(String userId) {
        User user = userMap.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user.getBalances();
    }

    public List<Expense> getGroupExpenses(String groupId) {
        Group group = groupMap.get(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }
        return new ArrayList<>(group.getExpenses());
    }
}
