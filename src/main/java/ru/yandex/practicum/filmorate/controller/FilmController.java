package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private int id = 1;
    private final HashMap<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        checkValidation(film);
        film.setId(id);
        this.id++;
        log.debug(film.toString());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        checkValidation(film);

        if (films.containsKey(film.getId())) {
            log.debug(film.toString());
            films.put(film.getId(), film);
            return film;
        } else {
            ValidationException e = new ValidationException("Фильм не найден");
            log.error(film.toString(), e);
            throw e;
        }
    }

    private void checkValidation(Film film) {
        if (film.getName().isBlank()) {
            ValidationException e = new ValidationException("Название фильма не может быть пустым");
            log.error(film.toString(), e);
            throw e;
        }

        if (film.getDescription().length() > 200) {
            ValidationException e = new ValidationException("Описание не может быть длиннее 200 символов."
                    + " Количество символов: " + film.getDescription().length());
            log.error(film.toString(), e);
            throw e;
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            ValidationException e = new ValidationException("Дата релиза фильма не может быть раньше 28.12.1895");
            log.error(film.toString(), e);
            throw e;
        }

        if (film.getDuration() <= 0) {
            ValidationException e = new ValidationException("Продолжительность фильма не может быть отрицательной "
                    + "или равной 0");
            log.error(film.toString(), e);
            throw e;
        }
    }
}
