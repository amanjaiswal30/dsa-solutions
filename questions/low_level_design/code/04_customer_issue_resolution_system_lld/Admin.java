public class Admin extends User {
    public Admin(String name, String email) {
        super(name, email);
    }

    // Convenience method — actual logic lives in TicketAssignmentService
    public void reassign(Ticket ticket, Agent agent, TicketAssignmentService service) {
        service.reassignTicket(ticket, agent, this);
    }
}
