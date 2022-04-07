package neoflix.routes;

import com.google.gson.Gson;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.GenreService;
import neoflix.services.MovieService;
import org.neo4j.driver.Driver;
import spark.RouteGroup;

import static spark.Spark.get;

public class GenreRoutes implements RouteGroup {
    private final Gson gson;
    private final GenreService genreService;
    private final MovieService movieService;

    public GenreRoutes(Driver driver, Gson gson) {
        genreService = new GenreService(driver); // new GenreServiceFixture();
        movieService = new MovieService(driver);
        this.gson = gson;
    }

    @Override
    public void addRoutes() {
        /*
         * @GET /genres/
         *
         * This route should retrieve a full list of Genres from the
         * database along with a poster and movie count.
         */
        get("", (req, res) -> genreService.all(), gson::toJson);

        /*
         * @GET /genres/:name
         *
         * This route should return information on a genre with a name
         * that matches the :name URL parameter.  If the genre is not found,
         * a 404 should be thrown.
         */
        get("/:name", (req, res) -> genreService.find(req.params(":name")), gson::toJson);

        /**
         * @GET /genres/:name/movies
         *
         * This route should return a paginated list of movies that are listed in
         * the genre whose name matches the :name URL parameter.
         */
        get("/:name/movies", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            return movieService.byGenre(req.params(":name"), Params.parse(req, Params.MOVIE_SORT), userId);
        }, gson::toJson);
    }

}
