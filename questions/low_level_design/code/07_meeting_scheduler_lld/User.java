import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    String userId;
    String userName;
    List<Meeting> calendar;

    public User(String userName) {
        this.userName = userName;
        this.calendar = new ArrayList<>();
        this.userId = UUID.randomUUID().toString();
    }
}
