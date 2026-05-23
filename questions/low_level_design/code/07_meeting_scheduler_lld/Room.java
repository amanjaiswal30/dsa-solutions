import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    String roomId;
    String roomName;
    int capacity;
    List<Meeting> calendar;

    public Room(String roomName, int capacity) {
        this.roomName = roomName;
        this.capacity = capacity;
        this.roomId = UUID.randomUUID().toString();
        this.calendar = new ArrayList<>();
    }
}
