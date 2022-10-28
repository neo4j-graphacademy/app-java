package neoflix;

import neoflix.services.GenreService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class _09_GenreListTest {
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
    void getGenreList() {
        GenreService genreService = new GenreService(driver);

        var output = genreService.all();
        assertNotNull(output);
        assertEquals(19, output.size());
        assertEquals("Action", output.get(0).get("name"));
        assertEquals("Western", output.get(18).get("name"));

        output.sort(Comparator.comparing(m -> Integer.parseInt(m.get("movies").toString()), Comparator.nullsLast(Comparator.reverseOrder())));

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                Which genre has the highest movie count?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(output.get(0).get("name"));
    }
}
