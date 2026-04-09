package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// hashes passwords with salt
public class PasswordHasher {

    private static final int SALT_BYTES = 16;

    // generates random salt
    public static String generateSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(bytes);
        return toHex(bytes);
    }

    // returns SHA-256(salt + password)
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // hashes password with new salt
    public static String[] hashWithNewSalt(String password) {
        String salt = generateSalt();
        return new String[]{ hash(password, salt), salt };
    }

    // checks if password matches stored hash
    public static boolean verify(String password, String salt, String storedHash) {
        String computed = hash(password, salt);
        return MessageDigest.isEqual(
            computed.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
