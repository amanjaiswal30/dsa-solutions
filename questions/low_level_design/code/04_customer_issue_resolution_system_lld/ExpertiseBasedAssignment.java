import java.util.List;
import java.util.Optional;

public class ExpertiseBasedAssignment implements AssignmentStrategy {
    @Override
    public Agent assignTicket(Ticket ticket, List<Agent> agentList) {
        Optional<Agent> availableAgent = agentList.stream()
                .filter(agent -> agent.isAvailable && agent.expertise.contains(ticket.issueType))
                .findFirst();

        if (availableAgent.isPresent()) {
            Agent agent = availableAgent.get();
            agent.acceptTicket(ticket);   // ✅ Actually assign the ticket
            agent.isAvailable = false;    // ✅ Mark agent busy
            System.out.println("Ticket " + ticket.id.substring(0, 8)
                    + " assigned to agent: " + agent.name);
            return agent;
        }
        return null;
    }
}
