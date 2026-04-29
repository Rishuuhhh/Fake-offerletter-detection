package service;

import java.util.*;

// pulls out important phrases from text
public class PhraseExtractor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a","an","the","is","are","was","were","be","been","being",
        "have","has","had","do","does","did","will","would","could",
        "should","may","might","shall","can","need","dare","ought",
        "to","of","in","for","on","with","at","by","from","as","into",
        "through","during","before","after","above","below","between",
        "and","but","or","nor","so","yet","both","either","neither",
        "not","no","this","that","these","those","i","you","he","she",
        "we","they","it","my","your","his","her","our","their","its",
        "me","him","us","them","who","which","what","when","where","how"
    ));

    // gets top N phrases from text
    public List<String> extractTopPhrases(String text, int n) {
        if (text == null || text.trim().isEmpty()) return Collections.emptyList();

        String normalized = text.toLowerCase(Locale.ROOT)
                                .replaceAll("[^a-z0-9 ]", " ")
                                .replaceAll("\\s+", " ")
                                .trim();
        String[] tokens = normalized.split(" ");

        // count 1-grams, 2-grams, and 3-grams
        Map<String, Integer> freq = new LinkedHashMap<>();
        freq.putAll(buildNgramFrequency(tokens, 1));
        freq.putAll(buildNgramFrequency(tokens, 2));
        freq.putAll(buildNgramFrequency(tokens, 3)); 

        // score by frequency * length
        Map<String, Double> scores = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            String phrase = e.getKey();
            // TWEAK: Multi-word phrases get a slight boost to highlight specific scam terms
            double multiplier = phrase.contains(" ") ? 1.5 : 1.0; 
            double score = e.getValue() * phrase.replace(" ", "").length() * multiplier;
            scores.put(phrase, score);
        }

        // sort by score
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Double> e : sorted) {
            if (result.size() >= n) break;
            result.add(e.getKey());
        }
        return result;
    }

    private Map<String, Integer> buildNgramFrequency(String[] tokens, int gramSize) {
        Map<String, Integer> freq = new LinkedHashMap<>();
        for (int i = 0; i <= tokens.length - gramSize; i++) {
            StringBuilder sb = new StringBuilder();
            boolean hasContent = false;
            for (int j = 0; j < gramSize; j++) {
                String token = tokens[i + j];
                if (token.isEmpty()) continue;
                if (sb.length() > 0) sb.append(" ");
                sb.append(token);
                if (!STOP_WORDS.contains(token) && token.length() > 2) hasContent = true;
            }
            String ngram = sb.toString().trim();
            if (ngram.isEmpty() || !hasContent) continue;
            if (STOP_WORDS.contains(ngram)) continue;
            freq.merge(ngram, 1, Integer::sum);
        }
        return freq;
    }
}
