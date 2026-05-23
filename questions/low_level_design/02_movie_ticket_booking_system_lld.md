# Low-Level Design: Movie Ticket Booking System

**Difficulty:** Hard 🔥  
**Interview duration:** 60–75 min  
**Code status:** ✅ Reference Java included (§3)  
**Companion code:** `LLD/Movie Ticket Booking System`  

---

## How to present in an interview

Present in this order — interviewers expect **flow first**, then **model**, then **code**:

1. **Core flow** — main use cases as numbered steps (happy path + key branches).
2. **Entities & relationships** — nouns and verbs from the flow; who owns whom.
3. **Reference implementation** — classes that map 1:1 to the model (only after flow is agreed).

Do not open with a class diagram or code dumps before the flow is clear.

---

## 1. Core flow

### 1.1 Book show

1. User searches **movie** in a **city** (Location).
2. User picks a **show** (movie + time + screen layout).
3. User selects **seats**; system locks/checks availability.
4. User pays; **ticket** confirmed with seat map snapshot.

---

## 2. Entities & relationships

_Deduced from the flows above — each entity should appear in at least one step._

| Entity | Responsibility | Key fields / collaborators |
|--------|----------------|----------------------------|
| **User** | Customer | profile, booking history |
| **Movie** | Catalog item | title, genre, metadata |
| **Location** | City / theatre region | cityName |
| **Show** | Screening instance | movie, showTime, seats |
| **Seat / ShowSeat** | Bookable unit | seatNumber, seatType, isAvailable |
| **Ticket** | Confirmed booking | show, seats, status |
| **BookingService** | Use cases | search, hold seats, confirm |

### Relationships

- Location hosts many Show; Show has many Seat
- Ticket aggregates selected Seat(s) for one Show

### Class diagram

```mermaid
classDiagram
    class BookingService
    class Genre {
        <<enumeration>>
    }
    class Location
    class Main
    class Movie
    class Seat
    class Show
    class Ticket
    class TicketStatus {
        <<enumeration>>
    }
    class User
```

---

## 3. Reference implementation (Java)

Companion project: **`LLD/Movie Ticket Booking System/`**. Copies below for browsing on GitHub (logical order, not A–Z).

**Run locally:**
```bash
cd LLD/Movie Ticket Booking System
javac src/*.java
java -cp src Main
```

| # | Source |
|---|--------|
| 1 | [`Genre.java`](code/02_movie_ticket_booking_system_lld/Genre.java) |
| 2 | [`TicketStatus.java`](code/02_movie_ticket_booking_system_lld/TicketStatus.java) |
| 3 | [`Location.java`](code/02_movie_ticket_booking_system_lld/Location.java) |
| 4 | [`Movie.java`](code/02_movie_ticket_booking_system_lld/Movie.java) |
| 5 | [`Seat.java`](code/02_movie_ticket_booking_system_lld/Seat.java) |
| 6 | [`Show.java`](code/02_movie_ticket_booking_system_lld/Show.java) |
| 7 | [`Ticket.java`](code/02_movie_ticket_booking_system_lld/Ticket.java) |
| 8 | [`User.java`](code/02_movie_ticket_booking_system_lld/User.java) |
| 9 | [`BookingService.java`](code/02_movie_ticket_booking_system_lld/BookingService.java) |
| 10 | [`Main.java`](code/02_movie_ticket_booking_system_lld/Main.java) |

