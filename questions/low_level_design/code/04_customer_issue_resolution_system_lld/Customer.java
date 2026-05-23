import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    List<Ticket> activeTickets;

    public Customer(String name, String email) {
        super(name, email);
        this.activeTickets = new ArrayList<>();
    }

    public void viewTickets() {
        System.out.println("=== Tickets for " + name + " ===");
        activeTickets.forEach(t -> System.out.println("  " + t));
    }
}
