import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryAppender implements Appender {
    private final List<LogEvent> events = new ArrayList<>();

    @Override
    public synchronized void append(LogEvent event) {
        events.add(event);
    }

    public synchronized List<LogEvent> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }
}
