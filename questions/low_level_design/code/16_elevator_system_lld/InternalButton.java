// InternalButton.java
public class InternalButton {
    private final int floor;

    public InternalButton(int floor) {
        this.floor = floor;
    }

    public void press() {
        System.out.println("[InternalButton] Requested floor: " + floor);
    }
}
