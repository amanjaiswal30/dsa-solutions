public class ConsoleAppender implements Appender {
    @Override
    public void append(LogEvent event) {
        System.out.printf(
                "%s [%s] %s %s - %s%n",
                event.getTimestamp(),
                event.getThreadName(),
                event.getLevel(),
                event.getLoggerName(),
                event.getMessage()
        );
    }
}
