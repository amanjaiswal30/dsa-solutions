import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private static volatile OrderService orderService;

    private final Map<String, Customer> customersByEmail;
    private final Map<String, DeliveryPartner> partnersByEmail;
    private final Map<String, Order> ordersById;
    private PartnerAssignmentStrategy assignmentStrategy;

    private OrderService() {
        this.customersByEmail = new HashMap<>();
        this.partnersByEmail = new HashMap<>();
        this.ordersById = new HashMap<>();
        this.assignmentStrategy = new NearestDeliveryPartnerAssignmentStrategy();
    }

    public static OrderService getInstance() {
        if (orderService == null) {
            synchronized (OrderService.class) {
                if (orderService == null) {
                    orderService = new OrderService();
                }
            }
        }
        return orderService;
    }

    public void setAssignmentStrategy(PartnerAssignmentStrategy assignmentStrategy) {
        this.assignmentStrategy = assignmentStrategy;
    }

    public Customer registerCustomer(String name, String email) {
        Customer customer = new Customer(name, email);
        customersByEmail.put(email, customer);
        return customer;
    }

    public DeliveryPartner registerDeliveryPartner(String name, String email, Location location) {
        DeliveryPartner partner = new DeliveryPartner(name, email, location);
        partnersByEmail.put(email, partner);
        return partner;
    }

    public void updatePartnerLocation(String partnerEmail, Location location) {
        DeliveryPartner partner = getPartnerOrThrow(partnerEmail);
        partner.updateLocation(location);
    }

    public Order placeOrder(String customerEmail, Location source, Location destination) {
        Customer customer = getCustomerOrThrow(customerEmail);

        Order order = new Order(customer, source, destination);
        DeliveryPartner assignedPartner = assignmentStrategy.assignPartner(
                new ArrayList<>(partnersByEmail.values()), order
        );

        if (assignedPartner == null) {
            throw new IllegalStateException("No delivery partner available");
        }

        order.assignPartner(assignedPartner);
        assignedPartner.setAvailable(false);

        ordersById.put(order.getOrderId(), order);
        customer.addOrder(order);

        return order;
    }

    public void markInTransit(String orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getOrderStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException("Order must be ASSIGNED to move IN_TRANSIT");
        }
        order.updateStatus(OrderStatus.IN_TRANSIT);
    }

    public void markDelivered(String orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getOrderStatus() != OrderStatus.IN_TRANSIT) {
            throw new IllegalStateException("Order must be IN_TRANSIT to mark DELIVERED");
        }
        order.updateStatus(OrderStatus.DELIVERED);
        if (order.getDeliveryPartner() != null) {
            order.getDeliveryPartner().setAvailable(true);
        }
    }

    public void cancelOrder(String orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered order cannot be cancelled");
        }
        order.updateStatus(OrderStatus.CANCELLED);
        if (order.getDeliveryPartner() != null) {
            order.getDeliveryPartner().setAvailable(true);
        }
    }

    public Order getOrder(String orderId) {
        return ordersById.get(orderId);
    }

    public List<Order> getOrdersForCustomer(String customerEmail) {
        return getCustomerOrThrow(customerEmail).getOrderList();
    }

    private Customer getCustomerOrThrow(String customerEmail) {
        Customer customer = customersByEmail.get(customerEmail);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerEmail);
        }
        return customer;
    }

    private DeliveryPartner getPartnerOrThrow(String partnerEmail) {
        DeliveryPartner partner = partnersByEmail.get(partnerEmail);
        if (partner == null) {
            throw new IllegalArgumentException("Delivery partner not found: " + partnerEmail);
        }
        return partner;
    }

    private Order getOrderOrThrow(String orderId) {
        Order order = ordersById.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }
}
