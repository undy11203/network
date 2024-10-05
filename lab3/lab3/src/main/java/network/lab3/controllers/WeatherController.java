package network.lab3.controllers;

import network.lab3.models.data.Weather;
import network.lab3.models.response.WeatherResponse;
import network.lab3.services.WeatherService;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class WeatherController extends BaseApiController<Weather, WeatherResponse> {
    private final WeatherService weatherService;
    private final double lat;
    private final double lng;

    public WeatherController(double lat, double lng) {
        super();
        this.weatherService = new WeatherService();
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected Request getRequest() {
        return weatherService.getRequest(lat, lng);
    }

    @Override
    protected WeatherResponse parseResponse(ResponseBody responseBody) throws IOException {
        return weatherService.responseBody(responseBody);
    }

    @Override
    protected Weather extractResult(WeatherResponse response) {
        return weatherService.getFromResponse(response);
    }
}


//public class WeatherContoller {
//    private final OkHttpClient httpClient;
//    private final WeatherService weatherService;
//
//    public WeatherContoller() {
//        this.httpClient = new OkHttpClient();
//        this.weatherService = new WeatherService();
//    }
//
//    public CompletableFuture<Weather> get(double lat, double lng) {
//        CompletableFuture<Weather> future = new CompletableFuture<>();
//
//        httpClient.newCall(weatherService.getRequest(lat, lng)).enqueue(new okhttp3.Callback() {
//            @Override
//            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    ResponseBody responseBody = response.body();
//                    if (responseBody != null) {
//                        WeatherResponse weatherResponse = weatherService.responseBody(responseBody);
//                        future.complete(weatherService.getFromResponse(weatherResponse));
//                    } else {
//                        future.completeExceptionally(new IOException("Empty response body"));
//                    }
//                } else {
//                    future.completeExceptionally(new IOException("Request failed with status code: " + response.code()));
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
//                future.completeExceptionally(e);
//            }
//        });
//
//        return future;
//    }
//}
