package utils;

public class InputValidator {

    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isValidSalary(double salary) {
        return salary >= 0;
    }

    /** Username must be 3–30 chars, alphanumeric or underscore only. */
    public static boolean isValidUsername(String s) {
        if (s == null) return false;
        return s.matches("[a-zA-Z0-9_]{3,30}");
    }

    /** Password must be at least 8 characters. */
    public static boolean isValidPassword(String p) {
        return p != null && p.length() >= 8;
    }

    /** Confirm password must match password (non-null). */
    public static boolean passwordsMatch(String p, String c) {
        return p != null && p.equals(c);
    }
}
