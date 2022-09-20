package neoflix.routes;

import io.javalin.apibuilder.EndpointGroup;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.MovieService;
import neoflix.services.PeopleService;
import org.neo4j.driver.Driver;

import static io.javalin.apibuilder.ApiBuilder.get;

public class PeopleRoutes implements EndpointGroup {
    private final PeopleService peopleService;
    private final MovieService movieService;

    public PeopleRoutes(Driver driver) {
        peopleService = new PeopleService(driver);
        movieService = new MovieService(driver);
    }

    @Override
    public void addEndpoints() {
        /*
         * @GET /people/
         *
         * This route should return a paginated list of People from the database
         */
        get("", ctx -> ctx.json(peopleService.all(Params.parse(ctx, Params.PEOPLE_SORT))));

        /*
         * @GET /people/{id}
         *
         * This route should the properties of a Person based on their tmdbId
         */
        get("/{id}", ctx -> ctx.json(peopleService.findById(ctx.pathParam("id"))));

        /*
         * @GET /people/{id}/similar
         *
         * This route should return a paginated list of similar people to the person
         * with the {id} supplied in the route params.
         */
        get("/{id}/similar", ctx -> ctx.json(peopleService.getSimilarPeople(ctx.pathParam("id"), Params.parse(ctx, Params.PEOPLE_SORT))));

        /*
         * @GET /people/{id}/acted
         *
         * This route should return a paginated list of movies that the person
         * with the {id} has acted in.
         */
        get("/{id}/acted", ctx -> {
            var userId = AppUtils.getUserId(ctx);
            var movies = movieService.getForActor(ctx.pathParam("id"), Params.parse(ctx, Params.MOVIE_SORT), userId);
            ctx.json(movies);
        });

        /*
         * @GET /people/{id}/directed
         *
         * This route should return a paginated list of movies that the person
         * with the {id} has directed.
         */
        get("/{id}/directed", ctx -> {
            var userId = AppUtils.getUserId(ctx);
            var movies = movieService.getForDirector(ctx.pathParam("id"), Params.parse(ctx, Params.MOVIE_SORT), userId);
            ctx.json(movies);
        });
    }

}
