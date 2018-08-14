package tmdb.response;

/**
 * FetchMoviesResponse is the response class for the fetchMovies API method.
 */
public class FetchMoviesResponse {
    private int numMoviesCreated;
    private int numMoviesFailed;

    public int getNumMoviesCreated() {
        return numMoviesCreated;
    }

    public void setNumMoviesCreated(int numMoviesCreated) {
        this.numMoviesCreated = numMoviesCreated;
    }

    public int getNumMoviesFailed() {
        return numMoviesFailed;
    }

    public void setNumMoviesFailed(int numMoviesFailed) {
        this.numMoviesFailed = numMoviesFailed;
    }
}
