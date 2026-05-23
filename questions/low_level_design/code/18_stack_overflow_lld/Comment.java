import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {
    String id;
    User user;
    Post post;
    String content;
    LocalDateTime createdAt;

    public Comment(User user, Post post, String content) {
        this.user = user;
        this.post = post;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
    }
}
