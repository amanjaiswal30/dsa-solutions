import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User {
    private final String id;
    private final String name;
    private final List<Booking> bookings;

    public User(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.bookings = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Booking> getBookings() { return Collections.unmodifiableList(bookings); }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }
}
