import java.util.UUID;

public class Tag {
    String id;
    String name;
    public Tag(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
}
