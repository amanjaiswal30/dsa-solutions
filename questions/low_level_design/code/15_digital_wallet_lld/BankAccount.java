public class BankAccount {
    private final String bankAccountId;
    private final String userId;
    private final String bankName;
    private final String accountNumberMasked;

    public BankAccount(String bankAccountId, String userId, String bankName, String accountNumberMasked) {
        this.bankAccountId = bankAccountId;
        this.userId = userId;
        this.bankName = bankName;
        this.accountNumberMasked = accountNumberMasked;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getUserId() {
        return userId;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumberMasked() {
        return accountNumberMasked;
    }
}
