package neoflix.services;

import neoflix.AppUtils;
import org.neo4j.driver.Driver;

import java.util.List;
import java.util.Map;

public class GenreService {
    private final Driver driver;

    private final List<Map<String,Object>> genres;

    public GenreService(Driver driver) {
        this.driver = driver;
        this.genres = AppUtils.loadFixtureList("genres");
    }

    /**
     * This method should return a list of genres from the database with a
     * `name` property, `movies` which is the count of the incoming `IN_GENRE`
     * relationships and a `poster` property to be used as a background.
     *
     * [
     *   {
     *    name: 'Action',
     *    movies: 1545,
     *    poster: 'https://image.tmdb.org/t/p/w440_and_h660_face/qJ2tW6WMUDux911r6m7haRef0WH.jpg'
     *   }, ...
     *
     * ]
     *
     * @return List<Genre> genres
     */
    // tag::all[]
    public List<Map<String,Object>> all() {
        // Open a new Session, close automatically at the end
        try (var session = driver.session()) {
            // Get a list of Genres from the database
            var query = """
                    MATCH (g:Genre)
                    WHERE g.name <> '(no genres listed)'
                    CALL {
                      WITH g
                      MATCH (g)<-[:IN_GENRE]-(m:Movie)
                      WHERE m.imdbRating IS NOT NULL
                      AND m.poster IS NOT NULL
                      RETURN m.poster AS poster
                      ORDER BY m.imdbRating DESC LIMIT 1
                    }
                    RETURN g {
                      .name,
                      link: '/genres/'+ g.name,
                      poster: poster,
                      movies: size( (g)<-[:IN_GENRE]-() )
                    } as genre
                    ORDER BY g.name ASC
                    """;
            var genres = session.readTransaction(
                    tx -> tx.run(query)
                            .list(row ->
                                row.get("genre").asMap()));

            // Return results
            return genres;
        }
    }
    // end::all[]

    /**
     * This method should find a Genre node by its name and return a set of properties
     * along with a `poster` image and `movies` count.
     *
     * If the genre is not found, a NotFoundError should be thrown.
     *
     * @param name                     The name of the genre
     * @return Genre  The genre information
     */
    // tag::find[]
    public Map<String,Object> find(String name) {
        // TODO: Open a new session
        // TODO: Get Genre information from the database
        // TODO: Throw a 404 Error if the genre is not found
        // TODO: Close the session

        return genres.stream()
                .filter(genre -> genre.get("name").equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Genre "+name+" not found"));
    }
    // end::find[]
}
