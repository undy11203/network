package network.lab3.models.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PlaceResponse {
    private Place[] places;

    public PlaceResponse(Place[] places) {
        this.places = places;
    }

    @Data
    public static class Place {
        private String name;
        private String osm;
        private String xid;
        private String wikidata;
        private int rate; // Добавлено поле rate
        private String kinds; // Изменено поле kind на kinds
        private Point point;

        @Data
        public static class Point {
            @SerializedName("lon")
            private double lng;

            private double lat;
        }
    }
}
