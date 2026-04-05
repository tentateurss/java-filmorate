package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validators.uservalidators.UserValidators;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userStorage.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserId(@PathVariable Long userId) {
        log.info("Запрос пользователя");
        User user = userService.getUserById(userId);
        return user;
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getUserAllFriends(@PathVariable Long userId) {
        log.info("Запрос списка всех друзей пользователя");
        return userService.getAllFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long userId, @PathVariable Long otherId) {
        log.info("Запрос общих друзей пользователя {}", userId);
        return userService.getCommonFriends(userId, otherId);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Начало запроса на добавление пользователя {}", user);

        UserValidators.validateUser(user, userStorage);

        userStorage.createUser(user);
        log.debug("Пользователь добавлен");
        log.info("Завершение запроса на добавление пользователя {}", user);

        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Начало запроса изменение пользователя {}", newUser);

        UserValidators.validateUserId(newUser.getId());

        User existingUser = userStorage.getUserById(newUser.getId())
                .orElseThrow(() -> {
                    log.warn("Передан пользователь, которого нет в базе");
                    return new NotFoundException("Пользователь с ID - " + newUser.getId() + " не найден");
                });

        UserValidators.validateUserForUpdate(newUser, existingUser, userStorage);

        userStorage.updateUser(existingUser);
        log.info("Завершение запроса изменения пользователя {}", existingUser);
        return existingUser;
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Запрос на добавления в друзья пользователя {}", userId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Запрос на удаление из друзей пользователя {}", userId);
        userService.deleteFriend(userId, friendId);
    }
}