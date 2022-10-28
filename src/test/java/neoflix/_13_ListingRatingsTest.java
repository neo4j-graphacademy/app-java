package neoflix;

import neoflix.services.RatingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import java.util.Map;

import static neoflix.Params.Sort.timestamp;
import static org.junit.jupiter.api.Assertions.*;

class _13_ListingRatingsTest {
    private static Driver driver;

    private static final String pulpFiction = "680";

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
    void getListOfRatings() {
        RatingService ratingService = new RatingService(driver);

        var limit = 2;

        var output = ratingService.forMovie(pulpFiction, new Params(null, timestamp, Params.Order.DESC, limit, 0));
        var paginated = ratingService.forMovie(pulpFiction, new Params(null, timestamp, Params.Order.DESC, limit, limit));

        assertNotNull(output);
        assertEquals(limit, output.size());

        assertNotEquals(output, paginated);
    }

    @Test
    void getOrderedPaginatedMovieRatings() {
        RatingService ratingService = new RatingService(driver);

        var limit = 1;

        var first = ratingService.forMovie(pulpFiction, new Params(null, timestamp, Params.Order.ASC, limit, 0));
        var last = ratingService.forMovie(pulpFiction, new Params(null, timestamp, Params.Order.DESC, limit, 0));

        assertNotEquals(first, last);

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                What is the name of the first person to rate the movie Pulp Fiction?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(((Map)first.get(0).get("user")).get("name"));
    }
}
