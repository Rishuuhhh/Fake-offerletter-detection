package service;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

// checks email domains and other basic rules
public class RuleChecker {

    private static final String TRUSTED_FILE = "data/trusted_domains.csv";
    private static final String BLOCKED_FILE = "data/blocked_domains.csv";

    private static Set<String> trusted = new HashSet<>();
    private static Set<String> blocked = new HashSet<>();

    static {
        reload();
    }

    public static void reload() {
        trusted = loadDomains(TRUSTED_FILE);
        blocked = loadDomains(BLOCKED_FILE);
    }

    private static Set<String> loadDomains(String filePath) {
        Set<String> domains = new HashSet<>();
        File file = new File(filePath);
        if (!file.exists()) return domains;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase(Locale.ROOT);
                if (line.isEmpty() || line.startsWith("#") || line.equals("domain")) continue;
                domains.add(line);
            }
        } catch (IOException e) {
            System.err.println("can't read " + filePath + ": " + e.getMessage());
        }
        return domains;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT).trim();
        if (blocked.contains(domain)) return false;
        if (trusted.contains(domain)) return true;
        return !(domain.equals("gmail.com") || domain.equals("yahoo.com") || domain.equals("outlook.com"));
    }

    public static boolean isBlockedDomain(String email) {
        if (email == null || !email.contains("@")) return true;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT).trim();
        return blocked.contains(domain);
    }

    public static boolean isTrustedDomain(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT).trim();
        return trusted.contains(domain);
    }

    public static boolean isUnrealisticSalary(double salary) {
        return salary > 1_000_000;
    }

    public static boolean hasWeakCompanyName(String name) {
        if (name == null) return true;
        String n = name.trim();
        return n.length() < 3 || n.matches("[0-9 ]+");
    }

    public static boolean isInternshipOffer(String offerType, String position) {
        String t = offerType == null ? "" : offerType.toLowerCase(Locale.ROOT);
        String p = position == null ? "" : position.toLowerCase(Locale.ROOT);
        return t.contains("intern") || p.contains("intern");
    }

    public static boolean mentionsFee(String description) {
        if (description == null) return false;
        String d = NlpSignalAnalyzer.normalize(description);
        return matchesWords(d, new String[][]{
            {"pay", "registration", "fee"},
            {"registration", "fee"},
            {"security", "deposit"},
            {"processing", "fee"},
            {"training", "fee"},
            {"joining", "fee"},
            {"required", "fee"},
            {"required", "fees"},
            {"kit", "charges"},
            {"refundable", "deposit"},
            {"advance", "payment"}
        }) || d.matches(".*\\b(?:pay|required|charge[ds]?|deposit)\\b.*\\bfees?\\b.*")
            || d.matches(".*\\bfees?\\b.*\\b(?:required|pay|deposit|charge[ds]?)\\b.*");
    }

    public static boolean mentionsUrgency(String description) {
        if (description == null) return false;
        String d = NlpSignalAnalyzer.normalize(description);
        return matchesWords(d, new String[][]{
            {"urgent", "hiring"},
            {"limited", "seats"},
            {"act", "now"},
            {"last", "chance"},
            {"deadline", "today"},
            {"immediate", "response"},
            {"hurry", "up"},
            {"do", "not", "delay"},
            {"limited", "time", "offer"},
            {"today", "is", "the", "last", "day"},
            {"apply", "immediately"},
            {"urgent", "needed"}
        }) || d.contains("urgent") || d.contains("hurry") || d.contains("act now");
    }

    public static boolean mentionsPersonalInfo(String description) {
        if (description == null) return false;
        String d = NlpSignalAnalyzer.normalize(description);
        return matchesWords(d, new String[][]{
            {"id", "proof"},
            {"aadhaar", "number"},
            {"aadhaar", "card"},
            {"pan", "card"},
            {"bank", "details"},
            {"bank", "account"},
            {"otp", "verification"},
            {"share", "your", "otp"},
            {"passport", "copy"},
            {"credit", "card", "details"},
            {"debit", "card", "details"}
        }) || d.contains("aadhaar") || d.contains("id proof");
    }

    public static boolean hasSuspiciousWords(String description) {
        if (description == null) return false;
        String d = NlpSignalAnalyzer.normalize(description);
        String[][] phrases = {
            {"urgent", "hiring"},
            {"pay", "registration", "fee"},
            {"guaranteed", "job"},
            {"laptop", "charges"},
            {"refundable", "security"},
            {"whatsapp", "interview"}
        };
        for (String[] words : phrases) {
            if (matchesWords(d, words)) return true;
        }
        return false;
    }

    private static boolean matchesWords(String text, String[][] phrases) {
        for (String[] words : phrases) {
            if (matchesWords(text, words)) return true;
        }
        return false;
    }

    private static boolean matchesWords(String text, String[] words) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) pattern.append(".*");
            pattern.append(Pattern.quote(words[i]));
        }
        return Pattern.compile(pattern.toString()).matcher(text).find();
    }
}
