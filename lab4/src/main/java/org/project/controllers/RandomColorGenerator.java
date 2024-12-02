package org.project.controllers;

import javafx.scene.paint.Color;

import java.util.Random;

public class RandomColorGenerator {
    public Color generateRandomColor() {
        Random random = new Random();

        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return Color.rgb(red, green, blue);
    }
}
