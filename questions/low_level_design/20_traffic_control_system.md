# Low-Level Design: Traffic Control System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-75 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a traffic signal control system that manages traffic lights at intersections, ensures safety, and optimizes traffic flow."*

### Clarifying Questions to Ask:

1. **Q:** What types of intersections should we support?  
   **A:** 4-way intersections (North, South, East, West) and T-junctions (3 directions).

2. **Q:** What signal types do we need?  
   **A:** Vehicle signals (Red, Yellow, Green) and pedestrian signals (Walk, Don't Walk).

3. **Q:** How should signals transition?  
   **A:** Fixed-time cycles initially. Red → Green → Yellow → Red. Green duration can vary by direction.

4. **Q:** What about pedestrian crossings?  
   **A:** Pedestrian signals should coordinate with vehicle signals. Walk only when vehicles have red.

5. **Q:** Should the system handle emergency vehicles?  
   **A:** Yes, ability to override normal operation and create green corridors.

6. **Q:** Do we need to prevent conflicts?  
   **A:** Critical - must never have conflicting green lights (e.g., North-South and East-West both green).

7. **Q:** Should timing be adaptive based on traffic?  
   **A:** Start with fixed timing. Mention adaptive as extension.

8. **Q:** How many intersections should the system manage?  
   **A:** Central system managing multiple intersections (10-100 intersections).

9. **Q:** What about night mode or special schedules?  
   **A:** Yes, different timing patterns for peak hours, off-peak, night (blinking yellow/red).

10. **Q:** Should we log signal changes and violations?  
    **A:** Yes, maintain audit log of all signal transitions and any detected violations.

11. **Q:** What about manual override?  
    **A:** Traffic control center should be able to manually control signals for maintenance or special events.

12. **Q:** Communication with signals?  
    **A:** Assume reliable network connection. Handle temporary disconnections gracefully.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

Listed point-wise as interviewer presents them:

1. System should manage multiple traffic intersections simultaneously
2. Each intersection has signals for multiple directions (N, S, E, W)
3. Each direction has a vehicle signal (Red, Yellow, Green)
4. Each direction should have a pedestrian signal (Walk, Don't Walk)
5. System must ensure safety: no conflicting green lights simultaneously
6. Signals should transition in safe sequence: Green → Yellow → Red → Green
7. Yellow light duration should be fixed (e.g., 3 seconds)
8. Green light duration can vary by direction (configurable per intersection)
9. System should support different timing schedules:
   - Peak hours (longer green for main roads)
   - Off-peak hours (balanced timing)
   - Night mode (blinking signals or shorter cycles)
10. Pedestrian signals should sync with vehicle signals
11. Pedestrian "Walk" only when vehicle signal is red
12. System should handle pedestrian button press requests
13. System must support emergency vehicle priority:
    - Override current cycle
    - Create green corridor for emergency vehicle path
    - Resume normal operation after emergency passes
14. System should detect and prevent signal malfunctions
15. System should maintain audit log of all signal state changes
16. System should support manual override by traffic control operators
17. System should handle intersection controller failures gracefully
18. When connection lost, intersection should operate in safe default mode
19. System should provide real-time status monitoring dashboard
20. System should generate reports: signal uptime, cycle statistics, emergency events

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Safety Analysis** (Most Critical)

**Think:** "What happens if system fails?"
- Conflicting greens → accidents, fatalities
- Signal failure → traffic chaos
- Wrong timing → pedestrian danger

**Deduced NFRs:**
- ✅ **Safety first** - Zero conflicting greens (formal verification)
- ✅ Fail-safe mode (all red or blinking red on critical failure)
- ✅ Minimum yellow time (3 seconds minimum, never skipped)
- ✅ Atomic state transitions (never stuck in intermediate state)
- ✅ Redundancy for critical operations

**Interview Explanation:**
"Safety is paramount. A bug in traffic signals can cause deaths. We need formal verification that no two conflicting directions ever get green simultaneously. Fail-safe: on any error, go to all-red state."

**Critical Constraints:**
```
Safety Rules (MUST be enforced):
1. North-South Green ⊥ East-West Green (mutually exclusive)
2. Yellow duration ≥ 3 seconds (legal minimum)
3. All-red gap between conflicting greens (1-2 seconds clearance)
```

---

#### 2. **Availability Analysis**

**Think:** "What's acceptable downtime?"
- Traffic signals operate 24/7
- Failure causes immediate traffic disruption
- Manual intervention difficult (signals on poles)

**Deduced NFRs:**
- ✅ 99.99% availability (< 52 minutes downtime/year)
- ✅ Autonomous operation during network outage
- ✅ Self-healing (detect failures, switch to backup mode)
- ✅ Hot standby for central controller

**Why 99.99%?** Physical infrastructure, critical public safety system.

**Fault Tolerance:**
```
Network Down → Intersection runs locally (pre-programmed schedule)
Controller Fail → Backup controller takes over
Power Loss → Battery backup (4 hours minimum)
```

---

#### 3. **Performance Analysis**

**Think:** "What operations are time-critical?"
- Signal transitions must be precise (not 3.5 seconds instead of 3)
- State changes must be immediate (no lag)
- Real-time monitoring for thousands of signals

**Deduced NFRs:**
- ✅ Signal change latency < 50ms (imperceptible)
- ✅ Timer accuracy ± 100ms (human imperceptible)
- ✅ State synchronization < 100ms across all signals at intersection
- ✅ Dashboard update < 500ms (acceptable for monitoring)

**Why these numbers?**
- Humans can't perceive < 100ms delays
- Signal precision prevents "yellow flash" (too short yellow)

---

#### 4. **Consistency Analysis**

**Think:** "What must be perfectly consistent?"
- All signals at intersection must agree on state
- State transitions must be atomic (all signals change together)
- Audit log must be complete (for accident investigation)

**Deduced NFRs:**
- ✅ Strong consistency within intersection (all signals synchronized)
- ✅ Eventual consistency across intersections (acceptable delay)
- ✅ Atomic state transitions (commit all or rollback all)
- ✅ Persistent audit log (survive system crash)

**Interview Explanation:**
"At single intersection, all signals must be perfectly synchronized. Can't have North signal showing green while controller thinks it's red. But across different intersections, slight delays are acceptable."

---

#### 5. **Scalability Analysis**

**Think:** "How many intersections?"
- City: 100-1000 intersections
- Each intersection: 4-8 signals
- Total: 1000-8000 signals to manage

**Deduced NFRs:**
- ✅ Support 1000+ intersections per central controller
- ✅ < 1MB memory per intersection (embedded controllers)
- ✅ Hierarchical architecture (central → regional → intersection)
- ✅ Bandwidth < 1KB/sec per intersection (minimal communication)

---

#### 6. **Maintainability Analysis**

**Think:** "How to update timing without downtime?"
- Schedule changes (holiday vs normal)
- New intersection types
- Bug fixes in controller software

**Deduced NFRs:**
- ✅ Hot-reload configuration (no downtime for schedule changes)
- ✅ Over-the-air firmware updates (during low-traffic hours)
- ✅ A/B testing support (try new timing on subset)
- ✅ Rollback capability (revert if issues detected)

---

**NFR Summary Table:**

| Dimension | Requirement | Deduction Method | Impact |
|-----------|-------------|------------------|--------|
| Safety | Zero conflicting greens | Worst-case analysis | Formal verification, all-red failsafe |
| Availability | 99.99% uptime | Critical infrastructure | Redundancy, local autonomy |
| Performance | State change < 50ms | Real-time requirement | Embedded real-time OS |
| Consistency | Atomic transitions | Accident investigation | Distributed consensus |
| Scalability | 1000+ intersections | City-scale | Hierarchical architecture |
| Maintainability | Zero-downtime updates | Operational requirement | Hot reload, versioning |

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Systematic Entity Identification Method

#### Step 1: Noun Extraction from Requirements

| Requirement | Nouns Identified |
|-------------|------------------|
| "Manage multiple traffic intersections" | Intersection, Traffic |
| "Signals for multiple directions (N,S,E,W)" | Signal, Direction |
| "Vehicle signal (Red, Yellow, Green)" | Vehicle Signal, Signal State |
| "Pedestrian signal (Walk, Don't Walk)" | Pedestrian Signal |
| "Safe sequence: Green → Yellow → Red" | Sequence, Transition |
| "Different timing schedules" | Schedule, Timing |
| "Peak hours, off-peak, night mode" | Schedule Type, Mode |
| "Emergency vehicle priority" | Emergency Event, Priority |
| "Green corridor for emergency path" | Corridor, Path |
| "Manual override by operators" | Override, Operator |
| "Maintain audit log" | Audit Log, Log Entry |
| "Real-time status dashboard" | Dashboard, Status |
| "Intersection controller" | Controller |

#### Step 2: Entity Validation

| Noun | Attributes? | Behavior? | Lifecycle? | Relationships? | Entity? |
|------|-------------|-----------|------------|----------------|---------|
| Intersection | ✅ id, location, type | ✅ coordinate signals | ✅ create/delete | ✅ has signals | ✅ YES |
| Signal | ✅ id, type, state, direction | ✅ change state | ✅ create/update | ✅ belongs to intersection | ✅ YES |
| SignalState | ✅ color, meaning | ✅ validate | ✅ enum | ✅ used by signal | ✅ YES (enum) |
| Direction | ✅ name, opposite | ✅ check conflict | ✅ enum | ✅ used by signal | ✅ YES (enum) |
| Schedule | ✅ timings, rules | ✅ get duration | ✅ configure | ✅ used by intersection | ✅ YES |
| ScheduleType | ✅ name | ✅ none | ✅ enum | ✅ type of schedule | ✅ YES (enum) |
| Controller | ✅ id, intersection, state | ✅ run cycle | ✅ start/stop | ✅ controls intersection | ✅ YES |
| EmergencyEvent | ✅ vehicle, path, time | ✅ activate | ✅ create/resolve | ✅ affects intersection | ✅ YES |
| ManualOverride | ✅ operator, reason | ✅ apply | ✅ create/end | ✅ affects intersection | ✅ YES |
| AuditLog | ✅ entries | ✅ record | ✅ persist | ✅ logs changes | ✅ YES |
| Dashboard | ⚠️ UI component | ⚠️ display | ⚠️ runtime | ✅ shows status | ⚠️ MAYBE (service) |

#### Step 3: Refinement and Grouping

**Group 1: Core Domain**
- Intersection (physical intersection)
- Signal (individual traffic light)
- SignalState (Red, Yellow, Green, etc.)
- Direction (North, South, East, West)

**Group 2: Control Logic**
- Controller (state machine for intersection)
- Schedule (timing configuration)
- Transition (state change logic)

**Group 3: Special Modes**
- EmergencyMode (emergency vehicle handling)
- ManualOverrideMode (operator control)
- NightMode (blinking or reduced cycle)

**Group 4: System Management**
- CentralController (manages multiple intersections)
- IntersectionController (manages one intersection)
- AuditLogger (records all changes)

**Group 5: Signal Types**
- VehicleSignal (for vehicles)
- PedestrianSignal (for pedestrians)
- Both inherit from Signal

### Final Entity List (15 Core Entities)

**Core Entities:**
1. **Intersection** - Physical intersection with multiple signals
2. **Signal** (Abstract) + VehicleSignal + PedestrianSignal
3. **SignalState** - Enum (RED, YELLOW, GREEN, WALK, DONT_WALK)
4. **Direction** - Enum (NORTH, SOUTH, EAST, WEST)

**Control Entities:**
5. **IntersectionController** - State machine for one intersection
6. **CentralController** - Manages multiple intersections (Singleton)
7. **SignalSchedule** - Timing configuration
8. **Transition** - Encapsulates state change

**Mode Entities:**
9. **OperatingMode** (Abstract) + NormalMode + EmergencyMode + ManualMode + NightMode
10. **EmergencyEvent** - Emergency vehicle event

**System Entities:**
11. **AuditLogger** - Records state changes
12. **SignalMonitor** - Detects malfunctions
13. **SafetyValidator** - Ensures no conflicting greens

**Why these entities?**
- Intersection: Core domain object
- Signal types: Different behavior (pedestrian has button)
- Controller: Encapsulates state machine logic
- Modes: Strategy pattern for different operations
- Validators: Separate safety logic from business logic

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Three-Pass Methodology

---

### Pass 1: Inheritance Hierarchies (IS-A)

**Analysis:**

**Signal Hierarchy:**
```
VehicleSignal IS-A Signal? → Yes (has Red/Yellow/Green)
PedestrianSignal IS-A Signal? → Yes (has Walk/Don't Walk)
```

**Decision:** Create Signal hierarchy
```
Signal (Abstract)
  ├─ VehicleSignal (Red, Yellow, Green)
  └─ PedestrianSignal (Walk, Don't Walk)
```

**Why abstract class vs interface?**
- Common state: `currentState`, `direction`, `lastChangeTime`
- Common behavior: `setState()`, `getState()`, `validate()`
- Signals share lifecycle management

**OperatingMode Hierarchy:**
```
OperatingMode (Abstract)
  ├─ NormalMode (standard cycles)
  ├─ EmergencyMode (override for green corridor)
  ├─ ManualMode (operator control)
  └─ NightMode (blinking or reduced)
```

**Why?** Each mode has different transition logic (Strategy pattern)

---

### Pass 2: Ownership Relationships (HAS-A)

#### Intersection ↔ Signal

**Q1:** Does Intersection contain Signals? → **Yes**  
**Q2:** Can Signal exist without Intersection? → **No**  
**Q3:** Delete Intersection → Delete Signals? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
Intersection ◆────→ Signal [4..8]
```

**Interview Explanation:**
"Signals are part of intersection infrastructure. They don't exist independently. When intersection is decommissioned, signals are removed."

#### Intersection ↔ IntersectionController

**Q1:** Does Intersection need Controller? → **Yes**  
**Q2:** Can Controller exist without Intersection? → **No** (controls specific intersection)  
**Q3:** Delete Intersection → Delete Controller? → **Yes**

**Conclusion:** **Composition** (bidirectional 1:1)
```
Intersection ◆────→ IntersectionController [1]
```

#### IntersectionController ↔ OperatingMode

**Q1:** Does Controller use Mode? → **Yes**  
**Q2:** Can Mode exist without Controller? → **Yes** (strategy object)  
**Q3:** Delete Controller → Delete Mode? → **No**

**Conclusion:** **Aggregation** (strategy pattern)
```
IntersectionController ◇────→ OperatingMode [1]
```

#### IntersectionController ↔ SignalSchedule

**Q1:** Does Controller use Schedule? → **Yes**  
**Q2:** Can Schedule exist without Controller? → **Yes** (configuration)  
**Q3:** Delete Controller → Delete Schedule? → **No**

**Conclusion:** **Aggregation**
```
IntersectionController ◇────→ SignalSchedule [1..*]
```

#### CentralController ↔ IntersectionController

**Q1:** Does CentralController manage IntersectionControllers? → **Yes**  
**Q2:** Can IntersectionController exist without CentralController? → **Yes** (autonomous mode)  
**Q3:** Delete CentralController → Delete IntersectionControllers? → **No**

**Conclusion:** **Aggregation** (weak management)
```
CentralController ◇────→ IntersectionController [1..*]
```

---

### Pass 3: Cardinality Mapping

| Relationship | From | To | Cardinality | Reasoning |
|--------------|------|----|-------------|-----------|
| Intersection → Signal | 1 | 4..8 | 1:N | 4-way has 4 vehicle + 4 pedestrian = 8 |
| Intersection → IntersectionController | 1 | 1 | 1:1 | One controller per intersection |
| IntersectionController → OperatingMode | 1 | 1 | 1:1 | One active mode at a time |
| IntersectionController → SignalSchedule | 1 | 1..N | 1:N | Multiple schedules (peak, off-peak) |
| CentralController → IntersectionController | 1 | N | 1:N | Central manages many intersections |
| Signal → SignalState | 1 | 1 | 1:1 | Signal has one current state |
| Signal → Direction | 1 | 1 | 1:1 | Signal controls one direction |
| EmergencyEvent → Intersection | 1 | N | 1:N | Emergency affects multiple intersections |

---

### Special Design Decisions

#### Decision 1: Signal State Machine

**Problem:** How to ensure safe transitions?

**Implementation:**
```
State Machine per Signal:
  RED → GREEN (with all-red gap)
  GREEN → YELLOW (always)
  YELLOW → RED (always)
  
Invalid transitions (throw exception):
  RED → YELLOW (skip green)
  YELLOW → GREEN (skip red)
  GREEN → RED (skip yellow)
```

**Interview Explanation:**
"Each signal is a state machine. We enforce valid transitions at Signal level. Yellow must always appear between green and red (safety requirement). Invalid transitions throw exception and trigger fail-safe mode."

#### Decision 2: Conflict Detection

**Problem:** Prevent North-South and East-West both green

**Implementation:**
```java
public class SafetyValidator {
    private static final Map<Direction, Set<Direction>> CONFLICTS = Map.of(
        Direction.NORTH, Set.of(Direction.EAST, Direction.WEST),
        Direction.SOUTH, Set.of(Direction.EAST, Direction.WEST),
        Direction.EAST, Set.of(Direction.NORTH, Direction.SOUTH),
        Direction.WEST, Set.of(Direction.NORTH, Direction.SOUTH)
    );
    
    public boolean isSafe(Map<Direction, SignalState> states) {
        Set<Direction> greens = findGreenDirections(states);
        
        for (Direction d1 : greens) {
            for (Direction d2 : greens) {
                if (CONFLICTS.get(d1).contains(d2)) {
                    return false; // Conflict detected!
                }
            }
        }
        return true;
    }
}
```

#### Decision 3: Emergency Override Strategy

**Problem:** How to create green corridor for emergency vehicle?

**Strategy:**
```
1. Emergency vehicle path: North → South (going straight)
2. Override cycle:
   - North signal: Force GREEN
   - South signal: Force GREEN (same direction)
   - East/West signals: Force RED
3. Hold until emergency passes (30 seconds timeout)
4. Resume normal cycle
```

---

### Complete Relationship Diagram

```
┌──────────────────────┐
│  CentralController   │
│     (Singleton)      │
└──────────┬───────────┘
           │
           │ [1:N Aggregation]
           │
           ▼
┌─────────────────────────┐
│ IntersectionController  │
└────────┬────────────────┘
         │
         ├─── [1:1 Composition] ──→ Intersection
         │                              │
         │                              └─── [1:N Composition] ──→ Signal
         │                                                              ├─ VehicleSignal
         │                                                              └─ PedestrianSignal
         │
         ├─── [1:1 Aggregation] ──→ OperatingMode
         │                              ├─ NormalMode
         │                              ├─ EmergencyMode
         │                              ├─ ManualMode
         │                              └─ NightMode
         │
         ├─── [1:N Aggregation] ──→ SignalSchedule
         │
         └─── [1:1 Association] ──→ SafetyValidator


EmergencyEvent ──→ [affects N] ──→ IntersectionController
AuditLogger ──→ [logs all] ──→ State Transitions
```

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌────────────────────────┐      ┌────────────────────────┐
│   <<enumeration>>      │      │   <<enumeration>>      │
│     SignalState        │      │      Direction         │
├────────────────────────┤      ├────────────────────────┤
│ RED                    │      │ NORTH                  │
│ YELLOW                 │      │ SOUTH                  │
│ GREEN                  │      │ EAST                   │
│ WALK                   │      │ WEST                   │
│ DONT_WALK              │      ├────────────────────────┤
│ BLINKING_RED           │      │ + getOpposite(): Dir   │
│ BLINKING_YELLOW        │      │ + getConflicting():    │
├────────────────────────┤      │   Set<Direction>       │
│ + isVehicleState()     │      └────────────────────────┘
│ + isPedestrianState()  │
│ + canTransitionTo(s)   │
│   : boolean            │
└────────────────────────┘
```

**Key Methods:**
- `canTransitionTo()`: Validates state machine transitions
- `getConflicting()`: Returns directions that conflict with this

---

### Class Diagram 2: Signal Hierarchy

```
┌──────────────────────────────────────────────┐
│           <<abstract>>                       │
│              Signal                          │
├──────────────────────────────────────────────┤
│ # id: String                                 │
│ # direction: Direction                       │
│ # currentState: SignalState                  │
│ # lastChangeTime: LocalDateTime              │
├──────────────────────────────────────────────┤
│ + Signal(id, direction)                      │
│ + setState(state: SignalState): void         │
│ + getState(): SignalState                    │
│ + getDirection(): Direction                  │
│ + getTimeSinceChange(): Duration             │
│ # validateTransition(newState): boolean      │
│ + canChangeTo(state): boolean [abstract]     │
└──────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┴──────────┐
        │                      │
┌───────┴──────────┐   ┌───────┴───────────┐
│  VehicleSignal   │   │ PedestrianSignal  │
├──────────────────┤   ├───────────────────┤
│                  │   │ - buttonPressed   │
│                  │   │ - walkDuration    │
├──────────────────┤   ├───────────────────┤
│+canChangeTo(s)   │   │+canChangeTo(s)    │
│  : boolean       │   │  : boolean        │
│+toString()       │   │+pressButton()     │
└──────────────────┘   │+toString()        │
                       └───────────────────┘
```

**Design Rationale:**
- Abstract `canChangeTo()`: Different validation for vehicle vs pedestrian
- VehicleSignal: Must go GREEN → YELLOW → RED
- PedestrianSignal: Simpler (WALK → DONT_WALK)

---

### Class Diagram 3: Intersection

```
┌──────────────────────────────────────────────────────┐
│              Intersection                            │
├──────────────────────────────────────────────────────┤
│ - id: String                                         │
│ - location: String                                   │
│ - type: IntersectionType                             │
│ - signals: Map<Direction, Signal>  ◆─────────────────┼──→ Signal [4..8]
│ - createdAt: LocalDateTime                           │
├──────────────────────────────────────────────────────┤
│ + Intersection(id, location, type)                   │
│ + addSignal(direction: Direction, signal: Signal)    │
│ + getSignal(direction: Direction): Signal            │
│ + getAllSignals(): List<Signal>                      │
│ + getVehicleSignals(): List<VehicleSignal>           │
│ + getPedestrianSignals(): List<PedestrianSignal>     │
│ + getId(): String                                    │
│ + getType(): IntersectionType                        │
└──────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: SignalSchedule

```
┌──────────────────────────────────────────────────────┐
│            SignalSchedule                            │
├──────────────────────────────────────────────────────┤
│ - name: String                                       │
│ - type: ScheduleType                                 │
│ - greenDurations: Map<Direction, Integer>            │
│ - yellowDuration: int                                │
│ - allRedDuration: int                                │
│ - activeHours: TimeRange                             │
├──────────────────────────────────────────────────────┤
│ + SignalSchedule(name, type)                         │
│ + setGreenDuration(dir: Direction, seconds: int)     │
│ + getGreenDuration(dir: Direction): int              │
│ + getYellowDuration(): int                           │
│ + getAllRedDuration(): int                           │
│ + isActive(time: LocalDateTime): boolean             │
└──────────────────────────────────────────────────────┘

┌────────────────────────┐
│   <<enumeration>>      │
│    ScheduleType        │
├────────────────────────┤
│ PEAK_HOURS             │
│ OFF_PEAK               │
│ NIGHT                  │
│ WEEKEND                │
│ HOLIDAY                │
└────────────────────────┘
```

**Example Configuration:**
```
Peak Hours (7-9 AM, 5-7 PM):
  - North/South (main road): 60 seconds green
  - East/West (side road): 30 seconds green
  
Off-Peak:
  - North/South: 40 seconds green
  - East/West: 40 seconds green
```

---

### Class Diagram 5: OperatingMode (Strategy Pattern)

```
┌────────────────────────────────────────────────┐
│           <<abstract>>                         │
│          OperatingMode                         │
├────────────────────────────────────────────────┤
│ # name: String                                 │
├────────────────────────────────────────────────┤
│ + OperatingMode(name)                          │
│ + getNextState(current: Direction, signals):   │
│   Transition [abstract]                        │
│ + onEnter(controller): void                    │
│ + onExit(controller): void                     │
│ + getName(): String                            │
└────────────────────────────────────────────────┘
                    △
                    │
    ┌───────────────┼────────────┬─────────────┐
    │               │            │             │
┌───┴──────┐  ┌────┴─────┐  ┌──┴────────┐  ┌─┴──────────┐
│  Normal  │  │Emergency │  │  Manual   │  │   Night    │
│   Mode   │  │   Mode   │  │   Mode    │  │   Mode     │
├──────────┤  ├──────────┤  ├───────────┤  ├────────────┤
│-schedule │  │-corridor │  │-operator  │  │-blinkRate  │
│-current  │  │-priority │  │-commands  │  │            │
├──────────┤  ├──────────┤  ├───────────┤  ├────────────┤
│+getNext()│  │+getNext()│  │+getNext() │  │+getNext()  │
│          │  │+override()│  │+execute() │  │+blink()    │
└──────────┘  └──────────┘  └───────────┘  └────────────┘
```

**Strategy Pattern Application:**
- Each mode implements different transition logic
- Controller switches modes dynamically
- Easy to add new modes (e.g., FloodMode, ConstructionMode)

---

### Class Diagram 6: IntersectionController (State Machine)

```
┌──────────────────────────────────────────────────────────┐
│         IntersectionController                           │
├──────────────────────────────────────────────────────────┤
│ - id: String                                             │
│ - intersection: Intersection         ◆───────────────────┼──→ Intersection [1]
│ - currentMode: OperatingMode         ◇───────────────────┼──→ OperatingMode [1]
│ - activeSchedule: SignalSchedule     ◇───────────────────┼──→ SignalSchedule [1]
│ - allSchedules: List<SignalSchedule>                     │
│ - currentPhase: Direction                                │
│ - phaseStartTime: LocalDateTime                          │
│ - running: boolean                                       │
│ - validator: SafetyValidator                             │
│ - logger: AuditLogger                                    │
├──────────────────────────────────────────────────────────┤
│ + IntersectionController(id, intersection)               │
│ + start(): void                                          │
│ + stop(): void                                           │
│ + runCycle(): void                                       │
│ + transitionToNextPhase(): void                          │
│ + setMode(mode: OperatingMode): void                     │
│ + setSchedule(schedule: SignalSchedule): void            │
│ + handleEmergency(event: EmergencyEvent): void           │
│ + applyManualOverride(override: ManualOverride): void    │
│ + getCurrentPhase(): Direction                           │
│ + getStatus(): ControllerStatus                          │
│ - executeTransition(transition: Transition): void        │
│ - validateSafety(): boolean                              │
│ - enterFailSafeMode(): void                              │
└──────────────────────────────────────────────────────────┘
```

**Key Methods:**
- `runCycle()`: Main control loop (runs continuously)
- `transitionToNextPhase()`: Move to next direction
- `validateSafety()`: Check for conflicting greens before transition
- `enterFailSafeMode()`: All red on error

---

### Class Diagram 7: Transition

```
┌────────────────────────────────────────────────┐
│            Transition                          │
├────────────────────────────────────────────────┤
│ - direction: Direction                         │
│ - targetState: SignalState                     │
│ - duration: int                                │
│ - allOthersRed: boolean                        │
├────────────────────────────────────────────────┤
│ + Transition(dir, state, duration)             │
│ + getDirection(): Direction                    │
│ + getTargetState(): SignalState                │
│ + getDuration(): int                           │
│ + shouldOthersBeRed(): boolean                 │
└────────────────────────────────────────────────┘
```

**Purpose:** Encapsulates a state change instruction

**Example:**
```java
Transition northToGreen = new Transition(
    Direction.NORTH, 
    SignalState.GREEN, 
    60,  // 60 seconds
    true // All other directions must be red
);
```

---

### Class Diagram 8: CentralController & SafetyValidator

```
┌──────────────────────────────────────────────────────┐
│          <<Singleton>>                               │
│         CentralController                            │
├──────────────────────────────────────────────────────┤
│ - instance: CentralController [static]               │
│ - controllers: Map<String, IntersectionController>   │
│                               ◇──────────────────────┼──→ IntersectionController [1..*]
│ - auditLogger: AuditLogger                           │
│ - monitor: SystemMonitor                             │
├──────────────────────────────────────────────────────┤
│ - CentralController() [private]                      │
│ + getInstance(): CentralController [static, sync]    │
│ + registerIntersection(controller): void             │
│ + getController(id: String): IntersectionController  │
│ + getAllControllers(): List<IntersectionController>  │
│ + broadcastEmergency(event: EmergencyEvent): void    │
│ + getSystemStatus(): SystemStatus                    │
│ + shutdown(): void                                   │
└──────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────┐
│         SafetyValidator                        │
├────────────────────────────────────────────────┤
│ - CONFLICTS: Map<Direction, Set<Direction>>    │
├────────────────────────────────────────────────┤
│ + SafetyValidator()                            │
│ + validateTransition(intersection, transition) │
│   : boolean                                    │
│ + hasConflictingGreens(signals): boolean       │
│ + getConflictingDirections(dir): Set<Dir>      │
└────────────────────────────────────────────────┘
```

---

### Complete System Architecture

```
          ┌──────────────────────┐
          │  CentralController   │
          │     (Singleton)      │
          └──────────┬───────────┘
                     │
         ┌───────────┴──────────┬─────────────┐
         │                      │             │
         ▼                      ▼             ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│Intersection    │   │Intersection    │   │Intersection    │
│Controller 1    │   │Controller 2    │   │Controller 3    │
└───────┬────────┘   └───────┬────────┘   └───────┬────────┘
        │                    │                    │
        │[controls]          │[controls]          │[controls]
        ▼                    ▼                    ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ Intersection  │   │ Intersection  │   │ Intersection  │
│   (4-way)     │   │   (4-way)     │   │ (T-junction)  │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        │[has 8]            │[has 8]            │[has 6]
        ▼                   ▼                   ▼
    [Signals]           [Signals]           [Signals]
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Enums

```java
// SignalState.java
public enum SignalState {
    // Vehicle states
    RED("Red", true),
    YELLOW("Yellow", true),
    GREEN("Green", true),
    BLINKING_RED("Blinking Red", true),
    BLINKING_YELLOW("Blinking Yellow", true),
    
    // Pedestrian states
    WALK("Walk", false),
    DONT_WALK("Don't Walk", false);
    
    private final String displayName;
    private final boolean isVehicleState;
    
    SignalState(String displayName, boolean isVehicleState) {
        this.displayName = displayName;
        this.isVehicleState = isVehicleState;
    }
    
    public boolean isVehicleState() {
        return isVehicleState;
    }
    
    public boolean isPedestrianState() {
        return !isVehicleState;
    }
    
    public boolean canTransitionTo(SignalState newState) {
        // Vehicle transitions
        if (this == RED && newState == GREEN) return true;
        if (this == GREEN && newState == YELLOW) return true;
        if (this == YELLOW && newState == RED) return true;
        
        // Pedestrian transitions
        if (this == DONT_WALK && newState == WALK) return true;
        if (this == WALK && newState == DONT_WALK) return true;
        
        // Blinking transitions (night mode)
        if (this == RED && newState == BLINKING_RED) return true;
        if (this == BLINKING_RED && newState == RED) return true;
        
        return false;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

// Direction.java
import java.util.*;

public enum Direction {
    NORTH("North"),
    SOUTH("South"),
    EAST("East"),
    WEST("West");
    
    private final String displayName;
    
    // Static initialization of conflicts
    private static final Map<Direction, Set<Direction>> CONFLICTS;
    static {
        CONFLICTS = new EnumMap<>(Direction.class);
        CONFLICTS.put(NORTH, Set.of(EAST, WEST));
        CONFLICTS.put(SOUTH, Set.of(EAST, WEST));
        CONFLICTS.put(EAST, Set.of(NORTH, SOUTH));
        CONFLICTS.put(WEST, Set.of(NORTH, SOUTH));
    }
    
    Direction(String displayName) {
        this.displayName = displayName;
    }
    
    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
    
    public Set<Direction> getConflictingDirections() {
        return CONFLICTS.get(this);
    }
    
    public boolean conflictsWith(Direction other) {
        return CONFLICTS.get(this).contains(other);
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

// IntersectionType.java
public enum IntersectionType {
    FOUR_WAY("4-Way Intersection"),
    T_JUNCTION("T-Junction"),
    Y_JUNCTION("Y-Junction");
    
    private final String displayName;
    
    IntersectionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

---

### Signal Hierarchy

```java
// Signal.java
import java.time.LocalDateTime;
import java.time.Duration;

public abstract class Signal {
    protected final String id;
    protected final Direction direction;
    protected SignalState currentState;
    protected LocalDateTime lastChangeTime;
    
    public Signal(String id, Direction direction, SignalState initialState) {
        this.id = id;
        this.direction = direction;
        this.currentState = initialState;
        this.lastChangeTime = LocalDateTime.now();
    }
    
    public synchronized void setState(SignalState newState) {
        if (!canChangeTo(newState)) {
            throw new IllegalStateException(
                String.format("Invalid transition: %s -> %s for %s signal",
                            currentState, newState, getClass().getSimpleName())
            );
        }
        
        System.out.println(String.format("[%s] %s Signal: %s -> %s",
            LocalDateTime.now(), direction, currentState, newState));
        
        this.currentState = newState;
        this.lastChangeTime = LocalDateTime.now();
    }
    
    public SignalState getState() {
        return currentState;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public String getId() {
        return id;
    }
    
    public Duration getTimeSinceChange() {
        return Duration.between(lastChangeTime, LocalDateTime.now());
    }
    
    public abstract boolean canChangeTo(SignalState newState);
    
    @Override
    public String toString() {
        return String.format("%s-%s: %s", 
            direction.getDisplayName(), 
            getClass().getSimpleName(), 
            currentState.getDisplayName());
    }
}

// VehicleSignal.java
public class VehicleSignal extends Signal {
    
    public VehicleSignal(String id, Direction direction) {
        super(id, direction, SignalState.RED);
    }
    
    @Override
    public boolean canChangeTo(SignalState newState) {
        if (!newState.isVehicleState()) {
            return false; // Can't transition to pedestrian state
        }
        
        return currentState.canTransitionTo(newState);
    }
}

// PedestrianSignal.java
public class PedestrianSignal extends Signal {
    private boolean buttonPressed;
    private int walkDuration;
    
    public PedestrianSignal(String id, Direction direction) {
        super(id, direction, SignalState.DONT_WALK);
        this.buttonPressed = false;
        this.walkDuration = 15; // Default 15 seconds
    }
    
    public void pressButton() {
        this.buttonPressed = true;
        System.out.println(String.format("[%s] Pedestrian button pressed",
            direction.getDisplayName()));
    }
    
    public boolean isButtonPressed() {
        return buttonPressed;
    }
    
    public void clearButton() {
        this.buttonPressed = false;
    }
    
    public int getWalkDuration() {
        return walkDuration;
    }
    
    public void setWalkDuration(int seconds) {
        this.walkDuration = seconds;
    }
    
    @Override
    public boolean canChangeTo(SignalState newState) {
        if (!newState.isPedestrianState()) {
            return false; // Can't transition to vehicle state
        }
        
        return currentState.canTransitionTo(newState);
    }
}
```

---

### Intersection

```java
// Intersection.java
import java.util.*;
import java.time.LocalDateTime;

public class Intersection {
    private final String id;
    private final String location;
    private final IntersectionType type;
    private final Map<Direction, VehicleSignal> vehicleSignals;
    private final Map<Direction, PedestrianSignal> pedestrianSignals;
    private final LocalDateTime createdAt;
    
    public Intersection(String id, String location, IntersectionType type) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.vehicleSignals = new EnumMap<>(Direction.class);
        this.pedestrianSignals = new EnumMap<>(Direction.class);
        this.createdAt = LocalDateTime.now();
        
        // Initialize signals for all directions
        initializeSignals();
    }
    
    private void initializeSignals() {
        List<Direction> directions = getDirectionsForType();
        
        for (Direction dir : directions) {
            String vehicleId = id + "-V-" + dir.name();
            String pedId = id + "-P-" + dir.name();
            
            vehicleSignals.put(dir, new VehicleSignal(vehicleId, dir));
            pedestrianSignals.put(dir, new PedestrianSignal(pedId, dir));
        }
    }
    
    private List<Direction> getDirectionsForType() {
        return switch (type) {
            case FOUR_WAY -> List.of(Direction.NORTH, Direction.SOUTH, 
                                    Direction.EAST, Direction.WEST);
            case T_JUNCTION -> List.of(Direction.NORTH, Direction.EAST, 
                                      Direction.WEST);
            case Y_JUNCTION -> List.of(Direction.NORTH, Direction.SOUTH, 
                                      Direction.EAST);
        };
    }
    
    public VehicleSignal getVehicleSignal(Direction direction) {
        return vehicleSignals.get(direction);
    }
    
    public PedestrianSignal getPedestrianSignal(Direction direction) {
        return pedestrianSignals.get(direction);
    }
    
    public List<VehicleSignal> getAllVehicleSignals() {
        return new ArrayList<>(vehicleSignals.values());
    }
    
    public List<PedestrianSignal> getAllPedestrianSignals() {
        return new ArrayList<>(pedestrianSignals.values());
    }
    
    public Set<Direction> getActiveDirections() {
        return vehicleSignals.keySet();
    }
    
    // Getters
    public String getId() { return id; }
    public String getLocation() { return location; }
    public IntersectionType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

---

### SignalSchedule

```java
// SignalSchedule.java
import java.util.*;
import java.time.LocalTime;

public class SignalSchedule {
    private final String name;
    private final ScheduleType type;
    private final Map<Direction, Integer> greenDurations;
    private final int yellowDuration;
    private final int allRedDuration;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public SignalSchedule(String name, ScheduleType type) {
        this.name = name;
        this.type = type;
        this.greenDurations = new EnumMap<>(Direction.class);
        this.yellowDuration = 3; // Fixed 3 seconds (safety requirement)
        this.allRedDuration = 2; // 2 second clearance between greens
    }
    
    public void setGreenDuration(Direction direction, int seconds) {
        if (seconds < 10) {
            throw new IllegalArgumentException(
                "Green duration must be at least 10 seconds");
        }
        greenDurations.put(direction, seconds);
    }
    
    public int getGreenDuration(Direction direction) {
        return greenDurations.getOrDefault(direction, 30); // Default 30s
    }
    
    public int getYellowDuration() {
        return yellowDuration;
    }
    
    public int getAllRedDuration() {
        return allRedDuration;
    }
    
    public void setActiveHours(LocalTime start, LocalTime end) {
        this.startTime = start;
        this.endTime = end;
    }
    
    public boolean isActive(LocalTime currentTime) {
        if (startTime == null || endTime == null) {
            return true; // Always active if no time restriction
        }
        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
    }
    
    // Getters
    public String getName() { return name; }
    public ScheduleType getType() { return type; }
}

// ScheduleType.java
public enum ScheduleType {
    PEAK_HOURS("Peak Hours"),
    OFF_PEAK("Off-Peak"),
    NIGHT("Night Mode"),
    WEEKEND("Weekend"),
    HOLIDAY("Holiday");
    
    private final String displayName;
    
    ScheduleType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

---

### Transition

```java
// Transition.java
public class Transition {
    private final Direction direction;
    private final SignalState targetState;
    private final int durationSeconds;
    private final boolean allOthersRed;
    
    public Transition(Direction direction, SignalState targetState, 
                     int durationSeconds, boolean allOthersRed) {
        this.direction = direction;
        this.targetState = targetState;
        this.durationSeconds = durationSeconds;
        this.allOthersRed = allOthersRed;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public SignalState getTargetState() {
        return targetState;
    }
    
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    public boolean shouldAllOthersBeRed() {
        return allOthersRed;
    }
    
    @Override
    public String toString() {
        return String.format("Transition[%s -> %s for %ds]",
            direction, targetState, durationSeconds);
    }
}
```

---

### SafetyValidator

```java
// SafetyValidator.java
import java.util.*;

public class SafetyValidator {
    
    public boolean validateTransition(Intersection intersection, 
                                     Transition transition) {
        // Simulate the transition
        Map<Direction, SignalState> futureStates = getCurrentStates(intersection);
        futureStates.put(transition.getDirection(), transition.getTargetState());
        
        // Check for conflicts
        return !hasConflictingGreens(futureStates);
    }
    
    public boolean hasConflictingGreens(Map<Direction, SignalState> states) {
        Set<Direction> greenDirections = new HashSet<>();
        
        // Find all directions with green light
        for (Map.Entry<Direction, SignalState> entry : states.entrySet()) {
            if (entry.getValue() == SignalState.GREEN) {
                greenDirections.add(entry.getKey());
            }
        }
        
        // Check if any two green directions conflict
        for (Direction d1 : greenDirections) {
            for (Direction d2 : greenDirections) {
                if (!d1.equals(d2) && d1.conflictsWith(d2)) {
                    System.err.println(String.format(
                        "⚠️  SAFETY VIOLATION: Conflicting greens detected: %s and %s",
                        d1, d2));
                    return true; // Conflict found!
                }
            }
        }
        
        return false; // Safe
    }
    
    private Map<Direction, SignalState> getCurrentStates(Intersection intersection) {
        Map<Direction, SignalState> states = new EnumMap<>(Direction.class);
        
        for (VehicleSignal signal : intersection.getAllVehicleSignals()) {
            states.put(signal.getDirection(), signal.getState());
        }
        
        return states;
    }
    
    public Set<Direction> getConflictingDirections(Direction direction) {
        return direction.getConflictingDirections();
    }
}
```

---

### OperatingMode Hierarchy

```java
// OperatingMode.java
public abstract class OperatingMode {
    protected final String name;
    
    public OperatingMode(String name) {
        this.name = name;
    }
    
    public abstract Transition getNextTransition(
        IntersectionController controller,
        Direction currentPhase
    );
    
    public void onEnter(IntersectionController controller) {
        System.out.println("Entering mode: " + name);
    }
    
    public void onExit(IntersectionController controller) {
        System.out.println("Exiting mode: " + name);
    }
    
    public String getName() {
        return name;
    }
}

// NormalMode.java
public class NormalMode extends OperatingMode {
    private Direction currentPhase;
    private int phaseStep; // 0=green, 1=yellow, 2=red
    
    public NormalMode() {
        super("Normal Mode");
        this.currentPhase = Direction.NORTH;
        this.phaseStep = 0;
    }
    
    @Override
    public Transition getNextTransition(IntersectionController controller,
                                       Direction current) {
        SignalSchedule schedule = controller.getActiveSchedule();
        
        // Phase cycle: NORTH (green→yellow→red) → EAST (green→yellow→red) → 
        //              SOUTH (green→yellow→red) → WEST (green→yellow→red)
        
        if (phaseStep == 0) {
            // Green phase
            int duration = schedule.getGreenDuration(currentPhase);
            phaseStep = 1;
            return new Transition(currentPhase, SignalState.GREEN, duration, true);
            
        } else if (phaseStep == 1) {
            // Yellow phase
            int duration = schedule.getYellowDuration();
            phaseStep = 2;
            return new Transition(currentPhase, SignalState.YELLOW, duration, true);
            
        } else {
            // Red phase + move to next direction
            int duration = schedule.getAllRedDuration();
            phaseStep = 0;
            currentPhase = getNextDirection(currentPhase);
            return new Transition(current, SignalState.RED, duration, false);
        }
    }
    
    private Direction getNextDirection(Direction current) {
        return switch (current) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
        };
    }
}

// EmergencyMode.java
public class EmergencyMode extends OperatingMode {
    private final Direction emergencyPath;
    private final int corridorDuration;
    
    public EmergencyMode(Direction emergencyPath) {
        super("Emergency Mode");
        this.emergencyPath = emergencyPath;
        this.corridorDuration = 30; // Hold for 30 seconds
    }
    
    @Override
    public Transition getNextTransition(IntersectionController controller,
                                       Direction current) {
        // Force green for emergency path
        return new Transition(emergencyPath, SignalState.GREEN, 
                            corridorDuration, true);
    }
    
    @Override
    public void onEnter(IntersectionController controller) {
        super.onEnter(controller);
        System.out.println("🚨 EMERGENCY: Creating green corridor for " + 
                         emergencyPath);
    }
}

// NightMode.java
public class NightMode extends OperatingMode {
    private boolean isBlinking;
    
    public NightMode() {
        super("Night Mode");
        this.isBlinking = false;
    }
    
    @Override
    public Transition getNextTransition(IntersectionController controller,
                                       Direction current) {
        // Alternate between blinking red (main) and blinking yellow (side)
        Direction mainRoad = Direction.NORTH; // Assume North-South is main
        
        if (current == mainRoad || current == mainRoad.getOpposite()) {
            return new Transition(current, SignalState.BLINKING_RED, 1, false);
        } else {
            return new Transition(current, SignalState.BLINKING_YELLOW, 1, false);
        }
    }
}
```

---

### IntersectionController (State Machine - Core Logic)

```java
// IntersectionController.java
import java.util.concurrent.*;
import java.time.LocalDateTime;

public class IntersectionController {
    private final String id;
    private final Intersection intersection;
    private OperatingMode currentMode;
    private SignalSchedule activeSchedule;
    private Direction currentPhase;
    private LocalDateTime phaseStartTime;
    private boolean running;
    private final SafetyValidator validator;
    private final ScheduledExecutorService executor;
    
    public IntersectionController(String id, Intersection intersection) {
        this.id = id;
        this.intersection = intersection;
        this.currentMode = new NormalMode();
        this.currentPhase = Direction.NORTH;
        this.phaseStartTime = LocalDateTime.now();
        this.running = false;
        this.validator = new SafetyValidator();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        
        // Default schedule
        this.activeSchedule = createDefaultSchedule();
    }
    
    private SignalSchedule createDefaultSchedule() {
        SignalSchedule schedule = new SignalSchedule("Default", ScheduleType.OFF_PEAK);
        for (Direction dir : Direction.values()) {
            schedule.setGreenDuration(dir, 30); // 30 seconds green
        }
        return schedule;
    }
    
    public synchronized void start() {
        if (running) {
            System.out.println("Controller already running");
            return;
        }
        
        running = true;
        System.out.println("\n🚦 Starting Intersection Controller: " + 
                         intersection.getId());
        
        // Start control loop
        executor.scheduleAtFixedRate(
            this::controlLoop,
            0,
            1,
            TimeUnit.SECONDS
        );
    }
    
    public synchronized void stop() {
        running = false;
        executor.shutdown();
        System.out.println("🛑 Stopped Intersection Controller: " + 
                         intersection.getId());
    }
    
    private void controlLoop() {
        try {
            // Get next transition from current mode
            Transition transition = currentMode.getNextTransition(this, currentPhase);
            
            // Validate safety
            if (!validator.validateTransition(intersection, transition)) {
                System.err.println("⚠️  Safety violation detected! Entering fail-safe mode.");
                enterFailSafeMode();
                return;
            }
            
            // Execute transition
            executeTransition(transition);
            
            // Wait for phase duration
            Thread.sleep(transition.getDurationSeconds() * 1000);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ Error in control loop: " + e.getMessage());
            enterFailSafeMode();
        }
    }
    
    private synchronized void executeTransition(Transition transition) {
        Direction dir = transition.getDirection();
        SignalState state = transition.getTargetState();
        
        // Get signals
        VehicleSignal vehicleSignal = intersection.getVehicleSignal(dir);
        PedestrianSignal pedSignal = intersection.getPedestrianSignal(dir);
        
        if (vehicleSignal == null) {
            System.err.println("No signal for direction: " + dir);
            return;
        }
        
        // Set vehicle signal
        vehicleSignal.setState(state);
        
        // Set pedestrian signal (inverse of vehicle)
        if (state == SignalState.RED) {
            pedSignal.setState(SignalState.WALK);
        } else if (state == SignalState.GREEN) {
            pedSignal.setState(SignalState.DONT_WALK);
        }
        
        // If others must be red, set them
        if (transition.shouldAllOthersBeRed()) {
            setOthersToRed(dir);
        }
        
        currentPhase = dir;
        phaseStartTime = LocalDateTime.now();
    }
    
    private void setOthersToRed(Direction except) {
        for (VehicleSignal signal : intersection.getAllVehicleSignals()) {
            if (!signal.getDirection().equals(except)) {
                if (signal.getState() != SignalState.RED) {
                    signal.setState(SignalState.RED);
                }
            }
        }
        
        for (PedestrianSignal signal : intersection.getAllPedestrianSignals()) {
            if (!signal.getDirection().equals(except)) {
                if (signal.getState() != SignalState.DONT_WALK) {
                    signal.setState(SignalState.DONT_WALK);
                }
            }
        }
    }
    
    private void enterFailSafeMode() {
        System.err.println("🚨 ENTERING FAIL-SAFE MODE: ALL RED");
        
        // Set all signals to RED
        for (VehicleSignal signal : intersection.getAllVehicleSignals()) {
            try {
                signal.setState(SignalState.RED);
            } catch (Exception e) {
                System.err.println("Failed to set signal to red: " + e.getMessage());
            }
        }
        
        for (PedestrianSignal signal : intersection.getAllPedestrianSignals()) {
            try {
                signal.setState(SignalState.DONT_WALK);
            } catch (Exception e) {
                System.err.println("Failed to set pedestrian signal: " + e.getMessage());
            }
        }
        
        stop();
    }
    
    public synchronized void setMode(OperatingMode newMode) {
        currentMode.onExit(this);
        this.currentMode = newMode;
        currentMode.onEnter(this);
    }
    
    public synchronized void handleEmergency(Direction emergencyPath) {
        System.out.println("🚨 Emergency vehicle detected on " + emergencyPath);
        setMode(new EmergencyMode(emergencyPath));
    }
    
    public void setSchedule(SignalSchedule schedule) {
        this.activeSchedule = schedule;
        System.out.println("Schedule changed to: " + schedule.getName());
    }
    
    // Getters
    public String getId() { return id; }
    public Intersection getIntersection() { return intersection; }
    public SignalSchedule getActiveSchedule() { return activeSchedule; }
    public Direction getCurrentPhase() { return currentPhase; }
    public boolean isRunning() { return running; }
}
```

---

### CentralController

```java
// CentralController.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CentralController {
    private static CentralController instance;
    private static final Object lock = new Object();
    
    private final Map<String, IntersectionController> controllers;
    
    private CentralController() {
        this.controllers = new ConcurrentHashMap<>();
    }
    
    public static CentralController getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new CentralController();
                }
            }
        }
        return instance;
    }
    
    public void registerIntersection(IntersectionController controller) {
        controllers.put(controller.getId(), controller);
        System.out.println("✅ Registered intersection: " + controller.getId());
    }
    
    public IntersectionController getController(String id) {
        return controllers.get(id);
    }
    
    public List<IntersectionController> getAllControllers() {
        return new ArrayList<>(controllers.values());
    }
    
    public void broadcastEmergency(Direction emergencyPath, 
                                   List<String> affectedIntersectionIds) {
        System.out.println("\n🚨 EMERGENCY BROADCAST: Path " + emergencyPath);
        
        for (String id : affectedIntersectionIds) {
            IntersectionController controller = controllers.get(id);
            if (controller != null) {
                controller.handleEmergency(emergencyPath);
            }
        }
    }
    
    public void startAll() {
        System.out.println("\n🚦 Starting all intersections...");
        for (IntersectionController controller : controllers.values()) {
            controller.start();
        }
    }
    
    public void stopAll() {
        System.out.println("\n🛑 Stopping all intersections...");
        for (IntersectionController controller : controllers.values()) {
            controller.stop();
        }
    }
    
    public void showSystemStatus() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    TRAFFIC SIGNAL SYSTEM STATUS       ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Total Intersections: " + controllers.size());
        
        int running = 0;
        for (IntersectionController c : controllers.values()) {
            if (c.isRunning()) running++;
        }
        
        System.out.println("║ Running: " + running + " / " + controllers.size());
        System.out.println("╚════════════════════════════════════════╝\n");
        
        for (IntersectionController controller : controllers.values()) {
            showIntersectionStatus(controller);
        }
    }
    
    private void showIntersectionStatus(IntersectionController controller) {
        Intersection intersection = controller.getIntersection();
        
        System.out.println("📍 " + intersection.getId() + 
                         " (" + intersection.getLocation() + ")");
        System.out.println("   Current Phase: " + controller.getCurrentPhase());
        System.out.println("   Signals:");
        
        for (VehicleSignal signal : intersection.getAllVehicleSignals()) {
            System.out.println("      " + signal.toString());
        }
        
        System.out.println();
    }
}
```

---

### Demo Application

```java
// TrafficSignalDemo.java
public class TrafficSignalDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   TRAFFIC SIGNAL CONTROL SYSTEM DEMO         ");
        System.out.println("═══════════════════════════════════════════════\n");
        
        CentralController central = CentralController.getInstance();
        
        // ===== SCENARIO 1: Create Intersection =====
        System.out.println("═══ SCENARIO 1: Setup Intersection ═══\n");
        
        Intersection intersection1 = new Intersection(
            "INT-001",
            "Main St & Oak Ave",
            IntersectionType.FOUR_WAY
        );
        
        IntersectionController controller1 = new IntersectionController(
            "CTRL-001",
            intersection1
        );
        
        central.registerIntersection(controller1);
        
        // ===== SCENARIO 2: Configure Schedule =====
        System.out.println("═══ SCENARIO 2: Configure Peak Hours Schedule ═══\n");
        
        SignalSchedule peakSchedule = new SignalSchedule(
            "Peak Hours",
            ScheduleType.PEAK_HOURS
        );
        peakSchedule.setGreenDuration(Direction.NORTH, 60); // Main road
        peakSchedule.setGreenDuration(Direction.SOUTH, 60);
        peakSchedule.setGreenDuration(Direction.EAST, 30);  // Side road
        peakSchedule.setGreenDuration(Direction.WEST, 30);
        
        controller1.setSchedule(peakSchedule);
        
        // ===== SCENARIO 3: Start Normal Operation =====
        System.out.println("═══ SCENARIO 3: Start Normal Operation ═══\n");
        
        controller1.start();
        
        System.out.println("Running normal cycle for 20 seconds...\n");
        Thread.sleep(20000);
        
        central.showSystemStatus();
        
        // ===== SCENARIO 4: Emergency Vehicle =====
        System.out.println("\n═══ SCENARIO 4: Emergency Vehicle Approaching ═══\n");
        
        controller1.handleEmergency(Direction.NORTH);
        
        System.out.println("Emergency mode active for 10 seconds...\n");
        Thread.sleep(10000);
        
        // Resume normal
        controller1.setMode(new NormalMode());
        System.out.println("Resumed normal operation\n");
        
        Thread.sleep(10000);
        
        // ===== SCENARIO 5: Night Mode =====
        System.out.println("\n═══ SCENARIO 5: Switch to Night Mode ═══\n");
        
        controller1.setMode(new NightMode());
        
        System.out.println("Night mode active for 10 seconds...\n");
        Thread.sleep(10000);
        
        // ===== SCENARIO 6: Safety Validation =====
        System.out.println("\n═══ SCENARIO 6: Safety Validation Test ═══\n");
        
        SafetyValidator validator = new SafetyValidator();
        
        Map<Direction, SignalState> safeConfig = Map.of(
            Direction.NORTH, SignalState.GREEN,
            Direction.SOUTH, SignalState.GREEN,
            Direction.EAST, SignalState.RED,
            Direction.WEST, SignalState.RED
        );
        
        System.out.println("Testing safe configuration (N-S green, E-W red):");
        System.out.println("   Safe: " + !validator.hasConflictingGreens(safeConfig));
        
        Map<Direction, SignalState> unsafeConfig = Map.of(
            Direction.NORTH, SignalState.GREEN,
            Direction.EAST, SignalState.GREEN,  // Conflict!
            Direction.SOUTH, SignalState.RED,
            Direction.WEST, SignalState.RED
        );
        
        System.out.println("\nTesting unsafe configuration (N green, E green):");
        System.out.println("   Safe: " + !validator.hasConflictingGreens(unsafeConfig));
        
        // Cleanup
        System.out.println("\n═══ Shutting Down System ═══\n");
        central.stopAll();
        
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("         DEMO COMPLETED                        ");
        System.out.println("═══════════════════════════════════════════════");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Singleton Pattern
**Where:** CentralController  
**Why:** Single central authority managing all intersections  
**Interview Justification:** "Only one central controller should exist to coordinate city-wide traffic. Multiple instances would lead to conflicting commands."

---

### Pattern 2: State Machine Pattern
**Where:** IntersectionController + Signal transitions  
**Why:** Signals have well-defined states and transitions  
**Interview Justification:** "Traffic signals are inherently state machines. Each signal transitions through Red → Green → Yellow → Red. State pattern ensures valid transitions and prevents illegal states (e.g., skipping yellow)."

---

### Pattern 3: Strategy Pattern
**Where:** OperatingMode hierarchy  
**Why:** Different modes have different transition logic  
**Interview Justification:** "Normal, Emergency, Night modes have different control algorithms. Strategy pattern allows switching modes at runtime without changing controller code."

---

### Pattern 4: Template Method Pattern
**Where:** OperatingMode.getNextTransition()  
**Why:** Common structure, variant behavior  
**Interview Justification:** "All modes follow same control loop (get transition → validate → execute), but calculate next transition differently. Template method defines structure, subclasses provide specifics."

---

### Pattern 5: Observer Pattern (Extension)
**Where:** Signal state changes notify monitoring system  
**Interview Justification:** "When signal changes state, multiple systems need notification: audit logger, dashboard, analytics. Observer decouples signal from subscribers."

```java
public interface SignalObserver {
    void onStateChange(Signal signal, SignalState oldState, SignalState newState);
}

public class Signal {
    private List<SignalObserver> observers = new ArrayList<>();
    
    public void addObserver(SignalObserver observer) {
        observers.add(observer);
    }
    
    public void setState(SignalState newState) {
        SignalState oldState = this.currentState;
        this.currentState = newState;
        
        // Notify all observers
        for (SignalObserver observer : observers) {
            observer.onStateChange(this, oldState, newState);
        }
    }
}
```

---

## 🔒 Step 8: Handle Concurrency (10 minutes)

### Critical Section Analysis

#### 1. **Signal State Transitions**

**Problem:** Multiple threads changing same signal state

**Solution:**
```java
public synchronized void setState(SignalState newState) {
    // Atomic check-and-set
    if (!canChangeTo(newState)) {
        throw new IllegalStateException(...);
    }
    this.currentState = newState;
    this.lastChangeTime = LocalDateTime.now();
}
```

**Interview Explanation:** "Signal state changes must be atomic. Synchronized ensures only one thread can change state at a time. Prevents race condition where two transitions happen simultaneously."

---

#### 2. **Controller Start/Stop**

**Problem:** Start called while already running

**Solution:**
```java
public synchronized void start() {
    if (running) {
        return; // Already running
    }
    running = true;
    executor.scheduleAtFixedRate(...);
}
```

---

#### 3. **Safety Validation**

**Problem:** Check-then-act race condition

**Bad:**
```java
if (validator.validateTransition(...)) {  // Check
    executeTransition(...);               // Act (time gap!)
}
```

**Good:**
```java
private synchronized void controlLoop() {
    Transition transition = currentMode.getNextTransition(...);
    if (!validator.validateTransition(...)) {
        enterFailSafeMode();
        return;
    }
    executeTransition(transition); // Still synchronized
}
```

---

#### 4. **Central Controller Registration**

**Problem:** Multiple controllers registering simultaneously

**Solution:**
```java
private final Map<String, IntersectionController> controllers = 
    new ConcurrentHashMap<>();

public void registerIntersection(IntersectionController controller) {
    controllers.put(controller.getId(), controller);
}
```

**Interview Explanation:** "ConcurrentHashMap handles concurrent puts safely. No need to synchronize entire method. Better performance for high-concurrency registration."

---

### Real-Time Constraints

**Timing Precision:**
```java
// Bad: Using Thread.sleep() (drift accumulates)
while (running) {
    doWork();
    Thread.sleep(1000); // Actual: 1000ms + doWork() time
}

// Good: Using ScheduledExecutorService (compensates for drift)
executor.scheduleAtFixedRate(
    this::controlLoop,
    0,
    1,
    TimeUnit.SECONDS  // Maintains 1Hz frequency
);
```

**Interview Explanation:** "Traffic signals need precise timing. Yellow must be exactly 3 seconds, not 3.5. ScheduledExecutorService maintains fixed rate by compensating for execution time."

---

## 💡 Step 9: Interview Discussion Points

### Question 1: "How would you implement adaptive traffic signals based on real-time traffic?"

**Answer:**

"Adaptive signals adjust timing based on traffic density.

**Sensors:**
```java
public class TrafficSensor {
    private final Direction direction;
    private int vehicleCount;
    private double averageSpeed;
    
    public TrafficDensity measureDensity() {
        // Using cameras, loop detectors, or radar
        if (vehicleCount > 50) return TrafficDensity.HEAVY;
        if (vehicleCount > 20) return TrafficDensity.MEDIUM;
        return TrafficDensity.LIGHT;
    }
}
```

**Adaptive Mode:**
```java
public class AdaptiveMode extends OperatingMode {
    private Map<Direction, TrafficSensor> sensors;
    
    @Override
    public Transition getNextTransition(IntersectionController controller,
                                       Direction current) {
        // Measure traffic density for all directions
        Map<Direction, TrafficDensity> densities = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction, TrafficSensor> entry : sensors.entrySet()) {
            densities.put(entry.getKey(), entry.getValue().measureDensity());
        }
        
        // Calculate optimal green time
        int greenDuration = calculateOptimalGreen(current, densities);
        
        return new Transition(current, SignalState.GREEN, greenDuration, true);
    }
    
    private int calculateOptimalGreen(Direction dir, 
                                     Map<Direction, TrafficDensity> densities) {
        TrafficDensity density = densities.get(dir);
        
        // Base duration + density multiplier
        return switch (density) {
            case HEAVY -> 60;  // Heavy traffic: 60 seconds
            case MEDIUM -> 40;
            case LIGHT -> 20;  // Light traffic: 20 seconds
        };
    }
}
```

**Benefits:**
- Reduces wait time by 20-30%
- Better flow during peak hours
- Responds to incidents (accident → longer red)

**Interview Discussion:**
'Adaptive signals use real-time data. Simple algorithm: heavier traffic → longer green. Advanced: use ML to predict patterns and optimize across multiple intersections (green wave).'"

---

### Question 2: "How to coordinate multiple intersections for 'green wave'?"

**Answer:**

"Green wave: driver hitting all greens at optimal speed.

**Concept:**
```
Intersection 1 --500m-- Intersection 2 --500m-- Intersection 3

If driver travels at 50 km/h:
- Distance: 500m
- Time: 36 seconds
- Offset Int2 green by 36 seconds after Int1
```

**Implementation:**
```java
public class GreenWaveCoordinator {
    private List<IntersectionController> corridor;
    private int optimalSpeed; // km/h
    private int distanceBetween; // meters
    
    public void coordinateCorridor() {
        int offset = calculateOffset();
        
        for (int i = 0; i < corridor.size(); i++) {
            IntersectionController controller = corridor.get(i);
            
            // Offset each intersection
            int phaseOffset = i * offset;
            controller.setPhaseOffset(phaseOffset);
        }
    }
    
    private int calculateOffset() {
        // time = distance / speed
        // speed in m/s = (km/h * 1000) / 3600
        double speedMs = (optimalSpeed * 1000.0) / 3600.0;
        return (int) (distanceBetween / speedMs);
    }
}

// In IntersectionController
public class IntersectionController {
    private int phaseOffset; // Delay before starting cycle
    
    public void start() {
        executor.scheduleAtFixedRate(
            this::controlLoop,
            phaseOffset, // Initial delay
            1,
            TimeUnit.SECONDS
        );
    }
}
```

**Result:** Vehicles moving at 50 km/h hit all greens. Reduces stops by 70%.

**Challenge:** Works for one direction only (e.g., morning commute)."

---

### Question 3: "How to handle signal hardware failure?"

**Answer:**

"Multi-layer fault detection and recovery.

**Layer 1: Self-Monitoring**
```java
public class SignalMonitor {
    private Map<Signal, HealthStatus> healthMap;
    
    public void monitorSignal(Signal signal) {
        // Check if signal is responding
        if (signal.getTimeSinceChange().toSeconds() > 300) {
            // Stuck in one state for 5 minutes
            reportMalfunction(signal, "Signal stuck");
        }
        
        // Check for rapid state changes (hardware fault)
        if (signal.getStateChangesPerMinute() > 100) {
            reportMalfunction(signal, "Rapid flickering");
        }
    }
    
    private void reportMalfunction(Signal signal, String reason) {
        System.err.println("⚠️  Signal malfunction: " + 
                         signal.getId() + " - " + reason);
        
        // Notify central controller
        centralController.handleSignalFailure(signal);
    }
}
```

**Layer 2: Degraded Operation**
```java
public void handleSignalFailure(Signal failedSignal) {
    Direction failedDirection = failedSignal.getDirection();
    
    // Option A: Skip failed direction (3-way operation)
    if (canOperateWithout(failedDirection)) {
        modifyScheduleToSkip(failedDirection);
    }
    
    // Option B: All-red + manual traffic control
    else {
        enterAllRedMode();
        notifyTrafficPolice();
    }
}
```

**Layer 3: Backup Hardware**
```java
public class RedundantSignal {
    private Signal primarySignal;
    private Signal backupSignal;
    
    public void setState(SignalState state) {
        try {
            primarySignal.setState(state);
        } catch (Exception e) {
            System.err.println("Primary failed, using backup");
            backupSignal.setState(state);
        }
    }
}
```

**Layer 4: Battery Backup**
- UPS: 4-8 hours operation during power outage
- Blinking red mode (low power consumption)

**Interview Tip:** 'Hardware failure is common. Design must include detection, graceful degradation, and redundancy. Safety first: when uncertain, go all-red.'"

---

### Question 4: "How to handle pedestrian button requests efficiently?"

**Answer:**

```java
public class PedestrianController {
    private IntersectionController controller;
    private Map<Direction, PedestrianSignal> pedSignals;
    private Map<Direction, Long> lastWalkTime;
    
    public void handleButtonPress(Direction direction) {
        PedestrianSignal signal = pedSignals.get(direction);
        
        // Debounce: Ignore if pressed recently
        Long last = lastWalkTime.get(direction);
        if (last != null && System.currentTimeMillis() - last < 60000) {
            System.out.println("Pedestrian button: Wait for next cycle");
            return;
        }
        
        // Register request
        signal.pressButton();
        System.out.println("Pedestrian request registered for " + direction);
        
        // Option 1: Wait for next red (vehicle)
        // Option 2: Shorten current green, give walk sooner
        
        // Using Option 1: Mark as pending
        controller.scheduleWalkPhase(direction);
    }
    
    public void giveWalkSignal(Direction direction) {
        PedestrianSignal signal = pedSignals.get(direction);
        
        if (!signal.isButtonPressed()) {
            return; // No request, skip walk phase
        }
        
        // Give walk signal
        signal.setState(SignalState.WALK);
        
        // Hold for walk duration (15-30 seconds)
        try {
            Thread.sleep(signal.getWalkDuration() * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Back to don't walk
        signal.setState(SignalState.DONT_WALK);
        signal.clearButton();
        
        lastWalkTime.put(direction, System.currentTimeMillis());
    }
}
```

**Optimization:** Only give walk if button pressed (save time when no pedestrians).

**Accessibility:** 
- Audio signals for visually impaired
- Longer walk times at senior centers/schools"

---

### Question 5: "How would you test this system?"

**Answer:**

"Multi-layer testing strategy:

**Unit Tests:**
```java
@Test
public void testSafetyValidator_DetectsConflictingGreens() {
    SafetyValidator validator = new SafetyValidator();
    
    Map<Direction, SignalState> states = Map.of(
        Direction.NORTH, SignalState.GREEN,
        Direction.EAST, SignalState.GREEN  // Conflict!
    );
    
    assertTrue(validator.hasConflictingGreens(states));
}

@Test
public void testSignalTransition_EnforcesYellowBetweenGreenAndRed() {
    VehicleSignal signal = new VehicleSignal("test", Direction.NORTH);
    signal.setState(SignalState.GREEN);
    
    // Should throw exception (must go through yellow)
    assertThrows(IllegalStateException.class, () -> {
        signal.setState(SignalState.RED);
    });
}
```

**Integration Tests:**
```java
@Test
public void testNormalMode_CompletesFullCycle() throws Exception {
    Intersection intersection = new Intersection("test", "loc", FOUR_WAY);
    IntersectionController controller = 
        new IntersectionController("ctrl", intersection);
    
    controller.start();
    Thread.sleep(150000); // 2.5 minutes
    
    // Verify all directions got green at least once
    for (Direction dir : Direction.values()) {
        assertTrue(gotGreen(intersection, dir));
    }
    
    controller.stop();
}
```

**Safety Property Verification (Formal):**
```java
@Test
public void testSafetyInvariant_NeverConflictingGreens() {
    // Run 10000 random state transitions
    for (int i = 0; i < 10000; i++) {
        randomTransition();
        assertFalse(hasConflictingGreens());
    }
}
```

**Simulation Tests:**
```java
@Test
public void testEmergencyMode_CreatesGreenCorridor() {
    controller.handleEmergency(Direction.NORTH);
    
    // Verify north is green, others are red
    assertEquals(SignalState.GREEN, 
                getNorthSignal().getState());
    assertEquals(SignalState.RED, 
                getEastSignal().getState());
}
```

**Load Tests:**
- 1000 intersections running simultaneously
- Verify CPU and memory usage acceptable

**Hardware-in-Loop Tests:**
- Connect to actual signal hardware
- Verify timing accuracy
- Test power failure scenarios

**Interview Tip:** 'Safety-critical system needs formal verification. Use model checking (TLA+) to prove safety properties hold under all possible executions.'"

---

## ✅ Step 10: SOLID Principles Check

### S - Single Responsibility Principle
| Class | Responsibility | One Reason to Change? |
|-------|----------------|----------------------|
| Signal | Manage signal state | ✅ Only if signal logic changes |
| IntersectionController | Control signal timing | ✅ Only if control algorithm changes |
| SafetyValidator | Validate safety rules | ✅ Only if safety rules change |
| OperatingMode | Define transition logic | ✅ Only if mode behavior changes |

### O - Open/Closed Principle
- ✅ Add new OperatingMode (e.g., FloodMode) without modifying controller
- ✅ Add new IntersectionType without changing Signal
- ✅ Add new monitoring without modifying controllers (Observer)

### L - Liskov Substitution Principle
```java
Signal signal = new VehicleSignal(...);  // Can substitute
signal = new PedestrianSignal(...);      // Any signal works

OperatingMode mode = new NormalMode();   // Can substitute
mode = new EmergencyMode(...);           // Any mode works
```
✅ All substitutions work correctly

### I - Interface Segregation Principle
- ✅ No fat interfaces
- ✅ Each interface has focused purpose

### D - Dependency Inversion Principle
```java
// Controller depends on OperatingMode abstraction
private OperatingMode currentMode; // ✅ Abstract, not concrete
```
✅ Depend on abstractions

---

## 🎯 Interview Tips & Talking Points

### Opening (30 seconds):
"Let me understand requirements - intersection types, safety constraints, timing requirements. Then identify core entities (Signal, Controller, Intersection), establish state machine for signal transitions, implement safety validator, and show how different modes work."

### During Safety Discussion (Critical!):
"Safety is paramount - we must formally verify no conflicting greens ever occur. I'm using SafetyValidator with explicit conflict rules. Any error triggers fail-safe mode (all red). Yellow duration is minimum 3 seconds (legal requirement)."

### During State Machine Discussion:
"Signals are state machines with enforced transitions: Green → Yellow → Red → Green. Skipping yellow is illegal and dangerous. Each transition validated before execution."

### During Mode Discussion:
"Using Strategy pattern for modes - Normal (fixed cycles), Emergency (override for green corridor), Night (blinking), Manual (operator control). Each mode calculates transitions differently."

### When Asked About Real-Time:
"Traffic signals need precise timing. Using ScheduledExecutorService for fixed-rate execution (compensates for processing time). Yellow must be exactly 3 seconds, not 3.2."

### Closing:
"Design ensures safety through formal validation, uses appropriate patterns (State Machine, Strategy), handles real-time constraints, and includes fail-safe mechanisms. Key trade-off: simplicity of fixed timing vs complexity of adaptive signals."

---

## 📈 Complexity Analysis

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| Validate Transition | O(D²) | O(1) |
| Execute Transition | O(D) | O(1) |
| Check Conflicts | O(D²) | O(1) |
| Get Next Transition | O(1) | O(1) |

Where D = number of directions (typically 4)

**System Scale:**
- Space: O(I × D) where I = intersections, D = directions
- 1000 intersections × 8 signals = 8000 signals (manageable)

---

## 🎓 Key Takeaways

### Interview Success Formula:

1. **Clarify** (5 min) - Safety first! Understand constraints
2. **Requirements** (7 min) - Point-wise functional, deduce NFRs (safety paramount)
3. **Entities** (12 min) - Signal hierarchy, Controller, Modes
4. **Relationships** (15 min) - State machine, Strategy pattern
5. **Class Diagrams** (12 min) - Focus on safety validation
6. **Implementation** (25 min) - State transitions, mode switching, safety checks
7. **Patterns** (5 min) - State Machine, Strategy, Singleton, Observer
8. **Safety Discussion** (10 min) - Fail-safe, validation, formal verification

### What Makes This Design Good:

✅ **Safety First** - Formal validation, fail-safe mode  
✅ **Real-Time** - Precise timing with ScheduledExecutorService  
✅ **Extensible** - Easy to add new modes, intersection types  
✅ **Fault Tolerant** - Handles failures gracefully  
✅ **State Machine** - Enforced valid transitions  
✅ **Strategy Pattern** - Pluggable operating modes  

### Common Mistakes to Avoid:

❌ Not validating safety before transitions  
❌ Allowing yellow to be skipped  
❌ No fail-safe mode  
❌ Imprecise timing (using Thread.sleep loops)  
❌ Not handling conflicting greens  
❌ Synchronous blocking operations in control loop  
❌ No emergency override capability  

---

**This systematic approach works for any real-time control system!**
