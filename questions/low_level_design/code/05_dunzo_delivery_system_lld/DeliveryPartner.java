public class DeliveryPartner extends User {
    private boolean available;
    private Location location;

    public DeliveryPartner(String name, String email, Location location) {
        super(name, email);
        this.available = true;
        this.location = location;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Location getLocation() {
        return location;
    }

    public void updateLocation(Location location) {
        this.location = location;
    }
}
