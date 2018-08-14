package tmdb.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tmdb.response.ListMoviesResponse.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * MoviesService fulfills the db operations for MoviesEndpoint.
 */
@Component
public class MoviesService {
    private static final int pageSize = 10;

    private static final String TMDB_ID = "TMDB_ID";
    private static final String IMDB_ID = "IMDB_ID";
    private static final String TITLE = "TITLE";
    private static final String OVERVIEW = "OVERVIEW";
    private static final String TAGLINE = "TAGLINE";
    private static final String LANGUAGE = "LANG";
    private static final String RELEASE_DATE = "RELEASE_DATE";
    private static final String UPVOTES = "UPVOTES";
    private static final String DOWNVOTES = "DOWNVOTES";
    private static final String POPULARITY = "POPULARITY";
    private static final String NUM_MOVIES = "NUM_MOVIES";

    private static final String SELECT_MOVIES_SQL =
            "SELECT TMDB_ID, IMDB_ID, TITLE, OVERVIEW, TAGLINE, LANG, RELEASE_DATE, UPVOTES, DOWNVOTES, " +
                    " (0.3 * (UPVOTES + DOWNVOTES)) + (0.7 * UPVOTES) AS POPULARITY FROM MOVIES " +
                    " ORDER BY RELEASE_DATE DESC, POPULARITY DESC LIMIT ? OFFSET ?";
    private static final String SELECT_NUM_MOVIES_SQL = "SELECT COUNT(*) AS NUM_MOVIES FROM MOVIES";
    private static final String UPDATE_UPVOTES_SQL = "UPDATE MOVIES SET UPVOTES = IFNULL(UPVOTES, 0) + 1 " +
            " WHERE TMDB_ID = ?";
    private static final String UPDATE_DOWNVOTES_SQL = "UPDATE MOVIES SET DOWNVOTES = IFNULL(DOWNVOTES, 0) + 1 " +
            " WHERE TMDB_ID = ?";

    private final JdbcTemplate jdbcTemplate;

    public MoviesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Calculates the offset for the query to the db and queries the db for the list of movies.
     * @param pageNumber The page number in the list of movies to return. Determines the offset for the db.
     * @return List of movies in the db ordered by release_date and popularity and limited by pageSize.
     */
    public List<Movie> getMovies(int pageNumber) {
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(SELECT_MOVIES_SQL, pageSize,
                (pageNumber - 1) * pageSize);
        List<Movie> movies = new ArrayList<>();

        while (resultSet.next()) {
            Movie movie = new Movie(resultSet.getInt(TMDB_ID), resultSet.getString(TITLE));
            movie.setImdbId(resultSet.getString(IMDB_ID));
            movie.setLanguage(resultSet.getString(LANGUAGE));
            movie.setOverview(resultSet.getString(OVERVIEW));
            movie.setTagline(resultSet.getString(TAGLINE));
            movie.setReleaseDate(resultSet.getDate(RELEASE_DATE));
            movie.setUpvotes(resultSet.getInt(UPVOTES));
            movie.setDownvotes(resultSet.getInt(DOWNVOTES));
            movie.setPopularity(resultSet.getFloat(POPULARITY));
            movies.add(movie);
        }

        return movies;
    }

    public int getNumberOfMoviePages() {
        int numMovies = getNumberOfMovies();

        return numMovies % pageSize == 0 ? numMovies / pageSize : (numMovies / pageSize + 1);
    }

    public int getNumberOfMovies() {
        return jdbcTemplate.queryForObject(SELECT_NUM_MOVIES_SQL, Integer.class);
    }

    @Transactional
    public int vote(boolean isUpvote, int tmdbId) {
        return jdbcTemplate.update(isUpvote ? UPDATE_UPVOTES_SQL : UPDATE_DOWNVOTES_SQL, tmdbId);
    }
}
