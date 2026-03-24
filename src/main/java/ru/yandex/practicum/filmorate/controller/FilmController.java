package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Запрос списка всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Начало запроса на добавление фильма {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма не прошло валидацию");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Описание фильма не прошло валидацию");
            throw new ValidationException("Длина описания не может быть длинне 200 символов");
        }

        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(earliestDate)) {
            log.warn("Дата релиза фильма не прошла валидацию");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 г.");
        }


        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Продолжительность фильма не прошла валидацию");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        film.setId(getNextId());
        log.debug("Фильму установлен ID {}", film.getId());
        films.put(film.getId(), film);
        log.debug("Фильм добавлен");

        log.info("Завершение запроса на добавление фильма {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Начало запроса изменение фильма {}", newFilm);

        if (newFilm.getId() == null) {
            log.warn("Передан запрос без ID");
            throw new ConditionsNotMetException("ID не указан");
        }

        Film existingFilm = films.get(newFilm.getId());
        if (existingFilm == null) {
            log.warn("Передан фильм, которого нет в базе");
            throw new NotFoundException("Фильм с ID - " + newFilm.getId() + " не найден");
        }

        boolean checkDuplicate = false;

        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            if (!newFilm.getName().equalsIgnoreCase(existingFilm.getName())) {
                existingFilm.setName(newFilm.getName());
                log.debug("Фильму установлено название {}", newFilm.getName());
                checkDuplicate = true;
            }
        }

        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                throw new ValidationException("Длина описания не может быть больше 200 символов");
            }
            existingFilm.setDescription(newFilm.getDescription());
            log.debug("Фильму установлено описание {}", newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            LocalDate earliestDate = LocalDate.of(1895, 12, 28);
            if (newFilm.getReleaseDate().isBefore(earliestDate)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 г.");
            }
            if (!newFilm.getReleaseDate().equals(existingFilm.getReleaseDate())) {
                existingFilm.setReleaseDate(newFilm.getReleaseDate());
                log.debug("Фильму установлена дата релиза {}", newFilm.getReleaseDate());
                checkDuplicate = true;
            }
        }

        if (newFilm.getDuration() != null) {
            if (newFilm.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
            existingFilm.setDuration(newFilm.getDuration());
            log.debug("Фильму установлена длительность {}", newFilm.getDuration());
        }

        if (checkDuplicate) {
            boolean duplicateExists = films.values().stream()
                    .anyMatch(film -> film != existingFilm &&
                            film.getName().equalsIgnoreCase(existingFilm.getName()) &&
                            film.getReleaseDate().equals(existingFilm.getReleaseDate()));

            if (duplicateExists) {
                log.warn("Передан фильм, который уже есть в базе");
                throw new DuplicatedDataException("Такой фильм уже есть в базе");
            }
        }

        films.put(existingFilm.getId(), existingFilm);
        log.info("Завершение запроса изменения фильма {}", existingFilm);
        return existingFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}