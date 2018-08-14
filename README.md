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

API Endpoints
====
* Populate the movies DB by navigating to `localhost:8080/hydrate` (add `?dailyUpdate=1` to the end for incremental updates)
* List a page of movies with `localhost:8080/movies?pageNumber=[page_number]` (or omit the `pageNumber` query parameter to retrieve page 1)
* Upvote a movie with `localhost:8080/movies/{:movieId}/vote`
* Downvote a movie with `localhost:8080/movies/{:movieId}/vote?isUpvote=false`

Future Improvements
====
* Make the TMDB API call in FetchMoviesService more robust and graceful. 
  ** Handle rate-limiting, timeouts, backoff and retries etc.
  ** Add more checks for bad data received from TMDB API.
  ** Return a more informative response with number of movies created and number of movies failed.
  ** Maybe this endpoint should be a cron job that runs every day.
* Try out different algorithms for ranking movies.
* A user should not be able to upvote a movie multiple times.
* Add ORM for SQL queries.
* Add logging.
