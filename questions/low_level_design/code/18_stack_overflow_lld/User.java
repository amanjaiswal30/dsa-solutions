import java.util.UUID;

public class User {
    String id;
    String name;

    public User(String name) {
        this.name = name;
        this.id = UUID.randomUUID().toString();
    }
}
