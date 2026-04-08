# Low-Level Design: Elevator System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design an elevator control system for a multi-story building that efficiently handles passenger requests and optimizes wait times."*

### Clarifying Questions to Ask:

1. **Q:** How many elevators and floors are we designing for?  
   **A:** Assume 4-8 elevators, 10-50 floors. Should be scalable.

2. **Q:** What types of requests do we need to handle?  
   **A:** External requests (floor button to go up/down) and internal requests (destination floor inside elevator).

3. **Q:** Should we optimize for any specific metric?  
   **A:** Minimize average wait time and travel time for passengers.

4. **Q:** What about elevator capacity limits?  
   **A:** Each elevator has max capacity (e.g., 10 people or 1000 kg).

5. **Q:** Should we handle emergencies?  
   **A:** Yes, emergency stop, fire mode, maintenance mode.

6. **Q:** What scheduling algorithm should we use?  
   **A:** Start with SCAN (elevator algorithm), discuss alternatives.

7. **Q:** Should elevators handle express zones or priorities?  
   **A:** Nice to have - VIP access, express elevators, peak hour optimization.

8. **Q:** What about energy efficiency?  
   **A:** Idle elevators should park at optimal floors, consolidate requests.

9. **Q:** Should we track metrics?  
   **A:** Yes - average wait time, trips completed, energy usage.

10. **Q:** What about concurrent access?  
    **A:** Thread-safe operations, multiple passengers requesting simultaneously.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Core Elevator Operations (FR1-FR8)
1. Users should press UP/DOWN button on floor to request elevator
2. Users inside elevator should select destination floor
3. Elevator should move UP, DOWN, or stay IDLE
4. Elevator doors should open automatically at destination floor
5. Elevator should display current floor and direction
6. Elevator should announce floor arrival (audio/visual)
7. System should dispatch optimal elevator for each request
8. Elevator should stop at floors with pending requests in its direction

#### Request Management (FR9-FR12)
9. System should queue external requests (floor → direction)
10. System should queue internal requests (elevator → floor)
11. System should prioritize requests in current direction
12. System should handle multiple simultaneous requests

#### Safety & Capacity (FR13-FR17)
13. Elevator should not exceed max weight capacity
14. Elevator should have emergency stop button
15. Elevator should have alarm/intercom for emergencies
16. Doors should have safety sensors (prevent closing on passengers)
17. Elevator should have overload warning

#### Advanced Features (FR18-FR25)
18. System should support maintenance mode (take elevator offline)
19. System should support fire mode (all elevators go to ground floor)
20. System should support VIP/express mode (skip intermediate floors)
21. System should optimize for peak hours (morning up, evening down)
22. Idle elevators should park at strategic floors
23. System should consolidate requests (group passengers going same direction)
24. System should provide real-time status monitoring
25. System should log all trips for analytics

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many buildings? How many elevators?"
- Single building: 4-8 elevators, 10-50 floors
- Multiple buildings: 100+ elevators across campus
- Peak load: 1000 requests/hour during rush hour
- Concurrent passengers: 100-500 people

**Deduced NFRs:**
- ✅ Horizontal scaling (add more elevators without code changes)
- ✅ Configurable parameters (floors, elevators, capacity)
- ✅ Efficient request dispatching (minimize CPU overhead)
- ✅ Memory-efficient queue management

---

#### 2. **Consistency Analysis**

**Think:** "What must be accurate?"
- Elevator position (cannot be on two floors simultaneously)
- Request fulfillment (each request served exactly once)
- Capacity limits (must be enforced strictly)
- Door state (cannot open while moving)

**Deduced NFRs:**
- ✅ **Strong consistency** for elevator state (position, direction, door status)
- ✅ **Atomic operations** for request assignment
- ✅ **Transactional safety** for capacity checks
- ✅ **State machine** for elevator lifecycle (prevent invalid transitions)

---

#### 3. **Availability Analysis**

**Think:** "Acceptable downtime?"
- Elevator failure affects building operations
- High availability critical (99.9%+)
- Graceful degradation (other elevators take load)

**Deduced NFRs:**
- ✅ **99.9% availability** per elevator
- ✅ **Redundant elevators** (n+1 redundancy)
- ✅ **Graceful degradation:**
  - If 1 elevator fails → redistribute load to others
  - If controller fails → manual override mode
- ✅ **Automatic failover** for control system
- ✅ **Health monitoring** (detect stuck elevators)

---

#### 4. **Maintainability Analysis**

**Think:** "How to operate and debug?"
- Need to monitor elevator performance
- Need to debug stuck elevators
- Need to add new scheduling algorithms

**Deduced NFRs:**
- ✅ **Comprehensive logging:**
  - Every state transition
  - Every request served
  - Anomalies (long wait times, stuck elevators)
- ✅ **Real-time dashboard** (current status of all elevators)
- ✅ **Pluggable scheduling algorithms** (Strategy pattern)
- ✅ **Simulation mode** (test new algorithms without real elevators)

---

#### 5. **Performance Analysis**

**Think:** "Response time expectations?"
- Request dispatch: < 100ms (decide which elevator)
- Elevator response: < 30 seconds (average wait time)
- Door operation: 2-5 seconds
- Floor transition: 2-3 seconds per floor

**Deduced NFRs:**
- ✅ **Dispatch algorithm: O(n)** where n = number of elevators (typically < 10)
- ✅ **Request queue: O(1)** insertion and retrieval
- ✅ **Non-blocking I/O** (elevator movement doesn't block dispatch)
- ✅ **Efficient data structures:**
  - Priority queue for requests
  - HashMap for elevator lookup
  - TreeSet for sorted floor requests

---

#### 6. **Security Analysis**

**Think:** "Security risks?"
- Unauthorized access to restricted floors
- Malicious button pressing (DoS)
- Emergency system abuse
- Physical safety (door sensors)

**Deduced NFRs:**
- ✅ **Access control** (key card for restricted floors)
- ✅ **Rate limiting** (prevent button spam)
- ✅ **Safety sensors** (infrared for door obstruction)
- ✅ **Emergency override** (fire fighters can control elevators)
- ✅ **Audit logging** (track who accessed restricted floors)

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Press UP/DOWN button" | Button, Floor, Request |
| "Select destination floor" | Destination, ElevatorPanel |
| "Elevator moves UP/DOWN" | Elevator, Direction, Movement |
| "Doors open" | Door, DoorState |
| "Display current floor" | Display, FloorIndicator |
| "Dispatch optimal elevator" | Dispatcher, DispatchStrategy |
| "Queue requests" | RequestQueue |
| "Max weight capacity" | WeightSensor, Capacity |
| "Emergency stop" | EmergencyButton, EmergencyMode |
| "Maintenance mode" | MaintenanceMode |
| "Controller" | ElevatorController |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| Button | ❌ NO | Part of Floor or ElevatorPanel |
| Floor | ✅ YES | Physical location with state |
| Request | ✅ YES | Core entity with lifecycle |
| Destination | ❌ NO | Attribute of Request |
| ElevatorPanel | ✅ YES | User interface inside elevator |
| Elevator | ✅ YES | Main entity with complex state |
| Direction | ✅ YES | Enum (UP, DOWN, IDLE) |
| Movement | ❌ NO | Behavior, not entity |
| Door | ✅ YES | Entity with state machine |
| DoorState | ✅ YES | Enum (OPEN, CLOSED, OPENING, CLOSING) |
| Display | ✅ YES | Output device |
| FloorIndicator | ❌ NO | Part of Display |
| Dispatcher | ✅ YES | Core system component |
| DispatchStrategy | ✅ YES | Strategy pattern entity |
| RequestQueue | ✅ YES | Container for requests |
| WeightSensor | ✅ YES | Hardware interface |
| Capacity | ❌ NO | Attribute |
| EmergencyButton | ❌ NO | Part of ElevatorPanel |
| EmergencyMode | ✅ YES | System mode |
| MaintenanceMode | ✅ YES | System mode |
| ElevatorController | ✅ YES | Central control system |

### Final Entity List

**Core Elevator Entities:**
1. **Elevator** - Main entity with state machine
2. **Floor** - Building floor with call buttons
3. **Door** - Elevator door with state
4. **ElevatorPanel** - Control panel inside elevator
5. **Display** - Floor indicator and direction display

**Request Entities:**
6. **Request** - Abstract request
7. **ExternalRequest** - Floor call (UP/DOWN)
8. **InternalRequest** - Destination floor from inside elevator
9. **Direction** - Enum (UP, DOWN, IDLE)

**System Entities:**
10. **ElevatorController** - Central control system
11. **Dispatcher** - Assigns elevators to requests
12. **DispatchStrategy** - Interface (FCFS, SCAN, LOOK, etc.)
13. **RequestQueue** - Pending requests per elevator

**State Entities:**
14. **ElevatorState** - Enum (IDLE, MOVING_UP, MOVING_DOWN, STOPPED, MAINTENANCE, EMERGENCY)
15. **DoorState** - Enum (OPEN, CLOSED, OPENING, CLOSING)

**Sensor & Safety:**
16. **WeightSensor** - Measures current load
17. **DoorSensor** - Detects obstruction
18. **EmergencySystem** - Handles emergency scenarios

**Monitoring:**
19. **ElevatorMetrics** - Performance tracking
20. **Trip** - Journey record (analytics)

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Core Relationships

#### ElevatorController ↔ Elevator
**Conclusion:** **Aggregation** (controller manages elevators)
```
ElevatorController ◇────→ Elevator [1..*]
```

#### Elevator ↔ Floor
**Conclusion:** **Association** (elevator at a floor)
```
Elevator ─────→ Floor [1] (current floor)
```

#### Elevator ↔ Door
**Conclusion:** **Composition** (elevator owns door)
```
Elevator ◆────→ Door [1]
```

#### Elevator ↔ ElevatorPanel
**Conclusion:** **Composition** (elevator has panel)
```
Elevator ◆────→ ElevatorPanel [1]
```

---

### Pass 2: Request Relationships

#### Request → Floor
**Conclusion:** **Association** (request targets floor)
```
Request ─────→ Floor [1]
```

#### Elevator ↔ Request
**Conclusion:** **Association** (elevator serves requests)
```
Elevator ─────→ RequestQueue ◆────→ Request [0..*]
```

#### Dispatcher ↔ Request
**Conclusion:** **Association** (dispatcher assigns requests)
```
Dispatcher ─────→ Request [0..*]
```

---

### Pass 3: Strategy Relationships

#### Dispatcher ↔ DispatchStrategy
**Conclusion:** **Association** (strategy pattern)
```
Dispatcher ─────→ DispatchStrategy [1]
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| ElevatorController → Elevator | 1:N | Aggregation |
| Elevator → Floor | N:1 | Association |
| Elevator → Door | 1:1 | Composition |
| Elevator → ElevatorPanel | 1:1 | Composition |
| Elevator → RequestQueue | 1:1 | Composition |
| RequestQueue → Request | 1:N | Composition |
| Dispatcher → Request | 1:N | Association |
| Dispatcher → DispatchStrategy | 1:1 | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ <<enumeration>> │  │ <<enumeration>>  │  │ <<enumeration>> │
│   Direction     │  │  ElevatorState   │  │   DoorState     │
├─────────────────┤  ├──────────────────┤  ├─────────────────┤
│ UP              │  │ IDLE             │  │ OPEN            │
│ DOWN            │  │ MOVING_UP        │  │ CLOSED          │
│ IDLE            │  │ MOVING_DOWN      │  │ OPENING         │
└─────────────────┘  │ STOPPED          │  │ CLOSING         │
                     │ MAINTENANCE      │  └─────────────────┘
                     │ EMERGENCY        │
                     └──────────────────┘
```

---

### Class Diagram 2: Elevator & Components

```
┌───────────────────────────────────────────────────────────┐
│                      Elevator                             │
├───────────────────────────────────────────────────────────┤
│ - elevatorId: int                                         │
│ - currentFloor: int                                       │
│ - direction: Direction                                    │
│ - state: ElevatorState                                    │
│ - door: Door                         ◆────────────────┐   │
│ - panel: ElevatorPanel               ◆────────────────┼─┐ │
│ - display: Display                   ◆────────────────┼─┼─┐
│ - weightSensor: WeightSensor         ◆────────────────┼─┼─┼─┐
│ - requestQueue: RequestQueue         ◆────────────────┼─┼─┼─┼─┐
│ - capacity: int (max passengers)                      │ │ │ │ │
│ - currentLoad: int                                    │ │ │ │ │
├───────────────────────────────────────────────────────────┤
│ + Elevator(id, capacity)                                  │
│ + moveUp(): void                                          │
│ + moveDown(): void                                        │
│ + stop(): void                                            │
│ + openDoor(): void                                        │
│ + closeDoor(): void                                       │
│ + addRequest(request: Request): void                      │
│ + processNextRequest(): void                              │
│ + isOverloaded(): boolean                                 │
│ + canTakeRequest(floor: int, direction: Direction): bool  │
└───────────────────────────────────────────────────────────┘
         │         │         │         │         │
         │         │         │         │         │
         ▼         ▼         ▼         ▼         ▼
    ┌──────┐  ┌──────────┐ ┌────────┐ ┌────────┐ ┌──────────┐
    │ Door │  │Elevator  │ │Display │ │Weight  │ │Request   │
    │      │  │Panel     │ │        │ │Sensor  │ │Queue     │
    ├──────┤  ├──────────┤ ├────────┤ ├────────┤ ├──────────┤
    │-state│  │-buttons: │ │-current│ │-current│ │-upReqs:  │
    │:Door │  │  Set<Int>│ │  Floor │ │  Weight│ │  TreeSet │
    │ State│  │-emergency│ │-dir:   │ │-max:   │ │-downReqs:│
    │      │  │  Button  │ │  Dir   │ │  Weight│ │  TreeSet │
    ├──────┤  ├──────────┤ ├────────┤ ├────────┤ ├──────────┤
    │+open │  │+press(n) │ │+update │ │+getCur │ │+add(req) │
    │+close│  │+emergency│ │+show() │ │+isOver │ │+getNext()│
    └──────┘  └──────────┘ └────────┘ └────────┘ └──────────┘
```

---

### Class Diagram 3: Requests

```
┌───────────────────────────────────────────────────────────┐
│               <<abstract>>                                │
│                  Request                                  │
├───────────────────────────────────────────────────────────┤
│ - requestId: String                                       │
│ - targetFloor: int                                        │
│ - timestamp: long                                         │
│ - status: RequestStatus (PENDING, ASSIGNED, COMPLETED)    │
├───────────────────────────────────────────────────────────┤
│ + Request(targetFloor)                                    │
│ + getTargetFloor(): int                                   │
│ + markCompleted(): void                                   │
└───────────────────────────────────────────────────────────┘
                         △
                         │
        ┌────────────────┴────────────────┐
        │                                 │
        ▼                                 ▼
┌─────────────────────┐      ┌──────────────────────────┐
│  ExternalRequest    │      │   InternalRequest        │
├─────────────────────┤      ├──────────────────────────┤
│ - sourceFloor: int  │      │ - elevator: Elevator     │
│ - direction:        │      │                          │
│   Direction         │      ├──────────────────────────┤
│   (UP/DOWN)         │      │ + InternalRequest(       │
├─────────────────────┤      │   elevator, targetFloor) │
│ + ExternalRequest(  │      └──────────────────────────┘
│   floor, direction) │
└─────────────────────┘

// ExternalRequest: Someone on Floor 5 presses UP button
// InternalRequest: Someone inside elevator presses Floor 10 button
```

---

### Class Diagram 4: Controller & Dispatcher

```
┌───────────────────────────────────────────────────────────┐
│              ElevatorController                           │
├───────────────────────────────────────────────────────────┤
│ - elevators: List<Elevator>                               │
│ - dispatcher: Dispatcher                                  │
│ - floors: List<Floor>                                     │
│ - emergencySystem: EmergencySystem                        │
│ - metrics: SystemMetrics                                  │
├───────────────────────────────────────────────────────────┤
│ + ElevatorController(numElevators, numFloors)             │
│ + requestElevator(floor: int, direction: Direction): void │
│ + handleEmergency(elevatorId: int): void                  │
│ + setMaintenanceMode(elevatorId: int, enabled: bool)      │
│ + getStatus(): List<ElevatorStatus>                       │
│ + optimizeIdleElevators(): void                           │
└───────────────────────────────────────────────────────────┘
                    │
                    │ uses
                    ▼
┌───────────────────────────────────────────────────────────┐
│                   Dispatcher                              │
├───────────────────────────────────────────────────────────┤
│ - strategy: DispatchStrategy                              │
│ - pendingRequests: Queue<ExternalRequest>                 │
├───────────────────────────────────────────────────────────┤
│ + Dispatcher(strategy: DispatchStrategy)                  │
│ + assignElevator(request: ExternalRequest,                │
│                  elevators: List<Elevator>): Elevator     │
│ + setStrategy(strategy: DispatchStrategy): void           │
└───────────────────────────────────────────────────────────┘
                    │
                    │ uses
                    ▼
┌───────────────────────────────────────────────────────────┐
│          <<interface>>                                    │
│        DispatchStrategy                                   │
├───────────────────────────────────────────────────────────┤
│ + selectElevator(request: ExternalRequest,                │
│                  elevators: List<Elevator>): Elevator     │
└───────────────────────────────────────────────────────────┘
                    △
                    │
    ┌───────────────┼───────────────┬─────────────┐
    │               │               │             │
    ▼               ▼               ▼             ▼
┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐
│  FCFS   │  │   SCAN   │  │   LOOK   │  │  Shortest    │
│Strategy │  │ Strategy │  │ Strategy │  │  Seek Time   │
├─────────┤  ├──────────┤  ├──────────┤  ├──────────────┤
│+select  │  │+select() │  │+select() │  │+select()     │
│Elevator │  │          │  │          │  │(minimizes    │
│()       │  │(Elevator │  │(Like SCAN│  │ travel time) │
│(First   │  │ algorithm│  │ but      │  └──────────────┘
│ Come    │  │- goes to │  │ reverses │
│ First   │  │  end)    │  │ earlier) │
│ Served) │  └──────────┘  └──────────┘
└─────────┘
```

---

### Class Diagram 5: Floor

```
┌───────────────────────────────────────────────────────────┐
│                       Floor                               │
├───────────────────────────────────────────────────────────┤
│ - floorNumber: int                                        │
│ - upButton: Button                                        │
│ - downButton: Button                                      │
│ - display: Display (shows arriving elevator)              │
│ - hasUpRequest: boolean                                   │
│ - hasDownRequest: boolean                                 │
├───────────────────────────────────────────────────────────┤
│ + Floor(floorNumber)                                      │
│ + pressUpButton(): ExternalRequest                        │
│ + pressDownButton(): ExternalRequest                      │
│ + clearUpRequest(): void                                  │
│ + clearDownRequest(): void                                │
│ + hasActiveRequest(): boolean                             │
└───────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums

```java
// Direction.java
public enum Direction {
    UP, DOWN, IDLE;
    
    public Direction opposite() {
        if (this == UP) return DOWN;
        if (this == DOWN) return UP;
        return IDLE;
    }
}
```

```java
// ElevatorState.java
public enum ElevatorState {
    IDLE,
    MOVING_UP,
    MOVING_DOWN,
    STOPPED,
    MAINTENANCE,
    EMERGENCY
}
```

```java
// DoorState.java
public enum DoorState {
    OPEN,
    CLOSED,
    OPENING,
    CLOSING
}
```

---

### Request Classes

```java
// Request.java (Abstract)
public abstract class Request {
    private final String requestId;
    private final int targetFloor;
    private final long timestamp;
    private RequestStatus status;
    
    public Request(int targetFloor) {
        this.requestId = UUID.randomUUID().toString();
        this.targetFloor = targetFloor;
        this.timestamp = System.currentTimeMillis();
        this.status = RequestStatus.PENDING;
    }
    
    public int getTargetFloor() {
        return targetFloor;
    }
    
    public void markCompleted() {
        this.status = RequestStatus.COMPLETED;
    }
    
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    public String getRequestId() { return requestId; }
    public RequestStatus getStatus() { return status; }
}
```

```java
// ExternalRequest.java
public class ExternalRequest extends Request {
    private final int sourceFloor;
    private final Direction direction;
    
    public ExternalRequest(int sourceFloor, Direction direction) {
        super(sourceFloor); // Target is same as source for external
        this.sourceFloor = sourceFloor;
        this.direction = direction;
    }
    
    public int getSourceFloor() {
        return sourceFloor;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    @Override
    public String toString() {
        return "ExternalRequest{floor=" + sourceFloor + ", dir=" + direction + "}";
    }
}
```

```java
// InternalRequest.java
public class InternalRequest extends Request {
    private final int elevatorId;
    
    public InternalRequest(int elevatorId, int targetFloor) {
        super(targetFloor);
        this.elevatorId = elevatorId;
    }
    
    public int getElevatorId() {
        return elevatorId;
    }
    
    @Override
    public String toString() {
        return "InternalRequest{elevator=" + elevatorId + 
               ", target=" + getTargetFloor() + "}";
    }
}
```

---

### Elevator Implementation

```java
// Elevator.java
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator {
    private final int elevatorId;
    private final int capacity;
    private final AtomicInteger currentFloor;
    private Direction direction;
    private ElevatorState state;
    private final Door door;
    private final ElevatorPanel panel;
    private final WeightSensor weightSensor;
    
    // Request queues (sorted by floor number)
    private final TreeSet<Integer> upRequests;     // Floors to stop when going up
    private final TreeSet<Integer> downRequests;   // Floors to stop when going down
    
    public Elevator(int elevatorId, int capacity, int initialFloor) {
        this.elevatorId = elevatorId;
        this.capacity = capacity;
        this.currentFloor = new AtomicInteger(initialFloor);
        this.direction = Direction.IDLE;
        this.state = ElevatorState.IDLE;
        this.door = new Door();
        this.panel = new ElevatorPanel(elevatorId);
        this.weightSensor = new WeightSensor(capacity);
        this.upRequests = new TreeSet<>();
        this.downRequests = new TreeSet<>();
    }
    
    /**
     * Add floor request to elevator's queue
     */
    public synchronized void addRequest(int floor) {
        if (floor == currentFloor.get()) {
            // Already at floor
            return;
        }
        
        if (floor > currentFloor.get()) {
            upRequests.add(floor);
        } else {
            downRequests.add(floor);
        }
        
        // If idle, start moving
        if (state == ElevatorState.IDLE) {
            processNextRequest();
        }
    }
    
    /**
     * Main elevator loop - processes requests
     */
    public void processNextRequest() {
        if (state == ElevatorState.MAINTENANCE || state == ElevatorState.EMERGENCY) {
            return;
        }
        
        // Determine next floor to visit
        Integer nextFloor = getNextFloor();
        
        if (nextFloor == null) {
            // No pending requests
            direction = Direction.IDLE;
            state = ElevatorState.IDLE;
            System.out.println("Elevator " + elevatorId + " is now IDLE at floor " + currentFloor.get());
            return;
        }
        
        // Move towards next floor
        if (nextFloor > currentFloor.get()) {
            direction = Direction.UP;
            state = ElevatorState.MOVING_UP;
            moveUp();
        } else {
            direction = Direction.DOWN;
            state = ElevatorState.MOVING_DOWN;
            moveDown();
        }
    }
    
    /**
     * Get next floor to visit based on SCAN algorithm
     */
    private Integer getNextFloor() {
        if (direction == Direction.UP || direction == Direction.IDLE) {
            // Continue going up if there are requests above
            if (!upRequests.isEmpty()) {
                return upRequests.first(); // Next floor going up
            }
            // No up requests, check down requests
            if (!downRequests.isEmpty()) {
                return downRequests.last(); // Highest floor going down
            }
        } else { // direction == DOWN
            // Continue going down if there are requests below
            if (!downRequests.isEmpty()) {
                return downRequests.last(); // Next floor going down
            }
            // No down requests, check up requests
            if (!upRequests.isEmpty()) {
                return upRequests.first(); // Lowest floor going up
            }
        }
        
        return null; // No requests
    }
    
    /**
     * Move elevator up one floor
     */
    private void moveUp() {
        try {
            Thread.sleep(2000); // Simulate 2 seconds per floor
            
            int newFloor = currentFloor.incrementAndGet();
            System.out.println("Elevator " + elevatorId + " arrived at floor " + newFloor);
            
            // Check if this floor has a request
            if (upRequests.contains(newFloor)) {
                stop(newFloor);
                upRequests.remove(newFloor);
            }
            
            // Continue to next floor
            processNextRequest();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Move elevator down one floor
     */
    private void moveDown() {
        try {
            Thread.sleep(2000); // Simulate 2 seconds per floor
            
            int newFloor = currentFloor.decrementAndGet();
            System.out.println("Elevator " + elevatorId + " arrived at floor " + newFloor);
            
            // Check if this floor has a request
            if (downRequests.contains(newFloor)) {
                stop(newFloor);
                downRequests.remove(newFloor);
            }
            
            // Continue to next floor
            processNextRequest();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Stop at floor and open doors
     */
    private void stop(int floor) {
        state = ElevatorState.STOPPED;
        System.out.println("Elevator " + elevatorId + " stopped at floor " + floor);
        
        // Open doors
        door.open();
        
        try {
            Thread.sleep(3000); // Doors stay open for 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Close doors
        door.close();
    }
    
    /**
     * Check if elevator can take this request
     * (Used by dispatcher to find best elevator)
     */
    public boolean canTakeRequest(int floor, Direction requestDirection) {
        // If idle, can take any request
        if (state == ElevatorState.IDLE) {
            return true;
        }
        
        // If moving in same direction and request is on the way
        if (direction == requestDirection) {
            if (direction == Direction.UP && floor > currentFloor.get()) {
                return true;
            }
            if (direction == Direction.DOWN && floor < currentFloor.get()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculate distance to serve this request
     */
    public int calculateDistance(int targetFloor, Direction targetDirection) {
        int currentPos = currentFloor.get();
        
        // If idle, direct distance
        if (state == ElevatorState.IDLE) {
            return Math.abs(targetFloor - currentPos);
        }
        
        // If moving in same direction and target is ahead
        if (direction == targetDirection) {
            if (direction == Direction.UP && targetFloor >= currentPos) {
                return targetFloor - currentPos;
            }
            if (direction == Direction.DOWN && targetFloor <= currentPos) {
                return currentPos - targetFloor;
            }
        }
        
        // Otherwise, need to finish current direction first
        // Estimate: distance to furthest request + distance back to target
        int furthestFloor = getFurthestFloor();
        return Math.abs(furthestFloor - currentPos) + Math.abs(furthestFloor - targetFloor);
    }
    
    private int getFurthestFloor() {
        if (direction == Direction.UP && !upRequests.isEmpty()) {
            return upRequests.last();
        }
        if (direction == Direction.DOWN && !downRequests.isEmpty()) {
            return downRequests.first();
        }
        return currentFloor.get();
    }
    
    public boolean isOverloaded() {
        return weightSensor.isOverloaded();
    }
    
    public void setMaintenanceMode(boolean enabled) {
        if (enabled) {
            state = ElevatorState.MAINTENANCE;
            upRequests.clear();
            downRequests.clear();
        } else {
            state = ElevatorState.IDLE;
        }
    }
    
    public void emergencyStop() {
        state = ElevatorState.EMERGENCY;
        upRequests.clear();
        downRequests.clear();
        System.out.println("EMERGENCY: Elevator " + elevatorId + " stopped at floor " + currentFloor.get());
    }
    
    // Getters
    public int getElevatorId() { return elevatorId; }
    public int getCurrentFloor() { return currentFloor.get(); }
    public Direction getDirection() { return direction; }
    public ElevatorState getState() { return state; }
    public boolean hasPendingRequests() {
        return !upRequests.isEmpty() || !downRequests.isEmpty();
    }
}
```

---

### Dispatcher Implementation

```java
// Dispatcher.java
import java.util.*;

public class Dispatcher {
    private DispatchStrategy strategy;
    
    public Dispatcher(DispatchStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Assign best elevator for external request
     */
    public Elevator assignElevator(ExternalRequest request, List<Elevator> elevators) {
        return strategy.selectElevator(request, elevators);
    }
    
    public void setStrategy(DispatchStrategy strategy) {
        this.strategy = strategy;
    }
}
```

```java
// DispatchStrategy.java (Interface)
public interface DispatchStrategy {
    Elevator selectElevator(ExternalRequest request, List<Elevator> elevators);
}
```

```java
// SCANStrategy.java (Elevator Algorithm)
public class SCANStrategy implements DispatchStrategy {
    
    @Override
    public Elevator selectElevator(ExternalRequest request, List<Elevator> elevators) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Elevator elevator : elevators) {
            // Skip elevators in maintenance or emergency
            if (elevator.getState() == ElevatorState.MAINTENANCE ||
                elevator.getState() == ElevatorState.EMERGENCY) {
                continue;
            }
            
            // Check if elevator can take this request efficiently
            if (elevator.canTakeRequest(request.getSourceFloor(), request.getDirection())) {
                int distance = elevator.calculateDistance(
                    request.getSourceFloor(),
                    request.getDirection()
                );
                
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }
        
        // If no elevator is suitable, pick the idle one closest
        if (bestElevator == null) {
            for (Elevator elevator : elevators) {
                if (elevator.getState() == ElevatorState.IDLE) {
                    int distance = Math.abs(
                        elevator.getCurrentFloor() - request.getSourceFloor()
                    );
                    
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestElevator = elevator;
                    }
                }
            }
        }
        
        return bestElevator;
    }
}
```

---

### Elevator Controller

```java
// ElevatorController.java
import java.util.*;
import java.util.concurrent.*;

public class ElevatorController {
    private final List<Elevator> elevators;
    private final Dispatcher dispatcher;
    private final int numFloors;
    private final ExecutorService executorService;
    
    public ElevatorController(int numElevators, int numFloors) {
        this.numFloors = numFloors;
        this.elevators = new ArrayList<>();
        this.dispatcher = new Dispatcher(new SCANStrategy());
        this.executorService = Executors.newFixedThreadPool(numElevators);
        
        // Initialize elevators (start at ground floor)
        for (int i = 0; i < numElevators; i++) {
            Elevator elevator = new Elevator(i, 10, 0);
            elevators.add(elevator);
            
            // Start elevator in separate thread
            executorService.submit(() -> {
                while (true) {
                    if (elevator.hasPendingRequests()) {
                        elevator.processNextRequest();
                    } else {
                        try {
                            Thread.sleep(1000); // Check every second
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Handle external request (button press on floor)
     */
    public void requestElevator(int floor, Direction direction) {
        if (floor < 0 || floor >= numFloors) {
            throw new IllegalArgumentException("Invalid floor: " + floor);
        }
        
        ExternalRequest request = new ExternalRequest(floor, direction);
        
        // Dispatch elevator
        Elevator assignedElevator = dispatcher.assignElevator(request, elevators);
        
        if (assignedElevator != null) {
            System.out.println("Assigned Elevator " + assignedElevator.getElevatorId() + 
                             " to floor " + floor + " " + direction);
            assignedElevator.addRequest(floor);
        } else {
            System.out.println("No elevator available for floor " + floor);
        }
    }
    
    /**
     * Handle internal request (button press inside elevator)
     */
    public void selectFloor(int elevatorId, int targetFloor) {
        if (elevatorId < 0 || elevatorId >= elevators.size()) {
            throw new IllegalArgumentException("Invalid elevator ID");
        }
        
        Elevator elevator = elevators.get(elevatorId);
        elevator.addRequest(targetFloor);
        
        System.out.println("Elevator " + elevatorId + " will stop at floor " + targetFloor);
    }
    
    public void setMaintenanceMode(int elevatorId, boolean enabled) {
        elevators.get(elevatorId).setMaintenanceMode(enabled);
    }
    
    public void emergencyStop(int elevatorId) {
        elevators.get(elevatorId).emergencyStop();
    }
    
    public void shutdown() {
        executorService.shutdownNow();
    }
    
    public List<Elevator> getElevators() {
        return elevators;
    }
}
```

---

### Demo

```java
// ElevatorSystemDemo.java
public class ElevatorSystemDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Elevator System Demo ===\n");
        
        // Create system: 3 elevators, 10 floors
        ElevatorController controller = new ElevatorController(3, 10);
        
        System.out.println("Elevator system initialized with 3 elevators, 10 floors\n");
        
        // Scenario 1: External requests (people calling elevator)
        System.out.println("--- Scenario 1: External Requests ---");
        controller.requestElevator(5, Direction.UP);
        Thread.sleep(1000);
        controller.requestElevator(3, Direction.DOWN);
        Thread.sleep(1000);
        controller.requestElevator(7, Direction.UP);
        
        // Wait for elevators to arrive
        Thread.sleep(10000);
        
        // Scenario 2: Internal requests (people inside elevator)
        System.out.println("\n--- Scenario 2: Internal Requests ---");
        controller.selectFloor(0, 8);  // Elevator 0 go to floor 8
        controller.selectFloor(1, 2);  // Elevator 1 go to floor 2
        
        Thread.sleep(10000);
        
        // Scenario 3: Emergency
        System.out.println("\n--- Scenario 3: Emergency ---");
        controller.emergencyStop(0);
        
        Thread.sleep(2000);
        
        System.out.println("\n=== Demo Complete ===");
        controller.shutdown();
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern
**Where:** Dispatch strategies (SCAN, FCFS, LOOK)  
**Why:** Pluggable scheduling algorithms  
**Interview Justification:** "Different buildings may need different dispatch strategies. Strategy pattern allows swapping algorithms without changing dispatcher."

---

### Pattern 2: State Machine Pattern
**Where:** Elevator state (IDLE, MOVING_UP, MOVING_DOWN, etc.)  
**Why:** Prevent invalid state transitions  
**Interview Justification:** "Elevator cannot open doors while moving. State machine enforces valid transitions (e.g., MOVING → STOPPED → DOOR_OPEN)."

---

### Pattern 3: Singleton Pattern
**Where:** ElevatorController  
**Why:** Single control system per building  
**Interview Justification:** "One controller manages all elevators. Singleton ensures centralized coordination."

---

### Pattern 4: Observer Pattern
**Where:** Floor buttons notify controller  
**Why:** Decouple floor from controller  
**Interview Justification:** "Button press triggers event, controller observes and dispatches elevator."

---

## 💡 Step 8: Interview Discussion Points

### 1. Scheduling Algorithm: SCAN vs FCFS vs LOOK

**Interviewer:** "Why SCAN over FCFS?"

**Answer:**
"**Three main algorithms:**

**1. FCFS (First Come First Served):**
```
Requests: Floor 10 (UP), Floor 2 (DOWN), Floor 8 (UP)
Elevator at Floor 5

FCFS: 5 → 10 → 2 → 8 (total: 5+8+6 = 19 floors)
```

**Pros:** Simple, fair  
**Cons:** Inefficient (lots of back-and-forth)

---

**2. SCAN (Elevator Algorithm) - RECOMMENDED:**
```
Requests: Floor 10 (UP), Floor 2 (DOWN), Floor 8 (UP)
Elevator at Floor 5, going UP

SCAN: 5 → 8 → 10 → 2 (total: 3+2+8 = 13 floors)
      (continue direction, then reverse)
```

**Pros:** Efficient, predictable  
**Cons:** Passengers at extremes wait longer

---

**3. LOOK (Optimized SCAN):**
```
Same as SCAN but reverses at last request (not end of building)

LOOK: 5 → 8 → 10 → 2 (same as SCAN in this case)
```

**Pros:** More efficient than SCAN (doesn't go to building end)  
**Cons:** Slightly more complex

---

**My recommendation: SCAN or LOOK**

Real-world elevators use SCAN because:
- Minimizes total travel distance
- Passengers can predict arrival (\"elevator is 5 floors away\")
- No starvation (everyone eventually served)"

---

### 2. Load Balancing: How to Distribute Requests?

**Interviewer:** "How do you decide which elevator to send?"

**Answer:**
"**Factors to consider:**

**1. Distance:**
```java
int distance = Math.abs(elevator.getCurrentFloor() - requestFloor);
```

**2. Direction compatibility:**
```java
if (elevator.getDirection() == request.getDirection() &&
    isOnTheWay(elevator, request)) {
    // This elevator is already going that way
    return lowPriority;
}
```

**3. Current load:**
```java
int score = distance * 2 + numPendingRequests * 3;
// Pick elevator with lowest score
```

**4. Idle elevators:**
```java
if (elevator.getState() == IDLE) {
    return highPriority; // Idle elevators respond immediately
}
```

**Scoring algorithm:**
```java
public int calculateScore(Elevator elevator, ExternalRequest request) {
    int score = 0;
    
    // Distance penalty
    score += elevator.calculateDistance(request.getFloor()) * 10;
    
    // Load penalty (busy elevators deprioritized)
    score += elevator.getPendingRequestCount() * 5;
    
    // Direction bonus (if going same way)
    if (elevator.getDirection() == request.getDirection()) {
        score -= 20; // Bonus
    }
    
    // Idle bonus
    if (elevator.getState() == IDLE) {
        score -= 30; // Strong bonus
    }
    
    return score; // Lower score = better choice
}
```

**Pick elevator with lowest score.**"

---

### 3. Optimization: Peak Hour Handling

**Interviewer:** "How to optimize for rush hour?"

**Answer:**
"**Peak hour patterns:**

**Morning (8-9 AM):**
- Everyone goes UP from ground floor
- Solution: Park all elevators at ground floor when idle

```java
public void optimizeIdleElevators(int currentHour) {
    if (currentHour >= 8 && currentHour <= 9) {
        // Morning rush: send idle elevators to ground floor
        for (Elevator elevator : elevators) {
            if (elevator.getState() == IDLE && 
                elevator.getCurrentFloor() != 0) {
                elevator.addRequest(0);
            }
        }
    }
}
```

**Evening (5-7 PM):**
- Everyone goes DOWN to ground floor
- Solution: Distribute elevators across floors

```java
if (currentHour >= 17 && currentHour <= 19) {
    // Evening rush: distribute elevators
    for (int i = 0; i < elevators.size(); i++) {
        Elevator elevator = elevators.get(i);
        if (elevator.getState() == IDLE) {
            int targetFloor = (numFloors / elevators.size()) * i;
            if (elevator.getCurrentFloor() != targetFloor) {
                elevator.addRequest(targetFloor);
            }
        }
    }
}
```

**Lunch time (12-1 PM):**
- Mixed traffic (up and down)
- Solution: Spread elevators evenly

**Zoning strategy:**
```
Building: 20 floors, 4 elevators

Elevator 1: Floors 1-5 (LOW zone)
Elevator 2: Floors 6-10 (MID-LOW zone)
Elevator 3: Floors 11-15 (MID-HIGH zone)
Elevator 4: Floors 16-20 (HIGH zone)

Each elevator primarily serves its zone
```

**Dynamic zoning:**
- Monitor wait times per zone
- Reassign elevators to overloaded zones"

---

### 4. Safety: Overload Handling

**Interviewer:** "What happens when elevator is overloaded?"

**Answer:**
"**Multi-level safety:**

**1. Warning phase (90% capacity):**
```java
if (weightSensor.getCurrentLoad() > capacity * 0.9) {
    display.showWarning(\"Nearing capacity\");
    playSound(\"beep-beep\");
}
```

**2. Overload phase (100%+ capacity):**
```java
if (weightSensor.isOverloaded()) {
    // Prevent doors from closing
    door.preventClose();
    
    // Display message
    display.showError(\"Overloaded - please exit\");
    playSound(\"continuous-beep\");
    
    // Keep doors open until load decreases
    while (weightSensor.isOverloaded()) {
        Thread.sleep(1000);
    }
    
    // Once acceptable, allow closing
    door.allowClose();
}
```

**3. Door safety:**
```java
public void closeDoor() {
    doorState = DoorState.CLOSING;
    
    // Monitor door sensor while closing
    while (doorState == CLOSING) {
        if (doorSensor.isObstructed()) {
            // Something blocking door
            doorState = OPENING;
            playSound(\"alert\");
            
            Thread.sleep(2000); // Wait 2 seconds
            doorState = CLOSING; // Try again
        }
    }
    
    doorState = DoorState.CLOSED;
}
```

**4. Emergency scenarios:**
```java
// Fire alarm
public void handleFireEmergency() {
    for (Elevator elevator : elevators) {
        elevator.clearAllRequests();
        elevator.addRequest(0); // Go to ground floor
        elevator.setFireMode(true); // Doors stay open
    }
}

// Power outage
public void handlePowerOutage() {
    for (Elevator elevator : elevators) {
        // Move to nearest floor
        int nearest = getNearestFloor(elevator.getCurrentFloor());
        elevator.addRequest(nearest);
        
        // Open doors and disable
        elevator.openDoors();
        elevator.setMaintenanceMode(true);
    }
}
```"

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅
- `Elevator`: Manages elevator state and movement only
- `Dispatcher`: Assigns elevators only
- `Door`: Manages door state only
- `Request`: Represents request only

### Open/Closed ✅
```java
// Add new dispatch strategy without modifying dispatcher
public class NearestCarStrategy implements DispatchStrategy { }
```

### Liskov Substitution ✅
```java
// All DispatchStrategy implementations interchangeable
DispatchStrategy strategy = new SCANStrategy();
strategy = new FCFSStrategy();  // Works seamlessly
```

### Interface Segregation ✅
```java
interface Movable {
    void moveUp();
    void moveDown();
}

interface DoorControllable {
    void openDoor();
    void closeDoor();
}

// Elevator implements both, Door only implements DoorControllable
```

### Dependency Inversion ✅
```java
public class Dispatcher {
    private DispatchStrategy strategy;  // Depends on abstraction
    
    public Dispatcher(DispatchStrategy strategy) {
        this.strategy = strategy;  // Inject dependency
    }
}
```

---

## 🎯 Key Takeaways

### Architecture
- ✅ **SCAN algorithm** for efficient scheduling
- ✅ **State machine** for elevator lifecycle
- ✅ **Strategy pattern** for pluggable dispatch
- ✅ **Thread-safe** operations (AtomicInteger, synchronized)
- ✅ **TreeSet** for sorted floor requests

### Safety
- ✅ Overload detection with weight sensor
- ✅ Door obstruction detection
- ✅ Emergency stop and fire mode
- ✅ Maintenance mode

### Optimization
- ✅ Peak hour parking (ground floor morning, distributed evening)
- ✅ Zoning for tall buildings
- ✅ Idle elevator repositioning
- ✅ Direction-based batching

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
