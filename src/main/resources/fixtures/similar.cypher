MATCH (:Movie {title: "Goodfellas"})<-[r:RATED]-(u:User)-[r2:RATED]->(n:Movie)

WHERE r.rating > 4.0 AND r2.rating >= r.rating

RETURN n {
  tmdbId:n.imdbId,
  .poster,
  .title,
  .year,
  .languages,
  .plot,
  imdbRating: n.imdbRating,
  genres: [ (n)-[:IN_GENRE]->(g) | g {link: '/genres/'+ g.name, .name}]
} AS movie

, avg(r2.rating) AS rating ORDER BY rating DESC LIMIT 5