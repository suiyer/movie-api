package tmdb;

import java.util.Date;
import java.util.List;

/**
 * ListMoviesResponse is the response class for the listMovies API method.
 */
public class ListMoviesResponse {
    private int pageNumber;
    private int totalPages;
    private int totalResults;
    private List<Movie> movies;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public static class Movie {
        private int tmdbId;
        private String imdbId;
        private String title;
        private String overview;
        private String tagline;
        private Date releaseDate;
        private String language;
        private int upvotes;
        private int downvotes;
        private float popularity;

        public Movie(int tmdbId, String title) {
            this.tmdbId = tmdbId;
            this.title = title;
        }

        public void setTmdbId(int tmdbId) {
            this.tmdbId = tmdbId;
        }

        public int getTmdbId() {
            return tmdbId;
        }

        public String getImdbId() {
            return imdbId;
        }

        public void setImdbId(String imdbId) {
            this.imdbId = imdbId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getOverview() {
            return overview;
        }

        public void setOverview(String overview) {
            this.overview = overview;
        }

        public String getTagline() {
            return tagline;
        }

        public void setTagline(String tagline) {
            this.tagline = tagline;
        }

        public Date getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(Date releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getUpvotes() {
            return upvotes;
        }

        public void setUpvotes(int upvotes) {
            this.upvotes = upvotes;
        }

        public int getDownvotes() {
            return downvotes;
        }

        public void setDownvotes(int downvotes) {
            this.downvotes = downvotes;
        }

        public float getPopularity() {
            return popularity;
        }

        public void setPopularity(float popularity) {
            this.popularity = popularity;
        }
    }
}
