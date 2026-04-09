package utils;

// checks user input during registration
public class InputValidator {

    // username: 3-30 chars, letters/numbers/underscore
    public static boolean isValidUsername(String s) {
        return s != null && s.matches("[a-zA-Z0-9_]{3,30}");
    }

    // password: at least 8 chars
    public static boolean isValidPassword(String p) {
        return p != null && p.length() >= 8;
    }

    // passwords must match
    public static boolean passwordsMatch(String password, String confirm) {
        return password != null && password.equals(confirm);
    }
}
