import java.util.ArrayList;
import java.util.List;

public class Agent extends User {
    List<Ticket> activeTickets;    // ✅ Added
    List<Ticket> resolvedTickets;
    List<IssueType> expertise;
    boolean isAvailable;

    public Agent(String name, String email, List<IssueType> expertise) {
        super(name, email);
        this.activeTickets = new ArrayList<>();
        this.resolvedTickets = new ArrayList<>();
        this.isAvailable = true;
        this.expertise = expertise;
    }

    public void acceptTicket(Ticket ticket) {
        activeTickets.add(ticket);
        ticket.assignTo(this);
        // Mark busy if you want max 1 ticket per agent at a time:
        // this.isAvailable = false;
    }

    // ✅ Agent resolves a ticket — moves it to resolvedTickets
    public void resolveTicket(Ticket ticket, TicketAssignmentService service) {
        if (!activeTickets.contains(ticket)) {
            System.out.println("Ticket not assigned to this agent.");
            return;
        }
        ticket.ticketStatus = TicketStatus.RESOLVED;
        activeTickets.remove(ticket);
        resolvedTickets.add(ticket);
        this.isAvailable = true;
        System.out.println(name + " resolved ticket: " + ticket.id.substring(0, 8));
        // ✅ Try to drain the waiting queue now that agent is free
        service.tryAssignFromQueue(this);
    }
}
