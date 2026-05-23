import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Show {
    Movie movie;
    LocalDateTime startTime;
    List<Seat> showSeats;

    public Show(Movie movie, LocalDateTime startTime, List<Seat> showSeats) {
        this.movie = movie;
        this.startTime = startTime;
        this.showSeats = showSeats;
    }

    List<Seat> getAvailableSeats() {
        return showSeats.stream().filter(seat -> seat.isAvailable).toList();
    }
}
