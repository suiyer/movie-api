package tmdb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MoviesController {

    private HydrateMoviesService ingester = new HydrateMoviesService();
    private MoviesService service = new MoviesService();

    @RequestMapping("/hydrate")
    public HydrateMoviesResponse hydrate(@RequestParam(value = "dailyUpdate", defaultValue = "false") boolean dailyUpdate)
            throws IOException, SQLException {
        HydrateMoviesResponse response = new HydrateMoviesResponse();
        response.setNumMoviesCreated(ingester.hydrateDB(dailyUpdate));

        return response;
    }

    @RequestMapping("/movies")
    public ListMoviesResponse movies(@RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber)
            throws SQLException {
        ListMoviesResponse response = new ListMoviesResponse();
        response.setMovies(service.getMovies(pageNumber));
        response.setTotalPages(service.getNumberOfMoviePages());
        response.setPageNumber(pageNumber);
        response.setTotalResults(service.getNumberOfMovies());

        return response;
    }

    @RequestMapping("/movies/{movieId}/vote")
    public MovieVoteResponse vote(@PathVariable int movieId, @RequestParam(value = "isUpvote", defaultValue = "true") boolean isUpvote)
            throws SQLException {
        int numMoviesVotedOn = service.vote(isUpvote, movieId);
        if (numMoviesVotedOn < 1) {
            throw new IllegalArgumentException("Movie with ID " + movieId + " does not exist.");
        }

        return new MovieVoteResponse(true);
    }

    /**
     * Handles any IllegalArgumentException thrown by the controller.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(new ApiError(badRequest, ex.getLocalizedMessage()), new HttpHeaders(), badRequest);
    }

    /**
     * Handles all Exceptions other than IllegalArgumentException.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherExceptions(Exception ex) {
        HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(new ApiError(internalServerError, ex.getLocalizedMessage()), internalServerError);
    }

    /**
     * Class that encloses the error to be returned to the user.
     */
    public static class ApiError {

        private HttpStatus status;
        private String message;

        ApiError(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
