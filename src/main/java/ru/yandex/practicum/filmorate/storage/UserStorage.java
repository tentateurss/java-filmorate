package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User createUser(User user);

    User updateUser(User newUser);

    void deleteUser(Long userId);

    boolean existsById(Long id);

    boolean emailExists(String email);

    long getNextId();
}