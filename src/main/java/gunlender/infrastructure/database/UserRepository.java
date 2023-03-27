package gunlender.infrastructure.database;

import gunlender.application.Repository;
import gunlender.application.dto.UpdateUserDto;
import gunlender.domain.entities.User;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository implements Repository {
    private final String databaseUrl;

    public UserRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public List<User> getUsers() throws RepositoryException {
        var users = new ArrayList<User>();

        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {

                statement.setQueryTimeout(30);

                var rs = statement.executeQuery("select * from users");

                while (rs.next()) {
                    users.add(User.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot get users from database", e);
        }

        return users;
    }

    public Optional<User> getUserById(UUID uuid) throws RepositoryException {
        Optional<User> user = Optional.empty();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from users where Id = ?")) {
                statement.setString(1, uuid.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                if (rs.next()) {
                    user = Optional.of(User.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get user with id '%s' from database", uuid.toString());
            throw new RepositoryException(msg, e);
        }

        return user;
    }

    public Optional<User> getUserByEmail(String email) throws RepositoryException {
        Optional<User> user = Optional.empty();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from users where Email = ?")) {
                statement.setString(1, email);
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                if (rs.next()) {
                    user = Optional.of(User.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get user with email address '%s' from database", email);
            throw new RepositoryException(msg, e);
        }

        return user;
    }

    public void addUser(User user) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("insert into users values (?, ? ,? ,?, ? ,?, ?)")) {
                statement.setQueryTimeout(30);

                statement.setString(1, user.getId().toString());
                statement.setString(2, user.getFirstName());
                statement.setString(3, user.getLastName());
                statement.setString(4, user.getEmail());
                statement.setString(5, user.getPasswordHash());
                statement.setString(6, user.getPhoneNumber());
                statement.setString(7, user.getAccountType().name());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert user to database", e);
        }
    }

    public void deleteUser(UUID id) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("delete from users where id = ?")) {
                statement.setQueryTimeout(30);
                statement.setString(1, id.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot delete user form database", e);
        }
    }

    public void updateUser(UUID id, UpdateUserDto user) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("update users set FirstName = ?, LastName = ?, " +
                    "Email = ?, PhoneNumber = ? where Id = ?")) {
                statement.setQueryTimeout(30);

                statement.setString(1, user.getFirstName());
                statement.setString(2, user.getLastName());
                statement.setString(3, user.getEmail());
                statement.setString(4, user.getPhoneNumber());
                statement.setString(5, id.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert user to database", e);
        }
    }

    public void updateUserPassword(UUID id, String passwordHash) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("update users set PasswordHash = ? where Id = ?")) {
                statement.setQueryTimeout(30);

                statement.setString(1, passwordHash);
                statement.setString(2, id.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert user to database", e);
        }
    }

    public void updateUserRole(UUID id, AuthManager.Role role) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("update users set AccountType = ? where Id = ?")) {
                statement.setQueryTimeout(30);

                statement.setString(1, AuthManager.roleToString(role));
                statement.setString(2, id.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert user to database", e);
        }
    }

    public void migrate() throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                statement.executeUpdate(String.format("create table if not exists users %s", User.toSqlTableDefinition()));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot migrate 'users' table", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }
}
