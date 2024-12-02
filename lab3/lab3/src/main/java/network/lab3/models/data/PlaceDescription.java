package network.lab3.models.data;

import lombok.Data;

@Data
public class PlaceDescription {
    private double lat;
    private double lng;
    private String name;
    private String kinds;

    private String description;

    @Override
    public String toString() {
        return "Место: " + name +
                "\n\tтип:" + kinds +
                "\n\tОписание: " + description;
    }
}
