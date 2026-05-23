import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SplitwiseService service = SplitwiseService.getInstance();

        User aman = new User("Aman");
        User raj = new User("Raj");
        User simran = new User("Simran");

        service.addUser(aman);
        service.addUser(raj);
        service.addUser(simran);

        Group trip = new Group("Goa Trip", Arrays.asList(aman, raj, simran));
        service.addGroup(trip);

        List<User> participants = Arrays.asList(aman, raj, simran);

        service.addExpense(
                trip.getGroupId(),
                1200,
                aman,
                participants,
                new EqualSplit(),
                null,
                "Dinner"
        );

        service.addExpense(
                trip.getGroupId(),
                1000,
                raj,
                participants,
                new ExactSplit(),
                Arrays.asList(400, 300, 300),
                "Taxi"
        );

        printUserBalances(aman);
        printUserBalances(raj);
        printUserBalances(simran);
        printGroupExpenses(trip);

    }

    private static void printUserBalances(User user) {
        System.out.println("Balances for " + user.getName() + ":");
        for (Map.Entry<String, Integer> entry : user.getBalances().entrySet()) {
            System.out.println("  with userId=" + entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();
    }

    private static void printGroupExpenses(Group group) {
        System.out.println("Expenses for group: " + group.getGroupName());
        for (Expense expense : group.getExpenses()) {
            System.out.println("  " + expense.getDescription()
                    + " | amount=" + expense.getAmount()
                    + " | paidBy=" + expense.getPaidByUser().getName());

            for (Split split : expense.getSplitList()) {
                System.out.println("    " + split.getUser().getName() + " -> " + split.getAmount());
            }
        }
        System.out.println();
    }

}
