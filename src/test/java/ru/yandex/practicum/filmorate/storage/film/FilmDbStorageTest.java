package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindFilmById() {
        Film film = new Film("Час пик", "описание фильма Час Пик",
                LocalDate.of(1998, 9, 18), 98);
        film.setMpa(new Mpa(1, "G"));
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate,
                new MpaDbStorage(jdbcTemplate),
                new GenreDbStorage(jdbcTemplate));
        Film createdFilm = filmStorage.create(film);

        Film findedFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(findedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdFilm);
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film("Час пик", "описание фильма Час Пик",
                LocalDate.of(1998, 9, 18), 98);
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate,
                new MpaDbStorage(jdbcTemplate),
                new GenreDbStorage(jdbcTemplate));
        film.setMpa(new Mpa(1, "G"));
        Film createdFilm = filmStorage.create(film);



        Film newFilm = new Film("Пять оттенков жёлтого", "описание",
                LocalDate.of(2021, 3, 11), 9);
        newFilm.setId(createdFilm.getId());
        Film updateFilm = filmStorage.update(newFilm);
        newFilm.setGenres(createdFilm.getGenres());
        newFilm.setLikes(0L);
        newFilm.setMpa(film.getMpa());


        assertThat(updateFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newFilm);
    }

    @Test
    public void testDeleteFilm() {
        Film film = new Film("Час пик", "описание фильма Час Пик",
                LocalDate.of(1998, 9, 18), 98);
        film.setMpa(new Mpa(1, "Комедия"));
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate,
                new MpaDbStorage(jdbcTemplate),
                new GenreDbStorage(jdbcTemplate));
        Film createdFilm = filmStorage.create(film);

        assertThrows(FilmNotFoundException.class, () -> {
            filmStorage.delete(createdFilm);
        });

    }

    @Test
    public void testGetFilms() {
        Film film = new Film("Час пик", "описание фильма Час Пик",
                LocalDate.of(1998, 9, 18), 98);
        film.setMpa(new Mpa(1, "Комедия"));
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate,
                new MpaDbStorage(jdbcTemplate),
                new GenreDbStorage(jdbcTemplate));
        Film createdFilm = filmStorage.create(film);

        Film anotherFilm = new Film("Пять оттенков жёлтого", "описание",
                LocalDate.of(2021, 3, 11), 9);
        anotherFilm.setId(2L);
        anotherFilm.setMpa(new Mpa(1, "Комедия"));
        Film createdAnotherFilm = filmStorage.create(anotherFilm);

        List<Film> newFilms = (List<Film>) filmStorage.getFilms();

        assertThat(newFilms.get(0))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdFilm);
        assertThat(newFilms.get(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdAnotherFilm);
    }
}
