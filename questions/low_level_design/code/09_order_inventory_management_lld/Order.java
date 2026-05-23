import java.util.Map;
import java.util.UUID;

public class Order {
    private final String orderId;
    private final User user;
    private OrderStatus orderStatus;
    private final Map<Item, Integer> itemMap;

    public Order(User user, Map<Item, Integer> itemMap) {
        this.orderId = UUID.randomUUID().toString();
        this.user = user;
        this.itemMap = itemMap;
        this.orderStatus = OrderStatus.BOOKED;
    }

    public String getOrderId()             { return orderId; }
    public User getUser()                  { return user; }
    public OrderStatus getOrderStatus()    { return orderStatus; }
    public Map<Item, Integer> getItemMap() { return itemMap; }

    public void setOrderStatus(OrderStatus status) { this.orderStatus = status; }

    public double getTotal() {
        return itemMap.entrySet().stream()
                .mapToDouble(e -> e.getKey().getItemPrice() * e.getValue())
                .sum();
    }
}
