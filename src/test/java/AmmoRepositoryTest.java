import gunlender.domain.entities.Ammo;
import gunlender.infrastructure.database.AmmoRepository;
import gunlender.infrastructure.database.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
 class AmmoRepositoryTest extends BaseRepositoryTest {
    private AmmoRepository getRepository() throws Exception {
        return (AmmoRepository) getRepository(AmmoRepository.class);
    }

    @Test
    void insertingNewUserDoesNotThrow() throws Exception {
        var ammoRepo = getRepository();
        var ammo = new Ammo(caliber(), amount(), price(), picture());
        assertDoesNotThrow(() -> ammoRepo.addAmmo(ammo));
    }

    @Test
    void retrievingAllUsersDoesNotThrow() throws Exception {
        var ammoRepo = getRepository();

        var ammo1 = new Ammo(caliber(), amount(), price(), picture());
        var ammo2 = new Ammo(caliber(), amount(), price(), picture());
        var ammo3 = new Ammo(caliber(), amount(), price(), picture());

        assertDoesNotThrow(() -> ammoRepo.addAmmo(ammo1));
        assertDoesNotThrow(() -> ammoRepo.addAmmo(ammo2));
        assertDoesNotThrow(() -> ammoRepo.addAmmo(ammo3));

        assertDoesNotThrow(() -> {
            var ammo = ammoRepo.getAmmo();
            assertEquals(3, ammo.size());
        });
    }

    @Test
    void retrievingExistingUserByIdDoesNotThrow() throws Exception {
        var ammoRepo = getRepository();

        var ammo1 = new Ammo(caliber(), amount(), price(), picture());

        assertDoesNotThrow(() -> ammoRepo.addAmmo(ammo1));

        assertDoesNotThrow(() -> {
            var ammo = ammoRepo.getAmmoById(ammo1.getId());
            assert (ammo.isPresent());
            assertEquals(ammo.get().getId(), ammo1.getId());
        });
    }

    @Test
    void retrievingNonExistingUserByIdDoesNotThrow() throws Exception {
        var ammoRepo = getRepository();

        assertDoesNotThrow(() -> {
            var ammo = ammoRepo.getAmmoById(UUID.randomUUID());
            assert (ammo.isEmpty());
        });
    }

    private String caliber() {
        return FAKER.cat().breed();
    }

    private int amount() {
        return (int) FAKER.number().randomNumber(2, false);
    }

    private double price() {
        return FAKER.number().randomDouble(2, 10,  1000);
    }

    private String picture() {
        return FAKER.regexify("https://[a-z]{5,20}.local");
    }
}
