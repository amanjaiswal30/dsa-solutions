import java.util.List;

public interface DriverAssignmentStrategy {
    Driver assignDriver(List<Driver> driverList, Location source);
}
