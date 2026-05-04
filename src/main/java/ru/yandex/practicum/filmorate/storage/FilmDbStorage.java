package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String FIND_ALL =
            "SELECT f.*, m.code AS mpa_name FROM film f " +
                    "LEFT JOIN rating_mpa m ON f.rating_mpa_id = m.rating_mpa_id";

    private static final String FIND_BY_ID =
            "SELECT f.*, m.code AS mpa_name FROM film f " +
                    "LEFT JOIN rating_mpa m ON f.rating_mpa_id = m.rating_mpa_id WHERE f.film_id = ?";

    private static final String INSERT =
            "INSERT INTO film (name, description, release_date, duration, rating_mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rating_mpa_id = ? WHERE film_id = ?";

    private static final String EXISTS_BY_ID = "SELECT COUNT(*) FROM film WHERE film_id = ?";

    private final GenreRowMapper genreRowMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmMapper, GenreRowMapper genreRowMapper) {
        super(jdbc, filmMapper);
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL); // Один запрос для фильмов с MPA

        Map<Long, List<Genre>> genresMap = getAllGenresForFilms();  // Один запрос для всех жанров всех фильмов

        Map<Long, Set<Long>> likesMap = getAllLikesForFilms(); // Один запрос для всех лайков всех фильмов

        // раздаём жанры и лайки по фильмам без доп запросов
        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), List.of()));
            film.setLikes(likesMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID, id);
        filmOpt.ifPresent(film -> {
            film.setGenres(getGenres(film.getId()));
            film.setLikes(getLikes(film.getId()));
        });
        return filmOpt;
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Long genreId : uniqueIds) {
                jdbc.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", id, genreId);
            }
        }

        return getFilmById(id).orElse(film);
    }

    @Override
    public Film updateFilm(Film newFilm) {
        update(UPDATE,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate() != null ? Date.valueOf(newFilm.getReleaseDate()) : null,
                newFilm.getDuration(),
                newFilm.getMpa() != null ? newFilm.getMpa().getId() : null,
                newFilm.getId()
        );

        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", newFilm.getId());
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            Set<Long> uniqueIds = newFilm.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Long genreId : uniqueIds) {
                jdbc.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                        newFilm.getId(), genreId);
            }
        }

        return getFilmById(newFilm.getId()).orElse(newFilm);
    }

    @Override
    public boolean existsById(Long id) {
        return existsById(EXISTS_BY_ID, id);
    }

    @Override
    public boolean isDuplicates(Film film) {
        String sql = "SELECT COUNT(*) FROM film WHERE LOWER(name) = LOWER(?) AND release_date = ? AND film_id != ?";
        Integer count = jdbc.queryForObject(sql, Integer.class,
                film.getName(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getId() != null ? film.getId() : -1
        );
        return count != null && count > 0;
    }

    @Override
    public long getNextId() {
        Long maxId = jdbc.queryForObject("SELECT MAX(film_id) FROM film", Long.class);
        return (maxId != null ? maxId : 0) + 1;
    }

    @Override
    public void deleteFilm(Long filmId) {
        jdbc.update("DELETE FROM film_like WHERE film_id = ?", filmId);
        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", filmId);
        jdbc.update("DELETE FROM film WHERE film_id = ?", filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("INSERT INTO film_like (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM film_like WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    private List<Genre> getGenres(Long filmId) {
        String sql = "SELECT g.genre_id, g.name FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.genre_id";
        return jdbc.query(sql, genreRowMapper, filmId);
    }

    private Set<Long> getLikes(Long filmId) {
        List<Long> userIds = jdbc.queryForList(
                "SELECT user_id FROM film_like WHERE film_id = ?", Long.class, filmId
        );
        return new HashSet<>(userIds);
    }

    private Map<Long, List<Genre>> getAllGenresForFilms() {
        String sql = "SELECT fg.film_id, g.genre_id, g.name FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id ORDER BY g.genre_id";

        return jdbc.query(sql, rs -> {
            Map<Long, List<Genre>> map = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            }
            return map;
        });
    }

    private Map<Long, Set<Long>> getAllLikesForFilms() {
        String sql = "SELECT film_id, user_id FROM film_like";

        return jdbc.query(sql, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Long userId = rs.getLong("user_id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return map;
        });
    }
}