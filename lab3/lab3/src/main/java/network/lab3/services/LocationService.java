package network.lab3.services;

import com.google.gson.Gson;
import network.lab3.models.data.Location;
import network.lab3.models.response.LocationResponse;
import network.lab3.utils.ConfigParser;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class LocationService {

    public Request getRequest(String query) {
        String apiKey = ConfigParser.getValue("LOCATION_KEY");
        String apiUrl = ConfigParser.getValue("LOCATION_URL") + "?q=" + query + "&key=" + apiKey;

        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        return request;

    }

    public LocationResponse responseBody(ResponseBody responseBody) throws IOException {
        String json = responseBody.string();
        return (new Gson()).fromJson(json, LocationResponse.class);
    }

    public Location[] getFromResponse(LocationResponse locationResponse) {
        Location[] locations = new Location[locationResponse.getHits().length];
        for (int i = 0; i < locationResponse.getHits().length; i++) {
            locations[i] = new Location();

            locations[i].setLat(locationResponse.getHits()[i].getPoint().getLat());
            locations[i].setLng(locationResponse.getHits()[i].getPoint().getLng());
            locations[i].setName(locationResponse.getHits()[i].getName());
            StringBuilder addressBuilder = new StringBuilder();
            if (locationResponse.getHits()[i].getCountry() != null) {
                addressBuilder.append(locationResponse.getHits()[i].getCountry()).append(", ");
            }
            if (locationResponse.getHits()[i].getCity() != null) {
                addressBuilder.append(locationResponse.getHits()[i].getCity()).append(", ");
            }
            if (locationResponse.getHits()[i].getStreet() != null) {
                addressBuilder.append(locationResponse.getHits()[i].getStreet()).append(", ");
            }
            if (locationResponse.getHits()[i].getHousenumber() != null) {
                addressBuilder.append(locationResponse.getHits()[i].getHousenumber()).append(", ");
            }
            if (locationResponse.getHits()[i].getPostcode() != null) {
                addressBuilder.append(locationResponse.getHits()[i].getPostcode());
            }

            locations[i].setAddress(addressBuilder.toString());
        }

        return locations;
    }
}
