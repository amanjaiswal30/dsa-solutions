import java.util.List;

public class NearestParkingSpot implements ParkingStrategy {
    @Override
    public ParkingSpot findAvailableSpot(Vehicle vehicle, List<Floor> floorList) {
        for (Floor floor : floorList) {
           for(ParkingSpot parkingSpot: floor.parkingSpotList) {
               if(parkingSpot.isFree && parkingSpot.canVehicleFit(vehicle)) {
                   return parkingSpot;
               }
           }
        }
        return null;
    }
}
