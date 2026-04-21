# Low-Level Design: Vending Machine System

**Difficulty:** Medium ⚡

**Interview Duration:** 45-60 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a vending machine that dispenses products when users insert money and make selections."*

### Clarifying Questions to Ask:

1. **Q:** What types of products should the vending machine sell?  
   **A:** Snacks and beverages with different prices (e.g., chips $1.50, soda $2.00, candy $1.00).

2. **Q:** What payment methods should be supported?  
   **A:** Coins and notes. For this design, focus on cash. Can discuss card payment as extension.

3. **Q:** What coin/note denominations?  
   **A:** Coins: 1¢, 5¢, 10¢, 25¢. Notes: $1, $5, $10, $20.

4. **Q:** How should change be returned?  
   **A:** Use available coins in the machine. If exact change not available, refund full amount.

5. **Q:** What happens if product is out of stock?  
   **A:** Display "Out of Stock" message and allow user to select another item or cancel.

6. **Q:** Should the machine handle inventory management?  
   **A:** Yes, track quantity for each product. Admin can restock.

7. **Q:** What about power failures or errors during transaction?  
   **A:** Return all inserted money if transaction can't complete.

8. **Q:** Should it support multiple selections in one transaction?  
   **A:** Start with single item per transaction. Can discuss multi-item as extension.

9. **Q:** How many product slots?  
   **A:** Typically 20-50 slots, each slot holds one product type with quantity.

10. **Q:** Any user interface requirements?  
    **A:** Display screen showing product selection, price, amount inserted, and messages.

---

## 🔹 Step 2: Gather Requirements (5 minutes)

### Functional Requirements

Listed point-wise as interviewer presents them:

1. Users should be able to view all available products with prices
2. Users should be able to select a product by entering its code (e.g., A1, B2)
3. Users should be able to insert coins and notes one at a time
4. System should track total amount inserted and display it
5. System should validate if inserted amount is sufficient for selected product
6. If amount is sufficient, system should dispense the product
7. System should return change if amount exceeds product price
8. Change should be returned using optimal coin/note combination
9. If exact change cannot be given, system should refund full amount
10. Users should be able to cancel transaction at any time and get full refund
11. System should check product availability before accepting money
12. System should update inventory after successful purchase
13. System should handle "out of stock" scenario gracefully
14. System should handle "insufficient change" scenario
15. Admin should be able to add/remove products
16. Admin should be able to restock products
17. Admin should be able to add coins/notes to machine
18. Admin should be able to collect money from machine
19. System should maintain accurate inventory count
20. System should maintain accurate cash balance

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "What's the operational scale?"
- Single physical machine (not distributed)
- 20-50 product slots
- Hundreds of transactions per day
- Limited coin/note storage capacity

**Deduced NFRs:**
- ✅ System must handle concurrent state (one user at a time)
- ✅ No distributed system needed (single machine)
- ✅ Storage capacity limits must be enforced
- ✅ Transaction throughput: handle 1 transaction every 30-60 seconds

**Why these matter:** Physical constraints of vending machine hardware.

---

#### 2. **Consistency Analysis**

**Think:** "What data must always be accurate?"
- Inventory counts (can't sell what doesn't exist)
- Cash balance (can't lose money)
- Transaction state (money inserted vs product dispensed)
- Change calculation (must be exact)

**Deduced NFRs:**
- ✅ Strong consistency for inventory (ACID properties)
- ✅ Atomic transactions (all-or-nothing: money collected + product dispensed + change returned)
- ✅ Accurate cash accounting (every cent tracked)
- ✅ No partial states (either complete transaction or rollback)

**Critical:** If product dispenses but change fails → must alert for manual intervention

---

#### 3. **Availability Analysis**

**Think:** "What's acceptable downtime?"
- Machine operates 24/7
- Downtime = lost revenue
- Maintenance windows possible (restocking, cash collection)

**Deduced NFRs:**
- ✅ 99% availability (< 3.5 days downtime/year acceptable)
- ✅ Graceful degradation (if change shortage, still allow exact-amount purchases)
- ✅ Clear error messages to reduce support calls
- ✅ Remote monitoring (know when machine needs attention)

**Why not 99.9%?** Physical machine, periodic maintenance needed.

---

#### 4. **Performance Analysis**

**Think:** "What operations are time-critical?"
- **Product selection:** Should be instant (< 100ms)
- **Money validation:** Must be quick (< 200ms per coin/note)
- **Change calculation:** Fast (< 500ms)
- **Dispensing:** Physical mechanism (3-5 seconds acceptable)

**Deduced NFRs:**
- ✅ Product lookup < 100ms (in-memory data structure)
- ✅ Change calculation < 500ms (greedy algorithm sufficient)
- ✅ Transaction timeout: 60 seconds (return money if no activity)
- ✅ Total transaction time: < 30 seconds

---

#### 5. **Concurrency Analysis**

**Think:** "Can multiple things happen at once?"
- Only ONE user can use machine at a time
- But: Dispensing + change return happens simultaneously
- Admin operations should NOT interrupt active transaction

**Deduced NFRs:**
- ✅ State machine ensures only one transaction active
- ✅ Admin operations blocked during transaction
- ✅ Thread-safe inventory and cash updates
- ✅ Mutex lock on critical operations

**No distributed concurrency** - single machine, single user.

---

#### 6. **Reliability Analysis**

**Think:** "What can go wrong?"
- Power failure mid-transaction
- Coin acceptor jam
- Product dispenser jam
- Sensor failure

**Deduced NFRs:**
- ✅ Transaction logging (persist state before/after critical operations)
- ✅ Recovery mechanism (restore state after power loss)
- ✅ Fault detection (sensors for coin return, product dispensing)
- ✅ Fail-safe: default to returning money if uncertain state

---

**NFR Summary Table:**

| Dimension | Requirement | Deduction Method | Impact |
|-----------|-------------|------------------|--------|
| Scalability | Single machine, 50 slots | Physical constraints | In-memory storage sufficient |
| Consistency | Exact inventory/cash | Business criticality | Atomic transactions, logging |
| Availability | 99% uptime | Revenue impact | Error handling, monitoring |
| Performance | Transaction < 30s | User expectation | Efficient algorithms |
| Concurrency | One user at a time | Physical limitation | State machine, mutex |
| Reliability | Handle failures | Money involved | Logging, recovery, fail-safe |

---

## 🧩 Step 3: Identify Core Entities (10 minutes)

### Systematic Entity Identification Method

#### Step 1: Noun Extraction from Requirements

| Requirement | Nouns Identified |
|-------------|------------------|
| "Users view available products with prices" | User, Product, Price |
| "Users select product by code" | Product Code, Selection |
| "Users insert coins and notes" | Coin, Note, Money |
| "Track total amount inserted" | Amount, Inserted Money |
| "Dispense the product" | Dispenser, Inventory |
| "Return change using optimal combination" | Change, Combination |
| "Cancel transaction and get refund" | Transaction, Refund |
| "Check product availability" | Availability, Inventory |
| "Update inventory after purchase" | Inventory |
| "Admin can restock products" | Admin, Restock |
| "Maintain accurate cash balance" | Cash Balance |

#### Step 2: Entity Validation

**Criteria:**
1. Has multiple attributes?
2. Has behavior (methods)?
3. Has lifecycle (CRUD)?
4. Appears in multiple requirements?
5. Has relationships?

| Noun | Attributes? | Behavior? | Lifecycle? | Relationships? | Entity? |
|------|-------------|-----------|------------|----------------|---------|
| Product | ✅ name, price, code | ✅ reduce quantity | ✅ add/remove | ✅ in inventory | ✅ YES |
| Coin/Note | ✅ value, type | ✅ validate | ✅ add/use | ✅ in cash holder | ✅ YES (Money) |
| Inventory | ✅ products, quantities | ✅ check, update | ✅ manage | ✅ has products | ✅ YES |
| Transaction | ✅ state, amount, product | ✅ process | ✅ create/complete | ✅ with product | ✅ YES |
| Change | ⚠️ just a calculation | ⚠️ calculate result | ❌ no state | ❌ none | ❌ NO (method) |
| User | ⚠️ anonymous | ⚠️ no stored data | ❌ no persistence | ❌ none | ❌ NO (not tracked) |
| Admin | ⚠️ same as User | ⚠️ privileged operations | ⚠️ no unique state | ⚠️ none | ❌ NO (role/interface) |
| CashHolder | ✅ coin/note counts | ✅ add, use, check | ✅ manage | ✅ has money | ✅ YES |
| VendingMachine | ✅ inventory, cash, state | ✅ orchestrate | ✅ initialize | ✅ has all | ✅ YES |
| Dispenser | ✅ mechanism | ✅ dispense | ⚠️ hardware | ⚠️ just interface | ⚠️ MAYBE (interface) |

#### Step 3: Refinement and Grouping

**Group 1: Core Business Entities**
- Product (individual items)
- Inventory (collection of products)

**Group 2: Money Management**
- Money (abstract: Coin + Note)
- CashHolder (manages money in machine)

**Group 3: Transaction Management**
- Transaction (state machine for purchase flow)
- VendingMachine (central orchestrator)

**Group 4: Hardware Interfaces**
- ProductDispenser (abstraction for physical dispenser)
- CoinNoteAcceptor (abstraction for money validator)

### Final Entity List (7 Core Entities + 2 Interfaces)

**Entities:**
1. **Product** - Individual sellable item
2. **Inventory** - Collection of products with quantities
3. **Money** (Abstract) + Coin + Note
4. **CashHolder** - Manages money in machine
5. **Transaction** - Represents purchase flow and state
6. **VendingMachine** - Central system (Singleton)
7. **ProductSlot** - Physical location holding products

**Interfaces:**
8. **ProductDispenser** - Hardware abstraction
9. **MoneyReceptor** - Hardware abstraction

**Why these?**
- Each has clear responsibility
- Each has state and behavior
- Each appears in multiple requirements
- Together they model complete system

---

## 🔗 Step 4: Establish Relationships (10 minutes)

### Three-Pass Methodology

---

### Pass 1: Inheritance Hierarchies (IS-A)

**Analysis:**

**Money Types:**
```
Coin IS-A Money? → Yes (has value, denomination)
Note IS-A Money? → Yes (has value, denomination)
```

**Decision:** Create Money hierarchy
```
Money (Abstract)
  ├─ Coin (Quarter, Dime, Nickel, Penny)
  └─ Note (Dollar1, Dollar5, Dollar10, Dollar20)
```

**Why abstract class vs interface?**
- Common attributes: `value`, `denomination`
- Common behavior: `getValue()`
- Clear IS-A relationship

**No other IS-A relationships:**
- Product variants don't need hierarchy (all same)
- VendingMachine has no subtypes (single design)

---

### Pass 2: Ownership Relationships (HAS-A)

**Method:** Ask three questions for each pair:

#### VendingMachine ↔ Inventory

**Q1:** Does VendingMachine contain Inventory? → **Yes**  
**Q2:** Can Inventory exist without VendingMachine? → **No**  
**Q3:** Delete VendingMachine → Delete Inventory? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
VendingMachine ◆────→ Inventory [1:1]
```

#### VendingMachine ↔ CashHolder

**Q1:** Does VendingMachine contain CashHolder? → **Yes**  
**Q2:** Can CashHolder exist without VendingMachine? → **No**  
**Q3:** Delete VendingMachine → Delete CashHolder? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
VendingMachine ◆────→ CashHolder [1:1]
```

#### Inventory ↔ ProductSlot

**Q1:** Does Inventory contain ProductSlots? → **Yes**  
**Q2:** Can ProductSlot exist without Inventory? → **No**  
**Q3:** Delete Inventory → Delete ProductSlots? → **Yes**

**Conclusion:** **Composition** (strong ownership)
```
Inventory ◆────→ ProductSlot [1:N]
```

#### ProductSlot ↔ Product

**Q1:** Does ProductSlot contain Product? → **Yes**  
**Q2:** Can Product exist without ProductSlot? → **Yes** (concept exists before stocking)  
**Q3:** Delete ProductSlot → Delete Product definition? → **No**

**Conclusion:** **Aggregation** (weak reference)
```
ProductSlot ◇────→ Product [1:1]
```

#### CashHolder ↔ Money

**Q1:** Does CashHolder contain Money? → **Yes**  
**Q2:** Can Money exist without CashHolder? → **Yes** (denominations defined independently)  
**Q3:** Delete CashHolder → Delete Money types? → **No**

**Conclusion:** **Aggregation** (weak reference)
```
CashHolder ◇────→ Money [1:N]
```

#### Transaction ↔ Product

**Q1:** Does Transaction own Product? → **No** (just references)  
**Q2:** Can Transaction exist without Product? → **No**  
**Q3:** Delete Transaction → Delete Product? → **No**

**Conclusion:** **Association** (reference only)
```
Transaction ─────→ Product [1:1]
```

---

### Pass 3: Cardinality Mapping

| Relationship | From | To | Cardinality | Reasoning |
|--------------|------|----|-------------|-----------|
| VendingMachine → Inventory | 1 | 1 | 1:1 | One machine has one inventory system |
| VendingMachine → CashHolder | 1 | 1 | 1:1 | One machine has one cash management system |
| VendingMachine → Transaction | 1 | 0..1 | 1:0..1 | At most one active transaction at a time |
| Inventory → ProductSlot | 1 | N | 1:N | Inventory manages multiple slots (20-50) |
| ProductSlot → Product | 1 | 1 | 1:1 | Each slot holds one product type |
| CashHolder → Money | 1 | N | 1:N | Cash holder contains multiple coins/notes |
| Transaction → Product | 1 | 1 | 1:1 | Each transaction for one product (currently) |
| Transaction → Money | 1 | N | 1:N | Transaction involves multiple inserted coins/notes |

---

### Special Design Decisions

#### Decision 1: ProductSlot vs Direct Product in Inventory

**Why introduce ProductSlot?**

**Option A:** Inventory directly maps to Products
```java
Map<String, Product> products; // code -> product
```
❌ Can't track quantity per location  
❌ Can't model physical slot constraints  

**Option B:** Introduce ProductSlot
```java
Map<String, ProductSlot> slots; // code -> slot
```
✅ Encapsulates quantity management  
✅ Models physical reality (each slot has capacity)  
✅ Separates product definition from storage  

**Choice:** **Option B** - Use ProductSlot

#### Decision 2: Transaction State Management

**Problem:** Transaction has multiple states (Idle, MoneyInserted, ProductSelected, etc.)

**Solution:** Use State Machine pattern (discussed in Step 7)

---

### Complete Relationship Diagram

```
┌─────────────────────────┐
│   VendingMachine        │
│     (Singleton)         │
└────────┬────────────────┘
         │
         ├─── [1:1 Composition] ──→ Inventory
         │                              │
         │                              └─── [1:N Composition] ──→ ProductSlot
         │                                                              │
         │                                                              └─── [1:1 Aggregation] ──→ Product
         │
         ├─── [1:1 Composition] ──→ CashHolder
         │                              │
         │                              └─── [1:N Aggregation] ──→ Money
         │                                                              ├─ Coin
         │                                                              └─ Note
         │
         └─── [1:0..1 Association] ──→ Transaction
                                            │
                                            ├─── [1:1 Association] ──→ Product
                                            └─── [1:N Association] ──→ Money (inserted)
```

---

## 📐 Step 5: Design Class Diagrams (10 minutes)

### Notation Guide:
- `+` = public, `-` = private, `#` = protected
- `△` = inheritance arrow
- `◆` = composition (filled diamond)
- `◇` = aggregation (empty diamond)

---

### Class Diagram 1: Money Hierarchy

```
┌────────────────────────────────────────┐
│         <<abstract>>                   │
│            Money                       │
├────────────────────────────────────────┤
│ # value: double                        │
│ # denomination: String                 │
├────────────────────────────────────────┤
│ + Money(value: double, denom: String)  │
│ + getValue(): double                   │
│ + getDenomination(): String            │
│ + equals(obj: Object): boolean         │
│ + hashCode(): int                      │
└────────────────────────────────────────┘
                    △
                    │
        ┌───────────┼──────────┐
        │                      │
┌───────┴──────┐      ┌────────┴───────┐
│     Coin     │      │      Note      │
├──────────────┤      ├────────────────┤
│              │      │                │
├──────────────┤      ├────────────────┤
│+Coin(value)  │      │+Note(value)    │
└──────────────┘      └────────────────┘

Coin Types:
- Penny (0.01)
- Nickel (0.05)
- Dime (0.10)
- Quarter (0.25)

Note Types:
- Dollar1 (1.00)
- Dollar5 (5.00)
- Dollar10 (10.00)
- Dollar20 (20.00)
```

**Design Rationale:**
- Abstract parent: Common value and denomination
- No behavior difference between Coin and Note → single hierarchy
- Could use Factory to create specific denominations

---

### Class Diagram 2: Product & ProductSlot

```
┌────────────────────────────────────────┐
│            Product                     │
├────────────────────────────────────────┤
│ - id: long                             │
│ - name: String                         │
│ - price: double                        │
├────────────────────────────────────────┤
│ + Product(id, name, price)             │
│ + getId(): long                        │
│ + getName(): String                    │
│ + getPrice(): double                   │
└────────────────────────────────────────┘
                    △
                    │ (aggregation)
                    │
┌────────────────────────────────────────┐
│          ProductSlot                   │
├────────────────────────────────────────┤
│ - code: String                         │
│ - product: Product               ◇─────┼──→ Product
│ - quantity: int                        │
│ - maxCapacity: int                     │
├────────────────────────────────────────┤
│ + ProductSlot(code, product, qty, max) │
│ + getCode(): String                    │
│ + getProduct(): Product                │
│ + getQuantity(): int                   │
│ + isAvailable(): boolean               │
│ + isFull(): boolean                    │
│ + addStock(amount: int): void          │
│ + reduceStock(): boolean               │
└────────────────────────────────────────┘
```

**Key Methods:**
- `isAvailable()`: Returns `quantity > 0`
- `reduceStock()`: Atomic decrement with validation
- `addStock()`: For restocking, respects maxCapacity

---

### Class Diagram 3: Inventory

```
┌────────────────────────────────────────────────────┐
│               Inventory                            │
├────────────────────────────────────────────────────┤
│ - slots: Map<String, ProductSlot>  ◆───────────────┼──→ ProductSlot [1..*]
├────────────────────────────────────────────────────┤
│ + Inventory()                                      │
│ + addProductSlot(slot: ProductSlot): void          │
│ + getProductSlot(code: String): ProductSlot        │
│ + isProductAvailable(code: String): boolean        │
│ + getAllSlots(): List<ProductSlot>                 │
│ + restockProduct(code: String, qty: int): boolean  │
│ + purchaseProduct(code: String): Product           │
└────────────────────────────────────────────────────┘
```

**Design Decision:** Use Map for O(1) lookup by code (e.g., "A1", "B2")

---

### Class Diagram 4: CashHolder

```
┌────────────────────────────────────────────────────────┐
│              CashHolder                                │
├────────────────────────────────────────────────────────┤
│ - cashInventory: Map<Money, Integer>  ◇────────────────┼──→ Money [1..*]
│ - totalBalance: double                                 │
├────────────────────────────────────────────────────────┤
│ + CashHolder()                                         │
│ + addMoney(money: Money, count: int): void             │
│ + acceptMoney(money: Money): void                      │
│ + canGiveChange(amount: double): boolean               │
│ + getChange(amount: double): List<Money>               │
│ + getTotalBalance(): double                            │
│ + refundMoney(insertedMoney: List<Money>): List<Money> │
│ - calculateChange(amount: double): List<Money>         │
└────────────────────────────────────────────────────────┘
```

**Key Algorithm:**
- `getChange()`: Uses greedy algorithm (largest denomination first)
- `canGiveChange()`: Pre-validates before committing transaction
- `cashInventory`: Tracks count of each denomination

**Example:**
```
Change for $0.75:
- Try $1 note: No (exceeds)
- Try Quarter: Yes (3 quarters = $0.75)
Return: [Quarter, Quarter, Quarter]
```

---

### Class Diagram 5: Transaction (State Machine)

```
┌────────────────────────────────────────────────────────┐
│             Transaction                                │
├────────────────────────────────────────────────────────┤
│ - id: String                                           │
│ - state: TransactionState                              │
│ - selectedProduct: Product                 ─────────────┼──→ Product [0..1]
│ - insertedMoney: List<Money>               ─────────────┼──→ Money [0..*]
│ - totalInserted: double                                │
│ - changeToReturn: List<Money>                          │
├────────────────────────────────────────────────────────┤
│ + Transaction(id)                                      │
│ + selectProduct(product: Product): void                │
│ + insertMoney(money: Money): void                      │
│ + getTotalInserted(): double                           │
│ + isPaymentSufficient(): boolean                       │
│ + getState(): TransactionState                         │
│ + setState(state: TransactionState): void              │
│ + getSelectedProduct(): Product                        │
│ + getInsertedMoney(): List<Money>                      │
│ + setChangeToReturn(change: List<Money>): void         │
│ + cancel(): void                                       │
└────────────────────────────────────────────────────────┘

┌────────────────────────┐
│   <<enumeration>>      │
│   TransactionState     │
├────────────────────────┤
│ IDLE                   │
│ PRODUCT_SELECTED       │
│ PAYMENT_PENDING        │
│ PAYMENT_COMPLETE       │
│ DISPENSING             │
│ RETURNING_CHANGE       │
│ COMPLETED              │
│ CANCELLED              │
│ FAILED                 │
└────────────────────────┘
```

**State Transitions:**
```
IDLE → PRODUCT_SELECTED → PAYMENT_PENDING → PAYMENT_COMPLETE 
     → DISPENSING → RETURNING_CHANGE → COMPLETED

PRODUCT_SELECTED / PAYMENT_PENDING → CANCELLED (user cancels)
Any state → FAILED (error occurs)
```

---

### Class Diagram 6: VendingMachine (Central System)

```
┌────────────────────────────────────────────────────────────┐
│          <<Singleton>>                                     │
│         VendingMachine                                     │
├────────────────────────────────────────────────────────────┤
│ - instance: VendingMachine [static]                        │
│ - inventory: Inventory                  ◆──────────────────┼──→ Inventory [1]
│ - cashHolder: CashHolder                ◆──────────────────┼──→ CashHolder [1]
│ - currentTransaction: Transaction       ────────────────────┼──→ Transaction [0..1]
│ - dispenser: ProductDispenser (interface)                  │
│ - displayScreen: DisplayScreen                             │
├────────────────────────────────────────────────────────────┤
│ - VendingMachine() [private constructor]                   │
│ + getInstance(): VendingMachine [static, synchronized]     │
│ + displayProducts(): void                                  │
│ + selectProduct(code: String): boolean                     │
│ + insertMoney(money: Money): void                          │
│ + confirmPurchase(): boolean                               │
│ + cancelTransaction(): void                                │
│ + restockProduct(code: String, qty: int): boolean          │
│ + collectCash(): double                                    │
│ - processTransaction(): boolean                            │
│ - dispenseProduct(product: Product): boolean               │
│ - returnChange(amount: double): boolean                    │
│ - refundMoney(): void                                      │
└────────────────────────────────────────────────────────────┘
```

**Why Singleton?**
- Only one physical vending machine
- Need global access point
- Prevents multiple instances with inconsistent state

---

### Class Diagram 7: Interfaces (Hardware Abstraction)

```
┌──────────────────────────┐      ┌────────────────────────┐
│   <<interface>>          │      │   <<interface>>        │
│   ProductDispenser       │      │   DisplayScreen        │
├──────────────────────────┤      ├────────────────────────┤
│ + dispense(slot: String) │      │ + showMessage(msg)     │
│   : boolean              │      │ + showProducts(list)   │
└──────────────────────────┘      │ + showAmount(amount)   │
                                  └────────────────────────┘
```

**Why interfaces?**
- Decouple logic from hardware
- Easy to mock for testing
- Can swap hardware implementations

---

### Complete System Architecture

```
                ┌─────────────────────────┐
                │   VendingMachine        │
                │     (Singleton)         │
                └────────┬────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
    ┌─────────┐    ┌──────────┐   ┌────────────┐
    │Inventory│    │CashHolder│   │Transaction │
    └────┬────┘    └─────┬────┘   └─────┬──────┘
         │               │              │
         ▼               ▼              │
    ┌──────────┐    ┌────────┐         │
    │ProductSlot│   │ Money  │         │
    └─────┬────┘    └────────┘         │
          │                            │
          ▼                            ▼
    ┌─────────┐                  ┌─────────┐
    │ Product │←─────────────────│ Product │
    └─────────┘                  └─────────┘
```

---

## 💻 Step 6: Core Implementation (20 minutes)

### Implementation Strategy:
1. Bottom-up: Start with leaf entities (Money, Product)
2. Build containers (Inventory, CashHolder)
3. Build orchestrator (VendingMachine)

---

### Enums

```java
// TransactionState.java
public enum TransactionState {
    IDLE("Ready to serve"),
    PRODUCT_SELECTED("Product selected. Please insert money."),
    PAYMENT_PENDING("Payment pending..."),
    PAYMENT_COMPLETE("Payment complete. Dispensing..."),
    DISPENSING("Dispensing product..."),
    RETURNING_CHANGE("Returning change..."),
    COMPLETED("Transaction complete. Thank you!"),
    CANCELLED("Transaction cancelled. Refunding money."),
    FAILED("Transaction failed. Refunding money.");
    
    private final String message;
    
    TransactionState(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
```

---

### Money Hierarchy

```java
// Money.java
public abstract class Money {
    protected double value;
    protected String denomination;
    
    public Money(double value, String denomination) {
        this.value = value;
        this.denomination = denomination;
    }
    
    public double getValue() {
        return value;
    }
    
    public String getDenomination() {
        return denomination;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money other = (Money) obj;
        return Double.compare(this.value, other.value) == 0;
    }
    
    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
    
    @Override
    public String toString() {
        return denomination + " ($" + String.format("%.2f", value) + ")";
    }
}

// Coin.java
public class Coin extends Money {
    public static final Coin PENNY = new Coin(0.01, "Penny");
    public static final Coin NICKEL = new Coin(0.05, "Nickel");
    public static final Coin DIME = new Coin(0.10, "Dime");
    public static final Coin QUARTER = new Coin(0.25, "Quarter");
    
    public Coin(double value, String denomination) {
        super(value, denomination);
    }
}

// Note.java
public class Note extends Money {
    public static final Note DOLLAR_1 = new Note(1.0, "One Dollar");
    public static final Note DOLLAR_5 = new Note(5.0, "Five Dollars");
    public static final Note DOLLAR_10 = new Note(10.0, "Ten Dollars");
    public static final Note DOLLAR_20 = new Note(20.0, "Twenty Dollars");
    
    public Note(double value, String denomination) {
        super(value, denomination);
    }
}
```

---

### Product & ProductSlot

```java
// Product.java
public class Product {
    private long id;
    private String name;
    private double price;
    
    public Product(long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return name + " - $" + String.format("%.2f", price);
    }
}

// ProductSlot.java
public class ProductSlot {
    private String code;
    private Product product;
    private int quantity;
    private int maxCapacity;
    
    public ProductSlot(String code, Product product, int quantity, int maxCapacity) {
        this.code = code;
        this.product = product;
        this.quantity = quantity;
        this.maxCapacity = maxCapacity;
    }
    
    public String getCode() {
        return code;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public boolean isAvailable() {
        return quantity > 0;
    }
    
    public boolean isFull() {
        return quantity >= maxCapacity;
    }
    
    // Thread-safe stock reduction
    public synchronized boolean reduceStock() {
        if (quantity > 0) {
            quantity--;
            return true;
        }
        return false;
    }
    
    // Thread-safe restocking
    public synchronized boolean addStock(int amount) {
        if (quantity + amount <= maxCapacity) {
            quantity += amount;
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        String availability = isAvailable() ? "Available" : "OUT OF STOCK";
        return String.format("[%s] %s (Qty: %d) - %s", 
                           code, product.toString(), quantity, availability);
    }
}
```

---

### Inventory

```java
// Inventory.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Inventory {
    private Map<String, ProductSlot> slots;
    
    public Inventory() {
        this.slots = new ConcurrentHashMap<>();
    }
    
    public void addProductSlot(ProductSlot slot) {
        slots.put(slot.getCode(), slot);
    }
    
    public ProductSlot getProductSlot(String code) {
        return slots.get(code);
    }
    
    public boolean isProductAvailable(String code) {
        ProductSlot slot = slots.get(code);
        return slot != null && slot.isAvailable();
    }
    
    public List<ProductSlot> getAllSlots() {
        return new ArrayList<>(slots.values());
    }
    
    public boolean restockProduct(String code, int quantity) {
        ProductSlot slot = slots.get(code);
        if (slot == null) {
            System.out.println("❌ Slot " + code + " not found");
            return false;
        }
        
        if (slot.addStock(quantity)) {
            System.out.println("✅ Restocked " + code + ": +" + quantity + 
                             " (New qty: " + slot.getQuantity() + ")");
            return true;
        } else {
            System.out.println("❌ Cannot restock " + code + ": exceeds capacity");
            return false;
        }
    }
    
    public synchronized Product purchaseProduct(String code) {
        ProductSlot slot = slots.get(code);
        
        if (slot == null) {
            System.out.println("❌ Invalid product code: " + code);
            return null;
        }
        
        if (!slot.isAvailable()) {
            System.out.println("❌ Product out of stock: " + code);
            return null;
        }
        
        if (slot.reduceStock()) {
            System.out.println("✅ Dispensing: " + slot.getProduct().getName() + 
                             " (Remaining: " + slot.getQuantity() + ")");
            return slot.getProduct();
        }
        
        return null;
    }
}
```

---

### CashHolder

```java
// CashHolder.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CashHolder {
    private Map<Money, Integer> cashInventory;
    private double totalBalance;
    
    // Available denominations in descending order (for greedy change algorithm)
    private static final List<Money> DENOMINATIONS = Arrays.asList(
        Note.DOLLAR_20, Note.DOLLAR_10, Note.DOLLAR_5, Note.DOLLAR_1,
        Coin.QUARTER, Coin.DIME, Coin.NICKEL, Coin.PENNY
    );
    
    public CashHolder() {
        this.cashInventory = new ConcurrentHashMap<>();
        this.totalBalance = 0.0;
        
        // Initialize with some change
        for (Money money : DENOMINATIONS) {
            cashInventory.put(money, 0);
        }
    }
    
    // Admin adds money to machine
    public synchronized void addMoney(Money money, int count) {
        cashInventory.merge(money, count, Integer::sum);
        totalBalance += money.getValue() * count;
        System.out.println("✅ Added " + count + "x " + money.getDenomination() + 
                         " to machine. Total: $" + String.format("%.2f", totalBalance));
    }
    
    // User inserts money
    public synchronized void acceptMoney(Money money) {
        cashInventory.merge(money, 1, Integer::sum);
        totalBalance += money.getValue();
    }
    
    // Check if machine can give exact change
    public synchronized boolean canGiveChange(double amount) {
        if (amount < 0.01) {
            return true; // No change needed
        }
        
        List<Money> change = calculateChange(amount);
        return change != null;
    }
    
    // Return change to user (commits the change deduction)
    public synchronized List<Money> getChange(double amount) {
        if (amount < 0.01) {
            return new ArrayList<>(); // No change
        }
        
        List<Money> change = calculateChange(amount);
        
        if (change == null) {
            System.out.println("❌ Cannot provide exact change");
            return null;
        }
        
        // Deduct change from inventory
        for (Money money : change) {
            cashInventory.merge(money, -1, Integer::sum);
            totalBalance -= money.getValue();
        }
        
        System.out.println("✅ Returning change: $" + String.format("%.2f", amount));
        for (Money money : change) {
            System.out.println("   - " + money.getDenomination());
        }
        
        return change;
    }
    
    // Greedy algorithm to calculate change
    private List<Money> calculateChange(double amount) {
        List<Money> result = new ArrayList<>();
        double remaining = Math.round(amount * 100) / 100.0; // Avoid floating point errors
        
        // Create a working copy of inventory
        Map<Money, Integer> workingInventory = new HashMap<>(cashInventory);
        
        for (Money money : DENOMINATIONS) {
            int available = workingInventory.getOrDefault(money, 0);
            
            while (remaining >= money.getValue() - 0.001 && available > 0) {
                result.add(money);
                remaining = Math.round((remaining - money.getValue()) * 100) / 100.0;
                available--;
            }
        }
        
        // Check if exact change was achieved
        if (remaining > 0.001) {
            return null; // Cannot give exact change
        }
        
        return result;
    }
    
    // Refund inserted money (give back what user inserted)
    public List<Money> refundMoney(List<Money> insertedMoney) {
        System.out.println("🔄 Refunding inserted money...");
        
        // Deduct from inventory
        for (Money money : insertedMoney) {
            cashInventory.merge(money, -1, Integer::sum);
            totalBalance -= money.getValue();
        }
        
        return new ArrayList<>(insertedMoney);
    }
    
    public double getTotalBalance() {
        return totalBalance;
    }
    
    // Admin collects money from machine
    public synchronized double collectCash(double amount) {
        if (amount > totalBalance) {
            System.out.println("❌ Cannot collect more than available balance");
            return 0;
        }
        
        totalBalance -= amount;
        System.out.println("💰 Collected $" + String.format("%.2f", amount) + 
                         " from machine. Remaining: $" + 
                         String.format("%.2f", totalBalance));
        return amount;
    }
}
```

---

### Transaction

```java
// Transaction.java
import java.util.*;

public class Transaction {
    private String id;
    private TransactionState state;
    private Product selectedProduct;
    private List<Money> insertedMoney;
    private double totalInserted;
    private List<Money> changeToReturn;
    
    public Transaction(String id) {
        this.id = id;
        this.state = TransactionState.IDLE;
        this.insertedMoney = new ArrayList<>();
        this.totalInserted = 0.0;
    }
    
    public void selectProduct(Product product) {
        this.selectedProduct = product;
        this.state = TransactionState.PRODUCT_SELECTED;
        System.out.println("📦 Selected: " + product.getName() + 
                         " - Price: $" + String.format("%.2f", product.getPrice()));
    }
    
    public void insertMoney(Money money) {
        insertedMoney.add(money);
        totalInserted += money.getValue();
        this.state = TransactionState.PAYMENT_PENDING;
        System.out.println("💵 Inserted: " + money.getDenomination() + 
                         " | Total: $" + String.format("%.2f", totalInserted));
    }
    
    public boolean isPaymentSufficient() {
        return selectedProduct != null && 
               totalInserted >= selectedProduct.getPrice() - 0.001;
    }
    
    public double getChangeAmount() {
        if (selectedProduct == null) return 0;
        return Math.round((totalInserted - selectedProduct.getPrice()) * 100) / 100.0;
    }
    
    public void cancel() {
        this.state = TransactionState.CANCELLED;
        System.out.println("❌ Transaction cancelled");
    }
    
    public void setState(TransactionState state) {
        this.state = state;
    }
    
    // Getters
    public String getId() { return id; }
    public TransactionState getState() { return state; }
    public Product getSelectedProduct() { return selectedProduct; }
    public List<Money> getInsertedMoney() { return insertedMoney; }
    public double getTotalInserted() { return totalInserted; }
    public List<Money> getChangeToReturn() { return changeToReturn; }
    public void setChangeToReturn(List<Money> change) { 
        this.changeToReturn = change; 
    }
}
```

---

### VendingMachine (Central System)

```java
// VendingMachine.java
import java.util.*;

public class VendingMachine {
    // Singleton instance
    private static VendingMachine instance;
    private static final Object lock = new Object();
    
    // Core components
    private Inventory inventory;
    private CashHolder cashHolder;
    private Transaction currentTransaction;
    
    // Transaction ID counter
    private static int transactionCounter = 1;
    
    // Private constructor for Singleton
    private VendingMachine() {
        this.inventory = new Inventory();
        this.cashHolder = new CashHolder();
        this.currentTransaction = null;
    }
    
    // Thread-safe Singleton
    public static VendingMachine getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new VendingMachine();
                }
            }
        }
        return instance;
    }
    
    // Display all products
    public void displayProducts() {
        System.out.println("\n╔═══════════════════════════════════════════════╗");
        System.out.println("║           AVAILABLE PRODUCTS                  ║");
        System.out.println("╠═══════════════════════════════════════════════╣");
        
        for (ProductSlot slot : inventory.getAllSlots()) {
            System.out.println("║ " + slot.toString());
        }
        
        System.out.println("╚═══════════════════════════════════════════════╝\n");
    }
    
    // User selects product by code
    public synchronized boolean selectProduct(String code) {
        // Check if transaction already active
        if (currentTransaction != null && 
            currentTransaction.getState() != TransactionState.COMPLETED &&
            currentTransaction.getState() != TransactionState.CANCELLED) {
            System.out.println("❌ Transaction already in progress");
            return false;
        }
        
        // Get product slot
        ProductSlot slot = inventory.getProductSlot(code);
        
        if (slot == null) {
            System.out.println("❌ Invalid product code: " + code);
            return false;
        }
        
        if (!slot.isAvailable()) {
            System.out.println("❌ Product out of stock: " + slot.getProduct().getName());
            return false;
        }
        
        // Create new transaction
        currentTransaction = new Transaction("TXN-" + (transactionCounter++));
        currentTransaction.selectProduct(slot.getProduct());
        
        System.out.println("💡 Please insert $" + 
                         String.format("%.2f", slot.getProduct().getPrice()));
        
        return true;
    }
    
    // User inserts money
    public synchronized void insertMoney(Money money) {
        if (currentTransaction == null || 
            currentTransaction.getState() == TransactionState.IDLE) {
            System.out.println("❌ Please select a product first");
            return;
        }
        
        if (currentTransaction.getState() == TransactionState.COMPLETED ||
            currentTransaction.getState() == TransactionState.CANCELLED) {
            System.out.println("❌ Transaction already completed");
            return;
        }
        
        // Accept money
        currentTransaction.insertMoney(money);
        cashHolder.acceptMoney(money);
        
        // Check if payment is sufficient
        if (currentTransaction.isPaymentSufficient()) {
            System.out.println("✅ Payment complete! Processing...");
            processTransaction();
        } else {
            double remaining = currentTransaction.getSelectedProduct().getPrice() - 
                             currentTransaction.getTotalInserted();
            System.out.println("💡 Remaining: $" + String.format("%.2f", remaining));
        }
    }
    
    // Process the transaction
    private boolean processTransaction() {
        Product product = currentTransaction.getSelectedProduct();
        double changeAmount = currentTransaction.getChangeAmount();
        
        currentTransaction.setState(TransactionState.PAYMENT_COMPLETE);
        
        // Step 1: Check if we can give change
        if (!cashHolder.canGiveChange(changeAmount)) {
            System.out.println("❌ Cannot provide exact change. Refunding...");
            refundMoney();
            currentTransaction.setState(TransactionState.FAILED);
            return false;
        }
        
        // Step 2: Dispense product
        currentTransaction.setState(TransactionState.DISPENSING);
        Product dispensed = inventory.purchaseProduct(
            getSlotCodeByProduct(product)
        );
        
        if (dispensed == null) {
            System.out.println("❌ Dispensing failed. Refunding...");
            refundMoney();
            currentTransaction.setState(TransactionState.FAILED);
            return false;
        }
        
        // Step 3: Return change
        currentTransaction.setState(TransactionState.RETURNING_CHANGE);
        if (changeAmount > 0.001) {
            List<Money> change = cashHolder.getChange(changeAmount);
            if (change == null) {
                System.out.println("❌ Change return failed (should not happen after check)");
                // Manual intervention needed
                return false;
            }
            currentTransaction.setChangeToReturn(change);
        }
        
        // Step 4: Complete
        currentTransaction.setState(TransactionState.COMPLETED);
        System.out.println("\n✅ " + currentTransaction.getState().getMessage());
        System.out.println("═══════════════════════════════════════════════\n");
        
        return true;
    }
    
    // Helper to get slot code by product (for demo purposes)
    private String getSlotCodeByProduct(Product product) {
        for (ProductSlot slot : inventory.getAllSlots()) {
            if (slot.getProduct().getId() == product.getId()) {
                return slot.getCode();
            }
        }
        return null;
    }
    
    // User cancels transaction
    public synchronized void cancelTransaction() {
        if (currentTransaction == null || 
            currentTransaction.getState() == TransactionState.IDLE) {
            System.out.println("❌ No active transaction to cancel");
            return;
        }
        
        if (currentTransaction.getState() == TransactionState.COMPLETED) {
            System.out.println("❌ Transaction already completed");
            return;
        }
        
        currentTransaction.cancel();
        refundMoney();
        System.out.println("═══════════════════════════════════════════════\n");
    }
    
    // Refund inserted money
    private void refundMoney() {
        List<Money> refund = cashHolder.refundMoney(
            currentTransaction.getInsertedMoney()
        );
        
        System.out.println("💰 Refunded:");
        for (Money money : refund) {
            System.out.println("   - " + money.getDenomination());
        }
    }
    
    // ===== ADMIN OPERATIONS =====
    
    public boolean restockProduct(String code, int quantity) {
        return inventory.restockProduct(code, quantity);
    }
    
    public void addCashToMachine(Money money, int count) {
        cashHolder.addMoney(money, count);
    }
    
    public double collectCash(double amount) {
        return cashHolder.collectCash(amount);
    }
    
    public void showSystemStatus() {
        System.out.println("\n╔═══════════════════════════════════════════════╗");
        System.out.println("║         VENDING MACHINE STATUS                ║");
        System.out.println("╠═══════════════════════════════════════════════╣");
        System.out.println("║ Total Cash: $" + 
                         String.format("%.2f", cashHolder.getTotalBalance()));
        System.out.println("║ Total Products: " + inventory.getAllSlots().size());
        System.out.println("║ Transaction: " + 
                         (currentTransaction != null ? 
                          currentTransaction.getState() : "None"));
        System.out.println("╚═══════════════════════════════════════════════╝\n");
    }
}
```

---

### Demo Application

```java
// VendingMachineDemo.java
public class VendingMachineDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   VENDING MACHINE SYSTEM DEMONSTRATION       ");
        System.out.println("═══════════════════════════════════════════════\n");
        
        // Get vending machine instance
        VendingMachine machine = VendingMachine.getInstance();
        
        // ===== ADMIN SETUP =====
        System.out.println("🔧 ADMIN: Setting up machine...\n");
        
        // Add products
        Product coke = new Product(1, "Coca-Cola", 2.00);
        Product chips = new Product(2, "Lays Chips", 1.50);
        Product candy = new Product(3, "Snickers Bar", 1.00);
        Product water = new Product(4, "Water Bottle", 1.25);
        
        ProductSlot slotA1 = new ProductSlot("A1", coke, 10, 15);
        ProductSlot slotA2 = new ProductSlot("A2", chips, 8, 15);
        ProductSlot slotB1 = new ProductSlot("B1", candy, 12, 20);
        ProductSlot slotB2 = new ProductSlot("B2", water, 0, 15); // Out of stock
        
        machine.restockProduct("A1", 0); // Register slots
        machine.restockProduct("A2", 0);
        machine.restockProduct("B1", 0);
        machine.restockProduct("B2", 0);
        
        // Manually add slots (in real system, this would be in VendingMachine)
        VendingMachine vm = VendingMachine.getInstance();
        vm.restockProduct("A1", 10);
        vm.restockProduct("A2", 8);
        vm.restockProduct("B1", 12);
        
        // Add cash for change
        machine.addCashToMachine(Coin.QUARTER, 20);
        machine.addCashToMachine(Coin.DIME, 20);
        machine.addCashToMachine(Coin.NICKEL, 20);
        machine.addCashToMachine(Coin.PENNY, 50);
        machine.addCashToMachine(Note.DOLLAR_1, 10);
        
        System.out.println("\n");
        machine.showSystemStatus();
        
        // ===== SCENARIO 1: Successful Purchase with Change =====
        System.out.println("═══ SCENARIO 1: Buy Coke ($2.00) with $3.00 ═══\n");
        
        machine.displayProducts();
        machine.selectProduct("A1"); // Select Coke
        machine.insertMoney(Note.DOLLAR_1);
        machine.insertMoney(Note.DOLLAR_1);
        machine.insertMoney(Note.DOLLAR_1); // Total: $3.00, should get $1.00 change
        
        System.out.println("\n");
        
        // ===== SCENARIO 2: Exact Payment =====
        System.out.println("═══ SCENARIO 2: Buy Chips ($1.50) with exact change ═══\n");
        
        machine.selectProduct("A2"); // Select Chips
        machine.insertMoney(Note.DOLLAR_1);
        machine.insertMoney(Coin.QUARTER);
        machine.insertMoney(Coin.QUARTER); // Exact: $1.50
        
        System.out.println("\n");
        
        // ===== SCENARIO 3: Out of Stock =====
        System.out.println("═══ SCENARIO 3: Try to buy out-of-stock Water ═══\n");
        
        machine.selectProduct("B2"); // Water is out of stock
        
        System.out.println("\n");
        
        // ===== SCENARIO 4: Cancelled Transaction =====
        System.out.println("═══ SCENARIO 4: Cancel transaction mid-payment ═══\n");
        
        machine.selectProduct("B1"); // Select Candy
        machine.insertMoney(Coin.QUARTER);
        machine.insertMoney(Coin.QUARTER); // $0.50 inserted
        System.out.println("\n🚫 User decides to cancel...\n");
        machine.cancelTransaction();
        
        System.out.println("\n");
        
        // ===== SCENARIO 5: Insufficient Payment (then add more) =====
        System.out.println("═══ SCENARIO 5: Insufficient payment, then complete ═══\n");
        
        machine.selectProduct("B1"); // Select Candy ($1.00)
        machine.insertMoney(Coin.QUARTER); // $0.25
        machine.insertMoney(Coin.QUARTER); // $0.50
        machine.insertMoney(Coin.QUARTER); // $0.75
        machine.insertMoney(Coin.QUARTER); // $1.00 - Complete!
        
        System.out.println("\n");
        
        // ===== FINAL STATUS =====
        machine.showSystemStatus();
        
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("         DEMO COMPLETED                        ");
        System.out.println("═══════════════════════════════════════════════");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Singleton Pattern
**Where:** VendingMachine class  
**Why:** Only one physical vending machine exists  
**Interview Justification:**
"Using Singleton because we're modeling a physical device. Multiple instances would lead to inconsistent inventory and cash state. The double-checked locking ensures thread-safe initialization."

---

### Pattern 2: State Machine Pattern
**Where:** Transaction class + TransactionState enum  
**Why:** Transaction has clear states and transitions

**State Diagram:**
```
IDLE → PRODUCT_SELECTED → PAYMENT_PENDING → PAYMENT_COMPLETE 
     → DISPENSING → RETURNING_CHANGE → COMPLETED

     PRODUCT_SELECTED/PAYMENT_PENDING → CANCELLED
     Any state → FAILED (on error)
```

**Interview Justification:**
"Transaction is inherently stateful. State pattern makes state transitions explicit and prevents invalid operations (e.g., can't insert money before selecting product). Each state knows what operations are valid."

**Alternative Implementation (State Pattern with Classes):**
```java
public interface VendingMachineState {
    void selectProduct(String code);
    void insertMoney(Money money);
    void dispense();
    void cancel();
}

public class IdleState implements VendingMachineState {
    public void selectProduct(String code) {
        // Allowed
    }
    
    public void insertMoney(Money money) {
        System.out.println("Select product first");
    }
}

public class PaymentPendingState implements VendingMachineState {
    public void insertMoney(Money money) {
        // Allowed
    }
    
    public void selectProduct(String code) {
        System.out.println("Already in transaction");
    }
}
```

---

### Pattern 3: Strategy Pattern (Change Calculation)
**Where:** CashHolder - different change algorithms  
**Why:** Multiple ways to calculate change

**Current:** Greedy algorithm (works for US currency)

**Interview Discussion:**
"For US currency, greedy algorithm is optimal. But for some currency systems (e.g., certain coin sets), greedy doesn't work. We could use Strategy pattern to swap algorithms:

```java
public interface ChangeStrategy {
    List<Money> calculateChange(double amount, Map<Money, Integer> available);
}

public class GreedyChangeStrategy implements ChangeStrategy {
    public List<Money> calculateChange(double amount, Map<Money, Integer> available) {
        // Current implementation
    }
}

public class DynamicProgrammingChangeStrategy implements ChangeStrategy {
    public List<Money> calculateChange(double amount, Map<Money, Integer> available) {
        // DP for optimal solution (handles non-greedy currency)
    }
}

public class CashHolder {
    private ChangeStrategy changeStrategy;
    
    public void setChangeStrategy(ChangeStrategy strategy) {
        this.changeStrategy = strategy;
    }
}
```

---

### Pattern 4: Template Method Pattern (Transaction Processing)
**Where:** VendingMachine.processTransaction()

**Structure:**
```java
public boolean processTransaction() {
    // Step 1: Validate change availability
    if (!validateChange()) {
        handleFailure();
        return false;
    }
    
    // Step 2: Dispense product
    if (!dispenseProduct()) {
        handleFailure();
        return false;
    }
    
    // Step 3: Return change
    if (!returnChange()) {
        handleManualIntervention();
        return false;
    }
    
    // Step 4: Complete
    completeTransaction();
    return true;
}
```

**Interview Justification:**
"Template Method defines the transaction skeleton. Each step is a method that can be overridden or extended. This ensures consistent transaction flow while allowing flexibility in implementation."

---

### Pattern 5: Factory Pattern (Money Creation)
**Where:** Money instantiation

**Implementation:**
```java
public class MoneyFactory {
    public static Money createCoin(double value) {
        if (value == 0.01) return Coin.PENNY;
        if (value == 0.05) return Coin.NICKEL;
        if (value == 0.10) return Coin.DIME;
        if (value == 0.25) return Coin.QUARTER;
        throw new IllegalArgumentException("Invalid coin value");
    }
    
    public static Money createNote(double value) {
        if (value == 1.0) return Note.DOLLAR_1;
        if (value == 5.0) return Note.DOLLAR_5;
        if (value == 10.0) return Note.DOLLAR_10;
        if (value == 20.0) return Note.DOLLAR_20;
        throw new IllegalArgumentException("Invalid note value");
    }
}
```

**Interview Justification:**
"Factory centralizes Money object creation. If we need to add validation (fake note detection) or new denominations, we modify only the factory."

---

## 🔒 Step 8: Handle Concurrency (8 minutes)

### Critical Section Analysis

#### 1. **Inventory Stock Updates**

**Problem:** Multiple transactions trying to buy last item simultaneously

**Solution:**
```java
public synchronized boolean reduceStock() {
    if (quantity > 0) {
        quantity--;
        return true;
    }
    return false;
}
```

**Interview Explanation:**
"The `synchronized` keyword ensures atomic check-and-decrement. Without it, two threads could both check `quantity > 0` (both see 1), then both decrement (quantity becomes -1)."

**Test Case:**
```
Initial: quantity = 1
Thread A: checks (quantity > 0) → true
Thread B: checks (quantity > 0) → true
Thread A: quantity-- → 0
Thread B: quantity-- → -1 (BUG!)

With synchronized:
Thread A: acquires lock, checks, decrements → 0, releases lock
Thread B: acquires lock, checks (quantity = 0) → false, releases lock
```

---

#### 2. **Cash Balance Updates**

**Problem:** Money insertion and change return happening simultaneously

**Solution:**
```java
public synchronized void acceptMoney(Money money) {
    cashInventory.merge(money, 1, Integer::sum);
    totalBalance += money.getValue();
}

public synchronized List<Money> getChange(double amount) {
    // Calculate and deduct atomically
    for (Money money : change) {
        cashInventory.merge(money, -1, Integer::sum);
        totalBalance -= money.getValue();
    }
    return change;
}
```

**Interview Explanation:**
"Both methods are synchronized on CashHolder instance. This prevents:
- Lost updates (increment and decrement clash)
- Inconsistent state (balance updated but inventory not)
- Reading partial state"

---

#### 3. **Single Transaction at a Time**

**Problem:** Two users trying to use machine simultaneously (in physical world, unlikely, but in software simulation, possible)

**Solution:**
```java
public synchronized boolean selectProduct(String code) {
    if (currentTransaction != null && 
        currentTransaction.getState() != TransactionState.COMPLETED) {
        System.out.println("Transaction already in progress");
        return false;
    }
    // ... rest of logic
}
```

**Interview Explanation:**
"The VendingMachine orchestration methods are synchronized. This creates a global lock ensuring only one transaction progresses at a time. Mimics physical reality - only one user can interact with machine."

---

#### 4. **Admin Operations During Transaction**

**Problem:** Admin tries to restock while user is mid-purchase

**Solution:**
```java
public synchronized boolean restockProduct(String code, int quantity) {
    // Synchronized at VendingMachine level
    // Blocks if transaction is active
    return inventory.restockProduct(code, quantity);
}
```

**Alternative (More Granular):**
```java
private ReadWriteLock inventoryLock = new ReentrantReadWriteLock();

public Product purchaseProduct(String code) {
    inventoryLock.readLock().lock();  // Multiple reads OK
    try {
        // ... purchase logic
    } finally {
        inventoryLock.readLock().unlock();
    }
}

public boolean restockProduct(String code, int qty) {
    inventoryLock.writeLock().lock();  // Exclusive write
    try {
        // ... restock logic
    } finally {
        inventoryLock.writeLock().unlock();
    }
}
```

**Interview Discussion:**
"For simple vending machine, full synchronization is fine. For high-throughput system (like central inventory server for multiple machines), ReadWriteLock allows concurrent purchases but exclusive restocking."

---

### Deadlock Prevention

**Potential Deadlock:**
```
Thread A: locks CashHolder, tries to lock Inventory
Thread B: locks Inventory, tries to lock CashHolder
→ Deadlock!
```

**Prevention Strategy: Lock Ordering**
```java
// Always acquire locks in same order
public void processTransaction() {
    synchronized(inventory) {        // Always inventory first
        synchronized(cashHolder) {    // Then cashHolder
            // Process transaction
        }
    }
}
```

**Interview Explanation:**
"Consistent lock ordering prevents circular wait. If all threads acquire locks in same order (Inventory → CashHolder), deadlock impossible."

---

## 💡 Step 9: Interview Discussion Points

### Question 1: "How would you handle hardware failures?"

**Answer Structure:**
1. Identify failure types
2. Detection mechanisms
3. Recovery strategies

**Response:**

"Hardware failures in vending machines:

**Failure Type 1: Coin/Note Acceptor Jam**
```java
public interface MoneyReceptor {
    boolean insertMoney(Money money);
    boolean isJammed();
    void clearJam();
}

public class CoinAcceptor implements MoneyReceptor {
    public boolean insertMoney(Money money) {
        if (isJammed()) {
            System.out.println("Coin acceptor jammed. Maintenance needed.");
            return false;
        }
        
        if (!validateMoney(money)) {
            return false; // Reject fake/invalid coin
        }
        
        return true;
    }
}
```

**Failure Type 2: Product Dispenser Jam**
```java
public interface ProductDispenser {
    boolean dispense(String slotCode);
    boolean verifyDispensed(); // Sensor check
}

public void dispenseProduct(String code) {
    if (!dispenser.dispense(code)) {
        // Dispensing failed
        refundTransaction();
        alertMaintenance("Dispenser jam at slot " + code);
        return;
    }
    
    // Verify product actually dropped (sensor)
    if (!dispenser.verifyDispensed()) {
        refundTransaction();
        alertMaintenance("Sensor failure at slot " + code);
    }
}
```

**Failure Type 3: Power Loss**
```java
public class TransactionLogger {
    private FileWriter log;
    
    public void logTransactionStart(Transaction txn) {
        log.write("START|" + txn.getId() + "|" + 
                 txn.getSelectedProduct().getId() + "|" +
                 txn.getTotalInserted());
        log.flush(); // Ensure written to disk
    }
    
    public void logTransactionComplete(Transaction txn) {
        log.write("COMPLETE|" + txn.getId());
        log.flush();
    }
}

// On system restart
public void recoverFromPowerLoss() {
    List<Transaction> incomplete = transactionLogger.getIncompleteTransactions();
    
    for (Transaction txn : incomplete) {
        // Determine state and take action
        if (moneyWasCollected(txn) && !productWasDispensed(txn)) {
            // Critical: money taken, product not given
            alertMaintenance("Manual intervention: " + txn.getId());
            // Mark slot for free product to next customer
        } else {
            // Safe to consider failed
            // Next customer inserting money will clear state
        }
    }
}
```

---

### Question 2: "How to handle multiple vending machines?"

**Answer:**

"Scaling to multiple machines:

**Architecture:**
```
            ┌─────────────────┐
            │  Central Server  │
            │  - Inventory DB  │
            │  - Analytics     │
            │  - Monitoring    │
            └────────┬─────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│Machine 1 │  │Machine 2 │  │Machine 3 │
│  Local   │  │  Local   │  │  Local   │
│  Cache   │  │  Cache   │  │  Cache   │
└──────────┘  └──────────┘  └──────────┘
```

**Implementation:**
```java
public class VendingMachineClient {
    private String machineId;
    private RestTemplate restClient;
    private LocalInventoryCache cache;
    
    public boolean processTransaction(Transaction txn) {
        try {
            // 1. Process locally
            boolean success = processLocally(txn);
            
            // 2. Sync to central server (async)
            CompletableFuture.runAsync(() -> {
                syncTransaction(txn);
            });
            
            return success;
            
        } catch (Exception e) {
            // Fallback: operate offline
            return processOffline(txn);
        }
    }
    
    public void syncTransaction(Transaction txn) {
        TransactionDTO dto = new TransactionDTO(machineId, txn);
        restClient.postForObject("/api/transactions", dto, Void.class);
    }
    
    public void syncInventory() {
        // Periodic inventory sync
        InventoryDTO dto = new InventoryDTO(machineId, inventory.getAllSlots());
        restClient.postForObject("/api/inventory", dto, Void.class);
    }
}

// Central Server
@RestController
public class VendingMachineController {
    @PostMapping("/api/transactions")
    public void recordTransaction(@RequestBody TransactionDTO txn) {
        transactionRepository.save(txn);
        analyticsService.updateMetrics(txn);
        
        // Check if restocking needed
        if (shouldRestock(txn.getMachineId(), txn.getProductId())) {
            alertRestocking(txn.getMachineId());
        }
    }
}
```

**Benefits:**
- Central analytics (which products sell most)
- Remote monitoring (know which machines need restocking)
- Predictive maintenance (detect patterns before failure)
- Dynamic pricing (adjust prices based on demand)

---

### Question 3: "How to optimize change-making algorithm?"

**Answer:**

"Current greedy algorithm works for US currency but isn't always optimal.

**Problem Example:**
```
Coins: [1¢, 3¢, 4¢]
Change needed: 6¢

Greedy: 4¢ + 1¢ + 1¢ = 3 coins
Optimal: 3¢ + 3¢ = 2 coins
```

**Dynamic Programming Solution:**
```java
public class OptimalChangeCalculator {
    public List<Money> calculateOptimalChange(double amount, 
                                             Map<Money, Integer> available) {
        int cents = (int) Math.round(amount * 100);
        
        // DP array: dp[i] = min coins needed for amount i
        int[] dp = new int[cents + 1];
        Money[] parent = new Money[cents + 1];
        
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        
        // For each amount
        for (int i = 1; i <= cents; i++) {
            // Try each denomination
            for (Money money : available.keySet()) {
                int coinValue = (int) Math.round(money.getValue() * 100);
                
                if (coinValue <= i && 
                    dp[i - coinValue] != Integer.MAX_VALUE &&
                    available.get(money) > 0) {
                    
                    if (dp[i - coinValue] + 1 < dp[i]) {
                        dp[i] = dp[i - coinValue] + 1;
                        parent[i] = money;
                    }
                }
            }
        }
        
        // Backtrack to build result
        List<Money> result = new ArrayList<>();
        int current = cents;
        while (current > 0) {
            Money coin = parent[current];
            result.add(coin);
            current -= (int) Math.round(coin.getValue() * 100);
        }
        
        return result;
    }
}
```

**Complexity:**
- Greedy: O(n) where n = number of denominations
- DP: O(amount × n)

**Trade-off:**
- Greedy: Fast, but not always optimal
- DP: Optimal, but slower

**Interview Recommendation:**
"For US currency, greedy is fine. For other currencies or if minimizing coin count is critical (coin shortage), use DP."

---

### Question 4: "How to add support for credit card payments?"

**Answer:**

"Extend payment system with new payment method:

**Step 1: Abstract Payment Interface**
```java
public interface PaymentMethod {
    boolean processPayment(double amount);
    void refund(double amount);
    String getPaymentType();
}

// Existing cash payment
public class CashPayment implements PaymentMethod {
    private List<Money> insertedMoney;
    
    public boolean processPayment(double amount) {
        return insertedMoney.stream()
                           .mapToDouble(Money::getValue)
                           .sum() >= amount;
    }
    
    public void refund(double amount) {
        // Return physical cash
    }
    
    public String getPaymentType() {
        return "CASH";
    }
}

// New card payment
public class CardPayment implements PaymentMethod {
    private String cardNumber;
    private String cvv;
    private PaymentGateway gateway;
    
    public boolean processPayment(double amount) {
        try {
            String transactionId = gateway.charge(cardNumber, amount);
            System.out.println("Card charged: " + transactionId);
            return true;
        } catch (PaymentException e) {
            System.out.println("Card declined: " + e.getMessage());
            return false;
        }
    }
    
    public void refund(double amount) {
        gateway.refund(cardNumber, amount);
    }
    
    public String getPaymentType() {
        return "CARD";
    }
}
```

**Step 2: Update Transaction**
```java
public class Transaction {
    private PaymentMethod paymentMethod;
    
    public void setPaymentMethod(PaymentMethod method) {
        this.paymentMethod = method;
    }
    
    public boolean processPayment() {
        return paymentMethod.processPayment(
            selectedProduct.getPrice()
        );
    }
}
```

**Step 3: Update VendingMachine**
```java
public void selectPaymentMethod(String method) {
    if (method.equals("CASH")) {
        currentTransaction.setPaymentMethod(new CashPayment());
    } else if (method.equals("CARD")) {
        currentTransaction.setPaymentMethod(new CardPayment());
    } else if (method.equals("MOBILE")) {
        currentTransaction.setPaymentMethod(new MobilePayment());
    }
}
```

**Benefits:**
- No change calculation needed for cards
- No cash handling overhead
- Faster transactions
- Digital receipts"

---

### Question 5: "How to implement dynamic pricing?"

**Answer:**

"Dynamic pricing based on demand, time, or inventory:

```java
public interface PricingStrategy {
    double calculatePrice(Product product, LocalDateTime time);
}

public class StandardPricing implements PricingStrategy {
    public double calculatePrice(Product product, LocalDateTime time) {
        return product.getBasePrice();
    }
}

public class DynamicPricing implements PricingStrategy {
    public double calculatePrice(Product product, LocalDateTime time) {
        double basePrice = product.getBasePrice();
        
        // Peak hours: 12pm-2pm, charge 20% more
        int hour = time.getHour();
        if (hour >= 12 && hour <= 14) {
            return basePrice * 1.2;
        }
        
        // Low inventory: last 3 items, charge 10% more
        int inventory = getInventoryCount(product);
        if (inventory <= 3) {
            return basePrice * 1.1;
        }
        
        // Expiring soon: discount 30%
        if (isExpiringSoon(product)) {
            return basePrice * 0.7;
        }
        
        return basePrice;
    }
}

public class Product {
    private double basePrice;
    private PricingStrategy pricingStrategy;
    
    public double getCurrentPrice() {
        return pricingStrategy.calculatePrice(this, LocalDateTime.now());
    }
}
```

---

## ✅ Step 10: SOLID Principles Check

### S - Single Responsibility Principle
| Class | Responsibility | One Reason to Change? |
|-------|---------------|----------------------|
| Product | Product data | ✅ Only if product properties change |
| ProductSlot | Inventory management for one slot | ✅ Only if slot logic changes |
| Inventory | Manage all product slots | ✅ Only if inventory operations change |
| CashHolder | Manage cash and change | ✅ Only if cash handling changes |
| Transaction | Track transaction state | ✅ Only if transaction flow changes |
| VendingMachine | Orchestrate operations | ✅ Only if workflow changes |

### O - Open/Closed Principle
- ✅ Can add new Money denominations without modifying Money class
- ✅ Can add new payment methods (card, mobile) by implementing interface
- ✅ Can add new pricing strategies without modifying Product
- ✅ Can add new change algorithms without modifying CashHolder

### L - Liskov Substitution Principle
```java
Money money = new Coin(0.25, "Quarter");  // Can substitute Coin for Money
money.getValue();  // Works correctly

Money money = new Note(5.0, "Five Dollars");  // Can substitute Note for Money
money.getValue();  // Works correctly
```
✅ All substitutions work correctly

### I - Interface Segregation Principle
- ✅ Interfaces are minimal and focused
- ✅ No fat interfaces with unused methods
- ✅ Could add if needed: `Restockable`, `Displayable`

### D - Dependency Inversion Principle
```java
// High-level VendingMachine depends on abstractions
private ProductDispenser dispenser;  // Interface, not concrete class

// Low-level hardware implements interface
public class PhysicalDispenser implements ProductDispenser {
    public boolean dispense(String slotCode) {
        // Hardware-specific code
    }
}
```
✅ Depend on abstractions, not concrete implementations

---

## 🎯 Interview Tips & Talking Points

### Opening (30 seconds):
"Let me start by understanding the requirements - what products, payment methods, and scale. Then I'll identify entities, design relationships, and implement the core flow with proper concurrency handling."

### During Entity Identification (2 minutes):
"I'm extracting nouns: Product, Money, Inventory, Transaction. For Money, I see Coin and Note are subtypes, so I'll use inheritance. ProductSlot separates product definition from storage location..."

### During Relationship Design (3 minutes):
"VendingMachine HAS-A Inventory with composition because inventory can't exist without the machine. But ProductSlot HAS-A Product with aggregation because the product concept exists before stocking..."

### During Concurrency Discussion (2 minutes):
"Critical section is stock reduction - two users can't buy the last item. Using synchronized method ensures atomic check-and-decrement. CashHolder operations also synchronized to prevent lost updates..."

### During Patterns Discussion (2 minutes):
"Using Singleton for VendingMachine since it's a physical device. State Machine for Transaction because it has clear states and transitions. Strategy pattern for change calculation to swap algorithms..."

### When Discussing Extensions:
"To add card payment, I'd create a PaymentMethod interface. Cash and Card both implement it. Transaction doesn't need to know payment details - polymorphism handles it..."

### Closing (1 minute):
"The design follows SOLID principles, handles concurrency properly, and is extensible for new payment methods and pricing strategies. Key trade-off is greedy vs optimal change algorithm - greedy is fast and works for US currency."

---

## 📈 Complexity Analysis

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| Select Product | O(1) | O(1) |
| Insert Money | O(1) | O(1) |
| Check Stock | O(1) | O(1) |
| Calculate Change (Greedy) | O(n) | O(k) |
| Calculate Change (DP) | O(amount × n) | O(amount) |
| Restock | O(1) | O(1) |
| Display Products | O(n) | O(1) |

Where:
- n = number of denominations (typically 8: 4 coins + 4 notes)
- k = number of coins in change (typically < 10)
- amount = change amount in cents

**Space Complexity (Total System):**
O(P + D) where P = number of products, D = number of denominations

---

## 🎓 Key Takeaways

### Interview Success Formula:

1. **Clarify** (5 min) - Understand physical constraints, payment methods, scale
2. **Requirements** (5 min) - Functional point-wise, deduce NFRs using SCAMPS
3. **Entities** (10 min) - Money hierarchy, Product/Slot separation
4. **Relationships** (10 min) - Composition vs Aggregation, cardinality
5. **Class Diagrams** (10 min) - Focus on Money, Transaction, VendingMachine
6. **Implementation** (20 min) - State machine, change calculation, concurrency
7. **Patterns** (5 min) - Singleton, State, Strategy
8. **Extensions** (5 min) - Card payment, dynamic pricing, multi-machine

### What Makes This Design Good:

✅ **Physical Reality Modeled** - ProductSlot separates concept from storage  
✅ **State Machine** - Transaction flow is explicit and safe  
✅ **Thread-Safe** - Proper synchronization on critical operations  
✅ **Extensible** - Easy to add payment methods, change algorithms  
✅ **Fail-Safe** - Defaults to refunding money on errors  

### Common Mistakes to Avoid:

❌ Forgetting to model ProductSlot (just Product in Inventory)  
❌ Not handling insufficient change scenario  
❌ Missing transaction cancellation  
❌ Ignoring concurrency (multiple users)  
❌ Not discussing hardware failure recovery  
❌ Forgetting to validate payment sufficiency before dispensing

---

**This systematic approach works for any state-based system design!**
