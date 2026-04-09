package store;

import model.User;
import utils.FileStore;

// saves and loads user accounts
public class UserStore {

    private static final String[] HEADERS = {"username", "password_hash", "salt"};

    private final FileStore fileStore;

    public UserStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public void save(User user) {
        fileStore.appendCsv(FileStore.CREDENTIALS_PATH, HEADERS,
            new String[]{ user.getUsername(), user.getPasswordHash(), user.getSalt() });
    }

    public User findByUsername(String username) {
        for (String[] row : fileStore.readCsv(FileStore.CREDENTIALS_PATH)) {
            if (row.length >= 3 && row[0].equals(username)) {
                return new User(row[0], row[1], row[2]);
            }
        }
        return null;
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }
}
