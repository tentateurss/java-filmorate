package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testCreateAndFindUserById() {
        User user = User.builder()
                .email("test@test.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User created = userStorage.createUser(user);

        Optional<User> found = userStorage.getUserById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
        assertThat(found.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testGetAllUsers() {
        User user1 = userStorage.createUser(User.builder()
                .email("user1@test.com").login("user1").name("User One")
                .birthday(LocalDate.of(2000, 1, 1)).build());

        User user2 = userStorage.createUser(User.builder()
                .email("user2@test.com").login("user2").name("User Two")
                .birthday(LocalDate.of(1995, 5, 5)).build());

        Collection<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testUpdateUser() {
        User user = userStorage.createUser(User.builder()
                .email("old@test.com").login("oldlogin").name("Old Name")
                .birthday(LocalDate.of(2000, 1, 1)).build());

        user.setEmail("new@test.com");
        user.setName("New Name");
        userStorage.updateUser(user);

        Optional<User> updated = userStorage.getUserById(user.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEmail()).isEqualTo("new@test.com");
        assertThat(updated.get().getName()).isEqualTo("New Name");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testDeleteUser() {
        User user = userStorage.createUser(User.builder()
                .email("delete@test.com").login("deletelogin").name("Delete Me")
                .birthday(LocalDate.of(2000, 1, 1)).build());

        userStorage.deleteUser(user.getId());

        Optional<User> found = userStorage.getUserById(user.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testEmailExists() {
        userStorage.createUser(User.builder()
                .email("exists@test.com").login("existslogin").name("Exists")
                .birthday(LocalDate.of(2000, 1, 1)).build());

        assertThat(userStorage.emailExists("exists@test.com")).isTrue();
        assertThat(userStorage.emailExists("nonexistent@test.com")).isFalse();
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void testFindUserByIdNotFound() {
        Optional<User> found = userStorage.getUserById(999L);
        assertThat(found).isEmpty();
    }
}