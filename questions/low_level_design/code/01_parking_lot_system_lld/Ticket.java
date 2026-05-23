import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    String ticketId;
    Vehicle vehicle;
    ParkingSpot parkingSpot;
    LocalDateTime entryTime;
    LocalDateTime exitTime;

    public Ticket(Vehicle vehicle, LocalDateTime entryTime, ParkingSpot parkingSpot) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryTime = entryTime;
    }
}
