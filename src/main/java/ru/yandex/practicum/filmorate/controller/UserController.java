package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        log.info("Запрос пользователя с ID {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getUserAllFriends(@PathVariable Long userId) {
        log.info("Запрос списка всех друзей пользователя {}", userId);
        return userService.getAllFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long userId, @PathVariable Long otherId) {
        log.info("Запрос общих друзей пользователя {} и {}", userId, otherId);
        return userService.getCommonFriends(userId, otherId);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Начало запроса на добавление пользователя {}", user);
        User created = userService.createUser(user);
        log.info("Завершение запроса на добавление пользователя {}", created);
        return created;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Начало запроса изменение пользователя {}", newUser);
        User updated = userService.updateUser(newUser);
        log.info("Завершение запроса изменения пользователя {}", updated);
        return updated;
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Запрос на добавление в друзья пользователя {} и {}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Запрос на удаление из друзей пользователя {} и {}", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Запрос на удаление пользователя с ID {}", userId);
        userService.deleteUser(userId);
    }
}