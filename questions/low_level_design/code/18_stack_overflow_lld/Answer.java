public class Answer extends Post {
    boolean accepted;
    public Answer(String content, User createdBy) {
        super(content,createdBy);
        this.accepted = false;
    }
}
