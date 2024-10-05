package network.lab3.controllers;

import network.lab3.models.data.PlaceDescription;
import network.lab3.models.response.PlaceDescriptorResponse;
import network.lab3.models.response.PlaceResponse;
import network.lab3.services.PlaceDescriptorService;
import network.lab3.services.PlaceService;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;

public class DescriptionPlacesContoller extends BaseApiController<ArrayList<PlaceDescription>,PlaceDescriptorResponse> {
    private final PlaceDescriptorService placeDescriptionService;
    private final String[] xids;

    public DescriptionPlacesContoller(String[] xids) {
        super();
        this.placeDescriptionService = new PlaceDescriptorService();
        this.xids = xids;
    }

    @Override
    protected Request getRequest() {
        return placeDescriptionService.getRequest();
    }

    @Override
    protected PlaceDescriptorResponse parseResponse(ResponseBody responseBody) throws IOException {
        return placeDescriptionService.responseBody(responseBody);
    }

    @Override
    protected ArrayList<PlaceDescription> extractResult(PlaceDescriptorResponse response) {
        return null;
    }
}
