package service;

import java.util.Locale;

public class RuleChecker {

    private static final String[] FREE_EMAIL_DOMAINS = {
        "@gmail.com", "@yahoo.com", "@hotmail.com", "@outlook.com", "@rediffmail.com"
    };

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && !isFreeEmailDomain(email);
    }

    public static boolean isUnrealisticSalary(double salary) {
        return salary > 1000000;
    }

    public static boolean isFreeEmailDomain(String email) {
        if (email == null) return true;
        String lower = email.toLowerCase(Locale.ROOT).trim();
        for (String domain : FREE_EMAIL_DOMAINS) {
            if (lower.endsWith(domain)) return true;
        }
        return false;
    }

    public static boolean hasWeakCompanyName(String companyName) {
        if (companyName == null) return true;
        String c = companyName.trim();
        return c.length() < 3 || c.matches("[0-9 ]+");
    }

    public static boolean isLikelyInternshipOffer(String offerType, String position) {
        String type = offerType == null ? "" : offerType.toLowerCase(Locale.ROOT);
        String pos  = position  == null ? "" : position.toLowerCase(Locale.ROOT);
        return type.contains("intern") || pos.contains("intern");
    }

    public static boolean hasSuspiciousWords(String desc) {
        if (desc == null) return false;
        String lower = desc.toLowerCase();
        return lower.contains("urgent hiring") ||
               lower.contains("pay registration fee") ||
               lower.contains("guaranteed job");
    }
}
