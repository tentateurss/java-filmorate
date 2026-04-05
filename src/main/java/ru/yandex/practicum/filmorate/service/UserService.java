package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAllFriends(Long userId) {
        User user = getUserById(userId);
        log.info("Запрос списка друзей пользователя с ID {}", userId);

        if (user.getFriends() == null) {
            return List.of();
        }

        return user.getFriends().stream()
                .map(friendId -> getUserById(friendId))
                .collect(Collectors.toList());
    }

    public void addFriend(Long userId, Long friendId) {
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

        log.debug("Пользователь {} и {} теперь друзья", userId, friendId);
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
