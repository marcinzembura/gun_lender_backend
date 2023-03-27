package gunlender;

import gunlender.domain.exceptions.CryptoException;
import gunlender.domain.exceptions.RepositoryException;
import gunlender.domain.services.AuthManager;
import gunlender.domain.services.CryptoService;
import gunlender.domain.services.JwtService;
import gunlender.infrastructure.database.AmmoRepository;
import gunlender.infrastructure.database.GunRepository;
import gunlender.infrastructure.database.LendingRepository;
import gunlender.infrastructure.database.UserRepository;
import gunlender.server.routes.*;
import io.javalin.Javalin;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    public static void main(String[] args) {
        var logger = LoggerFactory.getLogger(Main.class);
        var connectionStr = "jdbc:sqlite:gunlender.db";

        var userRepo = new UserRepository(connectionStr);
        var ammoRepo = new AmmoRepository(connectionStr);
        var gunRepo = new GunRepository(connectionStr);
        var lendingRepo = new LendingRepository(connectionStr);

        try {
            userRepo.migrate();
            ammoRepo.migrate();
            gunRepo.migrate();
            lendingRepo.migrate();
        } catch (RepositoryException e) {
            logger.error("Cannot migrate repository", e);
            System.exit(1);
        }

        var cryptoService = new CryptoService();
        var jwtService = new JwtService();

        try {
            var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            cryptoService.setSkf(skf);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Cannot create CryptoService", e);
            System.exit(1);
        }

        var app = Javalin.create(config -> config.plugins.enableCors(cors -> cors.add(it -> {
            it.anyHost();
            it.exposeHeader("Authorization");
            it.exposeHeader("UserRole");
        })));
        app.cfg.accessManager(new AuthManager(jwtService));
        app.routes(() -> {
            get("health_check", new HealthCheckHandler(), AuthManager.Role.ANYONE);
            get("me", new UserInfoHandler(userRepo), AuthManager.Role.STANDARD_USER, AuthManager.Role.ADMINISTRATOR);
            post("register", new RegisterHandler(userRepo, cryptoService), AuthManager.Role.ANYONE);
            post("login", new LoginHandler(userRepo, cryptoService, jwtService), AuthManager.Role.ANYONE);
            patch("user/{user-id}/password/", new ChangePasswordHandler(cryptoService, userRepo),
                    AuthManager.Role.STANDARD_USER, AuthManager.Role.ADMINISTRATOR);
            patch("user/{user-id}/role/", new ChangeRoleHandler(userRepo), AuthManager.Role.ADMINISTRATOR);
            crud("user/{user-id}", new UserController(userRepo), AuthManager.Role.STANDARD_USER,
                    AuthManager.Role.ADMINISTRATOR);
            crud("ammo/{ammo-id}", new AmmoController(ammoRepo), AuthManager.Role.STANDARD_USER,
                    AuthManager.Role.ADMINISTRATOR);
            crud("gun/{gun-id}", new GunController(gunRepo), AuthManager.Role.STANDARD_USER,
                    AuthManager.Role.ADMINISTRATOR, AuthManager.Role.ANYONE);
            crud("lending/{lending-id}", new LendingController(lendingRepo, gunRepo, ammoRepo, userRepo),
                    AuthManager.Role.STANDARD_USER, AuthManager.Role.ADMINISTRATOR);
        });

        app.exception(RepositoryException.class, (ex, ctx) -> {
            logger.error("Repository error", ex);
            ctx.status(500);
        });
        app.exception(CryptoException.class, (ex, ctx) -> {
            logger.error("Cryptography error", ex);
            ctx.status(500);
        });
        app.start(8080);
    }
}