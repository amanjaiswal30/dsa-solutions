import java.util.ArrayList;
import java.util.List;

public class ExactSplit implements SplitStrategy {
    @Override
    public List<Split> split(int amount, List<User> users, List<Integer> customAmount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("Users list cannot be empty.");
        }
        if (customAmount == null || users.size() != customAmount.size()) {
            throw new IllegalArgumentException("customAmount size must match users size.");
        }

        int total = 0;
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            int share = customAmount.get(i);
            if (share < 0) {
                throw new IllegalArgumentException("Split amount cannot be negative.");
            }
            total += share;
            splits.add(new Split(users.get(i), share));
        }

        if (total != amount) {
            throw new IllegalArgumentException("Exact split total must equal expense amount.");
        }

        return splits;
    }
}
