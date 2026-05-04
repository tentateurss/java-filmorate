package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User createUser(User user) {
        user.setId(getNextId());
        if (user.getFriends() == null) {
            user.setFriends(new HashMap<>());
        }
        users.put(user.getId(), user);
        log.debug("Пользователь сохранён в хранилище с ID {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        users.put(newUser.getId(), newUser);
        log.debug("Пользователь обновлён в хранилище с ID {}", newUser.getId());
        return newUser;
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь, которого требуется удалить - не найден");
        }

        for (User otherUser : users.values()) {
            if (otherUser.getFriends() != null) {
                otherUser.getFriends().remove(userId);
            }
        }

        users.remove(userId);
        log.debug("Пользователь с ID {} удалён из хранилища и из списков друзей", userId);
    }

    @Override
    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}