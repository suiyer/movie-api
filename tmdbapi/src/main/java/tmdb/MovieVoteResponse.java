package tmdb;

public class MovieVoteResponse {

    private boolean movieVotedSuccessfully;

    public MovieVoteResponse(boolean movieVotedSuccessfully) {
        this.movieVotedSuccessfully = movieVotedSuccessfully;
    }

    public boolean isMovieVotedSuccessfully() {
        return movieVotedSuccessfully;
    }

    public void setMovieVotedSuccessfully(boolean movieVotedSuccessfully) {
        this.movieVotedSuccessfully = movieVotedSuccessfully;
    }
}
