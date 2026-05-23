import java.util.List;

public class Location {
    String cityName;
    List<Show> shows;

    public Location(String cityName, List<Show> shows) {
        this.cityName = cityName;
        this.shows = shows;
    }
}
