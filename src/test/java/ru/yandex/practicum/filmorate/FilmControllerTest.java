package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {
    private FilmController filmController;
    private UserController userController;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;
    private UserService userService;
    private Film validFilm;
    private User validUser;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmStorage, filmService);
        userController = new UserController(userStorage, userService);

        validFilm = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        validUser = User.builder()
                .email("test@test.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    // Тесты POST /films
    @Test
    void createFilmShouldReturnFilmWhenFilmIsValid() {
        Film created = filmController.createFilm(validFilm);

        assertNotNull(created.getId());
        assertEquals(validFilm.getName(), created.getName());
        assertEquals(validFilm.getDescription(), created.getDescription());
        assertEquals(validFilm.getReleaseDate(), created.getReleaseDate());
        assertEquals(validFilm.getDuration(), created.getDuration());
    }

    @Test
    void createFilmShouldThrowExceptionWhenNameIsBlank() {
        Film filmWithBlankName = Film.builder()
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithBlankName));

        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilmShouldThrowExceptionWhenNameIsNull() {
        Film filmWithNullName = Film.builder()
                .name(null)
                .description("Valid description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithNullName));

        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilmShouldThrowExceptionWhenDescriptionTooLong() {
        String longDescription = "a".repeat(201);
        Film filmWithLongDescription = Film.builder()
                .name("Valid Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithLongDescription));

        assertEquals("Длина описания не может быть больше 200 символов", exception.getMessage());
    }

    @Test
    void createFilmShouldCreateFilmWhenDescriptionExactly200Chars() {
        String exactDescription = "a".repeat(200);
        Film filmWithExactDescription = Film.builder()
                .name("Valid Film")
                .description(exactDescription)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        Film created = filmController.createFilm(filmWithExactDescription);

        assertEquals(exactDescription, created.getDescription());
    }

    @Test
    void createFilmShouldThrowExceptionWhenReleaseDateTooEarly() {
        Film filmWithEarlyDate = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithEarlyDate));

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 г.", exception.getMessage());
    }

    @Test
    void createFilmShouldCreateFilmWhenReleaseDateIsEarliestDate() {
        Film filmWithEarliestDate = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .build();

        Film created = filmController.createFilm(filmWithEarliestDate);

        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void createFilmShouldThrowExceptionWhenDurationNegative() {
        Film filmWithNegativeDuration = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(-10)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithNegativeDuration));

        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilmShouldThrowExceptionWhenDurationZero() {
        Film filmWithZeroDuration = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(0)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithZeroDuration));

        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createFilmShouldCreateFilmWhenDurationPositive() {
        Film created = filmController.createFilm(validFilm);

        assertEquals(120, created.getDuration());
    }

    @Test
    void createFilmShouldThrowExceptionWhenReleaseDateIsNull() {
        Film filmWithNullDate = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(null)
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(filmWithNullDate));

        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    // Тесты GET /films
    @Test
    void getAllFilmsShouldReturnEmptyCollectionWhenNoFilms() {
        Collection<Film> films = filmController.getAllFilms();

        assertNotNull(films);
        assertTrue(films.isEmpty());
    }

    @Test
    void getAllFilmsShouldReturnAllFilms() {
        filmController.createFilm(validFilm);

        Film secondFilm = Film.builder()
                .name("Second Film")
                .description("Second description")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .build();
        filmController.createFilm(secondFilm);

        Collection<Film> films = filmController.getAllFilms();

        assertEquals(2, films.size());
    }

    // Тесты PUT /films
    @Test
    void updateFilmShouldReturnUpdatedFilmWhenFilmExists() {
        Film created = filmController.createFilm(validFilm);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .duration(150)
                .build();

        Film updated = filmController.updateFilm(updatedFilm);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Updated Film", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), updated.getReleaseDate());
        assertEquals(150, updated.getDuration());
    }

    @Test
    void updateFilmShouldUpdateOnlyProvidedFields() {
        Film created = filmController.createFilm(validFilm);

        Film partialUpdate = Film.builder()
                .id(created.getId())
                .name("Only Name Updated")
                .build();

        Film updated = filmController.updateFilm(partialUpdate);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Only Name Updated", updated.getName());
        assertEquals(validFilm.getDescription(), updated.getDescription());
        assertEquals(validFilm.getReleaseDate(), updated.getReleaseDate());
        assertEquals(validFilm.getDuration(), updated.getDuration());
    }

    @Test
    void updateFilmShouldUpdateOnlyDescription() {
        Film created = filmController.createFilm(validFilm);

        Film partialUpdate = Film.builder()
                .id(created.getId())
                .description("New description")
                .build();

        Film updated = filmController.updateFilm(partialUpdate);

        assertEquals("New description", updated.getDescription());
        assertEquals(validFilm.getName(), updated.getName());
        assertEquals(validFilm.getReleaseDate(), updated.getReleaseDate());
        assertEquals(validFilm.getDuration(), updated.getDuration());
    }

    @Test
    void updateFilmShouldUpdateOnlyReleaseDate() {
        Film created = filmController.createFilm(validFilm);

        LocalDate newDate = LocalDate.of(2023, 6, 15);
        Film partialUpdate = Film.builder()
                .id(created.getId())
                .releaseDate(newDate)
                .build();

        Film updated = filmController.updateFilm(partialUpdate);

        assertEquals(newDate, updated.getReleaseDate());
        assertEquals(validFilm.getName(), updated.getName());
        assertEquals(validFilm.getDescription(), updated.getDescription());
        assertEquals(validFilm.getDuration(), updated.getDuration());
    }

    @Test
    void updateFilmShouldUpdateOnlyDuration() {
        Film created = filmController.createFilm(validFilm);

        Film partialUpdate = Film.builder()
                .id(created.getId())
                .duration(180)
                .build();

        Film updated = filmController.updateFilm(partialUpdate);

        assertEquals(180, updated.getDuration());
        assertEquals(validFilm.getName(), updated.getName());
        assertEquals(validFilm.getDescription(), updated.getDescription());
        assertEquals(validFilm.getReleaseDate(), updated.getReleaseDate());
    }

    @Test
    void updateFilmShouldThrowExceptionWhenIdIsNull() {
        Film filmWithoutId = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> filmController.updateFilm(filmWithoutId));

        assertEquals("ID не указан", exception.getMessage());
    }

    @Test
    void updateFilmShouldThrowExceptionWhenFilmNotFound() {
        Film nonExistentFilm = Film.builder()
                .id(999L)
                .name("Non Existent")
                .description("Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.updateFilm(nonExistentFilm));

        assertEquals("Фильм с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void updateFilmShouldThrowExceptionWhenDuplicateExists() {
        Film firstFilm = Film.builder()
                .name("Unique Film")
                .description("First description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();
        filmController.createFilm(firstFilm);

        Film secondFilm = Film.builder()
                .name("Another Film")
                .description("Second description")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .build();
        Film createdSecond = filmController.createFilm(secondFilm);

        Film duplicateUpdate = Film.builder()
                .id(createdSecond.getId())
                .name("Unique Film")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> filmController.updateFilm(duplicateUpdate));

        assertEquals("Такой фильм уже есть в базе", exception.getMessage());
    }

    @Test
    void updateFilmShouldNotThrowExceptionWhenSameNameAndDate() {
        Film created = filmController.createFilm(validFilm);

        Film updateWithSameData = Film.builder()
                .id(created.getId())
                .name(validFilm.getName())
                .releaseDate(validFilm.getReleaseDate())
                .description("New description")
                .build();

        assertDoesNotThrow(() -> filmController.updateFilm(updateWithSameData));

        Film updated = filmController.updateFilm(updateWithSameData);
        assertEquals("New description", updated.getDescription());
        assertEquals(validFilm.getName(), updated.getName());
    }

    @Test
    void updateFilmShouldNotCreateDuplicateWhenOnlyNameChanges() {
        Film firstFilm = Film.builder()
                .name("First Film")
                .description("First description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();
        filmController.createFilm(firstFilm);

        Film secondFilm = Film.builder()
                .name("Second Film")
                .description("Second description")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .build();
        Film createdSecond = filmController.createFilm(secondFilm);

        Film update = Film.builder()
                .id(createdSecond.getId())
                .name("First Film")
                .build();

        assertDoesNotThrow(() -> filmController.updateFilm(update));

        Film updated = filmController.updateFilm(update);
        assertEquals("First Film", updated.getName());
        assertEquals(LocalDate.of(2021, 1, 1), updated.getReleaseDate());
    }

    // Тесты лайков
    @Test
    void addLikeShouldAddLikeToFilm() {
        Film film = filmController.createFilm(validFilm);
        User user = userController.createUser(validUser);

        filmController.addLike(film.getId(), user.getId());

        Film likedFilm = filmController.getFilmId(film.getId());
        assertEquals(1, likedFilm.getLikes().size());
        assertTrue(likedFilm.getLikes().contains(user.getId()));
    }

    @Test
    void addLikeShouldThrowExceptionWhenFilmNotFound() {
        User user = userController.createUser(validUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.addLike(999L, user.getId()));

        assertEquals("Фильм с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void addLikeShouldThrowExceptionWhenUserNotFound() {
        Film film = filmController.createFilm(validFilm);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.addLike(film.getId(), 999L));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void addLikeShouldThrowExceptionWhenUserAlreadyLiked() {
        Film film = filmController.createFilm(validFilm);
        User user = userController.createUser(validUser);

        filmController.addLike(film.getId(), user.getId());

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> filmController.addLike(film.getId(), user.getId()));

        assertEquals("Пользователь уже поставил лайк этому фильму", exception.getMessage());
    }

    @Test
    void deleteLikeShouldRemoveLikeFromFilm() {
        Film film = filmController.createFilm(validFilm);
        User user = userController.createUser(validUser);

        filmController.addLike(film.getId(), user.getId());
        filmController.deleteLike(film.getId(), user.getId());

        Film unlikedFilm = filmController.getFilmId(film.getId());
        assertEquals(0, unlikedFilm.getLikes().size());
        assertFalse(unlikedFilm.getLikes().contains(user.getId()));
    }

    @Test
    void deleteLikeShouldThrowExceptionWhenFilmNotFound() {
        User user = userController.createUser(validUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.deleteLike(999L, user.getId()));

        assertEquals("Фильм с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void deleteLikeShouldThrowExceptionWhenUserNotFound() {
        Film film = filmController.createFilm(validFilm);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.deleteLike(film.getId(), 999L));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void deleteLikeShouldThrowExceptionWhenUserDidNotLike() {
        Film film = filmController.createFilm(validFilm);
        User user = userController.createUser(validUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.deleteLike(film.getId(), user.getId()));

        assertEquals("Пользователь не ставил лайк этому фильму", exception.getMessage());
    }

    // Тесты для популярных фильмов
    @Test
    void getPopularFilmsShouldReturnFilmsSortedByLikes() {
        Film film1 = filmController.createFilm(Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .build());

        Film film2 = filmController.createFilm(Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(110)
                .build());

        Film film3 = filmController.createFilm(Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(120)
                .build());

        User user1 = userController.createUser(validUser);
        User user2 = userController.createUser(User.builder()
                .email("user2@test.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        filmController.addLike(film2.getId(), user1.getId());
        filmController.addLike(film2.getId(), user2.getId());
        filmController.addLike(film1.getId(), user1.getId());

        Collection<Film> popularFilms = filmController.getPopularityFilm(10L);
        List<Film> filmsList = popularFilms.stream().toList();

        assertEquals(3, filmsList.size());
        assertEquals(film2.getId(), filmsList.get(0).getId());
        assertEquals(film1.getId(), filmsList.get(1).getId());
        assertEquals(film3.getId(), filmsList.get(2).getId());
    }

    @Test
    void getPopularFilmsShouldReturnLimitedCount() {
        for (int i = 0; i < 15; i++) {
            Film film = Film.builder()
                    .name("Film " + i)
                    .description("Description " + i)
                    .releaseDate(LocalDate.of(2020, 1, 1))
                    .duration(100 + i)
                    .build();
            filmController.createFilm(film);
        }

        Collection<Film> popularFilms = filmController.getPopularityFilm(5L);

        assertEquals(5, popularFilms.size());
    }

    @Test
    void getPopularFilmsShouldReturnDefault10WhenCountNotSpecified() {
        for (int i = 0; i < 15; i++) {
            Film film = Film.builder()
                    .name("Film " + i)
                    .description("Description " + i)
                    .releaseDate(LocalDate.of(2020, 1, 1))
                    .duration(100 + i)
                    .build();
            filmController.createFilm(film);
        }

        Collection<Film> popularFilms = filmController.getPopularityFilm(null);

        assertEquals(10, popularFilms.size());
    }

    @Test
    void getPopularFilmsShouldReturnEmptyListWhenNoFilms() {
        Collection<Film> popularFilms = filmController.getPopularityFilm(10L);

        assertNotNull(popularFilms);
        assertTrue(popularFilms.isEmpty());
    }
}