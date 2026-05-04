package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Старые тесты для InMemory")
@SpringBootTest
public class UserControllerTest {

    private UserController userController;
    private UserStorage userStorage;
    private UserService userService;
    private User validUser;
    private User friend1;
    private User friend2;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = null;
        userController = new UserController(userService);

        validUser = User.builder()
                .email("test@test.com")
                .login("TEST")
                .name("TEST TEST")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        friend1 = User.builder()
                .email("friend1@test.com")
                .login("friend1")
                .name("Friend One")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        friend2 = User.builder()
                .email("friend2@test.com")
                .login("friend2")
                .name("Friend Two")
                .birthday(LocalDate.of(1998, 8, 8))
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

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(userWithBlankEmail));

        assertEquals("Почта введена некорректно", exception.getMessage());
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailDoesNotContainAt() {
        User userWithInvalidEmail = User.builder()
                .email("testtest.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(userWithInvalidEmail));

        assertEquals("Почта введена некорректно", exception.getMessage());
    }

    @Test
    void createUserShouldThrowExceptionWhenLoginBlank() {
        User userWithBlankLogin = User.builder()
                .email("test@test.com")
                .login("")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(userWithBlankLogin));

        assertEquals("Логин не должен быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserShouldThrowExceptionWhenLoginContainsSpaces() {
        User userWithSpacesInLogin = User.builder()
                .email("test@test.com")
                .login("test login")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(userWithSpacesInLogin));

        assertEquals("Логин не должен быть пустым и содержать пробелы", exception.getMessage());
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

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(userWithNullBirthday));

        assertEquals("Дата рождения должна быть указана", exception.getMessage());
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

        assertEquals("Данная почта уже используется", exception.getMessage());
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

    // Тесты друзей
    @Test
    void addFriendShouldAddFriendToBothUsers() {
        User user = userController.createUser(validUser);
        User friend = userController.createUser(friend1);

        userController.addFriend(user.getId(), friend.getId());

        Collection<User> userFriends = userController.getUserAllFriends(user.getId());
        Collection<User> friendFriends = userController.getUserAllFriends(friend.getId());

        assertEquals(1, userFriends.size());
        assertEquals(1, friendFriends.size());
        assertEquals(friend.getId(), userFriends.iterator().next().getId());
        assertEquals(user.getId(), friendFriends.iterator().next().getId());
    }

    @Test
    void addFriendShouldThrowExceptionWhenUserNotFound() {
        User friend = userController.createUser(friend1);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.addFriend(999L, friend.getId()));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void addFriendShouldThrowExceptionWhenFriendNotFound() {
        User user = userController.createUser(validUser);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.addFriend(user.getId(), 999L));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void deleteFriendShouldRemoveFriendFromBothUsers() {
        User user = userController.createUser(validUser);
        User friend = userController.createUser(friend1);

        userController.addFriend(user.getId(), friend.getId());
        userController.deleteFriend(user.getId(), friend.getId());

        Collection<User> userFriends = userController.getUserAllFriends(user.getId());
        Collection<User> friendFriends = userController.getUserAllFriends(friend.getId());

        assertEquals(0, userFriends.size());
        assertEquals(0, friendFriends.size());
    }

    @Test
    void deleteFriendShouldThrowExceptionWhenUserNotFound() {
        User friend = userController.createUser(friend1);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.deleteFriend(999L, friend.getId()));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void getCommonFriendsShouldReturnMutualFriends() {
        User user = userController.createUser(validUser);
        User friend1User = userController.createUser(friend1);
        User friend2User = userController.createUser(friend2);

        userController.addFriend(user.getId(), friend1User.getId());
        userController.addFriend(user.getId(), friend2User.getId());

        userController.addFriend(friend1User.getId(), user.getId());
        userController.addFriend(friend1User.getId(), friend2User.getId());

        Collection<User> commonFriends = userController.getCommonFriends(user.getId(), friend1User.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(friend2User.getId(), commonFriends.iterator().next().getId());
    }

    @Test
    void getCommonFriendsShouldReturnEmptyListWhenNoMutualFriends() {
        User user = userController.createUser(validUser);
        User friend = userController.createUser(friend1);

        userController.addFriend(user.getId(), friend.getId());

        Collection<User> commonFriends = userController.getCommonFriends(user.getId(), friend.getId());

        assertEquals(0, commonFriends.size());
    }

    @Test
    void getCommonFriendsShouldThrowExceptionWhenUserNotFound() {
        User friend = userController.createUser(friend1);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.getCommonFriends(999L, friend.getId()));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void getUserAllFriendsShouldReturnEmptyListWhenNoFriends() {
        User user = userController.createUser(validUser);

        Collection<User> friends = userController.getUserAllFriends(user.getId());

        assertNotNull(friends);
        assertTrue(friends.isEmpty());
    }

    @Test
    void getUserAllFriendsShouldThrowExceptionWhenUserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.getUserAllFriends(999L));

        assertEquals("Пользователь с ID - 999 не найден", exception.getMessage());
    }

    @Test
    void addFriendShouldThrowExceptionWhenAddingSelf() {
        User user = userController.createUser(validUser);

        ConditionsNotMetException exception = assertThrows(ConditionsNotMetException.class,
                () -> userController.addFriend(user.getId(), user.getId()));

        assertEquals("Нельзя добавить самого себя в друзья", exception.getMessage());
    }
}