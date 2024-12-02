package network.lab3.services;

import com.google.gson.Gson;
import network.lab3.models.data.Weather;
import network.lab3.models.response.WeatherResponse;
import network.lab3.utils.ConfigParser;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;

public class WeatherService {

    public Request getRequest(double lat, double lng) {
        String apiKey = ConfigParser.getValue("WEATHER_KEY");
        String apiUrl = ConfigParser.getValue("WEATHER_URL") + "?units=metric" + "&lat=" + lat + "&lon=" + lng +"&appid=" + apiKey;

        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        return request;

    }

    public WeatherResponse responseBody(ResponseBody responseBody) throws IOException {
        String json = responseBody.string();
        return (new Gson()).fromJson(json, WeatherResponse.class);
    }

    public Weather getFromResponse(WeatherResponse weatherResponse) {
        Weather weather = new Weather();
        weather.setTemperature(weatherResponse.getMain().getTemp());
        weather.setFeelsLikeTemperature(weatherResponse.getMain().getFeelsLike());
        weather.setWindSpeed(weatherResponse.getWind().getSpeed());
        weather.setWindDirection(weatherResponse.getWind().getDeg());
        weather.setPressure(weatherResponse.getMain().getPressure());
        weather.setHumidity(weatherResponse.getMain().getHumidity());
        weather.setDesc(weatherResponse.getWeather()[0].getDescription());

        return weather;
    }
}
