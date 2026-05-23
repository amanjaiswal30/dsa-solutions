import java.util.*;

public class BookingService {
    private static final int BASE_RATE_PER_KM = 100;
    private static BookingService instance;

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Car> cars = new HashMap<>();
    private final Map<String, Booking> bookings = new HashMap<>();
    private final Map<String, List<Payment>> paymentsByBooking = new HashMap<>();

    private BookingService() {}

    public static synchronized BookingService getInstance() {
        if (instance == null) {
            instance = new BookingService();
        }
        return instance;
    }

    public String registerUser(String name) {
        User user = new User(name);
        users.put(user.getId(), user);
        return user.getId();
    }

    public void registerCar(String id, String model, int currentOdometerReading) {
        cars.put(id, new Car(id, model, currentOdometerReading));
    }

    public Booking createBooking(String userId, String carId, int kmsBookedFor, double locationMultiplier) {
        User user = users.get(userId);
        if (user == null) throw new IllegalArgumentException("Invalid user");
        if (kmsBookedFor <= 0) throw new IllegalArgumentException("kmsBookedFor must be > 0");
        if (locationMultiplier <= 0) throw new IllegalArgumentException("locationMultiplier must be > 0");

        Car car = cars.get(carId);
        if (car == null) throw new IllegalArgumentException("Invalid carId");
        if (!car.isAvailable()) throw new IllegalStateException("Selected car is not available");

        car.markBooked();
        Booking booking = new Booking(
                user,
                car,
                kmsBookedFor,
                locationMultiplier,
                BASE_RATE_PER_KM,
                car.getCurrentOdometerReading()
        );

        bookings.put(booking.getId(), booking);
        user.addBooking(booking);
        paymentsByBooking.put(booking.getId(), new ArrayList<>());
        return booking;
    }

    public List<Booking> getBookingsForUser(String userId) {
        User user = users.get(userId);
        if (user == null) throw new IllegalArgumentException("Invalid userId");
        return user.getBookings();
    }


    public void cancelBooking(String bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        booking.cancel();
        booking.getCar().markAvailable();
    }

    public Payment pay(String bookingId, int amount, PaymentMethod method) {
        Booking booking = getBookingOrThrow(bookingId);
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for cancelled booking");
        }

        Payment payment = new Payment(bookingId, amount, method);
        booking.addPayment(amount);
        paymentsByBooking.get(bookingId).add(payment);

        if (booking.getRemainingAmount() > 0) {
            booking.markPaymentPending();
        } else {
            booking.markPaymentCompleted();
        }
        return payment;
    }

    public void completeTrip(String bookingId, int endOdometerReading) {
        Booking booking = getBookingOrThrow(bookingId);
        Car car = booking.getCar();

        booking.completeTrip(endOdometerReading);
        car.updateOdometer(endOdometerReading);
        car.markAvailable();

        if (booking.getRemainingAmount() > 0) {
            booking.markPaymentPending();
        } else {
            booking.markPaymentCompleted();
        }
    }

    public int getRemainingAmount(String bookingId) {
        return getBookingOrThrow(bookingId).getRemainingAmount();
    }

    public Booking getBooking(String bookingId) {
        return getBookingOrThrow(bookingId);
    }

    private Booking getBookingOrThrow(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("Invalid bookingId");
        return booking;
    }

    private Car findAvailableCar() {
        for (Car car : cars.values()) {
            if (car.isAvailable()) return car;
        }
        return null;
    }
}
