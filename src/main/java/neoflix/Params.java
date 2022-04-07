package neoflix;

import static neoflix.Params.Sort.*;

import spark.Request;

import java.util.EnumSet;

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

    public static Params parse(Request req, EnumSet<Sort> validSort) {
        String q = req.queryParamsSafe("q");
        Sort sort = Sort.of(req.queryParamsSafe("sort"));
        Order order = Order.of(req.queryParamsSafe("order"));
        int limit = Integer.parseInt(req.queryParamOrDefault("limit", "6"));
        int skip = Integer.parseInt(req.queryParamOrDefault("skip", "0"));
        // Only accept valid sort fields
        if (!validSort.contains(sort)) {
            sort = validSort.iterator().next();
        }
        return new Params(q, sort, order, limit, skip);
    }
}
