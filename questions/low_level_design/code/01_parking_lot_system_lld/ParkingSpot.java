import java.util.UUID;

public class ParkingSpot {
    String spotId;
    SpotType spotType;
    Vehicle vehicle;
    boolean isFree;

    public ParkingSpot(SpotType spotType) {
        this.spotType = spotType;
        this.vehicle = null;
        this.isFree = true;
        this.spotId = UUID.randomUUID().toString();
    }

    boolean canVehicleFit(Vehicle vehicle) {
        switch (spotType) {
            case SMALL:
                return vehicle.vehicleType == VehicleType.BIKE;
            case MEDIUM:
                return vehicle.vehicleType == VehicleType.BIKE || vehicle.vehicleType == VehicleType.CAR;
            case LARGE:
                return vehicle.vehicleType == VehicleType.BIKE || vehicle.vehicleType == VehicleType.CAR || vehicle.vehicleType == VehicleType.TRUCK;
            default:
                return false;

        }
    }

    void updateParkingSpot(Vehicle vehicle, boolean hasVehicleEntered) {
        if(hasVehicleEntered) {
            this.isFree = false;
            this.vehicle = vehicle;
        }
        else{
            this.isFree = true;
            this.vehicle = null;

        }
    }
}
