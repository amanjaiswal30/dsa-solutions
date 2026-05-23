import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        OrderService orderService = OrderService.getInstance();
        Customer customer = new Customer("aman", "jaiswal");
        orderService.userMap.put(customer.id,  customer);
        DeliveryAgent deliveryAgent1 = new DeliveryAgent("ravi", "jaiswal");
        DeliveryAgent deliveryAgent2 = new DeliveryAgent("kishan", "jaiswal");
        orderService.deliveryAgentMap.put(deliveryAgent1.id, deliveryAgent1);
        orderService.deliveryAgentMap.put(deliveryAgent2.id, deliveryAgent2);
        MenuItem rice = new MenuItem("Rice", 100);
        MenuItem dal = new MenuItem("Dal", 50);
        MenuItem chicken = new MenuItem("Chicken", 300);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(rice);
        menuItems.add(dal);
        menuItems.add(chicken);
        MenuInventory menuInventory = new MenuInventory();
        menuInventory.menuStock.put(rice.menuId,10);
        menuInventory.menuStock.put(dal.menuId,20);
        menuInventory.menuStock.put(chicken.menuId,30);
        Restaurant restaurant = new Restaurant("Dhaba", new Location(1,1),menuItems, menuInventory);
        Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put(rice.menuId, 4);
        orderItems.put(dal.menuId, 4);
        Order order = orderService.placeOrder(orderItems, customer, restaurant);
        Map<String, Integer> orderItems1 = new HashMap<>();
        orderItems1.put(rice.menuId, 4);
        orderItems1.put(dal.menuId, 4);
        Map<String, Integer> orderItems2 = new HashMap<>();
        orderItems2.put(rice.menuId, 2);
        orderItems2.put(dal.menuId, 2);
        Order order1 = orderService.placeOrder(orderItems1, customer, restaurant);
        Order order2 = orderService.placeOrder(orderItems2, customer, restaurant);
        System.out.println(order.orderStatus);
        System.out.println(order1.orderStatus);
        System.out.println(order2.orderStatus);
        orderService.assignDeliveryPartner(order);
        orderService.assignDeliveryPartner(order1);
        orderService.assignDeliveryPartner(order2);
        System.out.println(order.orderStatus);
        System.out.println(order1.orderStatus);
        System.out.println(order2.orderStatus);
    }
}
