package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private int id = 1;
    private final HashMap<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        checkLoginAndName(user);
        user.setId(id);
        this.id++;
        users.put(user.getId(), user);
        log.debug("Создание пользователя - " + user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.debug(user.toString());
        checkLoginAndName(user);

        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.debug("Обновление пользователя - " + user);
            return user;
        } else {
            ValidationException e = new ValidationException("Пользователь не найден");
            log.error(user.toString(), e);
            throw e;
        }
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
}
