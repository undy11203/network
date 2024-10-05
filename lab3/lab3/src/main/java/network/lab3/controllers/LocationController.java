package network.lab3.controllers;

import network.lab3.models.data.Location;
import network.lab3.models.response.LocationResponse;
import network.lab3.services.LocationService;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LocationController extends BaseApiController<Location[], LocationResponse> {
    private final LocationService locationService;
    private final String locationName;

    public LocationController(String locationName) {
        super();
        this.locationService = new LocationService();
        this.locationName = locationName;
    }

    @Override
    protected Request getRequest() {
        return locationService.getRequest(locationName);
    }

    @Override
    protected LocationResponse parseResponse(ResponseBody responseBody) throws IOException {
        return locationService.responseBody(responseBody);
    }

    @Override
    protected Location[] extractResult(LocationResponse response) {
        return locationService.getFromResponse(response);
    }
}


//public class LocationController {
//    private final OkHttpClient httpClient;
//    private final LocationService locationService;
//
//    public LocationController() {
//        this.httpClient = new OkHttpClient();
//        this.locationService = new LocationService();
//    }
//
//    public CompletableFuture<Location[]> get(String locationName) {
//        CompletableFuture<Location[]> future = new CompletableFuture<>();
//
//        httpClient.newCall(locationService.getRequest(locationName)).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                future.completeExceptionally(e);
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    ResponseBody responseBody = response.body();
//                    if (responseBody != null) {
//                        LocationResponse locationResponse = locationService.responseBody(responseBody);
//                        future.complete(locationService.getFromResponse(locationResponse));
//                    } else {
//                        future.completeExceptionally(new IOException("Empty response body"));
//                    }
//                } else {
//                    future.completeExceptionally(new IOException("Request failed with status code: " + response.code()));
//                }
//            }
//        });
//
//        return future;
//    }
//}
