package store;

import model.FeedbackEvent;
import utils.FileStore;

// logs user feedback
public class FeedbackLogger {

    private static final String[] HEADERS = {"username", "offer_hash", "system_verdict", "user_verdict", "phrases"};

    private final FileStore fileStore;

    public FeedbackLogger(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public void log(FeedbackEvent event) {
        fileStore.appendCsv(FileStore.FEEDBACK_LOG_PATH, HEADERS, new String[]{
            event.getUsername(),
            event.getOfferDescriptionHash(),
            event.getSystemVerdict(),
            event.getUserVerdict(),
            event.getPhrasesExtracted()
        });
    }
}
