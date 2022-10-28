MATCH (m:Movie {title: "Goodfellas"})-[r:RATED]-(u:User)
WITH {
imdbRating: r.rating, timestamp: r.timestamp,
user: u {tmdbId:u.userId, .name}
} AS r
ORDER BY r.timestamp DESC
RETURN collect(r)[0..5]
