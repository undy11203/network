package network.lab3.services;

import com.google.gson.Gson;
import network.lab3.models.response.PlaceDescriptorResponse;
import network.lab3.models.response.PlaceResponse;
import network.lab3.utils.ConfigParser;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class PlaceDescriptorService implements IService {
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

    public String[] getFromResponse(PlaceResponse placeResponse) {
        return null;
    }
}
