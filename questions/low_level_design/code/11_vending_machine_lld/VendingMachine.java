public class VendingMachine {
    private final Inventory inventory;
    private final MoneyHolder moneyHolder;
    private final ProductDispatcher productDispatcher;
    private VendingMachineState state = VendingMachineState.IDLE;
    private Transaction activeTransaction;

    public VendingMachine(Inventory inventory, MoneyHolder moneyHolder, ProductDispatcher productDispatcher) {
        if (inventory == null || moneyHolder == null || productDispatcher == null) {
            throw new IllegalArgumentException("Vending machine dependencies cannot be null");
        }
        this.inventory = inventory;
        this.moneyHolder = moneyHolder;
        this.productDispatcher = productDispatcher;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public MoneyHolder getMoneyHolder() {
        return moneyHolder;
    }

    public ProductDispatcher getProductDispatcher() {
        return productDispatcher;
    }

    public VendingMachineState getState() {
        return state;
    }

    public void setState(VendingMachineState state) {
        this.state = state;
    }

    public Transaction getActiveTransaction() {
        return activeTransaction;
    }

    public void setActiveTransaction(Transaction transaction) {
        this.activeTransaction = transaction;
    }

    public void clearActiveTransaction() {
        this.activeTransaction = null;
    }
}
