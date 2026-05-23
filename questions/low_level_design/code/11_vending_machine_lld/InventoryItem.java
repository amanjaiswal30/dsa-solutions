public class InventoryItem {
    private final Product product;
    private int quantity;

    public InventoryItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public void increment(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Quantity to increment cannot be negative");
        }
        quantity += count;
    }

    public void decrement() {
        if (quantity <= 0) {
            throw new IllegalStateException("Product is out of stock");
        }
        quantity--;
    }
}
