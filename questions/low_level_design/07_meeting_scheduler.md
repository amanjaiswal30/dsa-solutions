# Low-Level Design: Meeting Scheduler

**Difficulty:** Medium ⚡

**Interview Duration:** 45–60 minutes

---

## 📋 Interview Approach

Design a **calendar / meeting scheduler** (Calendly-style or Outlook-style subset): users define **availability**, **bookable slots**, **invitees**, **conflict rules**, and **notifications**—without building full video or email servers.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says

*"Design a meeting scheduler where a host shares availability, invitees pick a slot, the system prevents double-booking, sends invites, and supports time zones and recurring meetings."*

### Clarifying Questions

1. **Single vs multi-user:** One host’s public link vs org-wide scheduling?  
2. **Duration:** Fixed length meetings vs host picks per booking?  
3. **Buffers:** Travel time between meetings?  
4. **Recurring:** RRULE-style or fixed weekly pattern?  
5. **Time zones:** Store in UTC; display in user zone?  
6. **Resources:** Optional room/calendar resource conflicts?  
7. **Reminders:** Email/push — out of scope as black-box `NotificationService`?

---

## 🔹 Step 2: Requirements

### Functional

1. **User** maintains **working hours** and **blocked** times (busy).  
2. **Host** creates a **schedulable event type** (e.g. 30-min intro, timezone default).  
3. **Invitee** sees **free slots** = intersection of (host availability) − (existing meetings) − (buffers).  
4. **Book** slot → creates **Meeting** with unique id, **ICS**-friendly fields (start, end, attendees).  
5. **Conflict detection:** no overlapping meetings for same host (and same room if modeled).  
6. **Cancel / reschedule** with notifications.  
7. **Recurring:** expand occurrences in a window (materialize or on-the-fly).

### Non-functional

- **Correctness:** No double-book under concurrency (same host).  
- **Performance:** Slot computation for N days in O(slots × log meetings) with sorted interval structure.

---

## 🔹 Step 3: Core Model

| Entity | Fields / notes |
|--------|----------------|
| `User` | id, defaultTimeZone, workingHours (weekly template) |
| `EventType` | hostId, durationMinutes, bufferBefore/After, title |
| `Meeting` | id, hostId, startUtc, endUtc, attendeeEmails, status (SCHEDULED/CANCELLED) |
| `BusyBlock` | optional explicit blocks (PTO) |
| `RecurrenceRule` | freq, until, byDay — expand to `MeetingInstance` or virtual |

### Slot generation

1. Load host’s **busy** intervals: existing meetings ∪ blocks.  
2. Merge overlapping busy intervals.  
3. Walk candidate days; within working hours, emit candidate starts every `duration + buffers`.  
4. Subtract any candidate that overlaps merged busy.

**Data structures:** `TreeMap` or sorted list of busy intervals by start time; binary search for overlap checks.

---

## 🔹 Step 4: Concurrency

- **Transaction:** `SELECT ... FOR UPDATE` on host’s row or **advisory lock** keyed by `hostId` when inserting meeting.  
- Alternatively **optimistic**: insert with unique constraint on `(host_id, start_utc)` if schema allows discrete slots only.

---

## 🔹 Step 5: Patterns

- **Value objects:** `TimeRange`, `ZonedInstant` wrappers to avoid timezone bugs.  
- **Strategy:** `AvailabilityCalculator` pluggable for “round robin hosts” (panel interviews).  
- **Factory:** `Meeting.fromBooking(slot, attendees)`.

---

## 🔹 Step 6: API sketch

```
POST /event-types
GET  /hosts/{id}/slots?from=&to=&eventTypeId=
POST /bookings  { eventTypeId, startUtc, inviteeEmail }
PATCH /meetings/{id}  { rescheduleToUtc }
DELETE /meetings/{id}
```

---

## 🔹 Follow-ups

- **Group meetings:** multiple hosts — slot = intersection of all availabilities.  
- **Round-robin pool:** assign one of N sales reps.  
- **Google Calendar sync:** outbound push via background job.
