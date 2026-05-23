import java.util.ArrayList;
import java.util.List;

public class User {
    String name;
    List<Ticket> tickets;


    public User(String name) {
        this.name = name;
        this.tickets = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", tickets=" + tickets +
                '}';
    }
}
