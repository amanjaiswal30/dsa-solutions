public class Main {
    public static void main(String[] args) {
        WalletService service = new WalletService();

        Wallet first = service.registerUser("Aman", "HDFC", "XXXX1234");
        Wallet second = service.registerUser("Riya", "ICICI", "XXXX5678");

        service.addMoney(first.walletId(), 1000);
        Transaction transfer = service.transfer(first.walletId(), second.walletId(), 250);

        System.out.println("Transfer status: " + transfer.getStatus());
        System.out.println("Wallet A balance: " + service.getBalance(first.walletId()));
        System.out.println("Wallet B balance: " + service.getBalance(second.walletId()));
        System.out.println("Wallet A transactions: " + service.getTransactions(first.walletId()).size());
    }
}
