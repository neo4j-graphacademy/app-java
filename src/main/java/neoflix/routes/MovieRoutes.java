package neoflix.routes;

import io.javalin.apibuilder.EndpointGroup;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.MovieService;
import neoflix.services.RatingService;
import org.neo4j.driver.Driver;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;

public class MovieRoutes implements EndpointGroup {
    private final MovieService movieService;
    private final RatingService ratingService;

    public MovieRoutes(Driver driver) {
        // tag::list[]
        movieService = new MovieService(driver);  // <1>
        // end::list[]
        ratingService = new RatingService(driver);
    }

    @Override
    public void addEndpoints() {
        /*
         * @GET /movies
         *
         * This route should return a paginated list of movies, sorted by the
         * `sort` query parameter,
         */
        // tag::list[]
        get("",  ctx -> {
            var params = Params.parse(ctx, Params.MOVIE_SORT); // <2>
            String userId = AppUtils.getUserId(ctx);  // <3>
            var movies = movieService.all(params, userId);  // <4>
            ctx.json(movies);
        });
        // end::list[]

        /*
         * @GET /movies/{id}
         *
         * This route should find a movie by its tmdbId and return its properties.
         */
        // tag::get[]
        get("/{id}", ctx -> {
            String userId = AppUtils.getUserId(ctx);
            Map<String, Object> movie = movieService.findById(ctx.pathParam("id"), userId);
            ctx.json(movie);
        });

        /*
         * @GET /movies/{id}/ratings
         *
         *
         * This route should return a paginated list of ratings for a movie, ordered by either
         * the rating itself or when the review was created.
         */
        // tag::ratings[]
        get("/{id}/ratings", ctx -> ctx.json(ratingService.forMovie(ctx.pathParam("id"), Params.parse(ctx, Params.RATING_SORT))));
        // end::ratings[]

        /*
         * @GET /movies/{id}/similar
         *
         * This route should return a paginated list of similar movies, ordered by the
         * similarity score in descending order.
         */
        // tag::similar[]
        get("/{id}/similar", ctx -> {
            var userId = AppUtils.getUserId(ctx);
            var movies = movieService.getSimilarMovies(ctx.pathParam("id"), Params.parse(ctx, Params.MOVIE_SORT), userId);
            ctx.json(movies);
        });
        // end::similar[]
    }
}
