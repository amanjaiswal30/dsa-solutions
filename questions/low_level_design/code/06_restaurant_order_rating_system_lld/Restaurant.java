import java.util.List;
import java.util.UUID;

public class Restaurant {
    String restaurantName;
    String restaurantId;
    Location location;
    List<MenuItem> menuItems;
    MenuInventory menuInventory;

    public Restaurant(String restaurantName, Location location, List<MenuItem> menuItems, MenuInventory menuInventory) {
        this.restaurantName = restaurantName;
        this.location = location;
        this.menuItems = menuItems;
        this.restaurantId = UUID.randomUUID().toString();
        this.menuInventory = menuInventory;
    }
}
