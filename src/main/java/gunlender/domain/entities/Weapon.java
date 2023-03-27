package gunlender.domain.entities;

import java.util.Locale;

public class Weapon {
    public enum WeaponType {
        REVOLVER, PISTOL, SUB_MACHINE_GUN, CARBINE, RIFLE, SHOTGUN
    }


    public static WeaponType FromString(String weaponType) {
        return switch (weaponType.toLowerCase(Locale.ROOT)) {
            case "revolver" -> WeaponType.REVOLVER;
            case "pistol" -> WeaponType.PISTOL;
            case "sub_machine_gun" -> WeaponType.SUB_MACHINE_GUN;
            case "carbine" -> WeaponType.CARBINE;
            case "rifle" -> WeaponType.RIFLE;
            case "shotgun" -> WeaponType.SHOTGUN;
            default -> throw new IllegalArgumentException();
        };
    }
}

