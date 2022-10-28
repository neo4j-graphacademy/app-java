package neoflix;

import neoflix.services.MovieService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import static neoflix.Params.Order.ASC;
import static neoflix.Params.Order.DESC;
import static neoflix.Params.Sort.*;
import static org.junit.jupiter.api.Assertions.*;

class _02_MovieListTest {

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
    void applyOrderListAndSkip() {
        MovieService movieService = new MovieService(driver);
        var limit = 1;
        var output = movieService.all(new Params(null, imdbRating, ASC, limit, 0), null);
        assertNotNull(output);
        assertEquals(limit, output.size());
        assertNotNull(output.get(0));
        var firstTitle = output.get(0).get("title");
        assertNotNull(firstTitle);
        assertEquals("Ring of Terror", firstTitle);

        var skip = 1;
        var next = movieService.all(new Params(null, Params.Sort.imdbRating, ASC, limit, skip), null);
        assertNotNull(next);
        assertEquals(limit, next.size());
        assertNotEquals(firstTitle, next.get(0).get("title"));
    }

    @Test
    void testSorting() {
        MovieService movieService = new MovieService(driver);
        var limit = 1;
        var byReleased = movieService.all(new Params(null, released, DESC, limit, 0), null);
        assertNotNull(byReleased);
        assertEquals(limit, byReleased.size());
        assertNotNull(byReleased.get(0));
        var releaseDate = byReleased.get(0).get("released");
        assertNotNull(releaseDate);
        assertEquals("2016-09-02", releaseDate);

        var byRating = movieService.all(new Params(null, Params.Sort.imdbRating, DESC, limit, 0), null);
        assertNotNull(byRating);
        assertEquals(limit, byRating.size());
        assertNotNull(byRating.get(0));
        var rating = byRating.get(0).get("imdbRating");
        assertNotNull(rating);
        assertEquals(9.6, rating);
    }

    @Test
    void orderMoviesByRating() {
        var movieService = new MovieService(driver);
        var limit = 1;
        var output = movieService.all(new Params(null, imdbRating, DESC, limit, 0), null);
        assertNotNull(output);
        assertEquals(limit, output.size());
        assertNotNull(output.get(0));
        var firstTitle = output.get(0).get("title");
        assertNotNull(firstTitle);

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                What is the title of the highest rated movie in the recommendations dataset?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(firstTitle);
    }
}