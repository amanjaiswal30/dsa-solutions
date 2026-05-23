public class ErrorConsoleAppender implements Appender {
    @Override
    public void append(LogEvent event) {
        if (event.getLevel().isEnabledFor(LogLevel.ERROR)) {
            System.err.printf(
                    "%s [%s] %s %s - %s%n",
                    event.getTimestamp(),
                    event.getThreadName(),
                    event.getLevel(),
                    event.getLoggerName(),
                    event.getMessage()
            );
        }
    }
}
