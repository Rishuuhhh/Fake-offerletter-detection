package model;

// learned phrase with weight
public class LearnedVocabEntry {

    private final String phrase;
    private final String category;
    private int weight;

    public LearnedVocabEntry(String phrase, String category, int weight) {
        this.phrase = phrase;
        this.category = category;
        this.weight = weight;
    }

    public void incrementWeight(int delta) { 
        this.weight += delta; 
    }

    public String getPhrase() { 
        return phrase; 
    }
    
    public String getCategory() { 
        return category; 
    }
    
    public int getWeight() { 
        return weight; 
    }
}
