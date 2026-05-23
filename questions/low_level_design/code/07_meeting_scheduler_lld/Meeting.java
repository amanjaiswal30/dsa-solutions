import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Meeting {
    String meetingId;
    Recurrence recurrence;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Room room;
    User organizedBy;
    List<User> participants;
    boolean isActive;

    public Meeting(Recurrence recurrence, LocalDateTime startTime, LocalDateTime endTime, Room room, User organizedBy, List<User> participants) {
        this.recurrence = recurrence;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.organizedBy = organizedBy;
        this.participants = participants;
        this.isActive = true;
        this.meetingId = UUID.randomUUID().toString();
    }
}
