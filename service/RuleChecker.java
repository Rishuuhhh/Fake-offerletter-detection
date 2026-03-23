package service;

public class RuleChecker {

    public static boolean isValidEmail(String email) {
        return email.contains("@") && !email.endsWith("@gmail.com");
    }

    public static boolean isUnrealisticSalary(double salary) {
        return salary > 1000000; // unrealistic threshold
    }

    public static boolean hasSuspiciousWords(String desc) {
        String lower = desc.toLowerCase();
        return lower.contains("urgent hiring") ||
               lower.contains("pay registration fee") ||
               lower.contains("guaranteed job");
    }
}
