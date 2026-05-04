package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validators.uservalidators.UserValidators;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserDbStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = (UserDbStorage) userStorage;
    }

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
        }
        if (newUser.getLogin() != null) {
            existingUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        if (newUser.getBirthday() != null) {
            existingUser.setBirthday(newUser.getBirthday());
        }

        User updated = userStorage.updateUser(existingUser);
        log.info("Завершение запроса изменения пользователя {}", updated);
        return updated;
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new NotFoundException("Пользователь с ID - " + id + " не найден");
                });
    }

    public void deleteUser(Long userId) {
        getUserById(userId);
        userStorage.deleteUser(userId);
        log.info("Пользователь с ID {} удалён", userId);
    }

    public Collection<User> getAllFriends(Long userId) {
        getUserById(userId);
        log.info("Запрос списка подтверждённых друзей пользователя с ID {}", userId);
        return userStorage.getConfirmedFriends(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }
        getUserById(userId);
        getUserById(friendId);

        if (userStorage.friendshipExists(userId, friendId)) {
            throw new ConditionsNotMetException("Заявка в друзья уже отправлена");
        }

        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        log.info("Пользователь {} удаляет из друзей {}", userId, friendId);
        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        getUserById(userId);
        getUserById(otherId);

        log.info("Запрос общих подтверждённых друзей пользователей {} и {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }
}