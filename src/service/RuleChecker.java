package service;

import java.io.*;
import java.util.*;

// checks email domains and other basic rules
public class RuleChecker {

    private static final String TRUSTED_FILE = "data/trusted_domains.csv";
    private static final String BLOCKED_FILE = "data/blocked_domains.csv";

    private static final Set<String> TRUSTED = loadDomains(TRUSTED_FILE);
    private static final Set<String> BLOCKED = loadDomains(BLOCKED_FILE);

    private static Set<String> loadDomains(String filePath) {
        Set<String> domains = new HashSet<>();
        File file = new File(filePath);
        if (!file.exists()) return domains;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    domains.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("can't read " + filePath + ": " + e.getMessage());
        }
        return domains;
    }

    // checks if email is from a real company
    public static boolean isValidEmail(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase().trim();
        if (BLOCKED.contains(domain)) return false;
        if (TRUSTED.contains(domain)) return true;

        
       // Also check for common public domains if not in trusted list
        return !(domain.equals("gmail.com") || domain.equals("yahoo.com") || domain.equals("outlook.com"));
    }

    public static boolean isBlockedDomain(String email) {
        if (email == null || !email.contains("@")) return true;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase().trim();
        return BLOCKED.contains(domain);
    }

    public static boolean isTrustedDomain(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase().trim();
        return TRUSTED.contains(domain);
    }

    // checks if salary is too high to be real
    public static boolean isUnrealisticSalary(double salary) {
        return salary > 1_000_000;
    }

    // checks if company name looks fake
    public static boolean hasWeakCompanyName(String name) {
        if (name == null) return true;
        String n = name.trim();
        return n.length() < 3 || n.matches("[0-9 ]+");
    }

    public static boolean isInternshipOffer(String offerType, String position) {
        String t = offerType == null ? "" : offerType.toLowerCase();
        String p = position == null ? "" : position.toLowerCase();
        return t.contains("intern") || p.contains("intern");
    }

    public static boolean hasSuspiciousWords(String description) {
        if (description == null) return false;
        String d = description.toLowerCase();

        // Added modern scam terms like "laptop fees" and "whatsapp"
        return d.contains("urgent hiring") || 
               d.contains("pay registration fee") || 
               d.contains("guaranteed job") ||
               d.contains("laptop charges") ||
               d.contains("refundable security") ||
               d.contains("whatsapp interview");

    }
}
