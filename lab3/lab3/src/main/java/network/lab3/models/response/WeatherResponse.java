package network.lab3.models.response;

import network.lab3.models.data.Weather;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class WeatherResponse {
    private Coord coord;
    private Weather[] weather;
    private String base;
    private Main main;
    private int visibility;
    private Wind wind;
    private Rain rain;
    private Clouds clouds;
    private long dt;
    private Sys sys;
    private int timezone;
    private int id;
    private String name;
    private int cod;

    @Data
    public static class Coord {
        private double lon;
        private double lat;
    }

    @Data
    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
    }

    @Data
    public static class Main {
        private double temp;
        @SerializedName("feels_like") private double feelsLike;
        @SerializedName("temp_min") private double tempMin;
        @SerializedName("temp_max") private double tempMax;
        private long pressure;
        private long humidity;
        @SerializedName("sea_level") private Integer seaLevel;
        @SerializedName("grnd_level") private Integer grndLevel;
    }

    @Data
    public static class Wind {
        private double speed;
        private int deg;
        private Double gust;
    }

    @Data
    public static class Rain {
        @SerializedName("1h") private Double oneHour;
    }

    @Data
    public static class Clouds {
        private int all;
    }

    @Data
    public static class Sys {
        private int type;
        private int id;
        private String country;
        private long sunrise;
        private long sunset;
    }
}

