package tmdb.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmdb.response.FetchMoviesResponse;
import tmdb.response.ListMoviesResponse;
import tmdb.response.MovieVoteResponse;
import tmdb.service.FetchMoviesService;
import tmdb.service.MoviesService;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class MoviesEndpoint {
    @Autowired
    private FetchMoviesService fetchService;
    @Autowired
    private MoviesService service;

    @RequestMapping("/hydrate")
    public FetchMoviesResponse fetchMovies(@RequestParam(value = "dailyUpdate", defaultValue = "false") boolean dailyUpdate)
            throws IOException, SQLException {
        FetchMoviesResponse response = new FetchMoviesResponse();
        response.setNumMoviesCreated(fetchService.fetchMovies(dailyUpdate));

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
