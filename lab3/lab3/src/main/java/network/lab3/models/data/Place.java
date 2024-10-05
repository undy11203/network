package network.lab3.models.data;

import lombok.Data;

@Data
public class Place {
    private double lat;
    private double lng;
    private String name;

    private String description;

    @Override
    public String toString() {
        return "Место: " + name +
                "\n\tDescription: " + description;
    }
}