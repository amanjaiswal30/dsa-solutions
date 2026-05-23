// ExternalButton.java
public class ExternalButton {
    private final int floor;
    private final Direction direction;

    public ExternalButton(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }

    public int getFloor() {
        return floor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void press() {
        System.out.println("[ExternalButton] Floor: " + floor + " Direction: " + direction);
    }
}
