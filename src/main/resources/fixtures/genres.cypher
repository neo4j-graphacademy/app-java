MATCH (g:Genre)<-[:IN_GENRE]-(m:Movie)
WHERE m.imdbRating IS NOT NULL AND m.poster IS NOT NULL AND g.name <> '(no genres listed)'
WITH g, m
ORDER BY m.imdbRating DESC

WITH g, collect(m)[0] AS movie

RETURN g {
  link: '/genres/'+ g.name,
  .name,
  movies: size((g)<-[:IN_GENRE]-()),
  poster: movie.poster
} AS genre
  ORDER BY g.name ASC
