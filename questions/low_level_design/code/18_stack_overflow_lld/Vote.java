import java.time.LocalDateTime;
import java.util.UUID;

public class Vote {
    String id;
    User user;
    Post post;
    VoteType voteType;
    LocalDateTime createdAt;

    public Vote(User user, VoteType voteType, Post post) {
        this.user = user;
        this.voteType = voteType;
        this.post = post;
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
}
