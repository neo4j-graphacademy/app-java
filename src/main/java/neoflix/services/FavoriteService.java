package neoflix.services;

import neoflix.AppUtils;
import neoflix.Params;
import org.neo4j.driver.Driver;

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
        // TODO: Open a new session
        // TODO: Retrieve a list of movies favorited by the user
        // TODO: Close session

        return AppUtils.process(userFavorites.getOrDefault(userId, List.of()),params);
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
        // TODO: Open a new Session
        // TODO: Create HAS_FAVORITE relationship within a Write Transaction
        // TODO: Close the session
        // TODO: Return movie details and `favorite` property

        var foundMovie = popular.stream().filter(m -> movieId.equals(m.get("tmdbId"))).findAny();

        if (users.stream().anyMatch(u -> u.get("userId").equals(userId)) || foundMovie.isEmpty()) {
            throw new RuntimeException("Couldn't create a favorite relationship for User %s and Movie %s".formatted(userId, movieId));
        }

        var movie = foundMovie.get();
        var favorites = userFavorites.computeIfAbsent(userId, (k) -> new ArrayList<>());
        if (!favorites.contains(movie)) {
            favorites.add(movie);
        }
        var copy = new HashMap<>(movie);
        copy.put("favorite", true);
        return copy;
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
        // TODO: Open a new Session
        // TODO: Delete the HAS_FAVORITE relationship within a Write Transaction
        // TODO: Close the session
        // TODO: Return movie details and `favorite` property
        if (users.stream().anyMatch(u -> u.get("userId").equals(userId))) {
            throw new RuntimeException("Couldn't remove a favorite relationship for User %s and Movie %s".formatted(userId, movieId));
        }

        var movie = popular.stream().filter(m -> movieId.equals(m.get("tmdbId"))).findAny().get();
        var favorites = userFavorites.computeIfAbsent(userId, (k) -> new ArrayList<>());
        if (favorites.contains(movie)) {
            favorites.remove(movie);
        }

        var copy = new HashMap<>(movie);
        copy.put("favorite", false);
        return copy;
    }
    // end::remove[]

}
