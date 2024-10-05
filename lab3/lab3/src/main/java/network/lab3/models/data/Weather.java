package network.lab3.models.data;

import lombok.Data;

@Data
public class Weather {
    private double temperature;
    private double feelsLikeTemperature;
    private double windSpeed;
    private double windDirection;
    private long pressure;
    private long humidity;
    private String desc;

    @Override
    public String toString() {
        return "\tТемпература: " + temperature + " °C\n\t" +
                "Ощущается как: " + feelsLikeTemperature + " °C\n\t" +
                "Скорость ветра: " + windSpeed + " м/с\n\t" +
                "Направление ветра: " + windDirection + "°\n\t" +
                "Давление: " + pressure + " гПа\n\t" +
                "Влажность: " + humidity + "%\n\t" +
                "Описание: " + desc;
    }
}
