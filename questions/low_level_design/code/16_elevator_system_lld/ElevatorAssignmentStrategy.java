import java.util.List;

// ElevatorAssignmentStrategy.java
public interface ElevatorAssignmentStrategy {
    Elevator assign(List<Elevator> elevators, int requestedFloor, Direction direction);
}
