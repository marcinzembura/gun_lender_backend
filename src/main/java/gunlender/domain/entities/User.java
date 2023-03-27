package gunlender.domain.entities;

import gunlender.domain.services.AuthManager;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Getter
public class User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private AuthManager.Role accountType;

    private static final String SQL_DEFINITION = "(Id VARCHAR(16) UNIQUE, FirstName VARCHAR(64), " +
            "LastName VARCHAR(128), Email VARCHAR(64) UNIQUE, PasswordHash VARCHAR(512), " +
            "PhoneNumber VARCHAR(9) UNIQUE, AccountType VARCHAR(32))";

    public User(String firstName, String lastName, String email, String phoneNumber, String passwordHash,
                AuthManager.Role type) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.accountType = type;
    }

    private User() {}

    public static User fromResultSet(ResultSet rs) throws SQLException {
        var user = new User();

        user.id = UUID.fromString(rs.getString("Id"));
        user.firstName = rs.getString("FirstName");
        user.lastName = rs.getString("LastName");
        user.email = rs.getString("Email");
        user.passwordHash = rs.getString("PasswordHash");
        user.phoneNumber = rs.getString("PhoneNumber");
        user.accountType = AuthManager.roleFromString(rs.getString("AccountType"));

        return user;
    }

    public static String toSqlTableDefinition() {
        return SQL_DEFINITION;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", accountType=" + accountType +
                '}';
    }
}

