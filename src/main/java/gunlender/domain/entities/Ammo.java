package gunlender.domain.entities;

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Getter
public class Ammo {
    private UUID id;
    private String caliber;
    private int amount;
    private double price;
    private String picture;

    private static final String SQL_DEFINITION = "(Id VARCHAR(16) UNIQUE, Caliber VARCHAR(32), Amount INT, " +
            "Price DECIMAL(3,2), Picture VARCHAR(256))";


    public Ammo(String caliber, int amount, double price, String picture) {
        this.id = UUID.randomUUID();
        this.caliber = caliber;
        this.amount = amount;
        this.price = price;
        this.picture = picture;
    }

    private Ammo() {}

    public static Ammo fromResultSet(ResultSet rs) throws SQLException {
        var ammo = new Ammo();

        ammo.id = UUID.fromString(rs.getString("Id"));
        ammo.caliber = rs.getString("Caliber");
        ammo.amount = rs.getInt("Amount");
        ammo.price = rs.getDouble("Price");
        ammo.picture = rs.getString("Picture");

        return ammo;
    }

    public static String toSqlTableDefinition() {
        return SQL_DEFINITION;
    }

    @Override
    public String toString() {
        return "Ammo{" +
                "id=" + id +
                ", caliber='" + caliber + '\'' +
                ", amount=" + amount +
                ", price=" + price +
                ", picture='" + picture + '\'' +
                '}';
    }
}
