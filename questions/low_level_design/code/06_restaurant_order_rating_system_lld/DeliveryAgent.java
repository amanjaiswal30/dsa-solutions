import java.util.UUID;

public class DeliveryAgent extends User {
    Location location;
    boolean isAvailable;
    public DeliveryAgent(String name, String email) {
        super(name, email);
        this.isAvailable = true;
        this.location = null;
    }
}
