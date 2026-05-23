import java.util.ArrayList;
import java.util.List;

public class PercentageSplit implements SplitStrategy {
    @Override
    public List<Split> split(int amount, List<User> users, List<Integer> percentages) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("Users list cannot be empty.");
        }
        if (percentages == null || users.size() != percentages.size()) {
            throw new IllegalArgumentException("Percentages size must match users size.");
        }

        int percentSum = 0;
        for (Integer p : percentages) {
            if (p == null || p < 0) {
                throw new IllegalArgumentException("Percentage must be non-negative.");
            }
            percentSum += p;
        }
        if (percentSum != 100) {
            throw new IllegalArgumentException("Total percentage must be 100.");
        }

        List<Split> splits = new ArrayList<>();
        int assigned = 0;
        for (int i = 0; i < users.size(); i++) {
            int share;
            if (i == users.size() - 1) {
                share = amount - assigned;
            } else {
                share = (amount * percentages.get(i)) / 100;
                assigned += share;
            }
            splits.add(new Split(users.get(i), share));
        }
        return splits;
    }
}
