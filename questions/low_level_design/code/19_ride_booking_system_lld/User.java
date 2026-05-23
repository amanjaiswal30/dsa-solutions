import java.util.UUID;

abstract class User {
String name;
String id;
String email;

    public User(String name, String email) {
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.email = email;
    }
}
