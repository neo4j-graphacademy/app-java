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
        // Open a new database session
        try (var session = driver.session()) {
            // Get a list of people from the database
            var res = session.executeRead(tx -> {
                String statement = String.format("""
                        MATCH (p:Person)
                        WHERE $q IS null OR p.name CONTAINS $q
                        RETURN p { .* } AS person
                        ORDER BY p.`%s` %s
                        SKIP $skip
                        LIMIT $limit
                        """, params.sort(Params.Sort.name), params.order());
                return tx.run(statement
                            , Values.parameters("q", params.query(), "skip", params.skip(), "limit", params.limit()))
                        .list(row -> row.get("person").asMap());
            });

            return res;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return List.of();
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
        // Open a new database session
        try (var session = driver.session()) {

            // Get a person from the database
            var person = session.executeRead(tx -> {
                        String query = """
                                    MATCH (p:Person {tmdbId: $id})
                                    RETURN p {
                                        .*,
                                        actedCount: size((p)-[:ACTED_IN]->()),
                                        directedCount: size((p)-[:DIRECTED]->())
                                    } AS person
                                """;
                        var res = tx.run(query, Values.parameters("id", id));
                        return res.single().get("person").asMap();
                    });
            return person;
        }
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
        // Open a new database session
        try (var session = driver.session()) {

            // Get a list of similar people to the person by their id
            var res = session.executeRead(tx -> tx.run("""
                    MATCH (:Person {tmdbId: $id})-[:ACTED_IN|DIRECTED]->(m)<-[r:ACTED_IN|DIRECTED]-(p)
                    RETURN p {
                        .*,
                        actedCount: size((p)-[:ACTED_IN]->()),
                        directedCount: size((p)-[:DIRECTED]->()),
                        inCommon: collect(m {.tmdbId, .title, type: type(r)})
                    } AS person
                    ORDER BY size(person.inCommon) DESC
                    SKIP $skip
                    LIMIT $limit
                    """,Values.parameters("id",id, "skip", params.skip(), "limit", params.limit()))
                    .list(row -> row.get("person").asMap()));

            return res;
        }
    }
    // end::getSimilarPeople[]

}