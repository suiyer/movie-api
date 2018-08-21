# movie-api
A movies API powered by The Movie Database

Setup Instructions
====
* Install Java JDK 8, Maven 3, and MySQL server (version >= 5.6).
* In mysql, create a `root` user with password `mysqlclient`:
```
mysql
> create user 'root'@'localhost' IDENTIFIED BY 'mysqlclient';
```
*  While logged in as `root`, create the `curology` database:
```
mysql -uroot -pmysqlclient
> create database curology;
```
* Clone the project - `git clone https://github.com/suiyer/movie-api.git`
* Build the project using `mvn clean install`
* Run the API server with `java -jar target/movies-rest-service-0.1.0.jar`

Implementation Details
====
* Used Spring Boot 2.0 for the Rest service.
* Used Spring Boot's `DataSource` bean configuration in `application.properties`. This implicitly ensures the `movies` table is created at application startup, and also gives us a free connection pool!
* With `DataSource` in use, we also get to autowire a transactional `jdbcTemplate` in `MoviesService` and `FetchMoviesService`, which greatly simplifies SQL query execution code.

Design
====
* It's a very simple design. 
* I wanted to optimize the app for the List Movies call, so sorting by popularity and pagination were the most important considerations. I used a MySQL database as it's easy to sort the data and paginate using limit and offset in SQL. Adding indexes to release_date, upvotes, and downvotes will make the sorting more performant. 
* While ingesting data from the TMDB API, I only added the columns that seemed most relevant for this exercise (Title, Overview, Tagline, Release Date, Language). 
* The results for List Movies are first sorted by release_date desc (latest first), then by 'Popularity'. Popularity is calculated as (0.7 * upvotes) + (0.3 * (upvotes + downvotes)). If 2 movies have equal upvotes, then the movie with more activity (upvotes or downvotes) overall is deemed to be more popular. This algorithm for determining popularity will rank a movie with 3 upvotes and 5 downvotes as more popular than a movie with 3 upvotes and 0 downvotes.
* The pageNumber specified in the List Movies api call determines the offset for the db call. Currently, the number of results returned are capped at 10 per page. 
* The Rest endpoints defined in the MoviesEndpoint class delegate to the FetchMoviesService and the MoviesService to perform the db operations.
* Any exceptions thrown during the execution are handled by the @ExceptionHandler methods in the MoviesEndpoint class.

API Endpoints
====
* Populate the movies DB by navigating to `localhost:8080/hydrate` (add `?dailyUpdate=1` to the end for incremental updates)
* List a page of movies with `localhost:8080/movies?pageNumber=[page_number]` (or omit the `pageNumber` query parameter to retrieve page 1)
* Upvote a movie with `localhost:8080/movies/{:movieId}/vote`
* Downvote a movie with `localhost:8080/movies/{:movieId}/vote?isUpvote=false`

Future Improvements
====
* Add informative error logging and a more informative error response.
* Make the TMDB API call in FetchMoviesService more robust and graceful. 
  * Handle rate-limiting, timeouts, backoff and retries etc.
  * Add more checks for bad data received from TMDB API.
  * Return a more informative response with number of movies created and number of movies failed.
  * Maybe this endpoint should be a cron job that runs every day.
* Try out different algorithms for ranking movies.
* A user should not be able to upvote a movie multiple times.
* Add ORM for SQL queries.
* Add logging.
