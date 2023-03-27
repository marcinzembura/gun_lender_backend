import gunlender.domain.entities.Gun;
import gunlender.domain.entities.Weapon;
import gunlender.infrastructure.database.GunRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class GunRepositoryTest extends BaseRepositoryTest {
    private GunRepository getRepository() throws Exception {
        return (GunRepository) getRepository(GunRepository.class);
    }

    @Test
    void insertingNewUserDoesNotThrow() throws Exception {
        var gunRepo = getRepository();
        var gun = new Gun(producer(), model(), weaponType(), caliber(), weight(), length(), amount(), price(), picture());
        assertDoesNotThrow(() -> gunRepo.addGun(gun));
    }

    @Test
    void retrievingAllUsersDoesNotThrow() throws Exception {
        var gunRepo = getRepository();

        var gun1 = new Gun(producer(), model(), weaponType(), caliber(), weight(), length(), amount(), price(), picture());
        var gun2 = new Gun(producer(), model(), weaponType(), caliber(), weight(), length(), amount(), price(), picture());
        var gun3 = new Gun(producer(), model(), weaponType(), caliber(), weight(), length(), amount(), price(), picture());

        assertDoesNotThrow(() -> gunRepo.addGun(gun1));
        assertDoesNotThrow(() -> gunRepo.addGun(gun2));
        assertDoesNotThrow(() -> gunRepo.addGun(gun3));

        assertDoesNotThrow(() -> {
            var guns = gunRepo.getGuns();
            assertEquals(3, guns.size());
        });
    }

    @Test
    void retrievingExistingUserByIdDoesNotThrow() throws Exception {
        var gunRepo = getRepository();

        var gun1 = new Gun(producer(), model(), weaponType(), caliber(), weight(), length(), amount(), price(), picture());

        assertDoesNotThrow(() -> gunRepo.addGun(gun1));

        assertDoesNotThrow(() -> {
            var guns = gunRepo.getGunById(gun1.getId());
            assert (guns.isPresent());
            assertEquals(guns.get().getId(), gun1.getId());
        });
    }

    @Test
    void retrievingNonExistingUserByIdDoesNotThrow() throws Exception {
        var gunRepo = getRepository();

        assertDoesNotThrow(() -> {
            var gun = gunRepo.getGunById(UUID.randomUUID());
            assert (gun.isEmpty());
        });
    }

    private String producer() {
        return FAKER.company().name();
    }

    private String model() {
        return FAKER.dog().name();
    }

    private Weapon.WeaponType weaponType() {
        return switch (FAKER.number().numberBetween(1, 6)) {
            case 1 -> Weapon.WeaponType.PISTOL;
            case 2 -> Weapon.WeaponType.REVOLVER;
            case 3 -> Weapon.WeaponType.SUB_MACHINE_GUN;
            case 4 -> Weapon.WeaponType.CARBINE;
            case 5 -> Weapon.WeaponType.RIFLE;
            default -> Weapon.WeaponType.SHOTGUN;
        };
    }

    private String caliber() {
        return FAKER.cat().name();
    }

    private double weight() {
        return FAKER.number().randomDouble(2, 3, 10);
    }

    private int length() {
        return (int) FAKER.number().randomNumber(3, false);
    }

    private int amount() {
        return (int) FAKER.number().randomNumber(2, false);
    }

    private double price() {
        return FAKER.number().randomDouble(2, 10, 1000);
    }

    private String picture() {
        return FAKER.regexify("https://[a-z]{5,20}.local");
    }
}
