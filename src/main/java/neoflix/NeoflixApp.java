package neoflix;

import java.util.*;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import neoflix.routes.*;

import static io.javalin.apibuilder.ApiBuilder.path;

public class NeoflixApp {

    public static void main(String[] args) {
        AppUtils.loadProperties();

        // tag::driver[]
        var driver = AppUtils.initDriver();
        // end::driver[]

        var jwtSecret = AppUtils.getJwtSecret();
        var port = AppUtils.getServerPort();

        var gson = GsonUtils.gson();
        var server = Javalin
            .create(config -> {
                config.addStaticFiles("/", Location.CLASSPATH);
                config.addStaticFiles(staticFiles -> {
                    staticFiles.hostedPath = "/";
                    staticFiles.directory = "/public";
                    staticFiles.location = Location.CLASSPATH;
                });
            })
            .before(ctx -> AppUtils.handleAuthAndSetUser(ctx.req, jwtSecret))
            .routes(() -> {
                path("/api", () -> {
                    path("/movies", new MovieRoutes(driver, gson));
                    path("/genres", new GenreRoutes(driver, gson));
                    path("/auth", new AuthRoutes(driver, gson, jwtSecret));
                    path("/account", new AccountRoutes(driver, gson));
                    path("/people", new PeopleRoutes(driver, gson));
                });
            })
            .exception(ValidationException.class, (exception, ctx) -> {
                var body = Map.of("message", exception.getMessage(), "details", exception.getDetails());
                ctx.status(422).contentType("application/json").result(gson.toJson(body));
            })
            .start(port);
        System.out.printf("Server listening on http://localhost:%d/%n", port);
    }
}