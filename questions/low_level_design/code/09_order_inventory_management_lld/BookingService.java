import java.util.HashMap;
import java.util.Map;

public class BookingService {
    private static BookingService instance;
    private final Map<String, Order> orderMap = new HashMap<>();
    private final Inventory inventory;

    private BookingService(Inventory inventory) {
        this.inventory = inventory;
    }

    public static BookingService getInstance(Inventory inventory) {
        if (instance == null) instance = new BookingService(inventory);
        return instance;
    }

    public Order placeOrder(User user, Map<String, Integer> itemQtyMap) {
        Map<Item, Integer> resolvedItems = new HashMap<>();

        for (Map.Entry<String, Integer> entry : itemQtyMap.entrySet()) {
            Item item = inventory.getProduct(entry.getKey());
            if (item == null) throw new RuntimeException("Item not found: " + entry.getKey());
            inventory.deductStock(entry.getKey(), entry.getValue()); // throws if insufficient stock
            resolvedItems.put(item, entry.getValue());
        }

        Order order = new Order(user, resolvedItems);
        orderMap.put(order.getOrderId(), order);
        user.addOrder(order);
        System.out.println("Order placed! ID: " + order.getOrderId() + " | Total: $" + order.getTotal());
        return order;
    }

    public void cancelOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order == null || order.getOrderStatus() == OrderStatus.CANCELED) return;
        order.getItemMap().forEach((item, qty) -> inventory.restock(item.getItemId(), qty));
        order.setOrderStatus(OrderStatus.CANCELED);
        System.out.println("Order " + orderId + " cancelled. Stock restored.");
    }
}
