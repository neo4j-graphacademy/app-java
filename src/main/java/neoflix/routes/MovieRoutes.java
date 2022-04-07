package neoflix.routes;

import com.google.gson.Gson;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.MovieService;
import neoflix.services.RatingService;
import org.neo4j.driver.Driver;
import spark.RouteGroup;

import java.util.Map;

import static spark.Spark.get;

public class MovieRoutes implements RouteGroup {
    private final Gson gson;
    private final MovieService movieService;
    private final RatingService ratingService;

    public MovieRoutes(Driver driver, Gson gson) {
        this.gson = gson;
        // tag::list[]
        movieService = new MovieService(driver);  // <1>
        // end::list[]
        ratingService = new RatingService(driver);
    }

    @Override
    public void addRoutes() {
        /*
         * @GET /movies
         *
         * This route should return a paginated list of movies, sorted by the
         * `sort` query parameter,
         */
        // tag::list[]
        get("", (req, res) -> {
            var params = Params.parse(req, Params.MOVIE_SORT); // <2>
            String userId = AppUtils.getUserId(req);  // <3>
            return movieService.all(params, userId);  // <4>
        }, gson::toJson);
        // end::list[]

        /*
         * @GET /movies/:id
         *
         * This route should find a movie by its tmdbId and return its properties.
         */
        // tag::get[]
        get("/:id", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            Map<String, Object> movie = movieService.findById(req.params(":id"), userId);
            return movie;
        }, gson::toJson);

        /*
         * @GET /movies/:id/ratings
         *
         *
         * This route should return a paginated list of ratings for a movie, ordered by either
         * the rating itself or when the review was created.
         */
        // tag::ratings[]
        get("/:id/ratings", (req, res) -> ratingService.forMovie(req.params(":id"), Params.parse(req, Params.RATING_SORT)), gson::toJson);
        // end::ratings[]

        /*
         * @GET /movies/:id/similar
         *
         * This route should return a paginated list of similar movies, ordered by the
         * similarity score in descending order.
         */
        // tag::similar[]
        get("/:id/similar", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            return movieService.getSimilarMovies(req.params(":id"), Params.parse(req, Params.MOVIE_SORT), userId);
        }, gson::toJson);
        // end::similar[]
    }

}
