import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        BookingService bookingService = BookingService.getInstance();
        Movie movie1 = new Movie("Name", Genre.COMEDY);
        Movie movie2 = new Movie("Name1", Genre.DRAMA);
        List<Seat> show1Seats = new ArrayList<>();
        List<Seat> show2Seats = new ArrayList<>();
        for(int i=1;i<=10;i++) {
            for(int j=1;j<=50;j++) {
                Seat seat1 = new Seat(i,j);
                Seat seat2 = new Seat(i,j);
                show1Seats.add(seat1);
                show2Seats.add(seat2);
            }
        }
        Show show1 = new Show(movie1, LocalDateTime.now(),show1Seats);
        Show show2 = new Show(movie2, LocalDateTime.now(),show2Seats);
        bookingService.addLocation(new Location("Bangalore",List.of(show1,show2)));
        System.out.println(bookingService.getAvailableSeatsCount(show1));
        System.out.println(bookingService.getAvailableSeatsCount(show2));
        User user1 = new User("Aman");
        User user2 = new User("Jan");
        User user3 = new User("Daniel");
        Ticket t1 = bookingService.bookTicket(user1,show1, List.of(new Seat(2,34), new Seat(2,35)));
        Ticket t2 = bookingService.bookTicket(user2,show2, List.of(new Seat(2,34), new Seat(2,35)));
        System.out.println(bookingService.getAvailableSeatsCount(show1));
        System.out.println(bookingService.getAvailableSeatsCount(show2));
        Ticket t3 = bookingService.bookTicket(user3,show2, List.of(new Seat(2,34), new Seat(2,35)));
        System.out.println(bookingService.getAvailableSeatsCount(show2));
        bookingService.cancelTicket(t2);
        System.out.println(bookingService.getAvailableSeatsCount(show2));
        Ticket t4 = bookingService.bookTicket(user3,show2, List.of(new Seat(2,34), new Seat(2,35)));
        System.out.println(bookingService.getAvailableSeatsCount(show2));
        System.out.println(t1.ticketStatus);
        System.out.println(t2.ticketStatus);
//        System.out.println(t3.ticketStatus);
        System.out.println(t4.ticketStatus);

    }
}
