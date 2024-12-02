package network.lab3.models.data;

import lombok.Data;

import java.util.List;

@Data
public class Result {
    Location location;
    List<PlaceDescription> places;
    Weather weather;

    public Result(Location location, List<PlaceDescription> places) {
        this.location = location;
        this.places = places;
    }

    public Result(Location selectedLocation, Weather weather, List<PlaceDescription> places) {
        this.location = selectedLocation;
        this.places = places;
        this.weather = weather;
    }
}

