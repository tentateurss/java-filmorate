package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.util.*;

@Repository("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String FIND_ALL = "SELECT * FROM \"user\"";
    private static final String FIND_BY_ID = "SELECT * FROM \"user\" WHERE user_id = ?";
    private static final String INSERT =
            "INSERT INTO \"user\" (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE \"user\" SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE = "DELETE FROM \"user\" WHERE user_id = ?";
    private static final String EXISTS_BY_ID = "SELECT COUNT(*) FROM \"user\" WHERE user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getAllUsers() {
        List<User> users = findMany(FIND_ALL);
        for (User user : users) {
            user.setFriends(getFriends(user.getId()));
        }
        return users;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        Optional<User> userOpt = findOne(FIND_BY_ID, id);
        userOpt.ifPresent(user -> user.setFriends(getFriends(user.getId())));
        return userOpt;
    }

    @Override
    public User createUser(User user) {
        long id = insert(INSERT,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null
        );
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        update(UPDATE,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday() != null ? Date.valueOf(newUser.getBirthday()) : null,
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public void deleteUser(Long userId) {
        jdbc.update("DELETE FROM friendship WHERE user_id = ? OR friend_id = ?", userId, userId);
        jdbc.update("DELETE FROM film_like WHERE user_id = ?", userId);
        delete(DELETE, userId);
    }

    @Override
    public boolean existsById(Long id) {
        return existsById(EXISTS_BY_ID, id);
    }

    @Override
    public boolean emailExists(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM \"user\" WHERE email = ?", Integer.class, email
        );
        return count != null && count > 0;
    }

    @Override
    public long getNextId() {
        Long maxId = jdbc.queryForObject("SELECT MAX(user_id) FROM \"user\"", Long.class);
        return (maxId != null ? maxId : 0) + 1;
    }

    // Методы для друзей
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(
                "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)",
                userId, friendId, "confirmed"
        );
    }

    public void confirmFriend(Long userId, Long friendId) {
        jdbc.update(
                "UPDATE friendship SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );
    }

    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(
                "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );
    }

    public List<User> getConfirmedFriends(Long userId) {
        String sql = "SELECT u.* FROM \"user\" u " +
                "JOIN friendship f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbc.query(sql, mapper, userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = "SELECT u.* FROM \"user\" u " +
                "JOIN friendship f1 ON u.user_id = f1.friend_id AND f1.user_id = ? AND f1.status = 'confirmed' " +
                "JOIN friendship f2 ON u.user_id = f2.friend_id AND f2.user_id = ? AND f2.status = 'confirmed'";
        return jdbc.query(sql, mapper, userId, otherId);
    }

    public boolean friendshipExists(Long userId, Long friendId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
                Integer.class, userId, friendId
        );
        return count != null && count > 0;
    }

    public Optional<FriendStatus> getFriendStatus(Long userId, Long friendId) {
        try {
            String status = jdbc.queryForObject(
                    "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?",
                    String.class, userId, friendId
            );
            return Optional.of("confirmed".equalsIgnoreCase(status)
                    ? FriendStatus.CONFIRMED : FriendStatus.UNCONFIRMED);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Map<Long, FriendStatus> getFriends(Long userId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT friend_id, status FROM friendship WHERE user_id = ?", userId
        );
        Map<Long, FriendStatus> friends = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long friendId = ((Number) row.get("friend_id")).longValue();
            String status = (String) row.get("status");
            friends.put(friendId, "confirmed".equalsIgnoreCase(status)
                    ? FriendStatus.CONFIRMED : FriendStatus.UNCONFIRMED);
        }
        return friends;
    }
}