# Low-Level Design: Course Registration System

**Difficulty:** Hard 🔥

**Interview Duration:** 60-90 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:

*"Design a course registration system for a university or online learning platform that manages catalogs, enrollment, waitlists, prerequisites, scheduling, grading, transcripts, and billing—while handling many students registering at once without double-booking or inconsistent state."*

### Clarifying Questions to Ask:

1. **Q:** Is this on-campus (physical rooms) only, online only, or hybrid?  
   **A:** Hybrid—sections may be in-person, online, or blended; classroom allocation applies where relevant.

2. **Q:** What is the unit of enrollment—course or section?  
   **A:** Students enroll in **sections** (specific time slot / instructor); the catalog is organized by **course** offering multiple sections per term.

3. **Q:** How are prerequisites represented?  
   **A:** Directed graph: course A requires B, C; co-requisites possible; minimum grade thresholds (e.g., C or better).

4. **Q:** What defines a schedule conflict?  
   **A:** Two enrolled sections overlap in time on the same calendar days (with configurable buffers, e.g., 10 minutes between classes).

5. **Q:** How should registration windows work?  
   **A:** Early (priority groups), regular, late—with possibly different fees or rules; drop/add has its own window.

6. **Q:** Concurrency expectations?  
   **A:** Thousands of students may hit “register” simultaneously for popular sections; **at-most-one seat** per capacity; no double enrollment in the same section; waitlist must be fair (FIFO or priority).

7. **Q:** Grading and transcripts?  
   **A:** Letter grades or points; incomplete/withdrawn states; transcript is immutable snapshot or regenerated from enrollments.

8. **Q:** Payment scope?  
   **A:** Tuition per credit or flat rate per term; scholarships/discounts as line items; payment may be prerequisite for “confirmed” enrollment (policy flag).

9. **Q:** Professor and room assignment?  
   **A:** Each section has assigned faculty; rooms have capacity and equipment; allocator must respect room capacity ≥ section enrollment cap.

10. **Q:** Search and scale?  
    **A:** Filter by department, credits, time, modality, instructor; pagination; index-friendly queries at scale.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

#### Catalog & Scheduling (FR1–FR8)

1. System shall maintain **courses** (code, title, description, credit hours, department).
2. System shall maintain **sections** per **term** (section id, course ref, capacity, modality, meeting pattern).
3. System shall store **schedule** for each section: recurring meetings (day-of-week, start/end time, timezone) or explicit session list for irregular courses.
4. System shall support **professor assignment** to sections (primary instructor; optional secondary).
5. System shall support **classroom allocation** with capacity and features (lab, projector); section room must satisfy capacity and conflict rules.
6. System shall support **course search and filtering** by keyword, department, credits, term, modality, instructor, time range, and “seats available.”
7. System shall expose catalog data as read-heavy APIs with consistent snapshot semantics during registration spikes.
8. System shall allow administrators to open/close sections and adjust capacity subject to business rules.

#### Enrollment & Capacity (FR9–FR16)

9. Students shall **enroll** in a section when eligible.
10. System shall enforce **seat limits**; when full, eligible students join a **waitlist** (ordered).
11. System shall **promote** from waitlist when a seat opens (automated job or synchronous on drop).
12. System shall enforce **max credits per term** per student program/rules.
13. System shall enforce **prerequisite** and **co-requisite** rules using completed and in-progress enrollments.
14. System shall detect **schedule conflicts** among a student’s enrolled sections for the same term.
15. System shall respect **registration periods**: early, regular, late—each with eligibility and optional fee multipliers.
16. System shall support **drop/add** within policy windows; drops may free seats and trigger waitlist promotion.

#### Academic Record & Billing (FR17–FR24)

17. System shall support **grade entry** (letter/numeric), **incomplete**, **withdrawn**, and **audit** modes per policy.
18. System shall generate **transcripts**: chronological terms, courses, credits, grades, GPA summary.
19. System shall compute **tuition** from registered credits and rules (resident, program, late registration surcharge).
20. System shall apply **payment status** to enrollment state if required (e.g., hold until paid).
21. System shall prevent duplicate enrollment in the **same section** and duplicate enrollment in **multiple sections of the same course** in one term unless policy allows retake.
22. System shall log **audit trails** for enrollment changes, grade changes, and administrative overrides.
23. System shall support **concurrent registration** without **double-booking** the last seat or corrupting waitlist order.
24. System shall provide **idempotent** registration requests (retry-safe) using client request tokens or idempotency keys.

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Scalability Analysis**

**Think:** "How many students, sections, and concurrent registrations?"

- Large university: 30k–50k students; 5k–15k sections per term.
- Peak: opening day registration—**10k+ RPS** on hot sections; burst traffic on popular CS electives.
- Catalog reads dominate writes; enrollment writes are bursty per section.

**Deduced NFRs:**

- ✅ Horizontal scaling of **stateless** API tier; **sharded** or partitioned data by `termId` / `studentId`.
- ✅ **Caching** for catalog (short TTL + invalidation on admin change).
- ✅ **Per-section serialization** of seat mutation (not one global lock for the whole system).
- ✅ Async **waitlist promotion** and notification to avoid blocking HTTP threads.

---

#### 2. **Consistency Analysis**

**Think:** "What must be exact?"

- Seat count vs. actual enrollments for a section.
- Waitlist ordering when multiple threads compete.
- Prerequisite evaluation vs. transcript source of truth.
- No two students both “granted” the last seat.

**Deduced NFRs:**

- ✅ **Strong consistency** for seat acquisition (transaction + row-level lock or atomic compare-and-swap).
- ✅ **Serializable or repeatable-read** isolation for “check conflict + check credits + enroll” bundle.
- ✅ **Eventual consistency** acceptable for search indexes and analytics if bounded lag is disclosed.
- ✅ **Idempotency** for retries after timeouts.

---

#### 3. **Availability Analysis**

**Think:** "Can registration go down during peak?"

- High availability expected during registration windows; read-only degradation preferred over wrong enrollments.

**Deduced NFRs:**

- ✅ **99.9%+** during registration weeks for core APIs.
- ✅ **Graceful degradation:** disable non-critical features (recommendations) under load; keep enroll/drop/critical path.
- ✅ **Circuit breakers** on downstream services (payment, notification).
- ✅ **Read replicas** for catalog; **primary** for seat mutations.

---

#### 4. **Maintainability Analysis**

**Think:** "Rules change every semester."

**Deduced NFRs:**

- ✅ **Policy engine** or strategy objects for registration period rules, credit caps, and prerequisite interpretation.
- ✅ **Feature flags** for payment-hold behavior and retake rules.
- ✅ Structured **logging** (correlation id, studentId, sectionId, termId).
- ✅ **Integration tests** for concurrency scenarios (last seat, waitlist race).

---

#### 5. **Performance Analysis**

**Think:** "SLAs for search vs. enroll?"

- Search: p95 **< 200ms** with pagination.
- Enroll: p95 **< 500ms** including validation; hot path O(k) where k = student’s enrollments in term (bounded, e.g. < 25).

**Deduced NFRs:**

- ✅ **Indexed** queries on `(termId, courseId)`, `(studentId, termId)`.
- ✅ **In-memory interval index** optional per student session for conflict checks after bulk fetch.
- ✅ Avoid N+1: batch-load sections and schedules for a term when validating cart/checkout.

---

#### 6. **Security Analysis**

**Think:** "Who can change grades or enroll someone else?"

**Deduced NFRs:**

- ✅ **RBAC:** student, registrar, instructor, bursar.
- ✅ **Authorization** on every enroll/drop/grade mutation.
- ✅ **Audit** on grade changes and override of prerequisites.
- ✅ **Rate limiting** per student to reduce scripted abuse.

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Step 1: Noun Extraction

| Requirement | Nouns |
|-------------|-------|
| "Course catalog" | Course, Department, CreditHours |
| "Sections, schedule" | Section, Term, Meeting, Schedule, TimeSlot |
| "Student enrollment" | Student, Enrollment, RegistrationCart |
| "Capacity, waitlist" | Capacity, Waitlist, WaitlistEntry |
| "Prerequisites" | PrerequisiteRule, CourseGraph, GradeRecord |
| "Schedule conflict" | ConflictDetector, CalendarInterval |
| "Registration periods" | RegistrationWindow, RegistrationPhase |
| "Search, filter" | SearchQuery, CatalogIndex |
| "Professor assignment" | Instructor, Assignment |
| "Classroom allocation" | Classroom, RoomAllocation |
| "Credit limits" | CreditLimitPolicy, Program |
| "Drop/add" | DropAddWindow, Withdrawal |
| "Grades, transcript" | Grade, Transcript, TermRecord |
| "Tuition" | Invoice, TuitionRate, Payment |
| "Concurrency" | SeatHold, LockToken, EnrollmentTransaction |

### Step 2: Entity Validation

| Noun | Entity? | Reasoning |
|------|---------|-----------|
| CreditHours | ❌ NO | Attribute of Course |
| TimeSlot | ✅ YES | Value object / embeddable for meetings |
| RegistrationCart | ✅ YES | Session-scoped aggregate before commit |
| CalendarInterval | ❌ NO | Value type used by detector |
| CatalogIndex | ❌ NO | Infrastructure (search index) |
| LockToken | ❌ NO | Implementation detail of concurrency |
| CourseGraph | ❌ NO | Derived structure from PrerequisiteRule rows |
| Withdrawal | ✅ YES | Domain event / record with W date |

### Final Entity List

**Catalog & Term:**

1. **Department**
2. **Course**
3. **Term** (e.g., Fall 2026)
4. **Section**
5. **Meeting** (section schedule occurrence pattern)
6. **Classroom**

**People & Roles:**

7. **Student**
8. **Instructor** (Faculty)
9. **Program** (major/track for credit rules)

**Registration:**

10. **RegistrationWindow** (phase: EARLY, REGULAR, LATE; start/end)
11. **Enrollment** (student, section, status: REGISTERED, WAITLISTED, DROPPED, WITHDRAWN, COMPLETED)
12. **WaitlistEntry** (position, timestamp, optional priority score)
13. **RegistrationCart** (draft selections before checkout)

**Rules & Policies:**

14. **PrerequisiteRule** (prerequisite course, min grade, co-req flag)
15. **CreditLimitPolicy** (max credits per term by program/standing)
16. **RetakePolicy** (allow/deny duplicate course in term)

**Allocation:**

17. **SectionAssignment** (instructor ↔ section)
18. **RoomAllocation** (classroom ↔ section)

**Academic Record & Billing:**

19. **GradeRecord** (enrollment, grade, recordedBy, timestamp)
20. **Transcript** (aggregate view; materialized report or computed)
21. **Invoice** / **TuitionLineItem**
22. **Payment** (status, amount, reference)

**Services (core components):**

23. **RegistrationService** (orchestrates enroll/drop)
24. **PrerequisiteValidator**
25. **ScheduleConflictDetector**
26. **SeatManager** (capacity + waitlist + concurrency)
27. **TuitionCalculator**

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Pass 1: Catalog Relationships

#### Department ↔ Course

**Conclusion:** **Composition/Aggregation** (department owns course taxonomy)

```
Department ◇────→ Course [1..*]
```

#### Course ↔ Section

**Conclusion:** **Composition** (sections belong to a course offering in a term)

```
Course ◆────→ Section [0..*]  (per Term)
Term ◆────→ Section [1..*]
```

#### Section ↔ Meeting

**Conclusion:** **Composition**

```
Section ◆────→ Meeting [1..*]
```

#### Section ↔ Classroom

**Conclusion:** **Association** (optional until allocated)

```
Section ─────→ Classroom [0..1]
```

---

### Pass 2: People & Enrollment

#### Student ↔ Enrollment

**Conclusion:** **Association**

```
Student ◇────→ Enrollment [0..*]
Section ◇────→ Enrollment [0..*]
```

#### Enrollment ↔ WaitlistEntry

**Conclusion:** **Mutually exclusive states** for same student-section attempt (model as status on Enrollment or separate WaitlistEntry with unique constraint)

```
Section ◆────→ WaitlistEntry [0..*]
Student ─────→ WaitlistEntry [0..*]
```

#### Instructor ↔ Section

**Conclusion:** **Association** via SectionAssignment

```
Instructor ─────→ SectionAssignment ─────→ Section
```

---

### Pass 3: Academic & Billing

#### Enrollment ↔ GradeRecord

**Conclusion:** **1:1** after grading (per enrollment)

```
Enrollment ◆────→ GradeRecord [0..1]
```

#### Student ↔ Transcript

**Conclusion:** **Derived aggregate** (not a single row—collection of term records)

```
Student ─────→ TermRecord [0..*] ─────→ CourseOutcome
```

#### Student ↔ Invoice

**Conclusion:** **1:N** per term

```
Student ─────→ Invoice [0..*] (per Term)
```

---

### Cardinality Summary

| Relationship | Cardinality | Type |
|--------------|-------------|------|
| Department → Course | 1:N | Aggregation |
| Course → Section (in Term) | 1:N | Composition |
| Term → Section | 1:N | Composition |
| Section → Meeting | 1:N | Composition |
| Section → Classroom | N:1 | Association |
| Student → Enrollment | 1:N | Association |
| Section → Enrollment | 1:N | Association |
| Section → WaitlistEntry | 1:N | Composition |
| Instructor → Section | M:N | Via SectionAssignment |
| Enrollment → GradeRecord | 1:1 | Composition |
| Student → Invoice | 1:N | Association |

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: Enums

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>     │  │ <<enumeration>>     │  │ <<enumeration>>     │
│ RegistrationPhase   │  │ EnrollmentStatus    │  │ SectionModality     │
├─────────────────────┤  ├─────────────────────┤  ├─────────────────────┤
│ EARLY               │  │ REGISTERED          │  │ IN_PERSON           │
│ REGULAR             │  │ WAITLISTED          │  │ ONLINE              │
│ LATE                │  │ DROPPED             │  │ HYBRID              │
│ CLOSED              │  │ WITHDRAWN           │  └─────────────────────┘
└─────────────────────┘  │ COMPLETED           │
                         │ AUDIT               │
                         └─────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐
│ <<enumeration>>     │  │ <<enumeration>>     │
│ GradeValue          │  │ PaymentStatus       │
├─────────────────────┤  ├─────────────────────┤
│ A,B,C,D,F           │  │ UNPAID              │
│ P,NP (pass/fail)    │  │ PARTIAL             │
│ I (incomplete)      │  │ PAID                │
│ W (withdrawn)       │  │ WAIVED              │
└─────────────────────┘  └─────────────────────┘
```

---

### Class Diagram 2: Catalog Core

```
┌──────────────────────────────────────────────────────────────────┐
│ Course                                                           │
├──────────────────────────────────────────────────────────────────┤
│ - courseId: String                                               │
│ - code: String            (e.g., "CS-101")                       │
│ - title: String                                                  │
│ - description: String                                            │
│ - creditHours: int                                               │
│ - departmentId: String                                           │
├──────────────────────────────────────────────────────────────────┤
│ + getPrerequisites(): List<PrerequisiteRule>                     │
└──────────────────────────────────────────────────────────────────┘
                              │
                              │ 1
                              ▼
                              * 
┌──────────────────────────────────────────────────────────────────┐
│ Section                                                          │
├──────────────────────────────────────────────────────────────────┤
│ - sectionId: String                                              │
│ - termId: String                                                 │
│ - courseId: String                                               │
│ - capacity: int                                                  │
│ - enrolledCount: int            (derived or denormalized)        │
│ - modality: SectionModality                                      │
│ - status: SectionStatus (OPEN, CLOSED, CANCELLED)                │
│ - primaryInstructorId: String                                    │
│ - classroomId: String (optional)                                 │
├──────────────────────────────────────────────────────────────────┤
│ + getMeetings(): List<Meeting>                                   │
│ + isFull(): boolean                                              │
└──────────────────────────────────────────────────────────────────┘
         │
         │ 1
         ▼
         *
┌──────────────────────────────────────────────────────────────────┐
│ Meeting                                                          │
├──────────────────────────────────────────────────────────────────┤
│ - dayOfWeek: DayOfWeek                                           │
│ - start: LocalTime                                               │
│ - end: LocalTime                                                 │
│ - timezone: ZoneId                                               │
│ - startDate: LocalDate  (inclusive)                              │
│ - endDate: LocalDate    (inclusive)                                │
├──────────────────────────────────────────────────────────────────┤
│ + toIntervalsWithin(termBounds): List<TimeInterval>              │
└──────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 3: Enrollment & Waitlist

```
┌──────────────────────────────────────────────────────────────────┐
│ Enrollment                                                       │
├──────────────────────────────────────────────────────────────────┤
│ - enrollmentId: String                                           │
│ - studentId: String                                              │
│ - sectionId: String                                              │
│ - termId: String                                                 │
│ - status: EnrollmentStatus                                       │
│ - registeredAt: Instant                                          │
│ - idempotencyKey: String (unique)                                │
├──────────────────────────────────────────────────────────────────┤
│ + drop(at: Instant, reason: String): void                        │
│ + completeWithGrade(g: GradeRecord): void                        │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ WaitlistEntry                                                    │
├──────────────────────────────────────────────────────────────────┤
│ - entryId: String                                                │
│ - studentId: String                                              │
│ - sectionId: String                                              │
│ - position: int               (logical; use tie-break timestamp) │
│ - createdAt: Instant                                               │
├──────────────────────────────────────────────────────────────────┤
│ + compareOrder(other): int    (FIFO)                             │
└──────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 4: Validation & Concurrency Services

```
┌──────────────────────────────────────────────────────────────────┐
│ <<interface>>                                                    │
│ PrerequisiteValidator                                            │
├──────────────────────────────────────────────────────────────────┤
│ + validate(studentId, courseId, termId): ValidationResult          │
└──────────────────────────────────────────────────────────────────┘
                         △
                         │
              ┌──────────┴──────────┐
              │ DefaultPrerequisite │
              │ Validator           │
              └─────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ <<interface>>                                                    │
│ ScheduleConflictDetector                                         │
├──────────────────────────────────────────────────────────────────┤
│ + conflicts(sections: List<SectionScheduleView>): Optional<Conflict>   │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ SeatManager                                                      │
├──────────────────────────────────────────────────────────────────┤
│ - sectionRepository: SectionRepository                           │
│ - enrollmentRepository: EnrollmentRepository                     │
│ - waitlistRepository: WaitlistRepository                         │
├──────────────────────────────────────────────────────────────────┤
│ + tryEnroll(studentId, sectionId, txn): SeatResult               │
│ + releaseSeatAndPromoteWaitlist(sectionId, txn): void            │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ RegistrationService                                            │
├──────────────────────────────────────────────────────────────────┤
│ - seatManager: SeatManager                                       │
│ - prereqValidator: PrerequisiteValidator                         │
│ - conflictDetector: ScheduleConflictDetector                     │
│ - windowService: RegistrationWindowService                         │
│ - creditPolicy: CreditLimitPolicy                                  │
│ - tuitionCalculator: TuitionCalculator                             │
├──────────────────────────────────────────────────────────────────┤
│ + register(studentId, sectionId, idempotencyKey): RegisterResult │
│ + drop(studentId, enrollmentId): void                            │
│ + addToWaitlist(studentId, sectionId): WaitlistResult            │
└──────────────────────────────────────────────────────────────────┘
```

---

### Class Diagram 5: Grades, Transcript, Tuition

```
┌──────────────────────────────────────────────────────────────────┐
│ GradeRecord                                                      │
├──────────────────────────────────────────────────────────────────┤
│ - enrollmentId: String                                           │
│ - grade: GradeValue                                              │
│ - points: BigDecimal                                             │
│ - recordedBy: String                                             │
│ - recordedAt: Instant                                            │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ TranscriptService                                                │
├──────────────────────────────────────────────────────────────────┤
│ + buildTranscript(studentId): TranscriptDTO                      │
│ + termGpa(studentId, termId): BigDecimal                         │
│ + cumulativeGpa(studentId): BigDecimal                           │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ TuitionCalculator                                                │
├──────────────────────────────────────────────────────────────────┤
│ + compute(termId, studentId, enrollments): Invoice                │
└──────────────────────────────────────────────────────────────────┘
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Value Objects: Time Interval & Conflict Detection

```java
// TimeInterval.java — closed-open [start, end) in a single logical timeline
import java.time.*;

public final class TimeInterval {
    private final Instant start;
    private final Instant end;

    public TimeInterval(Instant start, Instant end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
        this.start = start;
        this.end = end;
    }

    /** Optional gap buffer (e.g., 10 minutes) modeled by shrinking intervals or expanding checks */
    public boolean overlaps(TimeInterval other, Duration buffer) {
        Instant s1 = this.start.minus(buffer);
        Instant e1 = this.end.plus(buffer);
        Instant s2 = other.start.minus(buffer);
        Instant e2 = other.end.plus(buffer);
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
}
```

```java
// SectionScheduleView.java — pre-expanded meetings for a term (cached per request)
import java.util.*;

public final class SectionScheduleView {
    private final String sectionId;
    private final List<TimeInterval> intervals;

    public SectionScheduleView(String sectionId, List<TimeInterval> intervals) {
        this.sectionId = sectionId;
        this.intervals = List.copyOf(intervals);
    }

    public String getSectionId() { return sectionId; }
    public List<TimeInterval> intervals() { return intervals; }
}
```

```java
// ScheduleConflictDetector.java
import java.time.Duration;
import java.util.*;

public interface ScheduleConflictDetector {
    Optional<Conflict> conflicts(List<SectionScheduleView> views);

    record Conflict(String sectionA, String sectionB, TimeInterval intervalA, TimeInterval intervalB) {}
}

public class DefaultScheduleConflictDetector implements ScheduleConflictDetector {

    private final Duration bufferBetweenClasses;

    public DefaultScheduleConflictDetector(Duration bufferBetweenClasses) {
        this.bufferBetweenClasses = bufferBetweenClasses;
    }

    @Override
    public Optional<ScheduleConflictDetector.Conflict> conflicts(List<SectionScheduleView> views) {
        List<IndexedInterval> flat = new ArrayList<>();
        for (SectionScheduleView v : views) {
            for (TimeInterval i : v.intervals()) {
                flat.add(new IndexedInterval(v.getSectionId(), i));
            }
        }
        flat.sort(Comparator.comparing(ii -> ii.interval.getStart()));

        for (int i = 0; i < flat.size(); i++) {
            for (int j = i + 1; j < flat.size(); j++) {
                if (flat.get(j).interval.getStart().isAfter(flat.get(i).interval.getEnd().plus(bufferBetweenClasses))) {
                    break;
                }
                IndexedInterval a = flat.get(i);
                IndexedInterval b = flat.get(j);
                if (!a.sectionId.equals(b.sectionId) &&
                    a.interval.overlaps(b.interval, bufferBetweenClasses)) {
                    return Optional.of(new ScheduleConflictDetector.Conflict(
                            a.sectionId, b.sectionId, a.interval, b.interval));
                }
            }
        }
        return Optional.empty();
    }

    private static final class IndexedInterval {
        final String sectionId;
        final TimeInterval interval;
        IndexedInterval(String sectionId, TimeInterval interval) {
            this.sectionId = sectionId;
            this.interval = interval;
        }
    }
}
```

---

### Prerequisite Validation (Topological + Grade Thresholds)

```java
// PrerequisiteRule.java
public class PrerequisiteRule {
    private final String courseId;          // owner course
    private final String prerequisiteCourseId;
    private final boolean coRequired;         // co-requisite in same term
    private final GradeValue minimumGrade;  // nullable = any pass

    public PrerequisiteRule(String courseId, String prerequisiteCourseId,
                            boolean coRequired, GradeValue minimumGrade) {
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
        this.coRequired = coRequired;
        this.minimumGrade = minimumGrade;
    }

    public String getCourseId() { return courseId; }
    public String getPrerequisiteCourseId() { return prerequisiteCourseId; }
    public boolean isCoRequired() { return coRequired; }
    public GradeValue getMinimumGrade() { return minimumGrade; }
}
```

```java
// DefaultPrerequisiteValidator.java — uses completed courses + current term enrollments
import java.util.*;

public class DefaultPrerequisiteValidator implements PrerequisiteValidator {

    public interface AcademicHistoryPort {
        boolean hasCompletedWithMinGrade(String studentId, String courseId, GradeValue min);
        boolean isEnrolledInTerm(String studentId, String termId, String courseId);
    }

    private final AcademicHistoryPort history;
    private final Map<String, List<PrerequisiteRule>> rulesByCourse;

    public DefaultPrerequisiteValidator(AcademicHistoryPort history,
                                        List<PrerequisiteRule> allRules) {
        this.history = history;
        this.rulesByCourse = new HashMap<>();
        for (PrerequisiteRule r : allRules) {
            rulesByCourse.computeIfAbsent(r.getCourseId(), k -> new ArrayList<>()).add(r);
        }
    }

    @Override
    public ValidationResult validate(String studentId, String courseId, String termId) {
        List<PrerequisiteRule> rules = rulesByCourse.getOrDefault(courseId, List.of());
        List<String> missing = new ArrayList<>();

        for (PrerequisiteRule r : rules) {
            String pre = r.getPrerequisiteCourseId();
            if (r.isCoRequired()) {
                boolean ok = history.isEnrolledInTerm(studentId, termId, pre)
                        || history.hasCompletedWithMinGrade(studentId, pre, r.getMinimumGrade());
                if (!ok) missing.add("Co-req/enrollment missing: " + pre);
            } else {
                boolean ok = history.hasCompletedWithMinGrade(studentId, pre, r.getMinimumGrade());
                if (!ok) missing.add("Prerequisite not met: " + pre);
            }
        }
        return missing.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(missing);
    }
}
```

---

### Seat Manager: Concurrency-Safe Last Seat

```java
// SeatResult.java
public sealed interface SeatResult {
    record Enrolled(String enrollmentId) implements SeatResult {}
    record Waitlisted(String waitlistEntryId, int position) implements SeatResult {}
    record Rejected(String reason) implements SeatResult {}
}
```

```java
// SeatManager.java — pseudocode aligned with DB transaction / SELECT FOR UPDATE
public class SeatManager {

    public interface UnitOfWork {
        <T> T execute(SeatTxn txn);
    }

    public interface SeatTxn {
        SectionRow lockSection(String sectionId);
        Optional<EnrollmentRow> findEnrollment(String studentId, String sectionId);
        EnrollmentRow insertEnrollment(String studentId, String sectionId, String termId, String idempotencyKey);
        void incrementEnrolledCount(String sectionId);
        int countEnrolled(String sectionId);
        WaitlistRow insertWaitlist(String studentId, String sectionId);
        int waitlistSize(String sectionId);
    }

    private final UnitOfWork uow;

    public SeatManager(UnitOfWork uow) {
        this.uow = uow;
    }

    /**
     * Must run inside Serializable or Repeatable Read transaction.
     * 1) Lock section row.
     * 2) Idempotency: return existing enrollment if key seen.
     * 3) If capacity left, insert REGISTERED and bump count.
     * 4) Else insert WAITLIST.
     */
    public SeatResult tryEnroll(String studentId, String sectionId, String termId, String idempotencyKey) {
        return uow.execute(txn -> {
            SectionRow sec = txn.lockSection(sectionId);

            EnrollmentRow existing = txn.findEnrollment(studentId, sectionId)
                    .orElse(null);
            if (existing != null) {
                if (idempotencyKey.equals(existing.getIdempotencyKey())) {
                    return new SeatResult.Enrolled(existing.getEnrollmentId());
                }
                return new SeatResult.Rejected("Already related to this section");
            }

            int enrolled = txn.countEnrolled(sectionId);
            if (enrolled < sec.getCapacity()) {
                EnrollmentRow row = txn.insertEnrollment(studentId, sectionId, termId, idempotencyKey);
                txn.incrementEnrolledCount(sectionId);
                return new SeatResult.Enrolled(row.getEnrollmentId());
            } else {
                WaitlistRow wl = txn.insertWaitlist(studentId, sectionId);
                return new SeatResult.Waitlisted(wl.getEntryId(), wl.getPosition());
            }
        });
    }

    /**
     * On drop: decrement count, then promote head of waitlist in same transaction.
     */
    public void releaseSeatAndPromoteWaitlist(String sectionId) {
        uow.execute(txn -> {
            txn.lockSection(sectionId);
            txn.decrementEnrolledCount(sectionId);
            WaitlistRow next = txn.pollWaitlistHead(sectionId);
            if (next != null) {
                EnrollmentRow row = txn.insertEnrollment(
                        next.getStudentId(), sectionId, next.getTermId(), next.getIdempotencyKey());
                txn.incrementEnrolledCount(sectionId);
                txn.removeWaitlistEntry(next.getEntryId());
                // emit domain event: WaitlistPromoted
            }
            return null;
        });
    }
}
```

**Interview note:** In SQL, `SELECT ... FROM sections WHERE id = ? FOR UPDATE` + unique constraint on `(student_id, section_id)` and on `idempotency_key` prevents double enrollment; **compare-and-swap** on `enrolled_count` is weaker—prefer **count of REGISTERED rows** or **reserved seats** as source of truth.

---

### Registration Service: Orchestration

```java
// RegistrationService.java
import java.time.*;
import java.util.*;

public class RegistrationService {

    private final SeatManager seatManager;
    private final PrerequisiteValidator prereqValidator;
    private final ScheduleConflictDetector conflictDetector;
    private final RegistrationWindowService windowService;
    private final CreditLimitPolicy creditPolicy;
    private final SectionCatalogPort catalog;
    private final EnrollmentQueryPort enrollments;

    public RegistrationService(SeatManager seatManager,
                               PrerequisiteValidator prereqValidator,
                               ScheduleConflictDetector conflictDetector,
                               RegistrationWindowService windowService,
                               CreditLimitPolicy creditPolicy,
                               SectionCatalogPort catalog,
                               EnrollmentQueryPort enrollments) {
        this.seatManager = seatManager;
        this.prereqValidator = prereqValidator;
        this.conflictDetector = conflictDetector;
        this.windowService = windowService;
        this.creditPolicy = creditPolicy;
        this.catalog = catalog;
        this.enrollments = enrollments;
    }

    public RegisterResult register(String studentId, String sectionId, String idempotencyKey) {
        SectionDTO sec = catalog.getSection(sectionId);
        String termId = sec.getTermId();
        String courseId = sec.getCourseId();

        if (!windowService.isOpen(studentId, termId, Instant.now())) {
            return RegisterResult.rejected("Registration window closed");
        }

        ValidationResult pre = prereqValidator.validate(studentId, courseId, termId);
        if (!pre.isOk()) {
            return RegisterResult.rejected(String.join("; ", pre.getMessages()));
        }

        List<SectionScheduleView> proposed = new ArrayList<>();
        proposed.add(catalog.getScheduleView(sectionId));
        for (String otherSectionId : enrollments.activeSectionIds(studentId, termId)) {
            proposed.add(catalog.getScheduleView(otherSectionId));
        }
        Optional<ScheduleConflictDetector.Conflict> c = conflictDetector.conflicts(proposed);
        if (c.isPresent()) {
            var x = c.get();
            return RegisterResult.rejected("Schedule conflict: " + x.sectionA() + " vs " + x.sectionB());
        }

        int creditsIfAdded = enrollments.sumCredits(studentId, termId) + sec.getCreditHours();
        if (!creditPolicy.allows(studentId, termId, creditsIfAdded)) {
            return RegisterResult.rejected("Exceeds credit limit for term");
        }

        SeatResult seat = seatManager.tryEnroll(studentId, sectionId, termId, idempotencyKey);
        if (seat instanceof SeatResult.Rejected r) {
            return RegisterResult.rejected(r.reason());
        }
        if (seat instanceof SeatResult.Waitlisted w) {
            return RegisterResult.waitlisted(w.waitlistEntryId(), w.position());
        }
        if (seat instanceof SeatResult.Enrolled e) {
            return RegisterResult.enrolled(e.enrollmentId());
        }
        throw new IllegalStateException();
    }
}
```

---

### Transcript & GPA (Simplified)

```java
// TranscriptService.java
import java.math.*;
import java.util.*;

public class TranscriptService {

    public interface EnrollmentHistoryPort {
        List<EnrollmentOutcome> findCompleted(String studentId);
    }

    private final EnrollmentHistoryPort history;

    public TranscriptService(EnrollmentHistoryPort history) {
        this.history = history;
    }

    public TranscriptDTO buildTranscript(String studentId) {
        List<EnrollmentOutcome> rows = history.findCompleted(studentId);
        rows.sort(Comparator.comparing(EnrollmentOutcome::getTermId));
        BigDecimal cumulativePoints = BigDecimal.ZERO;
        int cumulativeCredits = 0;
        List<TermLineDTO> lines = new ArrayList<>();
        String currentTerm = null;
        BigDecimal termPoints = BigDecimal.ZERO;
        int termCredits = 0;

        for (EnrollmentOutcome r : rows) {
            if (currentTerm != null && !currentTerm.equals(r.getTermId())) {
                lines.add(new TermLineDTO(currentTerm, termPoints, termCredits));
                termPoints = BigDecimal.ZERO;
                termCredits = 0;
            }
            currentTerm = r.getTermId();
            if (r.getGrade().isCountableForGpa()) {
                termPoints = termPoints.add(r.getGradePoints().multiply(BigDecimal.valueOf(r.getCreditHours())));
                termCredits += r.getCreditHours();
                cumulativePoints = cumulativePoints.add(r.getGradePoints().multiply(BigDecimal.valueOf(r.getCreditHours())));
                cumulativeCredits += r.getCreditHours();
            }
        }
        if (currentTerm != null) {
            lines.add(new TermLineDTO(currentTerm, termPoints, termCredits));
        }
        BigDecimal cgpa = cumulativeCredits == 0 ? BigDecimal.ZERO
                : cumulativePoints.divide(BigDecimal.valueOf(cumulativeCredits), 3, RoundingMode.HALF_UP);
        return new TranscriptDTO(studentId, lines, cgpa);
    }

    // DTOs / records omitted for brevity
}
```

---

### Tuition Calculation (Per Credit + Phase Surcharge)

```java
// TuitionCalculator.java
import java.math.*;
import java.util.*;

public class TuitionCalculator {

    public Invoice compute(String termId,
                           String studentId,
                           List<EnrollmentLine> registeredSections,
                           RegistrationPhase phase,
                           TuitionRateCard rateCard) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<LineItem> items = new ArrayList<>();
        for (EnrollmentLine line : registeredSections) {
            BigDecimal perCredit = rateCard.rateFor(studentId, line.getCourseCode());
            BigDecimal lineTotal = perCredit.multiply(BigDecimal.valueOf(line.getCreditHours()));
            items.add(new LineItem("TUITION_" + line.getSectionId(), lineTotal));
            subtotal = subtotal.add(lineTotal);
        }
        BigDecimal surcharge = rateCard.latePhaseSurcharge(phase, subtotal);
        if (surcharge.signum() > 0) {
            items.add(new LineItem("LATE_REG_SURCHARGE", surcharge));
        }
        return new Invoice(termId, studentId, items, subtotal.add(surcharge));
    }
}
```

---

### Demo Scenario (Concurrent Last Seat)

```java
// Conceptual test: two threads, one seat — only one ENROLLED, other WAITLIST or REJECTED
// Use database transaction tests or Testcontainers with real isolation level.
public class RegistrationConcurrencyDemo {
    public static void main(String[] args) throws Exception {
        // RegistrationService svc = ... wired with real UnitOfWork -> DataSource
        // ExecutorService pool = Executors.newFixedThreadPool(2);
        // CountDownLatch latch = new CountDownLatch(1);
        // Callable<RegisterResult> a = () -> { latch.await(); return svc.register("S1","SEC1","key-A"); };
        // Callable<RegisterResult> b = () -> { latch.await(); return svc.register("S2","SEC1","key-B"); };
        // Future<RegisterResult> fa = pool.submit(a);
        // Future<RegisterResult> fb = pool.submit(b);
        // latch.countDown();
        // assert one ENROLLED and one WAITLISTED for capacity=1
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Strategy Pattern

**Where:** `RegistrationWindowService` (eligibility by student standing), `TuitionRateCard` (resident vs. non-resident), optional `WaitlistPromotionPolicy` (FIFO vs. priority).  
**Why:** Semester rules and billing change without rewriting core orchestration.  
**Interview justification:** "Registrar policies are volatile; strategies isolate them from `RegistrationService`."

---

### Pattern 2: Template Method / Orchestrated Use Case

**Where:** `RegistrationService.register` fixed pipeline: window → prereq → conflict → credits → seat.  
**Why:** Consistent ordering; easy to reason about failures.  
**Interview justification:** "Every registration follows the same validation sequence; we only swap collaborators."

---

### Pattern 3: Unit of Work / Repository

**Where:** `SeatManager` + `SeatTxn` abstraction over DB transaction.  
**Why:** Atomic seat mutation and waitlist promotion.  
**Interview justification:** "Enrollment is inherently transactional; UoW makes tests use an in-memory fake."

---

### Pattern 4: Ports & Adapters (Hexagonal)

**Where:** `AcademicHistoryPort`, `SectionCatalogPort`, `EnrollmentQueryPort`.  
**Why:** Core domain independent of ORM and external student information system.  
**Interview justification:** "Universities plug in Banner/PeopleSoft/SIS via adapters."

---

### Pattern 5: Optimistic vs. Pessimistic Concurrency

**Where:** Pessimistic lock on `section` row for seat; optimistic versioning on `Section` aggregate optional for admin capacity edits.  
**Why:** Prevent lost updates on `enrolledCount` under burst load.  
**Interview justification:** "Hot sections need row-level pessimistic locking; catalog edits can use optimistic versioning."

---

## 💡 Step 8: Interview Discussion Points

### 1. Schedule Conflict Detection: Modeling & Edge Cases

**Interviewer:** "How do you detect conflicts reliably?"

**Answer:**

- Expand each `Meeting` into concrete `TimeInterval`s over the term (skip holidays via academic calendar service).
- Flatten all intervals for the student’s proposed schedule; **sort by start**; sweep for overlaps with **O(n log n)** complexity (n = total meeting instances, bounded by term length × sections).
- Apply **buffer** between back-to-back classes if policy requires.
- Handle **online async** sections: zero or sparse intervals—treat as non-overlapping or require explicit “synchronous exam” blocks.
- **Cross-timezone:** normalize to student’s timezone or institution canonical zone; document ambiguity.

---

### 2. Prerequisite Graph: Cycles and Co-requisites

**Interviewer:** "What if prerequisites form a cycle?"

**Answer:**

- **Admin-time validation** when publishing catalog: detect cycles in directed graph of prerequisites; reject or break cycles.
- **Co-requisites:** satisfied if **same-term enrollment** OR already completed.
- **Concurrent enrollment in A and B** when B is prereq of A: usually **disallowed** unless B is co-req; policy flag.

---

### 3. Concurrency: Last Seat and Waitlist Fairness

**Interviewer:** "Two students click enroll at the same time on the last seat."

**Answer:**

- **Single row lock per section** (or per “seat pool” shard) serializes seat acquisition.
- **Unique constraints:** `(student_id, section_id)` for active enrollment; `idempotency_key` unique for retry safety.
- **Source of truth:** count `REGISTERED` enrollments vs. `capacity`, not a free-floating counter without constraints.
- **Waitlist:** strict `created_at` ordering; promotion in **same transaction** as drop to avoid double promotion.

---

### 4. Registration Phases & Drop/Add

**Interviewer:** "How do early and late periods differ in code?"

**Answer:**

- `RegistrationWindowService` returns allowed **phase** based on time range **and** student attributes (honors, athletes, year).
- **Late phase** triggers `TuitionCalculator` surcharges.
- **Drop/add:** separate window; **W vs. dropped** affects transcript and refund rules—model as state transitions with timestamps.

---

### 5. Classroom Allocation vs. Online Sections

**Interviewer:** "Where does room assignment fit?"

**Answer:**

- **Constraint solver** (greedy first fit or backtracking for small N): room capacity ≥ section cap; equipment tags; no double-booking of room intervals (same conflict algorithm as students).
- **Online sections:** `classroomId` null; skip room conflict.

---

### 6. Payment Holds

**Interviewer:** "Should unpaid students keep seats?"

**Answer:**

- Policy: **soft hold** (seat held with timeout) vs. **hard enroll** (must pay later). Implement **seat reservation** with TTL for soft hold; release on expiry and promote waitlist—requires scheduled job and careful locking.

---

## 🏆 SOLID Principles Verification

### Single Responsibility ✅

- `SeatManager`: seat acquisition and waitlist promotion only.
- `PrerequisiteValidator`: prerequisite logic only.
- `ScheduleConflictDetector`: temporal overlap only.
- `TuitionCalculator`: pricing only.

### Open/Closed ✅

```java
public interface WaitlistPromotionPolicy {
    List<WaitlistEntry> reorder(List<WaitlistEntry> entries);
}
// New policy for veterans priority without changing SeatManager internals
```

### Liskov Substitution ✅

- Any `PrerequisiteValidator` implementation usable by `RegistrationService` without breaking contracts.

### Interface Segregation ✅

- Split `SectionCatalogPort` (reads) from `SeatTxn` (writes) so read-only services don’t depend on mutation APIs.

### Dependency Inversion ✅

- `RegistrationService` depends on `PrerequisiteValidator`, not concrete SIS client.

---

## 🎯 Key Takeaways

### Architecture

- ✅ **Section-level enrollment** with **term** scope; catalog centered on **Course → Section → Meeting**.
- ✅ **Row-level locking** (or equivalent) on **hot sections** + **unique constraints** for **idempotency** and **no double booking**.
- ✅ **Prerequisite validation** against **history + current term**; **graph validation** at catalog publish time.
- ✅ **Schedule conflicts** via **flattened intervals** + **sweep line**; configurable **buffer**.
- ✅ **Registration phases** and **drop/add** modeled as **time + policy**; **tuition** recomputed from **enrolled credits**.
- ✅ **Transcript/GPA** from **immutable grade outcomes**; audit **overrides**.

### Concurrency & Consistency

- ✅ One transaction bundles **validate + enroll** or **drop + waitlist promote**.
- ✅ Retries safe with **idempotency keys**.
- ✅ Waitlist **FIFO** unless **strategy** injected.

### Operations

- ✅ **Read replicas** for search; **primary** for enrollment.
- ✅ **Feature flags** for payment holds and retake rules.

---

**Total: 137 DSA + 12 LLD Problems**

Ready for review!
