import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private static BookingService instance;
    List<Location> locations;

    private BookingService() {
        locations = new ArrayList<>();
    }

    public static BookingService getInstance() {
        if (instance == null) {
            instance = new BookingService();
        }
        return instance;
    }

    void addLocation(Location location) {
        this.locations.add(location);
    }

    Ticket bookTicket(User user, Show show, List<Seat> seatList) {
        if (areSeatsAvailable(show, seatList)) {
            Ticket ticket = new Ticket(user, show, seatList);
            user.tickets.add(ticket);
            updateShowSeats(show, seatList, false);
            return ticket;
        }
        return null;
    }

    void cancelTicket(Ticket ticket) {
        if (ticket != null) {
            for (Seat seat : ticket.show.showSeats) {
                for (Seat s : ticket.seats) {
                    if (seatMatches(s, seat)) {
                        seat.isAvailable = true;
                        break;
                    }
                }
            }
            ticket.ticketStatus = TicketStatus.CANCELLED;
        }
    }


    private void updateShowSeats(Show show, List<Seat> seatList, boolean isCancelled) {
        if (!isCancelled) {
            for (Seat seat : show.showSeats) {
                for (Seat s : seatList) {
                    if (seatMatches(s, seat)) {
                        seat.isAvailable = false;
                        break;
                    }
                }
            }
        }
    }


    List<Seat> getAvailableSeats(Show show) {
        return show.getAvailableSeats();
    }

    int getAvailableSeatsCount(Show show) {
        return show.getAvailableSeats().size();
    }

    private boolean areSeatsAvailable(Show show, List<Seat> seatList) {
        List<Seat> availableSeats = getAvailableSeats(show);
        for (Seat seat : seatList) {
            boolean found = false;
            for (Seat available : availableSeats) {
                if (seatMatches(available, seat)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }


    private boolean seatMatches(Seat a, Seat b) {
        return a.row == b.row && a.seatNumber == b.seatNumber;
    }
}
