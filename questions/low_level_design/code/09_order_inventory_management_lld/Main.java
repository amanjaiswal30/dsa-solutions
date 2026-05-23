import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Inventory inventory = Inventory.getInstance();
        BookingService bookingService = BookingService.getInstance(inventory);

        Item jeans  = new Item("Jeans", 200);
        Item tshirt = new Item("Tshirt", 150);

        inventory.addProduct(jeans,  100);
        inventory.addProduct(tshirt, 50);

        inventory.printStock();

        User user = new User("Aman");
        Order order = bookingService.placeOrder(user, Map.of(
                jeans.getItemId(),  2,
                tshirt.getItemId(), 1
        ));

        inventory.printStock();

        bookingService.cancelOrder(order.getOrderId());
        inventory.printStock();
    }
}
