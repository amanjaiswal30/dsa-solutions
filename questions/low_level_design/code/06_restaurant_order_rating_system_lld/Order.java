import java.util.Map;
import java.util.UUID;

public class Order {
    String orderId;
    Map<String, Integer> orderDetails;
    Restaurant restaurant;
    DeliveryAgent deliveryAgent;
    OrderStatus orderStatus;
    User orderPlacedBy;

    public Order(Map<String, Integer> orderDetails, User orderPlacedBy, Restaurant restaurant) {
        this.orderDetails = orderDetails;
        this.orderPlacedBy = orderPlacedBy;
        this.orderId = UUID.randomUUID().toString();
        this.orderStatus = OrderStatus.BOOKED;
        this.deliveryAgent = null;
        this.restaurant = restaurant;
    }

}
