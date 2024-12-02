package network.lab3.services;

import com.google.gson.Gson;
import network.lab3.models.data.PlaceDescription;
import network.lab3.models.response.PlaceDescriptorResponse;
import network.lab3.utils.ConfigParser;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class PlaceDescriptorService {
    public Request getRequest(String xid) {
        String apiKey = ConfigParser.getValue("PLACE_KEY");
        String apiUrl = ConfigParser.getValue("PLACE_DESCRIPTION_URL") + xid + "?apikey=" + apiKey;

        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        return request;
    }

    public PlaceDescriptorResponse responseBody(ResponseBody responseBody) throws IOException {
        String json = responseBody.string();

        Gson gson = new Gson();

        return gson.fromJson(json, PlaceDescriptorResponse.class);
    }

    public PlaceDescription getFromResponse(PlaceDescriptorResponse placeResponse) {
        PlaceDescription place = new PlaceDescription();
        place.setName(placeResponse.getName());
        place.setLat(placeResponse.getPoint().getLat());
        place.setLng(placeResponse.getPoint().getLon());
        place.setKinds(placeResponse.getKinds());
        if (placeResponse.getInfo() == null) {
            place.setDescription("not stated");
        } else {
            place.setDescription(placeResponse.getInfo().getDescr());
        }

        return place;
    }
}
