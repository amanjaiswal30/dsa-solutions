import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideBookingService {
    Map<String, Customer> customerMap;
    Map<String, Ride> rideMap;
    Map<String, Driver> driverMap;
    DriverAssignmentStrategy driverAssignmentStrategy;
    private static  RideBookingService instance;
    private RideBookingService(DriverAssignmentStrategy driverAssignmentStrategy) {
        customerMap = new HashMap<>();
        rideMap = new HashMap<>();
        driverMap = new HashMap<>();
        this.driverAssignmentStrategy = driverAssignmentStrategy;

    }

    public static RideBookingService getInstance(DriverAssignmentStrategy driverAssignmentStrategy) {
        if (instance == null) {
            instance = new RideBookingService(driverAssignmentStrategy);
        }
        return instance;
    }

    void addCustomer(Customer customer) {
        customerMap.put(customer.id, customer);
    }



    void addRide(Ride ride) {
        rideMap.put(ride.rideId, ride);
    }
    void addDriver(Driver driver) {
        driverMap.put(driver.id, driver);
    }

    double getPrice(PricingStrategy pricingStrategy, Location from, Location to) {
        return 2 * pricingStrategy.getEstimatedPrice(from, to);
    }

    Ride bookRide(Customer customer, Location from, Location to, PricingStrategy pricingStrategy) {
        double price = getPrice(pricingStrategy, from, to);
        Ride ride = new Ride(from, to, price, customer);
        rideMap.put(ride.rideId, ride);
        return ride;
    }

    Driver assignDriver(Ride ride) {
        Driver driver = driverAssignmentStrategy.assignDriver(driverMap.values().stream().toList(), ride.start);
        if (driver != null) {
            driver.isAvailable = false;
            ride.driver = driver;
            ride.rideStatus = RideStatus.DRIVER_ASSIGNED;
            return driver;
        }
        return null;
    }


}
