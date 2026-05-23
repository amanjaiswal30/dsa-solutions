import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private final String id;
    private final String bookingId;
    private final int amount;
    private final PaymentMethod method;
    private final LocalDateTime paidAt;

    public Payment(String bookingId, int amount, PaymentMethod method) {
        this.id = UUID.randomUUID().toString();
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.paidAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public int getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
