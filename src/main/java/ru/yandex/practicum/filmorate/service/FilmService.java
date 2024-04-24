package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film deleteLike(long filmId, long userId) {
        return filmStorage.deleteLike(filmId, userId);
    }

    public Film addLike(long filmId, long userId) {
        return filmStorage.addLike(filmId, userId);
    }

    public Collection<Film> mostLikeFilms(Integer count) {
        int size = getFilms().size();
        if ((count == null || count > size) && size >= 10) {
            count = 10;
        }

        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> Long.compare(f2.getLikes(), f1.getLikes()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film delete(Film film) {
        return filmStorage.delete(film);
    }
}
