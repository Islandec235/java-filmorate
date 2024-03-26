package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private int id = 1;
    private final HashMap<Integer, Film> films = new HashMap<>();

    public Collection<Film> getFilms() {
        return films.values();
    }


    public Film create(Film film) {
        checkReleaseDate(film);
        film.setId(id);
        this.id++;
        films.put(film.getId(), film);
        log.debug("Создание фильма - " + film);
        return film;
    }

    public Film update(Film film) {
        checkReleaseDate(film);

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.debug("Обновление фильма - " + film);
            return film;
        } else {
            FilmNotFoundException e = new FilmNotFoundException("Фильм не найден");
            log.error(film.toString(), e);
            throw e;
        }
    }

    public Film delete(Film film) {
        if ((Integer) film.getId() == null || !films.containsKey(film.getId())) {
            FilmNotFoundException e = new FilmNotFoundException("Фильм не найден");
            log.error(film.toString(), e);
            throw e;
        }

        films.remove(film.getId());
        log.debug("Удаление фильма - " + film);
        return film;
    }

    public Film getFilmById(int id) {
        if (!films.containsKey(id)) {
            FilmNotFoundException e = new FilmNotFoundException("Фильм не найден");
            log.error(String.valueOf(id), e);
            throw e;
        }

        return films.get(id);
    }

    private void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            ValidationException e = new ValidationException("Дата релиза фильма не может быть раньше 28.12.1895");
            log.error(film.toString(), e);
            throw e;
        }
    }
}
