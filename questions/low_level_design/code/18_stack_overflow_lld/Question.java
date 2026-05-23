import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Question extends Post {
    String title;
    List<Tag>  tags;
    List<Answer> answerList;
    QuestionStatus questionStatus;
    public Question(String title, String content, List<Tag> tags, User createdBy) {
        super(content, createdBy);
        this.title = title;
        this.tags = tags;
        this.answerList = new ArrayList<>();
    }
}
