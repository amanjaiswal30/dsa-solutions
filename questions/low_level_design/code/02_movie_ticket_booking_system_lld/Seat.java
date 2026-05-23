import java.util.Objects;

public class Seat {
    int row;
    int seatNumber;
    boolean isAvailable;

    public Seat(int row, int seatNumber) {
        this.row = row;
        this.seatNumber = seatNumber;
        this.isAvailable = true;
    }
}
