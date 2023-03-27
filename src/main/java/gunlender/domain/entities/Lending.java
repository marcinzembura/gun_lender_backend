package gunlender.domain.entities;

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Lending {
    private UUID userId;
    private UUID gunId;
    private UUID ammoId;
    private int ammoAmount;
    private Instant reservationDate;
    private double totalPrice;

    private static final String SQL_DEFINITION = "(UserId VARCHAR(16), GunId VARCHAR(16), AmmoId VARCHAR(16), " +
            "AmmoAmount INT, ReservationDate DATETIME, TotalPrice DECIMAL(10,2))";

    public Lending(UUID userId, UUID gunId, UUID ammoId, int ammoAmount, Instant reservationDate, double totalPrice) {
        this.userId = userId;
        this.gunId = gunId;
        this.ammoId = ammoId;
        this.ammoAmount = ammoAmount;
        this.reservationDate = reservationDate;
        this.totalPrice = totalPrice;
    }

    private Lending() {}

    public static Lending fromResultSet(ResultSet rs) throws SQLException {
        var lending = new Lending();

        lending.userId = UUID.fromString(rs.getString("UserId"));
        lending.gunId = UUID.fromString(rs.getString("GunId"));
        lending.ammoId = UUID.fromString(rs.getString("AmmoId"));
        lending.ammoAmount = rs.getInt("AmmoAmount");
        lending.reservationDate = Instant.parse(rs.getString("ReservationDate"));
        lending.totalPrice = rs.getDouble("TotalPrice");

        return lending;
    }

    public static String toSqlTableDefinition() {
        return SQL_DEFINITION;
    }

    @Override
    public String toString() {
        return "Lending{" +
                "userId=" + userId +
                ", gunId=" + gunId +
                ", ammoId=" + ammoId +
                ", ammoAmount=" + ammoAmount +
                ", reservationDate=" + reservationDate +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
