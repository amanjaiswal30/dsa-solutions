// Elevator.java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Elevator {
    private final int id;
    private int currentFloor;
    private ElevatorState state;

    // Display shows only current floor as per requirement.
    private final Display display;

    // One button per floor inside this elevator cabin.
    private final List<InternalButton> internalButtons;

    // FIFO queues for pending stops by direction.
    private final Queue<Integer> upQueue;
    private final Queue<Integer> downQueue;

    private final int totalFloors;

    public Elevator(int id, int totalFloors) {
        this.id = id;
        this.totalFloors = totalFloors;
        this.currentFloor = 0; // ground floor
        this.state = ElevatorState.IDLE;
        this.display = new Display();
        this.upQueue = new LinkedList<>();
        this.downQueue = new LinkedList<>();

        this.internalButtons = new ArrayList<>();
        for (int i = 0; i < totalFloors; i++) {
            internalButtons.add(new InternalButton(i));
        }
    }

    /** Adds an internal floor request (pressed from inside cabin). */
    public void addInternalRequest(int floor) {
        if (floor > currentFloor) {
            upQueue.add(floor);
        } else if (floor < currentFloor) {
            downQueue.add(floor);
        } else {
            // Already at requested floor: simulate stop/open-close delay.
            stopAtFloor();
            return;
        }
        processRequests();
    }

    /** Adds an external request assigned by ElevatorController. */
    public void addExternalRequest(int floor, Direction direction) {
        if (direction == Direction.UP) {
            upQueue.add(floor);
        } else {
            downQueue.add(floor);
        }
        processRequests();
    }

    /**
     * Drains pending requests.
     * Rule:
     * - Continue UP if already moving up, or if idle and UP requests exist.
     * - Otherwise serve DOWN requests.
     */
    private void processRequests() {
        while (!upQueue.isEmpty() || !downQueue.isEmpty()) {
            if (state == ElevatorState.MOVING_UP
                    || (state == ElevatorState.IDLE && !upQueue.isEmpty())) {
                moveUp();
            } else if (state == ElevatorState.MOVING_DOWN || !downQueue.isEmpty()) {
                moveDown();
            }
        }
        state = ElevatorState.IDLE;
        display.update(currentFloor);
    }

    /** Serve all queued UP stops in FIFO order. */
    private void moveUp() {
        state = ElevatorState.MOVING_UP;
        while (!upQueue.isEmpty()) {
            int nextFloor = upQueue.poll();
            travelTo(nextFloor);
        }
    }

    /** Serve all queued DOWN stops in FIFO order. */
    private void moveDown() {
        state = ElevatorState.MOVING_DOWN;
        while (!downQueue.isEmpty()) {
            int nextFloor = downQueue.poll();
            travelTo(nextFloor);
        }
    }

    /** Moves floor-by-floor to target and updates display at each step. */
    private void travelTo(int targetFloor) {
        System.out.println("[Elevator-" + id + "] Travelling from floor " + currentFloor + " to " + targetFloor);
        while (currentFloor != targetFloor) {
            if (currentFloor < targetFloor) {
                currentFloor++;
            } else {
                currentFloor--;
            }
            display.update(currentFloor);
            sleep(500); // transit time between floors
        }
        stopAtFloor();
    }

    /** Simulates dwell time at a stop (door mechanics intentionally omitted). */
    private void stopAtFloor() {
        System.out.println("[Elevator-" + id + "] Stopped at floor: " + currentFloor);
        sleep(1000);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public ElevatorState getState() {
        return state;
    }

    public List<InternalButton> getInternalButtons() {
        return internalButtons;
    }

    /** Public entrypoint for cabin button press. */
    public void pressInternalButton(int floor) {
        if (floor >= 0 && floor < totalFloors) {
            internalButtons.get(floor).press(); // log/UX only
            addInternalRequest(floor);          // scheduling happens in elevator
        } else {
            System.out.println("[Elevator-" + id + "] Invalid floor: " + floor);
        }
    }
}
