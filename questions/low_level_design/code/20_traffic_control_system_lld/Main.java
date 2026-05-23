import java.util.EnumMap;

public class Main {

    public static void main(String[] args) {
        Intersection intersection = buildIntersection("INT-1");

        SignalStrategy strategy = new RoundRobinSignalStrategy(
                new SignalTiming(30, 5), // NORMAL
                new SignalTiming(45, 5)  // PEAK
        );

        TrafficControlService service = new TrafficControlService(strategy, TrafficMode.NORMAL);
        service.runOneCycle(intersection);

        System.out.println("---- Switching to PEAK mode ----");
        service.setTrafficMode(TrafficMode.PEAK);
        service.runOneCycle(intersection);
    }

    private static Intersection buildIntersection(String id) {
        Intersection intersection = new Intersection();
        intersection.id = id;
        intersection.signalMap = new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            TrafficSignal signal = new TrafficSignal();
            signal.id = id + "-" + direction;
            signal.direction = direction;
            signal.signalColor = SignalColor.RED;
            intersection.signalMap.put(direction, signal);
        }

        return intersection;
    }
}
