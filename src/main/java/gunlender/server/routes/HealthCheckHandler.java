package gunlender.server.routes;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class HealthCheckHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.status(200);
    }
}
