CREATE TABLE IF NOT EXISTS rating_mpa (
    rating_mpa_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS "user" (
    user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(256) NOT NULL UNIQUE,
    login VARCHAR(256) NOT NULL UNIQUE,
    name VARCHAR(256),
    birthday DATE
    );

CREATE TABLE IF NOT EXISTS film (
    film_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INTEGER,
    rating_mpa_id INTEGER,
    FOREIGN KEY (rating_mpa_id) REFERENCES rating_mpa(rating_mpa_id)
    );

CREATE TABLE IF NOT EXISTS film_genre (
    film_id INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
    );

CREATE TABLE IF NOT EXISTS film_like (
    film_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id),
    FOREIGN KEY (user_id) REFERENCES "user"(user_id)
    );

CREATE TABLE IF NOT EXISTS friendship (
    user_id INTEGER NOT NULL,
    friend_id INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'unconfirmed',
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    FOREIGN KEY (friend_id) REFERENCES "user"(user_id)
    );