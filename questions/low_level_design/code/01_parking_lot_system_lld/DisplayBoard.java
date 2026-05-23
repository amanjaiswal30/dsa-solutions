import java.util.HashMap;
import java.util.Map;

public class DisplayBoard {
    Map<SpotType, Integer> freeSpots;

    public DisplayBoard() {
        this.freeSpots = new HashMap<>();
    }

    void updateDisplayBoard(SpotType spotType, boolean hasVehicleEntered) {
        if(hasVehicleEntered) freeSpots.put(spotType, freeSpots.get(spotType) - 1);
        else freeSpots.put(spotType, freeSpots.get(spotType) + 1);
    }

    @Override
    public String toString() {
        return "DisplayBoard{" +
                "freeSpots=" + freeSpots +
                '}';
    }

    int getAvailableSportsForSpotType(SpotType spotType) {
        return freeSpots.get(spotType);
    }
}
