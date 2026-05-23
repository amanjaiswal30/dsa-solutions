import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Inventory {
    private final Map<String, InventoryItem> items = new LinkedHashMap<>();

    public void addProduct(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        InventoryItem item = items.get(product.getId());
        if (item == null) {
            items.put(product.getId(), new InventoryItem(product, quantity));
        } else {
            item.increment(quantity);
        }
    }

    public boolean isAvailable(String productId) {
        InventoryItem item = items.get(productId);
        return item != null && item.isAvailable();
    }

    public Product getProduct(String productId) {
        InventoryItem item = items.get(productId);
        return item == null ? null : item.getProduct();
    }

    public int getQuantity(String productId) {
        InventoryItem item = items.get(productId);
        return item == null ? 0 : item.getQuantity();
    }

    public void decrementQuantity(String productId) {
        InventoryItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product not found in inventory: " + productId);
        }
        item.decrement();
    }

    public Collection<InventoryItem> getAllItems() {
        return new ArrayList<>(items.values());
    }
}
