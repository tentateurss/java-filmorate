package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.put(film.getId(), film);
        log.debug("Фильм сохранён в хранилище с ID {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        films.put(newFilm.getId(), newFilm);
        log.debug("Фильм обновлён в хранилище с ID {}", newFilm.getId());
        return newFilm;
    }

    @Override
    public boolean existsById(Long id) {
        return films.containsKey(id);
    }

    @Override
    public boolean isDuplicates(Film film) {
        return films.values().stream()
                .anyMatch(existingFilm ->
                        !existingFilm.getId().equals(film.getId()) &&
                                existingFilm.getName().equalsIgnoreCase(film.getName()) &&
                                existingFilm.getReleaseDate().equals(film.getReleaseDate()));
    }

    @Override
    public void deleteFilm(Long filmId) {
        films.remove(filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {

    }

    @Override
    public void removeLike(Long filmId, Long userId) {

    }

    @Override
    public long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
