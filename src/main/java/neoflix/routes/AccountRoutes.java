package neoflix.routes;

import com.google.gson.Gson;

import io.javalin.apibuilder.EndpointGroup;
import neoflix.Params;
import neoflix.AppUtils;
import neoflix.services.FavoriteService;
import neoflix.services.RatingService;
import org.neo4j.driver.Driver;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AccountRoutes implements EndpointGroup {
    private final Gson gson;
    private final FavoriteService favoriteService;
    private final RatingService ratingService;

    public AccountRoutes(Driver driver, Gson gson) {
        this.gson = gson;
        favoriteService = new FavoriteService(driver);
        ratingService = new RatingService(driver);
    }

    @Override
    public void addEndpoints() {
        /*
         * @GET /account/
         *
         * This route simply returns the claims made in the JWT token
         */
        get("", ctx -> ctx.result(gson.toJson(ctx.attribute("user"))));

        /*
         * @GET /account/favorites/
         *
         * This route should return a list of movies that a user has added to their
         * Favorites link by clicking the Bookmark icon on a Movie card.
         */
        // tag::list[]
        get("/favorites", ctx -> {
            var userId = AppUtils.getUserId(ctx);
            var favorites = favoriteService.all(userId, Params.parse(ctx, Params.MOVIE_SORT));
            ctx.result(gson.toJson(favorites));
        });
        // end::list[]

        /*
         * @POST /account/favorites/{id}
         *
         * This route should create a `:HAS_FAVORITE` relationship between the current user
         * and the movie with the {id} parameter.
         */
        // tag::add[]
        post("/favorites/{id}", ctx -> {
            var userId = AppUtils.getUserId(ctx);
            var newFavorite = favoriteService.add(userId, ctx.pathParam("id"));
            ctx.result(gson.toJson(newFavorite));
        });
        // end::add[]

        /*
         * @DELETE /account/favorites/{id}
         *
         * This route should remove the `:HAS_FAVORITE` relationship between the current user
         * and the movie with the {id} parameter.
         */
        // tag::delete[]
        delete("/favorites/{id}", ctx -> {
            var userId = AppUtils.getUserId(ctx); // TODO
            var deletedFavorite = favoriteService.remove(userId, ctx.pathParam("id"));
            ctx.result(gson.toJson(deletedFavorite));
        });
        // end::delete[]

        /*
         * @POST /account/ratings/{id}
         *
         * This route should create a `:RATING` relationship between the current user
         * and the movie with the {id} parameter.  The rating value will be posted as part
         * of the post body {"rating": "5"}.
         */
        // tag::rating[]
        post("/ratings/{id}", ctx -> {
            var userId = AppUtils.getUserId(ctx); // TODO
            var value = Integer.parseInt(gson.fromJson(ctx.body(), Map.class).get("rating").toString());
            var rating = ratingService.add(userId, ctx.pathParam("id"), value);
            ctx.result(gson.toJson(rating));
        });
        // end::rating[]
    }

}
