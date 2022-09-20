package neoflix;

import java.util.*;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.json.JsonMapper;
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
                var jsonMapper = new JsonMapper() {
                    @Override
                    public String toJsonString(Object obj) {
                        return gson.toJson(obj);
                    }

                    @Override
                    public <T> T fromJsonString(String json, Class<T> targetClass) {
                        return gson.fromJson(json, targetClass);
                    }
                };
                config.jsonMapper(jsonMapper);
            })
            .before(ctx -> AppUtils.handleAuthAndSetUser(ctx.req, jwtSecret))
            .routes(() -> {
                path("/api", () -> {
                    path("/movies", new MovieRoutes(driver));
                    path("/genres", new GenreRoutes(driver));
                    path("/auth", new AuthRoutes(driver, jwtSecret));
                    path("/account", new AccountRoutes(driver));
                    path("/people", new PeopleRoutes(driver));
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