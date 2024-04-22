package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> getUsers() {
        String sqlQuery = "SELECT * FROM users;";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User create(User user) {
        validate(user);
        String sqlQuery = "INSERT INTO users (login, name, email, birthday) VALUES (?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setTimestamp(4, Timestamp.valueOf(user.getBirthday().atStartOfDay()));
            return statement;
        }, keyHolder);

        log.debug("Создание пользователя - " + user);

        long id = keyHolder.getKey().longValue();
        return getUserById(id);
    }

    @Override
    public User update(User user) {
        log.debug(user.toString());
        String sqlQuery = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());

        log.debug("Обновление пользователя - " + user);

        return getUserById(user.getId());
    }

    @Override
    public User delete(User user) {
        String sqlQueryFriends = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
        String sqlQueryLikedFilms = "DELETE FROM likes WHERE user_id = ?";
        String sqlQueryUsers = "DELETE FROM users WHERE id = ?;";
        log.debug("Удаление пользователя - " + user);
        jdbcTemplate.update(sqlQueryFriends, user.getId(), user.getId());
        jdbcTemplate.update(sqlQueryLikedFilms, user.getId());
        jdbcTemplate.update(sqlQueryUsers, user.getId());
        return user;
    }

    @Override
    public User getUserById(long id) {
        try {
            String sqlQuery = "SELECT * FROM users WHERE id = ?;";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            log.error(String.valueOf(id), e);
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

    @Override
    public User addFriend(long userId, long friendId) {
        if (getUserById(userId) == null) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(userId), e);
            throw e;
        } else if (getUserById(friendId) == null) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(friendId), e);
            throw e;
        } else {
            String sqlDelFriend = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?;";
            jdbcTemplate.update(sqlDelFriend, userId, friendId);
            String sqlQuery = "INSERT INTO friends (user_id, friend_id, status) " +
                    "VALUES (?, ?, ?);";
            jdbcTemplate.update(sqlQuery, userId, friendId, "Accept");
            return getUserById(userId);
        }
    }

    @Override
    public User deleteFriend(long userId, long friendId) {
        if (getUserById(userId) == null) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(userId), e);
            throw e;
        } else if (getUserById(friendId) == null) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(friendId), e);
            throw e;
        } else {
            String sqlQuery = "DELETE FROM friends WHERE user_id = ?";
            jdbcTemplate.update(sqlQuery, userId);
            return getUserById(userId);
        }
    }

    private Set<Long> findUserFriends(long id) {
        Set<Long> friends = new HashSet<>();
        String sqlQuery = "SELECT friend_id, status FROM friends WHERE user_id = ?;";
        SqlRowSet friendsRow = jdbcTemplate.queryForRowSet(sqlQuery, id);

        if (friendsRow.next()) {
            friends.add(friendsRow.getLong("friend_id"));
        }

        return friends;
    }

    private Set<Long> findLikedFilms(long id) {
        Set<Long> likedFilms = new HashSet<>();
        String sqlQuery = "SELECT film_id FROM likes WHERE user_id = ?;";
        SqlRowSet filmsRow = jdbcTemplate.queryForRowSet(sqlQuery, id);

        if (filmsRow.next()) {
            likedFilms.add(filmsRow.getLong("film_id"));
        }

        return likedFilms;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        long id = resultSet.getLong("id");
        String email = resultSet.getString("email");
        String login = resultSet.getString("login");
        String name = resultSet.getString("name");
        LocalDate birthday = resultSet.getDate("birthday").toLocalDate();
        User user = new User(email, login, birthday);

        if (name != null) {
            user.setName(resultSet.getString("name"));
        }
        user.setFriends(findUserFriends(id));
        user.setLikedFilms(findLikedFilms(id));
        user.setId(id);
        return user;
    }

    private void validate(User user) {
        if (user.getLogin().contains(" ")) {
            ValidException e = new ValidException("Логин содержит пробелы");
            log.error(user.toString(), e);
            throw e;
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
