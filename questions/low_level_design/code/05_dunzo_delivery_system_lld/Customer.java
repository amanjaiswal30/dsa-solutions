import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer extends User {
    private final List<Order> orderList;

    public Customer(String name, String email) {
        super(name, email);
        this.orderList = new ArrayList<>();
    }

    public void addOrder(Order order) {
        this.orderList.add(order);
    }

    public List<Order> getOrderList() {
        return Collections.unmodifiableList(orderList);
    }
}
