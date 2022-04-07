package neoflix;

import static spark.Spark.*;

import java.util.*;
import com.google.gson.Gson;
import neoflix.routes.*;
import org.neo4j.driver.*;

public class NeoflixApp {

    public static void main(String[] args) throws Exception {
        AppUtils.loadProperties();
        int port = AppUtils.getServerPort();
        port(port);
        // tag::driver[]
        Driver driver = AppUtils.initDriver();
        // end::driver[]
        Gson gson = GsonUtils.gson();

        staticFiles.location("/public");
        String jwtSecret = AppUtils.getJwtSecret();
        before((req, res) -> AppUtils.handleAuthAndSetUser(req, jwtSecret));
        path("/api", () -> {
            path("/movies", new MovieRoutes(driver, gson));
            path("/genres", new GenreRoutes(driver, gson));
            path("/auth", new AuthRoutes(driver, gson, jwtSecret));
            path("/account", new AccountRoutes(driver, gson));
            path("/people", new PeopleRoutes(driver, gson));
        });
        exception(ValidationException.class, (exception, request, response) -> {
            response.status(422);
            var body = Map.of("message",exception.getMessage(), "details", exception.getDetails());
            response.body(gson.toJson(body));
            response.type("application/json");
        });
        System.out.printf("Started server at port %d%n", port);
    }
}