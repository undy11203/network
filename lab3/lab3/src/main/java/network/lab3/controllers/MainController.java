package network.lab3.controllers;

import network.lab3.models.data.Location;
import network.lab3.models.data.PlaceDescription;
import network.lab3.models.data.Result;
import network.lab3.models.data.Weather;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MainController {
    public void start() {
        Scanner consoleScanner = new Scanner(System.in);
        String locationName = getLocation(consoleScanner);
        LocationController locationController = new LocationController(locationName);
        CompletableFuture<Location[]> locationFuture = locationController.search();
        System.out.println("Search location...");
        locationFuture.thenAccept(locations -> {
            try {
                Location currentLocation = chooseLocation(locations, consoleScanner);
                WeatherController weatherContoller = new WeatherController(currentLocation.getLat(), currentLocation.getLng());
                PlacesController placesController = new PlacesController(currentLocation.getLat(), currentLocation.getLng());

                CompletableFuture<Weather> weatherFuture = weatherContoller.search();

                System.out.println("Search weather...");
                CompletableFuture<String[]> placesFuture = placesController.search();
                System.out.println("Search places...");

                placesFuture.thenAccept((xids) -> {
                    DescriptionPlacesContoller descriptionPlaces = new DescriptionPlacesContoller(xids);
                    ArrayList<PlaceDescription> descriptions = descriptionPlaces.searchPlacesDescriptions();
                    System.out.println("Search descriptions...");

                    try {
                        Result result = new Result(currentLocation, weatherFuture.get(), descriptions);

                        showFinalResults(result);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }

                });

            } catch (Exception e) {
                System.out.println(e.getCause().getMessage());
            }
        });

    }

    private Location chooseLocation(Location[] locations,Scanner scanner) {
        if (locations.length > 0) {
            System.out.println("Выберите локацию:");
            for (int i = 0; i < locations.length; i++) {
                System.out.println(i + 1 + ". " + locations[i]);
            }

            System.out.print("Введите номер выбранной локации: ");
            int selectedLocationIndex = Integer.parseInt(scanner.nextLine()) - 1;
            System.out.println();

            return locations[selectedLocationIndex];
        }
        return null;
    }

    private String getLocation(Scanner scanner) {
        System.out.println("Введите локацию: ");
        return scanner.nextLine();
    }

    private void showFinalResults(Result result) {

        for (PlaceDescription place : result.getPlaces()) {
            System.out.println("\n" + place);
            System.out.println();
        }

    }
}
