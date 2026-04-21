# Low-Level Design: Parking Lot System

**Difficulty:** Medium ⚡

**Interview Duration:** 45-60 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a parking lot system that can handle multiple floors, different vehicle types, and automatic fee calculation."*

### Clarifying Questions to Ask:

1. **Q:** What types of vehicles should the system support?  
   **A:** Cars, motorcycles, trucks, and vans.

2. **Q:** Are there different types of parking spots?  
   **A:** Yes - compact spots, large spots, motorcycle spots, and handicapped spots.

3. **Q:** How should parking fees be calculated?  
   **A:** Based on vehicle type and duration. Hourly rate system.

4. **Q:** Should the system handle payments?  
   **A:** Yes, support multiple payment methods (cash, credit card, digital wallet).

5. **Q:** How many entry/exit points?  
   **A:** Multiple entry and exit panels.

6. **Q:** Do we need to track spot availability in real-time?  
   **A:** Yes, display boards should show available spots per floor.

7. **Q:** Scale expectations?  
   **A:** Should support 10+ floors, 2000+ spots, hundreds of concurrent vehicles.

---

## 🔹 Step 2: Gather Requirements (5 minutes)

### Functional Requirements

List exactly as interviewer presents them:

1. The system should allow vehicles to enter the parking lot
2. The system should issue a parking ticket to each vehicle upon entry
3. The system should assign appropriate parking spots based on vehicle type
4. Different vehicles need different spot types (motorcycle → motorcycle spot, truck → large spot)
5. The system should track which spots are occupied and which are free
6. The system should calculate parking fees based on vehicle type and parking duration
7. The system should accept payment through multiple methods (cash, card, digital wallet)
8. The system should allow vehicles to exit after successful payment
9. Each floor should have a display board showing available spots by type
10. Admin should be able to add or remove floors and parking spots
11. The system should prevent overbooking (two vehicles in one spot)
12. The system should handle multiple entry and exit points

### Non-Functional Requirements - How to Deduce Them

**Systematic Approach to NFR Deduction:**

#### Framework: Use SCAMPS
- **S**calability
- **C**onsistency
- **A**vailability
- **M**aintainability
- **P**erformance
- **S**ecurity

Let's apply this:

#### 1. **Scalability** (Ask: "What's the expected load?")
- **Deduction:** 10 floors × 200 spots/floor = 2000 spots
- **Requirement:** System must handle 2000+ concurrent spot assignments
- **Implication:** Need efficient data structures, possibly sharding

#### 2. **Consistency** (Ask: "What must always be accurate?")
- **Deduction:** Two vehicles cannot occupy the same spot
- **Requirement:** Strong consistency for spot assignment (ACID transactions)
- **Implication:** Use locks, synchronized blocks, or database transactions

#### 3. **Availability** (Ask: "What's the uptime requirement?")
- **Deduction:** Physical parking lot operates 24/7
- **Requirement:** 99.9% availability (< 9 hours downtime/year)
- **Implication:** Need redundancy, failover mechanisms

#### 4. **Maintainability** (Ask: "How often will requirements change?")
- **Deduction:** May add new vehicle types, spot types, payment methods
- **Requirement:** Code should be extensible without major refactoring
- **Implication:** Use SOLID principles, design patterns

#### 5. **Performance** (Ask: "What operations are time-critical?")
- **Deduction:** Finding available spot should be quick (users waiting at entry)
- **Requirement:** Spot assignment < 100ms, payment processing < 500ms
- **Implication:** Use indexed data structures, caching

#### 6. **Concurrency** (Ask: "How many simultaneous operations?")
- **Deduction:** Multiple vehicles entering/exiting at same time
- **Requirement:** Thread-safe operations, prevent race conditions
- **Implication:** Synchronization, atomic operations

**Note:** In interview, verbalize this thinking process. Show you're not just listing NFRs randomly, but deducing them from the problem context.

---

## 🧩 Step 3: Identify Core Entities (10 minutes)

### Systematic Approach to Entity Identification

**Method: Noun Extraction + CRUD Analysis**

#### Process:
1. **Extract nouns from requirements**
2. **Check if it has state (attributes)**
3. **Check if it has behavior (methods)**
4. **Check if it needs CRUD operations**
5. **Group related concepts**

Let's apply this systematically:

### From Requirements → Entities

| Requirement Phrase | Noun Identified | Has State? | Has Behavior? | Entity? |
|-------------------|-----------------|------------|---------------|---------|
| "vehicles to enter" | Vehicle | ✅ license, type | ✅ park, exit | ✅ YES |
| "parking ticket to each" | Ticket | ✅ time, spot | ✅ calculate fee | ✅ YES |
| "parking spots based on type" | Spot | ✅ location, status | ✅ assign, free | ✅ YES |
| "parking fees based on" | Fee/Payment | ✅ amount, method | ✅ process | ✅ YES |
| "each floor should have" | Floor | ✅ spots list | ✅ find spot | ✅ YES |
| "display board showing" | DisplayBoard | ✅ availability | ✅ update | ✅ YES |
| "multiple entry points" | EntryPanel | ✅ location | ✅ print ticket | ✅ YES |
| "exit after payment" | ExitPanel | ✅ location | ✅ process exit | ✅ YES |
| "the parking lot" | ParkingLot | ✅ floors | ✅ manage all | ✅ YES |

### Entity Refinement - Apply Hierarchies

**Step 1:** Identify entities with multiple variants
- Vehicle → Car, Motorcycle, Truck, Van (different parking needs)
- ParkingSpot → Compact, Large, Motorcycle, Disabled (different sizes)
- Payment → Cash, CreditCard, DigitalWallet (different processing)

**Step 2:** Identify entities that need to be unique
- ParkingLot → Should be Singleton (only one parking lot)

**Step 3:** Identify entities that connect others
- ParkingTicket → Links Vehicle + ParkingSpot + Time

### Final Entity List (9 Core Entities)

1. **Vehicle** (Abstract) + 4 concrete types
2. **ParkingSpot** (Abstract) + 4 concrete types
3. **ParkingTicket** (Concrete)
4. **Payment** (Abstract) + 3 concrete types
5. **ParkingFloor** (Concrete)
6. **ParkingLot** (Singleton)
7. **EntryPanel** (Concrete)
8. **ExitPanel** (Concrete)
9. **DisplayBoard** (Concrete)

**Why these and not others?**
- Each has clear state and behavior
- Each appears in multiple requirements
- Each has a distinct lifecycle
- Each has CRUD operations or business logic

---

## 🔗 Step 4: Establish Relationships (10 minutes)

### Systematic Approach to Relationship Mapping

**Method: Three-Pass Analysis**

#### Pass 1: Identify Inheritance (IS-A)

**Rule:** If Entity X is a specialized version of Entity Y → Inheritance

| Parent | Child | Why? |
|--------|-------|------|
| Vehicle | Car, Motorcycle, Truck, Van | All are types of vehicles with same base behavior but different size requirements |
| ParkingSpot | Compact, Large, Motorcycle, Disabled | All are parking spots but with different capacity rules |
| Payment | Cash, CreditCard, DigitalWallet | All process payments but with different mechanisms |

**Design Decision:** Use abstract classes (not interfaces) because:
- Common implementation exists (license number for vehicles, spot status for spots)
- Establishes clear "is-a" relationship
- Allows shared code in parent class

#### Pass 2: Identify Ownership (HAS-A)

**Rule:** Ask "Does X own Y's lifecycle?" → Strong (Composition) vs Weak (Aggregation)

| Container | Contained | Type | Why? |
|-----------|-----------|------|------|
| ParkingLot | ParkingFloor | **Composition** (Strong) | Floors cannot exist without parking lot. Deleting parking lot deletes floors. |
| ParkingFloor | ParkingSpot | **Composition** (Strong) | Spots belong to specific floor. Deleting floor deletes spots. |
| ParkingSpot | Vehicle | **Aggregation** (Weak) | Vehicle exists before parking and after leaving. Spot doesn't own vehicle. |
| ParkingTicket | Vehicle | **Aggregation** (Weak) | Ticket references vehicle but doesn't control its lifecycle. |
| ParkingTicket | ParkingSpot | **Aggregation** (Weak) | Ticket records which spot was used but doesn't own it. |

**Test Question:** "If I delete the container, should the contained object be deleted?"
- **Yes** → Composition (strong ownership)
- **No** → Aggregation (weak reference)

#### Pass 3: Identify Cardinality

**Method:** For each relationship, ask "How many?"

| Relationship | From | To | Cardinality |
|--------------|------|----|-----------| 
| ParkingLot ↔ ParkingFloor | 1 | Many | 1:N |
| ParkingFloor ↔ ParkingSpot | 1 | Many | 1:N |
| ParkingSpot ↔ Vehicle | 0..1 | 0..1 | 0..1:0..1 |
| ParkingLot ↔ EntryPanel | 1 | Many | 1:N |
| ParkingLot ↔ ExitPanel | 1 | Many | 1:N |
| ParkingFloor ↔ DisplayBoard | 1 | 1 | 1:1 |
| Vehicle ↔ ParkingTicket | 1 | 1 | 1:1 |
| Payment ↔ ParkingTicket | 1 | 1 | 1:1 |

### Relationship Summary Diagram

```
ParkingLot (Singleton)
    │
    ├─── [1:N Composition] ──→ ParkingFloor
    │                               │
    │                               ├─── [1:N Composition] ──→ ParkingSpot
    │                               │                              │
    │                               │                              └─── [0..1:0..1 Aggregation] ──→ Vehicle
    │                               │
    │                               └─── [1:1 Association] ──→ DisplayBoard
    │
    ├─── [1:N Composition] ──→ EntryPanel
    │
    └─── [1:N Composition] ──→ ExitPanel

ParkingTicket
    ├─── [1:1 Aggregation] ──→ Vehicle
    ├─── [1:1 Aggregation] ──→ ParkingSpot
    └─── [1:1 Association] ──→ Payment
```

---

## 📐 Step 5: Design Class Diagrams (10 minutes)

### Design Approach:
1. Start with core entities
2. Add attributes (focus on essential ones)
3. Add key methods (focus on public interface)
4. Show relationships with proper notation

### Notation Guide:
- `+` = public
- `-` = private
- `#` = protected
- `<<abstract>>` = abstract class
- `△` = inheritance arrow
- `◆` = composition (filled diamond)
- `◇` = aggregation (empty diamond)

---

### Class Diagram 1: Vehicle Hierarchy

```
┌─────────────────────────────────────────┐
│         <<abstract>>                    │
│            Vehicle                      │
├─────────────────────────────────────────┤
│ - licenseNumber: String                 │
│ - type: VehicleType                     │
│ - entryTime: LocalDateTime              │
├─────────────────────────────────────────┤
│ + Vehicle(license: String, type: Type)  │
│ + getLicenseNumber(): String            │
│ + getType(): VehicleType                │
│ + getEntryTime(): LocalDateTime         │
└─────────────────────────────────────────┘
                    △
                    │
        ┌───────────┼──────────┬─────────┐
        │           │          │         │
┌───────┴───┐  ┌───┴────┐  ┌──┴────┐  ┌─┴─────────┐
│    Car    │  │ Truck  │  │  Van  │  │Motorcycle │
├───────────┤  ├────────┤  ├───────┤  ├───────────┤
│           │  │        │  │       │  │           │
├───────────┤  ├────────┤  ├───────┤  ├───────────┤
│+Car(...)  │  │+Truck()│  │+Van() │  │+Motor...()│
└───────────┘  └────────┘  └───────┘  └───────────┘
```

**Why this hierarchy?**
- Common attributes (license, entry time) in parent
- Type-specific behavior in children
- Open for extension (can add Bus, Bicycle later)

---

### Class Diagram 2: ParkingSpot Hierarchy

```
┌──────────────────────────────────────────────┐
│            <<abstract>>                      │
│            ParkingSpot                       │
├──────────────────────────────────────────────┤
│ - id: String                                 │
│ - floor: int                                 │
│ - isFree: boolean                            │
│ - vehicle: Vehicle                           │
│ - type: SpotType                             │
├──────────────────────────────────────────────┤
│ + ParkingSpot(id, floor, type)               │
│ + assignVehicle(v: Vehicle): boolean         │
│ + removeVehicle(): void                      │
│ + isFree(): boolean                          │
│ + canFitVehicle(v: Vehicle): boolean [abstract]│
│ + getId(): String                            │
│ + getType(): SpotType                        │
└──────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┼──────────┬──────────┐
        │           │          │          │
┌───────┴──────┐ ┌─┴────────┐ ┌┴────────┐ ┌┴──────────┐
│ CompactSpot  │ │LargeSpot │ │Motorcycle│ │Disabled  │
│              │ │          │ │  Spot    │ │  Spot    │
├──────────────┤ ├──────────┤ ├──────────┤ ├──────────┤
│              │ │          │ │          │ │          │
├──────────────┤ ├──────────┤ ├──────────┤ ├──────────┤
│+canFit():    │ │+canFit():│ │+canFit():│ │+canFit():│
│  CAR,        │ │  ALL     │ │  MOTOR   │ │  CAR with│
│  MOTORCYCLE  │ │          │ │  only    │ │  permit  │
└──────────────┘ └──────────┘ └──────────┘ └──────────┘
```

**Key Design Decision:**
- `canFitVehicle()` is abstract → each subclass defines its own rules
- `assignVehicle()` is synchronized → prevents race conditions
- Spot doesn't own Vehicle → aggregation (◇) not composition

---

### Class Diagram 3: ParkingTicket

```
┌──────────────────────────────────────────────┐
│           ParkingTicket                      │
├──────────────────────────────────────────────┤
│ - ticketNumber: String                       │
│ - issuedAt: LocalDateTime                    │
│ - exitTime: LocalDateTime                    │
│ - vehicle: Vehicle                  ◇────────┼──→ Vehicle
│ - spot: ParkingSpot                 ◇────────┼──→ ParkingSpot
│ - status: TicketStatus                       │
├──────────────────────────────────────────────┤
│ + ParkingTicket(vehicle, spot)               │
│ + calculateDuration(): Duration              │
│ + updateExitTime(): void                     │
│ + markAsPaid(): void                         │
│ + getVehicle(): Vehicle                      │
│ + getSpot(): ParkingSpot                     │
│ + getStatus(): TicketStatus                  │
└──────────────────────────────────────────────┘
```

**Why this design?**
- Acts as "glue" between Vehicle and ParkingSpot
- Maintains temporal information (issued/exit time)
- Independent lifecycle (ticket created after vehicle enters, destroyed after exit)

---

### Class Diagram 4: Payment Hierarchy

```
┌──────────────────────────────────────────────┐
│           <<abstract>>                       │
│             Payment                          │
├──────────────────────────────────────────────┤
│ - amount: double                             │
│ - timestamp: LocalDateTime                   │
│ - status: PaymentStatus                      │
│ - ticket: ParkingTicket            ◇─────────┼──→ Ticket
├──────────────────────────────────────────────┤
│ + Payment(amount, ticket)                    │
│ + processPayment(): boolean [abstract]       │
│ + getAmount(): double                        │
│ + getStatus(): PaymentStatus                 │
│ # markCompleted(): void                      │
│ # markFailed(): void                         │
└──────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┼──────────────┐
        │           │              │
┌───────┴────────┐ ┌┴─────────────┐ ┌┴──────────────┐
│ CashPayment   │ │CreditCard    │ │DigitalWallet │
│               │ │Payment       │ │Payment       │
├───────────────┤ ├──────────────┤ ├───────────────┤
│-cashReceived  │ │-cardNumber   │ │-walletId      │
│               │ │-cvv          │ │-transactionId │
├───────────────┤ ├──────────────┤ ├───────────────┤
│+processPayment│ │+processPayment││+processPayment│
│+getChange()   │ │-validateCard()││               │
└───────────────┘ └──────────────┘ └───────────────┘
```

**Design Pattern:** Strategy Pattern
- Each payment method has different processing logic
- Easy to add new payment methods (UPI, PayPal)
- Client code doesn't need to know payment details

---

### Class Diagram 5: ParkingFloor

```
┌──────────────────────────────────────────────┐
│          ParkingFloor                        │
├──────────────────────────────────────────────┤
│ - floorNumber: int                           │
│ - spots: Map<String, ParkingSpot>  ◆─────────┼──→ ParkingSpot [1..*]
│ - displayBoard: DisplayBoard       ◆─────────┼──→ DisplayBoard [1]
├──────────────────────────────────────────────┤
│ + ParkingFloor(floorNumber: int)             │
│ + addSpot(spot: ParkingSpot): void           │
│ + removeSpot(spotId: String): void           │
│ + findAvailableSpot(vehicle: Vehicle): Spot  │
│ + updateDisplayBoard(): void                 │
│ + getFloorNumber(): int                      │
└──────────────────────────────────────────────┘
```

**Composition vs Aggregation:**
- `spots`: Composition (◆) - spots belong to floor, deleted with floor
- `displayBoard`: Composition (◆) - board is part of floor infrastructure

---

### Class Diagram 6: ParkingLot (Central System)

```
┌──────────────────────────────────────────────────┐
│          <<Singleton>>                           │
│           ParkingLot                             │
├──────────────────────────────────────────────────┤
│ - instance: ParkingLot [static]                  │
│ - name: String                                   │
│ - address: String                                │
│ - floors: List<ParkingFloor>      ◆──────────────┼──→ Floor [1..*]
│ - entryPanels: List<EntryPanel>   ◆──────────────┼──→ Entry [1..*]
│ - exitPanels: List<ExitPanel>     ◆──────────────┼──→ Exit [1..*]
│ - activeTickets: Map<String,Ticket>              │
├──────────────────────────────────────────────────┤
│ - ParkingLot() [private constructor]             │
│ + getInstance(): ParkingLot [static, synchronized]│
│ + parkVehicle(vehicle: Vehicle): Ticket          │
│ + unparkVehicle(ticket, payment): Payment        │
│ + calculateFee(ticket: Ticket): double           │
│ + isFull(): boolean                              │
│ - findAvailableSpot(vehicle): Spot               │
│ - updateAllDisplayBoards(): void                 │
└──────────────────────────────────────────────────┘
```

**Why Singleton?**
- Only one parking lot instance should exist
- Global access point needed
- Prevents multiple instances with inconsistent state

---

### Class Diagram 7: EntryPanel & ExitPanel

```
┌──────────────────────────┐       ┌──────────────────────────┐
│     EntryPanel           │       │      ExitPanel           │
├──────────────────────────┤       ├──────────────────────────┤
│ - id: String             │       │ - id: String             │
├──────────────────────────┤       ├──────────────────────────┤
│ + EntryPanel(id)         │       │ + ExitPanel(id)          │
│ + printTicket(vehicle,   │       │ + processPayment(payment)│
│   spot): Ticket          │       │   : boolean              │
│ + getId(): String        │       │ + printReceipt(payment)  │
└──────────────────────────┘       │ + getId(): String        │
                                   └──────────────────────────┘
```

---

### Class Diagram 8: DisplayBoard

```
┌──────────────────────────────────────────────┐
│          DisplayBoard                        │
├──────────────────────────────────────────────┤
│ - id: String                                 │
│ - floorNumber: int                           │
│ - availableSpots: Map<SpotType, Integer>     │
├──────────────────────────────────────────────┤
│ + DisplayBoard(id, floorNumber)              │
│ + updateAvailability(type, count): void      │
│ + showAvailability(): void                   │
│ + getAvailableSpots(type): int               │
└──────────────────────────────────────────────┘
```

---

### Complete System Architecture

```
                    Entry/Exit
                        │
                        ▼
        ┌───────────────────────────────┐
        │      ParkingLot (Singleton)   │◀──── Admin
        │  - findAvailableSpot()        │
        │  - parkVehicle()              │
        │  - unparkVehicle()            │
        │  - calculateFee()             │
        └───────────┬───────────────────┘
                    │
                    │ 1:N (Composition)
                    ▼
        ┌───────────────────────────────┐
        │       ParkingFloor            │
        │  - findAvailableSpot()        │
        │  - updateDisplayBoard()       │
        └───────────┬───────────────────┘
                    │
            ┌───────┴────────┐
            │                │
            │ 1:N            │ 1:1
            ▼                ▼
    ┌──────────────┐   ┌────────────┐
    │ ParkingSpot  │   │DisplayBoard│
    │-assignVehicle│   │-update...  │
    └──────┬───────┘   └────────────┘
           │
           │ 0..1:0..1 (Aggregation)
           ▼
    ┌──────────────┐
    │   Vehicle    │
    └──────┬───────┘
           │
           │ 1:1
           ▼
    ┌──────────────┐
    │ParkingTicket │
    └──────┬───────┘
           │
           │ 1:1
           ▼
    ┌──────────────┐
    │   Payment    │
    └──────────────┘
```

---

## 💻 Step 6: Core Implementation (15 minutes)

### Strategy: Implement Core Flow First

**Priority Order:**
1. Enums (simple, no dependencies)
2. Leaf entities (Vehicle, no dependencies)
3. Container entities (ParkingSpot)
4. Connector entities (ParkingTicket, Payment)
5. Aggregate roots (ParkingFloor, ParkingLot)

### Implementation

#### Enums

```java
// VehicleType.java
public enum VehicleType {
    CAR, MOTORCYCLE, TRUCK, VAN
}

// SpotType.java
public enum SpotType {
    COMPACT, LARGE, MOTORCYCLE, DISABLED
}

// PaymentStatus.java
public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

// TicketStatus.java
public enum TicketStatus {
    ACTIVE, PAID, LOST
}
```

---

#### Vehicle Hierarchy

```java
// Vehicle.java
import java.time.LocalDateTime;

public abstract class Vehicle {
    private String licenseNumber;
    private VehicleType type;
    private LocalDateTime entryTime;
    
    public Vehicle(String licenseNumber, VehicleType type) {
        this.licenseNumber = licenseNumber;
        this.type = type;
        this.entryTime = LocalDateTime.now();
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
}

// Concrete implementations
public class Car extends Vehicle {
    public Car(String licenseNumber) {
        super(licenseNumber, VehicleType.CAR);
    }
}

public class Motorcycle extends Vehicle {
    public Motorcycle(String licenseNumber) {
        super(licenseNumber, VehicleType.MOTORCYCLE);
    }
}

public class Truck extends Vehicle {
    public Truck(String licenseNumber) {
        super(licenseNumber, VehicleType.TRUCK);
    }
}

public class Van extends Vehicle {
    public Van(String licenseNumber) {
        super(licenseNumber, VehicleType.VAN);
    }
}
```

---

#### ParkingSpot Hierarchy

```java
// ParkingSpot.java
public abstract class ParkingSpot {
    private String id;
    private int floor;
    private boolean isFree;
    private Vehicle vehicle;
    private SpotType type;
    
    public ParkingSpot(String id, int floor, SpotType type) {
        this.id = id;
        this.floor = floor;
        this.type = type;
        this.isFree = true;
        this.vehicle = null;
    }
    
    // Thread-safe assignment
    public synchronized boolean assignVehicle(Vehicle vehicle) {
        if (!isFree) {
            return false; // Already occupied
        }
        
        if (!canFitVehicle(vehicle)) {
            return false; // Vehicle doesn't fit
        }
        
        this.vehicle = vehicle;
        this.isFree = false;
        return true;
    }
    
    public synchronized void removeVehicle() {
        this.vehicle = null;
        this.isFree = true;
    }
    
    public boolean isFree() {
        return isFree;
    }
    
    // Abstract method - each subclass defines its rules
    public abstract boolean canFitVehicle(Vehicle vehicle);
    
    // Getters
    public String getId() { return id; }
    public int getFloor() { return floor; }
    public SpotType getType() { return type; }
    public Vehicle getVehicle() { return vehicle; }
}

// CompactSpot.java
public class CompactSpot extends ParkingSpot {
    public CompactSpot(String id, int floor) {
        super(id, floor, SpotType.COMPACT);
    }
    
    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        VehicleType type = vehicle.getType();
        return type == VehicleType.CAR || type == VehicleType.MOTORCYCLE;
    }
}

// LargeSpot.java
public class LargeSpot extends ParkingSpot {
    public LargeSpot(String id, int floor) {
        super(id, floor, SpotType.LARGE);
    }
    
    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        return true; // Can fit any vehicle type
    }
}

// MotorcycleSpot.java
public class MotorcycleSpot extends ParkingSpot {
    public MotorcycleSpot(String id, int floor) {
        super(id, floor, SpotType.MOTORCYCLE);
    }
    
    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        return vehicle.getType() == VehicleType.MOTORCYCLE;
    }
}

// DisabledSpot.java
public class DisabledSpot extends ParkingSpot {
    public DisabledSpot(String id, int floor) {
        super(id, floor, SpotType.DISABLED);
    }
    
    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        // In real system, check for disabled permit
        return vehicle.getType() == VehicleType.CAR;
    }
}
```

---

#### ParkingTicket

```java
// ParkingTicket.java
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

public class ParkingTicket {
    private String ticketNumber;
    private LocalDateTime issuedAt;
    private LocalDateTime exitTime;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private TicketStatus status;
    
    public ParkingTicket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketNumber = "TKT-" + UUID.randomUUID().toString().substring(0, 8);
        this.vehicle = vehicle;
        this.spot = spot;
        this.issuedAt = LocalDateTime.now();
        this.status = TicketStatus.ACTIVE;
    }
    
    public Duration calculateDuration() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(issuedAt, end);
    }
    
    public void updateExitTime() {
        this.exitTime = LocalDateTime.now();
    }
    
    public void markAsPaid() {
        this.status = TicketStatus.PAID;
    }
    
    // Getters
    public String getTicketNumber() { return ticketNumber; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getExitTime() { return exitTime; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public TicketStatus getStatus() { return status; }
}
```

---

#### Payment Hierarchy

```java
// Payment.java
import java.time.LocalDateTime;

public abstract class Payment {
    private double amount;
    private LocalDateTime timestamp;
    private PaymentStatus status;
    private ParkingTicket ticket;
    
    public Payment(double amount, ParkingTicket ticket) {
        this.amount = amount;
        this.ticket = ticket;
        this.timestamp = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }
    
    // Template method - define payment workflow
    public abstract boolean processPayment();
    
    protected void markCompleted() {
        this.status = PaymentStatus.COMPLETED;
    }
    
    protected void markFailed() {
        this.status = PaymentStatus.FAILED;
    }
    
    // Getters
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public PaymentStatus getStatus() { return status; }
    public ParkingTicket getTicket() { return ticket; }
}

// CashPayment.java
public class CashPayment extends Payment {
    private double cashReceived;
    
    public CashPayment(double amount, ParkingTicket ticket, double cashReceived) {
        super(amount, ticket);
        this.cashReceived = cashReceived;
    }
    
    @Override
    public boolean processPayment() {
        if (cashReceived >= getAmount()) {
            markCompleted();
            return true;
        }
        markFailed();
        return false;
    }
    
    public double getChange() {
        return cashReceived - getAmount();
    }
}

// CreditCardPayment.java
public class CreditCardPayment extends Payment {
    private String cardNumber;
    private String cvv;
    
    public CreditCardPayment(double amount, ParkingTicket ticket, 
                            String cardNumber, String cvv) {
        super(amount, ticket);
        this.cardNumber = cardNumber;
        this.cvv = cvv;
    }
    
    @Override
    public boolean processPayment() {
        if (validateCard()) {
            // In production: call payment gateway API
            markCompleted();
            return true;
        }
        markFailed();
        return false;
    }
    
    private boolean validateCard() {
        return cardNumber != null && cardNumber.length() == 16 && 
               cvv != null && cvv.length() == 3;
    }
}

// DigitalWalletPayment.java
public class DigitalWalletPayment extends Payment {
    private String walletId;
    private String transactionId;
    
    public DigitalWalletPayment(double amount, ParkingTicket ticket, String walletId) {
        super(amount, ticket);
        this.walletId = walletId;
    }
    
    @Override
    public boolean processPayment() {
        // In production: call wallet API
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
        markCompleted();
        return true;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
}
```

---

#### DisplayBoard

```java
// DisplayBoard.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayBoard {
    private String id;
    private int floorNumber;
    private Map<SpotType, Integer> availableSpots;
    
    public DisplayBoard(String id, int floorNumber) {
        this.id = id;
        this.floorNumber = floorNumber;
        this.availableSpots = new ConcurrentHashMap<>();
        
        // Initialize all spot types to 0
        for (SpotType type : SpotType.values()) {
            availableSpots.put(type, 0);
        }
    }
    
    public void updateAvailability(SpotType type, int count) {
        availableSpots.put(type, count);
    }
    
    public void showAvailability() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║    Floor " + floorNumber + " - Available Spots    ║");
        System.out.println("╠════════════════════════════════════╣");
        for (Map.Entry<SpotType, Integer> entry : availableSpots.entrySet()) {
            System.out.printf("║ %-20s : %4d     ║%n", 
                            entry.getKey(), entry.getValue());
        }
        System.out.println("╚════════════════════════════════════╝\n");
    }
    
    public int getAvailableSpots(SpotType type) {
        return availableSpots.getOrDefault(type, 0);
    }
}
```

---

#### ParkingFloor

```java
// ParkingFloor.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingFloor {
    private int floorNumber;
    private Map<String, ParkingSpot> spots;
    private DisplayBoard displayBoard;
    
    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spots = new ConcurrentHashMap<>();
        this.displayBoard = new DisplayBoard("DB-F" + floorNumber, floorNumber);
    }
    
    public void addSpot(ParkingSpot spot) {
        spots.put(spot.getId(), spot);
        updateDisplayBoard();
    }
    
    public void removeSpot(String spotId) {
        spots.remove(spotId);
        updateDisplayBoard();
    }
    
    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        // Priority: Try to find exact match first
        for (ParkingSpot spot : spots.values()) {
            if (spot.isFree() && spot.canFitVehicle(vehicle)) {
                return spot;
            }
        }
        return null;
    }
    
    public void updateDisplayBoard() {
        // Count available spots by type
        Map<SpotType, Integer> counts = new EnumMap<>(SpotType.class);
        for (SpotType type : SpotType.values()) {
            counts.put(type, 0);
        }
        
        for (ParkingSpot spot : spots.values()) {
            if (spot.isFree()) {
                counts.merge(spot.getType(), 1, Integer::sum);
            }
        }
        
        // Update display board
        for (Map.Entry<SpotType, Integer> entry : counts.entrySet()) {
            displayBoard.updateAvailability(entry.getKey(), entry.getValue());
        }
    }
    
    // Getters
    public int getFloorNumber() { return floorNumber; }
    public Map<String, ParkingSpot> getSpots() { return spots; }
    public DisplayBoard getDisplayBoard() { return displayBoard; }
}
```

---

#### EntryPanel & ExitPanel

```java
// EntryPanel.java
public class EntryPanel {
    private String id;
    
    public EntryPanel(String id) {
        this.id = id;
    }
    
    public ParkingTicket printTicket(Vehicle vehicle, ParkingSpot spot) {
        ParkingTicket ticket = new ParkingTicket(vehicle, spot);
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║      PARKING TICKET              ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║ Ticket: " + ticket.getTicketNumber() + "      ║");
        System.out.println("║ Spot: " + spot.getId() + "                ║");
        System.out.println("║ Time: " + ticket.getIssuedAt().toLocalTime() + "       ║");
        System.out.println("╚══════════════════════════════════╝\n");
        return ticket;
    }
    
    public String getId() {
        return id;
    }
}

// ExitPanel.java
public class ExitPanel {
    private String id;
    
    public ExitPanel(String id) {
        this.id = id;
    }
    
    public boolean processPayment(Payment payment) {
        return payment.processPayment();
    }
    
    public void printReceipt(Payment payment) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║       PARKING RECEIPT            ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.printf("║ Amount Paid: $%-18.2f║%n", payment.getAmount());
        System.out.println("║ Status: " + payment.getStatus() + "             ║");
        System.out.println("║ Time: " + payment.getTimestamp().toLocalTime() + "          ║");
        System.out.println("╚══════════════════════════════════╝\n");
    }
    
    public String getId() {
        return id;
    }
}
```

---

#### ParkingLot (Singleton - Core System)

```java
// ParkingLot.java
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingLot {
    // Singleton instance
    private static ParkingLot instance;
    private static final Object lock = new Object();
    
    // Parking lot properties
    private String name;
    private String address;
    private List<ParkingFloor> floors;
    private List<EntryPanel> entryPanels;
    private List<ExitPanel> exitPanels;
    private Map<String, ParkingTicket> activeTickets;
    
    // Pricing configuration (per hour)
    private static final Map<VehicleType, Double> HOURLY_RATES = Map.of(
        VehicleType.MOTORCYCLE, 10.0,
        VehicleType.CAR, 20.0,
        VehicleType.VAN, 25.0,
        VehicleType.TRUCK, 30.0
    );
    
    // Private constructor for Singleton
    private ParkingLot() {
        this.floors = new ArrayList<>();
        this.entryPanels = new ArrayList<>();
        this.exitPanels = new ArrayList<>();
        this.activeTickets = new ConcurrentHashMap<>();
    }
    
    // Double-checked locking for thread-safe Singleton
    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ParkingLot();
                }
            }
        }
        return instance;
    }
    
    public void initialize(String name, String address) {
        this.name = name;
        this.address = address;
    }
    
    // Configuration methods
    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }
    
    public void addEntryPanel(EntryPanel panel) {
        entryPanels.add(panel);
    }
    
    public void addExitPanel(ExitPanel panel) {
        exitPanels.add(panel);
    }
    
    // Core business logic: Park Vehicle
    public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
        // Step 1: Find available spot
        ParkingSpot spot = findAvailableSpot(vehicle);
        
        if (spot == null) {
            System.out.println("❌ No available spot for " + vehicle.getType() + 
                             " (" + vehicle.getLicenseNumber() + ")");
            return null;
        }
        
        // Step 2: Assign vehicle to spot
        if (!spot.assignVehicle(vehicle)) {
            System.out.println("❌ Failed to assign spot");
            return null;
        }
        
        // Step 3: Generate ticket
        ParkingTicket ticket = new ParkingTicket(vehicle, spot);
        activeTickets.put(ticket.getTicketNumber(), ticket);
        
        // Step 4: Update display boards
        updateAllDisplayBoards();
        
        System.out.println("✅ Vehicle parked successfully");
        System.out.println("   License: " + vehicle.getLicenseNumber());
        System.out.println("   Spot: " + spot.getId() + " (Floor " + spot.getFloor() + ")");
        
        return ticket;
    }
    
    // Core business logic: Unpark Vehicle
    public synchronized Payment unparkVehicle(ParkingTicket ticket, Payment payment) {
        // Step 1: Validate ticket
        if (ticket == null || !activeTickets.containsKey(ticket.getTicketNumber())) {
            System.out.println("❌ Invalid or expired ticket");
            return null;
        }
        
        // Step 2: Calculate fee
        double fee = calculateFee(ticket);
        System.out.println("💰 Parking Fee: $" + String.format("%.2f", fee));
        
        // Step 3: Verify payment amount
        if (payment.getAmount() < fee) {
            System.out.println("❌ Insufficient payment. Required: $" + fee);
            return null;
        }
        
        // Step 4: Process payment
        if (!payment.processPayment()) {
            System.out.println("❌ Payment processing failed");
            return null;
        }
        
        // Step 5: Free the spot
        ticket.updateExitTime();
        ticket.getSpot().removeVehicle();
        ticket.markAsPaid();
        
        // Step 6: Remove from active tickets
        activeTickets.remove(ticket.getTicketNumber());
        
        // Step 7: Update display boards
        updateAllDisplayBoards();
        
        System.out.println("✅ Vehicle exited successfully");
        
        return payment;
    }
    
    // Fee calculation logic
    public double calculateFee(ParkingTicket ticket) {
        Duration duration = ticket.calculateDuration();
        long hours = duration.toHours();
        
        // Minimum 1 hour charge
        if (hours == 0) {
            hours = 1;
        }
        
        VehicleType type = ticket.getVehicle().getType();
        double hourlyRate = HOURLY_RATES.getOrDefault(type, 20.0);
        
        return hours * hourlyRate;
    }
    
    // Helper: Find available spot across all floors
    private ParkingSpot findAvailableSpot(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }
    
    // Helper: Update all display boards
    private void updateAllDisplayBoards() {
        for (ParkingFloor floor : floors) {
            floor.updateDisplayBoard();
        }
    }
    
    // Status check
    public boolean isFull() {
        for (ParkingFloor floor : floors) {
            if (floor.findAvailableSpot(new Car("DUMMY")) != null) {
                return false;
            }
        }
        return true;
    }
    
    public void showStatus() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  " + name + " - SYSTEM STATUS");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Total Floors: " + floors.size());
        System.out.println("║ Active Tickets: " + activeTickets.size());
        System.out.println("╚════════════════════════════════════════╝");
        
        for (ParkingFloor floor : floors) {
            floor.getDisplayBoard().showAvailability();
        }
    }
    
    // Getters
    public String getName() { return name; }
    public String getAddress() { return address; }
}
```

---

#### Demo Application

```java
// ParkingLotDemo.java
public class ParkingLotDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("   PARKING LOT SYSTEM DEMONSTRATION   ");
        System.out.println("═══════════════════════════════════════\n");
        
        // Initialize parking lot
        ParkingLot parkingLot = ParkingLot.getInstance();
        parkingLot.initialize("Downtown Parking Plaza", "123 Main Street");
        
        // Setup Floor 1
        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSpot(new CompactSpot("C1-01", 1));
        floor1.addSpot(new CompactSpot("C1-02", 1));
        floor1.addSpot(new CompactSpot("C1-03", 1));
        floor1.addSpot(new LargeSpot("L1-01", 1));
        floor1.addSpot(new LargeSpot("L1-02", 1));
        floor1.addSpot(new MotorcycleSpot("M1-01", 1));
        floor1.addSpot(new MotorcycleSpot("M1-02", 1));
        floor1.addSpot(new DisabledSpot("D1-01", 1));
        
        // Setup Floor 2
        ParkingFloor floor2 = new ParkingFloor(2);
        floor2.addSpot(new CompactSpot("C2-01", 2));
        floor2.addSpot(new CompactSpot("C2-02", 2));
        floor2.addSpot(new LargeSpot("L2-01", 2));
        floor2.addSpot(new MotorcycleSpot("M2-01", 2));
        
        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);
        
        // Add entry/exit panels
        parkingLot.addEntryPanel(new EntryPanel("ENTRY-01"));
        parkingLot.addEntryPanel(new EntryPanel("ENTRY-02"));
        parkingLot.addExitPanel(new ExitPanel("EXIT-01"));
        
        // Show initial status
        parkingLot.showStatus();
        
        System.out.println("\n═══ VEHICLE ENTRY SIMULATION ═══\n");
        
        // Vehicle 1: Car
        Vehicle car1 = new Car("ABC-1234");
        ParkingTicket ticket1 = parkingLot.parkVehicle(car1);
        
        // Vehicle 2: Motorcycle
        Vehicle bike1 = new Motorcycle("XYZ-5678");
        ParkingTicket ticket2 = parkingLot.parkVehicle(bike1);
        
        // Vehicle 3: Truck
        Vehicle truck1 = new Truck("TRK-9012");
        ParkingTicket ticket3 = parkingLot.parkVehicle(truck1);
        
        // Show updated status
        parkingLot.showStatus();
        
        // Simulate parking duration
        System.out.println("⏰ Simulating 2 hours of parking...\n");
        try {
            Thread.sleep(2000); // 2 seconds = 2 hours in demo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n═══ VEHICLE EXIT SIMULATION ═══\n");
        
        // Exit car with credit card payment
        if (ticket1 != null) {
            double fee1 = parkingLot.calculateFee(ticket1);
            Payment payment1 = new CreditCardPayment(fee1, ticket1, 
                                                    "4532-1234-5678-9010", "123");
            parkingLot.unparkVehicle(ticket1, payment1);
        }
        
        // Exit motorcycle with cash payment
        if (ticket2 != null) {
            double fee2 = parkingLot.calculateFee(ticket2);
            Payment payment2 = new CashPayment(fee2, ticket2, 50.0);
            parkingLot.unparkVehicle(ticket2, payment2);
            if (payment2 instanceof CashPayment) {
                System.out.println("💵 Change: $" + ((CashPayment)payment2).getChange());
            }
        }
        
        // Show final status
        parkingLot.showStatus();
        
        System.out.println("═══════════════════════════════════════");
        System.out.println("         DEMO COMPLETED                ");
        System.out.println("═══════════════════════════════════════");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Singleton Pattern
**Where:** ParkingLot class  
**Why:** Only one parking lot instance should manage the entire system  
**Interview Answer:** "I'm using Singleton because we need a single point of control for all parking operations. Multiple instances would lead to inconsistent state."

### Pattern 2: Strategy Pattern
**Where:** Payment hierarchy  
**Why:** Different payment processing algorithms  
**Interview Answer:** "Each payment method has different processing logic. Strategy pattern lets us add new payment methods without modifying existing code (Open-Closed Principle)."

### Pattern 3: Factory Pattern (Optional)
**Where:** Vehicle/Spot creation  
**Why:** Centralize object creation  
**Interview Answer:** "If creation logic becomes complex (e.g., spot selection based on multiple criteria), we can introduce a factory."

---

## 🔒 Step 8: Handle Concurrency (5 minutes)

### Critical Sections Identified:

#### 1. Spot Assignment
**Problem:** Two vehicles trying to take same spot simultaneously

**Solution:**
```java
public synchronized boolean assignVehicle(Vehicle vehicle) {
    if (!isFree) {
        return false;
    }
    this.vehicle = vehicle;
    this.isFree = false;
    return true;
}
```

**Interview Explanation:** "The `assignVehicle()` method is synchronized to ensure atomic check-and-set. This prevents race conditions where two threads check `isFree` simultaneously."

#### 2. Active Tickets Map
**Problem:** Concurrent additions/removals

**Solution:**
```java
private Map<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();
```

**Interview Explanation:** "Using ConcurrentHashMap instead of HashMap because multiple threads will be adding/removing tickets. It provides thread-safe operations without synchronizing the entire map."

#### 3. Display Board Updates
**Problem:** Stale data on display boards

**Solution:**
```java
private void updateAllDisplayBoards() {
    for (ParkingFloor floor : floors) {
        floor.updateDisplayBoard(); // Recalculates from current state
    }
}
```

**Interview Explanation:** "Display boards recalculate from actual spot states rather than incrementing/decrementing counters. This prevents drift from missed updates."

---

## 📊 Step 9: Database Schema (Optional - If Time Permits)

```sql
-- Vehicles table
CREATE TABLE vehicles (
    license_number VARCHAR(20) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    INDEX idx_entry_time (entry_time)
);

-- Parking spots table
CREATE TABLE parking_spots (
    spot_id VARCHAR(10) PRIMARY KEY,
    floor_number INT NOT NULL,
    spot_type VARCHAR(20) NOT NULL,
    is_free BOOLEAN DEFAULT TRUE,
    current_vehicle VARCHAR(20),
    FOREIGN KEY (current_vehicle) REFERENCES vehicles(license_number),
    INDEX idx_floor_free (floor_number, is_free)
);

-- Parking tickets table
CREATE TABLE parking_tickets (
    ticket_number VARCHAR(50) PRIMARY KEY,
    vehicle_license VARCHAR(20) NOT NULL,
    spot_id VARCHAR(10) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (vehicle_license) REFERENCES vehicles(license_number),
    FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id),
    INDEX idx_status (status),
    INDEX idx_issued_at (issued_at)
);

-- Payments table
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20),
    status VARCHAR(20),
    timestamp TIMESTAMP,
    FOREIGN KEY (ticket_number) REFERENCES parking_tickets(ticket_number)
);
```

---

## 💡 Step 10: Interview Discussion Points

### Question 1: "How would you handle lost tickets?"

**Answer:**
```java
public class LostTicketHandler {
    private static final double LOST_TICKET_PENALTY = 100.0;
    
    public Payment handleLostTicket(Vehicle vehicle) {
        // 1. Search for vehicle in parking records
        ParkingTicket ticket = findTicketByLicense(vehicle.getLicenseNumber());
        
        if (ticket == null) {
            // Charge maximum penalty (e.g., 24 hours)
            return createPenaltyPayment(vehicle, LOST_TICKET_PENALTY);
        }
        
        // Charge calculated fee + penalty
        double fee = calculateFee(ticket);
        return createPenaltyPayment(vehicle, fee + LOST_TICKET_PENALTY);
    }
}
```

### Question 2: "How to scale to multiple parking lots?"

**Answer:**
"Currently using Singleton which limits to one parking lot. To support multiple:
1. Remove Singleton pattern
2. Create ParkingLotRegistry to manage multiple lots
3. Add `parkingLotId` to all entities
4. Shard database by `parkingLotId`"

```java
public class ParkingLotRegistry {
    private Map<String, ParkingLot> parkingLots;
    
    public ParkingLot getParkingLot(String id) {
        return parkingLots.get(id);
    }
}
```

### Question 3: "How to optimize spot finding for large parking lots?"

**Answer:**
"Current O(n) scan through all spots. Optimizations:
1. **Index by type:** Maintain separate lists for each spot type
2. **Bitmap:** Use bitmap to track free/occupied (fast bit operations)
3. **Cache:** Cache first available spot per type"

```java
public class ParkingFloor {
    private Map<SpotType, Queue<ParkingSpot>> availableSpotsByType;
    
    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        SpotType preferredType = getPreferredSpotType(vehicle);
        Queue<ParkingSpot> queue = availableSpotsByType.get(preferredType);
        return queue.peek(); // O(1) instead of O(n)
    }
}
```

### Question 4: "How to handle reservations?"

**Answer:**
```java
public class Reservation {
    private String reservationId;
    private Vehicle vehicle;
    private SpotType spotType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
}

// In ParkingSpot:
private Reservation reservation;

public boolean isReserved() {
    return reservation != null && reservation.isValid();
}
```

### Question 5: "How to implement dynamic pricing?"

**Answer:**
```java
public interface PricingStrategy {
    double calculatePrice(Duration duration, VehicleType type);
}

public class PeakHourPricing implements PricingStrategy {
    @Override
    public double calculatePrice(Duration duration, VehicleType type) {
        double baseRate = getBaseRate(type);
        int hour = LocalTime.now().getHour();
        
        // Peak hours: 8-10 AM, 5-7 PM
        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 19)) {
            return baseRate * 1.5 * duration.toHours(); // 50% surge
        }
        
        return baseRate * duration.toHours();
    }
}

// In ParkingLot:
private PricingStrategy pricingStrategy;

public double calculateFee(ParkingTicket ticket) {
    return pricingStrategy.calculatePrice(
        ticket.calculateDuration(),
        ticket.getVehicle().getType()
    );
}
```

---

## ✅ Step 11: SOLID Principles Check

### Single Responsibility Principle (SRP)
- ✅ **ParkingSpot:** Only manages spot state
- ✅ **Payment:** Only handles payment processing
- ✅ **ParkingLot:** Coordinates but delegates to specific classes

### Open-Closed Principle (OCP)
- ✅ Can add new vehicle types without modifying ParkingSpot
- ✅ Can add new payment methods without modifying ParkingLot
- ✅ Use abstract classes for extension

### Liskov Substitution Principle (LSP)
- ✅ Any `Vehicle` subtype can be used wherever `Vehicle` is expected
- ✅ Any `Payment` subtype can process payment correctly

### Interface Segregation Principle (ISP)
- ✅ No fat interfaces with unused methods
- ✅ Could add if needed: `Printable`, `Searchable`

### Dependency Inversion Principle (DIP)
- ✅ ParkingLot depends on abstraction (`Vehicle`, `Payment`)
- ✅ Not on concrete types (`Car`, `CashPayment`)

---

## 🎯 Interview Tips & Talking Points

### What to Say in Interview:

1. **When starting:**
   "Let me first clarify requirements and understand the scope..."

2. **During entity identification:**
   "I'm extracting nouns from requirements and checking if they need state and behavior..."

3. **During relationship design:**
   "ParkingLot owns ParkingFloor (composition) because floors can't exist independently..."

4. **When discussing concurrency:**
   "Since multiple vehicles can enter simultaneously, I'm using synchronized methods and ConcurrentHashMap..."

5. **When discussing patterns:**
   "I'm using Singleton for ParkingLot because we need a single point of control..."

6. **When asked about scale:**
   "For 100,000 spots, I'd add indexes by spot type, use distributed caching, and potentially shard by floor..."

### Common Interview Follow-ups:

✅ Lost tickets → Penalty system  
✅ Reservations → Add Reservation entity  
✅ Multiple parking lots → Remove Singleton  
✅ Dynamic pricing → Strategy pattern  
✅ VIP parking → Add priority queue  
✅ Electric charging → Add ChargingSpot subclass

---

## 📈 Complexity Analysis

| Operation | Time Complexity | Space Complexity | Notes |
|-----------|----------------|------------------|-------|
| Park Vehicle | O(F × S) | O(1) | F=floors, S=spots/floor |
| Unpark Vehicle | O(1) | O(1) | Hash map lookup |
| Calculate Fee | O(1) | O(1) | Direct calculation |
| Update Display | O(S) | O(1) | S=spots on floor |

**With Optimization (indexed by type):**
| Operation | Optimized Time |
|-----------|---------------|
| Park Vehicle | O(F) | Check first available per floor |

---

## 🎓 Key Takeaways

1. **Systematic Approach:** Requirements → Entities → Relationships → Implementation
2. **NFR Deduction:** Use SCAMPS framework, not random guessing
3. **Entity Identification:** Noun extraction + CRUD analysis
4. **Relationship Mapping:** Three-pass analysis (Inheritance → Ownership → Cardinality)
5. **Design Patterns:** Apply where they solve specific problems, not everywhere
6. **Concurrency:** Identify critical sections and protect them
7. **SOLID Principles:** Validate design against all five principles

**Interview Success Formula:** Clarify → Structure → Design → Implement → Discuss Trade-offs
