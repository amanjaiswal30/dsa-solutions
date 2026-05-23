import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger {
    private final String name;
    private final LoggerService service;
    private volatile LogLevel level;
    private volatile Logger parent;
    private volatile boolean additive = true;
    private final List<Appender> appenders = new ArrayList<>();

    public Logger(String name, LoggerService service) {
        this.name = name;
        this.service = service;
    }

    public Logger getParent() {
        return parent;
    }

    public void setParent(Logger parent) {
        this.parent = parent;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public boolean isAdditive() {
        return additive;
    }

    public synchronized void addAppender(Appender appender) {
        appenders.add(appender);
    }

    public synchronized List<Appender> getOwnAppenders() {
        return Collections.unmodifiableList(new ArrayList<>(appenders));
    }

    public LogLevel getEffectiveLevel() {
        if (level != null) {
            return level;
        }
        if (parent != null) {
            return parent.getEffectiveLevel();
        }
        return service.getLogLevel();
    }

    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    public void log(LogLevel eventLevel, String message) {
        if (!eventLevel.isEnabledFor(getEffectiveLevel())) {
            return;
        }

        LogEvent event = new LogEvent(eventLevel, name, message);
        service.publish(this, event);
    }
}
