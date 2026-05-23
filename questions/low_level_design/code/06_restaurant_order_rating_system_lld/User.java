import java.util.UUID;

abstract class User {
    String id;
    String name;
    String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.id = UUID.randomUUID().toString();
    }
}
