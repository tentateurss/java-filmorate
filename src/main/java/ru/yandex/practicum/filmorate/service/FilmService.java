package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long filmId, Long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);

        Film film = getFilmById(filmId);
        getUserById(userId);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getLikes().contains(userId)) {
            throw new DuplicatedDataException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        log.debug("Пользователь {} поставил лайк фильму {}. Количество лайков фильма - {}",
                userId, filmId, film.getLikes().size());
    }

    public void deleteLike(Long filmId, Long userId) {
        log.debug("Пользователь {} убирает лайк с фильма {}", userId, filmId);

        Film film = getFilmById(filmId);
        getUserById(userId);

        if (film.getLikes() == null) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);
        log.debug("Пользователь {} убрал лайк с фильма {}. Количество лайков фильма - {}",
                userId, filmId, film.getLikes().size());
    }

    public List<Film> getPopularFilms(Long count) {
        if (count == null || count <= 0) {
            count = 10L;
        }
        long finalCount = count;

        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(finalCount)
                .collect(Collectors.toList());
    }

    private User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new NotFoundException("Пользователь с ID - " + id + " не найден");
                });
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID {} не найден", id);
                    return new NotFoundException("Фильм с ID - " + id + " не найден");
                });
    }
}