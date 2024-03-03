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

import javax.validation.constraints.Email;
import java.time.LocalDate;
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
    public User create(@Email @RequestBody User user) {
        checkValidation(user);
        user.setId(id);
        this.id++;
        log.debug(user.toString());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Email @RequestBody User user) {
        log.debug(user.toString());
        checkValidation(user);

        if (users.containsKey(user.getId())) {
            log.debug(user.toString());
            users.put(user.getId(), user);
            return user;
        } else {
            ValidationException e = new ValidationException("Пользователь не найден");
            log.error(user.toString(), e);
            throw e;
        }
    }

    private void checkValidation(User user) {
        if (user.getEmail().isBlank()) {
            ValidationException e = new ValidationException("Email пользователя не может быть пустым");
            log.error(user.toString(), e);
            throw e;
        }

        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            ValidationException e = new ValidationException("Логин пустой или содержит пробелы");
            log.error(user.toString(), e);
            throw e;
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            ValidationException e = new ValidationException("Дата рождения не может быть в будущем");
            log.error(user.toString(), e);
            throw e;
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
