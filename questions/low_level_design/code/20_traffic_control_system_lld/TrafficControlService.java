import java.util.List;
import java.util.Map;

public class TrafficControlService {

    private final SignalStrategy signalStrategy;
    private TrafficMode trafficMode;

    public TrafficControlService(SignalStrategy signalStrategy, TrafficMode initialMode) {
        this.signalStrategy = signalStrategy;
        this.trafficMode = initialMode;
    }

    public void setTrafficMode(TrafficMode trafficMode) {
        this.trafficMode = trafficMode;
    }

    public void runOneCycle(Intersection intersection) {
        List<Phase> plan = signalStrategy.getPhasePlan(intersection, trafficMode);
        if (plan.isEmpty()) {
            System.out.println("No phases available for intersection: " +
                    (intersection != null ? intersection.id : "null"));
            return;
        }

        System.out.println("Running cycle for intersection: " + intersection.id + " [Mode=" + trafficMode + "]");

        for (Phase phase : plan) {
            setAllRed(intersection.signalMap);

            TrafficSignal active = intersection.signalMap.get(phase.direction);
            if (active == null) {
                continue;
            }

            active.signalColor = SignalColor.GREEN;
            System.out.println("GREEN  -> " + phase.direction + " for " + phase.signalTiming.greenSec + " sec");

            active.signalColor = SignalColor.YELLOW;
            System.out.println("YELLOW -> " + phase.direction + " for " + phase.signalTiming.yellowSec + " sec");

            active.signalColor = SignalColor.RED;
            System.out.println("RED    -> " + phase.direction);
        }
    }

    private void setAllRed(Map<Direction, TrafficSignal> signalMap) {
        for (TrafficSignal signal : signalMap.values()) {
            signal.signalColor = SignalColor.RED;
        }
    }
}
