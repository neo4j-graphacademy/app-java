package neoflix;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class _01_ConnectToNeo4jTest {

    @Test
    void createDriverAndConnectToServer() {
        AppUtils.loadProperties();
        assertNotNull(AppUtils.getNeo4jUri(), "neo4j uri defined");
        assertNotNull(AppUtils.getNeo4jUsername(), "username defined");
        assertNotNull(AppUtils.getNeo4jPassword(), "password defined");

        Driver driver = AppUtils.initDriver();
        Assumptions.assumeTrue(driver != null);
        assertNotNull(driver, "driver instantiated");
        assertDoesNotThrow(driver::verifyConnectivity,"unable to verify connectivity");
    }
}