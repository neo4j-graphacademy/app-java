package neoflix;

import neoflix.services.AuthService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class _04_ConstraintErrorTest {
    private static Driver driver;
    private static String jwtSecret;

    private static final String email = UUID.randomUUID() +"@neo4j.com";
    private static final String password = UUID.randomUUID().toString();
    private static final String name = "Graph Academy";

    @BeforeAll
    static void initDriverAuth() {
        AppUtils.loadProperties();
        driver = AppUtils.initDriver();
        jwtSecret = AppUtils.getJwtSecret();
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) {
            driver.session().executeWrite(tx -> tx.run("MATCH (u:User {email: $email}) DETACH DELETE u", Values.parameters("email", email)));
            driver.close();
        }
    }

    /*
     * If this error fails, try running the following query in your Sandbox to create the unique constraint
     *   CREATE CONSTRAINT UserEmailUnique FOR ( user:User ) REQUIRE (user.email) IS UNIQUE
     */
    @Test
    void findUniqueConstraint() {
        Assumptions.assumeTrue(driver != null);
        try (var session = driver.session()) {
            session.executeRead(tx -> {
                var constraint = tx.run("""
                    SHOW CONSTRAINTS
                    YIELD name, type, labelsOrTypes, properties
                    WHERE type = 'UNIQUENESS'
                    AND 'User' IN labelsOrTypes
                    AND 'email' IN properties
                    RETURN name
                """);
                assertNotNull(constraint);
                assertEquals(1, constraint.stream().count(), "Found unique constraint");
                return null;
            });
        }
    }

    @Test
    void checkConstraintWithDuplicateUser() {
        AuthService authService = new AuthService(driver, jwtSecret);
        var output = authService.register(email, password, name);

        assertEquals(email, output.get("email"), "email property");
        assertEquals(name, output.get("name"), "name property");
        assertNotNull(output.get("token"), "token property generated");
        assertNotNull(output.get("userId"), "userId property generated");
        assertNull(output.get("password"), "no password returned");

        //Retry with same credentials
        try {
            authService.register(email, password, name);
            fail("Retry should fail");
        } catch (Exception e) {
            assertEquals("An account already exists with the email address", e.getMessage());
        }
    }
}
