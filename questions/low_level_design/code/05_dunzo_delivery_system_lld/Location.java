public class Location {
    private final double lat;
    private final double lng;

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double distanceTo(Location other) {
        double dLat = other.lat - this.lat;
        double dLng = other.lng - this.lng;
        return Math.sqrt(dLat * dLat + dLng * dLng);
    }

    @Override
    public String toString() {
        return "(" + lat + ", " + lng + ")";
    }
}
