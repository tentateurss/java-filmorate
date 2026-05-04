package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testCreateAndFindFilmById() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new Mpa(1L, "G"))
                .build();

        Film created = filmStorage.createFilm(film);

        Optional<Film> found = filmStorage.getFilmById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getName()).isEqualTo("Test Film");
        assertThat(found.get().getDuration()).isEqualTo(120);
        assertThat(found.get().getMpa()).isNotNull();
        assertThat(found.get().getMpa().getId()).isEqualTo(1L);
        assertThat(found.get().getMpa().getName()).isEqualTo("G");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testCreateFilmWithGenres() {
        Film film = Film.builder()
                .name("Film with genres")
                .description("Desc")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new Mpa(1L, "G"))
                .genres(List.of(new Genre(1L, "Комедия"), new Genre(3L, "Мультфильм")))
                .build();

        Film created = filmStorage.createFilm(film);

        assertThat(created.getGenres()).hasSize(2);
        assertThat(created.getGenres()).extracting(Genre::getName)
                .containsExactly("Комедия", "Мультфильм");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testGetAllFilms() {
        filmStorage.createFilm(Film.builder()
                .name("Film 1").description("Desc 1")
                .releaseDate(LocalDate.of(2020, 1, 1)).duration(100)
                .mpa(new Mpa(1L, "G")).build());

        filmStorage.createFilm(Film.builder()
                .name("Film 2").description("Desc 2")
                .releaseDate(LocalDate.of(2021, 1, 1)).duration(110)
                .mpa(new Mpa(2L, "PG")).build());

        Collection<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testUpdateFilm() {
        Film film = filmStorage.createFilm(Film.builder()
                .name("Old Name").description("Old Desc")
                .releaseDate(LocalDate.of(2020, 1, 1)).duration(100)
                .mpa(new Mpa(1L, "G")).build());

        film.setName("New Name");
        film.setDuration(150);
        film.setMpa(new Mpa(2L, "PG"));
        filmStorage.updateFilm(film);

        Optional<Film> updated = filmStorage.getFilmById(film.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name");
        assertThat(updated.get().getDuration()).isEqualTo(150);
        assertThat(updated.get().getMpa().getId()).isEqualTo(2L);
        assertThat(updated.get().getMpa().getName()).isEqualTo("PG");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testUpdateFilmGenres() {
        Film film = filmStorage.createFilm(Film.builder()
                .name("Genre Test").description("Desc")
                .releaseDate(LocalDate.of(2020, 1, 1)).duration(100)
                .mpa(new Mpa(1L, "G"))
                .genres(List.of(new Genre(1L, "Комедия")))
                .build());

        film.setGenres(List.of(new Genre(2L, "Драма"), new Genre(4L, "Триллер")));
        filmStorage.updateFilm(film);

        Optional<Film> updated = filmStorage.getFilmById(film.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getGenres()).hasSize(2);
        assertThat(updated.get().getGenres()).extracting(Genre::getName)
                .containsExactly("Драма", "Триллер");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testIsDuplicates() {
        filmStorage.createFilm(Film.builder()
                .name("Unique Film").description("Desc")
                .releaseDate(LocalDate.of(2020, 1, 1)).duration(100)
                .mpa(new Mpa(1L, "G")).build());

        Film duplicate = Film.builder()
                .name("Unique Film")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();

        assertThat(filmStorage.isDuplicates(duplicate)).isTrue();
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testFindFilmByIdNotFound() {
        Optional<Film> found = filmStorage.getFilmById(999L);
        assertThat(found).isEmpty();
    }
}