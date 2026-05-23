// NearestElevatorStrategy.java
import java.util.List;

public class NearestElevatorStrategy implements ElevatorAssignmentStrategy {

    @Override
    public Elevator assign(List<Elevator> elevators, int requestedFloor, Direction direction) {
        Elevator best = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int distance = Math.abs(elevator.getCurrentFloor() - requestedFloor);

            // Prefer IDLE elevators or ones already moving in the same direction
            boolean sameDirection = (direction == Direction.UP && elevator.getState() == ElevatorState.MOVING_UP
                    && elevator.getCurrentFloor() <= requestedFloor)
                    || (direction == Direction.DOWN && elevator.getState() == ElevatorState.MOVING_DOWN
                    && elevator.getCurrentFloor() >= requestedFloor);

            boolean isIdle = elevator.getState() == ElevatorState.IDLE;

            if ((isIdle || sameDirection) && distance < minDistance) {
                minDistance = distance;
                best = elevator;
            }
        }

        // Fallback: pick closest regardless of state
        if (best == null) {
            for (Elevator elevator : elevators) {
                int distance = Math.abs(elevator.getCurrentFloor() - requestedFloor);
                if (distance < minDistance) {
                    minDistance = distance;
                    best = elevator;
                }
            }
        }

        return best;
    }
}
