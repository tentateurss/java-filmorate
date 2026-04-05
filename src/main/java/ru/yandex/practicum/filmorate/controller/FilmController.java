package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validators.filmvalidators.FilmValidators;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Запрос списка всех фильмов");
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmId(@PathVariable Long filmId) {
        log.info("Запрос на получения фильма по ID");
        Film film = filmService.getFilmById(filmId);
        return film;
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularityFilm(@RequestParam(value = "count", defaultValue = "10") Long count) {
        log.info("Запрос на получения популярных фильмов");
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Начало запроса на добавление фильма {}", film);

        FilmValidators.validateFilm(film, filmStorage);

        Film created = filmStorage.createFilm(film);
        log.debug("Фильм добавлен");
        log.info("Завершение запроса на добавление фильма {}", created);
        return created;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Начало запроса изменение фильма {}", newFilm);

        FilmValidators.validateFilmId(newFilm.getId());

        Film existingFilm = filmStorage.getFilmById(newFilm.getId())
                .orElseThrow(() -> {
                    log.warn("Передан фильм, которого нет в базе");
                    return new NotFoundException("Фильм с ID - " + newFilm.getId() + " не найден");
                });

        FilmValidators.validateFilmForUpdate(newFilm, existingFilm, filmStorage);

        Film updated = filmStorage.updateFilm(existingFilm);
        log.info("Завершение запроса изменения фильма {}", updated);
        return updated;
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable Long filmId, @PathVariable Long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable Long filmId, @PathVariable Long userId) {
        log.info("Пользователь {} убирает лайк с фильма {}", userId, filmId);
        filmService.deleteLike(filmId, userId);
    }
}