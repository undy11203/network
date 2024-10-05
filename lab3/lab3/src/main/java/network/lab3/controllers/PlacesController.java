package network.lab3.controllers;

import network.lab3.models.response.PlaceResponse;
import network.lab3.services.PlaceService;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class PlacesController extends BaseApiController<String[], PlaceResponse> {
    private final PlaceService placeService;
    private final double lat;
    private final double lng;

    public PlacesController(double lat, double lng) {
        super();
        this.placeService = new PlaceService();
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected Request getRequest() {
        return placeService.getRequest(lat, lng);
    }

    @Override
    protected PlaceResponse parseResponse(ResponseBody responseBody) throws IOException {
        return placeService.responseBody(responseBody);
    }

    @Override
    protected String[] extractResult(PlaceResponse response) {
        return placeService.getFromResponse(response);
    }
}
