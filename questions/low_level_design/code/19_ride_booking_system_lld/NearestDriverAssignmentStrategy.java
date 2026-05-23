import java.util.List;

public class NearestDriverAssignmentStrategy implements DriverAssignmentStrategy {
    @Override
    public Driver assignDriver(List<Driver> driverList, Location source) {
        double minDistance = Double.MAX_VALUE;
        Driver nearestDriver = null;
        for (Driver driver : driverList) {
            if (driver.isAvailable && driver.location.distanceTo(source) < minDistance) {
                minDistance = driver.location.distanceTo(source);
                nearestDriver = driver;
            }
        }

        return nearestDriver;
    }
}
