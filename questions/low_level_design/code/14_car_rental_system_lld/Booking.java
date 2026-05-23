import java.util.UUID;

public class Booking {
    private final String id;
    private final User user;
    private final Car car;
    private final int kmsBookedFor;
    private final double locationMultiplier;
    private final int ratePerKm;
    private final int startOdometer;

    private BookingStatus bookingStatus;
    private int paidAmount;
    private Integer endOdometer;
    private Integer finalAmount;

    public Booking(User user, Car car, int kmsBookedFor, double locationMultiplier,
                   int ratePerKm, int startOdometer) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.car = car;
        this.kmsBookedFor = kmsBookedFor;
        this.locationMultiplier = locationMultiplier;
        this.ratePerKm = ratePerKm;
        this.startOdometer = startOdometer;
        this.bookingStatus = BookingStatus.BOOKED;
        this.paidAmount = 0;
    }

    public String getId() { return id; }
    public User getUser() { return user; }
    public Car getCar() { return car; }
    public int getKmsBookedFor() { return kmsBookedFor; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public int getPaidAmount() { return paidAmount; }
    public Integer getFinalAmount() { return finalAmount; }

    public int getEstimatedAmount() {
        return (int) Math.ceil(kmsBookedFor * ratePerKm * locationMultiplier);
    }

    public void cancel() {
        if (bookingStatus != BookingStatus.BOOKED) {
            throw new IllegalStateException("Only BOOKED booking can be cancelled");
        }
        bookingStatus = BookingStatus.CANCELLED;
    }

    public void addPayment(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        paidAmount += amount;
    }

    public void completeTrip(int endOdometer) {
        if (endOdometer < startOdometer) {
            throw new IllegalArgumentException("End odometer cannot be less than start odometer");
        }
        this.endOdometer = endOdometer;
        int actualKms = endOdometer - startOdometer;
        this.finalAmount = (int) Math.ceil(actualKms * ratePerKm * locationMultiplier);
        this.bookingStatus = BookingStatus.TRIP_COMPLETED;
    }

    public int getRemainingAmount() {
        int payable = (finalAmount != null) ? finalAmount : getEstimatedAmount();
        return Math.max(0, payable - paidAmount);
    }

    public void markPaymentPending() {
        this.bookingStatus = BookingStatus.PAYMENT_PENDING;
    }

    public void markPaymentCompleted() {
        this.bookingStatus = BookingStatus.PAYMENT_COMPLETED;
    }
}
