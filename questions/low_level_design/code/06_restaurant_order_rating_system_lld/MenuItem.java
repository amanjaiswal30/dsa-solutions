import java.util.UUID;

public class MenuItem {
    String menuId;
    String name;
    int price;
    public MenuItem(String name, int price) {
        this.name = name;
        this.price = price;
        this.menuId = UUID.randomUUID().toString();
    }
}
