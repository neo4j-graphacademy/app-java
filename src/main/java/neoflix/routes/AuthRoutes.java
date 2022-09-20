package neoflix.routes;

import io.javalin.apibuilder.EndpointGroup;
import neoflix.services.AuthService;
import org.neo4j.driver.Driver;

import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthRoutes implements EndpointGroup {
    private final AuthService authService;

    public AuthRoutes(Driver driver, String jwtSecret) {
        authService = new AuthService(driver, jwtSecret);
    }

    static class UserData { String email, name, password; };

    @Override
    public void addEndpoints() {
        /*
         * @POST /auth/login
         *
         * Authenticates the user against the Neo4j database.
         *
         * The Authorization header contains a JWT token, which is used to authenticate the request.
         */
        // tag::login[]
        post("/login", ctx -> {
            var userData = ctx.bodyAsClass(UserData.class);
            var user = authService.authenticate(userData.email, userData.password);
            if (user != null) {
                ctx.attribute("user", user.get("userId"));
            }
            ctx.json(user);
        });
        // end::login[]

        /*
         * @POST /auth/register
         *
         * This route should use the AuthService to create a new User node
         * in the database with an encrypted password before returning a User record which
         * includes a `token` property.  This token is then used in the `JwtStrategy` from
         * `src/passport/jwt.strategy.js` to authenticate the request.
         */
        // tag::register[]
        post("/register", ctx -> {
            var userData = ctx.bodyAsClass(UserData.class);
            ctx.json(authService.register(userData.email, userData.password, userData.name));
        });
        // end::register[]
    }
}
