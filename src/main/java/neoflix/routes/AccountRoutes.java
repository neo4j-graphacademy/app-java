package neoflix.routes;

import com.google.gson.Gson;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.FavoriteService;
import neoflix.services.RatingService;
import org.neo4j.driver.Driver;
import spark.RouteGroup;

import static spark.Spark.*;

public class AccountRoutes implements RouteGroup {
    private final Gson gson;
    private final FavoriteService favoriteService;
    private final RatingService ratingService;

    public AccountRoutes(Driver driver, Gson gson) {
        this.gson = gson;
        favoriteService = new FavoriteService(driver);
        ratingService = new RatingService(driver);
    }

    @Override
    public void addRoutes() {
        /*
         * @GET /account/
         *
         * This route simply returns the claims made in the JWT token
         */
        get("", (req, res) -> req.attribute("user"), gson::toJson);

        /*
         * @GET /account/favorites/
         *
         * This route should return a list of movies that a user has added to their
         * Favorites link by clicking the Bookmark icon on a Movie card.
         */
        // tag::list[]
        get("/favorites", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            return favoriteService.all(userId, Params.parse(req, Params.MOVIE_SORT));
        }, gson::toJson);
        // end::list[]

        /*
         * @POST /account/favorites/:id
         *
         * This route should create a `:HAS_FAVORITE` relationship between the current user
         * and the movie with the :id parameter.
         */
        // tag::add[]
        post("/favorites/:id", (req, res) -> {
            String userId = AppUtils.getUserId(req);
            return favoriteService.add(userId, req.params(":id"));
        }, gson::toJson);
        // end::add[]

        /*
         * @DELETE /account/favorites/:id
         *
         * This route should remove the `:HAS_FAVORITE` relationship between the current user
         * and the movie with the :id parameter.
         */
        // tag::delete[]
        delete("/favorites/:id", (req, res) -> {
            String userId = AppUtils.getUserId(req); // TODO
            return favoriteService.remove(userId, req.params(":id"));
        }, gson::toJson);
        // end::delete[]

        /*
         * @POST /account/ratings/:id
         *
         * This route should create a `:RATING` relationship between the current user
         * and the movie with the :id parameter.  The rating value will be posted as part
         * of the post body.
         */
        // tag::rating[]
        post("/ratings/:id", (req, res) -> {
            String userId = AppUtils.getUserId(req); // TODO
            int rating = Integer.parseInt(req.body());
            return ratingService.add(userId, req.params(":id"), rating);
        }, gson::toJson);
        // end::rating[]
    }

}
