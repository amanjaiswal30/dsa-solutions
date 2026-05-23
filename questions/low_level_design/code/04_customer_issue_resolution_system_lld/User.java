import java.util.UUID;

abstract class User {
    String name;
    String email;
    String id;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.id = UUID.randomUUID().toString();
    }
}
