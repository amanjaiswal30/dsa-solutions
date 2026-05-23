import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        MeetingRoomService service = MeetingRoomService.getInstance();

        User aman = new User("Aman");
        User raj = new User("Raj");
        User neha = new User("Neha");

        service.addUser(aman);
        service.addUser(raj);
        service.addUser(neha);

        Room atlas = new Room("Atlas", 4);
        service.addRoom(atlas);

        LocalDateTime start = LocalDateTime.of(2026, 5, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 10, 11, 0);

        Meeting m1 = service.createMeeting(
                Recurrence.DAILY,
                start,
                end,
                aman,
                Arrays.asList(raj, neha),
                atlas
        );
        System.out.println("Created meeting: " + m1.meetingId);

        try {
            service.createMeeting(
                    Recurrence.DAILY,
                    LocalDateTime.of(2026, 5, 10, 10, 30),
                    LocalDateTime.of(2026, 5, 10, 11, 30),
                    aman,
                    List.of(raj),
                    atlas
            );
        } catch (Exception ex) {
            System.out.println("Expected conflict: " + ex.getMessage());
        }

        boolean cancelled = service.cancelMeeting(m1.meetingId);
        System.out.println("Cancelled first meeting: " + cancelled);

        Meeting m2 = service.createMeeting(
                Recurrence.DAILY,
                LocalDateTime.of(2026, 5, 10, 10, 30),
                LocalDateTime.of(2026, 5, 10, 11, 30),
                aman,
                List.of(raj),
                atlas
        );
        System.out.println("Created after cancellation: " + m2.meetingId);

        List<Meeting> weeklySeries = service.createRecurringMeetings(
                Recurrence.WEEKLY,
                3,
                LocalDateTime.of(2026, 5, 11, 12, 0),
                LocalDateTime.of(2026, 5, 11, 13, 0),
                neha,
                List.of(aman),
                atlas
        );

        System.out.println("Created recurring meetings count: " + weeklySeries.size());
        System.out.println("Aman active meetings: " + service.getMeetingsForUser(aman).size());
        System.out.println("Atlas active meetings: " + service.getMeetingsForRoom(atlas).size());
    }
}
