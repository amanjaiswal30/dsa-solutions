import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Setup
        Customer customer1 = new Customer("Aman", "aman@gmail.com");
        Customer customer2 = new Customer("Sara", "sara@gmail.com");
        Agent agent1 = new Agent("Ravi", "ravi@company.com", List.of(IssueType.PAYMENT));
        Agent agent2 = new Agent("Priya", "priya@company.com", List.of(IssueType.PAYMENT, IssueType.DATA_CORRECTION));
        Admin admin = new Admin("Manisha", "manisha@company.com");

        TicketAssignmentService service = TicketAssignmentService.getInstance(
                new ExpertiseBasedAssignment(), List.of(agent1, agent2));

        // Ticket 1 — assigned to agent1
        Ticket t1 = service.assignTicket(customer1, "Payment not working", IssueType.PAYMENT, Priority.HIGH);

        // Ticket 2 — assigned to agent2
        Ticket t2 = service.assignTicket(customer2, "Wrong name on account", IssueType.DATA_CORRECTION, Priority.MEDIUM);

        // Ticket 3 — both payment agents busy, goes to queue
        Ticket t3 = service.assignTicket(customer1, "Double charged", IssueType.PAYMENT, Priority.HIGH);

        service.printQueueStatus();
        customer1.viewTickets();

        // Agent1 resolves t1 → auto-picks t3 from queue
        System.out.println("\n--- agent1 resolves ticket 1 ---");
        agent1.resolveTicket(t1, service);

        service.printQueueStatus();
        customer1.viewTickets();

        // Admin reassigns t2 to agent1
        System.out.println("\n--- Admin reassigns t2 ---");
        admin.reassign(t2, agent1, service);
    }
}
