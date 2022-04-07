package neoflix.routes;

import com.google.gson.Gson;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.MovieService;
import neoflix.services.PeopleService;
import org.neo4j.driver.Driver;
import spark.RouteGroup;

import static spark.Spark.get;

public class PeopleRoutes implements RouteGroup {
    private final Gson gson;
    private final PeopleService peopleService;
    private final MovieService movieService;

    public PeopleRoutes(Driver driver, Gson gson) {
        this.gson = gson;
        peopleService = new PeopleService(driver);
        movieService = new MovieService(driver);
    }

    @Override
    public void addRoutes() {
        /*
         * @GET /people/
         *
         * This route should return a paginated list of People from the database
         */
        get("", (req, res) -> peopleService.all(Params.parse(req, Params.PEOPLE_SORT)), gson::toJson);

        /*
         * @GET /people/:id
         *
         * This route should the properties of a Person based on their tmdbId
         */
        get("/:id", (req, res) -> peopleService.findById(req.params(":id")), gson::toJson);

        /*
         * @GET /people/:id/similar
         *
         * This route should return a paginated list of similar people to the person
         * with the :id supplied in the route params.
         */
        get("/:id/similar", (req, res) -> peopleService.getSimilarPeople(req.params(":id"), Params.parse(req, Params.PEOPLE_SORT)), gson::toJson);

        /*
         * @GET /people/:id/acted
         *
         * This route should return a paginated list of movies that the person
         * with the :id has acted in.
         */
        get("/:id/acted", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            return movieService.getForActor(req.params(":id"), Params.parse(req, Params.MOVIE_SORT), userId);
        }, gson::toJson);

        /*
         * @GET /people/:id/directed
         *
         * This route should return a paginated list of movies that the person
         * with the :id has acted in.
         */
        get("/:id/directed", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            return movieService.getForDirector(req.params(":id"), Params.parse(req, Params.MOVIE_SORT), userId);
        }, gson::toJson);
    }

}
