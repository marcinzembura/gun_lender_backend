package gunlender.server.routes;

import gunlender.application.dto.UpdateUserDto;
import gunlender.domain.entities.User;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class UserController implements CrudHandler {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void create(@NotNull Context ctx) {
        ctx.redirect("/register");
    }

    @Override
    public void delete(@NotNull Context ctx, @NotNull String s) {
        Optional<User> userToDelete;

        try {
            userToDelete = userRepository.getUserById(UUID.fromString((s)));
        } catch (RepositoryException ex) {
            logger.error("Cannot delete user", ex);
            ctx.status(500);
            return;
        }

        if (userToDelete.isPresent()) {
            var user = userToDelete.get();
            if (accountBelongsToLoggedUser(ctx, user) || AuthManager.isLoggedUserAdmin(ctx)) {
                deleteUser(ctx, user);
                ctx.status(200);
            } else {
                ctx.status(400).result("Insufficient permissions");
            }
        } else {
            ctx.status(404);
        }
    }

    @Override
    public void getAll(@NotNull Context ctx) {
        if (!AuthManager.isLoggedUserAdmin(ctx)) {
            ctx.status(401);
            return;
        }

        try {
            var users = userRepository.getUsers();
            ctx.json(users);
        } catch (RepositoryException ex) {
            logger.error("Cannot fetch all users", ex);
            ctx.status(500);
        }
    }

    @Override
    public void getOne(@NotNull Context ctx, @NotNull String s) {
        Optional<User> user;

        try {
            user = userRepository.getUserById(UUID.fromString((s)));
        } catch (RepositoryException ex) {
            logger.error("Cannot delete user", ex);
            ctx.status(500);
            return;
        }

        if (user.isPresent()) {
            ctx.json(user.get());
        } else {
            ctx.status(404);
        }
    }

    @Override
    public void update(@NotNull Context ctx, @NotNull String s) {
        var updateUserDto = ctx.bodyAsClass(UpdateUserDto.class);

        Optional<User> userToUpdate;

        try {
            userToUpdate = userRepository.getUserById(UUID.fromString((s)));
        } catch (RepositoryException ex) {
            logger.error("Cannot delete user", ex);
            ctx.status(500);
            return;
        }

        if (userToUpdate.isPresent()) {
            var user = userToUpdate.get();
            if (AuthManager.isLoggedUserAdmin(ctx) || accountBelongsToLoggedUser(ctx, user)) {
                try {
                    userRepository.updateUser(user.getId(), updateUserDto);
                } catch (RepositoryException ex) {
                    logger.error("Cannot update user", ex);
                    ctx.status(500);
                }
            } else {
                ctx.status(400).result("Insufficient permissions");
            }
        }

    }

    private boolean accountBelongsToLoggedUser(Context ctx, User user) {
        return user.getEmail().equals(ctx.sessionAttribute("Email"));
    }

    private void deleteUser(Context ctx, User user) {
        try {
            userRepository.deleteUser(user.getId());
        } catch (RepositoryException ex) {
            logger.error("Cannot delete user", ex);
            ctx.status(500);
        }
    }

}
