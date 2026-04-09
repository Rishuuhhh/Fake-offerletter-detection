package auth;

import db.UserStore;
import model.User;
import utils.InputValidator;
import utils.PasswordHasher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Stateful service managing registration, login, lockout, and session lifecycle.
 * Lockout state is in-memory and resets on application restart.
 */
public class AuthService {

    private static final int  MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS   = 60_000L;

    public enum RegistrationResult {
        SUCCESS, USERNAME_TAKEN, INVALID_USERNAME, INVALID_PASSWORD, PASSWORD_MISMATCH
    }

    public enum LoginResult {
        SUCCESS, INVALID_CREDENTIALS, LOCKED_OUT
    }

    private Session currentSession;
    private final Map<String, Integer> failCounts   = new HashMap<>();
    private final Map<String, Long>    lockoutTimes = new HashMap<>();
    private final UserStore            userStore;

    public AuthService(UserStore userStore) {
        this.userStore = userStore;
    }

    // ── Registration ───────────────────────────────────────────────

    public RegistrationResult register(String username, String password, String confirm) {
        if (!InputValidator.isValidUsername(username))  return RegistrationResult.INVALID_USERNAME;
        if (!InputValidator.isValidPassword(password))  return RegistrationResult.INVALID_PASSWORD;
        if (!InputValidator.passwordsMatch(password, confirm)) return RegistrationResult.PASSWORD_MISMATCH;
        if (userStore.existsByUsername(username))       return RegistrationResult.USERNAME_TAKEN;

        String[] hashAndSalt = PasswordHasher.hashWithNewSalt(password);
        User user = new User(username, hashAndSalt[0], hashAndSalt[1], LocalDateTime.now().toString());
        userStore.save(user);
        return RegistrationResult.SUCCESS;
    }

    // ── Login ──────────────────────────────────────────────────────

    public LoginResult login(String username, String password) {
        // Check lockout
        if (isLockedOut(username)) return LoginResult.LOCKED_OUT;

        User user = userStore.findByUsername(username);
        if (user == null || !PasswordHasher.verify(password, user.getSalt(), user.getPasswordHash())) {
            int fails = failCounts.getOrDefault(username, 0) + 1;
            failCounts.put(username, fails);
            if (fails >= MAX_ATTEMPTS) lockoutTimes.put(username, System.currentTimeMillis());
            return LoginResult.INVALID_CREDENTIALS;
        }

        // Success
        failCounts.remove(username);
        lockoutTimes.remove(username);
        currentSession = new Session(username);
        return LoginResult.SUCCESS;
    }

    /** Returns remaining lockout seconds, or 0 if not locked out. */
    public int getLockoutSecondsRemaining(String username) {
        Long start = lockoutTimes.get(username);
        if (start == null) return 0;
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed >= LOCKOUT_MS) {
            lockoutTimes.remove(username);
            failCounts.remove(username);
            return 0;
        }
        return (int) ((LOCKOUT_MS - elapsed) / 1000) + 1;
    }

    public void logout() {
        currentSession = null;
    }

    public Session getSession()  { return currentSession; }
    public boolean isLoggedIn()  { return currentSession != null; }

    // ── Helpers ────────────────────────────────────────────────────

    private boolean isLockedOut(String username) {
        return getLockoutSecondsRemaining(username) > 0;
    }
}
