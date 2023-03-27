package gunlender.server.routes;

import gunlender.application.dto.RegisterUserDto;
import gunlender.domain.entities.User;
import gunlender.domain.exceptions.CryptoException;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.domain.services.CryptoService;
import gunlender.infrastructure.database.UserRepository;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class RegisterHandler implements Handler {
    private final UserRepository userRepository;
    private final CryptoService cryptoService;

    public RegisterHandler(UserRepository userRepo, CryptoService cryptoSrv) {
        userRepository = userRepo;
        cryptoService = cryptoSrv;
    }

    @Override
    public void handle(@NotNull Context ctx) throws RepositoryException, CryptoException {
        var registerDto = ctx.bodyAsClass(RegisterUserDto.class);
        var user = userRepository.getUserByEmail(registerDto.getEmail());

        if (user.isPresent()) {
            ctx.status(400).result("Given email is already taken");
            return;
        }

        var passwordHash = cryptoService.hashPassword(registerDto.getPassword());

        var newUser = new User(registerDto.getFirstName(),
                registerDto.getLastName(),
                registerDto.getEmail(),
                registerDto.getPhoneNumber(),
                passwordHash,
                AuthManager.Role.STANDARD_USER);

        userRepository.addUser(newUser);

        ctx.status(201);
    }
}
