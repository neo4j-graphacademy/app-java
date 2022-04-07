package neoflix.services;

import neoflix.AppUtils;
import neoflix.AuthUtils;
import neoflix.Params;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PeopleService {
    private final Driver driver;
    private final List<Map<String,Object>> people;

    /**
     * The constructor expects an instance of the Neo4j Driver, which will be
     * used to interact with Neo4j.
     *
     * @param driver
     */
    public PeopleService(Driver driver) {
        this.driver = driver;
        this.people = AppUtils.loadFixtureList("people");
    }

    /**
     * This method should return a paginated list of People (actors or directors),
     * with an optional filter on the person's name based on the `q` parameter.
     *
     * Results should be ordered by the `sort` parameter and limited to the
     * number passed as `limit`.  The `skip` variable should be used to skip a
     * certain number of rows.
     *
     * @param params        Used to filter on the person's name, and query parameters for pagination and ordering
     * @return List<Person>
     */
    // tag::all[]
    public List<Map<String,Object>> all(Params params) {
        // TODO: Get a list of people from the database
        if (params.query() != null) {
            return AppUtils.process(people.stream()
                    .filter(p -> ((String)p.get("name"))
                            .contains(params.query()))
                    .toList(), params);
        }
        return AppUtils.process(people, params);
    }
    // end::all[]

    /**
     * Find a user by their ID.
     *
     * If no user is found, a NotFoundError should be thrown.
     *
     * @param id   The tmdbId for the user
     * @return Person
     */
    // tag::findById[]
    public Map<String, Object> findById(String id) {
        // TODO: Find a user by their ID

        return people.stream().filter(p -> id.equals(p.get("tmdbId"))).findAny().get();
    }
    // end::findById[]

    /**
     * Get a list of similar people to a Person, ordered by their similarity score
     * in descending order.
     *
     * @param id     The ID of the user
     * @param params Query parameters for pagination and ordering
     * @return List<Person> similar people
     */
    // tag::getSimilarPeople[]
    public List<Map<String,Object>> getSimilarPeople(String id, Params params) {
        // TODO: Get a list of similar people to the person by their id

        return AppUtils.process(people, params);
    }
    // end::getSimilarPeople[]

}