// Main.java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        int totalFloors = 10;
        int totalElevators = 3;

        ElevatorController controller = ElevatorController.getInstance(
                totalElevators, totalFloors, new NearestElevatorStrategy()
        );

        // Person on floor 0 wants to go UP
        Elevator assigned = controller.handleExternalRequest(0, Direction.UP);
        Thread.sleep(1000);

        if (assigned != null) {
            assigned.pressInternalButton(5);
        }

        // Another person on floor 7 wants to go DOWN
        Elevator assigned2 = controller.handleExternalRequest(7, Direction.DOWN);
        Thread.sleep(1000);

        // They press floor 2 inside the elevator that was assigned to them
        if (assigned2 != null) {
            assigned2.pressInternalButton(2);
        }
    }
}
