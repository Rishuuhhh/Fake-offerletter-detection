package model;

/** A vocabulary entry learned from user feedback. Weight is mutable (incremented on repeated feedback). */
public class LearnedVocabEntry {
    private final int    id;
    private final String phrase;
    private final String category;  // "fraud" or "genuine"
    private       int    weight;    // positive = fraud signal, negative = genuine signal
    private final String source;    // "feedback"
    private       String updatedAt; // ISO-8601, updated on each upsert

    public LearnedVocabEntry(String phrase, String category, int weight) {
        this(0, phrase, category, weight, "feedback", "");
    }

    public LearnedVocabEntry(int id, String phrase, String category, int weight,
                             String source, String updatedAt) {
        this.id        = id;
        this.phrase    = phrase;
        this.category  = category;
        this.weight    = weight;
        this.source    = source;
        this.updatedAt = updatedAt;
    }

    public void incrementWeight(int delta) {
        this.weight += delta;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int    getId()        { return id; }
    public String getPhrase()    { return phrase; }
    public String getCategory()  { return category; }
    public int    getWeight()    { return weight; }
    public String getSource()    { return source; }
    public String getUpdatedAt() { return updatedAt; }
}
