package service;

import java.io.*;
import java.util.*;
import java.util.regex.*;

// loads keywords from files and checks text for scam signals
public class NlpSignalAnalyzer {

    public static final String FAKE_FILE = "data/fake_keywords.csv";
    public static final String GENUINE_FILE = "data/genuine_keywords.csv";

    private String[] highRisk;
    private String[] mediumRisk;
    private String[] rushWords;
    private String[] paymentWords;
    private String[] genuineWords;

    // catches fake domains like amazon-hr.com
    private static final Pattern DOMAIN_SPOOF = Pattern.compile(
        "\\b(?:amazon|google|microsoft|infosys|tcs|wipro|flipkart|hdfc|icici)" +
        "[\\-\\.][a-z0-9\\-\\.]+\\.(com|net|org|in)\\b"
    );

    // catches salary scams
    private static final Pattern SALARY_PROMISE = Pattern.compile(
        "earn\\s+rs|guaranteed salary|salary of \\d+|stipend of \\d+|rs\\.?\\s*\\d+\\s*per month"
    );

    public NlpSignalAnalyzer() {
        reload();
    }

    // reads keywords from both files
    public void reload() {
        List<String> high = new ArrayList<>();
        List<String> medium = new ArrayList<>();
        List<String> rush = new ArrayList<>();
        List<String> pay = new ArrayList<>();

        // load fake keywords
        try (BufferedReader br = new BufferedReader(new FileReader(FAKE_FILE))) {
            String line;
            br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int comma = line.indexOf(',');
                if (comma < 0) continue;
                String type = line.substring(0, comma).trim();
                String phrase = line.substring(comma + 1).trim();
                if (type.equals("high")) high.add(phrase);
                else if (type.equals("medium")) medium.add(phrase);
                else if (type.equals("rush")) rush.add(phrase);
                else if (type.equals("payment")) pay.add(phrase);
            }
        } catch (IOException e) {
            System.err.println("can't read " + FAKE_FILE + ": " + e.getMessage());
        }

        highRisk = high.toArray(new String[0]);
        mediumRisk = medium.toArray(new String[0]);
        rushWords = rush.toArray(new String[0]);
        paymentWords = pay.toArray(new String[0]);

        List<String> gen = new ArrayList<>();

        // load genuine keywords
        try (BufferedReader br = new BufferedReader(new FileReader(GENUINE_FILE))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) gen.add(line);
            }
        } catch (IOException e) {
            System.err.println("can't read " + GENUINE_FILE + ": " + e.getMessage());
        }

        genuineWords = gen.toArray(new String[0]);
    }

    public static class NlpResult {
        public final int risk;
        public final List<String> notes;

        NlpResult(int risk, List<String> notes) {
            this.risk = Math.max(0, Math.min(risk, 100));
            this.notes = notes;
        }

        public int getRisk() { return risk; }
        public List<String> getNotes() { return notes; }
    }

    // checks text for scam signals
    public NlpResult analyze(String text) {
        String t = normalize(text);
        List<String> notes = new ArrayList<>();
        int score = 0;

        if (t.isEmpty()) {
            notes.add("No description provided.");
            return new NlpResult(5, notes);
        }

        // check high-risk phrases
        int highHits = 0;
        for (String p : highRisk) {
            int n = count(t, p);
            if (n >= 1) { 
                score += 15; 
                highHits++; 
            }
            if (n >= 2) score += 7;
        }
        if (highHits > 0) notes.add("High-risk phrases found: " + highHits);

        // check medium-risk
        int medHits = 0;
        for (String p : mediumRisk) {
            if (t.contains(p)) { 
                score += 7; 
                medHits++; 
            }
        }
        if (medHits > 0) notes.add("Medium-risk phrases found: " + medHits);

        // check genuine phrases
        int genHits = 0;
        for (String p : genuineWords) {
            if (t.contains(p)) { 
                score -= 6; 
                genHits++; 
            }
        }
        if (genHits >= 2) notes.add("Legitimate recruitment signals found.");

        // urgency words
        int rushHits = 0;
        for (String p : rushWords) { 
            if (t.contains(p)) rushHits++; 
        }
        score += Math.min(rushHits, 5) * 5;
        if (rushHits >= 2) notes.add("Pressure/urgency language detected.");

        // payment words
        int payHits = 0;
        for (String p : paymentWords) { 
            if (t.contains(p)) payHits++; 
        }
        score += Math.min(payHits, 5) * 4;
        if (payHits >= 2) notes.add("Financial request language detected.");

        // domain spoofing check
        if (DOMAIN_SPOOF.matcher(t).find()) {
            score += 20;
            notes.add("Spoofed company domain detected.");
        }

        // salary scam check
        if (SALARY_PROMISE.matcher(t).find()) {
            score += 10;
            notes.add("Unrealistic salary promise detected.");
        }

        if (notes.isEmpty()) notes.add("No significant risk signals found.");

        return new NlpResult(Math.max(0, Math.min(score, 100)), notes);
    }

    // adds phrase to fake keywords
    public void appendFakeKeyword(String phrase) {
        appendIfNew(FAKE_FILE, "medium," + phrase.toLowerCase().trim());
        reload();
    }

    // adds phrase to genuine keywords
    public void appendGenuineKeyword(String phrase) {
        appendIfNew(GENUINE_FILE, phrase.toLowerCase().trim());
        reload();
    }

    private void appendIfNew(String file, String line) {
        // check if already exists
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String existing;
            while ((existing = br.readLine()) != null) {
                if (existing.trim().equalsIgnoreCase(line)) return;
            }
        } catch (IOException e) { return; }

        // add it
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("can't append to " + file + ": " + e.getMessage());
        }
    }

    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static int count(String text, String phrase) {
        int n = 0;
        int i = 0;
        while ((i = text.indexOf(phrase, i)) != -1) { 
            n++; 
            i += phrase.length(); 
        }
        return n;
    }
}
