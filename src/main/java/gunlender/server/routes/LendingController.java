package gunlender.server.routes;

import gunlender.application.dto.GunDto;
import gunlender.application.dto.LendingDto;
import gunlender.application.dto.UpdateLendingDto;
import gunlender.domain.entities.Ammo;
import gunlender.domain.entities.Gun;
import gunlender.domain.entities.Lending;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.infrastructure.database.AmmoRepository;
import gunlender.infrastructure.database.GunRepository;
import gunlender.infrastructure.database.LendingRepository;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class LendingController implements CrudHandler {
    private final Logger logger = LoggerFactory.getLogger(LendingController.class);
    private final LendingRepository lendingRepository;
    private final GunRepository gunRepository;
    private final AmmoRepository ammoRepository;
    private final UserRepository userRepository;

    public LendingController(LendingRepository lendingRepository, GunRepository gunRepository,
                             AmmoRepository ammoRepository, UserRepository userRepository) {
        this.lendingRepository = lendingRepository;
        this.gunRepository = gunRepository;
        this.ammoRepository = ammoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void create(@NotNull Context ctx) {
        var lendingDto = ctx.bodyAsClass(LendingDto.class);
        Optional<Gun> gunOpt = getGun(lendingDto.getGunId(), ctx);
        Optional<Ammo> ammoOpt = getAmmo(lendingDto.getAmmoId(), ctx);

        if (gunOpt.isEmpty()) {
            return;
        }

        if (ammoOpt.isEmpty()) {
            return;
        }

        var gun = gunOpt.get();

        if (gun.getAmount() <= 0) {
            logger.error("Cannot lend gun because requested gun is not available");
            ctx.status(400);
            return;
        }

        var ammo = ammoOpt.get();
        var price = gun.getPrice() + ammo.getPrice() * lendingDto.getAmmoAmount();

        var newGun = GunDto.fromEntity(gun);
        newGun.setAmount(newGun.getAmount() - 1);

        var lending = new Lending(lendingDto.getUserId(),
                lendingDto.getGunId(),
                lendingDto.getAmmoId(),
                lendingDto.getAmmoAmount(),
                lendingDto.getReservationDate(),
                price
        );

        Connection conn = null;

        try {
            conn = lendingRepository.getConnection();
            conn.setAutoCommit(false);
            gunRepository.updateGun(gun.getId(), newGun, conn);
            lendingRepository.addLending(lending, conn);
            conn.commit();
            ctx.status(201);
        } catch (RepositoryException | SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex2) {
                    logger.error("Cannot rollback transaction", ex2);
                }
            }
            logger.error("Cannot lend gun", ex);
            ctx.status(500);
        }
    }

    @Override
    public void delete(@NotNull Context ctx, @NotNull String s) {
        var userId = UUID.fromString(s);
        var gunId = UUID.fromString(Objects.requireNonNull(ctx.queryParam("gun")));
        var ammoId = UUID.fromString(Objects.requireNonNull(ctx.queryParam("ammo")));

        Optional<Lending> lendingToDelete;

        try {
            lendingToDelete = lendingRepository.getLending(userId, gunId, ammoId);
        } catch (RepositoryException ex) {
            logger.error("Cannot delete lending", ex);
            ctx.status(500);
            return;
        }

        Connection conn = null;

        try {
            conn = lendingRepository.getConnection();
            conn.setAutoCommit(false);

            if (lendingToDelete.isEmpty()) {
                ctx.status(404);
                return;
            }

            var lending = lendingToDelete.get();
            var cannotDelete = !(lendingBelongsToLoggedUser(ctx, lending) || AuthManager.isLoggedUserAdmin(ctx));

            if (cannotDelete) {
                ctx.status(401).result("Insufficient permissions");
                return;
            }

            lendingRepository.deleteLending(lending.getUserId(),
                    lending.getGunId(),
                    lending.getAmmoId(),
                    conn);

            Optional<Gun> gunOpt = getGun(gunId, ctx);

            if (gunOpt.isPresent()) {
                var gun = gunOpt.get();
                var newGun = GunDto.fromEntity(gun);
                newGun.setAmount(gun.getAmount() + 1);
                gunRepository.updateGun(gun.getId(), newGun, conn);
            }

            conn.commit();
            ctx.status(200);
        } catch (RepositoryException | SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex2) {
                    logger.error("Cannot rollback transaction", ex2);
                }
            }
            logger.error("Cannot delete lending data", ex);
            ctx.status(500);
        }
    }

    @Override
    public void getAll(@NotNull Context ctx) {
        List<Lending> lendings;

        if (AuthManager.isLoggedUserAdmin(ctx)) {
            try {
                lendings = lendingRepository.getLendings();
            } catch (RepositoryException ex) {
                logger.error("Cannot get all lendings for administrator", ex);
                ctx.status(500);
                return;
            }
        } else {
            try {
                var loggedUser = userRepository.getUserByEmail(ctx.sessionAttribute("Email"));

                if (loggedUser.isEmpty()) {
                    ctx.status(401);
                    return;
                }

                lendings = lendingRepository.getLendingByUserId(loggedUser.get().getId());
            } catch (RepositoryException ex) {
                logger.error("Cannot get all lendings for user", ex);
                ctx.status(500);
                return;
            }
        }

        ctx.json(lendings);
    }
    @Override
    public void getOne(@NotNull Context ctx, @NotNull String s) {
        var userId = UUID.fromString(s);
        var gunId = UUID.fromString(Objects.requireNonNull(ctx.queryParam("gun")));
        var ammoId = UUID.fromString(Objects.requireNonNull(ctx.queryParam("ammo")));

        Optional<Lending> lendingOpt;

        try {
            lendingOpt = lendingRepository.getLending(userId, gunId, ammoId);
        } catch (RepositoryException ex) {
            logger.error("Cannot get lending from database", ex);
            ctx.status(500);
            return;
        }

        if (lendingOpt.isEmpty()) {
            ctx.status(404).result("Lending data with given ID doesn't exist");
            return;
        }

        ctx.json(lendingOpt.get());
    }

    @Override
    public void update(@NotNull Context ctx, @NotNull String s) {
        var gunId = UUID.fromString(Objects.requireNonNull(ctx.queryParam("gun")));
        var ammoId = UUID.fromString(Objects.requireNonNull(ctx.queryParam("ammo")));

        var lendingDto = ctx.bodyAsClass(LendingDto.class);

        var lendingOpt = getLending(lendingDto.getUserId(), lendingDto.getGunId(),
                lendingDto.getAmmoId(), ctx);

        if (lendingOpt.isEmpty()) {
            return;
        }

        var lending = lendingOpt.get();

        try {
            if (lendingBelongsToLoggedUser(ctx, lending) || AuthManager.isLoggedUserAdmin(ctx)) {
                var updateLendingDto = new UpdateLendingDto(lendingDto, gunId, ammoId);
                updateLending(updateLendingDto, ctx);
            }
        } catch (RepositoryException ex) {
            logger.error("Cannot update lending data in database", ex);
            ctx.status(500);
        }


    }

    private void updateLending(UpdateLendingDto updateLendingDto, Context ctx) {
        var newGunOpt = getGun(updateLendingDto.getNewGunId(), ctx);

        if (newGunOpt.isEmpty()) {
            return;
        }

        var oldGunOpt = getGun(updateLendingDto.getOldGunId(), ctx);

        if (oldGunOpt.isEmpty()) {
            return;
        }

        var newAmmoOpt = getAmmo(updateLendingDto.getNewAmmoId(), ctx);

        if (newAmmoOpt.isEmpty()) {
            return;
        }

        var oldGun = oldGunOpt.get();
        var newGun = newGunOpt.get();

        if (newGun.getAmount() <= 0) {
            logger.error("Cannot lend gun because requested gun is not available");
            ctx.status(400).result("Requested gun is not available");
            return;
        }

        var newAmmo = newAmmoOpt.get();
        var newPrice = newGun.getPrice() + newAmmo.getPrice() * updateLendingDto.getAmmoAmount();

        Connection conn = null;

        try {
            conn = lendingRepository.getConnection();
            conn.setAutoCommit(false);

            if (updateLendingDto.getNewGunId() != updateLendingDto.getOldGunId()) {
                // Increase old gun amount
                var oldGunDto = GunDto.fromEntity(oldGun);
                oldGunDto.setAmount(oldGunDto.getAmount() + 1);
                gunRepository.updateGun(oldGun.getId(), oldGunDto, conn);

                // Decrease new gun amount
                var newGunDto = GunDto.fromEntity(newGun);
                newGunDto.setAmount(newGunDto.getAmount() - 1);
                gunRepository.updateGun(newGun.getId(), newGunDto, conn);
            }

            lendingRepository.updateLending(updateLendingDto, newPrice, conn);

            conn.commit();
            ctx.status(201);
        } catch (RepositoryException | SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex2) {
                    logger.error("Cannot rollback transaction", ex2);
                }
            }
            logger.error("Cannot lend gun", ex);
            ctx.status(500);
        }
    }

    private Optional<Lending> getLending(UUID userId, UUID gunId, UUID ammoId, Context ctx) {
        try {
            var lending = lendingRepository.getLending(userId, gunId, ammoId);
            if (lending.isEmpty()) {
                ctx.status(404);
            }
            return lending;
        } catch (RepositoryException ex) {
            logger.error("Cannot update lending data", ex);
            ctx.status(500);
            return Optional.empty();
        }
    }

    private Optional<Gun> getGun(UUID gunId, Context ctx) {
        try {
            var gun = gunRepository.getGunById(gunId);
            if (gun.isEmpty()) {
                ctx.status(404).result("Gun with given ID doesn't exist");
            }
            return gun;
        } catch (RepositoryException ex) {
            logger.error("Cannot get gun for lending", ex);
            ctx.status(500);
            return Optional.empty();
        }
    }

    private Optional<Ammo> getAmmo(UUID ammoId, Context ctx) {
        try {
            var ammo = ammoRepository.getAmmoById(ammoId);
            if (ammo.isEmpty()) {
                ctx.status(404).result("Ammo with given ID doesn't exist");
            }
            return ammo;
        } catch (RepositoryException ex) {
            logger.error("Cannot get ammo for lending", ex);
            ctx.status(500);
            return Optional.empty();
        }
    }

    private boolean lendingBelongsToLoggedUser(Context ctx, Lending lending) throws RepositoryException {
        var user = userRepository.getUserByEmail(ctx.sessionAttribute("Email"));
        return user.map(value -> value.getId().equals(lending.getUserId())).orElse(false);
    }
}
