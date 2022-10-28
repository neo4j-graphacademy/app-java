package neoflix;

import neoflix.services.MovieService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import static neoflix.Params.Sort.title;
import static org.junit.jupiter.api.Assertions.*;

class _12_MovieDetailsTest {
    private static Driver driver;

    private static final String userId = "fe770c6b-4034-4e07-8e40-2f39e7a6722c";
    private static final String email = "graphacademy.movielists@neo4j.com";
    private static final String lockStock = "100";

    @BeforeAll
    static void initDriver() {
        AppUtils.loadProperties();
        driver = AppUtils.initDriver();
        if (driver != null)
            driver.session().executeWrite(tx -> tx.run("""
                MERGE (u:User {userId: $userId}) SET u.email = $email
                """, Values.parameters("userId", userId, "email", email)));
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) driver.close();
    }

    @Test
    void getMovieById() {
        MovieService movieService = new MovieService(driver);

        var output = movieService.findById(lockStock, userId);
        assertNotNull(output);
        assertEquals(lockStock, output.get("tmdbId"));
        assertEquals("Lock, Stock & Two Smoking Barrels", output.get("title"));
    }

    @Test
    void getSimilarMoviesByScore() {
        MovieService movieService = new MovieService(driver);

        var limit = 1;

        var output = movieService.getSimilarMovies(lockStock, new Params(null, title, Params.Order.ASC, limit, 0), userId);
        var paginated = movieService.getSimilarMovies(lockStock, new Params(null, title, Params.Order.ASC, limit, 1), userId);

        assertNotNull(output);
        assertEquals(limit, output.size());

        assertNotEquals(output, paginated);

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                What is the title of the most similar movie to Lock, Stock & Two Smoking Barrels?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(output.get(0).get("title"));
    }
}
