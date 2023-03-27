import gunlender.domain.entities.Lending;
import gunlender.infrastructure.database.LendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class LendingRepositoryTest extends BaseRepositoryTest {

    LendingRepository getRepository() throws Exception {
        return (LendingRepository) getRepository(LendingRepository.class);
    }

    @Test
    void insertingNewLendingDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();
        var lending = new Lending(id(), id(), id(), amount(), date(), price());
        assertDoesNotThrow(() -> lendingRepo.addLending(lending));
    }

    @Test
    void retrievingAllLendingsDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        var lending1 = new Lending(id(), id(), id(), amount(), date(), price());
        var lending2 = new Lending(id(), id(), id(), amount(), date(), price());
        var lending3 = new Lending(id(), id(), id(), amount(), date(), price());

        assertDoesNotThrow(() -> lendingRepo.addLending(lending1));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending2));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending3));

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendings();
            assertEquals(3, lendings.size());
        });
    }

    @Test
    void retrievingExistingLendingByUserIdDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        var userId = id();
        var lending1 = new Lending(userId, id(), id(), amount(), date(), price());
        var lending2 = new Lending(userId, id(), id(), amount(), date(), price());
        var lending3 = new Lending(id(), id(), id(), amount(), date(), price());

        assertDoesNotThrow(() -> lendingRepo.addLending(lending1));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending2));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending3));

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendingByUserId(userId);
            assertEquals(2, lendings.size());
            assertEquals(lendings.get(0).getUserId(), lending1.getUserId());
            assertEquals(lendings.get(1).getUserId(), lending2.getUserId());
        });
    }

    @Test
    void retrievingNonExistingLendingByUserIdDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendingByUserId(UUID.randomUUID());
            assert (lendings.isEmpty());
        });
    }

    @Test
    void retrievingExistingLendingByGunIdDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        var gunId = id();
        var lending1 = new Lending(id(), gunId, id(), amount(), date(), price());
        var lending2 = new Lending(id(), gunId, id(), amount(), date(), price());
        var lending3 = new Lending(id(), id(), id(), amount(), date(), price());

        assertDoesNotThrow(() -> lendingRepo.addLending(lending1));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending2));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending3));

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendingByGunId(gunId);
            assertEquals(2, lendings.size());
            assertEquals(lendings.get(0).getGunId(), lending1.getGunId());
            assertEquals(lendings.get(1).getGunId(), lending2.getGunId());
        });
    }

    @Test
    void retrievingNonExistingLendingByGunIdDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendingByGunId(UUID.randomUUID());
            assert (lendings.isEmpty());
        });
    }


    @Test
    void retrievingExistingLendingByAmmoIdDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        var ammoId = id();
        var lending1 = new Lending(id(), id(), ammoId, amount(), date(), price());
        var lending2 = new Lending(id(), id(), ammoId, amount(), date(), price());
        var lending3 = new Lending(id(), id(), id(), amount(), date(), price());

        assertDoesNotThrow(() -> lendingRepo.addLending(lending1));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending2));
        assertDoesNotThrow(() -> lendingRepo.addLending(lending3));

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendingByAmmoId(ammoId);
            assertEquals(2, lendings.size());
            assertEquals(lendings.get(0).getAmmoId(), lending1.getAmmoId());
            assertEquals(lendings.get(1).getAmmoId(), lending2.getAmmoId());
        });
    }

    @Test
    void retrievingNonExistingLendingByAmmoIdDoesNotThrow() throws Exception {
        var lendingRepo = getRepository();

        assertDoesNotThrow(() -> {
            var lendings = lendingRepo.getLendingByAmmoId(UUID.randomUUID());
            assert (lendings.isEmpty());
        });
    }

    private UUID id() {
        return UUID.randomUUID();
    }

    private int amount() {
        return (int) FAKER.number().randomNumber(2, false);
    }

    private Instant date() {
        return FAKER.date().birthday().toInstant();
    }

    private double price() {
        return FAKER.number().randomDouble(2, 10, 1000);
    }
}