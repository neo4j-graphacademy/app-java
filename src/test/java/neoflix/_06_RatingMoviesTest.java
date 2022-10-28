package neoflix;

import neoflix.services.RatingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class _06_RatingMoviesTest {
    private static Driver driver;

    private static final String email = "graphacademy.reviewer@neo4j.com";
    private static final String movieId = "680";
    private static final String userId = "1185150b-9e81-46a2-a1d3-eb649544b9c4";
    private static final int rating = 5;

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
    void writeMovieRatingAsInt() {
        RatingService ratingService = new RatingService(driver);

        var output = ratingService.add(userId, movieId, rating);

        assertNotNull(output);
        assertEquals(movieId, output.get("tmdbId"));
        assertEquals(rating, Integer.parseInt(output.get("rating").toString()));
    }
}
