public class Display {
    private int currentFloor;

    public void update(int floor) {
        this.currentFloor = floor;
        System.out.println("[Display] Floor: " + floor);
    }

    public int getCurrentFloor() {
        return currentFloor;
    }
}
