import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Inventory {
    private static Inventory instance;
    private final Map<String, ItemStock> inventory = new ConcurrentHashMap<>(); // itemId -> ItemStock

    private Inventory() {}

    public static synchronized Inventory getInstance() {
        if (instance == null) instance = new Inventory();
        return instance;
    }

    public void addProduct(Item item, int qty) {
        ItemStock existing = inventory.get(item.getItemId());
        if (existing != null) {
            existing.addStock(qty);
        } else {
            inventory.put(item.getItemId(), new ItemStock(item, qty));
        }
    }

    public void restock(String itemId, int qty) {
        ItemStock itemStock = inventory.get(itemId);
        if (itemStock == null) throw new RuntimeException("Item not found: " + itemId);
        itemStock.addStock(qty);
    }

    public boolean deductStock(String itemId, int qty) {
        ItemStock itemStock = inventory.get(itemId);
        if (itemStock == null) throw new RuntimeException("Item not found: " + itemId);
        if (!itemStock.deductStock(qty))
            throw new RuntimeException("Insufficient stock: " + itemStock.getItem().getItemName());
        return true;
    }

    public Item getProduct(String itemId) {
        ItemStock itemStock = inventory.get(itemId);
        return itemStock != null ? itemStock.getItem() : null;
    }

    public int getStock(String itemId) {
        ItemStock itemStock = inventory.get(itemId);
        return itemStock != null ? itemStock.getQuantity() : 0;
    }

    public void printStock() {
        System.out.println("\n-- Inventory --");
        inventory.values().forEach(itemStock ->
                System.out.println(
                        itemStock.getItem().getItemName() + ": " +
                                itemStock.getQuantity() + " units @ $" +
                                itemStock.getItem().getItemPrice()
                ));
    }
}
