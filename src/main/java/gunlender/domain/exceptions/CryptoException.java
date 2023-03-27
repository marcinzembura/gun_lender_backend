package gunlender.domain.exceptions;

public class CryptoException extends Exception {
    public CryptoException(String msg, Exception ex) {
        super(msg, ex);
    }

    public CryptoException(String msg) {
        super(msg);
    }
}
