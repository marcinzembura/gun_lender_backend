package gunlender.infrastructure.database;

import gunlender.application.Repository;
import gunlender.application.dto.UpdateLendingDto;
import gunlender.domain.entities.Lending;
import gunlender.domain.exceptions.RepositoryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LendingRepository implements Repository {
    private final String databaseUrl;

    public LendingRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public List<Lending> getLendings() throws RepositoryException {
        var lendings = new ArrayList<Lending>();

        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery("select * from lendings");

                while (rs.next()) {
                    lendings.add(Lending.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot get all lending data from database", e);
        }

        return lendings;
    }

    public List<Lending> getLendingByUserId(UUID userId) throws RepositoryException {
        var lendings = new ArrayList<Lending>();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from lendings where UserId = ?")) {
                statement.setString(1, userId.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                while (rs.next()) {
                    lendings.add(Lending.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get lendings with UserId '%s' from database", userId.toString());
            throw new RepositoryException(msg, e);
        }

        return lendings;
    }

    public List<Lending> getLendingByGunId(UUID gunId) throws RepositoryException {
        var lendings = new ArrayList<Lending>();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from lendings where GunId = ?")) {
                statement.setString(1, gunId.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                while (rs.next()) {
                    lendings.add(Lending.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get lendings with GunId '%s' from database", gunId.toString());
            throw new RepositoryException(msg, e);
        }

        return lendings;
    }

    public List<Lending> getLendingByAmmoId(UUID ammoId) throws RepositoryException {
        var lendings = new ArrayList<Lending>();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from lendings where AmmoId = ?")) {
                statement.setString(1, ammoId.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                while (rs.next()) {
                    lendings.add(Lending.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get lendings with AmmoId '%s' from database", ammoId.toString());
            throw new RepositoryException(msg, e);
        }

        return lendings;
    }

    public Optional<Lending> getLending(UUID userId, UUID gunId, UUID ammoId) throws RepositoryException {
        Optional<Lending> lending = Optional.empty();

        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement("select * from lendings where UserId = ? AND AmmoId = ?" +
                    "AND GunId = ?")) {
                statement.setString(1, userId.toString());
                statement.setString(2, ammoId.toString());
                statement.setString(3, gunId.toString());
                statement.setQueryTimeout(30);

                var rs = statement.executeQuery();

                if (rs.next()) {
                    lending = Optional.of(Lending.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            var msg = String.format("Cannot get lendings with AmmoId '%s' from database", ammoId.toString());
            throw new RepositoryException(msg, e);
        }

        return lending;
    }

    public void addLending(Lending lending) throws RepositoryException {
        try (var connection = getConnection()) {
            addLending(lending, connection);
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert lending to database", e);
        }
    }

    public void addLending(Lending lending, Connection connection) throws RepositoryException {
        try (var statement = connection.prepareStatement("insert into lendings values (?, ?, ?, ?, ?, ?)")) {
            statement.setQueryTimeout(30);

            statement.setString(1, lending.getUserId().toString());
            statement.setString(2, lending.getGunId().toString());
            statement.setString(3, lending.getAmmoId().toString());
            statement.setInt(4, lending.getAmmoAmount());
            statement.setString(5, lending.getReservationDate().toString());
            statement.setDouble(6, lending.getTotalPrice());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert lending to database", e);
        }
    }

    public void deleteLending(UUID userId, UUID gunId, UUID ammoId) throws RepositoryException {
        try (var connection = getConnection()) {
            deleteLending(userId, gunId, ammoId, connection);
        } catch (SQLException e) {
            throw new RepositoryException("Cannot delete lending data from database", e);
        }
    }

    public void deleteLending(UUID userId, UUID gunId, UUID ammoId, Connection connection) throws RepositoryException {
            try (var statement = connection.prepareStatement("delete from lendings where UserId = ? AND GunId = ? " +
                    "AND AmmoId = ?")) {
                statement.setQueryTimeout(30);
                statement.setString(1, userId.toString());
                statement.setString(2, gunId.toString());
                statement.setString(3, ammoId.toString());

                statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot delete lending data from database", e);
        }
    }


    public void updateLending(UpdateLendingDto updateLendingDto, double totalPrice) throws RepositoryException {
        try (var connection = getConnection()) {
            updateLending(updateLendingDto, totalPrice, connection);
        } catch (SQLException e) {
            throw new RepositoryException("Cannot update lending data in database", e);
        }
    }

    public void updateLending(UpdateLendingDto updateLendingDto, double totalPrice, Connection connection) throws RepositoryException {
            try (var statement = connection.prepareStatement("update lendings set GunId = ?, AmmoId = ?, AmmoAmount = ?," +
                    "ReservationDate = ?, TotalPrice = ? where UserId = ? AND GunId = ? AND AmmoId = ?")) {
                statement.setQueryTimeout(30);
                statement.setString(1, updateLendingDto.getNewGunId().toString());
                statement.setString(2, updateLendingDto.getNewAmmoId().toString());
                statement.setInt(3, updateLendingDto.getAmmoAmount());
                statement.setString(4, updateLendingDto.getReservationDate().toString());
                statement.setDouble(5, totalPrice);
                statement.setString(6, updateLendingDto.getUserId().toString());
                statement.setString(7, updateLendingDto.getOldGunId().toString());
                statement.setString(8, updateLendingDto.getOldAmmoId().toString());

                statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot update lending data in database", e);
        }
    }

    public void migrate() throws RepositoryException {
        try (var connection = getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                statement.executeUpdate(String.format("create table if not exists lendings %s", Lending.toSqlTableDefinition()));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot migrate 'lendings' table", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }
}
