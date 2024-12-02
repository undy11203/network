package network.lab3.services;

import com.google.gson.Gson;
import network.lab3.models.response.PlaceResponse;
import network.lab3.utils.ConfigParser;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class PlaceService {
    public Request getRequest(double lat, double lng) {
        double width = 0.01; // ширина коробки
        double height = 0.01; // высота коробки

        double lonMin = lng - (width / 2);
        double latMin = lat - (height / 2);
        double lonMax = lng + (width / 2);
        double latMax = lat + (height / 2);

        String apiKey = ConfigParser.getValue("PLACE_KEY");
        String apiUrl = ConfigParser.getValue("PLACE_URL") +
                String.format("?lon_min=%.2f&lon_max=%.2f&lat_min=%.2f&lat_max=%.2f&format=json&apikey=", lonMin, lonMax, latMin, latMax) + apiKey;

        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        return request;
    }

    public PlaceResponse responseBody(ResponseBody responseBody) throws IOException {
        String json = responseBody.string();

        Gson gson = new Gson();

        return gson.fromJson("{\"places\":" + json + "}", PlaceResponse.class);
    }

    public String[] getFromResponse(PlaceResponse placeResponse) {
        PlaceResponse.Place[] places = placeResponse.getPlaces();
        String[] xids = new String[places.length];

        for (int i = 0; i < places.length; i++) {
            xids[i] = places[i].getXid();
        }

        return xids;
    }
}
