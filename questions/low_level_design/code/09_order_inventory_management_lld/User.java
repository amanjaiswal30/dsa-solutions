import java.util.ArrayList;
import java.util.List;

public class User {
    private final String name;
    private final List<Order> orderList;

    public User(String name) {
        this.name = name;
        this.orderList = new ArrayList<>();
    }

    public String getName()           { return name; }
    public List<Order> getOrderList() { return orderList; }
    public void addOrder(Order order) { orderList.add(order); }
}
