package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private int id = 1;
    private final HashMap<Integer, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User create(User user) {
        checkLoginAndName(user);
        user.setId(id);
        this.id++;
        users.put(user.getId(), user);
        log.debug("Создание пользователя - " + user);
        return user;
    }

    @Override
    public User update(User user) {
        log.debug(user.toString());

        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.debug("Обновление пользователя - " + user);
            return user;
        } else {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(user.toString(), e);
            throw e;
        }
    }

    @Override
    public User delete(User user) {
        if ((Integer) user.getId() == null || !users.containsKey(user.getId())) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(user.toString(), e);
            throw e;
        }
        users.remove(user.getId());
        log.debug("Удаление пользователя - " + user);
        return user;
    }

    private void checkLoginAndName(User user) {
        if (user.getLogin().contains(" ")) {
            ValidationException e = new ValidationException("Логин содержит пробелы");
            log.error(user.toString(), e);
            throw e;
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User getUserById(int id) {
        if (!users.containsKey(id)) {
            UserNotFoundException e = new UserNotFoundException("Пользователь не найден");
            log.error(String.valueOf(id), e);
            throw e;
        }

        return users.get(id);
    }
}
