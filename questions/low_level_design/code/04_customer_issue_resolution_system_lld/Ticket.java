import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    String id;
    LocalDateTime createdAt;
    Priority priority;
    String description;
    TicketStatus ticketStatus;
    IssueType issueType;
    User raisedBy;
    Agent assignedAgent; // ✅ Added

    public Ticket(Priority priority, String description, IssueType issueType, User raisedBy) {
        this.priority = priority;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
        this.issueType = issueType;
        this.raisedBy = raisedBy;
        this.ticketStatus = TicketStatus.OPEN;
        this.assignedAgent = null;
    }

    public void assignTo(Agent agent) {
        this.assignedAgent = agent;
        this.ticketStatus = TicketStatus.IN_PROGRESS;
    }

    @Override
    public String toString() {
        return "[Ticket id=" + id.substring(0, 8) + ", issue=" + issueType
                + ", priority=" + priority + ", status=" + ticketStatus
                + ", agent=" + (assignedAgent != null ? assignedAgent.name : "QUEUED") + "]";
    }
}
