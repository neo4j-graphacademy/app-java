package neoflix.services;

import neoflix.AppUtils;
import neoflix.Params;
import neoflix.ValidationException;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.NoSuchRecordException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteService {
    private final Driver driver;

    private final List<Map<String,Object>> popular;
    private final List<Map<String,Object>> users;
    private final Map<String,List<Map<String,Object>>> userFavorites = new HashMap<>();

    /**
     * The constructor expects an instance of the Neo4j Driver, which will be
     * used to interact with Neo4j.
     *
     * @param driver
     */
    public FavoriteService(Driver driver) {
        this.driver = driver;
        this.popular = AppUtils.loadFixtureList("popular");
        this.users = AppUtils.loadFixtureList("users");
    }

    /**
     * This method should retrieve a list of movies that have an incoming :HAS_FAVORITE
     * relationship from a User node with the supplied `userId`.
     *
     * Results should be ordered by the `sort` parameter, and in the direction specified
     * in the `order` parameter.
     * Results should be limited to the number passed as `limit`.
     * The `skip` variable should be used to skip a certain number of rows.
     *
     * @param userId  The unique ID of the user
     * @param params Query params for pagination and sorting
     * @return List<Movie> An list of Movie objects
     */
    // tag::all[]
    public List<Map<String, Object>> all(String userId, Params params) {
        // Open a new session
        try (var session = this.driver.session()) {
            // Retrieve a list of movies favorited by the user
            var favorites = session.executeRead(tx -> {
                String query = String.format("""
                            MATCH (u:User {userId: $userId})-[r:HAS_FAVORITE]->(m:Movie)
                            RETURN m {
                            .*,
                                favorite: true
                            } AS movie
                            ORDER BY m.`%s` %s
                            SKIP $skip
                            LIMIT $limit
                        """, params.sort(Params.Sort.title), params.order());
                var res = tx.run(query, Values.parameters("userId", userId, "skip", params.skip(), "limit", params.limit()));
                return res.list(row -> row.get("movie").asMap());
            });
            return favorites;
        }
    }
    // end::all[]

    /**
     * This method should create a `:HAS_FAVORITE` relationship between
     * the User and Movie ID nodes provided.
     *
     * If either the user or movie cannot be found, a `NotFoundError` should be thrown.
     *
     * @param userId The unique ID for the User node
     * @param movieId The unique tmdbId for the Movie node
     * @return Map<String,Object></String,Object> The updated movie record with `favorite` set to true
     */
    // tag::add[]
    public Map<String,Object> add(String userId, String movieId) {
        // Open a new Session
        try (var session = this.driver.session()) {
            // tag::create[]
            // Create HAS_FAVORITE relationship within a Write Transaction
            var favorite = session.executeWrite(tx -> {
                String statement = """
                            MATCH (u:User {userId: $userId})
                            MATCH (m:Movie {tmdbId: $movieId})

                            MERGE (u)-[r:HAS_FAVORITE]->(m)
                                    ON CREATE SET r.createdAt = datetime()

                            RETURN m {
                                .*,
                                favorite: true
                            } AS movie
                        """;
                var res = tx.run(statement, Values.parameters("userId", userId, "movieId", movieId));
                // return res.single().get("movie").asMap();
                return res.single().get("movie").asMap();
            });
            // end::create[]

            // tag::return[]
            // Return movie details and `favorite` property
            return favorite;
            // end::return[]
        // tag::throw[]
        // Throw an error if the user or movie could not be found
        } catch (NoSuchRecordException e) {
            throw new ValidationException(String.format("Couldn't create a favorite relationship for User %s and Movie %s", userId, movieId), Map.of("movie",movieId, "user",userId));
        }
        // end::throw[]
    }
    // end::add[]

    /*
     *This method should remove the `:HAS_FAVORITE` relationship between
     * the User and Movie ID nodes provided.
     * If either the user, movie or the relationship between them cannot be found,
     * a `NotFoundError` should be thrown.

     * @param userId The unique ID for the User node
     * @param movieId The unique tmdbId for the Movie node
     * @return Map<String,Object></String,Object> The updated movie record with `favorite` set to true
     */
    // tag::remove[]
    public Map<String,Object> remove(String userId, String movieId) {
        // Open a new Session
        try (var session = this.driver.session()) {
            // tag::remove[]
            // Removes HAS_FAVORITE relationship within a Write Transaction
            var favorite = session.executeWrite(tx -> {
                String statement = """
                          MATCH (u:User {userId: $userId})-[r:HAS_FAVORITE]->(m:Movie {tmdbId: $movieId})
                          DELETE r

                          RETURN m {
                            .*,
                            favorite: false
                          } AS movie
                        """;
                var res = tx.run(statement, Values.parameters("userId", userId, "movieId", movieId));
                return res.single().get("movie").asMap();
            });
            // end::remove[]

            // tag::return-remove[]
            // Return movie details and `favorite` property
            return favorite;
            // end::return-remove[]
        // tag::throw-remove[]
        // Throw an error if the user or movie could not be found
        } catch (NoSuchRecordException e) {
            throw new ValidationException("Could not remove favorite movie for user", Map.of("movie",movieId, "user",userId));
        }
        // end::throw-remove[]
    }
    // end::remove[]

}
