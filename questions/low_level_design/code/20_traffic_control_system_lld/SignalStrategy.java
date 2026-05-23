import java.util.List;

public interface SignalStrategy {
    List<Phase> getPhasePlan(Intersection intersection, TrafficMode trafficMode);
}
