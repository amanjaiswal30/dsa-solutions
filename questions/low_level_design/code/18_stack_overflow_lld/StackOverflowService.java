import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StackOverflowService {
    private static final StackOverflowService INSTANCE = new StackOverflowService();

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Question> questions = new LinkedHashMap<>();
    private final Map<String, Answer> answers = new LinkedHashMap<>();
    private final Map<String, Post> posts = new LinkedHashMap<>();

    private StackOverflowService() {}

    public static StackOverflowService getInstance() {
        return INSTANCE;
    }

    public User registerUser(String name) {
        User user = new User(name);
        users.put(user.id, user);
        return user;
    }

    public Question askQuestion(User user, String title, String content, List<String> tagNames) {
        validateUser(user);

        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            tags.add(new Tag(tagName));
        }

        Question question = new Question(title, content, tags, user);
        question.questionStatus = QuestionStatus.OPEN;

        questions.put(question.id, question);
        posts.put(question.id, question);
        return question;
    }

    public Answer postAnswer(User user, String questionId, String content) {
        validateUser(user);

        Question question = getExistingQuestion(questionId);
        if (question.questionStatus == QuestionStatus.CLOSED) {
            throw new IllegalStateException("Question is closed.");
        }

        Answer answer = new Answer(content, user);
        question.answerList.add(answer);

        answers.put(answer.id, answer);
        posts.put(answer.id, answer);
        return answer;
    }

    public void acceptAnswer(User questionOwner, String questionId, String answerId) {
        validateUser(questionOwner);
        Question question = getExistingQuestion(questionId);

        if (!question.createdBy.id.equals(questionOwner.id)) {
            throw new IllegalStateException("Only question owner can accept an answer.");
        }

        Answer toAccept = answers.get(answerId);
        if (toAccept == null || !question.answerList.contains(toAccept)) {
            throw new IllegalArgumentException("Answer does not belong to this question.");
        }

        for (Answer answer : question.answerList) {
            answer.accepted = false;
        }
        toAccept.accepted = true;
    }

    public Comment addComment(User user, String postId, String content) {
        validateUser(user);
        Post post = getExistingPost(postId);

        Comment comment = new Comment(user, post, content);
        post.comments.add(comment);
        return comment;
    }

    public void vote(User user, String postId, VoteType voteType) {
        validateUser(user);
        Post post = getExistingPost(postId);

        // one vote per user per post
        post.votes.removeIf(v -> v.user.id.equals(user.id));
        post.votes.add(new Vote(user, voteType, post));
    }

    public int getScore(String postId) {
        Post post = getExistingPost(postId);
        int score = 0;
        for (Vote vote : post.votes) {
            score += (vote.voteType == VoteType.UPVOTE) ? 1 : -1;
        }
        return score;
    }

    public Question getQuestionById(String questionId) {
        return getExistingQuestion(questionId);
    }

    public List<Question> listQuestions() {
        return new ArrayList<>(questions.values());
    }

    private void validateUser(User user) {
        Objects.requireNonNull(user, "User cannot be null.");
        if (!users.containsKey(user.id)) {
            throw new IllegalArgumentException("User is not registered.");
        }
    }

    private Question getExistingQuestion(String questionId) {
        Question question = questions.get(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        return question;
    }

    private Post getExistingPost(String postId) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found: " + postId);
        }
        return post;
    }
}
