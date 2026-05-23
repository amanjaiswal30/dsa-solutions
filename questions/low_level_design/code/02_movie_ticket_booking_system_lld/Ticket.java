import java.util.List;
import java.util.UUID;

public class Ticket {
    String ticketId;
    User bookedByUser;
    TicketStatus ticketStatus;
    Show show;
    List<Seat> seats;

    public Ticket(User bookedByUser, Show show, List<Seat> seats) {
        this.bookedByUser = bookedByUser;
        this.show = show;
        this.seats = seats;
        this.ticketId = UUID.randomUUID().toString();
        this.ticketStatus = TicketStatus.BOOKED;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", bookedByUser=" + bookedByUser +
                ", ticketStatus=" + ticketStatus +
                ", show=" + show +
                ", seats=" + seats +
                '}';
    }
}
