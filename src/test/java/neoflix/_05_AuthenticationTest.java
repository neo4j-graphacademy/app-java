package neoflix;

import neoflix.services.AuthService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import static org.junit.jupiter.api.Assertions.*;

class _05_AuthenticationTest {
    private static Driver driver;
    private static String jwtSecret;

    private static final String email = "authenticated@neo4j.com";
    private static final String password = "AuthenticateM3!";
    private static final String name = "Authenticated User";

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
    void authenticateUser() {
        AuthService authService = new AuthService(driver, jwtSecret);
        authService.register(email, password, name);

        var output = authService.authenticate(email, password);
        assertEquals(email, output.get("email"), "email property");
        assertEquals(name, output.get("name"), "name property");
        assertNotNull(output.get("token"), "token property generated");
        assertNotNull(output.get("userId"), "userId property generated");
        assertNull(output.get("password"), "no password returned");

        setUserAuthTestTimestamp();
    }

    @Test
    void tryAuthWithIncorrectPassword() {
        AuthService authService = new AuthService(driver, jwtSecret);

        try {
            authService.authenticate(email, "unknown");
            fail("incorrect password auth should fail");
        } catch (Exception e) {
            assertEquals("Incorrect email", e.getMessage());
        }
    }

    @Test
    void tryAuthWithIncorrectUsername() {
        AuthService authService = new AuthService(driver, jwtSecret);

        try {
            authService.authenticate("unknown", "unknown");
            fail("Auth with unknown username should fail");
        } catch (Exception e) {
            assertEquals("Incorrect email", e.getMessage());
        }
    }

    void setUserAuthTestTimestamp() {
        if (driver != null) try (var session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
                        MATCH (u:User {email: $email})
                        SET u.authenticatedAt = datetime()
                        """, Values.parameters("email", email));
                return null;
            });
        }
    }
}
