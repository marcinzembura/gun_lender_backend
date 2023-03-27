package gunlender.domain.exceptions;

import java.sql.SQLException;

public class RepositoryException extends Exception {
    public RepositoryException(String msg, SQLException ex) {
        super(msg, ex);
    }
}
