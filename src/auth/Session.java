package auth;

/** Immutable value object representing an authenticated user session. */
public final class Session {
    private final String username;
    private final long   loginTimeMs;

    public Session(String username) {
        this.username    = username;
        this.loginTimeMs = System.currentTimeMillis();
    }

    public String getUsername()    { return username; }
    public long   getLoginTimeMs() { return loginTimeMs; }
}
