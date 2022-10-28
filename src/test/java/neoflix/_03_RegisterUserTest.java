package neoflix;

import neoflix.services.AuthService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import static org.junit.jupiter.api.Assertions.*;

class _03_RegisterUserTest {

    private static Driver driver;
    private static String jwtSecret;

    private static final String email = "graphacademy.register@neo4j.com";
    private static final String password = "letmein";
    private static final String name = "Graph Academy";

    @BeforeAll
    static void initDriverAuth() {
        AppUtils.loadProperties();
        driver = AppUtils.initDriver();
        jwtSecret = AppUtils.getJwtSecret();

        if (driver != null) driver.session().executeWrite(tx -> tx.run("MATCH (u:User {email: $email}) DETACH DELETE u", Values.parameters("email", email)));
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) driver.close();
    }

    @Test
    void registerUser() {
        AuthService authService = new AuthService(driver, jwtSecret);
        var output = authService.register(email, password, name);
        assertNotNull(output);
        assertEquals(4, output.size(), "4 properties returned");

        assertEquals(email, output.get("email"), "email property");
        assertEquals(name, output.get("name"), "name property");
        assertNotNull(output.get("token"), "token property generated");
        assertNotNull(output.get("userId"), "userId property generated");
        assertNull(output.get("password"), "no password returned");

        // Expect user exists in database
        if (driver != null) try (var session = driver.session()) {
            session.executeRead(tx -> {
                    var user = tx.run(
                            "MATCH (u:User {email: $email}) RETURN u",
                            Values.parameters("email", email))
                    .single().get("u").asMap();

                assertEquals(email, user.get("email"), "email property");
                assertEquals(name, user.get("name"), "name property");
                assertNotNull(user.get("userId"), "userId property generated");
                assertNotEquals(password, user.get("password"), "password was hashed");
                return null;
            });
        }
    }
}