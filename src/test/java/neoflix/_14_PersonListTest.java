package neoflix;

import neoflix.services.PeopleService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import static neoflix.Params.Sort.name;
import static org.junit.jupiter.api.Assertions.*;

class _14_PersonListTest {
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
    void getPaginatedPersonList() {
        PeopleService peopleService = new PeopleService(driver);

        var limit = 3;

        var output = peopleService.all(new Params(null, name, Params.Order.ASC, limit, 0));
        assertNotNull(output);
        assertEquals(limit, output.size());
        assertEquals("'Snub' Pollard", output.get(0).get("name"));

        var paginated = peopleService.all(new Params(null, name, Params.Order.ASC, limit, limit));
        assertNotNull(paginated);
        assertEquals(limit, paginated.size());
        assertNotEquals(output, paginated);
        assertEquals("50 Cent", paginated.get(0).get("name"));
    }

    @Test
    void getOrderedPaginatedPersonList() {
        PeopleService peopleService = new PeopleService(driver);

        var q = "A";

        var first = peopleService.all(new Params(q, name, Params.Order.ASC, 1, 0));
        var last = peopleService.all(new Params(q, name, Params.Order.DESC, 1, 0));
        assertNotNull(first);
        assertEquals(1, first.size());
        assertEquals("'Spring' Mark Adley", first.get(0).get("name"));
        assertNotEquals(first, last);
        assertEquals("Álex Angulo", last.get(0).get("name"));
    }

    @Test
    void getOrderedPaginatedQueryForPersons() {
        PeopleService peopleService = new PeopleService(driver);

        var first = peopleService.all(new Params(null, name, Params.Order.ASC, 1, 0));
        assertNotNull(first);
        assertEquals(1, first.size());

        System.out.println("""

                Here is the answer to the quiz question on the lesson:
                What is the name of the first person in the database in alphabetical order?
                Copy and paste the following answer into the text box:
                """);
        System.out.println(first.get(0).get("name").toString().trim());
    }
}
