package neoflix;

import static neoflix.Params.Sort.*;

import io.javalin.http.Context;

import java.util.EnumSet;
import java.util.Optional;

public record Params(String query, Sort sort, Order order, int limit, int skip) {
    public Sort sort(Sort defaultSort) {
        return sort == null ? defaultSort : sort;
    }

    public enum Order {
        ASC, DESC;

        static Order of(String value) {
            if (value == null || value.isBlank() || !"DESC".equalsIgnoreCase(value)) return ASC;
            return DESC;
        }
    }

    public enum Sort { /* Movie */
        title, released, imdbRating, score,
        /* Person */ name, born, movieCount,
        /* */ rating, timestamp;

        static Sort of(String name) {
            if (name == null || name.isBlank()) return null;
            return Sort.valueOf(name);
        }
    }

    public static final EnumSet<Sort> MOVIE_SORT = EnumSet.of(title, released, imdbRating, score);
    public static final EnumSet<Sort> PEOPLE_SORT = EnumSet.of(name, born, movieCount);
    public static final EnumSet<Sort> RATING_SORT = EnumSet.of(rating, timestamp);

    public static Params parse(Context ctx, EnumSet<Sort> validSort) {
        String q = ctx.queryParam("q");
        Sort sort = Sort.of(ctx.queryParam("sort"));
        Order order = Order.of(ctx.queryParam("order"));
        int limit = Integer.parseInt(Optional.ofNullable(ctx.queryParam("limit")).orElse("6"));
        int skip = Integer.parseInt(Optional.ofNullable(ctx.queryParam("skip")).orElse("0"));
        // Only accept valid sort fields
        if (!validSort.contains(sort)) {
            sort = validSort.iterator().next();
        }
        return new Params(q, sort, order, limit, skip);
    }
}
