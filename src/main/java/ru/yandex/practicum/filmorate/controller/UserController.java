package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Начало запроса на добавление пользователя {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Почта {} не прошла валидацию", user.getEmail());
            throw new ValidationException("Почта введена некорректно");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Логин пользователя не прошёл валидацию");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }

        if (user.getBirthday() == null) {
            log.warn("Дата рождения не указана");
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения пользователя не прошла валидацию");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("У пользователя {} не передано имя, был установлен логин {} вместо имени", user, user.getLogin());
            user.setName(user.getLogin());
        }

        boolean emailExists = users.values().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExists) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", user.getEmail());
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }

        user.setId(getNextId());
        log.debug("Пользователю установлен ID {}", user.getId());
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен");

        log.info("Завершение запроса на добавление пользователя {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Начало запроса изменение пользователя {}", newUser);

        if (newUser.getId() == null) {
            log.warn("Передан запрос без ID");
            throw new ConditionsNotMetException("ID не указан");
        }

        User existingUser = users.get(newUser.getId());
        if (existingUser == null) {
            log.warn("Передан пользователь, которого нет в базе");
            throw new NotFoundException("Пользователь с ID - " + newUser.getId() + " не найден");
        }

        if (newUser.getBirthday() != null && newUser.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения пользователя не прошла валидацию");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                throw new ValidationException("Почта введена некорректно");
            }
            boolean emailExists = users.values().stream()
                    .anyMatch(user -> user.getEmail().equalsIgnoreCase(newUser.getEmail()));

            if (emailExists) {
                log.warn("Передана почта, которая указана у другого пользователя");
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            existingUser.setEmail(newUser.getEmail());
            log.debug("Пользователю установлена почта {}", newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
            }
            existingUser.setLogin(newUser.getLogin());
            log.debug("Пользователю установлен логин {}", newUser.getLogin());
        }

        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
            log.debug("Пользователю установлено имя {}", newUser.getName());
        } else if (newUser.getLogin() != null) {
            existingUser.setName(newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            existingUser.setBirthday(newUser.getBirthday());
            log.debug("Пользователю установлена дата рождения {}", newUser.getBirthday());
        }

        users.put(existingUser.getId(), existingUser);
        log.info("Завершение запроса изменения пользователя {}", existingUser);
        return existingUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}