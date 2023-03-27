package gunlender.server.routes;

import gunlender.application.dto.ChangePasswordDto;
import gunlender.domain.entities.User;
import gunlender.domain.exceptions.CryptoException;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.domain.services.CryptoService;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChangePasswordHandler implements Handler {
    private final CryptoService cryptoService;
    private final UserRepository userRepository;

    public ChangePasswordHandler(CryptoService cryptoService, UserRepository userRepository) {
        this.cryptoService = cryptoService;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(@NotNull Context ctx) throws RepositoryException, CryptoException {
        var userId = UUID.fromString(ctx.pathParam("user-id"));
        var changePasswordDto = ctx.bodyAsClass(ChangePasswordDto.class);

        var user = userRepository.getUserById(userId);

        if (user.isPresent()) {
            if (accountBelongsToLoggedUser(ctx, user.get()) || AuthManager.isLoggedUserAdmin(ctx)) {
                var hash = cryptoService.hashPassword(changePasswordDto.getPassword());
                userRepository.updateUserPassword(user.get().getId(), hash);
            } else {
                ctx.status(400).result("Insufficient permissions");
            }
        } else {
            ctx.status(404);
        }
    }

    private boolean accountBelongsToLoggedUser(Context ctx, User user) {
        return user.getEmail().equals(ctx.sessionAttribute("Email"));
    }
}
