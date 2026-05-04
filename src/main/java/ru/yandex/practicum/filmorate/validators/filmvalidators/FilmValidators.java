package ru.yandex.practicum.filmorate.validators.filmvalidators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
public class FilmValidators {
    private static final LocalDate earliestDate = LocalDate.of(1895, 12, 28);

    public static void validateFilmName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма не прошло валидацию");
            throw new ValidationException("Название не может быть пустым");
        }
    }

    public static void validateFilmDescription(Film film) {
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Описание фильма не прошло валидацию");
            throw new ValidationException("Длина описания не может быть больше 200 символов");
        }
    }

    public static void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(earliestDate)) {
            log.warn("Дата релиза фильма не прошла валидацию");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 г.");
        }
    }

    public static void validateFilmDuration(Film film) {
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Продолжительность фильма не прошла валидацию");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public static void validateFilmId(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("ID не указан");
        }
    }

    public static void validateFilm(Film film) {
        validateFilmName(film);
        validateFilmDescription(film);
        validateFilmReleaseDate(film);
        validateFilmDuration(film);
    }

    public static void validateFilmForUpdate(Film newFilm) {
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Название фильма не прошло валидацию при обновлении");
                throw new ValidationException("Название не может быть пустым");
            }
        }

        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                log.warn("Описание фильма не прошло валидацию при обновлении");
                throw new ValidationException("Длина описания не может быть больше 200 символов");
            }
        }

        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(earliestDate)) {
                log.warn("Дата релиза фильма не прошла валидацию при обновлении");
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 г.");
            }
        }

        if (newFilm.getDuration() != null) {
            if (newFilm.getDuration() <= 0) {
                log.warn("Продолжительность фильма не прошла валидацию при обновлении");
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
        }
    }
}