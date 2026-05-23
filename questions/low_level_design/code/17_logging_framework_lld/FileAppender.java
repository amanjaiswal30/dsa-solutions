import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileAppender implements Appender {
    private final String filePath;

    public FileAppender(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public synchronized void append(LogEvent event) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.printf(
                    "%s [%s] %s %s - %s%n",
                    event.getTimestamp(),
                    event.getThreadName(),
                    event.getLevel(),
                    event.getLoggerName(),
                    event.getMessage()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to write log to file: " + filePath, e);
        }
    }
}
