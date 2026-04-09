package store;

import model.AnalysisRecord;
import utils.FileStore;

import java.util.ArrayList;
import java.util.List;

// saves and loads analysis history
public class HistoryStore {

    private static final String[] HEADERS = {"username", "company_name", "offer_type", "risk_score", "nlp_score", "verdict"};

    private final FileStore fileStore;

    public HistoryStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public void save(AnalysisRecord record) {
        fileStore.appendCsv(FileStore.HISTORY_PATH, HEADERS, new String[]{
            record.getUsername(),
            record.getCompanyName(),
            record.getOfferType(),
            String.valueOf(record.getRiskScore()),
            String.valueOf(record.getNlpScore()),
            record.getVerdict()
        });
    }

    public List<AnalysisRecord> findByUsername(String username) {
        List<AnalysisRecord> list = new ArrayList<>();
        for (String[] row : fileStore.readCsv(FileStore.HISTORY_PATH)) {
            if (row.length >= 6 && row[0].equals(username)) {
                try {
                    list.add(new AnalysisRecord(
                        row[0], row[1], row[2],
                        Integer.parseInt(row[3]),
                        Integer.parseInt(row[4]),
                        row[5]));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }
}
