package gunlender.server.routes;

import gunlender.application.dto.GunDto;
import gunlender.domain.entities.Gun;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.infrastructure.database.GunRepository;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class GunController implements CrudHandler {
    private final Logger logger = LoggerFactory.getLogger(GunController.class);
    private final GunRepository gunRepository;

    public GunController(GunRepository gunRepository) {
        this.gunRepository = gunRepository;
    }

    @Override
    public void create(@NotNull Context ctx) {
         if (!AuthManager.isLoggedUserAdmin(ctx)) {
             ctx.status(400).result("Insufficient permissions");
             return;
         }

         var gunDto = ctx.bodyAsClass(GunDto.class);

         var gun = new Gun(gunDto.getProducer(), gunDto.getModel(), gunDto.getType(), gunDto.getCaliber(),
                 gunDto.getWeight(), gunDto.getLength(), gunDto.getAmount(), gunDto.getPrice(), gunDto.getPicture());

        try {
            gunRepository.addGun(gun);
            ctx.status(201);
        } catch (RepositoryException ex) {
            logger.error("Cannot create new gun", ex);
            ctx.status(500);
        }
    }

    @Override
    public void delete(@NotNull Context ctx, @NotNull String s) {
        if (!AuthManager.isLoggedUserAdmin(ctx)) {
            ctx.status(400).result("Insufficient permissions");
            return;
        }

        var gunId = UUID.fromString(s);

        try {
            gunRepository.deleteGun(gunId);
            ctx.status(200);
        } catch (RepositoryException ex) {
            logger.error("Cannot delete gun", ex);
            ctx.status(500);
        }
    }

    @Override
    public void getAll(@NotNull Context ctx) {
        try {
            var guns = gunRepository.getGuns();
            ctx.json(guns);
        } catch (RepositoryException ex) {
            logger.error("Cannot get guns", ex);
            ctx.status(500);
        }
    }

    @Override
    public void getOne(@NotNull Context ctx, @NotNull String s) {
        if (!AuthManager.isLogged(ctx)) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        var gunId = UUID.fromString(s);
        Optional<Gun> gun;

        try {
            gun = gunRepository.getGunById(gunId);
        } catch (RepositoryException ex) {
            logger.error("Cannot get gun", ex);
            ctx.status(500);
            return;
        }

        if (gun.isPresent()) {
            ctx.json(gun.get());
        } else {
            ctx.status(404);
        }
    }

    @Override
    public void update(@NotNull Context ctx, @NotNull String s) {
        if (!AuthManager.isLoggedUserAdmin(ctx)) {
            ctx.status(400).result("Insufficient permissions");
            return;
        }

        var gunId = UUID.fromString(s);
        var gunDto = ctx.bodyAsClass(GunDto.class);

        try {
            gunRepository.updateGun(gunId, gunDto);
            ctx.status(200);
        } catch (RepositoryException ex) {
            logger.error("Cannot create new gun", ex);
            ctx.status(500);
        }
    }
}
