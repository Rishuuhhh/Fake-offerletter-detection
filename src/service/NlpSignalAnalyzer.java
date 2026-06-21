package service;

import java.io.*;
import java.util.*;
import java.util.regex.*;

// loads keywords from files and checks text for scam signals
public class NlpSignalAnalyzer {

    public static final String FAKE_FILE = "data/fake_keywords.csv";
    public static final String GENUINE_FILE = "data/genuine_keywords.csv";

    private String[] highRisk = new String[0];
    private String[] mediumRisk = new String[0];
    private String[] rushWords = new String[0];
    private String[] paymentWords = new String[0];
    private String[] genuineWords = new String[0];

    private static final Pattern DOMAIN_SPOOF = Pattern.compile(
        "\\b(?:amazon|google|microsoft|infosys|tcs|wipro|flipkart|hdfc|icici)" +
        "[\\-\\.][a-z0-9\\-\\.]+\\.(com|net|org|in)\\b"
    );

    private static final Pattern SALARY_PROMISE = Pattern.compile(
        "earn\\s+rs|get paid rs|guaranteed salary|salary of \\d+|stipend of \\d+" +
        "|rs\\.?\\s*\\d+\\s*(?:per month|monthly)|lakh guaranteed|income of \\d+"
    );

    public NlpSignalAnalyzer() {
        reload();
    }

    public void reload() {
        List<String> high = new ArrayList<>();
        List<String> medium = new ArrayList<>();
        List<String> rush = new ArrayList<>();
        List<String> pay = new ArrayList<>();

        loadFakeKeywords(high, medium, rush, pay);
        highRisk = high.toArray(new String[0]);
        mediumRisk = medium.toArray(new String[0]);
        rushWords = rush.toArray(new String[0]);
        paymentWords = pay.toArray(new String[0]);
        genuineWords = loadGenuineKeywords().toArray(new String[0]);
    }

    private void loadFakeKeywords(List<String> high, List<String> medium,
                                  List<String> rush, List<String> pay) {
        try (BufferedReader br = new BufferedReader(new FileReader(FAKE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (isHeaderLine(line, "type")) continue;

                int comma = line.indexOf(',');
                if (comma < 0) continue;

                String type = line.substring(0, comma).trim().toLowerCase(Locale.ROOT);
                String phrase = normalize(line.substring(comma + 1));
                if (phrase.isEmpty()) continue;

                switch (type) {
                    case "high": high.add(phrase); break;
                    case "medium": medium.add(phrase); break;
                    case "rush": rush.add(phrase); break;
                    case "payment": pay.add(phrase); break;
                    default: break;
                }
            }
        } catch (IOException e) {
            System.err.println("can't read " + FAKE_FILE + ": " + e.getMessage());
        }
    }

    private List<String> loadGenuineKeywords() {
        List<String> gen = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(GENUINE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (isHeaderLine(line, "phrase")) continue;
                String phrase = normalize(line);
                if (!phrase.isEmpty()) gen.add(phrase);
            }
        } catch (IOException e) {
            System.err.println("can't read " + GENUINE_FILE + ": " + e.getMessage());
        }
        return gen;
    }

    private static boolean isHeaderLine(String line, String headerName) {
        return line.equalsIgnoreCase(headerName) || line.toLowerCase(Locale.ROOT).startsWith(headerName + ",");
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

    public NlpResult analyze(String text) {
        String raw = text == null ? "" : text.toLowerCase(Locale.ROOT);
        String t = normalize(text);
        List<String> notes = new ArrayList<>();
        int score = 0;

        if (t.isEmpty()) {
            notes.add("No description provided.");
            return new NlpResult(5, notes);
        }

        int highHits = 0;
        for (String phrase : highRisk) {
            if (phrase.isEmpty() || !matches(t, phrase)) continue;
            highHits++;
            score += 15;
            if (countOccurrences(t, phrase) >= 2) score += 7;
        }
        if (highHits > 0) notes.add("High-risk phrases found: " + highHits);

        int medHits = countMatches(t, mediumRisk);
        if (medHits > 0) {
            score += medHits * 7;
            notes.add("Medium-risk phrases found: " + medHits);
        }

        int genHits = countMatches(t, genuineWords);
        if (genHits > 0) score -= genHits * 6;
        if (genHits >= 2) notes.add("Legitimate recruitment signals found.");

        int rushHits = countMatches(t, rushWords);
        score += Math.min(rushHits, 5) * 5;
        if (rushHits >= 2) notes.add("Pressure/urgency language detected.");

        int payHits = countMatches(t, paymentWords);
        score += Math.min(payHits, 5) * 4;
        if (payHits >= 2) notes.add("Financial request language detected.");

        if (DOMAIN_SPOOF.matcher(raw).find()) {
            score += 20;
            notes.add("Spoofed company domain detected.");
        }

        if (SALARY_PROMISE.matcher(t).find()) {
            score += 10;
            notes.add("Unrealistic salary promise detected.");
        }

        if (notes.isEmpty()) notes.add("No significant risk signals found.");

        return new NlpResult(score, notes);
    }

    public void appendFakeKeyword(String phrase) {
        appendIfNew(FAKE_FILE, "medium," + normalize(phrase));
        reload();
    }

    public void appendGenuineKeyword(String phrase) {
        appendIfNew(GENUINE_FILE, normalize(phrase));
        reload();
    }

    private void appendIfNew(String file, String line) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String existing;
            while ((existing = br.readLine()) != null) {
                if (normalize(existing).contains(normalize(line))) return;
            }
        } catch (IOException e) {
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("can't append to " + file + ": " + e.getMessage());
        }
    }

    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int countMatches(String text, String[] phrases) {
        int hits = 0;
        for (String phrase : phrases) {
            if (!phrase.isEmpty() && matches(text, phrase)) hits++;
        }
        return hits;
    }

    // matches exact phrase or same words with gaps, e.g. "pay registration fee" in "pay a registration fee"
    private static boolean matches(String text, String phrase) {
        if (text.contains(phrase)) return true;
        String[] words = phrase.split(" ");
        if (words.length <= 1) return false;
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) pattern.append(".*");
            pattern.append(Pattern.quote(words[i]));
        }
        return Pattern.compile(pattern.toString()).matcher(text).find();
    }

    private static int countOccurrences(String text, String phrase) {
        int n = 0, i = 0;
        while ((i = text.indexOf(phrase, i)) != -1) {
            n++;
            i += phrase.length();
        }
        return n;
    }
}
