import java.util.List;

public interface ParkingStrategy {
    ParkingSpot findAvailableSpot(Vehicle vehicle, List<Floor> floorList);
}
