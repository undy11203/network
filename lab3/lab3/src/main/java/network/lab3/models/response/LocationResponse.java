package network.lab3.models.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import lombok.Data;
import java.util.List;

@Data
public class LocationResponse {
    private Location[] hits;

    @Data
    public static class Location {
        private Point point;
        private Double[] extent;
        private String name;
        private String country;
        private String countrycode;
        private String city;
        private String street;
        private String postcode;
        private String housenumber;
        private Long osm_id;
        private String osm_type;
        private String osm_key;
        private String osm_value;
    }

    @Data
    public static class Point {
        private double lat;
        private double lng;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}

