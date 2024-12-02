package org.project.exceptions;

public class UserInputException extends Exception {
    private UserInputException(String message) {
        super(message);
    }

    public static UserInputException emptyWidth() {
        return new UserInputException("Please, enter width");
    }

    public static UserInputException invalidWidth(int max, int min) {
        return new UserInputException("Width value should be from " + min + " to " +max);
    }

    public static UserInputException notIntegerWidth() {
        return new UserInputException("Please, check width and enter again");
    }

    public static UserInputException emptyHeight() {
        return new UserInputException("Please, enter height");
    }

    public static UserInputException invalidHeight(int max, int min) {
        return new UserInputException("Height value should be from " + min + " to " +max);
    }

    public static UserInputException notIntegerHeight() {
        return new UserInputException("Please, check height and enter again");
    }

    public static UserInputException emptyFoodStatic() {
        return new UserInputException("Please, enter foodStatic");
    }

    public static UserInputException invalidFoodStatic(int max, int min) {
        return new UserInputException("FoodStatic value should be from " + min + " to " +max);
    }

    public static UserInputException notIntegerFoodStatic() {
        return new UserInputException("Please, check foodStatic and enter again");
    }

    public static UserInputException emptyDelay() {
        return new UserInputException("Please, enter delay");
    }

    public static UserInputException invalidDelay(int max, int min) {
        return new UserInputException("Delay value should be from " + min + " to " +max);
    }

    public static UserInputException notIntegerDelay() {
        return new UserInputException("Please, check delay and enter again");
    }

    public static UserInputException emptyNickname() {
        return new UserInputException("Please, enter nickname");
    }

    public static UserInputException emptyGameName() {
        return new UserInputException("Please, enter game name");
    }
}
