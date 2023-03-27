package gunlender.domain.services;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

public class AuthManager implements AccessManager {

    private final JwtService jwtService;

    public AuthManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    public void manage(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> set) throws Exception {
        var userInfo = getUserInfo(ctx);

        if (set.contains(userInfo.Role)) {
            ctx.sessionAttribute("Role", roleToString(userInfo.Role));
            ctx.sessionAttribute("Email", userInfo.Email);
            handler.handle(ctx);
        } else {
            ctx.status(401).result("Unauthorized");
        }
    }

    private UserInfo getUserInfo(Context ctx) {
        var jwt = ctx.header("Authorization");

        if (jwt != null) {
            var claims = jwtService.getClaims(jwt);
            if (claims.isPresent()) {
                var role = claims.get().getBody().get("Role", String.class);
                var subject = claims.get().getBody().getSubject();
                return new UserInfo(roleFromString(role), subject);
            }
        }

        return new UserInfo(Role.ANYONE, "");
    }

    private record UserInfo(Role Role, String Email) {}

    public enum Role implements RouteRole {
        ANYONE, STANDARD_USER, ADMINISTRATOR
    }

    public static Role roleFromString(String role) {
        return switch (role.toLowerCase(Locale.ROOT)) {
            case "standard_user" -> Role.STANDARD_USER;
            case "administrator" -> Role.ADMINISTRATOR;
            default -> Role.ANYONE;
        };
    }

    public static String roleToString(Role role) {
        return switch (role) {
            case ADMINISTRATOR -> "administrator";
            case STANDARD_USER -> "standard_user";
            case ANYONE -> "anyone";
        };
    }

    public static boolean isLoggedUserAdmin(Context ctx) {
        return ctx.sessionAttribute("Role") == AuthManager.roleToString(Role.ADMINISTRATOR);
    }

    public static boolean isLogged(Context ctx) {
        return ctx.sessionAttribute("Role") != AuthManager.roleToString(Role.ANYONE);
    }
}
