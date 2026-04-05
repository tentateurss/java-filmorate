package ru.yandex.practicum.filmorate.validators.uservalidators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

@Slf4j
public class UserValidators {

    public static void validateUserId(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("ID не указан");
        }
    }

    public static void validateUserEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Почта {} не прошла валидацию", user.getEmail());
            throw new ValidationException("Почта введена некорректно");
        }
    }

    public static void validateUserLogin(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Логин пользователя не прошёл валидацию");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }
    }

    public static void validateUserBirthday(User user) {
        if (user.getBirthday() == null) {
            log.warn("Дата рождения не указана");
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения пользователя не прошла валидацию");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public static void setUserNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("У пользователя {} не передано имя, был установлен логин {} вместо имени",
                    user, user.getLogin());
            user.setName(user.getLogin());
        }
    }

    public static void validateEmailDuplicate(User user, UserStorage userStorage) {
        if (userStorage.emailExists(user.getEmail())) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", user.getEmail());
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }
    }

    public static void validateForCreate(User user, UserStorage userStorage) {
        validateUserEmail(user);
        validateUserLogin(user);
        validateUserBirthday(user);
        setUserNameIfBlank(user);
        validateEmailDuplicate(user, userStorage);
    }

    public static void validateForUpdate(User newUser, User existingUser, UserStorage userStorage) {
        validateUserId(newUser.getId());

        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения пользователя не прошла валидацию");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                throw new ValidationException("Почта введена некорректно");
            }
            if (userStorage.emailExists(newUser.getEmail())) {
                log.warn("Передана почта, которая указана у другого пользователя");
                throw new DuplicatedDataException("Данная почта уже используется");
            }
        }

        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
            }
        }
    }
}