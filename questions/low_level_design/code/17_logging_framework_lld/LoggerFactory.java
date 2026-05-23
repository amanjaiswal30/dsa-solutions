public final class LoggerFactory {
    private LoggerFactory() {
    }

    private static LoggerService service() {
        return LoggerService.getInstance();
    }

    public static Logger getLogger(String name) {
        return service().getLogger(name);
    }

    public static Logger getLogger(Class<?> type) {
        if (type == null) {
            return getLogger("ROOT");
        }
        return getLogger(type.getName());
    }

    public static void setLogLevel(LogLevel logLevel) {
        service().setLogLevel(logLevel);
    }

    public static void addAppender(String loggerName, Appender appender) {
        service().addAppender(loggerName, appender);
    }

    public static void addAppender(Logger logger, Appender appender) {
        service().addAppender(logger, appender);
    }

    public static void addRootAppender(Appender appender) {
        service().addRootAppender(appender);
    }
}
