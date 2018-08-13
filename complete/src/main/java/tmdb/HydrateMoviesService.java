package tmdb;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
 * HydrateMoviesService is responsible for fetching movies from the TMDB API and storing them in the db.
 */
public class HydrateMoviesService {

    private static final String INSERT_MOVIE_SQL = "INSERT IGNORE INTO MOVIES " +
            "(tmdb_id, imdb_id, title, language, overview, tagline, release_date)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String URL_FORMAT = "https://api.themoviedb.org/3/discover/movie?" +
            "api_key=%s&language=en-US&include_adult=false&include_video=false" +
            "&primary_release_date.gte=%s&primary_release_date.lte=%s&page=%s";
    private static final String API_KEY = "403ffcb3b4481da342203f94fb6e937e";

    private static final String ID = "id";
    private static final String PAGE = "page";
    private static final String TOTAL_RESULTS = "total_results";
    private static final String TOTAL_PAGES = "total_pages";
    private static final String IMDB_ID = "imdb_id";
    private static final String TITLE = "title";
    private static final String LANGUAGE = "language";
    private static final String OVERVIEW = "overview";
    private static final String TAGLINE = "tagline";
    private static final String RELEASE_DATE = "release_date";
    private static final String RESULTS = "results";

    private DatabaseConnectionManager db = DatabaseConnectionManager.getInstance();

    /**
     * Connects to the TMDB API, parses the JSON response, and creates movies in the db.
     * @param dailyUpdate Whether this is a daily update or weekly update.
     * @return the number of movies created in the db.
     * @throws SQLException if there are problems with the db connection.
     * @throws IOException if there is a problem with the API access URL.
     */
    public int hydrateDB(boolean dailyUpdate) throws SQLException, IOException {
        LocalDate today = LocalDate.now();
        int numDaysToUpdate = 7;
        if (dailyUpdate) {
            numDaysToUpdate = 1;
        }
        LocalDate dateToUpdateFrom = today.minusDays(numDaysToUpdate);
        int pageNumber = 1;
        int totalPages = 1;
        int numMoviesCreated = 0;

        do {
            URL tmdb = new URL(String.format(URL_FORMAT, API_KEY,
                    dateToUpdateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    pageNumber++));
            URLConnection yc = tmdb.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JsonObject jsonObject = new JsonParser().parse(inputLine).getAsJsonObject();
                totalPages = jsonObject.get(TOTAL_PAGES).getAsInt();
                numMoviesCreated += createMoviesInDB(jsonObject);
            }
            in.close();
        } while (pageNumber <= totalPages);

        return numMoviesCreated;
    }

    private int createMoviesInDB(JsonObject jsonObject) throws SQLException {
        int numMoviesCreated = 0;
        System.out.println("Page no:" + jsonObject.get(PAGE).getAsInt());
        System.out.println("Total Results:" + jsonObject.get(TOTAL_RESULTS).getAsInt());
        JsonArray jarray = jsonObject.getAsJsonArray(RESULTS);
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_MOVIE_SQL)) {

            for (int i = 0; i < jarray.size(); i++) {
                JsonObject jsonMovie = jarray.get(i).getAsJsonObject();

                statement.setInt(1, jsonMovie.get(ID).getAsInt());
                statement.setString(2,
                        jsonMovie.get(IMDB_ID) != null ? jsonMovie.get(IMDB_ID).getAsString() : null);
                statement.setString(3, jsonMovie.get(TITLE).getAsString());
                statement.setString(4,
                        jsonMovie.get(LANGUAGE) != null ? jsonMovie.get(LANGUAGE).getAsString() : null);
                statement.setString(5, jsonMovie.get(OVERVIEW).getAsString());
                statement.setString(6,
                        jsonMovie.get(TAGLINE) != null ? jsonMovie.get(TAGLINE).getAsString() : null);
                statement.setString(7, jsonMovie.get(RELEASE_DATE).getAsString());

                statement.executeUpdate();
                numMoviesCreated += statement.getUpdateCount();
            }
        }

        return numMoviesCreated;
    }
}
