public class Wallet {
    private final String walletId;
    private final String userId;
    private final String bankAccountId;
    private double balance;
    private boolean active;

    public Wallet(String walletId, String userId, String bankAccountId, double balance, boolean active) {
        this.walletId = walletId;
        this.userId = userId;
        this.bankAccountId = bankAccountId;
        this.balance = balance;
        this.active = active;
    }

    public String walletId() {
        return walletId;
    }

    public String userId() {
        return userId;
    }

    public String bankAccountId() {
        return bankAccountId;
    }

    public double balance() {
        return balance;
    }

    public boolean active() {
        return active;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
