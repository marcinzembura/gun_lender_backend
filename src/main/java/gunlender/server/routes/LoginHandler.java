package gunlender.server.routes;

import gunlender.application.dto.LoginDto;
import gunlender.domain.exceptions.CryptoException;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.CryptoService;
import gunlender.domain.services.JwtService;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class LoginHandler implements Handler {
    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final JwtService jwtService;

    public LoginHandler(UserRepository userRepository, CryptoService cryptoService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.cryptoService = cryptoService;
        this.jwtService = jwtService;
    }

    @Override
    public void handle(@NotNull Context ctx) throws RepositoryException, CryptoException {
        var loginDto = ctx.bodyAsClass(LoginDto.class);
        var user = userRepository.getUserByEmail(loginDto.getEmail());

        if (user.isEmpty()) {
            ctx.status(400).result("Password or email address are invalid");
            return;
        }

        var storedPasswordHash = user.get().getPasswordHash();

        if (cryptoService.comparePasswordAndHash(loginDto.getPassword(), storedPasswordHash)) {
            var jwt = jwtService.generateJwt(user.get().getEmail(), user.get().getAccountType().name());
            ctx.status(200).header("Authorization", "Bearer " + jwt);
            ctx.status(200).header("UserRole", user.get().getAccountType().name() );
        } else {
            ctx.status(400).result("Password or email address are invalid");
        }
    }
}
