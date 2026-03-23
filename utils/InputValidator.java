package utils;

public class InputValidator {

    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isValidSalary(double salary) {
        return salary >= 0;
    }
}