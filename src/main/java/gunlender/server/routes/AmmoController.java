package gunlender.server.routes;

import gunlender.application.dto.AmmoDto;
import gunlender.domain.entities.Ammo;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.infrastructure.database.AmmoRepository;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class AmmoController implements CrudHandler {
    private final AmmoRepository ammoRepository;

    public AmmoController(AmmoRepository ammoRepository) {
        this.ammoRepository = ammoRepository;
    }

    @Override
    public void create(@NotNull Context context) {
        if (!AuthManager.isLoggedUserAdmin(context)) {
            context.status(400).result("Insufficient permissions");
            return;
        }

        final AmmoDto ammo = context.bodyAsClass(AmmoDto.class);
        try {
            ammoRepository.addAmmo(
                    new Ammo(
                            ammo.getCaliber(),
                            Integer.parseInt(ammo.getAmount()),
                            Double.parseDouble(ammo.getPrice()),
                            ammo.getPicture()
                    )
            );
            context.status(200);
        } catch (RepositoryException e) {
            context.status(500);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(@NotNull Context context, @NotNull String s) {
        if (!AuthManager.isLoggedUserAdmin(context)) {
            context.status(400).result("Insufficient permissions");
            return;
        }

        try {
            context.status(ammoRepository.removeAmmo(UUID.fromString(s)) == 0 ? 404 : 200);
        } catch (RepositoryException e) {
            context.status(500);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getAll(@NotNull Context context) {
        try {
            context.json(ammoRepository.getAmmo());
            context.status(200);
        } catch (RepositoryException e) {
            context.status(500);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getOne(@NotNull Context context, @NotNull String s) {
        try {
            final Optional<Ammo> ammo = ammoRepository.getAmmoById(UUID.fromString(s));
            if (ammo.isPresent()) {
                context.json(ammo.get());
                context.status(200);
            } else {
                context.status(404);
            }
        } catch (RepositoryException e) {
            context.status(500);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(@NotNull Context context, @NotNull String s) {
        if (!AuthManager.isLoggedUserAdmin(context)) {
            context.status(400).result("Insufficient permissions");
            return;
        }

        final AmmoDto ammo = context.bodyAsClass(AmmoDto.class);
        try {
            ammoRepository.updateAmmo(
                    new Ammo(
                            ammo.getCaliber(),
                            Integer.parseInt(ammo.getAmount()),
                            Double.parseDouble(ammo.getPrice()),
                            ammo.getPicture()
                    )
            );
            context.status(200);
        } catch (RepositoryException e) {
            context.status(500);
            throw new RuntimeException(e);
        }
    }
}
