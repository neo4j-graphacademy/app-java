package neoflix.routes;

import io.javalin.apibuilder.EndpointGroup;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.GenreService;
import neoflix.services.MovieService;
import org.neo4j.driver.Driver;

import static io.javalin.apibuilder.ApiBuilder.get;

public class GenreRoutes implements EndpointGroup {
    private final GenreService genreService;
    private final MovieService movieService;

    public GenreRoutes(Driver driver) {
        genreService = new GenreService(driver); // new GenreServiceFixture();
        movieService = new MovieService(driver);
    }

    @Override
    public void addEndpoints() {
        /*
         * @GET /genres/
         *
         * This route should retrieve a full list of Genres from the
         * database along with a poster and movie count.
         */
        get("", ctx -> ctx.json(genreService.all()));

        /*
         * @GET /genres/{name}
         *
         * This route should return information on a genre with a name
         * that matches the {name} URL parameter.  If the genre is not found,
         * a 404 should be thrown.
         */
        get("/{name}", ctx -> ctx.json(genreService.find(ctx.pathParam("name"))));

        /**
         * @GET /genres/{name}/movies
         *
         * This route should return a paginated list of movies that are listed in
         * the genre whose name matches the {name} URL parameter.
         */
        get("/{name}/movies", ctx -> {
            var userId = AppUtils.getUserId(ctx);
            var movies = movieService.byGenre(ctx.pathParam("name"), Params.parse(ctx, Params.MOVIE_SORT), userId);
            ctx.json(movies);
        });
    }

}
