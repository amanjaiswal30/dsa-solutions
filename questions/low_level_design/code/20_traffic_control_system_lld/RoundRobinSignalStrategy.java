import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoundRobinSignalStrategy implements SignalStrategy {

    private final SignalTiming normalTiming;
    private final SignalTiming peakTiming;

    public RoundRobinSignalStrategy(SignalTiming normalTiming, SignalTiming peakTiming) {
        this.normalTiming = normalTiming;
        this.peakTiming = peakTiming;
    }

    @Override
    public List<Phase> getPhasePlan(Intersection intersection, TrafficMode trafficMode) {
        List<Phase> phases = new ArrayList<>();
        if (intersection == null || intersection.signalMap == null || intersection.signalMap.isEmpty()) {
            return phases;
        }

        SignalTiming timing = (trafficMode == TrafficMode.PEAK) ? peakTiming : normalTiming;

        // Deterministic order avoids random switching behavior.
        Direction[] order = new Direction[] {
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
        };

        Map<Direction, TrafficSignal> signalMap = intersection.signalMap;
        for (Direction direction : order) {
            if (signalMap.containsKey(direction)) {
                phases.add(new Phase(direction, timing));
            }
        }
        return phases;
    }
}
