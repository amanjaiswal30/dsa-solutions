public class Main {
    public static void main(String[] args) {
        DriverAssignmentStrategy driverAssignmentStrategy = new NearestDriverAssignmentStrategy();
        RideBookingService rideBookingService = RideBookingService.getInstance(driverAssignmentStrategy);
        Customer aman = new Customer("Aman","emamkl");
        Customer ben = new Customer("Ben","emamkl");
        Customer jan = new Customer("Jan","emamkl");
        Driver kamlesh = new Driver("Kamlesh", "enak", new Location(32.12,43.54));
        Driver raju = new Driver("Raju", "eana", new Location(12.23,54.21));
        rideBookingService.addCustomer(aman);
        rideBookingService.addCustomer(ben);
        rideBookingService.addCustomer(jan);
        rideBookingService.addDriver(kamlesh);
        rideBookingService.addDriver(raju);

        Ride ride = rideBookingService.bookRide(aman, new Location(53.21,32.11), new Location(11.32,23.45), new FixedPricingStrategy());
        Ride ride2 = rideBookingService.bookRide(ben, new Location(53.21,32.11), new Location(11.32,23.45), new FixedPricingStrategy());
        Ride ride3 = rideBookingService.bookRide(ben, new Location(53.21,32.11), new Location(11.32,23.45), new FixedPricingStrategy());
        System.out.println(ride);
        System.out.println(ride2);
        System.out.println(ride3);
        rideBookingService.assignDriver(ride);
        System.out.println(ride);
        rideBookingService.assignDriver(ride2);
        System.out.println(ride2);
        rideBookingService.assignDriver(ride3);
        System.out.println(ride3);

    }
}
