package gunlender.application.dto;

import gunlender.domain.entities.Gun;
import gunlender.domain.entities.Weapon;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class GunDto {
    @NotNull
    private String producer;
    @NotNull
    private String model;
    @NotNull
    private Weapon.WeaponType type;
    @NotNull
    private String caliber;
    private double weight;
    private int length;
    private int amount;
    private double price;
    @NotNull
    private String picture;

    public static GunDto fromEntity(Gun gun) {
        var newGun = new GunDto();
        newGun.setProducer(gun.getProducer());
        newGun.setModel(gun.getModel());
        newGun.setType(gun.getType());
        newGun.setCaliber(gun.getCaliber());
        newGun.setWeight(gun.getWeight());
        newGun.setLength(gun.getLength());
        newGun.setAmount(gun.getAmount());
        newGun.setPrice(gun.getPrice());
        newGun.setPicture(gun.getPicture());

        return newGun;
    }
}
