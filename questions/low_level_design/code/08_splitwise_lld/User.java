import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {
    private final String userId;
    private final String name;
    private final Map<String, Integer> balances;

    public User(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.name = name;
        this.userId = UUID.randomUUID().toString();
        this.balances = new HashMap<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getBalances() {
        return Collections.unmodifiableMap(balances);
    }

    public void adjustBalance(String otherUserId, int delta) {
        if (otherUserId == null || otherUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("otherUserId cannot be empty.");
        }
        if (delta == 0) {
            return;
        }

        int updated = balances.getOrDefault(otherUserId, 0) + delta;
        if (updated == 0) {
            balances.remove(otherUserId);
        } else {
            balances.put(otherUserId, updated);
        }
    }
}
