public class Split {
    private final User user;
    private final int amount;

    public Split(User user, int amount) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Split amount cannot be negative.");
        }
        this.user = user;
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public int getAmount() {
        return amount;
    }
}
