import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    List<Ride> pastRides;
    public Customer(String name, String email) {
        super(name, email);
        this.pastRides = new ArrayList<>();
    }
}
