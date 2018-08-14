package tmdb.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tmdb.db.DatabaseConnectionManager;
import tmdb.response.ListMoviesResponse.Movie;

/**
 * MoviesService fulfills the db operations for MoviesEndpoint.
 */
public class MoviesService {
    private static final DatabaseConnectionManager db = DatabaseConnectionManager.getInstance();
    private static final int pageSize = 10;

    private static final String TMDB_ID = "TMDB_ID";
    private static final String IMDB_ID = "IMDB_ID";
    private static final String TITLE = "TITLE";
    private static final String OVERVIEW = "OVERVIEW";
    private static final String TAGLINE = "TAGLINE";
    private static final String LANGUAGE = "LANGUAGE";
    private static final String RELEASE_DATE = "RELEASE_DATE";
    private static final String UPVOTES = "UPVOTES";
    private static final String DOWNVOTES = "DOWNVOTES";
    private static final String POPULARITY = "POPULARITY";
    private static final String NUM_MOVIES = "NUM_MOVIES";

    private static final String SELECT_MOVIES_SQL =
            "SELECT TMDB_ID, IMDB_ID, TITLE, OVERVIEW, TAGLINE, LANGUAGE, RELEASE_DATE, UPVOTES, DOWNVOTES, " +
                    " (0.5 * (UPVOTES + DOWNVOTES)) + (0.5 * UPVOTES) AS POPULARITY FROM MOVIES " +
                    " ORDER BY RELEASE_DATE DESC, POPULARITY DESC LIMIT ? OFFSET ?";
    private static final String SELECT_NUM_MOVIES_SQL = "SELECT COUNT(*) AS NUM_MOVIES FROM MOVIES";
    private static final String UPDATE_UPVOTES_SQL = "UPDATE MOVIES SET UPVOTES = IFNULL(UPVOTES, 0) + 1 " +
            " WHERE TMDB_ID = ?";
    private static final String UPDATE_DOWNVOTES_SQL = "UPDATE MOVIES SET DOWNVOTES = IFNULL(DOWNVOTES, 0) + 1 " +
            " WHERE TMDB_ID = ?";


    /**
     * Calculates the offset for the query to the db and queries the db for the list of movies.
     * @param pageNumber The page number in the list of movies to return. Determines the offset for the db.
     * @return List of movies in the db ordered by release_date and popularity and limited by pageSize.
     * @throws java.sql.SQLException if there is a problem with the db connection.
     */
    public List<Movie> getMovies(int pageNumber) throws java.sql.SQLException {
        List<Movie> movies = new ArrayList<>();
        try (Connection connection = db.getConnection();
            PreparedStatement statement = connection.prepareStatement(SELECT_MOVIES_SQL)){
            statement.setInt(1, pageSize);
            statement.setInt(2, pageNumber * pageSize);
            ResultSet resultSet = statement.executeQuery();
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
        }

        return movies;
    }

    public int getNumberOfMoviePages() throws java.sql.SQLException {
        int numMovies = getNumberOfMovies();

        return numMovies % pageSize == 0 ? numMovies / pageSize : (numMovies / pageSize + 1);
    }

    public int getNumberOfMovies() throws SQLException {
        int numMovies = 0;
        try (Connection connection = db.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_NUM_MOVIES_SQL)) {
           ResultSet resultSet = statement.executeQuery();
           while (resultSet.next()) {
               numMovies = resultSet.getInt(NUM_MOVIES);
           }
        }
        return numMovies;
    }

    public int vote(boolean isUpvote, int tmdbId) throws SQLException {
        try (Connection connection = db.getConnection()) {
            PreparedStatement statement;
            if (isUpvote) {
                statement = connection.prepareStatement(UPDATE_UPVOTES_SQL);
            } else {
                statement = connection.prepareStatement(UPDATE_DOWNVOTES_SQL);
            }
            statement.setInt(1, tmdbId);
            statement.executeUpdate();
            return statement.getUpdateCount();
        }
    }
}
