package gunlender.domain.services;

import gunlender.domain.exceptions.CryptoException;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class CryptoService {
    private final SecureRandom rng;
    private final int keyLength;
    @Setter
    @Getter
    private SecretKeyFactory skf;

    public CryptoService() {
        this.rng = new SecureRandom();
        this.keyLength = 64 * 8;
    }

    public CryptoService(String algorithmName) throws NoSuchAlgorithmException {
        this.rng = new SecureRandom();
        this.keyLength = 64 * 8;
        this.skf = SecretKeyFactory.getInstance(algorithmName);
    }

    public byte[] generateSalt() {
        var salt = new byte[16];
        rng.nextBytes(salt);
        return salt;
    }

    public String hashPassword(String password) throws CryptoException {
        var salt = generateSalt();
        return hashPassword(password, salt, 100);
    }


    public String hashPassword(String password, byte[] salt) throws CryptoException {
        return hashPassword(password, salt, 100);
    }

    public String hashPassword(String password, byte[] salt, int iterations) throws CryptoException {
        if (skf == null) {
            throw new CryptoException("SecretKeyFactory is not set!");
        }

        try {
            var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            var hash = skf.generateSecret(spec).getEncoded();
            return iterations + ":" + toBase64(salt) + ":" + toBase64(hash);
        } catch (InvalidKeySpecException ex) {
            throw new CryptoException("Cannot hash password because key is invalid", ex);
        } catch (Exception ex) {
            throw new CryptoException("Cannot hash password", ex);
        }
    }


    public  boolean comparePasswordAndHash(String password, String hash) throws CryptoException {
        var hashInfo = getHashInfo(hash);
        var newHash = hashPassword(password, hashInfo.salt, hashInfo.iterations);
        return hash.equals(newHash);
    }

    public static HashInfo getHashInfo(String hash) {
        var hashParts = hash.split(":");
        var iterations = Integer.parseInt(hashParts[0]);
        var salt = fromBase64(hashParts[1]);
        return new HashInfo(iterations, salt);
    }

    private static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] fromBase64(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    public record HashInfo(int iterations, byte[] salt) {
    }
}
