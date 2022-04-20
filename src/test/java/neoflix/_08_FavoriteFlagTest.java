// Task: Rewrite the AuthService to allow users to authenticate against the database
// Outcome: A user will be able to authenticate against their database record
package neoflix;

import neoflix.services.AuthService;
import neoflix.services.FavoriteService;
import neoflix.services.MovieService;
import org.junit.jupiter.api.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import java.util.Map;

import static neoflix.Params.Order.DESC;
import static neoflix.Params.Sort.imdbRating;
import static org.junit.jupiter.api.Assertions.*;

class _08_FavoriteFlagTest {
    private static Driver driver;

    private static String userId;
    private static final String email = "graphacademy.flag@neo4j.com"; // users get an error since this email already exists, might want to remove or change it before this module

    @BeforeAll
    static void initDriver() {
        AppUtils.loadProperties();
        driver = AppUtils.initDriver();
        var user = new AuthService(driver, AppUtils.getJwtSecret()).register(email, "letmein", email);
        userId = (String)user.get("userId");
        if (driver != null) driver.session().writeTransaction(tx -> tx.run("""
                MERGE (u:User {userId: $userId}) SET u.email = $email
                """, Values.parameters("userId", userId, "email", email)));
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) driver.close();
    }

    @BeforeEach
    void setUp() {
        if (driver != null) try (var session = driver.session()) {
            session.writeTransaction(tx ->
                    tx.run("MATCH (u:User {userId: $userId})-[r:HAS_FAVORITE]->(m:Movie) DELETE r",
                            Values.parameters("userId", userId)));
        }
    }

    @Test
    void favoriteMovieReturnsFlaggedInMovieList() {
        MovieService movieService = new MovieService(driver);
        FavoriteService favoriteService = new FavoriteService(driver);

        // Get the most popular movie
        var topMovie = movieService.all(new Params(null, imdbRating, DESC, 1, 0), userId);

        // Add top movie to user favorites
        var topMovieId = topMovie.get(0).get("tmdbId").toString();
        var add = favoriteService.add(userId, topMovieId);
        assertEquals(topMovieId, add.get("tmdbId"));
        assertTrue((Boolean)add.get("favorite"), "top movie is favorite");

        var addCheck = favoriteService.all(userId, new Params(null, imdbRating, Params.Order.ASC, 999, 0));

        assertEquals(1, addCheck.size());
        var found = addCheck.stream().allMatch(movie -> movie.get("tmdbId").equals(topMovieId));
        assertTrue(found);

        var topTwo = movieService.all(new Params(null, imdbRating, DESC, 2, 0), userId);
        assertEquals(topMovieId, topTwo.get(0).get("tmdbId"));

        Assumptions.assumeTrue(topTwo.get(0).get("favorite") != null);
        assertEquals(true, topTwo.get(0).get("favorite"));
        assertEquals(false, topTwo.get(1).get("favorite"));
    }
}
