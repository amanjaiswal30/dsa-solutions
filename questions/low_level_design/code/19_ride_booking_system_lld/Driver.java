import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Driver extends User {
    List<Ride> previousRides;
    boolean isAvailable;
    Location location;
    public Driver(String name, String email, Location location) {
        super(name, email);
        this.location = location;
        this.isAvailable = true;
        this.previousRides = new ArrayList<>();
    }

    void acceptRide(Ride ride) {
        this.previousRides.add(ride);
        this.isAvailable = false;
    }
}
