
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WalletService {
    private final Map<String, User> usersById = new HashMap<>();
    private final Map<String, BankAccount> bankAccountsByUserId = new HashMap<>();
    private final Map<String, Wallet> walletsById = new HashMap<>();
    private final Map<String, String> walletIdByUserId = new HashMap<>();
    private final Map<String, List<Transaction>> transactionsByWallet = new HashMap<>();

    public synchronized Wallet registerUser(String name, String bankName, String accountNumberMasked) {
        User user = new User(newId(), name);
        BankAccount bankAccount = new BankAccount(newId(), user.getUserId(), bankName, accountNumberMasked);
        Wallet wallet = new Wallet(newId(), user.getUserId(), bankAccount.getBankAccountId(), 0.0, true);

        usersById.put(user.getUserId(), user);
        bankAccountsByUserId.put(user.getUserId(), bankAccount);
        walletsById.put(wallet.walletId(), wallet);
        walletIdByUserId.put(user.getUserId(), wallet.walletId());

        return wallet;
    }

    public synchronized Transaction addMoney(String walletId, double amount) {
        Wallet wallet = walletsById.get(walletId);
        if (wallet == null) {
            return record(TransactionType.ADD_MONEY, null, walletId, amount, TransactionStatus.FAILED, "Wallet not found");
        }
        String error = validateWalletAndAmount(wallet, amount);
        if (error != null) {
            return record(TransactionType.ADD_MONEY, null, walletId, amount, TransactionStatus.FAILED, error);
        }

        wallet.setBalance(wallet.balance() + amount);
        return record(TransactionType.ADD_MONEY, null, walletId, amount, TransactionStatus.SUCCESS, "Bank to wallet top up");
    }

    public synchronized Transaction transfer(String fromWalletId, String toWalletId, double amount) {
        Wallet fromWallet = walletsById.get(fromWalletId);
        Wallet toWallet = walletsById.get(toWalletId);

        if (fromWallet == null || toWallet == null) {
            return record(TransactionType.TRANSFER, fromWalletId, toWalletId, amount, TransactionStatus.FAILED,
                    "One or both wallets not found");
        }
        if (fromWalletId.equals(toWalletId)) {
            return record(TransactionType.TRANSFER, fromWalletId, toWalletId, amount, TransactionStatus.FAILED,
                    "Source and destination wallets must be different");
        }

        String fromError = validateWalletAndAmount(fromWallet, amount);
        String toError = validateWalletAndAmount(toWallet, amount);
        if (fromError != null || toError != null) {
            return record(TransactionType.TRANSFER, fromWalletId, toWalletId, amount, TransactionStatus.FAILED,
                    fromError != null ? fromError : toError);
        }
        if (fromWallet.balance() < amount) {
            return record(TransactionType.TRANSFER, fromWalletId, toWalletId, amount, TransactionStatus.FAILED,
                    "Insufficient balance");
        }

        fromWallet.setBalance(fromWallet.balance() - amount);
        toWallet.setBalance(toWallet.balance() + amount);

        return record(TransactionType.TRANSFER, fromWalletId, toWalletId, amount, TransactionStatus.SUCCESS,
                "Wallet to wallet transfer");
    }

    public synchronized Transaction withdraw(String walletId, double amount) {
        Wallet wallet = walletsById.get(walletId);
        if (wallet == null) {
            return record(TransactionType.WITHDRAW, walletId, null, amount, TransactionStatus.FAILED, "Wallet not found");
        }

        String error = validateWalletAndAmount(wallet, amount);
        if (error != null) {
            return record(TransactionType.WITHDRAW, walletId, null, amount, TransactionStatus.FAILED, error);
        }
        if (wallet.balance() < amount) {
            return record(TransactionType.WITHDRAW, walletId, null, amount, TransactionStatus.FAILED,
                    "Insufficient balance");
        }

        wallet.setBalance(wallet.balance() - amount);
        return record(TransactionType.WITHDRAW, walletId, null, amount, TransactionStatus.SUCCESS,
                "Wallet to bank withdrawal");
    }

    public synchronized double getBalance(String walletId) {
        Wallet wallet = walletsById.get(walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }
        return wallet.balance();
    }

    public synchronized List<Transaction> getTransactions(String walletId) {
        return new ArrayList<>(transactionsByWallet.getOrDefault(walletId, List.of()));
    }

    public synchronized Wallet getWalletByUser(String userId) {
        String walletId = walletIdByUserId.get(userId);
        if (walletId == null) {
            throw new IllegalArgumentException("Wallet not found for user");
        }
        return walletsById.get(walletId);
    }

    public synchronized BankAccount getLinkedBankAccount(String walletId) {
        Wallet wallet = walletsById.get(walletId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }

        BankAccount bankAccount = bankAccountsByUserId.get(wallet.userId());
        if (bankAccount == null || !bankAccount.getBankAccountId().equals(wallet.bankAccountId())) {
            throw new IllegalStateException("Linked bank account not found for wallet");
        }
        return bankAccount;
    }

    private String validateWalletAndAmount(Wallet wallet, double amount) {
        if (!wallet.active()) {
            return "Wallet is inactive";
        }
        if (amount <= 0) {
            return "Amount should be greater than zero";
        }
        return null;
    }

    private Transaction record(TransactionType type,
                               String fromWalletId,
                               String toWalletId,
                               double amount,
                               TransactionStatus status,
                               String note) {
        Transaction txn = new Transaction(newId(), type, fromWalletId, toWalletId, amount, status, note,
                Instant.now().getEpochSecond());

        if (fromWalletId != null) {
            transactionsByWallet.computeIfAbsent(fromWalletId, key -> new ArrayList<>()).add(txn);
        }
        if (toWalletId != null && !toWalletId.equals(fromWalletId)) {
            transactionsByWallet.computeIfAbsent(toWalletId, key -> new ArrayList<>()).add(txn);
        }
        return txn;
    }

    private String newId() {
        return UUID.randomUUID().toString();
    }
}
