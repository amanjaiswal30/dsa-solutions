#!/usr/bin/env python3
"""Generate interview-format LLD markdown files (*_lld.md)."""

from __future__ import annotations

import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from lld_java_analysis import (
    build_diagram_for_project,
    read_java_sources_ordered,
)

LLD_ROOT = Path("/Users/aman.jaiswal/Desktop/LLD")
REPO_ROOT = Path("/Users/aman.jaiswal/Desktop/dsa-solutions")
OUT_DIR = REPO_ROOT / "questions/low_level_design"
ASSETS_ROOT = REPO_ROOT / "assets"


@dataclass
class LldTopic:
    num: str
    slug: str
    title: str
    difficulty: str
    duration: str
    project: str | None
    flows: list[tuple[str, list[str]]]
    entities: list[tuple[str, str, str]]  # name, responsibility, fields
    relationships: list[str]
    design_notes: list[str] = field(default_factory=list)


TOPICS: list[LldTopic] = [
    LldTopic(
        "01", "01_parking_lot_system_lld", "Parking Lot System", "Medium ⚡", "45–60 min",
        "ParkingLot",
        [
            ("Vehicle entry", [
                "Vehicle arrives at an **entry gate**.",
                "System picks an available **parking spot** (strategy: nearest / first-free).",
                "Spot is marked occupied; **display board** counts are updated.",
                "A **ticket** is issued linking vehicle, spot, and entry time.",
            ]),
            ("Vehicle exit", [
                "Vehicle presents ticket at **exit gate**.",
                "System computes fee from duration and vehicle/spot type (extensible).",
                "Payment succeeds → spot freed, ticket closed, display board updated.",
            ]),
        ],
        [
            ("ParkingLot", "Singleton orchestrator", "floors, gates, activeTickets, displayBoard, parkingStrategy"),
            ("Floor", "Groups spots", "floorNumber, parkingSpotList"),
            ("ParkingSpot", "Physical slot", "spotType, vehicle, isOccupied"),
            ("Vehicle / Car", "Parked asset", "licenseNumber, vehicleType"),
            ("Ticket", "Entry proof", "ticketId, vehicle, parkingSpot, entryTime"),
            ("Gate / EntryGate / ExitGate", "Access points", "gate id, floor"),
            ("DisplayBoard", "Availability UI", "freeSpots per SpotType"),
            ("ParkingStrategy", "Spot selection", "NearestParkingSpot implementation"),
        ],
        [
            "ParkingLot **1—*** many Floor; Floor **1—*** many ParkingSpot",
            "Ticket **1—1** Vehicle; Ticket **1—1** ParkingSpot",
            "ParkingLot uses ParkingStrategy to search across floors",
        ],
        ["Strategy pattern for spot assignment", "Singleton for single-lot instance"],
    ),
    LldTopic(
        "02", "02_movie_ticket_booking_system_lld", "Movie Ticket Booking System", "Hard 🔥", "60–75 min",
        "Movie Ticket Booking System",
        [
            ("Book show", [
                "User searches **movie** in a **city** (Location).",
                "User picks a **show** (movie + time + screen layout).",
                "User selects **seats**; system locks/checks availability.",
                "User pays; **ticket** confirmed with seat map snapshot.",
            ]),
        ],
        [
            ("User", "Customer", "profile, booking history"),
            ("Movie", "Catalog item", "title, genre, metadata"),
            ("Location", "City / theatre region", "cityName"),
            ("Show", "Screening instance", "movie, showTime, seats"),
            ("Seat / ShowSeat", "Bookable unit", "seatNumber, seatType, isAvailable"),
            ("Ticket", "Confirmed booking", "show, seats, status"),
            ("BookingService", "Use cases", "search, hold seats, confirm"),
        ],
        [
            "Location hosts many Show; Show has many Seat",
            "Ticket aggregates selected Seat(s) for one Show",
        ],
    ),
    LldTopic(
        "03", "03_chess_game_lld", "Chess Game", "Hard 🔥", "60–75 min",
        None,
        [
            ("Play game", [
                "Two players join; **board** initialized.",
                "Active player submits **move**; engine validates legality.",
                "Board updates; check checkmate/stalemate/draw rules.",
                "Optional: undo/redo via **command** stack.",
            ]),
        ],
        [
            ("Game", "Session orchestrator", "board, players, status, turn"),
            ("Board", "8×8 state", "squares, pieces"),
            ("Piece / Move", "Rules + application", "from, to, capture, special moves"),
            ("Player", "Human or AI", "color, clock"),
            ("GameStatus", "Lifecycle", "IN_PROGRESS, CHECKMATE, STALEMATE, …"),
        ],
        [
            "Game **1—1** Board; Game **2—** Player",
            "MoveCommand applies to Board (Command pattern for undo)",
        ],
    ),
    LldTopic(
        "04", "04_customer_issue_resolution_system_lld", "Customer Issue Resolution System", "Medium ⚡", "45–60 min",
        "Customer Issue Resolution System",
        [
            ("Ticket lifecycle", [
                "**Customer** raises a **ticket** (type, priority, description).",
                "**TicketAssignmentService** assigns to an **Agent** (strategy: expertise-based).",
                "Agent works ticket: OPEN → IN_PROGRESS → RESOLVED.",
                "**Admin** can reassign if needed.",
            ]),
        ],
        [
            ("User", "Base identity", "name, email"),
            ("Customer", "Requester", "tickets raised"),
            ("Agent", "Resolver", "expertise, activeTickets, availability"),
            ("Admin", "Operations", "reassign capability"),
            ("Ticket", "Work item", "status, priority, issueType, assignee"),
            ("AssignmentStrategy", "Routing", "ExpertiseBasedAssignment"),
            ("TicketAssignmentService", "Facade", "create, assign, reassign, resolve"),
        ],
        [
            "Customer **1—*** Ticket; Agent **0—*** Ticket (assigned)",
            "TicketAssignmentService uses AssignmentStrategy",
        ],
    ),
    LldTopic(
        "05", "05_dunzo_delivery_system_lld", "Dunzo / Hyperlocal Delivery", "Hard 🔥", "60–75 min",
        "Dunzo",
        [
            ("Deliver parcel", [
                "Customer creates **order** (pickup → drop **locations**).",
                "System assigns nearest free **delivery partner** (strategy).",
                "Partner picks up → in transit → delivered; **order status** updates.",
            ]),
        ],
        [
            ("Customer", "Sender", "profile, orders"),
            ("Order", "Delivery job", "pickup, drop, status"),
            ("Location", "Geo point", "lat/long or address"),
            ("DeliveryPartner", "Courier", "location, availability"),
            ("PartnerAssignmentStrategy", "Matching", "nearest partner"),
            ("OrderService", "Orchestration", "place, assign, update status"),
        ],
        [
            "Order **2—** Location (pickup, drop)",
            "OrderService selects DeliveryPartner via strategy",
        ],
    ),
    LldTopic(
        "06", "06_restaurant_order_rating_system_lld", "Restaurant Order & Rating (Zomato-style)", "Hard 🔥", "60–75 min",
        "Zomato",
        [
            ("Food order", [
                "User browses **restaurant** menu (**menu items** + inventory).",
                "User places **order** with quantities.",
                "Kitchen/restaurant accepts; **order status** progresses.",
                "Nearest **delivery agent** assigned for fulfillment.",
            ]),
        ],
        [
            ("User", "Customer", "orders history"),
            ("Restaurant", "Seller", "location, menu"),
            ("MenuItem", "Catalog line", "name, price"),
            ("MenuInventory", "Stock per item", "quantity"),
            ("Order", "Purchase", "items, status, agent"),
            ("DeliveryAgent", "Rider", "location, isAvailable"),
            ("OrderService", "Workflow", "place, assign rider, track"),
        ],
        [
            "Restaurant **1—*** MenuItem; Order **1—*** line items",
            "Order **0—1** DeliveryAgent when out for delivery",
        ],
    ),
    LldTopic(
        "07", "07_meeting_scheduler_lld", "Meeting Scheduler", "Medium ⚡", "45–60 min",
        "MeetingScheduler",
        [
            ("Schedule meeting", [
                "Organizer defines **meeting** (time range, recurrence, participants).",
                "System checks **room** and attendee **calendars** for conflicts.",
                "On success, room booked and meeting marked active.",
                "Cancel/reschedule frees the room slot.",
            ]),
        ],
        [
            ("User", "Participant", "name, meetings list"),
            ("Meeting", "Event", "start, end, recurrence, participants, room"),
            ("Room", "Resource", "capacity, bookings"),
            ("Recurrence", "Repeat rule", "NONE, DAILY, WEEKLY, …"),
            ("MeetingRoomService", "Singleton scheduler", "create, conflict check, cancel"),
        ],
        [
            "Meeting **1—1** Room; Meeting ***—*** User participants",
            "MeetingRoomService owns conflict detection across rooms/users",
        ],
    ),
    LldTopic(
        "08", "08_splitwise_lld", "Splitwise", "Medium ⚡", "45–60 min",
        "Splitwise",
        [
            ("Split expense", [
                "User creates **group** with members.",
                "User adds **expense** with amount and **split strategy** (equal / exact / %).",
                "**SplitwiseService** updates pairwise balances / settlements.",
            ]),
        ],
        [
            ("User", "Member", "name, email, balance map"),
            ("Group", "Expense pool", "members"),
            ("Expense", "Spend event", "amount, paidBy, splits"),
            ("Split", "Per-user share", "user, amount"),
            ("SplitStrategy", "Algorithm", "Equal, Exact, Percentage"),
            ("SplitwiseService", "Ledger", "add expense, show balances"),
        ],
        [
            "Group **1—*** User; Expense **1—*** Split",
            "Expense delegates split computation to SplitStrategy",
        ],
    ),
    LldTopic(
        "09", "09_order_inventory_management_lld", "Order & Inventory Management", "Hard 🔥", "60–75 min",
        "InventoryManagement",
        [
            ("Place order", [
                "Catalog loaded into **inventory** (item → quantity).",
                "User places **order** with line items.",
                "System reserves/decrements stock; **order status** updated.",
                "Insufficient stock → reject before confirmation.",
            ]),
        ],
        [
            ("User", "Buyer", "orders"),
            ("Item", "SKU", "id, name, price"),
            ("ItemStock / Inventory", "Availability", "item → quantity"),
            ("Order", "Purchase", "lines, status"),
            ("BookingService", "Checkout", "place order, update inventory"),
        ],
        [
            "Inventory **1—*** ItemStock; Order references Item + quantity",
            "BookingService coordinates atomic stock decrement",
        ],
    ),
    LldTopic(
        "10", "10_text_editor_lld", "Text Editor", "Hard 🔥", "60–75 min",
        None,
        [
            ("Edit document", [
                "User moves **cursor**; inserts/deletes text in **buffer**.",
                "Optional **selection** for bulk delete/replace.",
                "**Undo/redo** via command stack (Command pattern).",
                "Copy/paste uses clipboard buffer.",
            ]),
        ],
        [
            ("EditorModel", "Facade", "cursor, selection, buffer"),
            ("TextBuffer", "Storage", "gap buffer / rope / piece table"),
            ("Command", "Undo unit", "execute, unexecute"),
            ("UndoManager", "History", "undo stack, redo stack"),
        ],
        [
            "EditorModel **1—1** TextBuffer",
            "Commands mutate buffer; UndoManager stacks Command objects",
        ],
    ),
    LldTopic(
        "11", "11_vending_machine_lld", "Vending Machine", "Medium ⚡", "45–60 min",
        "Vending Machine",
        [
            ("Purchase", [
                "Customer selects **product** from **inventory**.",
                "Customer inserts **cash/coins** into **money holder**.",
                "Machine validates price + change availability.",
                "Product **dispatched**; change returned; else refund.",
            ]),
            ("Admin", [
                "Admin restocks inventory and loads cash float.",
            ]),
        ],
        [
            ("VendingMachine", "Context + state", "inventory, moneyHolder, state"),
            ("Inventory / InventoryItem", "Stock", "product, quantity"),
            ("Product", "SKU", "code, price"),
            ("Money / Coin / Cash", "Payment", "denomination"),
            ("ProductDispatcher", "Physical output", "dispense product"),
            ("VendingMachineService", "API", "select, pay, cancel"),
        ],
        [
            "VendingMachine **1—1** Inventory and MoneyHolder",
            "State pattern for idle / hasMoney / dispensing / refund",
        ],
    ),
    LldTopic(
        "12", "12_atm_machine_lld", "ATM Machine", "Hard 🔥", "60–75 min",
        "ATM",
        [
            ("Withdraw / inquire", [
                "Customer inserts **card**; enters PIN → **authenticate**.",
                "Select operation: balance / withdraw / mini statement.",
                "Withdraw: validate account + ATM **cash inventory**; **dispense** notes.",
                "Session ends; card ejected; transaction logged.",
            ]),
            ("Admin", [
                "Restock note denominations in ATM cassette map.",
            ]),
        ],
        [
            ("ATM", "Hardware + cash", "note inventory, status"),
            ("ATMService", "Session facade", "card session, operations"),
            ("Customer", "Owner", "cards"),
            ("Card", "Auth token", "PIN, linked BankAccount"),
            ("BankAccount", "Ledger", "balance, transactions"),
            ("NoteDenomination", "Cash units", "₹100, ₹500, …"),
            ("TransactionRecord", "Audit", "type, amount, timestamp"),
        ],
        [
            "Customer **1—*** Card **1—1** BankAccount",
            "ATMService drives ATMState (insert card → auth → operate → eject)",
        ],
    ),
    LldTopic(
        "13", "13_tic_tac_toe_lld", "Tic Tac Toe", "Easy ✅ → Medium ⚡", "30–45 min",
        "TicTacToe",
        [
            ("Play round", [
                "Two **players** (X and O) alternate turns.",
                "**Board** cell click; validate empty cell.",
                "Check win on row/col/diag or **draw** when full.",
                "Reset starts new game.",
            ]),
        ],
        [
            ("Game", "Rules engine", "board, players, state, winner"),
            ("Board", "Grid", "cells, size"),
            ("Cell", "Slot", "symbol or empty"),
            ("Player", "Participant", "name, symbol"),
            ("GameState", "Lifecycle", "NOT_STARTED, IN_PROGRESS, WON, DRAW"),
        ],
        [
            "Game **1—1** Board; Game **2—** Player",
            "TicTacToe facade wraps Game for CLI/demo",
        ],
    ),
    LldTopic(
        "14", "14_car_rental_system_lld", "Car Rental System (ZoomCar-style)", "Hard 🔥", "60–75 min",
        "ZoomCar",
        [
            ("Rent car", [
                "User picks available **car** and books for N km.",
                "System charges **initial amount** (rate × km × location multiplier).",
                "Ride completes → final km from odometer → **remaining payment**.",
                "**Booking status**: BOOKED → COMPLETED with payment states.",
            ]),
        ],
        [
            ("User", "Renter", "bookings"),
            ("Car", "Fleet unit", "availability, odometer, ratePerKm"),
            ("Booking", "Reservation", "kmsBooked, status, amounts"),
            ("Payment", "Settlement", "method, amount"),
            ("BookingService", "Workflow", "book, complete, pay"),
        ],
        [
            "User **1—*** Booking **1—1** Car",
            "Payment settles initial vs final fare on completion",
        ],
    ),
    LldTopic(
        "15", "15_digital_wallet_lld", "Digital Wallet", "Hard 🔥", "60–75 min",
        "DigitalWallet",
        [
            ("Wallet ops", [
                "User registers **wallet** linked to **bank account**.",
                "**Add money** from bank → wallet balance.",
                "**Transfer** P2P: debit source, credit destination, **transaction** record.",
                "Failed/invalid transfer rolls back or marks FAILED.",
            ]),
        ],
        [
            ("User", "Identity", "name, bank link"),
            ("Wallet", "Balance store", "walletId, balance"),
            ("BankAccount", "External rail", "account mask"),
            ("Transaction", "Immutable log", "type, status, amount"),
            ("WalletService", "API", "register, addMoney, transfer"),
        ],
        [
            "User **1—1** Wallet; WalletService writes Transaction per transfer",
        ],
    ),
    LldTopic(
        "16", "16_elevator_system_lld", "Elevator System", "Hard 🔥", "60–75 min",
        "Elevator Management System",
        [
            ("Serve request", [
                "Passenger presses **external** (up/down) or **internal** (floor) button.",
                "**ElevatorController** assigns elevator (nearest strategy).",
                "Elevator moves floor-by-floor; updates **direction** and **state**.",
                "Doors open at requested floors; queue drained.",
            ]),
        ],
        [
            ("ElevatorController", "Building brain", "elevator pool"),
            ("Elevator", "Car", "current floor, direction, state, requests"),
            ("ExternalButton / InternalButton", "Inputs", "floor, direction"),
            ("Display", "UI", "floor indicator"),
            ("ElevatorAssignmentStrategy", "Dispatch", "nearest idle"),
        ],
        [
            "ElevatorController **1—*** Elevator",
            "Each Elevator maintains pending up/down stops",
        ],
    ),
    LldTopic(
        "17", "17_logging_framework_lld", "Logging Framework", "Medium ⚡", "45–60 min",
        "Logger",
        [
            ("Log event", [
                "App calls **`LoggerFactory.getLogger(name)`** (hierarchical name, e.g. `com.shop.order.payment`).",
                "Factory resolves **parent** chain up to `ROOT` and caches loggers.",
                "Logger checks **effective level** (own level, else parent, else global default).",
                "`Logger.log` builds **LogEvent** → **`LoggerFactory.publish`** collects appenders (own + parents if additive).",
                "Each **Appender** writes the event (console, file, in-memory, error console).",
            ]),
        ],
        [
            ("LoggerFactory", "Static facade / registry", "loggerCache, rootLogger, getLogger, publish, collectAppenders"),
            ("Logger", "Named node", "level, parent, appenders, additive, trace…fatal API"),
            ("LogEvent", "Immutable payload", "timestamp, level, loggerName, message, thread"),
            ("Appender", "Sink interface", "append(LogEvent)"),
            ("ConsoleAppender / FileAppender / ErrorConsoleAppender / InMemoryAppender", "Concrete sinks", ""),
            ("LogLevel", "Severity + filter", "TRACE…FATAL, isEnabledFor"),
        ],
        [
            "LoggerFactory **1—*** many Logger (cached by name); parent links form a tree to ROOT",
            "Logger **1—*** Appender (own list); **additive** controls walking up to parent appenders",
            "Logger.log → LoggerFactory.publish → Appender.append(LogEvent)",
        ],
        ["LoggerService removed — registry and publish live on LoggerFactory (static facade)"],
    ),
    LldTopic(
        "18", "18_stack_overflow_lld", "Stack Overflow (Q&A)", "Hard 🔥", "60–75 min",
        "Stack Overflow",
        [
            ("Q&A flow", [
                "User posts **question** with **tags**.",
                "Others post **answers** and **comments**.",
                "Voting (up/down) on posts; reputation side-effect (optional).",
                "Question author **accepts** one answer.",
            ]),
        ],
        [
            ("User", "Member", "reputation"),
            ("Question", "Thread root", "title, body, tags, status"),
            ("Answer", "Response", "body, votes"),
            ("Comment", "Short reply", "on question or answer"),
            ("Vote", "Feedback", "type, voter"),
            ("Tag", "Topic label", "name"),
            ("StackOverflowService", "Facade", "post, vote, accept"),
        ],
        [
            "Question **1—*** Answer; Post hierarchy for comments",
            "Vote attached to Question or Answer",
        ],
    ),
    LldTopic(
        "19", "19_ride_booking_system_lld", "Ride Booking System (Uber-style)", "Hard 🔥", "60–75 min",
        "Uber",
        [
            ("Book ride", [
                "Rider sets **source** and **destination**; sees **price** (pricing strategy).",
                "Rider confirms → **ride** created.",
                "System assigns **nearest driver** (assignment strategy).",
                "Driver accepts → trip in progress → completed; **ride status** updates.",
            ]),
        ],
        [
            ("User / Customer", "Rider", "ride history"),
            ("Driver", "Supply", "location, isFree"),
            ("Ride", "Trip", "source, dest, price, status"),
            ("Location", "Geo", "lat, lon"),
            ("PricingStrategy", "Fare", "fixed, surge, …"),
            ("DriverAssignmentStrategy", "Match", "nearest driver"),
            ("RideBookingService", "Orchestration", "estimate, book, assign"),
        ],
        [
            "Ride **2—** Location; Ride **1—1** Customer and Driver",
            "RideBookingService composes pricing + assignment strategies",
        ],
    ),
    LldTopic(
        "20", "20_traffic_control_system_lld", "Traffic Control System", "Medium ⚡", "45–60 min",
        "Traffic Control System",
        [
            ("Signal cycle", [
                "**Intersection** has multiple **traffic signals** (directions).",
                "**Signal strategy** (e.g. round-robin) picks next green phase.",
                "Only compatible signals green; others red/yellow.",
                "**Traffic mode** (normal vs peak) adjusts **timing**.",
            ]),
        ],
        [
            ("Intersection", "Junction", "2/3/4-way, signals"),
            ("TrafficSignal", "Light group", "direction, current color"),
            ("SignalColor", "Phase", "RED, YELLOW, GREEN"),
            ("SignalTiming / Phase", "Duration config", "per mode"),
            ("SignalStrategy", "Scheduler", "RoundRobin, …"),
            ("TrafficControlService", "Runner", "start cycle, switch phases"),
        ],
        [
            "Intersection **1—*** TrafficSignal",
            "TrafficControlService applies SignalStrategy so one conflicting path is green",
        ],
    ),
]


def render_flows(flows: list[tuple[str, list[str]]]) -> str:
    parts: list[str] = []
    for i, (name, steps) in enumerate(flows, 1):
        parts.append(f"### 1.{i} {name}\n")
        for j, step in enumerate(steps, 1):
            parts.append(f"{j}. {step}")
        parts.append("")
    return "\n".join(parts).rstrip()


def render_entities(entities: list[tuple[str, str, str]]) -> str:
    lines = [
        "| Entity | Responsibility | Key fields / collaborators |",
        "|--------|----------------|----------------------------|",
    ]
    for name, resp, fields in entities:
        lines.append(f"| **{name}** | {resp} | {fields} |")
    return "\n".join(lines)


BLANK_IMPLEMENTATION = ""

def render_code(topic: LldTopic) -> str:
    if not topic.project:
        return BLANK_IMPLEMENTATION
    files = read_java_sources_ordered(LLD_ROOT / topic.project)
    if not files:
        return "_Companion project folder exists but no `src/*.java` files found._\n"

    lines = [
        f"Reference implementation from **`LLD/{topic.project}/`** (all sources in this file).",
        "",
        "Classes in **logical order**: enums → interfaces → domain → strategies → services → `Main`.",
        "",
        "**Run:**",
        "```bash",
        f"cd LLD/{topic.project}",
        "javac src/*.java",
        "java -cp src Main",
        "```",
        "",
    ]
    for name, source in files:
        lines.append(f"### `{name}`\n")
        lines.append("```java")
        lines.append(source.rstrip("\n"))
        lines.append("```\n")
    return "\n".join(lines)


def render_doc(topic: LldTopic) -> str:
    code_status = "✅ Reference Java included (§3)" if topic.project else ""
    companion = f"`LLD/{topic.project}`" if topic.project else ""
    notes = topic.design_notes
    notes_block = ""
    if notes:
        notes_block = "\n### Design notes\n\n" + "\n".join(f"- {n}" for n in notes) + "\n"

    rel_block = "\n".join(f"- {r}" for r in topic.relationships)

    diagram_block = ""
    if topic.project:
        diagram_md, _ = build_diagram_for_project(
            topic.project, topic.slug, ASSETS_ROOT, render_svg=False
        )
        if diagram_md:
            diagram_block = diagram_md + "\n"

    meta_lines = [
        f"**Difficulty:** {topic.difficulty}  ",
        f"**Interview duration:** {topic.duration}  ",
    ]
    if code_status:
        meta_lines.append(f"**Code status:** {code_status}  ")
    if companion:
        meta_lines.append(f"**Companion code:** {companion}  ")
    meta_block = "\n".join(meta_lines)

    return f"""# Low-Level Design: {topic.title}

{meta_block}

---

## How to present in an interview

Present in this order — interviewers expect **flow first**, then **model**, then **code**:

1. **Core flow** — main use cases as numbered steps (happy path + key branches).
2. **Entities & relationships** — nouns and verbs from the flow; who owns whom.
3. **Reference implementation** — classes that map 1:1 to the model (only after flow is agreed).

Do not open with a class diagram or code dumps before the flow is clear.

---

## 1. Core flow

{render_flows(topic.flows)}

---

## 2. Entities & relationships

_Deduced from the flows above — each entity should appear in at least one step._

{render_entities(topic.entities)}

### Relationships

{rel_block}
{notes_block}
{diagram_block}---

## 3. Reference implementation (Java)

{render_code(topic)}
"""


def main() -> None:
    old_suffixes = [
        "01_parking_lot_system.md",
        "02_movie_ticket_booking_system.md",
        "03_chess_game.md",
        "04_customer_issue_resolution_system.md",
        "05_dunzo_delivery_system.md",
        "06_restaurant_order_rating_system.md",
        "07_meeting_scheduler.md",
        "08_splitwise.md",
        "09_order_inventory_management.md",
        "10_text_editor.md",
        "11_vending_machine.md",
        "12_atm_machine.md",
        "13_tic_tac_toe.md",
        "14_car_rental_system.md",
        "15_digital_wallet.md",
        "16_elevator_system.md",
        "17_logging_framework.md",
        "18_stack_overflow.md",
        "19_ride_booking_system.md",
        "20_traffic_control_system.md",
    ]

    for topic in TOPICS:
        path = OUT_DIR / f"{topic.slug}.md"
        path.write_text(render_doc(topic), encoding="utf-8")
        print(f"Wrote {path.name}")

    code_dir = OUT_DIR / "code"
    if code_dir.is_dir():
        import shutil
        shutil.rmtree(code_dir)
        print("Removed questions/low_level_design/code/ (sources live in each *_lld.md)")

    for old in old_suffixes:
        old_path = OUT_DIR / old
        if old_path.exists():
            old_path.unlink()
            print(f"Removed {old}")

    print("Done.")


if __name__ == "__main__":
    main()
