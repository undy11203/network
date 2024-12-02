package network.lab3.controllers;

import network.lab3.models.data.PlaceDescription;
import network.lab3.models.response.PlaceDescriptorResponse;
import network.lab3.models.response.PlaceResponse;
import network.lab3.services.PlaceDescriptorService;
import network.lab3.services.PlaceService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DescriptionPlacesContoller {
    private final PlaceDescriptorService placeDescriptionService;
    private final String[] xids;
    private final OkHttpClient httpClient;

    public DescriptionPlacesContoller(String[] xids) {
        this.httpClient = new OkHttpClient();
        this.placeDescriptionService = new PlaceDescriptorService();
        this.xids = xids;
    }

    public ArrayList<PlaceDescription> searchPlacesDescriptions() {
        ArrayList<PlaceDescription> places = new ArrayList<>();
        List<CompletableFuture<PlaceDescription>> futures = new ArrayList<>();

        for (String xid : xids) {
            CompletableFuture<PlaceDescription> placeFuture = getOnePlaceDesc(xid);
            futures.add(placeFuture);
        }

        for (CompletableFuture<PlaceDescription> placeFuture : futures) {
            try {
                PlaceDescription place = placeFuture.join();
                places.add(place);
            } catch (CompletionException e) {
                //System.err.println("ERROR: " + e.getCause().getMessage());
            }
        }

        return places;
    }

//    public CompletableFuture<ArrayList<PlaceDescription>> searchPlacesDescriptions() {
//        List<CompletableFuture<PlaceDescription>> futures = new ArrayList<>();
//
//        for (String xid : xids) {
//            CompletableFuture<PlaceDescription> placeFuture = getOnePlaceDesc(xid);
//            futures.add(placeFuture);
//        }
//
//        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> {
//                    ArrayList<PlaceDescription> places = new ArrayList<>();
//                    for (CompletableFuture<PlaceDescription> placeFuture : futures) {
//                        try {
//                            places.add(placeFuture.join());
//                        } catch (CompletionException e) {
//                            System.err.println("Error fetching place description: " + e.getCause().getMessage());
//                            places.add(null);
//                        }
//                    }
//                    return places;
//                });
//    }

    private CompletableFuture<PlaceDescription> getOnePlaceDesc(String xid) {
        CompletableFuture<PlaceDescription> future = new CompletableFuture<>();
        httpClient.newCall(placeDescriptionService.getRequest(xid)).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        try {
                            PlaceDescriptorResponse parsedResponse =
                                    placeDescriptionService.responseBody(responseBody);

                            if (parsedResponse.getName() != null
                                    && parsedResponse.getPoint() != null) {
                                future.complete(extractResult(parsedResponse));
                            } else {
                                future.completeExceptionally(new NullPointerException("One of the mandatory fields is null"));
                            }
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    } else {
                        future.completeExceptionally(new IOException("Empty response body"));
                    }

                } else {
                    future.completeExceptionally(new IOException("Request failed with status code: " + response.code()));
                }
            }
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private PlaceDescription extractResult(PlaceDescriptorResponse parsedResponse) {
        return placeDescriptionService.getFromResponse(parsedResponse);
    }
}
