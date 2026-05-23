public class Car {
    private final String id;
    private final String model;
    private int currentOdometerReading;
    private boolean available;

    public Car(String id, String model, int currentOdometerReading) {
        this.id = id;
        this.model = model;
        this.currentOdometerReading = currentOdometerReading;
        this.available = true;
    }

    public String getId() { return id; }
    public String getModel() { return model; }
    public int getCurrentOdometerReading() { return currentOdometerReading; }
    public boolean isAvailable() { return available; }

    public void markBooked() { this.available = false; }
    public void markAvailable() { this.available = true; }
    public void updateOdometer(int newReading) { this.currentOdometerReading = newReading; }
}
