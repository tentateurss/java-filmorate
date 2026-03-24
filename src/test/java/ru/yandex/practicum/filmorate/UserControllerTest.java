package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = User.builder()
                .email("test@test.com")
                .login("TEST")
                .name("TEST TEST")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    // Тесты POST /users
    @Test
    void createUserShouldReturnUserWhenUserIsValid() {
        User created = userController.createUser(validUser);

        assertNotNull(created.getId());
        assertEquals(validUser.getEmail(), created.getEmail());
        assertEquals(validUser.getLogin(), created.getLogin());
        assertEquals(validUser.getName(), created.getName());
        assertEquals(validUser.getBirthday(), created.getBirthday());
    }

    @Test
    void createUserShouldSetNameEqualsLoginWhenNameIsNull() {
        User userWithoutName = User.builder()
                .email("test@test.com")
                .login("testlogin")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userController.createUser(userWithoutName);

        assertEquals("testlogin", created.getName());
    }

    @Test
    void createUserShouldSetNameEqualsLoginWhenNameIsBlank() {
        User userWithBlankName = User.builder()
                .email("test@test.com")
                .login("testlogin")
                .name("   ")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userController.createUser(userWithBlankName);

        assertEquals("testlogin", created.getName());
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailIsBlank() {
        User userWithBlankEmail = User.builder()
                .email("")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userController.createUser(userWithBlankEmail);
        assertEquals("", created.getEmail());
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailDoesNotContainAt() {
        User userWithInvalidEmail = User.builder()
                .email("testtest.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userController.createUser(userWithInvalidEmail);
        assertEquals("testtest.com", created.getEmail());
    }

    @Test
    void createUserShouldThrowExceptionWhenLoginBlank() {
        User userWithBlankLogin = User.builder()
                .email("test@test.com")
                .login("")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userController.createUser(userWithBlankLogin);
        assertEquals("", created.getLogin());
    }

    @Test
    void createUserShouldThrowExceptionWhenLoginContainsSpaces() {
        User userWithSpacesInLogin = User.builder()
                .email("test@test.com")
                .login("test login")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userController.createUser(userWithSpacesInLogin);
        assertEquals("test login", created.getLogin());
    }

    @Test
    void createUserShouldThrowExceptionWhenBirthdayInFuture() {
        User userWithFutureBirthday = User.builder()
                .email("test@test.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(userWithFutureBirthday));

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void createUserShouldThrowExceptionWhenBirthdayIsNull() {
        User userWithNullBirthday = User.builder()
                .email("test@test.com")
                .login("testlogin")
                .name("Test User")
                .birthday(null)
                .build();

        User created = userController.createUser(userWithNullBirthday);
        assertNull(created.getBirthday());
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailAlreadyExists() {
        userController.createUser(validUser);

        User duplicateUser = User.builder()
                .email("test@test.com")
                .login("anotherlogin")
                .name("Another User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> userController.createUser(duplicateUser));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
    }

    // Тесты GET /users
    @Test
    void getAllUsersShouldReturnEmptyCollectionWhenNoUsers() {
        Collection<User> users = userController.getAllUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        userController.createUser(validUser);

        User secondUser = User.builder()
                .email("second@test.com")
                .login("secondlogin")
                .name("Second User")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
        userController.createUser(secondUser);

        Collection<User> users = userController.getAllUsers();

        assertEquals(2, users.size());
    }

    // Тесты PUT /users
    @Test
    void updateUserShouldReturnUpdatedUserWhenUserExists() {
        User created = userController.createUser(validUser);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("updated@example.com")
                .login("updatedlogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User updated = userController.updateUser(updatedUser);

        assertEquals(created.getId(), updated.getId());
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("updatedlogin", updated.getLogin());
        assertEquals("Updated Name", updated.getName());
        assertEquals(LocalDate.of(1995, 5, 5), updated.getBirthday());
    }

    @Test
    void updateUserShouldUpdateOnlyProvidedFields() {
        User created = userController.createUser(validUser);

        User partialUpdate = User.builder()
                .id(created.getId())
                .name("Only Name Updated")
                .build();

        User updated = userController.updateUser(partialUpdate);

        assertEquals(created.getId(), updated.getId());
        assertEquals(validUser.getEmail(), updated.getEmail());
        assertEquals(validUser.getLogin(), updated.getLogin());
        assertEquals("Only Name Updated", updated.getName());
        assertEquals(validUser.getBirthday(), updated.getBirthday());
    }

    @Test
    void updateUserShouldUpdateOnlyEmail() {
        User created = userController.createUser(validUser);

        User partialUpdate = User.builder()
                .id(created.getId())
                .email("newemail@example.com")
                .build();

        User updated = userController.updateUser(partialUpdate);

        assertEquals("newemail@example.com", updated.getEmail());
        assertEquals(validUser.getLogin(), updated.getLogin());
        assertEquals(validUser.getName(), updated.getName());
        assertEquals(validUser.getBirthday(), updated.getBirthday());
    }

    @Test
    void updateUserShouldUpdateOnlyLogin() {
        User created = userController.createUser(validUser);

        User partialUpdate = User.builder()
                .id(created.getId())
                .login("newlogin")
                .build();

        User updated = userController.updateUser(partialUpdate);

        assertEquals("newlogin", updated.getLogin());
        assertEquals(validUser.getEmail(), updated.getEmail());
        assertEquals(validUser.getName(), updated.getName());
        assertEquals(validUser.getBirthday(), updated.getBirthday());
    }

    @Test
    void updateUserShouldUpdateOnlyBirthday() {
        User created = userController.createUser(validUser);

        LocalDate newBirthday = LocalDate.of(1998, 8, 8);
        User partialUpdate = User.builder()
                .id(created.getId())
                .birthday(newBirthday)
                .build();

        User updated = userController.updateUser(partialUpdate);

        assertEquals(newBirthday, updated.getBirthday());
        assertEquals(validUser.getEmail(), updated.getEmail());
        assertEquals(validUser.getLogin(), updated.getLogin());
        assertEquals(validUser.getName(), updated.getName());
    }

    @Test
    void updateUserShouldThrowExceptionWhenIdIsNull() {
        User userWithoutId = User.builder()
                .email("user@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> userController.updateUser(userWithoutId));

        assertEquals("ID не указан", exception.getMessage());
    }

    @Test
    void updateUserShouldThrowExceptionWhenUserNotFound() {
        User nonExistentUser = User.builder()
                .id(999L)
                .email("user@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.updateUser(nonExistentUser));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void updateUserShouldThrowExceptionWhenEmailAlreadyExists() {
        User firstUser = User.builder()
                .email("first@example.com")
                .login("firstlogin")
                .name("First User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userController.createUser(firstUser);

        User secondUser = User.builder()
                .email("second@example.com")
                .login("secondlogin")
                .name("Second User")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
        User createdSecond = userController.createUser(secondUser);

        User updateWithExistingEmail = User.builder()
                .id(createdSecond.getId())
                .email("first@example.com")
                .build();

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> userController.updateUser(updateWithExistingEmail));

        assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    void updateUserShouldNotThrowExceptionWhenEmailIsSame() {
        User created = userController.createUser(validUser);

        User updateWithSameEmail = User.builder()
                .id(created.getId())
                .email(validUser.getEmail())
                .name("New Name")
                .build();

        assertDoesNotThrow(() -> userController.updateUser(updateWithSameEmail));

        User updated = userController.updateUser(updateWithSameEmail);
        assertEquals("New Name", updated.getName());
        assertEquals(validUser.getEmail(), updated.getEmail());
    }
}