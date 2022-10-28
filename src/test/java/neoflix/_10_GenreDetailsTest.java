package neoflix;

import neoflix.services.GenreService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class _10_GenreDetailsTest {
    private static Driver driver;

    @BeforeAll
    static void initDriver() {
        AppUtils.loadProperties();
        driver = AppUtils.initDriver();
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) driver.close();
    }

    @Test
    void getGenreDetails() {
        GenreService genreService = new GenreService(driver);

        var genreName = "Action";

        var output = genreService.find(genreName);
        assertNotNull(output);
        assertEquals(genreName, output.get("name"));

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                How many movies are in the Action genre?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(output.get("movies"));
    }
}
