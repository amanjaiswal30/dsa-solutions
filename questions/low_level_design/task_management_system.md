# Low-Level Design: Task Management System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-75 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a task management system like Jira or Trello where teams can create projects, manage tasks, track progress, and collaborate."*

### Clarifying Questions to Ask:

1. **Q:** What are the core entities we need to support?  
   **A:** Projects, Tasks, Users, Teams, Comments, Attachments.

2. **Q:** What types of users are there?  
   **A:** Admin (full access), Project Manager (manage projects), Developer (work on tasks), Viewer (read-only).

3. **Q:** What task states should we support?  
   **A:** TODO, IN_PROGRESS, IN_REVIEW, DONE, BLOCKED. Should be configurable per project.

4. **Q:** Can tasks have subtasks?  
   **A:** Yes, tasks can have multiple levels of subtasks (tree structure).

5. **Q:** What about task priority and assignment?  
   **A:** Tasks have priority (LOW, MEDIUM, HIGH, URGENT). Can be assigned to one user.

6. **Q:** Should we support sprints/iterations?  
   **A:** Yes, tasks can be grouped into sprints with start/end dates.

7. **Q:** What about task dependencies?  
   **A:** Yes, Task A can block Task B (can't start B until A is done).

8. **Q:** Should we track task history/activity?  
   **A:** Yes, maintain audit log of all changes (who changed what, when).

9. **Q:** What about notifications?  
   **A:** Yes, notify users on task assignment, status changes, comments, mentions.

10. **Q:** Should we support different project types?  
    **A:** Yes, Kanban (continuous flow) and Scrum (sprint-based).

11. **Q:** What about search and filtering?  
    **A:** Search by title, filter by assignee, status, priority, sprint, tags.

12. **Q:** Scale expectations?  
    **A:** Support 1000+ users, 100+ projects, 100,000+ tasks.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

Listed point-wise as interviewer presents them:

1. Users should be able to register and create profiles
2. Users can have different roles: Admin, Project Manager, Developer, Viewer
3. Users should be able to create projects
4. Each project has a name, description, and project type (Kanban/Scrum)
5. Projects have configurable workflow states (TODO, IN_PROGRESS, DONE, etc.)
6. Users should be able to create tasks within projects
7. Tasks have: title, description, priority, status, assignee, due date
8. Tasks can have subtasks (parent-child relationship, unlimited depth)
9. Tasks can have dependencies (Task A blocks Task B)
10. Users should be able to assign tasks to team members
11. Users should be able to change task status
12. Users should be able to add comments to tasks
13. Users should be able to attach files to tasks
14. Users should be able to add tags/labels to tasks
15. System should support sprints:
    - Sprint has name, start date, end date
    - Tasks can be added to sprints
    - Sprint has capacity and tracks progress
16. System should track task history (who changed what, when)
17. System should send notifications:
    - Task assigned to you
    - Task status changed
    - New comment on your task
    - Mentioned in comment
18. Users should be able to search tasks by title
19. Users should be able to filter tasks:
    - By assignee
    - By status
    - By priority
    - By sprint
    - By tags
20. System should generate reports:
    - Sprint burndown chart
    - User workload
    - Task completion statistics

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many users, projects, tasks?"
- 1000 users
- 100 projects
- 100,000 tasks
- 1M comments/attachments

**Deduced NFRs:**
- ✅ Database must handle 100K+ tasks efficiently
- ✅ Pagination for task lists (load 50 at a time)
- ✅ Lazy loading for subtasks (load on demand)
- ✅ Indexed searches (title, assignee, status)
- ✅ Sharding strategy for large datasets

**Interview Explanation:**
"With 100K tasks, we can't load all into memory. Need pagination, indexing, and efficient queries. Task tree can be deep - lazy load children to avoid loading entire tree."

---

#### 2. **Consistency Analysis**

**Think:** "What must be accurate?"
- Task state transitions (can't go from TODO to DONE directly if workflow requires IN_PROGRESS)
- Sprint capacity (can't over-allocate)
- Task dependencies (can't mark blocked task as done if blocker is still open)
- Audit history (for compliance)

**Deduced NFRs:**
- ✅ ACID transactions for state changes
- ✅ Optimistic locking for concurrent edits (version field)
- ✅ Workflow validation (enforce state machine)
- ✅ Persistent audit log

**Trade-off Discussion:**
"Strong consistency for task updates (one user editing at a time wins). Eventual consistency for notifications (acceptable delay of 1-2 seconds)."

---

#### 3. **Performance Analysis**

**Think:** "What operations are frequent?"
- Task list loading (most common operation)
- Task updates (status change, assignment)
- Comment posting
- Search

**Deduced NFRs:**
- ✅ Task list load < 500ms
- ✅ Task update < 200ms
- ✅ Search results < 1s
- ✅ Real-time updates for active tasks (WebSocket)

**Optimizations:**
- Cache recent tasks (Redis)
- Denormalize task summaries
- Background indexing for search

---

#### 4. **Availability Analysis**

**Think:** "What's acceptable downtime?"
- Work tracking system - teams depend on it
- Downtime = productivity loss
- But not life-critical

**Deduced NFRs:**
- ✅ 99.9% availability (< 8.7 hours downtime/year)
- ✅ Graceful degradation (read-only mode if DB issues)
- ✅ Data backups (daily)
- ✅ Multi-region deployment for distributed teams

**Why 99.9%?** Balances cost and reliability for business application.

---

#### 5. **Concurrency Analysis**

**Think:** "What can happen simultaneously?"
- Multiple users editing same task
- Multiple users adding comments to same task
- Multiple users creating tasks in same project
- User A assigns task while User B changes status

**Deduced NFRs:**
- ✅ Optimistic locking (version field on tasks)
- ✅ Last-write-wins for comments (append-only)
- ✅ Atomic operations for state changes
- ✅ Lock-free comment addition (concurrent safe)

**Critical Sections:**
- Task state change (validate workflow)
- Task assignment (update assignee)
- Sprint capacity updates

---

#### 6. **Usability Analysis**

**Think:** "How to make it intuitive?"
- Drag-and-drop for task status changes (Kanban board)
- Real-time collaboration (see who's viewing/editing)
- Keyboard shortcuts (power users)
- Rich text editor for descriptions

**Deduced NFRs:**
- ✅ WebSocket for real-time updates
- ✅ Presence tracking (show active users)
- ✅ Undo/redo for changes
- ✅ Mobile-responsive UI

---

**NFR Summary Table:**

| Dimension | Requirement | Deduction Method | Impact |
|-----------|-------------|------------------|--------|
| Scalability | 100K+ tasks | Data volume | Pagination, indexing, lazy loading |
| Consistency | Accurate state | Workflow integrity | Optimistic locking, validation |
| Performance | List load < 500ms | User expectation | Caching, denormalization |
| Availability | 99.9% uptime | Business impact | Backups, failover |
| Concurrency | Multi-user editing | Collaboration | Optimistic locking, versioning |
| Usability | Real-time updates | UX requirement | WebSocket, presence |

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Systematic Entity Identification Method

#### Step 1: Noun Extraction from Requirements

| Requirement | Nouns Identified |
|-------------|------------------|
| "Users can register and create profiles" | User, Profile |
| "Users have different roles" | Role |
| "Create projects" | Project |
| "Project types (Kanban/Scrum)" | Project Type |
| "Configurable workflow states" | Workflow, State |
| "Create tasks within projects" | Task |
| "Title, description, priority, status" | Task Attributes |
| "Tasks have subtasks" | Subtask, Parent Task |
| "Tasks have dependencies" | Dependency |
| "Assign tasks to team members" | Assignment, Team Member |
| "Add comments to tasks" | Comment |
| "Attach files to tasks" | Attachment, File |
| "Add tags/labels" | Tag, Label |
| "Support sprints" | Sprint |
| "Track task history" | History, Activity Log |
| "Send notifications" | Notification |
| "Search and filter" | Search, Filter |
| "Generate reports" | Report |

#### Step 2: Entity Validation

| Noun | Attributes? | Behavior? | Lifecycle? | Relationships? | Entity? |
|------|-------------|-----------|------------|----------------|---------|
| User | ✅ name, email, role | ✅ login, create | ✅ CRUD | ✅ owns tasks | ✅ YES |
| Profile | ⚠️ same as User | ⚠️ no unique | ⚠️ part of User | ⚠️ none | ❌ NO (merge) |
| Role | ✅ name, permissions | ✅ check access | ✅ enum/entity | ✅ belongs to user | ✅ YES (enum) |
| Project | ✅ name, desc, type | ✅ add tasks | ✅ CRUD | ✅ has tasks | ✅ YES |
| ProjectType | ✅ name | ✅ none | ✅ enum | ✅ type of project | ✅ YES (enum) |
| Workflow | ✅ states, transitions | ✅ validate | ✅ configure | ✅ belongs to project | ✅ YES |
| State | ✅ name, order | ✅ transition | ✅ enum/entity | ✅ used in workflow | ✅ YES |
| Task | ✅ title, desc, priority | ✅ assign, update | ✅ CRUD | ✅ has comments | ✅ YES |
| Subtask | ⚠️ same as Task | ⚠️ same as Task | ⚠️ same | ⚠️ just parent-child | ❌ NO (Task with parent) |
| Dependency | ✅ blocker, blocked | ✅ validate | ✅ create/delete | ✅ between tasks | ✅ YES |
| Comment | ✅ content, author | ✅ edit, delete | ✅ CRUD | ✅ belongs to task | ✅ YES |
| Attachment | ✅ file, metadata | ✅ upload, download | ✅ CRUD | ✅ belongs to task | ✅ YES |
| Tag | ✅ name, color | ✅ categorize | ✅ CRUD | ✅ many-to-many | ✅ YES |
| Sprint | ✅ name, dates, capacity | ✅ add tasks | ✅ CRUD | ✅ has tasks | ✅ YES |
| ActivityLog | ✅ action, timestamp | ✅ record | ✅ create | ✅ logs changes | ✅ YES |
| Notification | ✅ message, type | ✅ send, mark read | ✅ CRUD | ✅ belongs to user | ✅ YES |
| Report | ⚠️ computed data | ⚠️ generate | ❌ no state | ❌ none | ❌ NO (service) |

#### Step 3: Refinement and Grouping

**Group 1: User Management**
- User (with role)
- Role (enum: ADMIN, PROJECT_MANAGER, DEVELOPER, VIEWER)

**Group 2: Project Management**
- Project
- ProjectType (enum: KANBAN, SCRUM)
- Workflow (state machine configuration)
- WorkflowState (individual states in workflow)

**Group 3: Task Management (Core)**
- Task (can be parent or child)
- Priority (enum: LOW, MEDIUM, HIGH, URGENT)
- TaskStatus (current state in workflow)

**Group 4: Task Relationships**
- TaskDependency (blocker-blocked relationship)
- Comment
- Attachment
- Tag

**Group 5: Sprint/Iteration**
- Sprint (for Scrum projects)

**Group 6: Activity Tracking**
- ActivityLog (audit trail)
- Notification (user alerts)

### Final Entity List (13 Core Entities)

**Core Entities:**
1. **User** - Team member with role
2. **Role** - Enum for permissions
3. **Project** - Container for tasks
4. **ProjectType** - Enum (Kanban/Scrum)
5. **Workflow** - State machine configuration
6. **Task** - Core work item (supports parent-child)
7. **Priority** - Enum (LOW, MEDIUM, HIGH, URGENT)
8. **TaskStatus** - Current state
9. **TaskDependency** - Blocker relationship
10. **Comment** - Discussion on tasks
11. **Attachment** - Files attached to tasks
12. **Tag** - Labels for categorization
13. **Sprint** - Time-boxed iteration
14. **ActivityLog** - Audit trail
15. **Notification** - User alerts

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Three-Pass Methodology

---

### Pass 1: Inheritance Hierarchies (IS-A)

**Analysis:**

**User Hierarchy?**
```
Admin IS-A User? → No, Admin is a ROLE, not a type
Different roles share same behavior, just different permissions
```

**Decision:** NO inheritance for User. Use Role enum + permission checking.

**Task Hierarchy?**
```
Subtask IS-A Task? → No, subtask is just a Task with a parent
Epic IS-A Task? → No, epic is just a Task with children
Story/Bug IS-A Task? → Maybe (different types)
```

**Decision:** 
- Option A: Single Task entity with `type` field (simpler)
- Option B: Task hierarchy (Story, Bug, Epic - more complex)

**Choice:** **Option A** - Single Task with type field (STORY, BUG, EPIC, TASK)

**Why?** All types share 95% of attributes and behavior. Type is just a label.

---

### Pass 2: Ownership Relationships (HAS-A)

#### Project ↔ Task

**Q1:** Does Project contain Tasks? → **Yes**  
**Q2:** Can Task exist without Project? → **No**  
**Q3:** Delete Project → Delete Tasks? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
Project ◆────→ Task [0..*]
```

#### Task ↔ Comment

**Q1:** Does Task contain Comments? → **Yes**  
**Q2:** Can Comment exist without Task? → **No**  
**Q3:** Delete Task → Delete Comments? → **Yes**

**Conclusion:** **Composition**
```
Task ◆────→ Comment [0..*]
```

#### Task ↔ Attachment

**Same analysis as Comment**

**Conclusion:** **Composition**
```
Task ◆────→ Attachment [0..*]
```

#### Task ↔ Tag

**Q1:** Does Task have Tags? → **Yes**  
**Q2:** Can Tag exist without Task? → **Yes** (shared across tasks)  
**Q3:** Delete Task → Delete Tags? → **No**

**Conclusion:** **Many-to-Many** (junction table needed)
```
Task ◇────→ [TaskTag] ←────◇ Tag [N:M]
```

#### Task ↔ Task (Parent-Child)

**Q1:** Does Task contain child Tasks? → **Sometimes**  
**Q2:** Can child exist without parent? → **Yes** (can be top-level)  
**Q3:** Delete parent → Delete children? → **Optional** (configurable)

**Conclusion:** **Aggregation** (self-referential)
```
Task ◇────→ Task (parent) [0..1]
Task ◇────→ Task (children) [0..*]
```

#### Task ↔ TaskDependency

**Q1:** Does Task have dependencies? → **Yes**  
**Q2:** Can dependency exist without tasks? → **No**  
**Q3:** Delete task → Delete dependencies? → **Yes**

**Conclusion:** **Association** (separate entity for many-to-many)
```
TaskDependency references two Tasks (blocker and blocked)
```

#### User ↔ Task (Assignment)

**Q1:** Does User own assigned Tasks? → **No** (just reference)  
**Q2:** Can Task exist without assignee? → **Yes** (unassigned)  
**Q3:** Delete user → Delete tasks? → **No**

**Conclusion:** **Aggregation** (weak reference)
```
User ◇────→ Task [0..*] (assignedTasks)
```

#### Project ↔ Sprint

**Q1:** Does Project contain Sprints? → **Yes** (for Scrum)  
**Q2:** Can Sprint exist without Project? → **No**  
**Q3:** Delete Project → Delete Sprints? → **Yes**

**Conclusion:** **Composition**
```
Project ◆────→ Sprint [0..*]
```

#### Sprint ↔ Task

**Q1:** Does Sprint contain Tasks? → **Yes**  
**Q2:** Can Task exist without Sprint? → **Yes** (backlog)  
**Q3:** Delete Sprint → Delete Tasks? → **No** (move to backlog)

**Conclusion:** **Aggregation**
```
Sprint ◇────→ Task [0..*]
```

---

### Pass 3: Cardinality Mapping

| Relationship | From | To | Cardinality | Reasoning |
|--------------|------|----|-------------|-----------|
| Project → Task | 1 | 0..N | 1:N | Project has many tasks |
| Project → Sprint | 1 | 0..N | 1:N | Project has many sprints |
| Project → Workflow | 1 | 1 | 1:1 | Project has one workflow |
| Task → Comment | 1 | 0..N | 1:N | Task has many comments |
| Task → Attachment | 1 | 0..N | 1:N | Task has many attachments |
| Task → Tag | 1 | 0..N | N:M | Task-Tag many-to-many |
| Task → Task (parent) | 1 | 0..1 | N:1 | Task has optional parent |
| Task → Task (children) | 1 | 0..N | 1:N | Task has multiple children |
| User → Task (assigned) | 1 | 0..N | 1:N | User assigned to many tasks |
| User → Task (created) | 1 | 0..N | 1:N | User creates many tasks |
| Sprint → Task | 1 | 0..N | 1:N | Sprint contains many tasks |
| Task → User (assignee) | 1 | 0..1 | N:1 | Task assigned to one user |
| Task → TaskDependency | 1 | 0..N | 1:N | Task blocks many tasks |
| User → Comment | 1 | 0..N | 1:N | User posts many comments |
| Task → ActivityLog | 1 | 0..N | 1:N | Task has activity history |

---

### Special Design Decisions

#### Decision 1: Task Tree Structure

**Problem:** Tasks can have unlimited subtask depth

**Implementation:**
```java
public class Task {
    private Long id;
    private Task parent;           // Reference to parent (null if root)
    private List<Task> children;   // List of child tasks
    
    public boolean isRoot() {
        return parent == null;
    }
    
    public int getDepth() {
        return parent == null ? 0 : parent.getDepth() + 1;
    }
    
    public Task getRoot() {
        return parent == null ? this : parent.getRoot();
    }
}
```

**Interview Explanation:**
"Tasks form a tree (Composite pattern). Each task knows its parent and children. Lazy load children to avoid loading entire tree. Depth is computed recursively."

#### Decision 2: Task Dependencies (DAG)

**Problem:** Task A blocks Task B, B blocks C → can't have cycles

**Validation:**
```java
public class TaskDependencyValidator {
    public boolean canAddDependency(Task blocker, Task blocked) {
        // Check for cycle: does blocked already block blocker (directly or transitively)?
        return !createsСycle(blocker, blocked);
    }
    
    private boolean createsCycle(Task blocker, Task blocked) {
        if (blocked.equals(blocker)) return true;
        
        // DFS to check if blocker is reachable from blocked
        Set<Task> visited = new HashSet<>();
        return dfs(blocked, blocker, visited);
    }
}
```

#### Decision 3: Workflow State Machine

**Problem:** Projects have different workflows (customize states and transitions)

**Implementation:**
```java
public class Workflow {
    private List<WorkflowState> states;
    private Map<WorkflowState, List<WorkflowState>> transitions;
    
    public boolean canTransition(WorkflowState from, WorkflowState to) {
        return transitions.get(from).contains(to);
    }
}

// Default workflow:
TODO → IN_PROGRESS → IN_REVIEW → DONE
TODO → BLOCKED
```

---

### Complete Relationship Diagram

```
┌────────────┐
│   User     │
└──────┬─────┘
       │
       ├─── [creates N] ──→ Project
       │                        │
       │                        ├─── [1:N Composition] ──→ Task
       │                        │                              │
       │                        │                              ├─── [1:N Comp] ──→ Comment
       │                        │                              │
       │                        │                              ├─── [1:N Comp] ──→ Attachment
       │                        │                              │
       │                        │                              ├─── [N:M Agg] ──→ Tag
       │                        │                              │
       │                        │                              ├─── [N:1 Agg] ──→ Task (parent)
       │                        │                              │
       │                        │                              └─── [1:N Agg] ──→ Task (children)
       │                        │
       │                        ├─── [1:N Composition] ──→ Sprint
       │                        │                              │
       │                        │                              └─── [1:N Agg] ──→ Task
       │                        │
       │                        └─── [1:1 Composition] ──→ Workflow
       │
       ├─── [assigned to N] ──→ Task
       │
       ├─── [posts N] ──→ Comment
       │
       └─── [receives N] ──→ Notification


TaskDependency (separate entity):
  - blocker: Task (N:1)
  - blocked: Task (N:1)

ActivityLog:
  - task: Task (N:1)
  - user: User (N:1)
```

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: User & Role

```
┌────────────────────────────┐      ┌────────────────────────┐
│          User              │      │   <<enumeration>>      │
├────────────────────────────┤      │        Role            │
│ - id: Long                 │      ├────────────────────────┤
│ - username: String         │      │ ADMIN                  │
│ - email: String            │      │ PROJECT_MANAGER        │
│ - passwordHash: String     │      │ DEVELOPER              │
│ - role: Role           ────┼──────→ VIEWER                 │
│ - createdAt: LocalDateTime │      ├────────────────────────┤
├────────────────────────────┤      │ + getPermissions():    │
│ + User(username, email...)  │      │   Set<Permission>      │
│ + canManageProject(): bool │      └────────────────────────┘
│ + canEditTask(): bool      │
│ + canDeleteTask(): bool    │
└────────────────────────────┘
```

---

### Class Diagram 2: Project & ProjectType

```
┌──────────────────────────────────────────────────┐
│              Project                             │
├──────────────────────────────────────────────────┤
│ - id: Long                                       │
│ - name: String                                   │
│ - description: String                            │
│ - type: ProjectType                              │
│ - owner: User                    ◇───────────────┼──→ User
│ - workflow: Workflow             ◆───────────────┼──→ Workflow [1]
│ - tasks: List<Task>              ◆───────────────┼──→ Task [0..*]
│ - sprints: List<Sprint>          ◆───────────────┼──→ Sprint [0..*]
│ - members: Set<User>                             │
│ - createdAt: LocalDateTime                       │
├──────────────────────────────────────────────────┤
│ + Project(name, description, type, owner)        │
│ + addTask(task: Task): void                      │
│ + addSprint(sprint: Sprint): void                │
│ + addMember(user: User): void                    │
│ + isMember(user: User): boolean                  │
│ + getTasks(): List<Task>                         │
│ + getActivesprints(): List<Sprint>               │
└──────────────────────────────────────────────────┘

┌────────────────────────┐
│   <<enumeration>>      │
│     ProjectType        │
├────────────────────────┤
│ KANBAN                 │
│ SCRUM                  │
└────────────────────────┘
```

---

### Class Diagram 3: Task (Core Entity)

```
┌──────────────────────────────────────────────────────────┐
│                    Task                                  │
├──────────────────────────────────────────────────────────┤
│ - id: Long                                               │
│ - title: String                                          │
│ - description: String                                    │
│ - type: TaskType                                         │
│ - priority: Priority                                     │
│ - status: TaskStatus                                     │
│ - project: Project               ◇───────────────────────┼──→ Project
│ - assignee: User                 ◇───────────────────────┼──→ User [0..1]
│ - creator: User                  ◇───────────────────────┼──→ User
│ - parent: Task                   ◇───────────────────────┼──→ Task [0..1]
│ - children: List<Task>           ◇───────────────────────┼──→ Task [0..*]
│ - sprint: Sprint                 ◇───────────────────────┼──→ Sprint [0..1]
│ - comments: List<Comment>        ◆───────────────────────┼──→ Comment [0..*]
│ - attachments: List<Attachment>  ◆───────────────────────┼──→ Attachment [0..*]
│ - tags: Set<Tag>                 ◇───────────────────────┼──→ Tag [0..*]
│ - dueDate: LocalDate                                     │
│ - estimatedHours: Double                                 │
│ - actualHours: Double                                    │
│ - version: Long                                          │
│ - createdAt: LocalDateTime                               │
│ - updatedAt: LocalDateTime                               │
├──────────────────────────────────────────────────────────┤
│ + Task(title, description, project, creator)             │
│ + assignTo(user: User): void                             │
│ + changeStatus(newStatus: TaskStatus): void              │
│ + addSubtask(task: Task): void                           │
│ + addComment(comment: Comment): void                     │
│ + addAttachment(attachment: Attachment): void            │
│ + addTag(tag: Tag): void                                 │
│ + isAssignedTo(user: User): boolean                      │
│ + isRoot(): boolean                                      │
│ + getDepth(): int                                        │
│ + getRoot(): Task                                        │
│ + getAllBlockers(): List<Task>                           │
│ + getAllBlocked(): List<Task>                            │
│ + canTransitionTo(status: TaskStatus): boolean           │
└──────────────────────────────────────────────────────────┘

┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│  <<enumeration>>   │  │  <<enumeration>>   │  │  <<enumeration>>   │
│     TaskType       │  │     Priority       │  │    TaskStatus      │
├────────────────────┤  ├────────────────────┤  ├────────────────────┤
│ EPIC               │  │ LOW                │  │ TODO               │
│ STORY              │  │ MEDIUM             │  │ IN_PROGRESS        │
│ TASK               │  │ HIGH               │  │ IN_REVIEW          │
│ BUG                │  │ URGENT             │  │ DONE               │
│ SUBTASK            │  └────────────────────┘  │ BLOCKED            │
└────────────────────┘                          └────────────────────┘
```

**Key Design:**
- `version` field for optimistic locking
- Self-referential parent-child for tree structure
- Composite pattern: task can contain tasks

---

### Class Diagram 4: TaskDependency

```
┌──────────────────────────────────────────────┐
│          TaskDependency                      │
├──────────────────────────────────────────────┤
│ - id: Long                                   │
│ - blocker: Task              ────────────────┼──→ Task (blocker)
│ - blocked: Task              ────────────────┼──→ Task (blocked)
│ - type: DependencyType                       │
│ - createdAt: LocalDateTime                   │
├──────────────────────────────────────────────┤
│ + TaskDependency(blocker, blocked, type)     │
│ + isResolved(): boolean                      │
└──────────────────────────────────────────────┘

┌────────────────────────┐
│   <<enumeration>>      │
│   DependencyType       │
├────────────────────────┤
│ BLOCKS                 │
│ IS_BLOCKED_BY          │
│ RELATES_TO             │
└────────────────────────┘
```

**Purpose:** Models "Task A must be done before Task B starts"

---

### Class Diagram 5: Comment & Attachment

```
┌────────────────────────────────────┐    ┌────────────────────────────────────┐
│          Comment                   │    │          Attachment                │
├────────────────────────────────────┤    ├────────────────────────────────────┤
│ - id: Long                         │    │ - id: Long                         │
│ - content: String                  │    │ - filename: String                 │
│ - author: User         ◇───────────┼──→ │ - fileSize: Long                   │
│ - task: Task                       │    │ - contentType: String              │
│ - parentComment: Comment [0..1]    │    │ - storageUrl: String               │
│ - createdAt: LocalDateTime         │    │ - uploadedBy: User     ◇───────────┼──→
│ - updatedAt: LocalDateTime         │    │ - task: Task                       │
├────────────────────────────────────┤    │ - uploadedAt: LocalDateTime        │
│ + Comment(content, author, task)   │    ├────────────────────────────────────┤
│ + edit(newContent: String): void   │    │ + Attachment(filename, url, user)   │
│ + canEdit(user: User): boolean     │    │ + download(): InputStream          │
│ + isReply(): boolean               │    │ + canDelete(user: User): boolean   │
└────────────────────────────────────┘    └────────────────────────────────────┘
```

**Comment:** Supports threaded replies (parent comment)

---

### Class Diagram 6: Sprint

```
┌──────────────────────────────────────────────────┐
│                Sprint                            │
├──────────────────────────────────────────────────┤
│ - id: Long                                       │
│ - name: String                                   │
│ - goal: String                                   │
│ - project: Project           ◇───────────────────┼──→ Project
│ - tasks: List<Task>          ◇───────────────────┼──→ Task [0..*]
│ - startDate: LocalDate                           │
│ - endDate: LocalDate                             │
│ - capacity: Integer                              │
│ - status: SprintStatus                           │
├──────────────────────────────────────────────────┤
│ + Sprint(name, project, start, end)              │
│ + addTask(task: Task): boolean                   │
│ + removeTask(task: Task): void                   │
│ + getTotalEstimate(): Double                     │
│ + getRemainingWork(): Double                     │
│ + getCompletionPercentage(): Double              │
│ + isActive(): boolean                            │
│ + start(): void                                  │
│ + complete(): void                               │
└──────────────────────────────────────────────────┘

┌────────────────────────┐
│   <<enumeration>>      │
│    SprintStatus        │
├────────────────────────┤
│ PLANNED                │
│ ACTIVE                 │
│ COMPLETED              │
└────────────────────────┘
```

---

### Class Diagram 7: Workflow & WorkflowState

```
┌──────────────────────────────────────────────────┐
│               Workflow                           │
├──────────────────────────────────────────────────┤
│ - id: Long                                       │
│ - name: String                                   │
│ - states: List<WorkflowState>  ◆─────────────────┼──→ WorkflowState [1..*]
│ - transitions: Map<State, List<State>>           │
│ - project: Project                               │
├──────────────────────────────────────────────────┤
│ + Workflow(name)                                 │
│ + addState(state: WorkflowState): void           │
│ + addTransition(from: State, to: State): void    │
│ + canTransition(from: State, to: State): boolean │
│ + getNextStates(current: State): List<State>     │
│ + getInitialState(): WorkflowState               │
└──────────────────────────────────────────────────┘

┌────────────────────────────────────┐
│        WorkflowState               │
├────────────────────────────────────┤
│ - id: Long                         │
│ - name: String                     │
│ - order: Integer                   │
│ - isInitial: boolean               │
│ - isFinal: boolean                 │
├────────────────────────────────────┤
│ + WorkflowState(name, order)       │
└────────────────────────────────────┘
```

**State Machine:** Workflow defines valid state transitions

---

### Class Diagram 8: ActivityLog & Notification

```
┌────────────────────────────────────────┐    ┌────────────────────────────────────────┐
│         ActivityLog                    │    │          Notification                  │
├────────────────────────────────────────┤    ├────────────────────────────────────────┤
│ - id: Long                             │    │ - id: Long                             │
│ - task: Task           ◇───────────────┼──→ │ - recipient: User      ◇───────────────┼──→
│ - user: User           ◇───────────────┼──→ │ - type: NotificationType               │
│ - action: ActivityType                 │    │ - title: String                        │
│ - fieldChanged: String                 │    │ - message: String                      │
│ - oldValue: String                     │    │ - relatedTask: Task    ◇───────────────┼──→
│ - newValue: String                     │    │ - isRead: boolean                      │
│ - timestamp: LocalDateTime             │    │ - createdAt: LocalDateTime             │
├────────────────────────────────────────┤    ├────────────────────────────────────────┤
│ + ActivityLog(task, user, action)      │    │ + Notification(recipient, type...)     │
│ + getDescription(): String             │    │ + markAsRead(): void                   │
└────────────────────────────────────────┘    │ + send(): void                         │
                                              └────────────────────────────────────────┘

┌────────────────────────┐    ┌────────────────────────┐
│   <<enumeration>>      │    │   <<enumeration>>      │
│    ActivityType        │    │  NotificationType      │
├────────────────────────┤    ├────────────────────────┤
│ CREATED                │    │ TASK_ASSIGNED          │
│ UPDATED                │    │ STATUS_CHANGED         │
│ STATUS_CHANGED         │    │ COMMENT_ADDED          │
│ ASSIGNED               │    │ MENTIONED              │
│ COMMENTED              │    │ DUE_DATE_APPROACHING   │
│ ATTACHED               │    │ TASK_COMPLETED         │
└────────────────────────┘    └────────────────────────┘
```

---

### Complete System Architecture

```
              ┌──────────┐
              │   User   │
              └────┬─────┘
                   │
        ┌──────────┼──────────┐
        │                     │
        ▼                     ▼
   ┌─────────┐          ┌──────────┐
   │ Project │          │   Task   │
   └────┬────┘          └─────┬────┘
        │                     │
        ├─ Workflow           ├─ Comment
        ├─ Sprint             ├─ Attachment
        └─ Task               ├─ Tag
                              ├─ ActivityLog
                              ├─ TaskDependency
                              └─ Notification
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Implementation Strategy:
1. Bottom-up: Enums first
2. Core entities: User, Project, Task
3. Supporting: Comment, Sprint, ActivityLog
4. Services: TaskService, NotificationService
5. Validators: DependencyValidator, WorkflowValidator

---

### Enums

```java
// Role.java
public enum Role {
    ADMIN("Admin", Set.of(Permission.ALL)),
    PROJECT_MANAGER("Project Manager", 
        Set.of(Permission.CREATE_PROJECT, Permission.MANAGE_PROJECT, 
               Permission.CREATE_TASK, Permission.EDIT_TASK, Permission.DELETE_TASK)),
    DEVELOPER("Developer",
        Set.of(Permission.CREATE_TASK, Permission.EDIT_OWN_TASK, Permission.COMMENT)),
    VIEWER("Viewer", Set.of(Permission.VIEW));
    
    private final String displayName;
    private final Set<Permission> permissions;
    
    Role(String displayName, Set<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }
    
    public Set<Permission> getPermissions() {
        return permissions;
    }
    
    public boolean hasPermission(Permission permission) {
        return permissions.contains(Permission.ALL) || 
               permissions.contains(permission);
    }
}

// Permission.java
public enum Permission {
    ALL,
    VIEW,
    CREATE_PROJECT,
    MANAGE_PROJECT,
    DELETE_PROJECT,
    CREATE_TASK,
    EDIT_TASK,
    EDIT_OWN_TASK,
    DELETE_TASK,
    COMMENT,
    ASSIGN_TASK
}

// Priority.java
public enum Priority {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    URGENT(4, "Urgent");
    
    private final int level;
    private final String displayName;
    
    Priority(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    public int getLevel() {
        return level;
    }
}

// TaskStatus.java
public enum TaskStatus {
    TODO("To Do", 1),
    IN_PROGRESS("In Progress", 2),
    IN_REVIEW("In Review", 3),
    DONE("Done", 4),
    BLOCKED("Blocked", 0);
    
    private final String displayName;
    private final int order;
    
    TaskStatus(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getOrder() {
        return order;
    }
}

// TaskType.java
public enum TaskType {
    EPIC("Epic"),
    STORY("Story"),
    TASK("Task"),
    BUG("Bug"),
    SUBTASK("Subtask");
    
    private final String displayName;
    
    TaskType(String displayName) {
        this.displayName = displayName;
    }
}

// ProjectType.java
public enum ProjectType {
    KANBAN("Kanban Board"),
    SCRUM("Scrum Board");
    
    private final String displayName;
    
    ProjectType(String displayName) {
        this.displayName = displayName;
    }
}
```

---

### User

```java
// User.java
import java.time.LocalDateTime;
import java.util.*;

public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private LocalDateTime createdAt;
    
    public User(Long id, String username, String email, String password, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = hashPassword(password);
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
    
    public boolean canManageProject() {
        return role.hasPermission(Permission.MANAGE_PROJECT);
    }
    
    public boolean canEditTask() {
        return role.hasPermission(Permission.EDIT_TASK);
    }
    
    public boolean canEditTask(Task task) {
        return role.hasPermission(Permission.EDIT_TASK) ||
               (role.hasPermission(Permission.EDIT_OWN_TASK) && 
                task.isAssignedTo(this));
    }
    
    public boolean canDeleteTask() {
        return role.hasPermission(Permission.DELETE_TASK);
    }
    
    public boolean canComment() {
        return role.hasPermission(Permission.COMMENT);
    }
    
    private String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

---

### Workflow

```java
// Workflow.java
import java.util.*;

public class Workflow {
    private Long id;
    private String name;
    private List<WorkflowState> states;
    private Map<TaskStatus, List<TaskStatus>> transitions;
    
    public Workflow(String name) {
        this.name = name;
        this.states = new ArrayList<>();
        this.transitions = new EnumMap<>(TaskStatus.class);
        
        // Initialize default workflow
        initializeDefaultWorkflow();
    }
    
    private void initializeDefaultWorkflow() {
        // TODO → IN_PROGRESS, BLOCKED
        transitions.put(TaskStatus.TODO, 
            List.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED));
        
        // IN_PROGRESS → IN_REVIEW, BLOCKED, TODO
        transitions.put(TaskStatus.IN_PROGRESS,
            List.of(TaskStatus.IN_REVIEW, TaskStatus.BLOCKED, TaskStatus.TODO));
        
        // IN_REVIEW → DONE, IN_PROGRESS
        transitions.put(TaskStatus.IN_REVIEW,
            List.of(TaskStatus.DONE, TaskStatus.IN_PROGRESS));
        
        // BLOCKED → TODO, IN_PROGRESS
        transitions.put(TaskStatus.BLOCKED,
            List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
        
        // DONE → (terminal state, but allow reopening)
        transitions.put(TaskStatus.DONE,
            List.of(TaskStatus.TODO));
    }
    
    public boolean canTransition(TaskStatus from, TaskStatus to) {
        List<TaskStatus> allowed = transitions.get(from);
        return allowed != null && allowed.contains(to);
    }
    
    public List<TaskStatus> getNextStates(TaskStatus current) {
        return transitions.getOrDefault(current, Collections.emptyList());
    }
    
    public void addTransition(TaskStatus from, TaskStatus to) {
        transitions.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }
    
    public String getName() {
        return name;
    }
}
```

---

### Task (Core Entity)

```java
// Task.java
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskType type;
    private Priority priority;
    private TaskStatus status;
    private Project project;
    private User assignee;
    private User creator;
    private Task parent;
    private List<Task> children;
    private Sprint sprint;
    private List<Comment> comments;
    private List<Attachment> attachments;
    private Set<Tag> tags;
    private LocalDate dueDate;
    private Double estimatedHours;
    private Double actualHours;
    private Long version; // For optimistic locking
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Task(Long id, String title, String description, 
               Project project, User creator) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = TaskType.TASK;
        this.priority = Priority.MEDIUM;
        this.status = TaskStatus.TODO;
        this.project = project;
        this.creator = creator;
        this.children = new CopyOnWriteArrayList<>();
        this.comments = new CopyOnWriteArrayList<>();
        this.attachments = new ArrayList<>();
        this.tags = new HashSet<>();
        this.version = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void assignTo(User user) {
        if (this.assignee != null) {
            System.out.println("Reassigning task from " + 
                             assignee.getUsername() + " to " + user.getUsername());
        }
        this.assignee = user;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    public void changeStatus(TaskStatus newStatus, User changer) {
        // Validate workflow transition
        Workflow workflow = project.getWorkflow();
        if (!workflow.canTransition(this.status, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid transition: %s -> %s", status, newStatus));
        }
        
        // Validate dependencies
        if (newStatus == TaskStatus.DONE && hasUnresolvedBlockers()) {
            throw new IllegalStateException(
                "Cannot mark as done: has unresolved blockers");
        }
        
        System.out.println(String.format("[%s] Status: %s -> %s by %s",
            title, status, newStatus, changer.getUsername()));
        
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    public void addSubtask(Task subtask) {
        if (subtask.equals(this)) {
            throw new IllegalArgumentException("Task cannot be its own subtask");
        }
        
        subtask.setParent(this);
        this.children.add(subtask);
        subtask.setProject(this.project);
        
        System.out.println("Added subtask: " + subtask.getTitle() + 
                         " to " + this.title);
    }
    
    public void addComment(Comment comment) {
        comments.add(comment);
    }
    
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }
    
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.associateWithTask(this);
    }
    
    public boolean isAssignedTo(User user) {
        return assignee != null && assignee.equals(user);
    }
    
    public boolean isRoot() {
        return parent == null;
    }
    
    public int getDepth() {
        return parent == null ? 0 : parent.getDepth() + 1;
    }
    
    public Task getRoot() {
        return parent == null ? this : parent.getRoot();
    }
    
    public boolean hasUnresolvedBlockers() {
        List<TaskDependency> blockers = getBlockers();
        for (TaskDependency dep : blockers) {
            if (dep.getBlocker().getStatus() != TaskStatus.DONE) {
                return true;
            }
        }
        return false;
    }
    
    public List<TaskDependency> getBlockers() {
        // Would be populated from TaskDependencyService
        return new ArrayList<>();
    }
    
    // Setters
    public void setParent(Task parent) { this.parent = parent; }
    public void setProject(Project project) { this.project = project; }
    public void setSprint(Sprint sprint) { this.sprint = sprint; }
    public void setPriority(Priority priority) { 
        this.priority = priority;
        this.version++;
    }
    public void setDueDate(LocalDate date) { 
        this.dueDate = date;
        this.version++;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskType getType() { return type; }
    public Priority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public Project getProject() { return project; }
    public User getAssignee() { return assignee; }
    public User getCreator() { return creator; }
    public Task getParent() { return parent; }
    public List<Task> getChildren() { return new ArrayList<>(children); }
    public Sprint getSprint() { return sprint; }
    public List<Comment> getComments() { return new ArrayList<>(comments); }
    public Long getVersion() { return version; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task)) return false;
        Task other = (Task) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

---

### Project

```java
// Project.java
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Project {
    private Long id;
    private String name;
    private String description;
    private ProjectType type;
    private User owner;
    private Workflow workflow;
    private List<Task> tasks;
    private List<Sprint> sprints;
    private Set<User> members;
    private LocalDateTime createdAt;
    
    public Project(Long id, String name, String description, 
                  ProjectType type, User owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.owner = owner;
        this.workflow = new Workflow("Default Workflow");
        this.tasks = new CopyOnWriteArrayList<>();
        this.sprints = new ArrayList<>();
        this.members = new HashSet<>();
        this.members.add(owner);
        this.createdAt = LocalDateTime.now();
    }
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }
    
    public void addSprint(Sprint sprint) {
        if (type != ProjectType.SCRUM) {
            throw new IllegalStateException("Only Scrum projects support sprints");
        }
        sprints.add(sprint);
    }
    
    public void addMember(User user) {
        members.add(user);
        System.out.println("Added " + user.getUsername() + " to project: " + name);
    }
    
    public void removeMember(User user) {
        if (user.equals(owner)) {
            throw new IllegalStateException("Cannot remove project owner");
        }
        members.remove(user);
    }
    
    public boolean isMember(User user) {
        return members.contains(user);
    }
    
    public List<Task> getTasksByStatus(TaskStatus status) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getStatus() == status) {
                result.add(task);
            }
        }
        return result;
    }
    
    public List<Task> getTasksByAssignee(User user) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isAssignedTo(user)) {
                result.add(task);
            }
        }
        return result;
    }
    
    public List<Sprint> getActiveSprints() {
        List<Sprint> active = new ArrayList<>();
        for (Sprint sprint : sprints) {
            if (sprint.isActive()) {
                active.add(sprint);
            }
        }
        return active;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public ProjectType getType() { return type; }
    public User getOwner() { return owner; }
    public Workflow getWorkflow() { return workflow; }
    public List<Task> getTasks() { return new ArrayList<>(tasks); }
    public Set<User> getMembers() { return new HashSet<>(members); }
}
```

---

### Sprint

```java
// Sprint.java
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Sprint {
    private Long id;
    private String name;
    private String goal;
    private Project project;
    private List<Task> tasks;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacityPoints;
    private SprintStatus status;
    
    public Sprint(Long id, String name, Project project, 
                 LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.project = project;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tasks = new ArrayList<>();
        this.status = SprintStatus.PLANNED;
        this.capacityPoints = 0;
    }
    
    public boolean addTask(Task task) {
        if (task.getProject() != this.project) {
            System.out.println("❌ Task must belong to same project");
            return false;
        }
        
        if (status == SprintStatus.COMPLETED) {
            System.out.println("❌ Cannot add tasks to completed sprint");
            return false;
        }
        
        tasks.add(task);
        task.setSprint(this);
        System.out.println("✅ Added task to sprint: " + name);
        return true;
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setSprint(null);
    }
    
    public double getTotalEstimate() {
        return tasks.stream()
                   .mapToDouble(t -> t.getEstimatedHours() != null ? 
                                    t.getEstimatedHours() : 0)
                   .sum();
    }
    
    public double getRemainingWork() {
        return tasks.stream()
                   .filter(t -> t.getStatus() != TaskStatus.DONE)
                   .mapToDouble(t -> t.getEstimatedHours() != null ? 
                                    t.getEstimatedHours() : 0)
                   .sum();
    }
    
    public double getCompletionPercentage() {
        if (tasks.isEmpty()) return 0;
        
        long completed = tasks.stream()
                             .filter(t -> t.getStatus() == TaskStatus.DONE)
                             .count();
        
        return (completed * 100.0) / tasks.size();
    }
    
    public long getDaysRemaining() {
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
    
    public boolean isActive() {
        return status == SprintStatus.ACTIVE;
    }
    
    public void start() {
        this.status = SprintStatus.ACTIVE;
        System.out.println("🚀 Sprint started: " + name);
    }
    
    public void complete() {
        this.status = SprintStatus.COMPLETED;
        System.out.println("✅ Sprint completed: " + name);
        
        // Move incomplete tasks to backlog
        for (Task task : tasks) {
            if (task.getStatus() != TaskStatus.DONE) {
                task.setSprint(null);
                System.out.println("   Moved to backlog: " + task.getTitle());
            }
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Task> getTasks() { return new ArrayList<>(tasks); }
    public SprintStatus getStatus() { return status; }
}

// SprintStatus.java
public enum SprintStatus {
    PLANNED, ACTIVE, COMPLETED
}
```

---

### TaskDependency

```java
// TaskDependency.java
import java.time.LocalDateTime;

public class TaskDependency {
    private Long id;
    private Task blocker;
    private Task blocked;
    private DependencyType type;
    private LocalDateTime createdAt;
    
    public TaskDependency(Long id, Task blocker, Task blocked, DependencyType type) {
        this.id = id;
        this.blocker = blocker;
        this.blocked = blocked;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
    
    public boolean isResolved() {
        return blocker.getStatus() == TaskStatus.DONE;
    }
    
    // Getters
    public Long getId() { return id; }
    public Task getBlocker() { return blocker; }
    public Task getBlocked() { return blocked; }
    public DependencyType getType() { return type; }
}

// DependencyType.java
public enum DependencyType {
    BLOCKS("blocks"),
    IS_BLOCKED_BY("is blocked by"),
    RELATES_TO("relates to");
    
    private final String description;
    
    DependencyType(String description) {
        this.description = description;
    }
}
```

---

### Comment & Attachment

```java
// Comment.java
import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private String content;
    private User author;
    private Task task;
    private Comment parentComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Comment(Long id, String content, User author, Task task) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.task = task;
        this.createdAt = LocalDateTime.now();
    }
    
    public void edit(String newContent, User editor) {
        if (!canEdit(editor)) {
            throw new SecurityException("User cannot edit this comment");
        }
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean canEdit(User user) {
        return user.equals(author) || user.getRole() == Role.ADMIN;
    }
    
    public boolean isReply() {
        return parentComment != null;
    }
    
    public void setParentComment(Comment parent) {
        this.parentComment = parent;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

// Attachment.java
import java.time.LocalDateTime;

public class Attachment {
    private Long id;
    private String filename;
    private long fileSize;
    private String contentType;
    private String storageUrl;
    private User uploadedBy;
    private Task task;
    private LocalDateTime uploadedAt;
    
    public Attachment(Long id, String filename, String storageUrl, 
                     User uploadedBy, Task task) {
        this.id = id;
        this.filename = filename;
        this.storageUrl = storageUrl;
        this.uploadedBy = uploadedBy;
        this.task = task;
        this.uploadedAt = LocalDateTime.now();
    }
    
    public boolean canDelete(User user) {
        return user.equals(uploadedBy) || 
               user.canDeleteTask() ||
               task.getCreator().equals(user);
    }
    
    // Getters
    public String getFilename() { return filename; }
    public String getStorageUrl() { return storageUrl; }
}

// Tag.java
import java.util.*;

public class Tag {
    private Long id;
    private String name;
    private String color;
    private Set<Task> tasks;
    
    public Tag(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.tasks = new HashSet<>();
    }
    
    public void associateWithTask(Task task) {
        tasks.add(task);
    }
    
    public int getUsageCount() {
        return tasks.size();
    }
    
    public String getName() { return name; }
    public String getColor() { return color; }
}
```

---

### Service Layer

```java
// TaskService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TaskService {
    private static TaskService instance;
    private Map<Long, Task> tasks;
    private Map<Long, TaskDependency> dependencies;
    private AtomicLong taskIdCounter;
    private AtomicLong dependencyIdCounter;
    
    private TaskService() {
        this.tasks = new ConcurrentHashMap<>();
        this.dependencies = new ConcurrentHashMap<>();
        this.taskIdCounter = new AtomicLong(1);
        this.dependencyIdCounter = new AtomicLong(1);
    }
    
    public static synchronized TaskService getInstance() {
        if (instance == null) {
            instance = new TaskService();
        }
        return instance;
    }
    
    public Task createTask(String title, String description, 
                          Project project, User creator) {
        if (!project.isMember(creator)) {
            throw new SecurityException("User is not a project member");
        }
        
        long id = taskIdCounter.getAndIncrement();
        Task task = new Task(id, title, description, project, creator);
        
        tasks.put(id, task);
        project.addTask(task);
        
        // Log activity
        ActivityLogger.log(task, creator, ActivityType.CREATED, null, null, null);
        
        System.out.println("✅ Task created: " + title + " (ID: " + id + ")");
        return task;
    }
    
    public Task getTask(long id) {
        return tasks.get(id);
    }
    
    public void assignTask(Task task, User assignee, User assigner) {
        if (!task.getProject().isMember(assignee)) {
            throw new IllegalArgumentException("Assignee must be project member");
        }
        
        User oldAssignee = task.getAssignee();
        task.assignTo(assignee);
        
        // Log activity
        ActivityLogger.log(task, assigner, ActivityType.ASSIGNED,
            "assignee",
            oldAssignee != null ? oldAssignee.getUsername() : "Unassigned",
            assignee.getUsername());
        
        // Notify assignee
        NotificationService.getInstance().createNotification(
            assignee,
            NotificationType.TASK_ASSIGNED,
            "You have been assigned task: " + task.getTitle(),
            task
        );
    }
    
    public void changeTaskStatus(Task task, TaskStatus newStatus, User changer) {
        TaskStatus oldStatus = task.getStatus();
        
        try {
            task.changeStatus(newStatus, changer);
            
            // Log activity
            ActivityLogger.log(task, changer, ActivityType.STATUS_CHANGED,
                "status", oldStatus.name(), newStatus.name());
            
            // Notify interested parties
            if (newStatus == TaskStatus.DONE && task.getCreator() != null) {
                NotificationService.getInstance().createNotification(
                    task.getCreator(),
                    NotificationType.TASK_COMPLETED,
                    task.getTitle() + " has been completed",
                    task
                );
            }
            
        } catch (IllegalStateException e) {
            System.err.println("❌ Status change failed: " + e.getMessage());
        }
    }
    
    public boolean addDependency(Task blocker, Task blocked, DependencyType type) {
        // Validate cycle
        if (wouldCreateCycle(blocker, blocked)) {
            System.out.println("❌ Cannot add dependency: creates cycle");
            return false;
        }
        
        long id = dependencyIdCounter.getAndIncrement();
        TaskDependency dependency = new TaskDependency(id, blocker, blocked, type);
        
        dependencies.put(id, dependency);
        
        System.out.println("✅ Dependency added: " + blocker.getTitle() + 
                         " " + type.description + " " + blocked.getTitle());
        
        return true;
    }
    
    private boolean wouldCreateCycle(Task blocker, Task blocked) {
        Set<Task> visited = new HashSet<>();
        return hasCycle(blocked, blocker, visited);
    }
    
    private boolean hasCycle(Task current, Task target, Set<Task> visited) {
        if (current.equals(target)) {
            return true;
        }
        
        if (visited.contains(current)) {
            return false;
        }
        
        visited.add(current);
        
        // Check all tasks that current blocks
        for (TaskDependency dep : getDependenciesFor(current)) {
            if (dep.getBlocked().equals(current) && 
                hasCycle(dep.getBlocker(), target, visited)) {
                return true;
            }
        }
        
        return false;
    }
    
    private List<TaskDependency> getDependenciesFor(Task task) {
        List<TaskDependency> result = new ArrayList<>();
        for (TaskDependency dep : dependencies.values()) {
            if (dep.getBlocked().equals(task) || dep.getBlocker().equals(task)) {
                result.add(dep);
            }
        }
        return result;
    }
}

// NotificationService.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationService {
    private static NotificationService instance;
    private Map<Long, Notification> notifications;
    private Map<Long, List<Notification>> userNotifications;
    private AtomicLong notificationIdCounter;
    
    private NotificationService() {
        this.notifications = new ConcurrentHashMap<>();
        this.userNotifications = new ConcurrentHashMap<>();
        this.notificationIdCounter = new AtomicLong(1);
    }
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    public Notification createNotification(User recipient, NotificationType type,
                                          String message, Task relatedTask) {
        long id = notificationIdCounter.getAndIncrement();
        Notification notification = new Notification(
            id, recipient, type, message, relatedTask
        );
        
        notifications.put(id, notification);
        userNotifications.computeIfAbsent(recipient.getId(), 
                         k -> new ArrayList<>()).add(notification);
        
        System.out.println("📬 Notification sent to " + recipient.getUsername() + 
                         ": " + message);
        
        return notification;
    }
    
    public List<Notification> getUnreadNotifications(User user) {
        List<Notification> unread = new ArrayList<>();
        List<Notification> userNotifs = userNotifications.get(user.getId());
        
        if (userNotifs != null) {
            for (Notification n : userNotifs) {
                if (!n.isRead()) {
                    unread.add(n);
                }
            }
        }
        
        return unread;
    }
}

// Notification.java
import java.time.LocalDateTime;

public class Notification {
    private Long id;
    private User recipient;
    private NotificationType type;
    private String title;
    private String message;
    private Task relatedTask;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    public Notification(Long id, User recipient, NotificationType type,
                       String message, Task relatedTask) {
        this.id = id;
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.relatedTask = relatedTask;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public boolean isRead() { return isRead; }
    public String getMessage() { return message; }
}

// ActivityLogger.java
public class ActivityLogger {
    private static List<ActivityLog> logs = new ArrayList<>();
    
    public static void log(Task task, User user, ActivityType action,
                          String field, String oldValue, String newValue) {
        ActivityLog log = new ActivityLog(
            (long) logs.size() + 1,
            task, user, action, field, oldValue, newValue
        );
        logs.add(log);
    }
    
    public static List<ActivityLog> getTaskHistory(Task task) {
        List<ActivityLog> history = new ArrayList<>();
        for (ActivityLog log : logs) {
            if (log.getTask().equals(task)) {
                history.add(log);
            }
        }
        return history;
    }
}

// ActivityLog.java
import java.time.LocalDateTime;

public class ActivityLog {
    private Long id;
    private Task task;
    private User user;
    private ActivityType action;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    
    public ActivityLog(Long id, Task task, User user, ActivityType action,
                      String field, String oldValue, String newValue) {
        this.id = id;
        this.task = task;
        this.user = user;
        this.action = action;
        this.fieldChanged = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getDescription() {
        if (action == ActivityType.CREATED) {
            return user.getUsername() + " created task";
        } else if (action == ActivityType.STATUS_CHANGED) {
            return user.getUsername() + " changed status from " + 
                   oldValue + " to " + newValue;
        } else if (action == ActivityType.ASSIGNED) {
            return user.getUsername() + " assigned to " + newValue;
        }
        return user.getUsername() + " updated " + fieldChanged;
    }
    
    public Task getTask() { return task; }
    public User getUser() { return user; }
}

// ActivityType.java
public enum ActivityType {
    CREATED, UPDATED, STATUS_CHANGED, ASSIGNED, 
    PRIORITY_CHANGED, COMMENTED, ATTACHED
}

// NotificationType.java
public enum NotificationType {
    TASK_ASSIGNED, STATUS_CHANGED, COMMENT_ADDED, 
    MENTIONED, DUE_DATE_APPROACHING, TASK_COMPLETED, SPRINT_STARTED
}
```

---

### Demo Application

```java
// TaskManagementDemo.java
import java.time.LocalDate;

public class TaskManagementDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   TASK MANAGEMENT SYSTEM DEMONSTRATION       ");
        System.out.println("═══════════════════════════════════════════════\n");
        
        TaskService taskService = TaskService.getInstance();
        NotificationService notificationService = NotificationService.getInstance();
        
        // ===== SCENARIO 1: Create Users and Project =====
        System.out.println("═══ SCENARIO 1: Setup Project and Team ═══\n");
        
        User alice = new User(1L, "alice_pm", "alice@company.com", 
                             "pass123", Role.PROJECT_MANAGER);
        User bob = new User(2L, "bob_dev", "bob@company.com", 
                           "pass456", Role.DEVELOPER);
        User charlie = new User(3L, "charlie_dev", "charlie@company.com",
                               "pass789", Role.DEVELOPER);
        
        Project project = new Project(1L, "Mobile App Rewrite", 
                                     "Rebuild mobile app in React Native",
                                     ProjectType.SCRUM, alice);
        
        project.addMember(bob);
        project.addMember(charlie);
        
        System.out.println("✅ Project created: " + project.getName());
        System.out.println("   Type: " + project.getType());
        System.out.println("   Members: " + project.getMembers().size() + "\n");
        
        // ===== SCENARIO 2: Create Sprint =====
        System.out.println("═══ SCENARIO 2: Create Sprint ═══\n");
        
        Sprint sprint1 = new Sprint(1L, "Sprint 1 - Authentication",
                                   project,
                                   LocalDate.now(),
                                   LocalDate.now().plusWeeks(2));
        
        project.addSprint(sprint1);
        sprint1.start();
        
        System.out.println();
        
        // ===== SCENARIO 3: Create Tasks =====
        System.out.println("═══ SCENARIO 3: Create Tasks ═══\n");
        
        Task epic = taskService.createTask(
            "User Authentication Feature",
            "Implement complete user authentication system",
            project, alice
        );
        epic.setPriority(Priority.HIGH);
        
        Task story1 = taskService.createTask(
            "Implement Login API",
            "Create REST API for user login with JWT",
            project, alice
        );
        epic.addSubtask(story1);
        sprint1.addTask(story1);
        
        Task story2 = taskService.createTask(
            "Implement Registration API",
            "Create REST API for user registration",
            project, alice
        );
        epic.addSubtask(story2);
        sprint1.addTask(story2);
        
        System.out.println();
        
        // ===== SCENARIO 4: Assign Tasks =====
        System.out.println("═══ SCENARIO 4: Assign Tasks ═══\n");
        
        taskService.assignTask(story1, bob, alice);
        taskService.assignTask(story2, charlie, alice);
        
        System.out.println();
        
        // ===== SCENARIO 5: Add Dependencies =====
        System.out.println("═══ SCENARIO 5: Add Task Dependencies ═══\n");
        
        Task story3 = taskService.createTask(
            "Integrate Auth with Frontend",
            "Connect login/registration screens to APIs",
            project, alice
        );
        
        // story3 blocked by story1 and story2
        taskService.addDependency(story1, story3, DependencyType.BLOCKS);
        taskService.addDependency(story2, story3, DependencyType.BLOCKS);
        
        // Try to create cycle (should fail)
        System.out.println("\nTrying to create cycle...");
        boolean cycleCreated = taskService.addDependency(story3, story1, 
                                                        DependencyType.BLOCKS);
        System.out.println("Cycle creation prevented: " + !cycleCreated + "\n");
        
        // ===== SCENARIO 6: Work on Tasks =====
        System.out.println("═══ SCENARIO 6: Task Status Changes ═══\n");
        
        // Bob starts working
        taskService.changeTaskStatus(story1, TaskStatus.IN_PROGRESS, bob);
        
        // Add comment
        Comment comment1 = new Comment(1L, "Implemented JWT generation logic", 
                                      bob, story1);
        story1.addComment(comment1);
        System.out.println("💬 Comment added by " + bob.getUsername());
        
        // Move to review
        taskService.changeTaskStatus(story1, TaskStatus.IN_REVIEW, bob);
        
        // Complete task
        taskService.changeTaskStatus(story1, TaskStatus.DONE, bob);
        
        System.out.println();
        
        // ===== SCENARIO 7: Try to complete blocked task =====
        System.out.println("═══ SCENARIO 7: Blocked Task Validation ═══\n");
        
        taskService.assignTask(story3, bob, alice);
        taskService.changeTaskStatus(story3, TaskStatus.IN_PROGRESS, bob);
        
        System.out.println("Trying to complete story3 (blocked by story2)...");
        try {
            taskService.changeTaskStatus(story3, TaskStatus.DONE, bob);
        } catch (IllegalStateException e) {
            System.out.println("❌ " + e.getMessage());
        }
        
        System.out.println("\nCompleting blocker (story2) first...");
        taskService.assignTask(story2, charlie, alice);
        taskService.changeTaskStatus(story2, TaskStatus.IN_PROGRESS, charlie);
        taskService.changeTaskStatus(story2, TaskStatus.DONE, charlie);
        
        System.out.println("\nNow completing story3...");
        taskService.changeTaskStatus(story3, TaskStatus.DONE, bob);
        
        System.out.println();
        
        // ===== SCENARIO 8: Sprint Summary =====
        System.out.println("═══ SCENARIO 8: Sprint Summary ═══\n");
        
        printSprintSummary(sprint1);
        
        // ===== SCENARIO 9: Notifications =====
        System.out.println("\n═══ SCENARIO 9: User Notifications ═══\n");
        
        List<Notification> bobNotifs = notificationService.getUnreadNotifications(bob);
        System.out.println(bob.getUsername() + " has " + bobNotifs.size() + 
                         " unread notifications:");
        for (Notification n : bobNotifs) {
            System.out.println("   📬 " + n.getMessage());
        }
        
        System.out.println();
        
        List<Notification> charlieNotifs = 
            notificationService.getUnreadNotifications(charlie);
        System.out.println(charlie.getUsername() + " has " + 
                         charlieNotifs.size() + " unread notifications:");
        for (Notification n : charlieNotifs) {
            System.out.println("   📬 " + n.getMessage());
        }
        
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("         DEMO COMPLETED                        ");
        System.out.println("═══════════════════════════════════════════════");
    }
    
    private static void printSprintSummary(Sprint sprint) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║          SPRINT SUMMARY                      ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║ Name: " + sprint.getName());
        System.out.println("║ Status: " + sprint.getStatus());
        System.out.println("║ Tasks: " + sprint.getTasks().size());
        System.out.println("║ Completion: " + 
                         String.format("%.1f%%", sprint.getCompletionPercentage()));
        System.out.println("║ Days Remaining: " + sprint.getDaysRemaining());
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Composite Pattern
**Where:** Task tree (parent-child)  
**Why:** Tasks can contain subtasks recursively  
**Interview Justification:** "Composite pattern treats individual tasks and task groups uniformly. Client code doesn't need to know if task has children."

---

### Pattern 2: Observer Pattern
**Where:** Notification system  
**Why:** Multiple subscribers to task events  
**Interview Justification:** "When task status changes, multiple systems react: send notification, log activity, update sprint burndown. Observer decouples these."

---

### Pattern 3: Strategy Pattern
**Where:** Workflow strategies (Kanban vs Scrum)  
**Why:** Different project types have different workflows  
**Interview Justification:** "Kanban has continuous flow, Scrum has sprints. Strategy pattern allows switching without changing task logic."

---

### Pattern 4: Factory Pattern
**Where:** Task creation, Notification creation  
**Why:** Complex initialization logic  
**Interview Justification:** "Creating task involves: set project, validate, log activity, notify assignee. Factory centralizes this logic."

---

### Pattern 5: Chain of Responsibility
**Where:** Permission checking  
**Why:** Multiple permission levels  
**Interview Justification:** "Check: user is project member → user has role → user is task assignee. Chain stops at first success."

---

## 🔐 Step 8: Concurrency Handling

### Concurrent Scenarios

#### 1. Multiple Users Editing Same Task

**Problem:** User A and User B both load Task #42, make changes, try to save

**Solution:** **Optimistic Locking** with version field

```java
public class Task {
    @Version
    private Long version;  // Incremented on every update
    
    // When updating:
    // UPDATE tasks SET status=?, version=version+1 
    // WHERE id=? AND version=?
    
    // If version doesn't match → OptimisticLockException
}

// In service:
public synchronized void updateTask(Task task, User user) {
    Task current = getTask(task.getId());
    
    if (!current.getVersion().equals(task.getVersion())) {
        throw new OptimisticLockException(
            "Task was updated by another user. Please refresh and try again."
        );
    }
    
    // Proceed with update
    task.incrementVersion();
    tasks.put(task.getId(), task);
}
```

**Interview Explanation:**
"Optimistic locking lets both users edit simultaneously, but only first save wins. Loser gets error and must refresh. Better UX than pessimistic locking (locking on load)."

---

#### 2. Multiple Users Adding Comments Simultaneously

**Problem:** User A and User B both post comments at same time

**Solution:** **Lock-Free Append** (comments are append-only)

```java
public class Task {
    // Use CopyOnWriteArrayList for thread-safe append
    private List<Comment> comments = new CopyOnWriteArrayList<>();
    
    public void addComment(Comment comment) {
        // No locking needed - COW List handles it
        comments.add(comment);
        
        // Notify observers asynchronously
        CompletableFuture.runAsync(() -> {
            notifyCommentAdded(comment);
        });
    }
}
```

**Why lock-free works:** Comments never conflict (no overwriting)

---

#### 3. Task Assignment Race Condition

**Problem:** User A assigns Task #42 to Bob. User B assigns Task #42 to Charlie simultaneously.

**Solution:** **Synchronized Assignment**

```java
public class TaskService {
    private final Object taskLock = new Object();
    
    public void assignTask(Task task, User assignee, User assigner) {
        synchronized (taskLock) {
            User currentAssignee = task.getAssignee();
            
            if (currentAssignee != null && !currentAssignee.equals(assignee)) {
                System.out.println("Warning: Reassigning from " + 
                                 currentAssignee.getUsername());
            }
            
            task.assignTo(assignee);
            
            // Log and notify within lock
            ActivityLogger.log(task, assigner, ActivityType.ASSIGNED, 
                             "assignee", 
                             currentAssignee != null ? 
                                 currentAssignee.getUsername() : null,
                             assignee.getUsername());
        }
    }
}
```

**Alternative:** Use `AtomicReference<User>` for assignee field

---

#### 4. Sprint Capacity Updates

**Problem:** Multiple tasks added to sprint simultaneously

**Solution:** **Atomic Counters**

```java
public class Sprint {
    private AtomicInteger currentCapacity = new AtomicInteger(0);
    private int maxCapacity = 40;
    
    public synchronized boolean addTask(Task task) {
        int estimate = (int) task.getEstimatedHours();
        
        // Check capacity atomically
        int newCapacity = currentCapacity.addAndGet(estimate);
        
        if (newCapacity > maxCapacity) {
            currentCapacity.addAndGet(-estimate);  // Rollback
            System.out.println("❌ Sprint at capacity");
            return false;
        }
        
        tasks.add(task);
        task.setSprint(this);
        return true;
    }
}
```

---

#### 5. Notification Queue

**Problem:** Thousands of notifications sent simultaneously

**Solution:** **Async Processing with BlockingQueue**

```java
public class NotificationService {
    private BlockingQueue<Notification> notificationQueue;
    private ExecutorService executor;
    
    public NotificationService() {
        this.notificationQueue = new LinkedBlockingQueue<>(10000);
        this.executor = Executors.newFixedThreadPool(5);
        
        // Start consumer threads
        for (int i = 0; i < 5; i++) {
            executor.submit(new NotificationConsumer(notificationQueue));
        }
    }
    
    public void sendNotificationAsync(Notification notification) {
        try {
            notificationQueue.put(notification);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static class NotificationConsumer implements Runnable {
        private BlockingQueue<Notification> queue;
        
        public void run() {
            while (true) {
                try {
                    Notification n = queue.take();
                    sendEmail(n);  // Actual send
                    Thread.sleep(100);  // Rate limiting
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
```

---

### Concurrency Summary Table

| Scenario | Solution | Trade-off |
|----------|----------|-----------|
| Task updates | Optimistic locking | Last write wins, retry on conflict |
| Comments | Lock-free append | No conflicts possible |
| Assignments | Synchronized method | Brief lock, acceptable delay |
| Sprint capacity | Atomic counters | Strong consistency required |
| Notifications | Async queue | Eventual delivery (1-2s delay) |

---

## 💾 Step 9: Database Schema

### Tables

```sql
-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Projects table
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,  -- KANBAN, SCRUM
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_owner (owner_id)
);

-- Project members (many-to-many)
CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Workflow states
CREATE TABLE workflow_states (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    status_name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    is_initial BOOLEAN DEFAULT FALSE,
    is_final BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY (project_id, status_name)
);

-- Workflow transitions
CREATE TABLE workflow_transitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY (project_id, from_status, to_status)
);

-- Tasks table (supports tree structure)
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,  -- EPIC, STORY, TASK, BUG
    priority VARCHAR(20) NOT NULL,  -- LOW, MEDIUM, HIGH, URGENT
    status VARCHAR(20) NOT NULL,
    project_id BIGINT NOT NULL,
    assignee_id BIGINT,
    creator_id BIGINT NOT NULL,
    parent_task_id BIGINT,  -- For subtasks
    sprint_id BIGINT,
    due_date DATE,
    estimated_hours DECIMAL(6, 2),
    actual_hours DECIMAL(6, 2),
    version BIGINT DEFAULT 0,  -- Optimistic locking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (creator_id) REFERENCES users(id),
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE SET NULL,
    
    INDEX idx_project (project_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status),
    INDEX idx_parent (parent_task_id),
    INDEX idx_sprint (sprint_id),
    INDEX idx_created (created_at)
);

-- Task dependencies
CREATE TABLE task_dependencies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_task_id BIGINT NOT NULL,
    blocked_task_id BIGINT NOT NULL,
    dependency_type VARCHAR(20) NOT NULL,  -- BLOCKS, RELATES_TO
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (blocker_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY (blocker_task_id, blocked_task_id),
    INDEX idx_blocked (blocked_task_id)
);

-- Comments
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    
    INDEX idx_task (task_id),
    INDEX idx_author (author_id)
);

-- Attachments
CREATE TABLE attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    storage_url VARCHAR(500) NOT NULL,
    task_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_task (task_id)
);

-- Tags
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(7),  -- Hex color #RRGGBB
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Task-Tag junction table (many-to-many)
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Sprints
CREATE TABLE sprints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    goal TEXT,
    project_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    capacity_points INT DEFAULT 40,
    status VARCHAR(20) NOT NULL,  -- PLANNED, ACTIVE, COMPLETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project (project_id),
    INDEX idx_dates (start_date, end_date)
);

-- Activity log (audit trail)
CREATE TABLE activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,  -- CREATED, UPDATED, STATUS_CHANGED, etc.
    field_changed VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    INDEX idx_task (task_id),
    INDEX idx_user (user_id),
    INDEX idx_timestamp (timestamp)
);

-- Notifications
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200),
    message TEXT NOT NULL,
    related_task_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    INDEX idx_recipient (recipient_id),
    INDEX idx_read_status (recipient_id, is_read)
);
```

### Key Indexing Strategies

1. **Tasks by Project**: `idx_project` for "get all tasks in project"
2. **Tasks by Assignee**: `idx_assignee` for "my tasks"
3. **Tasks by Status**: `idx_status` for Kanban columns
4. **Task Tree**: `idx_parent` for loading subtasks
5. **Activity History**: `idx_task` for task audit trail
6. **Unread Notifications**: Composite index on `(recipient_id, is_read)`

---

## 💡 Step 10: Interview Discussion Points

### 1. Lost Update Problem

**Interviewer:** "What if two users update the same task simultaneously?"

**Answer:**
"We use optimistic locking with a `version` field. When User A loads Task #42 (version=5) and User B also loads it, both see version=5. When User A saves first, version becomes 6. When User B tries to save, the database checks `WHERE id=42 AND version=5` - which fails because version is now 6. User B gets an error and must refresh."

**Follow-up:** "Why not pessimistic locking (lock on load)?"

**Answer:**
"Pessimistic locking (SELECT FOR UPDATE) prevents others from even reading while one user edits. In a collaborative tool where multiple people view tasks, this creates a terrible UX. Optimistic locking lets everyone work freely - conflicts are rare and we handle them gracefully."

---

### 2. Dependency Cycle Detection

**Interviewer:** "How do you prevent circular dependencies?"

**Answer:**
"Before adding dependency 'A blocks B', we run DFS from B to check if A is reachable. If yes, it would create a cycle. Algorithm:

```java
boolean wouldCreateCycle(A, B):
    visited = {}
    return dfs(B, A, visited)

dfs(current, target, visited):
    if current == target: return true  // Cycle!
    if visited.contains(current): return false
    
    visited.add(current)
    for dependency in current.getBlockedBy():
        if dfs(dependency.blocker, target, visited):
            return true
    return false
```

Time complexity: O(V + E) where V = tasks, E = dependencies."

---

### 3. Task Tree vs Flat Structure

**Interviewer:** "Why support task trees (parent-child)? Why not flat?"

**Answer:**
"Task trees (Composite pattern) model real-world hierarchies:
- Epic → Stories → Subtasks
- Enables better organization and planning
- Can compute progress roll-up (if all children done, parent auto-done)
- Flexible depth (not limited to 3 levels)

We use lazy loading - load only immediate children, not entire tree. Self-referential foreign key `parent_task_id` makes DB queries simple."

---

### 4. Scalability to 1M Tasks

**Interviewer:** "How would you scale to 1 million tasks?"

**Answer:**

**Database:**
- Partition tasks table by `project_id` (hash sharding)
- Archive completed tasks older than 6 months to separate table
- Use read replicas for queries (assignee searches, reports)

**Caching:**
- Redis cache for active tasks (20% of tasks account for 80% of reads)
- Cache key: `task:{project_id}:{task_id}`
- TTL: 5 minutes, invalidate on update

**Search:**
- Use Elasticsearch for full-text search on title/description
- Index: `tasks` with fields: project_id, assignee_id, status, priority, tags
- Query: `GET /tasks/_search?q=assignee:bob AND status:IN_PROGRESS`

**API:**
- Pagination (50 tasks per page)
- Filter at DB level, not in memory
- Lazy load children (don't load entire subtask tree)"

---

### 5. Real-Time Updates

**Interviewer:** "How do users see updates in real-time?"

**Answer:**

**WebSocket Architecture:**
```
User A ─┐
User B ─┼─→ WebSocket Server ─→ Redis Pub/Sub
User C ─┘                            │
                                     ↓
                                  Subscribers
```

When Task #42 changes:
1. Backend publishes to Redis: `PUBLISH task:42:updates {status: "DONE"}`
2. WebSocket server subscribed to `task:42:updates`
3. Push to all connected clients viewing Task #42

**Presence tracking:**
- User opens Task #42 → WebSocket sends `SUBSCRIBE task:42`
- Server tracks: `task:42 → [alice, bob]`
- When alice edits → notify bob "Alice is editing"

**Fallback:** If WebSocket connection drops, client polls every 30 seconds."

---

### 6. Permissions at Scale

**Interviewer:** "How do you check permissions efficiently?"

**Answer:**

**Hierarchical permissions:**
```
User → Project Member → Role → Permissions
```

**Permission check:**
```java
boolean canEditTask(User user, Task task) {
    // 1. Check project membership
    if (!task.getProject().isMember(user)) return false;
    
    // 2. Check role permissions
    if (user.getRole().hasPermission(Permission.EDIT_TASK)) return true;
    
    // 3. Check ownership
    if (user.getRole().hasPermission(Permission.EDIT_OWN_TASK) 
        && task.isAssignedTo(user)) return true;
    
    return false;
}
```

**Optimization:**
- Cache user permissions in JWT token (avoid DB lookup on every request)
- Project membership cached in Redis (Set: `project:123:members → {alice, bob}`)
- Bulk permission checks for list views (single query for 50 tasks)"

---

### 7. Sprint Burndown Chart

**Interviewer:** "How would you generate a burndown chart?"

**Answer:**

**Daily snapshots:**
```sql
CREATE TABLE sprint_snapshots (
    id BIGINT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    date DATE NOT NULL,
    remaining_hours DECIMAL(8, 2),
    completed_tasks INT,
    UNIQUE (sprint_id, date)
);
```

**Nightly job:**
```java
@Scheduled(cron = "0 0 1 * * *")  // 1 AM daily
public void captureSprintSnapshots() {
    for (Sprint sprint : getActiveSprints()) {
        double remaining = sprint.getRemainingWork();
        int completed = sprint.getCompletedTaskCount();
        
        saveSnapshot(sprint, LocalDate.now(), remaining, completed);
    }
}
```

**Chart API:**
```java
GET /sprints/123/burndown

Response:
{
  "ideal": [40, 35, 30, 25, 20, 15, 10, 5, 0],
  "actual": [40, 38, 32, 28, 20, 15, 12, 8, 5]
}
```

**Interview Tip:** Mention trade-off: real-time calculation vs pre-computed snapshots (accuracy vs performance)."

---

## 🏆 SOLID Principles Verification

### Single Responsibility Principle (SRP) ✅

**Each class has ONE reason to change:**

| Class | Responsibility | Reason to Change |
|-------|---------------|------------------|
| `User` | User identity, authentication | User model changes |
| `Task` | Task state management | Task attributes change |
| `TaskService` | Task business logic | Task operations change |
| `NotificationService` | Notification delivery | Notification logic changes |
| `Workflow` | State machine validation | Workflow rules change |

**Anti-pattern avoided:**
```java
// ❌ BAD: Task class doing too much
public class Task {
    public void save() { /* DB logic */ }
    public void sendEmail() { /* Email logic */ }
    public void generatePDF() { /* Report logic */ }
}
```

---

### Open/Closed Principle (OCP) ✅

**Open for extension, closed for modification:**

```java
// Workflow is extensible without modifying core logic
public class CustomWorkflow extends Workflow {
    public CustomWorkflow() {
        super("Agile Workflow");
        
        // Add custom transitions
        addTransition(TaskStatus.TODO, TaskStatus.BLOCKED);
        addTransition(TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS);
    }
    
    @Override
    public boolean canTransition(TaskStatus from, TaskStatus to) {
        // Custom validation
        if (from == TaskStatus.DONE && to == TaskStatus.TODO) {
            return userIsAdmin();  // Only admins can reopen
        }
        return super.canTransition(from, to);
    }
}
```

---

### Liskov Substitution Principle (LSP) ✅

**Subtypes must be substitutable for base types:**

```java
// Task can be a regular task or subtask - both behave the same
Task epic = new Task(...);
Task subtask = new Task(...);

epic.addSubtask(subtask);  // Subtask IS-A Task
subtask.addSubtask(anotherTask);  // Subtask can also have children

// LSP: Any code expecting Task works with both
void processTask(Task task) {
    task.changeStatus(TaskStatus.DONE, user);  // Works for both
}
```

---

### Interface Segregation Principle (ISP) ✅

**Clients shouldn't depend on interfaces they don't use:**

```java
// Instead of one bloated interface:
// ❌ interface TaskOperations {
//     void create(); void update(); void delete();
//     void assign(); void comment(); void attach();
// }

// Segregated interfaces:
interface Assignable {
    void assignTo(User user);
}

interface Commentable {
    void addComment(Comment comment);
}

interface Attachable {
    void addAttachment(Attachment file);
}

// Task implements only what it needs
public class Task implements Assignable, Commentable, Attachable { ... }
```

---

### Dependency Inversion Principle (DIP) ✅

**Depend on abstractions, not concretions:**

```java
// ❌ BAD: TaskService directly depends on EmailNotifier
public class TaskService {
    private EmailNotifier notifier = new EmailNotifier();
    
    public void assignTask(Task task, User user) {
        task.assignTo(user);
        notifier.sendEmail(user, "Task assigned");  // Tightly coupled
    }
}

// ✅ GOOD: Depend on abstraction
public interface Notifier {
    void sendNotification(User user, String message);
}

public class TaskService {
    private Notifier notifier;  // Abstraction
    
    public TaskService(Notifier notifier) {
        this.notifier = notifier;  // Injected
    }
    
    public void assignTask(Task task, User user) {
        task.assignTo(user);
        notifier.sendNotification(user, "Task assigned");
    }
}

// Can inject EmailNotifier, SlackNotifier, or PushNotifier
```

---

## 🎯 Key Takeaways

This Task Management System showcases:

### Core Patterns
- ✅ **Composite Pattern** - Task tree (parent-child hierarchy)
- ✅ **Observer Pattern** - Notification system
- ✅ **Strategy Pattern** - Workflow strategies (Kanban vs Scrum)
- ✅ **Factory Pattern** - Task/Notification creation
- ✅ **Chain of Responsibility** - Permission checking

### Technical Highlights
- ✅ **Graph Algorithms** - Dependency cycle detection (DFS)
- ✅ **Optimistic Locking** - Concurrent task edits (version field)
- ✅ **State Machine** - Workflow validation
- ✅ **Tree Traversal** - Task depth calculation, root finding
- ✅ **Self-Referential Relationships** - Parent-child tasks

### System Design
- ✅ **Many-to-Many** - Project members, task tags
- ✅ **Aggregation vs Composition** - Sprint-task vs Project-task
- ✅ **Role-Based Access Control** - Permission system
- ✅ **Audit Trail** - Activity logging
- ✅ **Async Processing** - Notification queue

### Interview Readiness
- ✅ Systematic NFR deduction (SCAMPS framework)
- ✅ Concurrency handling (race conditions, deadlocks)
- ✅ Scalability strategies (caching, sharding, search)
- ✅ SOLID principles application
- ✅ Trade-off discussions (consistency vs performance)

---

**Total: 136 DSA + 6 LLD Problems**

All changes ready for review!
