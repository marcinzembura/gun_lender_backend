package gunlender.server.routes;

import gunlender.domain.entities.User;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UserInfoHandler implements Handler {

    private final UserRepository userRepository;

    public UserInfoHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        final String email = ctx.sessionAttribute("Email");
        if (email != null) {
            final Optional<User> user = userRepository.getUserByEmail(email);
            user.ifPresent(ctx::json);
            ctx.status(201);
        } else {
            ctx.status(500);
        }
    }
}
