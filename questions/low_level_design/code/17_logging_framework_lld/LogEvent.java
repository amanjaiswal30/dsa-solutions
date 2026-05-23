import java.time.Instant;

public class LogEvent {
    private final Instant timestamp;
    private final LogLevel level;
    private final String loggerName;
    private final String message;
    private final String threadName;

    public LogEvent(LogLevel level, String loggerName, String message) {
        this.timestamp = Instant.now();
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
        this.threadName = Thread.currentThread().getName();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getMessage() {
        return message;
    }

    public String getThreadName() {
        return threadName;
    }
}
