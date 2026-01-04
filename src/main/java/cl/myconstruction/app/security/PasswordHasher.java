package cl.myconstruction.app.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;

    public record Hash(byte[] hash, byte[] salt) {}

    public static Hash hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password, salt);
        return new Hash(hash, salt);
    }

    public static boolean verify(String password, byte[] expectedHash, byte[] salt) {
        byte[] actualHash = pbkdf2(password, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar hash de contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    private PasswordHasher() {}
}
