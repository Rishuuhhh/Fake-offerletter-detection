package store;

import model.LearnedVocabEntry;
import utils.FileStore;

import java.util.ArrayList;
import java.util.List;

// saves learned phrases
public class VocabularyStore {

    private static final String[] HEADERS = {"phrase", "category", "weight"};

    private final FileStore fileStore;

    public VocabularyStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    // adds new phrase or updates existing one
    public void upsert(LearnedVocabEntry entry) {
        List<LearnedVocabEntry> entries = loadAll();
        boolean found = false;
        for (LearnedVocabEntry e : entries) {
            if (e.getPhrase().equals(entry.getPhrase())) {
                e.incrementWeight(entry.getWeight());
                found = true;
                break;
            }
        }
        if (!found) entries.add(entry);
        rewrite(entries);
    }

    public List<LearnedVocabEntry> loadAll() {
        List<LearnedVocabEntry> list = new ArrayList<>();
        for (String[] row : fileStore.readCsv(FileStore.VOCAB_PATH)) {
            if (row.length >= 3) {
                try {
                    list.add(new LearnedVocabEntry(row[0], row[1], Integer.parseInt(row[2])));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }

    private void rewrite(List<LearnedVocabEntry> entries) {
        List<String[]> rows = new ArrayList<>();
        for (LearnedVocabEntry e : entries) {
            rows.add(new String[]{ e.getPhrase(), e.getCategory(), String.valueOf(e.getWeight()) });
        }
        fileStore.rewriteCsv(FileStore.VOCAB_PATH, HEADERS, rows);
    }
}
