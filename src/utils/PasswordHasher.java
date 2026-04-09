package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHasher {

    private static final int SALT_BYTES = 16;

    /** Generates a 16-byte random salt, hex-encoded (32 chars). */
    public static String generateSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes);
    }

    /**
     * Returns SHA-256(salt + password) as a lowercase hex string (64 chars).
     * Never stores or returns the plaintext password.
     */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Hashes the password with a freshly generated salt.
     * @return String[]{hash, salt}
     */
    public static String[] hashWithNewSalt(String password) {
        String salt = generateSalt();
        return new String[]{hash(password, salt), salt};
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     * Returns true iff hash(password, salt) equals storedHash.
     */
    public static boolean verify(String password, String salt, String storedHash) {
        String computed = hash(password, salt);
        try {
            return MessageDigest.isEqual(
                computed.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
