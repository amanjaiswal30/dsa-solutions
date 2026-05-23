import java.time.LocalDateTime;
import java.util.UUID;

public class Ride {
    String rideId;
    Location start;
    Location end;
    double price;
    Customer bookedByCustomer;
    Driver driver;
    RideStatus rideStatus;
    LocalDateTime rideTime;


    public Ride(Location start, Location end, double price, Customer bookedByCustomer) {
        this.start = start;
        this.rideId = UUID.randomUUID().toString();
        this.end = end;
        this.price = price;
        this.bookedByCustomer = bookedByCustomer;
        this.driver = null;
        this.rideTime = LocalDateTime.now();
        this.rideStatus = RideStatus.FINDING_DRIVER;
    }

    @Override
    public String toString() {
        return "Ride{" +
                "rideId='" + rideId + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", price=" + price +
                ", bookedByCustomer=" + bookedByCustomer +
                ", driver=" + driver +
                ", rideStatus=" + rideStatus +
                ", rideTime=" + rideTime +
                '}';
    }
}
