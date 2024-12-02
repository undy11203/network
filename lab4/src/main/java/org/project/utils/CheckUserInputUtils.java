package org.project.utils;

import org.project.exceptions.UserInputException;

public class CheckUserInputUtils {
        private final static int MIN_WIDTH = 10;
        private final static int MAX_WIDTH = 100;
        private final static int MIN_HEIGHT = 10;
        private final static int MAX_HEIGHT = 100;
        private final static int MIN_FOOD_STATIC = 0;
        private final static int MAX_FOOD_STATIC = 100;
        private final static int MIN_DELAY = 100;
        private final static int MAX_DELAY = 3000;

        public static void checkWidth(String width) throws UserInputException {
            if (width.isEmpty()) {
                throw UserInputException.emptyWidth();
            }
            try {
                int widthVal = Integer.parseInt(width);
                if (widthVal > MAX_WIDTH || widthVal < MIN_WIDTH) {
                    throw UserInputException.invalidWidth(MAX_WIDTH, MIN_WIDTH);
                }
            } catch (NumberFormatException nfe) {
                throw UserInputException.notIntegerWidth();
            }
        }

        public static void checkHeight(String height) throws UserInputException {
            if (height.isEmpty()) {
                throw UserInputException.emptyHeight();
            }
            try {
                int heightVal = Integer.parseInt(height);
                if (heightVal > MAX_HEIGHT || heightVal < MIN_HEIGHT) {
                    throw UserInputException.invalidHeight(MAX_HEIGHT, MIN_HEIGHT);
                }
            } catch (NumberFormatException nfe) {
                throw UserInputException.notIntegerHeight();
            }
        }

        public static void checkFoodStatic(String foodStatic) throws UserInputException {
            if (foodStatic.isEmpty()) {
                throw UserInputException.emptyFoodStatic();
            }
            try {
                int foodStaticVal = Integer.parseInt(foodStatic);
                if (foodStaticVal > MAX_FOOD_STATIC || foodStaticVal < MIN_FOOD_STATIC) {
                    throw UserInputException.invalidFoodStatic(MAX_FOOD_STATIC, MIN_FOOD_STATIC);
                }
            } catch (NumberFormatException nfe) {
                throw UserInputException.notIntegerFoodStatic();
            }
        }

        public static void checkDelay(String delay) throws UserInputException {
            if (delay.isEmpty()) {
                throw UserInputException.emptyDelay();
            }
            try {
                int delayVal = Integer.parseInt(delay);
                if (delayVal > MAX_DELAY || delayVal < MIN_DELAY) {
                    throw UserInputException.invalidDelay(MAX_DELAY, MIN_DELAY);
                }
            } catch (NumberFormatException nfe) {
                throw UserInputException.notIntegerDelay();
            }
        }

        public static void checkNickname(String nickname) throws UserInputException {
            if (nickname.isEmpty()) {
                throw UserInputException.emptyNickname();
            }
        }

        public static void checkGameName(String gameName) throws UserInputException {
            if (gameName.isEmpty()) {
                throw UserInputException.emptyGameName();
            }
        }
}
