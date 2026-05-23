import java.util.List;

public class VendingMachineService {
    private final VendingMachine vendingMachine;

    public VendingMachineService(VendingMachine vendingMachine) {
        if (vendingMachine == null) {
            throw new IllegalArgumentException("VendingMachine cannot be null");
        }
        this.vendingMachine = vendingMachine;
    }

    public String selectProduct(String productId) {
        ensureMachineAvailable();
        Product product = vendingMachine.getInventory().getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (!vendingMachine.getInventory().isAvailable(productId)) {
            throw new IllegalStateException("Product is out of stock: " + productId);
        }
        vendingMachine.setActiveTransaction(new Transaction(product));
        vendingMachine.setState(VendingMachineState.PRODUCT_SELECTED);
        return "Selected: " + product;
    }

    public String addProductToInventory(Product product, int quantity) {
        vendingMachine.getInventory().addProduct(product, quantity);
        return "Added " + quantity + " unit(s) of " + product.getName();
    }

    public String insertMoney(Money money) {
        ensureTransactionExists();
        Transaction txn = vendingMachine.getActiveTransaction();
        txn.addMoney(money);
        vendingMachine.setState(VendingMachineState.COLLECTING_PAYMENT);
        int inserted = txn.getInsertedAmount();
        int price = txn.getProduct().getPriceInCents();
        return "Inserted ₹" + inserted + ", pending ₹" + Math.max(price - inserted, 0);
    }

    public String confirmPurchase() {
        ensureTransactionExists();
        Transaction txn = vendingMachine.getActiveTransaction();
        Product product = txn.getProduct();
        int insertedAmount = txn.getInsertedAmount();
        int productPrice = product.getPriceInCents();

        if (insertedAmount < productPrice) {
            throw new IllegalStateException("Insufficient payment. Remaining due: " + (productPrice - insertedAmount));
        }

        int changeNeeded = insertedAmount - productPrice;
        if (!vendingMachine.getMoneyHolder().canMakeChange(changeNeeded)) {
            List<Money> refund = refundCurrentTransaction();
            txn.markRefunded();
            clearTransaction();
            return "Change unavailable. Refunded ₹" + totalAmount(refund);
        }

        vendingMachine.setState(VendingMachineState.PROCESSING);

        if (!vendingMachine.getProductDispatcher().dispense(product)) {
            throw new IllegalStateException("Product dispenser failed");
        }

        vendingMachine.getMoneyHolder().addMoney(txn.getInsertedMoney());
        List<Money> change = vendingMachine.getMoneyHolder().dispenseChange(changeNeeded);

        vendingMachine.getInventory().decrementQuantity(product.getId());
        txn.markCompleted();
        vendingMachine.setState(VendingMachineState.IDLE);
        vendingMachine.clearActiveTransaction();
        return "Dispensed " + product.getName() + ", change returned ₹" + totalAmount(change);
    }

    public String cancelTransaction() {
        ensureTransactionExists();
        List<Money> refund = refundCurrentTransaction();
        vendingMachine.getActiveTransaction().markRefunded();
        clearTransaction();
        return "Transaction cancelled. Refunded ₹" + totalAmount(refund);
    }

    public String restock(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Product existing = vendingMachine.getInventory().getProduct(productId);
        if (existing == null) {
            throw new IllegalArgumentException("Cannot restock unknown product: " + productId);
        }
        vendingMachine.getInventory().addProduct(existing, quantity);
        return "Restocked " + existing.getName() + " with quantity " + quantity;
    }

    public String restock(Product product, int quantity) {
        vendingMachine.getInventory().addProduct(product, quantity);
        return "Restocked " + product.getName() + " with quantity " + quantity;
    }

    public String handlePowerFailure() {
        if (vendingMachine.getActiveTransaction() == null) {
            vendingMachine.setState(VendingMachineState.OUT_OF_SERVICE);
            return "Power failure: no active transaction. Machine out of service.";
        }
        List<Money> refund = refundCurrentTransaction();
        vendingMachine.getActiveTransaction().markRefunded();
        clearTransaction();
        vendingMachine.setState(VendingMachineState.OUT_OF_SERVICE);
        return "Power failure. Refunded ₹" + totalAmount(refund) + ". Machine out of service.";
    }

    public void restoreService() {
        if (vendingMachine.getState() == VendingMachineState.OUT_OF_SERVICE) {
            vendingMachine.setState(VendingMachineState.IDLE);
        }
    }

    public VendingMachineState getState() {
        return vendingMachine.getState();
    }

    private List<Money> refundCurrentTransaction() {
        return vendingMachine.getActiveTransaction().getInsertedMoney();
    }

    private void ensureMachineAvailable() {
        if (vendingMachine.getState() == VendingMachineState.OUT_OF_SERVICE) {
            throw new IllegalStateException("Machine is out of service");
        }
        if (!vendingMachine.getProductDispatcher().isOperational()) {
            throw new IllegalStateException("Product dispatcher is not operational");
        }
    }

    private void ensureTransactionExists() {
        ensureMachineAvailable();
        if (vendingMachine.getActiveTransaction() == null) {
            throw new IllegalStateException("No active transaction. Please select a product first.");
        }
    }

    private void clearTransaction() {
        vendingMachine.clearActiveTransaction();
        vendingMachine.setState(VendingMachineState.IDLE);
    }

    private int totalAmount(List<Money> moneyList) {
        int total = 0;
        for (Money money : moneyList) {
            total += money.getDenominationInCents();
        }
        return total;
    }
}
