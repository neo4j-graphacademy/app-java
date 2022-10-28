package neoflix;

import neoflix.services.PeopleService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import static neoflix.Params.Sort.name;
import static org.junit.jupiter.api.Assertions.*;

class _15_PersonProfileTest {
    private static Driver driver;

    private static final String coppola = "1776";

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
    void findPersonById() {
        PeopleService peopleService = new PeopleService(driver);

        var output = peopleService.findById(coppola);
        assertNotNull(output);
        assertEquals(coppola, output.get("tmdbId"));
        assertEquals("Francis Ford Coppola", output.get("name"));
        assertEquals(16, Integer.parseInt(output.get("directedCount").toString()));
        assertEquals(2, Integer.parseInt(output.get("actedCount").toString()));
    }

    @Test
    void getSimilarPeopleByPersonId() {
        PeopleService peopleService = new PeopleService(driver);

        var limit = 2;

        var output = peopleService.getSimilarPeople(coppola, new Params(null, name, Params.Order.ASC, limit, 0));
        assertNotNull(output);
        assertEquals(limit, output.size());

        var secondOutput = peopleService.getSimilarPeople(coppola, new Params(null, name, Params.Order.ASC, limit, limit));
        assertNotNull(secondOutput);
        assertEquals(limit, secondOutput.size());
        assertNotEquals(output, secondOutput);

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                According to our algorithm, who is the most similar person to Francis Ford Coppola?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(output.get(0).get("name"));
    }
}
