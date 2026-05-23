import java.util.ArrayList;
import java.util.List;

public class Floor {
    int floorNumber;
    List<ParkingSpot> parkingSpotList;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        parkingSpotList = new ArrayList<>();
    }
    void addParkingSpot(ParkingSpot parkingSpot) {
        parkingSpotList.add(parkingSpot);
    }
}
