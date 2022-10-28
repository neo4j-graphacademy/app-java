package neoflix;

import neoflix.services.MovieService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import static neoflix.Params.Sort.released;
import static neoflix.Params.Sort.title;
import static org.junit.jupiter.api.Assertions.*;

class _11_MovieListTest {
    private static Driver driver;

    private static final String userId = "fe770c6b-4034-4e07-8e40-2f39e7a6722c";
    private static final String email = "graphacademy.movielists@neo4j.com";
    private static final String tomHanks = "31";
    private static final String coppola = "1776";

    @BeforeAll
    static void initDriver() {
        AppUtils.loadProperties();
        driver = AppUtils.initDriver();

        if (driver != null) driver.session().executeWrite(tx -> tx.run("""
                MERGE (u:User {userId: $userId}) SET u.email = $email
                """, Values.parameters("userId", userId, "email", email)));
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) driver.close();
    }

    @Test
    void getPaginatedMoviesByGenre() {
        MovieService movieService = new MovieService(driver);

        var genreName = "Comedy";
        var limit = 3;

        var output = movieService.byGenre(genreName, new Params(null, title, Params.Order.ASC, limit, 0), userId);
        assertNotNull(output);
        assertEquals(limit, output.size());

        var secondOutput = movieService.byGenre(genreName, new Params(null, title, Params.Order.ASC, limit, limit), userId);
        assertNotNull(secondOutput);
        assertEquals(limit, secondOutput.size());

        assertNotEquals(output.get(0).get("title"), secondOutput.get(0).get("title"));

        var reordered = movieService.byGenre(genreName, new Params(null, released, Params.Order.ASC, limit, limit), userId);
        assertNotEquals(output.get(0).get("title"), reordered.get(0).get("title"));
    }

    @Test
    void getPaginatedMoviesByActor() {
        MovieService movieService = new MovieService(driver);

        var limit = 2;

        var output = movieService.getForActor(tomHanks, new Params(null, title, Params.Order.ASC, limit, 0), userId);
        assertNotNull(output);
        assertEquals(limit, output.size());
        assertEquals("'burbs, The", output.get(0).get("title"));

        var secondOutput = movieService.getForActor(tomHanks, new Params(null, title, Params.Order.ASC, limit, limit), userId);
        assertNotNull(secondOutput);
        assertEquals(limit, secondOutput.size());
        assertEquals("Apollo 13", secondOutput.get(0).get("title"));

        assertNotEquals(output.get(0).get("title"), secondOutput.get(0).get("title"));

        var reordered = movieService.getForActor(tomHanks, new Params(null, released, Params.Order.ASC, limit, limit), userId);
        assertNotEquals(output.get(0).get("title"), reordered.get(0).get("title"));
    }

    @Test
    void getPaginatedMoviesByDirector() {
        MovieService movieService = new MovieService(driver);

        var limit = 1;

        var output = movieService.getForDirector(coppola, new Params(null, title, Params.Order.ASC, limit, 0), userId);
        assertNotNull(output);
        assertEquals(limit, output.size());
        assertEquals("Apocalypse Now", output.get(0).get("title"));

        var secondOutput = movieService.getForDirector(coppola, new Params(null, title, Params.Order.ASC, limit, limit), userId);
        assertNotNull(secondOutput);
        assertEquals(limit, secondOutput.size());
        assertEquals("Conversation, The", secondOutput.get(0).get("title"));

        assertNotEquals(output.get(0).get("title"), secondOutput.get(0).get("title"));

        var reordered = movieService.getForDirector(coppola, new Params(null, title, Params.Order.DESC, limit, 0), userId);
        assertNotEquals(output.get(0).get("title"), reordered.get(0).get("title"));
    }

    @Test
    void getMoviesDirectedByCoppola() {
        MovieService movieService = new MovieService(driver);

        var output = movieService.getForDirector(coppola, new Params(null, title, Params.Order.ASC, 30, 0), userId);
        assertNotNull(output);
        assertEquals(16, output.size());
        assertEquals("Apocalypse Now", output.get(0).get("title"));
        assertEquals("Tucker: The Man and His Dream", output.get(15).get("title"));

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                How many films has Francis Ford Coppola directed?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(output.size());
    }
}
