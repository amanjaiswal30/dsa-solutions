public class Location {
    double latitude;
    double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    double distanceTo(Location location) {
        double x = Math.abs(location.latitude - this.latitude);
        double y = Math.abs(location.longitude - this.longitude);
        return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    }
}
