package gunlender.infrastructure.database;

import gunlender.application.Repository;
import gunlender.application.dto.GunDto;
import gunlender.domain.entities.Gun;
import gunlender.domain.exceptions.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GunRepository implements Repository {
    private final String databaseUrl;

    public GunRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public List<Gun> getGuns() throws RepositoryException {
        var guns = new ArrayList<Gun>();

        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery("select * from guns");

                while (rs.next()) {
                    guns.add(Gun.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot get all guns from database", e);
        }

        return guns;
    }

    public Optional<Gun> getGunById(UUID uuid) throws RepositoryException {
        Optional<Gun> gun = Optional.empty();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from guns where Id = ?")) {
                statement.setString(1, uuid.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                if (rs.next()) {
                    gun = Optional.of(Gun.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get gun with Id '%s' from database", uuid.toString());
            throw new RepositoryException(msg, e);
        }

        return gun;
    }


    public void addGun(Gun gun) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("insert into guns values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                statement.setQueryTimeout(30);

                statement.setString(1, gun.getId().toString());
                statement.setString(2, gun.getProducer());
                statement.setString(3, gun.getModel());
                statement.setString(4, gun.getType().name());
                statement.setString(5, gun.getCaliber());
                statement.setDouble(6, gun.getWeight());
                statement.setInt(7, gun.getLength());
                statement.setInt(8, gun.getAmount());
                statement.setDouble(9, gun.getPrice());
                statement.setString(10, gun.getPicture());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert gun to database", e);
        }
    }

    public void updateGun(UUID id, GunDto gun) throws RepositoryException {
        try (var connection = getConnection()) {
            updateGun(id, gun, connection);
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert gun to database", e);
        }
    }

    public void updateGun(UUID id, GunDto gun, Connection connection) throws RepositoryException {
        try (var statement = connection.prepareStatement("update guns set Producer = ?, Model = ?," +
                "Type = ?, Caliber = ?, Weight = ?, Length = ?, Amount = ?, Price = ?, Picture = ? where Id = ?")) {
            statement.setQueryTimeout(30);

            statement.setString(1, gun.getProducer());
            statement.setString(2, gun.getModel());
            statement.setString(3, gun.getType().name());
            statement.setString(4, gun.getCaliber());
            statement.setDouble(5, gun.getWeight());
            statement.setInt(6, gun.getLength());
            statement.setInt(7, gun.getAmount());
            statement.setDouble(8, gun.getPrice());
            statement.setString(9, gun.getPicture());
            statement.setString(10, id.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert gun to database", e);
        }
    }

    public void deleteGun(UUID id) throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("delete from guns where Id = ?")) {
                statement.setQueryTimeout(30);

                statement.setString(1, id.toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert gun to database", e);
        }
    }

    public void migrate() throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                statement.executeUpdate(String.format("create table if not exists guns %s", Gun.toSqlTableDefinition()));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot migrate 'guns' table", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }
}
