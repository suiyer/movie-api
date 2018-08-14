CREATE TABLE IF NOT EXISTS movies (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tmdb_id INT NOT NULL UNIQUE KEY,
    imdb_id VARCHAR(15),
    lang VARCHAR(10), /* language is a reserved keyword! */
    title VARCHAR(100),
    overview VARCHAR(2500),
    release_date DATE,
    tagline VARCHAR(500),
    upvotes INT,
    downvotes INT);
