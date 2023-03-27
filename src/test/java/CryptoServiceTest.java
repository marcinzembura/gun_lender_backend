import gunlender.domain.exceptions.CryptoException;
import gunlender.domain.services.CryptoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    @Test
    void generatingSaltDoesNotThrow() {
        var crypto = new CryptoService();

        assertDoesNotThrow(() -> {
            var salt = crypto.generateSalt();
            assertEquals(16, salt.length);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"password1", "SomePassword", "SpecialPassword123!!@@"})
    void hashingPasswordWhenAlgorithmIsNotSetThrows(String password) {
        var crypto = new CryptoService();

        assertThrows(CryptoException.class, () -> crypto.hashPassword(password));
    }

    @Test
    void hashingPasswordWhenAlgorithmIsSetDoesNotThrow() throws NoSuchAlgorithmException {
        var crypto = new CryptoService("PBKDF2WithHmacSHA1");

        assertDoesNotThrow(() -> {
            var hash = crypto.hashPassword("SomePassword");
            assert (!hash.isEmpty());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"password1", "SomePassword", "SpecialPassword123!!@@"})
    void theSamePasswordsHaveTheSameHash(String password) throws NoSuchAlgorithmException {
        var crypto = new CryptoService("PBKDF2WithHmacSHA1");

        assertDoesNotThrow(() -> {
            var hash1 = crypto.hashPassword(password);
            assertTrue(crypto.comparePasswordAndHash(password, hash1));
        });
    }
}
