MATCH (n:Movie)

WHERE n.released IS NOT NULL and n.poster IS NOT NULL

WITH n {
  .tmdbId,
  .poster,
  .title,
  .year,
  .languages,
  .plot,
  imdbRating: n.imdbRating,
  directors: [ (n)<-[:DIRECTED]-(d) | d { tmdbId:d.imdbId, .name } ],
  actors: [ (n)<-[:ACTED_IN]-(p) | p { tmdbId:p.imdbId, .name } ][0..5],
  genres: [ (n)-[:IN_GENRE]->(g) | g {link: '/genres/'+ g.name, .name}]
}
ORDER BY n.released DESC
LIMIT 6
RETURN collect(n)
