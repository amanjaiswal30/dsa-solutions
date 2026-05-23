public class Main {
    public static void main(String[] args) {
        ATM atmEntity = new ATM("ATM-DEL-01", "Delhi CP Kiosk");
        ATMService atmService = ATMService.getInstance(atmEntity);

        Customer customer = new Customer("C1001", "Aman");
        BankAccount account = new BankAccount("A2001", customer.getCustomerId(), 25000);

        Card primaryCard = new Card("4111111111111111", "1234", account);
        Card backupCard = new Card("5555555555554444", "4321", account);

        customer.addCard(primaryCard);
        customer.addCard(backupCard);

        atmService.addAccount(account);
        atmService.addCard(primaryCard);
        atmService.addCard(backupCard);

        atmService.restock(NoteDenomination.TWO_THOUSAND, 10);
        atmService.restock(NoteDenomination.FIVE_HUNDRED, 20);
        atmService.restock(NoteDenomination.TWO_HUNDRED, 30);
        atmService.restock(NoteDenomination.ONE_HUNDRED, 50);

        atmService.insertCard(primaryCard.getCardNumber());
        atmService.authenticate("1234");

        System.out.println("Balance: " + atmService.checkBalance());
        System.out.println("Dispensed notes: " + atmService.withdraw(3700));
        System.out.println("Balance after withdrawal: " + atmService.checkBalance());
        System.out.println("Mini statement:");
        atmService.miniStatement(5).forEach(System.out::println);

        atmService.ejectCard();
        System.out.println("ATM remaining cash: " + atmService.getAtmCashBalance());
    }
}
