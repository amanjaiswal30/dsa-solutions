import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TicketAssignmentService {
    private static TicketAssignmentService instance;
    AssignmentStrategy strategy;
    List<Agent> agentList;
    Queue<Ticket> waitingQueue; // ✅ Queue for unassigned tickets

    private TicketAssignmentService(AssignmentStrategy assignmentStrategy, List<Agent> agentList) {
        this.strategy = assignmentStrategy;
        this.agentList = agentList;
        this.waitingQueue = new LinkedList<>();
    }

    public static TicketAssignmentService getInstance(AssignmentStrategy assignmentStrategy, List<Agent> agentList) {
        if (instance == null) {
            instance = new TicketAssignmentService(assignmentStrategy, agentList);
        }
        return instance;
    }

    public Ticket assignTicket(User raisedBy, String description, IssueType issueType, Priority priority) {
        Ticket ticket = new Ticket(priority, description, issueType, raisedBy);

        // Track ticket on customer side
        if (raisedBy instanceof Customer customer) {
            customer.activeTickets.add(ticket);
        }

        Agent agent = strategy.assignTicket(ticket, agentList);
        if (agent == null) {
            // ✅ No agent available — put ticket in waiting queue
            waitingQueue.add(ticket);
            System.out.println("No agent available. Ticket " + ticket.id.substring(0, 8)
                    + " added to waiting queue. Queue size: " + waitingQueue.size());
        }
        return ticket; // always return ticket (never null) so caller can track it
    }

    // ✅ Called when an agent becomes free — try to assign queued tickets
    public void tryAssignFromQueue(Agent freedAgent) {
        if (waitingQueue.isEmpty()) return;

        // Find a queued ticket this agent can handle
        Ticket pending = waitingQueue.stream()
                .filter(t -> freedAgent.expertise.contains(t.issueType) && freedAgent.isAvailable)
                .findFirst()
                .orElse(null);

        if (pending != null) {
            waitingQueue.remove(pending);
            freedAgent.acceptTicket(pending);
            freedAgent.isAvailable = false;
            System.out.println("Queued ticket " + pending.id.substring(0, 8)
                    + " auto-assigned to freed agent: " + freedAgent.name);
        }
    }

    // ✅ Admin can manually reassign a ticket to a specific agent
    public void reassignTicket(Ticket ticket, Agent newAgent, Admin admin) {
        System.out.println("Admin " + admin.name + " reassigning ticket " + ticket.id.substring(0, 8)
                + " to agent: " + newAgent.name);

        // Remove from old agent if assigned
        if (ticket.assignedAgent != null) {
            Agent oldAgent = ticket.assignedAgent;
            oldAgent.activeTickets.remove(ticket);
            oldAgent.isAvailable = true;
        }
        // Remove from queue if waiting
        waitingQueue.remove(ticket);

        newAgent.acceptTicket(ticket);
        newAgent.isAvailable = false;
    }

    public void printQueueStatus() {
        System.out.println("=== Waiting Queue (" + waitingQueue.size() + " tickets) ===");
        waitingQueue.forEach(t -> System.out.println("  " + t));
    }
}
