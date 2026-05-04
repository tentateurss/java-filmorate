package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.validators.filmvalidators.FilmValidators;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       MpaDbStorage mpaDbStorage,
                       GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: {}", film);
        FilmValidators.validateFilm(film);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaDbStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с ID " + film.getMpa().getId() + " не найден"));
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreDbStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с ID " + genre.getId() + " не найден"));
            }
        }

        Film created = filmStorage.createFilm(film);
        return created;
    }

    public Film updateFilm(Film newFilm) {
        log.info("Обновление фильма: {}", newFilm);

        FilmValidators.validateFilmId(newFilm.getId());

        Film existingFilm = filmStorage.getFilmById(newFilm.getId())
                .orElseThrow(() -> {
                    log.warn("Фильм с ID {} не найден", newFilm.getId());
                    return new NotFoundException("Фильм с ID - " + newFilm.getId() + " не найден");
                });

        FilmValidators.validateFilmForUpdate(newFilm);

        if (newFilm.getMpa() != null && newFilm.getMpa().getId() != null) {
            mpaDbStorage.findById(newFilm.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с ID " + newFilm.getMpa().getId() + " не найден"));
            existingFilm.setMpa(newFilm.getMpa());
        }

        if (newFilm.getGenres() != null) {
            for (Genre genre : newFilm.getGenres()) {
                genreDbStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с ID " + genre.getId() + " не найден"));
            }
            existingFilm.setGenres(newFilm.getGenres());
        }

        if (newFilm.getName() != null && !newFilm.getName().equalsIgnoreCase(existingFilm.getName())) {
            existingFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            existingFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null && !newFilm.getReleaseDate().equals(existingFilm.getReleaseDate())) {
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            existingFilm.setDuration(newFilm.getDuration());
        }

        Film updated = filmStorage.updateFilm(existingFilm);
        log.info("Фильм обновлен: {}", updated);
        return updated;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        if (film.getLikes() != null && film.getLikes().contains(userId)) {
            throw new DuplicatedDataException("Пользователь уже поставил лайк этому фильму");
        }

        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        if (film.getLikes() == null || !film.getLikes().contains(userId)) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        if (count == null) {
            count = 10L;
        }

        if (count <= 0) {
            return List.of();
        }

        long finalCount = count;
        log.info("Запрос топ - {} популярных фильмов", finalCount);

        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(finalCount)
                .collect(Collectors.toList());
    }

    public void deleteFilm(Long filmId) {
        getFilmById(filmId);
        filmStorage.deleteFilm(filmId);
        log.info("Фильм с ID {} удалён", filmId);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID - " + id + " не найден"));
    }
}