import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerService {
    private static final String ROOT_NAME = "ROOT";
    private static final LoggerService INSTANCE = new LoggerService();

    private volatile LogLevel logLevel = LogLevel.INFO;
    private final Map<String, Logger> loggerCache = new ConcurrentHashMap<>();
    private final Logger rootLogger;

    private LoggerService() {
        rootLogger = new Logger(ROOT_NAME, this);
        rootLogger.setAdditive(false);
        loggerCache.put(ROOT_NAME, rootLogger);
    }

    public static LoggerService getInstance() {
        return INSTANCE;
    }

    public Logger getRootLogger() {
        return rootLogger;
    }

    public Logger getLogger(String name) {
        if (name == null || name.isBlank() || ROOT_NAME.equals(name)) {
            return rootLogger;
        }
        return loggerCache.computeIfAbsent(name, this::createLogger);
    }

    private Logger createLogger(String name) {
        Logger logger = new Logger(name, this);
        logger.setParent(resolveParent(name));
        return logger;
    }

    private Logger resolveParent(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot <= 0) {
            return rootLogger;
        }
        String parentName = name.substring(0, lastDot);
        return getLogger(parentName);
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void addAppender(String loggerName, Appender appender) {
        Logger logger = loggerCache.get(loggerName);
        if (logger == null) {
            throw new IllegalStateException("Logger must be created before adding appenders: " + loggerName);
        }
        logger.addAppender(appender);
    }

    public void addAppender(Logger logger, Appender appender) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger cannot be null");
        }
        logger.addAppender(appender);
    }

    public void addRootAppender(Appender appender) {
        rootLogger.addAppender(appender);
    }

    public void publish(Logger sourceLogger, LogEvent event) {
        Set<Appender> appenders = collectAppenders(sourceLogger);
        for (Appender appender : appenders) {
            appender.append(event);
        }
    }


    private Set<Appender> collectAppenders(Logger sourceLogger) {
        Set<Appender> collected = new LinkedHashSet<>();
        Logger cursor = sourceLogger;
        while (cursor != null) {
            collected.addAll(cursor.getOwnAppenders());
            if (!cursor.isAdditive()) {
                break;
            }
            cursor = cursor.getParent();
        }
        return collected;
    }
}
