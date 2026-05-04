package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validators.uservalidators.UserValidators;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userStorage.getAllUsers();
    }


    public User createUser(User user) {
        log.info("Начало запроса на добавление пользователя {}", user);

        UserValidators.validateForCreate(user, userStorage);

        User created = userStorage.createUser(user);
        log.info("Завершение запроса на добавление пользователя {}", created);
        return created;
    }

    public User updateUser(User newUser) {
        log.info("Начало запроса изменение пользователя {}", newUser);

        UserValidators.validateUserId(newUser.getId());

        User existingUser = userStorage.getUserById(newUser.getId())
                .orElseThrow(() -> {
                    log.warn("Передан пользователь, которого нет в базе");
                    return new NotFoundException("Пользователь с ID - " + newUser.getId() + " не найден");
                });

        UserValidators.validateForUpdate(newUser, existingUser, userStorage);

        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            existingUser.setEmail(newUser.getEmail());
            log.debug("Пользователю установлена почта {}", newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            existingUser.setLogin(newUser.getLogin());
            log.debug("Пользователю установлен логин {}", newUser.getLogin());
        }

        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
            log.debug("Пользователю установлено имя {}", newUser.getName());
        }

        if (newUser.getBirthday() != null) {
            existingUser.setBirthday(newUser.getBirthday());
            log.debug("Пользователю установлена дата рождения {}", newUser.getBirthday());
        }

        User updated = userStorage.updateUser(existingUser);
        log.info("Завершение запроса изменения пользователя {}", updated);
        return updated;
    }

    public Collection<User> getAllFriends(Long userId) {
        User user = getUserById(userId);
        log.info("Запрос списка друзей пользователя с ID {}", userId);

        if (user.getFriends() == null) {
            return List.of();
        }

        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья: userId={}", userId);
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        log.info("Пользователь {} добавляет в друзья {}", userId, friendId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.debug("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Пользователь {} удаляет из друзей {}", userId, friendId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.debug("Пользователь {} и {} теперь не друзья", userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрос общих друзей пользователей {} и {}", userId, otherId);

        User user = getUserById(userId);
        User other = getUserById(otherId);

        if (user.getFriends() == null || other.getFriends() == null) {
            return List.of();
        }

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new NotFoundException("Пользователь с ID - " + id + " не найден");
                });
    }
}