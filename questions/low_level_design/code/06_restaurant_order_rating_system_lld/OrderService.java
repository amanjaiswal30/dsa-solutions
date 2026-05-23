import java.util.HashMap;
import java.util.Map;

public class OrderService {
    private static  OrderService instance;
    int defaultRadius = 5;
    Map<String, Customer> userMap;
    Map<String, Restaurant> restaurantMap;
    Map<String, DeliveryAgent> deliveryAgentMap;
    Map<String, Order> orderMap;
    private OrderService() {
        userMap = new HashMap<>();
        restaurantMap = new HashMap<>();
        deliveryAgentMap = new HashMap<>();
        orderMap = new HashMap<>();
    }
    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    Order placeOrder(Map<String, Integer> orderItems, User placedBy, Restaurant restaurant) {
        if(itemsAvailable(orderItems, restaurant)) {
            Order order = new Order(orderItems, placedBy, restaurant);
            orderMap.put(order.orderId, order);
            reduceInventory(orderItems, restaurant);
            return order;
        }
        return null;
    }

    DeliveryAgent assignDeliveryPartner(Order order) {
        Location restaurantLocation = order.restaurant.location;
        DeliveryAgent deliveryAgent = findNearestDeliveryAgent(deliveryAgentMap,defaultRadius, restaurantLocation);
        if(deliveryAgent != null) {
            updateOrderStatus(order, deliveryAgent, OrderStatus.PICKED_UP);
            deliveryAgent.isAvailable = false;
            return deliveryAgent;
        }
        return null;
    }


    private void updateOrderStatus(Order order, DeliveryAgent deliveryAgent, OrderStatus orderStatus) {
        order.orderStatus = orderStatus;
        order.deliveryAgent = deliveryAgent;
    }

    private DeliveryAgent findNearestDeliveryAgent(Map<String,DeliveryAgent> deliveryAgentMap, int defaultRadius, Location restaurantLocation) {
        for(DeliveryAgent deliveryAgent : deliveryAgentMap.values()) {
            if(deliveryAgent.isAvailable) return deliveryAgent;
        }
        return null;
    }

    private void reduceInventory(Map<String, Integer> orderItems, Restaurant restaurant) {
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            String menuId = entry.getKey();
            int quantity = entry.getValue();
            restaurant.menuInventory.menuStock.put(menuId, restaurant.menuInventory.menuStock.get(menuId) - quantity);
        }
    }

    private boolean itemsAvailable(Map<String, Integer> orderItems, Restaurant restaurant) {
        for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
            String menuItemId = entry.getKey();
            int quantity = entry.getValue();
            int inventory = restaurant.menuInventory.menuStock.getOrDefault(menuItemId, 0);
            if(inventory - quantity < 0) {
                return false;
            }
        }
        return true;
    }
}
