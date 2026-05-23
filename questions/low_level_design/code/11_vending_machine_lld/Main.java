public class Main {
    public static void main(String[] args) {
        Customer customer = new Customer("C001", "Aman");
        Admin admin = new Admin("A001", "Store Admin");

        Inventory inventory = new Inventory();
        MoneyHolder moneyHolder = new MoneyHolder();
        ProductDispatcher productDispatcher = new ProductDispatcher();
        VendingMachine machine = new VendingMachine(inventory, moneyHolder, productDispatcher);
        VendingMachineService vendingMachine = new VendingMachineService(machine);

        System.out.println(customer);
        System.out.println(admin);

        Product chips = new Product("P1", "Chips", 30);
        Product soda = new Product("P2", "Soda", 50);

        System.out.println(vendingMachine.addProductToInventory(chips, 5));
        System.out.println(vendingMachine.addProductToInventory(soda, 2));

        // seed the machine's cash reserve with coins and notes
        moneyHolder.addMoney(new Coin(10));
        moneyHolder.addMoney(new Coin(10));
        moneyHolder.addMoney(new Coin(10));
        moneyHolder.addMoney(new Coin(10));
        moneyHolder.addMoney(new Coin(10));
        moneyHolder.addMoney(new Coin(20));
        moneyHolder.addMoney(new Cash(50));

        // customer buys Chips (₹30) with a ₹50 note — expects ₹20 change
        System.out.println("\n--- Customer buys Chips ---");
        System.out.println(vendingMachine.selectProduct("P1"));
        System.out.println(vendingMachine.insertMoney(new Cash(50)));
        System.out.println(vendingMachine.confirmPurchase());

        // admin restocks Chips
        System.out.println("\n--- Admin restocks Chips ---");
        System.out.println(vendingMachine.restock("P1", 3));

        // customer buys Soda (₹50) with a ₹100 note — expects ₹50 change
        System.out.println("\n--- Customer buys Soda ---");
        System.out.println(vendingMachine.selectProduct("P2"));
        System.out.println(vendingMachine.insertMoney(new Cash(100)));
        System.out.println(vendingMachine.confirmPurchase());

        // simulate power failure
        System.out.println("\n--- Power Failure ---");
        System.out.println(vendingMachine.handlePowerFailure());
    }
}
