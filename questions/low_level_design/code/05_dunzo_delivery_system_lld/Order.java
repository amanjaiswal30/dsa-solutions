import java.time.LocalDateTime;
import java.util.UUID;

public class Order {
    private final String orderId;
    private OrderStatus orderStatus;
    private DeliveryPartner deliveryPartner;
    private final Customer orderPlacedBy;
    private final Location source;
    private final Location destination;
    private final LocalDateTime orderDate;

    public Order(Customer orderPlacedBy, Location source, Location destination) {
        this.orderPlacedBy = orderPlacedBy;
        this.source = source;
        this.destination = destination;
        this.orderDate = LocalDateTime.now();
        this.orderId = UUID.randomUUID().toString();
        this.orderStatus = OrderStatus.BOOKED;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public Customer getOrderPlacedBy() {
        return orderPlacedBy;
    }

    public Location getSource() {
        return source;
    }

    public Location getDestination() {
        return destination;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void assignPartner(DeliveryPartner partner) {
        if (this.orderStatus != OrderStatus.BOOKED) {
            throw new IllegalStateException("Partner can only be assigned when order is BOOKED");
        }
        this.deliveryPartner = partner;
        this.orderStatus = OrderStatus.ASSIGNED;
    }

    public void updateStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }
}
