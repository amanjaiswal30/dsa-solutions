import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

abstract class Post {
    String id;
    String content;
    LocalDateTime createdAt;
    List<Comment> comments;
    List<Vote> votes;
    User createdBy;

    public Post(String content, User user) {
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
        this.votes = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.createdBy = user;
    }
}
