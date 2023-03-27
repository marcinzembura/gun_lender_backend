package gunlender.server.routes;

import gunlender.application.dto.ChangeRoleDto;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChangeRoleHandler implements Handler {
    private final UserRepository userRepository;

    public ChangeRoleHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(@NotNull Context ctx) throws RepositoryException {
        var userId = UUID.fromString(ctx.pathParam("user-id"));
        var changeRoleDto = ctx.bodyAsClass(ChangeRoleDto.class);

        var user = userRepository.getUserById(userId);

        if (user.isPresent()) {
            if (AuthManager.isLoggedUserAdmin(ctx)) {
                userRepository.updateUserRole(user.get().getId(), changeRoleDto.getRole());
            } else {
                ctx.status(400).result("Insufficient permissions");
            }
        } else {
            ctx.status(404);
        }
    }
}
