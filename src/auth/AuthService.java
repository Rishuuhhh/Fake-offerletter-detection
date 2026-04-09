package auth;

import model.User;
import store.UserStore;
import utils.InputValidator;
import utils.PasswordHasher;
import java.util.HashMap;
import java.util.Map;

// handles login, registration, and lockout
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 60_000L;

    public enum RegistrationResult {
        SUCCESS, USERNAME_TAKEN, INVALID_USERNAME, INVALID_PASSWORD, PASSWORD_MISMATCH
    }

    public enum LoginResult {
        SUCCESS, INVALID_CREDENTIALS, LOCKED_OUT
    }

    private final UserStore userStore;
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, Long> lockoutStart = new HashMap<>();
    private Session currentSession;

    public AuthService(UserStore userStore) {
        this.userStore = userStore;
    }

    public RegistrationResult register(String username, String password, String confirm) {
        if (!InputValidator.isValidUsername(username)) return RegistrationResult.INVALID_USERNAME;
        if (!InputValidator.isValidPassword(password)) return RegistrationResult.INVALID_PASSWORD;
        if (!InputValidator.passwordsMatch(password, confirm)) return RegistrationResult.PASSWORD_MISMATCH;
        if (userStore.existsByUsername(username)) return RegistrationResult.USERNAME_TAKEN;

        String[] hashAndSalt = PasswordHasher.hashWithNewSalt(password);
        userStore.save(new User(username, hashAndSalt[0], hashAndSalt[1]));
        return RegistrationResult.SUCCESS;
    }

    public LoginResult login(String username, String password) {
        if (isLockedOut(username)) return LoginResult.LOCKED_OUT;

        User user = userStore.findByUsername(username);
        boolean valid = user != null && PasswordHasher.verify(password, user.getSalt(), user.getPasswordHash());

        if (!valid) {
            int attempts = failedAttempts.getOrDefault(username, 0) + 1;
            failedAttempts.put(username, attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockoutStart.put(username, System.currentTimeMillis());
            }
            return LoginResult.INVALID_CREDENTIALS;
        }

        // clear failure state
        failedAttempts.remove(username);
        lockoutStart.remove(username);
        currentSession = new Session(username);
        return LoginResult.SUCCESS;
    }

    public int getLockoutSecondsRemaining(String username) {
        Long start = lockoutStart.get(username);
        if (start == null) return 0;
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed >= LOCKOUT_DURATION_MS) {
            lockoutStart.remove(username);
            failedAttempts.remove(username);
            return 0;
        }
        return (int) ((LOCKOUT_DURATION_MS - elapsed) / 1000) + 1;
    }

    public void logout() { 
        currentSession = null; 
    }

    public Session getSession() { 
        return currentSession; 
    }
    
    public boolean isLoggedIn() { 
        return currentSession != null; 
    }

    private boolean isLockedOut(String username) {
        return getLockoutSecondsRemaining(username) > 0;
    }
}
