package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        User user = new User("user@email.ru", "vanya123", LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User createdUser = userStorage.create(user);
        user.setId(createdUser.getId());

        User findedUser = userStorage.getUserById(createdUser.getId());

        assertThat(findedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdUser);
    }

    @Test
    public void testUpdateUser() {
        User user = new User("user@email.ru", "vanya123", LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User createdUser = userStorage.create(user);

        User newUser = new User("user@yandex.ru", "Ivan123",
                LocalDate.of(1990, 1, 1));
        newUser.setId(createdUser.getId());
        newUser.setName(newUser.getLogin());
        User updateUser = userStorage.update(newUser);

        assertThat(updateUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);
    }

    @Test
    public void testDeleteUser() {
        User user = new User("user@email.ru", "vanya123",
                LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.create(user);

        User deleteUser = userStorage.delete(user);
        assertThat(deleteUser)
                .isNotNull()
                .isEqualTo(user);
    }

    @Test
    public void testGetUsers() {
        User user = new User("user@email.ru", "vanya123",
                LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User createdUser = userStorage.create(user);

        User anotherUser = new User("SecondUser@email.ru", "Tolik321",
                LocalDate.of(1995, 2, 2));

        User createdAnotherUser = userStorage.create(anotherUser);

        List<User> newUsers = (List<User>) userStorage.getUsers();

        assertThat(newUsers.get(0))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdUser);
        assertThat(newUsers.get(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdAnotherUser);
    }
}
