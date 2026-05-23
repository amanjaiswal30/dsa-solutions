import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        List<Floor> floorList = new ArrayList<>();
        EntryGate entryGate = new EntryGate(1,1);
        ExitGate exitGate = new ExitGate(2,1);
        for(int i=1;i<=10;i++) {
            Floor floor = new Floor(i);
            if (i < 4) floor.addParkingSpot(new ParkingSpot(SpotType.SMALL));
            else if (i >= 4 && i <= 7) floor.addParkingSpot(new ParkingSpot(SpotType.MEDIUM));
            else floor.addParkingSpot(new ParkingSpot(SpotType.LARGE));
            floorList.add(floor);
        }
        ParkingLot parkingLot = ParkingLot.getInstance(floorList, List.of(entryGate), List.of(exitGate), new NearestParkingSpot());
        System.out.println(parkingLot.getAllAvailableSpots());
        Car car = new Car("1234");
        Ticket t1 = parkingLot.parkVehicle(car);
        parkingLot.parkVehicle(new Car("124"));
        parkingLot.parkVehicle(new Car("125"));
        parkingLot.parkVehicle(new Car("126"));
        parkingLot.parkVehicle(new Car("127"));
        parkingLot.parkVehicle(new Car("128"));
        parkingLot.parkVehicle(new Car("129"));
        parkingLot.parkVehicle(new Car("130"));
        System.out.println(parkingLot.getAllAvailableSpots());
        parkingLot.unParkVehicle(t1);
        System.out.println(parkingLot.getAllAvailableSpots());
        }
}
