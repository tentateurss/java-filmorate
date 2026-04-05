package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validators.filmvalidators.FilmValidators;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: {}", film);

        FilmValidators.validateFilm(film);

        if (filmStorage.isDuplicates(film)) {
            log.warn("Обнаружен дубликат фильма при создании");
            throw new DuplicatedDataException("Такой фильм уже есть в базе");
        }

        Film created = filmStorage.createFilm(film);
        log.info("Фильм создан с ID: {}", created.getId());
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

        if (newFilm.getName() != null && !newFilm.getName().equalsIgnoreCase(existingFilm.getName())) {
            existingFilm.setName(newFilm.getName());
            log.debug("Обновлено название: {}", newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            existingFilm.setDescription(newFilm.getDescription());
            log.debug("Обновлено описание");
        }

        if (newFilm.getReleaseDate() != null && !newFilm.getReleaseDate().equals(existingFilm.getReleaseDate())) {
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
            log.debug("Обновлена дата релиза: {}", newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            existingFilm.setDuration(newFilm.getDuration());
            log.debug("Обновлена длительность: {}", newFilm.getDuration());
        }


        if (filmStorage.isDuplicates(existingFilm)) {
            log.warn("Обнаружен дубликат фильма при обновлении");
            throw new DuplicatedDataException("Такой фильм уже есть в базе");
        }

        Film updated = filmStorage.updateFilm(existingFilm);
        log.info("Фильм обновлен: {}", updated);
        return updated;
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);

        Film film = getFilmById(filmId);

        userService.getUserById(userId);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getLikes().contains(userId)) {
            throw new DuplicatedDataException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        log.debug("Лайк добавлен. Теперь у фильма {} лайков: {}", filmId, film.getLikes().size());
    }

    public void deleteLike(Long filmId, Long userId) {
        log.debug("Пользователь {} убирает лайк с фильма {}", userId, filmId);

        Film film = getFilmById(filmId);

        userService.getUserById(userId);

        if (film.getLikes() == null || !film.getLikes().contains(userId)) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);
        log.debug("Лайк удален. Теперь у фильма {} лайков: {}", filmId, film.getLikes().size());
    }

    public List<Film> getPopularFilms(Long count) {
        if (count == null) {
            count = 10L;
        }

        if (count <= 0) {
            log.info("Запрошено {} фильмов, возвращаем пустой список", count);
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

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID {} не найден", id);
                    return new NotFoundException("Фильм с ID - " + id + " не найден");
                });
    }
}