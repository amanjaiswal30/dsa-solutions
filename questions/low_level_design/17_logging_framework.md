# Low-Level Design: Logging Framework

**Difficulty:** Hard 🔥

**Interview Duration:** 60-75 minutes

---

## 📋 Interview Approach

This document follows a **systematic interview approach** showing exactly how to tackle this problem in a real interview setting.

---

## 🎯 Step 1: Understand the Problem (5 minutes)

### What the Interviewer Says:
*"Design a logging framework like Log4j or SLF4J that can be used by applications to log messages at different levels to various destinations."*

### Clarifying Questions to Ask:

1. **Q:** What log levels should be supported?  
   **A:** DEBUG, INFO, WARN, ERROR, FATAL (5 levels with increasing severity).

2. **Q:** Where should logs be written?  
   **A:** Multiple destinations - Console, File, Database, Remote server. Should support multiple simultaneously.

3. **Q:** Should we support different log formats?  
   **A:** Yes - Plain text, JSON, XML. Users should be able to define custom formats.

4. **Q:** How should the API look for developers?  
   **A:** Simple API like `logger.info("message")`, `logger.error("error message", exception)`.

5. **Q:** Should it support configuration?  
   **A:** Yes, via configuration file (properties or XML) or programmatically.

6. **Q:** What about log file management?  
   **A:** Support file rotation - by size (e.g., max 10MB) or by time (daily rotation).

7. **Q:** Should logging be synchronous or asynchronous?  
   **A:** Support both. Async for performance-critical applications.

8. **Q:** How to handle different loggers for different classes?  
   **A:** Each class can get its own logger instance by name (e.g., `Logger.getLogger(MyClass.class)`).

9. **Q:** Should it support filtering?  
   **A:** Yes, filter by log level, logger name pattern, or custom filters.

10. **Q:** What about log message parameters?  
    **A:** Support parameterized logging like `logger.info("User {} logged in", username)` to avoid string concatenation.

11. **Q:** Thread safety requirements?  
    **A:** Must be thread-safe. Multiple threads will log simultaneously.

12. **Q:** Performance expectations?  
    **A:** Logging should not block application. Async logging should handle 10,000+ logs/second.

---

## 🔹 Step 2: Gather Requirements (5-7 minutes)

### Functional Requirements

Listed point-wise as interviewer presents them:

1. System should support five log levels: DEBUG, INFO, WARN, ERROR, FATAL
2. Each logger should have a configurable minimum log level threshold
3. Messages below threshold should be ignored (e.g., if level is WARN, ignore DEBUG and INFO)
4. Loggers should be obtained by name or class (e.g., `Logger.getLogger("com.app.UserService")`)
5. API should support simple logging: `logger.info("message")`
6. API should support parameterized logging: `logger.info("User {} did {}", user, action)`
7. API should support exception logging: `logger.error("Failed to process", exception)`
8. System should support multiple output destinations (appenders):
   - ConsoleAppender (stdout/stderr)
   - FileAppender (write to file)
   - DatabaseAppender (write to DB)
   - RemoteAppender (send to remote logging server)
9. Multiple appenders can be attached to a single logger
10. Each appender should have its own log format (Formatter)
11. System should support multiple formatters:
    - SimpleFormatter (plain text)
    - JSONFormatter
    - XMLFormatter
    - PatternFormatter (user-defined pattern)
12. Log messages should include: timestamp, log level, logger name, thread name, message
13. System should support file rotation:
    - Size-based (rotate when file reaches X MB)
    - Time-based (daily, hourly rotation)
14. System should support asynchronous logging (non-blocking)
15. System should support custom filters to selectively log messages
16. Configuration should be possible via:
    - Configuration file (properties/XML)
    - Programmatic API
17. Loggers should follow hierarchy (e.g., `com.app` is parent of `com.app.UserService`)
18. Child loggers can inherit configuration from parent
19. System should have a root logger as default
20. System should buffer async logs and flush periodically or on shutdown

### Non-Functional Requirements - Systematic Deduction

**Framework: SCAMPS**

#### 1. **Performance Analysis**

**Think:** "Logging is called thousands of times - can't be slow"
- Application threads should not block on logging
- Typical application: 1000-10000 log statements per second
- Heavy I/O operations (file writes, network calls)

**Deduced NFRs:**
- ✅ Logging operation must be < 1ms (synchronous) or < 0.1ms (async)
- ✅ Async logging required for high-throughput apps
- ✅ Batch I/O operations (buffer writes, flush periodically)
- ✅ Lazy string evaluation (don't build message if level filtered)

**Interview Explanation:**
"Logging is on critical path. If logger.info() takes 10ms and we call it 100 times, that's 1 second overhead. Async logging moves I/O to background thread, application thread returns immediately."

**Measurement:**
```
Synchronous: log() → format() → write to file → return (10-50ms)
Asynchronous: log() → add to queue → return (< 1ms)
             Background thread: dequeue → format → write
```

---

#### 2. **Scalability Analysis**

**Think:** "Will be used across entire application"
- Every class can have its own logger
- Large applications: 1000+ logger instances
- Millions of log messages per day

**Deduced NFRs:**
- ✅ Logger instances must be lightweight (minimal memory)
- ✅ Reuse logger instances (cache by name)
- ✅ Efficient queue for async logging (bounded to prevent OOM)
- ✅ Log file compression for long-term storage

**Why these matter:**
- 1000 loggers × 1KB each = 1MB (acceptable)
- 1000 loggers × 1MB each = 1GB (unacceptable)

---

#### 3. **Consistency Analysis**

**Think:** "What must be accurate?"
- Log message integrity (no partial writes)
- Log order (especially for debugging)
- No lost messages (especially ERROR/FATAL)

**Deduced NFRs:**
- ✅ Atomic log writes (no interleaving from multiple threads)
- ✅ FIFO order for logs from same thread (causal order)
- ✅ Async queue must persist critical logs on shutdown
- ✅ Log files must be consistent (no corruption)

**Trade-off Discussion:**
"Perfect global ordering across all threads is expensive. We guarantee FIFO per thread, but logs from different threads may interleave. This is acceptable for debugging."

**Critical:** ERROR and FATAL logs should be flushed immediately (not buffered).

---

#### 4. **Availability Analysis**

**Think:** "What if logging fails?"
- File system full
- Network unavailable (remote appender)
- Database down

**Deduced NFRs:**
- ✅ Logging failure should NOT crash application
- ✅ Fail-safe: log to stderr if primary destination fails
- ✅ Circuit breaker for remote appenders (stop trying if repeatedly failing)
- ✅ Graceful degradation (disable appender if failing)

**Interview Explanation:**
"Logging is infrastructure. Application should continue working even if logging fails. We catch all exceptions in logging code and log to stderr as fallback."

---

#### 5. **Concurrency Analysis**

**Think:** "Multiple threads logging simultaneously"
- All threads share logger instances
- Write to same file from multiple threads
- Async queue accessed by multiple producers

**Deduced NFRs:**
- ✅ Thread-safe logger API (multiple threads can call logger.info())
- ✅ Thread-safe appenders (synchronized file writes)
- ✅ Thread-safe async queue (ConcurrentLinkedQueue or BlockingQueue)
- ✅ No deadlocks (avoid nested locks)

**Critical Sections:**
1. Logger creation (LoggerFactory)
2. Log message formatting
3. File writing
4. Queue operations

---

#### 6. **Maintainability Analysis**

**Think:** "How easy to extend?"
- Add new appenders (Kafka, Elasticsearch)
- Add new formatters
- Add custom filters

**Deduced NFRs:**
- ✅ Open-Closed Principle (extend without modifying)
- ✅ Plugin architecture for appenders/formatters
- ✅ Clear interfaces for extensibility
- ✅ Comprehensive configuration options

---

**NFR Summary Table:**

| Dimension | Requirement | Deduction Method | Impact |
|-----------|-------------|------------------|--------|
| Performance | log() < 1ms | Critical path analysis | Async logging, buffering |
| Scalability | 1000+ loggers | Application size | Lightweight instances, caching |
| Consistency | No lost ERROR logs | Business criticality | Immediate flush for errors |
| Availability | No app crash on log failure | Fault tolerance | Exception handling, fallback |
| Concurrency | Thread-safe operations | Multi-threading | Synchronized blocks, queues |
| Maintainability | Easy to add appenders | Extensibility | Strategy pattern, interfaces |

---

## 🧩 Step 3: Identify Core Entities (10-12 minutes)

### Systematic Entity Identification Method

#### Step 1: Noun Extraction from Requirements

| Requirement | Nouns Identified |
|-------------|------------------|
| "Support log levels: DEBUG, INFO, WARN..." | Log Level |
| "Loggers obtained by name or class" | Logger, Logger Name |
| "Simple logging: logger.info(message)" | Log Message |
| "Support multiple output destinations" | Destination, Appender |
| "ConsoleAppender, FileAppender" | Console, File |
| "Each appender has log format" | Format, Formatter |
| "SimpleFormatter, JSONFormatter" | Format Type |
| "Log messages include timestamp, level, thread" | Log Entry, Timestamp |
| "Support file rotation" | Rotation Policy |
| "Asynchronous logging" | Async Handler, Queue |
| "Custom filters" | Filter |
| "Configuration file" | Configuration |
| "Logger hierarchy" | Logger Hierarchy, Parent Logger |

#### Step 2: Entity Validation

| Noun | Attributes? | Behavior? | Lifecycle? | Relationships? | Entity? |
|------|-------------|-----------|------------|----------------|---------|
| Logger | ✅ name, level | ✅ log, setLevel | ✅ create/get | ✅ has appenders | ✅ YES |
| LogLevel | ✅ name, severity | ✅ compare | ✅ enum | ✅ used by logger | ✅ YES (enum) |
| LogMessage | ✅ level, message, time | ✅ format | ✅ create/discard | ✅ belongs to logger | ✅ YES |
| Appender | ✅ formatter, level | ✅ append | ✅ create/close | ✅ outputs logs | ✅ YES (abstract) |
| Formatter | ✅ pattern | ✅ format | ✅ configure | ✅ used by appender | ✅ YES (interface) |
| Filter | ✅ criteria | ✅ decide | ✅ configure | ✅ used by logger | ✅ YES (interface) |
| LoggerFactory | ✅ logger cache | ✅ getLogger | ✅ initialize | ✅ creates loggers | ✅ YES (singleton) |
| Configuration | ⚠️ just data | ⚠️ load/parse | ✅ load/apply | ✅ configures system | ⚠️ MAYBE |
| RotationPolicy | ✅ threshold | ✅ shouldRotate | ✅ configure | ✅ used by file appender | ✅ YES (interface) |
| AsyncHandler | ✅ queue, thread | ✅ enqueue, flush | ✅ start/stop | ✅ wraps appender | ✅ YES |

#### Step 3: Refinement and Grouping

**Group 1: Core Logging**
- Logger (main API entry point)
- LogLevel (enum for severity)
- LogMessage (data structure)

**Group 2: Output Handling**
- Appender (abstract base for destinations)
- ConsoleAppender, FileAppender, DatabaseAppender (concrete)
- AsyncAppender (wrapper for async behavior)

**Group 3: Formatting**
- Formatter (interface)
- SimpleFormatter, JSONFormatter, PatternFormatter (concrete)

**Group 4: Filtering**
- Filter (interface)
- LevelFilter, NameFilter, CustomFilter (concrete)

**Group 5: File Management**
- RotationPolicy (interface)
- SizeBasedRotation, TimeBasedRotation (concrete)

**Group 6: Logger Management**
- LoggerFactory (singleton for creating loggers)
- LoggerConfig (configuration per logger)

### Final Entity List (12 Core Entities + Interfaces)

**Core Entities:**
1. **Logger** - Main API, receives log calls
2. **LogLevel** - Enum for severity levels
3. **LogMessage** - Encapsulates log data
4. **LoggerFactory** - Creates and caches loggers (Singleton)

**Output Entities:**
5. **Appender** (Abstract) + Concrete types:
   - ConsoleAppender
   - FileAppender
   - DatabaseAppender
   - AsyncAppender

**Formatting Entities:**
6. **Formatter** (Interface) + Implementations:
   - SimpleFormatter
   - JSONFormatter
   - PatternFormatter

**Filtering Entities:**
7. **Filter** (Interface) + Implementations:
   - LevelFilter
   - NameFilter

**File Management:**
8. **RotationPolicy** (Interface)
   - SizeBasedRotation
   - TimeBasedRotation

**Why these?**
- Logger: Main API (every class interacts with this)
- Appender: Strategy pattern for output destinations
- Formatter: Strategy pattern for message format
- Filter: Chain of Responsibility for selective logging
- LoggerFactory: Centralized logger creation and caching

---

## 🔗 Step 4: Establish Relationships (12-15 minutes)

### Three-Pass Methodology

---

### Pass 1: Inheritance Hierarchies (IS-A)

**Analysis:**

**Appender Hierarchy:**
```
Appender IS-A output destination? → Yes (abstract base)
ConsoleAppender IS-A Appender? → Yes
FileAppender IS-A Appender? → Yes
DatabaseAppender IS-A Appender? → Yes
AsyncAppender IS-A Appender? → Yes (decorator pattern actually)
```

**Decision:** Create Appender hierarchy
```
Appender (Abstract)
  ├─ ConsoleAppender
  ├─ FileAppender
  ├─ DatabaseAppender
  └─ AsyncAppender (wraps another appender)
```

**Why abstract class vs interface?**
- Common state: `formatter`, `filters`, `minLevel`
- Common behavior: `shouldLog()`, `applyFilters()`
- Template method: `append()` calls `doAppend()` (hook method)

**Formatter Hierarchy:**
```
All formatters implement same interface: format(LogMessage) → String
No shared state → Use Interface
```

**Interface:**
```
Formatter (Interface)
  ├─ SimpleFormatter
  ├─ JSONFormatter
  ├─ PatternFormatter
  └─ XMLFormatter
```

**Filter Hierarchy:**
Similar to Formatter → Interface

---

### Pass 2: Ownership Relationships (HAS-A)

#### Logger ↔ Appender

**Q1:** Does Logger contain Appenders? → **Yes**  
**Q2:** Can Appender exist without Logger? → **Yes** (can be shared)  
**Q3:** Delete Logger → Delete Appenders? → **No** (may be shared)

**Conclusion:** **Aggregation** (weak reference)
```
Logger ◇────→ Appender [0..N]
```

**Interview Explanation:**
"Appenders can be shared across multiple loggers (e.g., all loggers write to same file). So it's aggregation, not composition."

#### Appender ↔ Formatter

**Q1:** Does Appender contain Formatter? → **Yes**  
**Q2:** Can Formatter exist without Appender? → **Yes**  
**Q3:** Delete Appender → Delete Formatter? → **No**

**Conclusion:** **Aggregation**
```
Appender ◇────→ Formatter [1]
```

#### FileAppender ↔ RotationPolicy

**Q1:** Does FileAppender contain RotationPolicy? → **Yes**  
**Q2:** Can RotationPolicy exist without FileAppender? → **Yes**  
**Q3:** Delete FileAppender → Delete RotationPolicy? → **No**

**Conclusion:** **Aggregation**
```
FileAppender ◇────→ RotationPolicy [0..1]
```

#### Logger ↔ LogMessage

**Q1:** Does Logger own LogMessage? → **No** (creates and passes)  
**Q2:** Can LogMessage exist without Logger? → **No**  
**Q3:** Delete Logger → Delete LogMessage? → **N/A** (transient)

**Conclusion:** **Association** (creates and passes to appenders)
```
Logger ─────→ LogMessage [creates]
```

#### LoggerFactory ↔ Logger

**Q1:** Does LoggerFactory contain Loggers? → **Yes** (cache)  
**Q2:** Can Logger exist without LoggerFactory? → **Technically yes, but not in design**  
**Q3:** Delete LoggerFactory → Delete Loggers? → **Yes** (on shutdown)

**Conclusion:** **Composition** (owns cache)
```
LoggerFactory ◆────→ Logger [1..N]
```

---

### Pass 3: Cardinality Mapping

| Relationship | From | To | Cardinality | Reasoning |
|--------------|------|----|-------------|-----------|
| Logger → Appender | 1 | 0..N | 1:N | Logger can have multiple destinations |
| Logger → Filter | 1 | 0..N | 1:N | Logger can have multiple filters |
| Logger → Parent | 1 | 0..1 | N:1 | Logger has one parent (hierarchy) |
| Appender → Formatter | 1 | 1 | 1:1 | Each appender has one formatter |
| Appender → Filter | 1 | 0..N | 1:N | Appender can have own filters |
| FileAppender → RotationPolicy | 1 | 0..1 | 1:0..1 | Optional rotation |
| AsyncAppender → Queue | 1 | 1 | 1:1 | Each async appender has its queue |
| AsyncAppender → Appender | 1 | 1 | 1:1 | Wraps one target appender |
| LoggerFactory → Logger | 1 | N | 1:N | Factory caches all loggers |

---

### Special Design Decisions

#### Decision 1: Logger Hierarchy

**Problem:** How to handle logger inheritance?

**Example:**
```
com.app                  (parent, level = INFO)
  ├─ com.app.service     (child, inherits INFO)
  └─ com.app.dao         (child, overrides to DEBUG)
```

**Implementation:**
```java
public class Logger {
    private String name;
    private Logger parent;
    private LogLevel level; // null = inherit from parent
    
    public LogLevel getEffectiveLevel() {
        if (level != null) return level;
        if (parent != null) return parent.getEffectiveLevel();
        return LogLevel.INFO; // default
    }
}
```

**Interview Explanation:**
"Logger hierarchy mirrors package structure. Child loggers inherit parent's configuration unless overridden. This allows 'set all com.app.* to DEBUG' without configuring each logger."

#### Decision 2: Async Appender (Decorator vs Inheritance)

**Option A:** AsyncConsoleAppender, AsyncFileAppender (inheritance)
❌ Violates DRY (duplicate async logic)

**Option B:** AsyncAppender wraps any appender (decorator)
✅ Single async implementation, wraps any appender

**Choice:** **Decorator Pattern**

```java
// Usage
Appender fileAppender = new FileAppender("app.log");
Appender asyncFile = new AsyncAppender(fileAppender);
logger.addAppender(asyncFile);
```

#### Decision 3: Filter Chain

**Problem:** Multiple filters - AND logic or OR logic?

**Decision:** Chain with AND logic (all filters must pass)

```java
public boolean shouldLog(LogMessage msg) {
    for (Filter filter : filters) {
        if (!filter.allow(msg)) {
            return false; // Any filter rejects → don't log
        }
    }
    return true;
}
```

---

### Complete Relationship Diagram

```
┌──────────────────┐
│  LoggerFactory   │
│   (Singleton)    │
└────────┬─────────┘
         │
         │ [1:N Composition]
         │
         ▼
    ┌─────────┐
    │ Logger  │
    └────┬────┘
         │
         ├─── [1:N Aggregation] ──→ Appender
         │                              │
         │                              ├─── [1:1 Aggregation] ──→ Formatter
         │                              │                              ├─ SimpleFormatter
         │                              │                              ├─ JSONFormatter
         │                              │                              └─ PatternFormatter
         │                              │
         │                              ├─── [1:N Aggregation] ──→ Filter
         │                              │
         │                              └─── Concrete Types:
         │                                      ├─ ConsoleAppender
         │                                      ├─ FileAppender
         │                                      │     └─ [1:0..1] ──→ RotationPolicy
         │                                      ├─ DatabaseAppender
         │                                      └─ AsyncAppender
         │                                            └─ [1:1] ──→ Queue
         │
         └─── [N:1] ──→ Logger (parent)


LogMessage (created by Logger, passed to Appenders)
```

---

## 📐 Step 5: Design Class Diagrams (12-15 minutes)

### Class Diagram 1: LogLevel Enum

```
┌────────────────────────────────────┐
│      <<enumeration>>               │
│         LogLevel                   │
├────────────────────────────────────┤
│ DEBUG(0, "DEBUG")                  │
│ INFO(1, "INFO")                    │
│ WARN(2, "WARN")                    │
│ ERROR(3, "ERROR")                  │
│ FATAL(4, "FATAL")                  │
├────────────────────────────────────┤
│ - level: int                       │
│ - name: String                     │
├────────────────────────────────────┤
│ + LogLevel(level, name)            │
│ + getLevel(): int                  │
│ + getName(): String                │
│ + isGreaterOrEqual(other): boolean │
└────────────────────────────────────┘
```

**Design Rationale:**
- Integer level for comparison (DEBUG < INFO < WARN...)
- `isGreaterOrEqual()` for threshold checking

---

### Class Diagram 2: LogMessage

```
┌──────────────────────────────────────────────┐
│            LogMessage                        │
├──────────────────────────────────────────────┤
│ - timestamp: LocalDateTime                   │
│ - level: LogLevel                            │
│ - loggerName: String                         │
│ - threadName: String                         │
│ - message: String                            │
│ - throwable: Throwable                       │
├──────────────────────────────────────────────┤
│ + LogMessage(level, logger, msg, throwable)  │
│ + getTimestamp(): LocalDateTime              │
│ + getLevel(): LogLevel                       │
│ + getLoggerName(): String                    │
│ + getThreadName(): String                    │
│ + getMessage(): String                       │
│ + getThrowable(): Throwable                  │
└──────────────────────────────────────────────┘
```

**Key Fields:**
- `timestamp`: When log was created (not when written)
- `threadName`: For debugging multi-threaded apps
- `throwable`: Optional exception

---

### Class Diagram 3: Logger

```
┌──────────────────────────────────────────────────┐
│              Logger                              │
├──────────────────────────────────────────────────┤
│ - name: String                                   │
│ - level: LogLevel                                │
│ - parent: Logger                        ◇────────┼──→ Logger (parent)
│ - appenders: List<Appender>             ◇────────┼──→ Appender [0..*]
│ - filters: List<Filter>                 ◇────────┼──→ Filter [0..*]
│ - additivity: boolean                            │
├──────────────────────────────────────────────────┤
│ + Logger(name, parent)                           │
│ + debug(message: String): void                   │
│ + debug(format: String, args: Object...): void   │
│ + info(message: String): void                    │
│ + info(format: String, args: Object...): void    │
│ + warn(message: String): void                    │
│ + error(message: String): void                   │
│ + error(msg: String, throwable: Throwable): void │
│ + fatal(message: String): void                   │
│ + setLevel(level: LogLevel): void                │
│ + addAppender(appender: Appender): void          │
│ + addFilter(filter: Filter): void                │
│ - log(level: LogLevel, msg: String, t): void     │
│ - shouldLog(level: LogLevel): boolean            │
│ - getEffectiveLevel(): LogLevel                  │
└──────────────────────────────────────────────────┘
```

**Key Methods:**
- Public API: `debug()`, `info()`, `warn()`, `error()`, `fatal()`
- Parameterized: `info("User {} logged in", username)`
- Internal: `log()` (all public methods delegate to this)

**Additivity:** If true, logs also go to parent's appenders (like inheritance)

---

### Class Diagram 4: Formatter Interface & Implementations

```
┌────────────────────────────┐
│     <<interface>>          │
│       Formatter            │
├────────────────────────────┤
│ + format(msg: LogMessage)  │
│   : String                 │
└────────────────────────────┘
           △
           │
    ┌──────┴─────┬─────────────┬──────────┐
    │            │             │          │
┌───┴──────┐ ┌──┴─────────┐ ┌─┴────────┐ ┌┴──────────┐
│ Simple   │ │   JSON     │ │ Pattern  │ │    XML    │
│Formatter │ │ Formatter  │ │Formatter │ │ Formatter │
├──────────┤ ├────────────┤ ├──────────┤ ├───────────┤
│          │ │            │ │-pattern  │ │           │
├──────────┤ ├────────────┤ ├──────────┤ ├───────────┤
│+format() │ │+format()   │ │+format() │ │+format()  │
└──────────┘ └────────────┘ └──────────┘ └───────────┘
```

**Format Examples:**

**SimpleFormatter:**
```
2026-02-01 10:30:45 [INFO] [com.app.UserService] [main] User logged in
```

**JSONFormatter:**
```json
{
  "timestamp": "2026-02-01T10:30:45",
  "level": "INFO",
  "logger": "com.app.UserService",
  "thread": "main",
  "message": "User logged in"
}
```

**PatternFormatter:**
```
Pattern: "%d{yyyy-MM-dd HH:mm:ss} [%p] [%c] - %m%n"
Output: 2026-02-01 10:30:45 [INFO] [com.app.UserService] - User logged in
```

---

### Class Diagram 5: Appender Hierarchy

```
┌──────────────────────────────────────────────────┐
│           <<abstract>>                           │
│            Appender                              │
├──────────────────────────────────────────────────┤
│ # formatter: Formatter              ◇────────────┼──→ Formatter
│ # filters: List<Filter>             ◇────────────┼──→ Filter [0..*]
│ # minLevel: LogLevel                             │
├──────────────────────────────────────────────────┤
│ + Appender(formatter)                            │
│ + append(msg: LogMessage): void [final]          │
│ # doAppend(msg: LogMessage): void [abstract]     │
│ + addFilter(filter: Filter): void                │
│ + setFormatter(formatter: Formatter): void       │
│ # shouldLog(msg: LogMessage): boolean            │
│ + close(): void [abstract]                       │
└──────────────────────────────────────────────────┘
                    △
                    │
        ┌───────────┼──────────┬──────────┬─────────────┐
        │           │          │          │             │
┌───────┴────┐ ┌───┴──────┐ ┌─┴────────┐ ┌┴──────────┐ ┌┴───────────┐
│  Console   │ │   File   │ │ Database │ │  Async    │ │   Remote   │
│ Appender   │ │ Appender │ │ Appender │ │ Appender  │ │  Appender  │
├────────────┤ ├──────────┤ ├──────────┤ ├───────────┤ ├────────────┤
│-stream     │ │-filePath │ │-connection│ │-queue     │ │-url        │
│            │ │-writer   │ │          │ │-worker    │ │-httpClient │
│            │ │-rotation │ │          │ │-target    │ │            │
├────────────┤ ├──────────┤ ├──────────┤ ├───────────┤ ├────────────┤
│+doAppend() │ │+doAppend()│ │+doAppend()│ │+doAppend()│ │+doAppend() │
│+close()    │ │+rotate() │ │+close()  │ │+flush()   │ │+close()    │
└────────────┘ └──────────┘ └──────────┘ └───────────┘ └────────────┘
```

**Template Method Pattern:**
```java
public final void append(LogMessage msg) {
    if (!shouldLog(msg)) return;
    doAppend(msg); // Hook method - subclass implements
}
```

---

### Class Diagram 6: Filter Interface

```
┌────────────────────────────┐
│     <<interface>>          │
│        Filter              │
├────────────────────────────┤
│ + allow(msg: LogMessage)   │
│   : boolean                │
└────────────────────────────┘
           △
           │
    ┌──────┴─────┬─────────────┐
    │            │             │
┌───┴──────┐ ┌──┴─────────┐ ┌─┴────────┐
│  Level   │ │   Name     │ │  Custom  │
│  Filter  │ │   Filter   │ │  Filter  │
├──────────┤ ├────────────┤ ├──────────┤
│-minLevel │ │-namePattern│ │-predicate│
├──────────┤ ├────────────┤ ├──────────┤
│+allow()  │ │+allow()    │ │+allow()  │
└──────────┘ └────────────┘ └──────────┘
```

**Examples:**

```java
// Only allow ERROR and above
Filter levelFilter = new LevelFilter(LogLevel.ERROR);

// Only allow loggers matching "com.app.service.*"
Filter nameFilter = new NameFilter("com.app.service.*");

// Custom: only log messages containing "payment"
Filter customFilter = new CustomFilter(
    msg -> msg.getMessage().contains("payment")
);
```

---

### Class Diagram 7: LoggerFactory

```
┌──────────────────────────────────────────────────┐
│          <<Singleton>>                           │
│         LoggerFactory                            │
├──────────────────────────────────────────────────┤
│ - instance: LoggerFactory [static]               │
│ - loggerCache: Map<String, Logger>  ◆────────────┼──→ Logger [1..*]
│ - rootLogger: Logger                             │
├──────────────────────────────────────────────────┤
│ - LoggerFactory() [private]                      │
│ + getInstance(): LoggerFactory [static, sync]    │
│ + getLogger(name: String): Logger [synchronized] │
│ + getLogger(clazz: Class<?>): Logger             │
│ + getRootLogger(): Logger                        │
│ - createLogger(name: String): Logger             │
│ - getParentLogger(name: String): Logger          │
│ + shutdown(): void                               │
└──────────────────────────────────────────────────┘
```

**Key Methods:**
- `getLogger()`: Returns cached logger or creates new
- `getParentLogger()`: Finds parent in hierarchy
  - "com.app.service.UserService" → parent is "com.app.service"
  - "com.app.service" → parent is "com.app"
  - "com.app" → parent is root logger

---

### Class Diagram 8: RotationPolicy

```
┌────────────────────────────────────┐
│     <<interface>>                  │
│      RotationPolicy                │
├────────────────────────────────────┤
│ + shouldRotate(currentFile): bool  │
│ + getNextFileName(current): String │
└────────────────────────────────────┘
           △
           │
    ┌──────┴─────┬────────────┐
    │            │            │
┌───┴────────┐ ┌─┴──────────┐ ┌┴──────────┐
│ SizeBased  │ │ TimeBased  │ │  Hybrid   │
│ Rotation   │ │ Rotation   │ │ Rotation  │
├────────────┤ ├────────────┤ ├───────────┤
│-maxSize    │ │-period     │ │-size,time │
│-maxFiles   │ │-dateFormat │ │           │
├────────────┤ ├────────────┤ ├───────────┤
│+shouldRotate│ │+shouldRotate│ │+should... │
│+getNextName│ │+getNextName│ │+getNext...│
└────────────┘ └────────────┘ └───────────┘
```

**Example:**

**Size-based:**
```
app.log (current, 9.5MB)
app.log.1 (10MB)
app.log.2 (10MB)
```

**Time-based:**
```
app-2026-02-01.log
app-2026-02-02.log
app-2026-02-03.log
```

---

### Complete System Architecture

```
          Application Code
                │
                ▼
        ┌──────────────┐
        │LoggerFactory │
        └──────┬───────┘
               │ creates & caches
               ▼
          ┌────────┐
          │ Logger │
          └────┬───┘
               │
        ┌──────┴──────┬──────────┐
        │             │          │
        ▼             ▼          ▼
    ┌─────────┐  ┌────────┐  ┌────────┐
    │Appender1│  │Append2 │  │Append3 │
    └────┬────┘  └───┬────┘  └───┬────┘
         │           │           │
         ▼           ▼           ▼
    ┌─────────┐  ┌────────┐  ┌────────┐
    │Formatter│  │Format. │  │Format. │
    └─────────┘  └────────┘  └────────┘
         │           │           │
         ▼           ▼           ▼
    [Console]    [File]      [Database]
```

---

## 💻 Step 6: Core Implementation (25-30 minutes)

### Implementation Strategy:
1. Bottom-up: Enums and data structures first
2. Core interfaces
3. Logger and LoggerFactory
4. Appenders and Formatters
5. Async handling

---

### Enums

```java
// LogLevel.java
public enum LogLevel {
    DEBUG(0, "DEBUG"),
    INFO(1, "INFO"),
    WARN(2, "WARN"),
    ERROR(3, "ERROR"),
    FATAL(4, "FATAL");
    
    private final int level;
    private final String name;
    
    LogLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isGreaterOrEqual(LogLevel other) {
        return this.level >= other.level;
    }
}
```

---

### LogMessage

```java
// LogMessage.java
import java.time.LocalDateTime;

public class LogMessage {
    private final LocalDateTime timestamp;
    private final LogLevel level;
    private final String loggerName;
    private final String threadName;
    private final String message;
    private final Throwable throwable;
    
    public LogMessage(LogLevel level, String loggerName, 
                     String message, Throwable throwable) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.loggerName = loggerName;
        this.threadName = Thread.currentThread().getName();
        this.message = message;
        this.throwable = throwable;
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public LogLevel getLevel() { return level; }
    public String getLoggerName() { return loggerName; }
    public String getThreadName() { return threadName; }
    public String getMessage() { return message; }
    public Throwable getThrowable() { return throwable; }
}
```

---

### Formatter Interface & Implementations

```java
// Formatter.java
public interface Formatter {
    String format(LogMessage message);
}

// SimpleFormatter.java
import java.time.format.DateTimeFormatter;

public class SimpleFormatter implements Formatter {
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public String format(LogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg.getTimestamp().format(DATE_FORMAT));
        sb.append(" [").append(msg.getLevel().getName()).append("]");
        sb.append(" [").append(msg.getLoggerName()).append("]");
        sb.append(" [").append(msg.getThreadName()).append("]");
        sb.append(" ").append(msg.getMessage());
        
        if (msg.getThrowable() != null) {
            sb.append("\n").append(getStackTrace(msg.getThrowable()));
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    private String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.toString()).append("\n");
        for (StackTraceElement element : t.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}

// JSONFormatter.java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.*;

public class JSONFormatter implements Formatter {
    private final Gson gson = new GsonBuilder()
                               .setPrettyPrinting()
                               .create();
    
    @Override
    public String format(LogMessage msg) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("timestamp", msg.getTimestamp().toString());
        json.put("level", msg.getLevel().getName());
        json.put("logger", msg.getLoggerName());
        json.put("thread", msg.getThreadName());
        json.put("message", msg.getMessage());
        
        if (msg.getThrowable() != null) {
            json.put("exception", msg.getThrowable().toString());
        }
        
        return gson.toJson(json) + "\n";
    }
}

// PatternFormatter.java
import java.time.format.DateTimeFormatter;

public class PatternFormatter implements Formatter {
    private final String pattern;
    
    // Pattern: %d{yyyy-MM-dd HH:mm:ss} [%p] [%c] [%t] - %m%n
    // %d = date, %p = level, %c = logger name, %t = thread, %m = message, %n = newline
    
    public PatternFormatter(String pattern) {
        this.pattern = pattern;
    }
    
    @Override
    public String format(LogMessage msg) {
        String result = pattern;
        
        // Replace patterns
        if (result.contains("%d")) {
            String datePattern = extractDatePattern(result);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);
            result = result.replaceFirst("%d\\{[^}]+\\}", 
                                        msg.getTimestamp().format(formatter));
        }
        
        result = result.replace("%p", msg.getLevel().getName());
        result = result.replace("%c", msg.getLoggerName());
        result = result.replace("%t", msg.getThreadName());
        result = result.replace("%m", msg.getMessage());
        result = result.replace("%n", "\n");
        
        return result;
    }
    
    private String extractDatePattern(String pattern) {
        int start = pattern.indexOf("%d{") + 3;
        int end = pattern.indexOf("}", start);
        return pattern.substring(start, end);
    }
}
```

---

### Filter Interface

```java
// Filter.java
public interface Filter {
    boolean allow(LogMessage message);
}

// LevelFilter.java
public class LevelFilter implements Filter {
    private final LogLevel minLevel;
    
    public LevelFilter(LogLevel minLevel) {
        this.minLevel = minLevel;
    }
    
    @Override
    public boolean allow(LogMessage message) {
        return message.getLevel().isGreaterOrEqual(minLevel);
    }
}

// NameFilter.java
import java.util.regex.Pattern;

public class NameFilter implements Filter {
    private final Pattern namePattern;
    
    public NameFilter(String pattern) {
        // Convert wildcard to regex: "com.app.*" -> "com\\.app\\..*"
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        this.namePattern = Pattern.compile(regex);
    }
    
    @Override
    public boolean allow(LogMessage message) {
        return namePattern.matcher(message.getLoggerName()).matches();
    }
}
```

---

### Appender Hierarchy

```java
// Appender.java
import java.util.*;

public abstract class Appender {
    protected Formatter formatter;
    protected List<Filter> filters;
    protected LogLevel minLevel;
    
    public Appender(Formatter formatter) {
        this.formatter = formatter;
        this.filters = new ArrayList<>();
        this.minLevel = LogLevel.DEBUG; // Log everything by default
    }
    
    // Template method
    public final void append(LogMessage message) {
        if (!shouldLog(message)) {
            return;
        }
        doAppend(message);
    }
    
    protected abstract void doAppend(LogMessage message);
    
    protected boolean shouldLog(LogMessage message) {
        // Check minimum level
        if (!message.getLevel().isGreaterOrEqual(minLevel)) {
            return false;
        }
        
        // Check filters (all must pass)
        for (Filter filter : filters) {
            if (!filter.allow(message)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void addFilter(Filter filter) {
        filters.add(filter);
    }
    
    public void setMinLevel(LogLevel level) {
        this.minLevel = level;
    }
    
    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }
    
    public abstract void close();
}

// ConsoleAppender.java
import java.io.PrintStream;

public class ConsoleAppender extends Appender {
    private final PrintStream stream;
    
    public ConsoleAppender(Formatter formatter) {
        this(formatter, System.out);
    }
    
    public ConsoleAppender(Formatter formatter, PrintStream stream) {
        super(formatter);
        this.stream = stream;
    }
    
    @Override
    protected synchronized void doAppend(LogMessage message) {
        String formatted = formatter.format(message);
        stream.print(formatted);
        
        // Flush ERROR and FATAL immediately
        if (message.getLevel().isGreaterOrEqual(LogLevel.ERROR)) {
            stream.flush();
        }
    }
    
    @Override
    public void close() {
        stream.flush();
    }
}

// FileAppender.java
import java.io.*;
import java.nio.file.*;

public class FileAppender extends Appender {
    private final String filePath;
    private BufferedWriter writer;
    private RotationPolicy rotationPolicy;
    private long currentSize;
    
    public FileAppender(String filePath, Formatter formatter) {
        super(formatter);
        this.filePath = filePath;
        this.currentSize = 0;
        try {
            this.writer = new BufferedWriter(new FileWriter(filePath, true));
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
        }
    }
    
    public void setRotationPolicy(RotationPolicy policy) {
        this.rotationPolicy = policy;
    }
    
    @Override
    protected synchronized void doAppend(LogMessage message) {
        try {
            // Check rotation
            if (rotationPolicy != null && rotationPolicy.shouldRotate(filePath)) {
                rotate();
            }
            
            String formatted = formatter.format(message);
            writer.write(formatted);
            currentSize += formatted.length();
            
            // Flush ERROR and FATAL immediately
            if (message.getLevel().isGreaterOrEqual(LogLevel.ERROR)) {
                writer.flush();
            }
            
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
    
    private void rotate() throws IOException {
        writer.close();
        
        String newFileName = rotationPolicy.getNextFileName(filePath);
        Files.move(Paths.get(filePath), Paths.get(newFileName));
        
        writer = new BufferedWriter(new FileWriter(filePath, true));
        currentSize = 0;
        
        System.out.println("Rotated log file: " + filePath + " -> " + newFileName);
    }
    
    @Override
    public void close() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close log file: " + e.getMessage());
        }
    }
}
```

---

### Rotation Policy

```java
// RotationPolicy.java
public interface RotationPolicy {
    boolean shouldRotate(String currentFilePath);
    String getNextFileName(String currentFilePath);
}

// SizeBasedRotationPolicy.java
import java.io.File;

public class SizeBasedRotationPolicy implements RotationPolicy {
    private final long maxSizeBytes;
    private int fileIndex;
    
    public SizeBasedRotationPolicy(long maxSizeMB) {
        this.maxSizeBytes = maxSizeMB * 1024 * 1024;
        this.fileIndex = 1;
    }
    
    @Override
    public boolean shouldRotate(String currentFilePath) {
        File file = new File(currentFilePath);
        return file.length() >= maxSizeBytes;
    }
    
    @Override
    public String getNextFileName(String currentFilePath) {
        return currentFilePath + "." + (fileIndex++);
    }
}

// TimeBasedRotationPolicy.java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

public class TimeBasedRotationPolicy implements RotationPolicy {
    private final DateTimeFormatter formatter;
    private String lastRotationDate;
    
    public TimeBasedRotationPolicy(String datePattern) {
        this.formatter = DateTimeFormatter.ofPattern(datePattern);
        this.lastRotationDate = LocalDateTime.now().format(formatter);
    }
    
    @Override
    public boolean shouldRotate(String currentFilePath) {
        String currentDate = LocalDateTime.now().format(formatter);
        return !currentDate.equals(lastRotationDate);
    }
    
    @Override
    public String getNextFileName(String currentFilePath) {
        String newDate = LocalDateTime.now().format(formatter);
        lastRotationDate = newDate;
        
        // app.log -> app-2026-02-01.log
        int dotIndex = currentFilePath.lastIndexOf(".");
        String baseName = currentFilePath.substring(0, dotIndex);
        String extension = currentFilePath.substring(dotIndex);
        
        return baseName + "-" + newDate + extension;
    }
}
```

---

### AsyncAppender (Decorator Pattern)

```java
// AsyncAppender.java
import java.util.concurrent.*;

public class AsyncAppender extends Appender {
    private final Appender targetAppender;
    private final BlockingQueue<LogMessage> queue;
    private final ExecutorService executor;
    private volatile boolean shutdown = false;
    
    public AsyncAppender(Appender targetAppender, int queueSize) {
        super(targetAppender.formatter);
        this.targetAppender = targetAppender;
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.executor = Executors.newSingleThreadExecutor();
        
        // Start background worker
        executor.submit(this::processQueue);
    }
    
    @Override
    protected void doAppend(LogMessage message) {
        try {
            // Non-blocking offer (fails if queue full)
            if (!queue.offer(message, 100, TimeUnit.MILLISECONDS)) {
                System.err.println("Async queue full, dropping log message");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void processQueue() {
        while (!shutdown || !queue.isEmpty()) {
            try {
                LogMessage message = queue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    targetAppender.append(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void flush() {
        while (!queue.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    @Override
    public void close() {
        shutdown = true;
        flush();
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        targetAppender.close();
    }
}
```

---

### Logger

```java
// Logger.java
import java.util.*;

public class Logger {
    private final String name;
    private Logger parent;
    private LogLevel level; // null = inherit from parent
    private List<Appender> appenders;
    private List<Filter> filters;
    private boolean additivity; // If true, also log to parent's appenders
    
    public Logger(String name, Logger parent) {
        this.name = name;
        this.parent = parent;
        this.level = null; // Inherit by default
        this.appenders = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.additivity = true;
    }
    
    // Public API
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }
    
    public void debug(String format, Object... args) {
        if (shouldLog(LogLevel.DEBUG)) {
            log(LogLevel.DEBUG, formatMessage(format, args), null);
        }
    }
    
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }
    
    public void info(String format, Object... args) {
        if (shouldLog(LogLevel.INFO)) {
            log(LogLevel.INFO, formatMessage(format, args), null);
        }
    }
    
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }
    
    public void warn(String format, Object... args) {
        if (shouldLog(LogLevel.WARN)) {
            log(LogLevel.WARN, formatMessage(format, args), null);
        }
    }
    
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }
    
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }
    
    public void error(String format, Object... args) {
        if (shouldLog(LogLevel.ERROR)) {
            log(LogLevel.ERROR, formatMessage(format, args), null);
        }
    }
    
    public void fatal(String message) {
        log(LogLevel.FATAL, message, null);
    }
    
    public void fatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }
    
    // Core logging method
    private void log(LogLevel level, String message, Throwable throwable) {
        if (!shouldLog(level)) {
            return;
        }
        
        LogMessage logMessage = new LogMessage(level, name, message, throwable);
        
        // Apply filters
        for (Filter filter : filters) {
            if (!filter.allow(logMessage)) {
                return;
            }
        }
        
        // Log to own appenders
        for (Appender appender : appenders) {
            try {
                appender.append(logMessage);
            } catch (Exception e) {
                System.err.println("Appender failed: " + e.getMessage());
            }
        }
        
        // Log to parent's appenders if additivity is true
        if (additivity && parent != null) {
            parent.logToAppenders(logMessage);
        }
    }
    
    // Helper for parent logging
    private void logToAppenders(LogMessage message) {
        for (Appender appender : appenders) {
            try {
                appender.append(message);
            } catch (Exception e) {
                System.err.println("Appender failed: " + e.getMessage());
            }
        }
        
        if (additivity && parent != null) {
            parent.logToAppenders(message);
        }
    }
    
    private boolean shouldLog(LogLevel level) {
        return level.isGreaterOrEqual(getEffectiveLevel());
    }
    
    public LogLevel getEffectiveLevel() {
        if (level != null) {
            return level;
        }
        if (parent != null) {
            return parent.getEffectiveLevel();
        }
        return LogLevel.INFO; // Default
    }
    
    private String formatMessage(String format, Object... args) {
        String result = format;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", String.valueOf(arg));
        }
        return result;
    }
    
    // Configuration methods
    public void setLevel(LogLevel level) {
        this.level = level;
    }
    
    public void addAppender(Appender appender) {
        appenders.add(appender);
    }
    
    public void addFilter(Filter filter) {
        filters.add(filter);
    }
    
    public void setAdditivity(boolean additivity) {
        this.additivity = additivity;
    }
    
    public String getName() {
        return name;
    }
}
```

---

### LoggerFactory

```java
// LoggerFactory.java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerFactory {
    private static LoggerFactory instance;
    private static final Object lock = new Object();
    
    private final Map<String, Logger> loggerCache;
    private final Logger rootLogger;
    
    private LoggerFactory() {
        this.loggerCache = new ConcurrentHashMap<>();
        this.rootLogger = new Logger("ROOT", null);
        this.rootLogger.setLevel(LogLevel.INFO);
        
        // Default: root logger logs to console
        this.rootLogger.addAppender(
            new ConsoleAppender(new SimpleFormatter())
        );
        
        loggerCache.put("ROOT", rootLogger);
    }
    
    public static LoggerFactory getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new LoggerFactory();
                }
            }
        }
        return instance;
    }
    
    public synchronized Logger getLogger(String name) {
        if (loggerCache.containsKey(name)) {
            return loggerCache.get(name);
        }
        
        Logger logger = createLogger(name);
        loggerCache.put(name, logger);
        return logger;
    }
    
    public Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
    
    private Logger createLogger(String name) {
        Logger parent = getParentLogger(name);
        return new Logger(name, parent);
    }
    
    private Logger getParentLogger(String name) {
        int lastDot = name.lastIndexOf('.');
        
        if (lastDot == -1) {
            // No parent, return root
            return rootLogger;
        }
        
        String parentName = name.substring(0, lastDot);
        
        // Get or create parent
        if (loggerCache.containsKey(parentName)) {
            return loggerCache.get(parentName);
        } else {
            return getLogger(parentName); // Recursive creation
        }
    }
    
    public Logger getRootLogger() {
        return rootLogger;
    }
    
    public void shutdown() {
        System.out.println("Shutting down logging framework...");
        
        for (Logger logger : loggerCache.values()) {
            for (Appender appender : logger.appenders) {
                appender.close();
            }
        }
        
        System.out.println("Logging framework shut down");
    }
}
```

---

### Demo Application

```java
// LoggingFrameworkDemo.java
public class LoggingFrameworkDemo {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   LOGGING FRAMEWORK DEMONSTRATION        ");
        System.out.println("═══════════════════════════════════════════\n");
        
        LoggerFactory factory = LoggerFactory.getInstance();
        
        // ===== SCENARIO 1: Basic Logging =====
        System.out.println("═══ SCENARIO 1: Basic Logging ═══\n");
        
        Logger logger = factory.getLogger("com.app.UserService");
        logger.debug("Debug message");
        logger.info("User logged in");
        logger.warn("Cache miss, fetching from database");
        logger.error("Failed to send email");
        
        System.out.println("\n");
        
        // ===== SCENARIO 2: Parameterized Logging =====
        System.out.println("═══ SCENARIO 2: Parameterized Logging ═══\n");
        
        String username = "john_doe";
        int attemptCount = 3;
        logger.info("User {} logged in after {} attempts", username, attemptCount);
        
        System.out.println("\n");
        
        // ===== SCENARIO 3: Exception Logging =====
        System.out.println("═══ SCENARIO 3: Exception Logging ═══\n");
        
        try {
            int result = 10 / 0;
        } catch (Exception e) {
            logger.error("Division error occurred", e);
        }
        
        System.out.println("\n");
        
        // ===== SCENARIO 4: Multiple Appenders =====
        System.out.println("═══ SCENARIO 4: Multiple Appenders (Console + File) ═══\n");
        
        Logger serviceLogger = factory.getLogger("com.app.PaymentService");
        
        // Add file appender
        FileAppender fileAppender = new FileAppender(
            "payment.log", 
            new SimpleFormatter()
        );
        fileAppender.setRotationPolicy(
            new SizeBasedRotationPolicy(10) // 10MB
        );
        serviceLogger.addAppender(fileAppender);
        
        serviceLogger.info("Payment processed: $99.99");
        serviceLogger.warn("Payment gateway slow response");
        
        System.out.println("\n");
        
        // ===== SCENARIO 5: JSON Logging =====
        System.out.println("═══ SCENARIO 5: JSON Format ═══\n");
        
        Logger apiLogger = factory.getLogger("com.app.APIGateway");
        apiLogger.addAppender(
            new ConsoleAppender(new JSONFormatter())
        );
        apiLogger.info("API request received");
        
        System.out.println("\n");
        
        // ===== SCENARIO 6: Logger Hierarchy =====
        System.out.println("═══ SCENARIO 6: Logger Hierarchy ═══\n");
        
        Logger rootLogger = factory.getRootLogger();
        rootLogger.setLevel(LogLevel.WARN); // Only WARN and above
        
        Logger childLogger = factory.getLogger("com.app.dao.UserDAO");
        childLogger.debug("This won't appear (below WARN)");
        childLogger.info("This won't appear either");
        childLogger.warn("This will appear (WARN)");
        
        System.out.println("\n");
        
        // ===== SCENARIO 7: Async Logging =====
        System.out.println("═══ SCENARIO 7: Async Logging (Performance) ═══\n");
        
        Logger highThroughputLogger = factory.getLogger("com.app.HighTraffic");
        
        FileAppender syncFileAppender = new FileAppender(
            "high-traffic.log",
            new SimpleFormatter()
        );
        AsyncAppender asyncAppender = new AsyncAppender(syncFileAppender, 1000);
        highThroughputLogger.addAppender(asyncAppender);
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            highThroughputLogger.info("High frequency log message {}", i);
        }
        long end = System.currentTimeMillis();
        
        System.out.println("Logged 1000 messages in " + (end - start) + "ms");
        asyncAppender.flush();
        
        System.out.println("\n");
        
        // ===== SCENARIO 8: Filters =====
        System.out.println("═══ SCENARIO 8: Filters (Only ERROR and above) ═══\n");
        
        Logger filteredLogger = factory.getLogger("com.app.Filtered");
        ConsoleAppender consoleAppender = new ConsoleAppender(new SimpleFormatter());
        consoleAppender.addFilter(new LevelFilter(LogLevel.ERROR));
        filteredLogger.addAppender(consoleAppender);
        
        filteredLogger.info("This won't appear (filtered out)");
        filteredLogger.warn("This won't appear either");
        filteredLogger.error("This will appear (ERROR)");
        
        System.out.println("\n");
        
        // Cleanup
        factory.shutdown();
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("         DEMO COMPLETED                    ");
        System.out.println("═══════════════════════════════════════════");
    }
}
```

---

## 🎨 Step 7: Design Patterns Applied

### Pattern 1: Singleton Pattern
**Where:** LoggerFactory  
**Why:** Single factory manages all loggers, ensures logger reuse

**Interview Justification:**
"LoggerFactory is Singleton because we need centralized logger management. Multiple factories would create duplicate loggers, breaking the hierarchy and caching mechanism."

---

### Pattern 2: Strategy Pattern
**Where:** Formatter interface, Appender hierarchy

**Why:** Multiple algorithms for same task

**Interview Justification:**
"Formatters use Strategy - each formatter has different algorithm (Simple vs JSON vs Pattern). Client (Appender) doesn't know implementation details. Easy to add new formatters without modifying appenders."

---

### Pattern 3: Template Method Pattern
**Where:** Appender.append()

**Structure:**
```java
public abstract class Appender {
    // Template method
    public final void append(LogMessage msg) {
        if (!shouldLog(msg)) return;    // Pre-condition
        doAppend(msg);                   // Hook method
    }
    
    protected abstract void doAppend(LogMessage msg); // Subclass implements
}
```

**Interview Justification:**
"Template Method defines skeleton (check filters, check level) and delegates actual writing to subclass. Ensures all appenders follow same flow."

---

### Pattern 4: Decorator Pattern
**Where:** AsyncAppender

**Structure:**
```java
AsyncAppender wraps any Appender
  - FileAppender → AsyncAppender(FileAppender)
  - ConsoleAppender → AsyncAppender(ConsoleAppender)
```

**Interview Justification:**
"Decorator adds async behavior to any appender without inheritance explosion. Don't need AsyncFileAppender, AsyncConsoleAppender, etc. Wrap any appender dynamically."

---

### Pattern 5: Chain of Responsibility
**Where:** Filter chain, Logger hierarchy

**Structure:**
```java
// Filter chain
for (Filter filter : filters) {
    if (!filter.allow(msg)) return false;
}

// Logger hierarchy (additivity)
Logger → log to own appenders
      → pass to parent
      → parent logs to its appenders
```

**Interview Justification:**
"Filters form a chain - message passes through all, any can reject. Logger hierarchy is also CoR - child logs, passes to parent, parent logs, passes to grandparent."

---

### Pattern 6: Factory Pattern
**Where:** LoggerFactory.getLogger()

**Interview Justification:**
"Factory centralizes logger creation. Handles caching (reuse existing loggers), builds hierarchy (find parent), ensures consistency."

---

### Pattern 7: Builder Pattern (Optional Extension)

**For Complex Configuration:**
```java
Logger logger = LoggerBuilder
    .forClass(MyClass.class)
    .setLevel(LogLevel.DEBUG)
    .addAppender(new FileAppender("app.log", new JSONFormatter()))
    .addFilter(new LevelFilter(LogLevel.INFO))
    .setAdditivity(false)
    .build();
```

---

## 🔒 Step 8: Handle Concurrency (10 minutes)

### Critical Section Analysis

#### 1. **Logger Creation (LoggerFactory)**

**Problem:** Multiple threads calling `getLogger("com.app.UserService")` simultaneously

**Solution:**
```java
public synchronized Logger getLogger(String name) {
    if (loggerCache.containsKey(name)) {
        return loggerCache.get(name);
    }
    Logger logger = createLogger(name);
    loggerCache.put(name, logger);
    return logger;
}
```

**Alternative (Double-Checked Locking):**
```java
public Logger getLogger(String name) {
    Logger logger = loggerCache.get(name);
    if (logger == null) {
        synchronized (lock) {
            logger = loggerCache.get(name);
            if (logger == null) {
                logger = createLogger(name);
                loggerCache.put(name, logger);
            }
        }
    }
    return logger;
}
```

**Interview Explanation:**
"getLogger() is synchronized at method level for simplicity. Logger creation is rare (happens once per logger name), so performance impact is negligible. For high-concurrency, use ConcurrentHashMap.computeIfAbsent()."

---

#### 2. **File Writing**

**Problem:** Multiple threads writing to same file

**Solution:**
```java
@Override
protected synchronized void doAppend(LogMessage message) {
    writer.write(formatter.format(message));
}
```

**Why synchronized?**
- Prevents interleaved writes
- File writes are NOT atomic in Java
- Without sync: "User A logged in User B logged out" could become "User A loggeUser B logged out d in"

---

#### 3. **Async Queue Operations**

**Problem:** Producer threads adding to queue, consumer thread removing

**Solution:**
```java
private final BlockingQueue<LogMessage> queue = 
    new ArrayBlockingQueue<>(queueSize);

// Producer (logger thread)
public void doAppend(LogMessage message) {
    queue.offer(message, 100, TimeUnit.MILLISECONDS);
}

// Consumer (background thread)
private void processQueue() {
    while (!shutdown) {
        LogMessage msg = queue.poll(100, TimeUnit.MILLISECONDS);
        if (msg != null) {
            targetAppender.append(msg);
        }
    }
}
```

**Interview Explanation:**
"BlockingQueue is thread-safe. Producer uses offer() (non-blocking with timeout), consumer uses poll() (blocking with timeout). If queue full, we drop messages (with warning) rather than block application thread."

---

#### 4. **Shutdown Sequence**

**Problem:** Application shutting down while logs in async queue

**Solution:**
```java
public void close() {
    shutdown = true;           // Signal worker to stop
    flush();                   // Wait for queue to empty
    executor.shutdown();       // Stop executor
    executor.awaitTermination(5, TimeUnit.SECONDS);
    targetAppender.close();    // Close underlying appender
}
```

**Interview Explanation:**
"Shutdown is multi-phase: (1) stop accepting new logs, (2) flush pending logs, (3) terminate worker thread, (4) close file handles. Ensures no lost ERROR/FATAL logs."

---

### Deadlock Prevention

**Potential Deadlock:**
```
Thread A: locks Logger, tries to lock Appender
Thread B: locks Appender, tries to lock Logger
```

**Prevention:** Lock hierarchy
```java
// Always lock in order: Logger → Appender → File
// Never lock Appender → Logger
```

**Our Design:**
- Logger.log() is NOT synchronized (no lock held)
- Appender.doAppend() IS synchronized (lock acquired only in appender)
- No nested locks → no deadlock

---

## 💡 Step 9: Interview Discussion Points

### Question 1: "How would you implement MDC (Mapped Diagnostic Context)?"

**Answer:**

"MDC stores contextual information per thread (e.g., user ID, request ID) that gets included in every log.

**Implementation:**
```java
public class MDC {
    private static final ThreadLocal<Map<String, String>> contextMap = 
        ThreadLocal.withInitial(HashMap::new);
    
    public static void put(String key, String value) {
        contextMap.get().put(key, value);
    }
    
    public static String get(String key) {
        return contextMap.get().get(key);
    }
    
    public static void clear() {
        contextMap.remove();
    }
    
    public static Map<String, String> getCopyOfContextMap() {
        return new HashMap<>(contextMap.get());
    }
}

// Usage in web request
@WebFilter
public class MDCFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", getCurrentUserId());
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}

// Formatter includes MDC
public class MDCAwareFormatter implements Formatter {
    public String format(LogMessage msg) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return String.format("[%s] [%s] %s", 
            mdc.get("requestId"),
            mdc.get("userId"),
            msg.getMessage()
        );
    }
}
```

**Output:**
```
[req-12345] [user-789] User logged in
[req-12345] [user-789] Fetched user profile
```

**Interview Discussion:**
- ThreadLocal ensures thread safety
- Each thread has its own context
- Must clear on thread reuse (thread pools)
- Useful for distributed tracing"

---

### Question 2: "How to handle log aggregation for microservices?"

**Answer:**

"In microservices, centralized logging is critical.

**Architecture:**
```
Microservice A ──→ Log Agent (Filebeat/Fluentd) ─┐
Microservice B ──→ Log Agent                     ├──→ Message Queue (Kafka) ──→ Elasticsearch ──→ Kibana
Microservice C ──→ Log Agent                     ┘
```

**Implementation:**

**Step 1: Structured Logging (JSON)**
```java
// All services use JSONFormatter
logger.addAppender(new FileAppender("app.log", new JSONFormatter()));
```

**Output:**
```json
{
  "timestamp": "2026-02-01T10:30:45",
  "service": "user-service",
  "level": "INFO",
  "trace_id": "abc123",
  "message": "User logged in"
}
```

**Step 2: RemoteAppender (send directly to aggregator)**
```java
public class KafkaAppender extends Appender {
    private KafkaProducer<String, String> producer;
    private String topic;
    
    public KafkaAppender(String bootstrapServers, String topic) {
        super(new JSONFormatter());
        this.topic = topic;
        
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        
        this.producer = new KafkaProducer<>(props);
    }
    
    @Override
    protected void doAppend(LogMessage message) {
        String json = formatter.format(message);
        ProducerRecord<String, String> record = 
            new ProducerRecord<>(topic, json);
        
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Failed to send log to Kafka: " + 
                                 exception.getMessage());
            }
        });
    }
    
    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
```

**Step 3: Add tracing context**
```java
logger.info("User logged in");
// Gets automatically enriched with:
// - service name (from config)
// - trace ID (from MDC)
// - host name
// - environment (prod/staging)
```

**Benefits:**
- Centralized search across all services
- Correlation by trace ID
- Alerting on patterns
- Real-time monitoring"

---

### Question 3: "How to optimize logging performance?"

**Answer:**

"Logging optimization has multiple dimensions:

**1. Conditional Logging (Lazy Evaluation)**

**Bad:**
```java
logger.debug("User data: " + user.toDetailedString()); 
// toDetailedString() called even if DEBUG disabled
```

**Good:**
```java
if (logger.isDebugEnabled()) {
    logger.debug("User data: " + user.toDetailedString());
}
```

**Better:**
```java
logger.debug("User data: {}", user::toDetailedString);
// Supplier pattern - only evaluated if needed
```

**2. Async Logging**

**Throughput:**
```
Synchronous: 1,000 logs/sec
Asynchronous: 50,000 logs/sec (50x improvement)
```

**3. Batched Writing**

```java
public class BatchedFileAppender extends Appender {
    private List<String> buffer = new ArrayList<>();
    private static final int BATCH_SIZE = 100;
    
    protected synchronized void doAppend(LogMessage message) {
        buffer.add(formatter.format(message));
        
        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }
    
    private void flush() {
        writer.write(String.join("", buffer));
        buffer.clear();
    }
}
```

**4. Lock-Free Data Structures**

```java
// Replace synchronized with lock-free
private final Queue<LogMessage> queue = 
    new ConcurrentLinkedQueue<>(); // Lock-free
```

**5. Sampling (for high-frequency logs)**

```java
public class SamplingFilter implements Filter {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final int sampleRate; // 1 in N
    
    public SamplingFilter(int sampleRate) {
        this.sampleRate = sampleRate;
    }
    
    @Override
    public boolean allow(LogMessage message) {
        return counter.incrementAndGet() % sampleRate == 0;
    }
}

// Only log 1 in 100 messages
logger.addFilter(new SamplingFilter(100));
```

**Benchmark Results:**
```
No logging:                1.0ms
Synchronous logging:       15.0ms (15x slower)
Async logging:             1.2ms (1.2x slower)
Async + batching:          1.1ms (1.1x slower)
```

**Interview Recommendation:**
'For performance-critical code, use async logging with batching. For high-frequency logs (e.g., per-request metrics), use sampling.'"

---

### Question 4: "How to implement log compression and archival?"

**Answer:**

```java
public class ArchivingRotationPolicy implements RotationPolicy {
    private final RotationPolicy delegate; // Size or time-based
    private final String archiveDirectory;
    
    public ArchivingRotationPolicy(RotationPolicy delegate, String archiveDir) {
        this.delegate = delegate;
        this.archiveDirectory = archiveDir;
    }
    
    @Override
    public boolean shouldRotate(String currentFilePath) {
        return delegate.shouldRotate(currentFilePath);
    }
    
    @Override
    public String getNextFileName(String currentFilePath) {
        String rotatedFile = delegate.getNextFileName(currentFilePath);
        
        // Compress rotated file
        CompletableFuture.runAsync(() -> {
            compressFile(rotatedFile);
            moveToArchive(rotatedFile + ".gz");
            deleteOldArchives();
        });
        
        return rotatedFile;
    }
    
    private void compressFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             GZIPOutputStream gzos = new GZIPOutputStream(
                 new FileOutputStream(filePath + ".gz"))) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }
            
            // Delete original
            new File(filePath).delete();
            
        } catch (IOException e) {
            System.err.println("Compression failed: " + e.getMessage());
        }
    }
    
    private void moveToArchive(String gzFilePath) {
        Path source = Paths.get(gzFilePath);
        Path dest = Paths.get(archiveDirectory, source.getFileName().toString());
        
        try {
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Archive move failed: " + e.getMessage());
        }
    }
    
    private void deleteOldArchives() {
        // Keep only last 30 days
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        
        try (DirectoryStream<Path> stream = 
             Files.newDirectoryStream(Paths.get(archiveDirectory), "*.gz")) {
            
            for (Path entry : stream) {
                BasicFileAttributes attrs = 
                    Files.readAttributes(entry, BasicFileAttributes.class);
                
                if (attrs.creationTime()
                        .toInstant()
                        .isBefore(cutoff.toInstant(ZoneOffset.UTC))) {
                    Files.delete(entry);
                    System.out.println("Deleted old archive: " + entry.getFileName());
                }
            }
        } catch (IOException e) {
            System.err.println("Archive cleanup failed: " + e.getMessage());
        }
    }
}

// Usage
FileAppender fileAppender = new FileAppender("app.log", new SimpleFormatter());
RotationPolicy policy = new ArchivingRotationPolicy(
    new SizeBasedRotationPolicy(100), // 100MB
    "/var/log/archives"
);
fileAppender.setRotationPolicy(policy);
```

**Compression Ratios:**
```
Text logs:        10:1 (100MB → 10MB)
JSON logs:        15:1 (100MB → 6.7MB)
Structured logs:  20:1 (100MB → 5MB)
```

---

### Question 5: "How would you add support for log sampling based on log level?"

**Answer:**

```java
public class LevelBasedSamplingFilter implements Filter {
    private final Map<LogLevel, Integer> sampleRates;
    private final Map<LogLevel, AtomicInteger> counters;
    
    public LevelBasedSamplingFilter() {
        this.sampleRates = new EnumMap<>(LogLevel.class);
        this.counters = new EnumMap<>(LogLevel.class);
        
        // Default sample rates
        sampleRates.put(LogLevel.DEBUG, 100);  // 1 in 100
        sampleRates.put(LogLevel.INFO, 10);    // 1 in 10
        sampleRates.put(LogLevel.WARN, 1);     // Always
        sampleRates.put(LogLevel.ERROR, 1);    // Always
        sampleRates.put(LogLevel.FATAL, 1);    // Always
        
        for (LogLevel level : LogLevel.values()) {
            counters.put(level, new AtomicInteger(0));
        }
    }
    
    public void setSampleRate(LogLevel level, int rate) {
        sampleRates.put(level, rate);
    }
    
    @Override
    public boolean allow(LogMessage message) {
        LogLevel level = message.getLevel();
        int rate = sampleRates.get(level);
        
        if (rate == 1) {
            return true; // Always log
        }
        
        AtomicInteger counter = counters.get(level);
        return counter.incrementAndGet() % rate == 0;
    }
}

// Usage
logger.addFilter(new LevelBasedSamplingFilter());
// DEBUG: 1 in 100
// INFO: 1 in 10
// WARN/ERROR/FATAL: Always logged
```

---

## ✅ Step 10: SOLID Principles Check

### S - Single Responsibility Principle
| Class | Responsibility | One Reason to Change? |
|-------|----------------|----------------------|
| Logger | Receive log calls, route to appenders | ✅ Only if logging API changes |
| Appender | Write logs to destination | ✅ Only if output mechanism changes |
| Formatter | Format log messages | ✅ Only if format structure changes |
| Filter | Decide if message should be logged | ✅ Only if filtering logic changes |
| LoggerFactory | Create and cache loggers | ✅ Only if creation logic changes |

### O - Open/Closed Principle
- ✅ Add new Appender (ElasticsearchAppender) without modifying Appender class
- ✅ Add new Formatter (CSVFormatter) without modifying Formatter interface
- ✅ Add new Filter (RateLimitFilter) without modifying Filter interface
- ✅ Add new RotationPolicy without modifying FileAppender

### L - Liskov Substitution Principle
```java
Appender appender = new FileAppender(...);  // Can substitute
appender = new ConsoleAppender(...);        // Any appender works
logger.addAppender(appender);               // No behavior change

Formatter formatter = new JSONFormatter();  // Can substitute
formatter = new SimpleFormatter();          // Any formatter works
```
✅ All substitutions work correctly

### I - Interface Segregation Principle
- ✅ Formatter: Single method `format()`
- ✅ Filter: Single method `allow()`
- ✅ RotationPolicy: Two related methods
- ✅ No fat interfaces

### D - Dependency Inversion Principle
```java
// Appender depends on Formatter interface
protected Formatter formatter; // ✅ Interface, not concrete

// Logger depends on Appender abstraction
private List<Appender> appenders; // ✅ Abstract, not concrete
```
✅ High-level modules depend on abstractions

---

## 🎯 Interview Tips & Talking Points

### Opening (30 seconds):
"Let me understand requirements first - log levels, destinations, performance needs, scale. Then I'll identify core entities (Logger, Appender, Formatter), design relationships using appropriate patterns (Strategy, Template Method, Decorator), and implement with thread safety."

### During Entity Identification (2 minutes):
"Core entities: Logger (API entry point), Appender (output strategy), Formatter (format strategy), Filter (chain of responsibility). Logger owns appenders (aggregation because appenders can be shared). Appender owns formatter (also aggregation)."

### During Pattern Discussion (3 minutes):
"Using multiple patterns: Strategy for appenders and formatters (swap implementations), Template Method in Appender (append() calls doAppend()), Decorator for AsyncAppender (wraps any appender to add async behavior), Chain of Responsibility for filters..."

### During Concurrency Discussion (2 minutes):
"Critical sections: logger creation (synchronized), file writing (synchronized doAppend), async queue (BlockingQueue). Logger.log() is NOT synchronized - each appender handles its own locking. No nested locks prevents deadlock..."

### When Asked About Performance:
"Key optimizations: async logging (moves I/O off critical path), batched writes (reduce syscalls), conditional logging (lazy evaluation), sampling for high-frequency logs. Async gives 50x throughput improvement..."

### When Asked About Extensions:
"Framework is extensible via interfaces: add KafkaAppender for distributed logging, add ElasticsearchAppender for full-text search, add CustomFilter for business logic filtering. Configuration via builder pattern or config file..."

### Closing (1 minute):
"Design uses appropriate patterns for extensibility, handles thread safety properly, optimizes performance through async logging, and follows SOLID principles. Key trade-offs: async gains performance but adds complexity, sampling reduces volume but may miss important logs."

---

## 📈 Complexity Analysis

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| log() call | O(1) | O(1) |
| log() with filtering | O(F) | O(1) |
| Synchronous append | O(M) | O(1) |
| Async append | O(1) | O(Q) |
| Format message | O(M) | O(M) |
| getLogger() | O(1) amortized | O(1) |
| Rotate file | O(N) | O(1) |

Where:
- F = number of filters
- M = message length
- Q = queue size
- N = file size

**Overall System:**
- Space: O(L + A × M) where L = loggers, A = appenders, M = avg message size
- Async queue: O(Q × M) where Q = queue capacity

---

## 🎓 Key Takeaways

### Interview Success Formula:

1. **Clarify** (5 min) - Understand levels, destinations, performance, scale
2. **Requirements** (7 min) - Functional point-wise, deduce NFRs using SCAMPS
3. **Entities** (12 min) - Logger, Appender, Formatter, Filter hierarchies
4. **Relationships** (15 min) - Aggregation for appenders, Strategy for formatters
5. **Class Diagrams** (12 min) - Focus on Template Method, Decorator patterns
6. **Implementation** (25 min) - Core API, async handling, thread safety
7. **Patterns** (5 min) - Strategy, Template Method, Decorator, Singleton, CoR
8. **Extensions** (10 min) - MDC, aggregation, compression, sampling

### What Makes This Design Good:

✅ **Separation of Concerns** - Logger, Appender, Formatter have distinct roles  
✅ **Extensible** - Easy to add new appenders, formatters, filters  
✅ **Performance** - Async logging, batching, conditional evaluation  
✅ **Thread-Safe** - Proper synchronization, lock-free queues  
✅ **Hierarchical** - Logger inheritance mirrors package structure  
✅ **Flexible** - Multiple appenders, filters, formats per logger  

### Common Mistakes to Avoid:

❌ Not considering performance (synchronous only)  
❌ Forgetting thread safety (file corruption)  
❌ Tight coupling (Logger creates FileWriter directly)  
❌ No filtering mechanism  
❌ No hierarchy (every logger independent)  
❌ Blocking on I/O (no async option)  
❌ Not handling appender failures gracefully  

---

**This systematic approach works for any infrastructure design problem!**
