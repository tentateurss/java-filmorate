package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    boolean existsById(Long id);

    boolean isDuplicates(Film film);

    long getNextId();

    void deleteFilm(Long filmId);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}
