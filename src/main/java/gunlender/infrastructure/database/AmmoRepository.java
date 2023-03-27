package gunlender.infrastructure.database;

import gunlender.application.Repository;
import gunlender.domain.entities.Ammo;
import gunlender.domain.exceptions.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AmmoRepository implements Repository {
    private final String databaseUrl;

    public AmmoRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public List<Ammo> getAmmo() throws RepositoryException {
        var ammo = new ArrayList<Ammo>();

        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {

                statement.setQueryTimeout(30);

                var rs = statement.executeQuery("select * from ammo");

                while (rs.next()) {
                    ammo.add(Ammo.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot get all ammo data from database", e);
        }

        return ammo;
    }


    public int removeAmmo(UUID uuid) throws RepositoryException {
        try (final Connection connection = getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from ammo where Id = ?")) {
                statement.setString(1, uuid.toString());
                statement.setQueryTimeout(30);
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException(String.format("Cannot delete ammo with Id '%s' from database", uuid.toString()), e);
        }
    }

    public Optional<Ammo> getAmmoById(UUID uuid) throws RepositoryException {
        Optional<Ammo> ammo = Optional.empty();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from ammo where Id = ?")) {
                statement.setString(1, uuid.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                if (rs.next()) {
                    ammo = Optional.of(Ammo.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get ammo with Id '%s' from database", uuid.toString());
            throw new RepositoryException(msg, e);
        }

        return ammo;
    }

    public void updateAmmo(Ammo ammo) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("update ammo set caliber = ?, amount = ?, price = ?, picture = ? where id = ?")) {
                statement.setQueryTimeout(30);

                statement.setString(1, ammo.getCaliber());
                statement.setInt(2, ammo.getAmount());
                statement.setDouble(3, ammo.getPrice());
                statement.setString(4, ammo.getPicture());
                statement.setString(5, ammo.getId().toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot update ammo in database", e);
        }
    }

    public void addAmmo(Ammo ammo) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("insert into ammo values (?, ?, ?, ?, ?)")) {
                statement.setQueryTimeout(30);

                statement.setString(1, ammo.getId().toString());
                statement.setString(2, ammo.getCaliber());
                statement.setInt(3, ammo.getAmount());
                statement.setDouble(4, ammo.getPrice());
                statement.setString(5, ammo.getPicture());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert ammo to database", e);
        }
    }

    public void migrate() throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                statement.executeUpdate(String.format("create table if not exists ammo %s", Ammo.toSqlTableDefinition()));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot migrate 'ammo' table", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }
}
