package model;

/** Immutable user credential record. Passwords are never stored in plaintext. */
public class User {
    private final String username;
    private final String passwordHash; // SHA-256(salt + password), hex-encoded
    private final String salt;         // 16-byte random hex salt
    private final String createdAt;    // ISO-8601 timestamp

    public User(String username, String passwordHash, String salt, String createdAt) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.salt         = salt;
        this.createdAt    = createdAt;
    }

    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt()         { return salt; }
    public String getCreatedAt()    { return createdAt; }
}
