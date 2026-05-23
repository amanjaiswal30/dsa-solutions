import java.util.List;

public interface AssignmentStrategy {
    Agent assignTicket(Ticket ticket, List<Agent> agentList);
}
