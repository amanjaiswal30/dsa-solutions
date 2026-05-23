import java.util.ArrayList;
import java.util.List;

public class EqualSplit implements SplitStrategy {
    @Override
    public List<Split> split(int amount, List<User> users, List<Integer> customAmount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("Users list cannot be empty.");
        }

        int n = users.size();
        int base = amount / n;
        int remainder = amount % n;

        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int share = base + (i < remainder ? 1 : 0);
            splits.add(new Split(users.get(i), share));
        }
        return splits;
    }
}
