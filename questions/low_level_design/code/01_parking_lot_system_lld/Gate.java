import java.util.UUID;

abstract class Gate {
    String id;
    int lat;
    int lon;
    GateType gatetype;

    public Gate(int lat, int lon, GateType gatetype) {
        this.lat = lat;
        this.id = UUID.randomUUID().toString();
        this.lon = lon;
        this.gatetype = gatetype;
    }
}
