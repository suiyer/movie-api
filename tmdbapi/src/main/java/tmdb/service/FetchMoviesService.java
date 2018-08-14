package tmdb.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
 * FetchMoviesService is responsible for fetching movies from the TMDB API and storing them in the db.
 */
@Component
public class FetchMoviesService {

    private static final String INSERT_MOVIE_SQL = "INSERT IGNORE INTO MOVIES " +
            "(tmdb_id, imdb_id, title, lang, overview, tagline, release_date)" +
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
    private static final String LANGUAGE = "lang";
    private static final String OVERVIEW = "overview";
    private static final String TAGLINE = "tagline";
    private static final String RELEASE_DATE = "release_date";
    private static final String RESULTS = "results";

    private final JdbcTemplate jdbcTemplate;

    public FetchMoviesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Connects to the TMDB API, parses the JSON response, and creates movies in the db.
     * @param dailyUpdate Whether this is a daily update or weekly update.
     * @return the number of movies created in the db.
     * @throws IOException if there is a problem with the API access URL.
     */
    public int fetchMovies(boolean dailyUpdate) throws IOException {
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

    // This method needs to be public in order for the @Transactional annotation to work correctly
    @Transactional
    public int createMoviesInDB(JsonObject jsonObject) {
        int numMoviesCreated = 0;
        JsonArray jarray = jsonObject.getAsJsonArray(RESULTS);

        System.out.println("Page no:" + jsonObject.get(PAGE).getAsInt());
        System.out.println("Total Results:" + jsonObject.get(TOTAL_RESULTS).getAsInt());
        System.out.println("No. of results on this page: " + jarray.size());

        for (int i = 0; i < jarray.size(); i++) {
            JsonObject jsonMovie = jarray.get(i).getAsJsonObject();
            numMoviesCreated += jdbcTemplate.update(
                    INSERT_MOVIE_SQL,
                    jsonMovie.get(ID).getAsInt(),
                    jsonMovie.get(IMDB_ID) != null ? jsonMovie.get(IMDB_ID).getAsString() : null,
                    jsonMovie.get(TITLE).getAsString(),
                    jsonMovie.get(LANGUAGE) != null ? jsonMovie.get(LANGUAGE).getAsString() : null,
                    jsonMovie.get(OVERVIEW).getAsString(),
                    jsonMovie.get(TAGLINE) != null ? jsonMovie.get(TAGLINE).getAsString() : null,
                    jsonMovie.get(RELEASE_DATE).getAsString());
        }

        return numMoviesCreated;
    }
}
