// ElevatorController.java
import java.util.ArrayList;
import java.util.List;

public class ElevatorController {

    private static ElevatorController instance;

    private final List<Elevator> elevators;
    private final List<ExternalButton> externalButtons;  // Single list
    private final ElevatorAssignmentStrategy strategy;

    private ElevatorController(int totalElevators, int totalFloors, ElevatorAssignmentStrategy strategy) {
        this.strategy = strategy;
        this.elevators = new ArrayList<>();
        this.externalButtons = new ArrayList<>();

        for (int i = 0; i < totalElevators; i++) {
            elevators.add(new Elevator(i + 1, totalFloors));
        }

        // Create external buttons for each floor
        for (int floor = 0; floor < totalFloors; floor++) {
            externalButtons.add(new ExternalButton(floor, Direction.UP));
            externalButtons.add(new ExternalButton(floor, Direction.DOWN));
        }
    }

    public static ElevatorController getInstance(int totalElevators, int totalFloors, ElevatorAssignmentStrategy strategy) {
        if (instance == null) {
            instance = new ElevatorController(totalElevators, totalFloors, strategy);
        }
        return instance;
    }

    /** Internal controller handling */
    public Elevator handleExternalRequest(int floor, Direction direction) {
        Elevator assigned = strategy.assign(elevators, floor, direction);
        if (assigned != null) {
            assigned.addExternalRequest(floor, direction);
        }
        return assigned;
    }

    /** Get an external button at a floor with given direction */
    public ExternalButton getExternalButton(int floor, Direction direction) {
        int index = floor * 2 + (direction == Direction.UP ? 0 : 1);
        return externalButtons.get(index);
    }

    public List<Elevator> getElevators() { return elevators; }
}
