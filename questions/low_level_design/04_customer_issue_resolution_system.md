# Low-Level Design: Customer Issue Resolution System

**Difficulty:** Hard 🔥

**Interview Duration:** 60–75 minutes

---

## 📋 Interview Approach

Design a **ticketing / helpdesk** system (Zendesk, Freshdesk, Intercom-style): customers raise **issues**, **agents** work **queues**, **SLAs** and **workflows** drive resolution, with **threads**, **attachments**, and **CSAT** at the end.

---

## 🎯 Step 1: Understand the Problem (5–8 minutes)

### What the Interviewer Says

*"Design a system where customers can report issues, support agents pick them up from queues, communicate in threads, meet SLAs, and close tickets with optional satisfaction surveys."*

### Clarifying Questions

1. **Channels:** Email, web form, chat handoff, phone (create ticket manually)?  
2. **Identity:** Guest vs logged-in customer; link to order/account id?  
3. **Priority:** P1–P4, or impact × urgency matrix?  
4. **Assignment:** Round-robin, load-based, skill-based (queues by product line)?  
5. **SLA:** First response time vs resolution time; business hours vs 24×7?  
6. **Workflow:** Linear states vs custom per “ticket type”?  
7. **Collaboration:** Internal notes vs public replies; @mentions?  
8. **Scale:** Tickets/day, concurrent agents, attachment size?

---

## 🔹 Step 2: Requirements

### Functional

1. Create/update **Ticket** with subject, description, requester, optional account/order refs.  
2. **States:** NEW → OPEN → PENDING (customer) → ON_HOLD → SOLVED → CLOSED (with reopen rules).  
3. **Assignment** to agent or **queue** (unassigned bucket).  
4. **Threaded messages:** public reply, internal note; attachments.  
5. **SLA timers** per priority; breach flags and escalations.  
6. **Tags / categories** for routing and reporting.  
7. **Search** (keyword, id, requester) with filters (status, assignee, date).  
8. **CSAT** (1–5 or thumbs) on solved tickets.  
9. **Audit trail** of field changes.  
10. **Macros / canned responses** (optional).

### Non-functional

- **Consistency:** Ticket number unique; message ordering per thread total order.  
- **Availability:** Agents can work; customers can submit when core is up.  
- **Observability:** SLA metrics, queue depth, agent throughput.  
- **Security:** PII in tickets; role-based access (agent vs admin vs customer portal).

---

## 🔹 Step 3: Core Model (OOP)

### Entities

| Entity | Responsibility |
|--------|------------------|
| `Ticket` | Id, subject, status, priority, requester, assignee, queue, category, createdAt, slaDue |
| `Message` | Ticket id, author (user/agent/system), body, visibility (public/internal), createdAt |
| `Attachment` | Message id, url or blob ref, size, virus-scan status |
| `Queue` | Name, routing rules, default SLA policy |
| `SlaPolicy` | First response minutes, resolution minutes by priority |
| `User` | Customer, Agent, Admin — roles and permissions |
| `Macro` | Template reply text + optional field updates |

### Key operations

- `TicketService.create(request)` → assigns id, applies SLA policy, may auto-route to queue.  
- `TicketService.assign(ticketId, agentId)` — optimistic lock on ticket version.  
- `MessagingService.addMessage(ticketId, draft)` — validates state (e.g. closed → reopen on customer reply).  
- `SlaEngine.onTicketEvent(event)` — recompute deadlines; schedule reminders.

### Patterns

- **State** or explicit **state machine** for ticket lifecycle (invalid transitions rejected).  
- **Strategy** for routing (round-robin vs skill-based).  
- **Observer / domain events** for SLA clock start/stop (e.g. PENDING pauses resolution SLA).  
- **Repository** abstraction over ticket store.

---

## 🔹 Step 4: SLA Logic (interview differentiator)

- **First response SLA:** clock starts at creation; stops at first **public** agent message.  
- **Resolution SLA:** clock starts at creation; pauses when status is `PENDING` or `ON_HOLD` (configurable).  
- Store `slaBreached: boolean` and `breachAt` for reporting; use scheduled jobs or workflow engine for reminders.

---

## 🔹 Step 5: Concurrency

- **Optimistic locking** on `Ticket` (`version` field) when two agents try to assign/update.  
- **Idempotent** message post with client token to avoid duplicate emails creating duplicate tickets.

---

## 🔹 Step 6: Minimal Java-style sketch

```java
public enum TicketStatus { NEW, OPEN, PENDING, ON_HOLD, SOLVED, CLOSED }

public final class Ticket {
    private final TicketId id;
    private TicketStatus status;
    private Priority priority;
    private UserId requester;
    private Optional<UserId> assignee;
    private Instant createdAt;
    private long version;
    // transitions via TicketStateMachine
}

public interface TicketStateMachine {
    void reopenFromCustomer(Ticket t);
    void resolve(Ticket t, UserId agent);
}
```

---

## 🔹 Complexity & trade-offs

- **Normalized DB:** tickets + messages tables; attachments metadata separate from blob store.  
- **Search:** external index (OpenSearch) vs DB full-text for v1.  
- **Multi-tenancy:** `tenantId` on every row if B2B SaaS.

---

## 🔹 Follow-ups

- **Chat handoff:** continuous session creates or merges into ticket.  
- **Bots:** first-line automation before human queue.  
- **Split tickets:** parent/child for large incidents.
