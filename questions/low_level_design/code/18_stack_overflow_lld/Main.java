import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        StackOverflowService service = StackOverflowService.getInstance();

        User aman = service.registerUser("Aman");
        User rita = service.registerUser("Rita");
        User dev = service.registerUser("Dev");

        Question question = service.askQuestion(
                aman,
                "How to design Stack Overflow LLD?",
                "Need entities, service, and flow.",
                Arrays.asList("java", "lld", "design")
        );

        Answer answer1 = service.postAnswer(rita, question.id, "Start with Post abstraction.");
        Answer answer2 = service.postAnswer(dev, question.id, "Use one singleton service first.");

        service.addComment(aman, question.id, "Thanks for the quick answers!");
        service.addComment(aman, answer2.id, "Can you add repository layer later?");

        service.vote(aman, answer1.id, VoteType.UPVOTE);
        service.vote(aman, answer2.id, VoteType.UPVOTE);
        service.vote(rita, question.id, VoteType.UPVOTE);
        service.vote(dev, question.id, VoteType.DOWNVOTE);

        service.acceptAnswer(aman, question.id, answer2.id);

        System.out.println("Question: " + question.title);
        System.out.println("Question score: " + service.getScore(question.id));
        System.out.println("Question comments: " + question.comments.size());
        System.out.println("Answers count: " + question.answerList.size());

        for (Answer answer : question.answerList) {
            System.out.println(
                    "- " + answer.content
                            + " | accepted=" + answer.accepted
                            + " | score=" + service.getScore(answer.id)
                            + " | comments=" + answer.comments.size()
            );
        }
    }
}
