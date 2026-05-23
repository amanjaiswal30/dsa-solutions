public class Main {
    public static void main(String[] args) {
        OrderService service = OrderService.getInstance();

        service.registerCustomer("Aman", "aman@dunzo.com");
        service.registerDeliveryPartner("Ravi", "ravi@dunzo.com", new Location(12.9716, 77.5946));
        service.registerDeliveryPartner("Neha", "neha@dunzo.com", new Location(12.9352, 77.6245));

        Order order = service.placeOrder(
                "aman@dunzo.com",
                new Location(12.9600, 77.6000),
                new Location(12.9900, 77.6500)
        );

        System.out.println("Order ID: " + order.getOrderId());
        System.out.println("Assigned Partner: " + order.getDeliveryPartner().getName());
        System.out.println("Status: " + order.getOrderStatus());

        service.markInTransit(order.getOrderId());
        System.out.println("Status after pickup: " + service.getOrder(order.getOrderId()).getOrderStatus());

        service.markDelivered(order.getOrderId());
        System.out.println("Final Status: " + service.getOrder(order.getOrderId()).getOrderStatus());
    }
}
