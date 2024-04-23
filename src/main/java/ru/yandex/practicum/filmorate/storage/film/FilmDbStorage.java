package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public Collection<Film> getFilms() {
        String sqlQuery = "SELECT id, " +
                "name, " +
                "description, " +
                "release_date, " +
                "duration, " +
                "mpa_id " +
                "FROM films;";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film create(Film film) {
        validate(film);
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_id)" +
                " VALUES (?, ?, ?, ?, ?);";

        try {
            mpaStorage.getMpaById(film.getMpa().getId());
        } catch (RatingNotFoundException e) {
            log.error(String.valueOf(film.getMpa()), e);
            throw new ValidException("Неверный id рейтинга");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (mpaStorage.getMpaById(film.getMpa().getId()) != null) {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
                statement.setString(1, film.getName());
                statement.setString(2, film.getDescription());
                statement.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
                statement.setInt(4, film.getDuration());
                statement.setInt(5, film.getMpa().getId());
                return statement;
            }, keyHolder);
        }
        long id = keyHolder.getKey().longValue();

        if (film.getGenres() != null) {
            String sqlQueryUpdateGenre = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?);";
            String sqlQuerySelectGenre = "SELECT film_id FROM film_genre WHERE film_id = ? AND genre_id = ?;";
            for (Genre genre : film.getGenres()) {
                try {
                    genreStorage.getGenreById(genre.getId());
                } catch (GenreNotFoundException e) {
                    log.error(String.valueOf(genre), e);
                    throw new ValidException("Неверный id жанра");
                }

                if (jdbcTemplate.query(sqlQuerySelectGenre, (resSet, rowNum) ->
                        resSet.getInt("film_id"), id, genre.getId()).isEmpty()) {
                    jdbcTemplate.update(sqlQueryUpdateGenre, id, genre.getId());
                }
            }
        }

        log.debug("Создание фильма - {}", film);

        return getFilmById(id);

    }

    @Override
    public Film update(Film film) {
        validate(film);
        String sqlQuery = "UPDATE films SET " +
                "name = ?, " +
                "description = ?, " +
                "release_date = ?," +
                "duration = ? WHERE id = ?;";

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        log.debug("Обновление фильма - {}", film);

        return getFilmById(film.getId());
    }

    @Override
    public Film delete(Film film) {
        String sqlQueryFilmGenre = "DELETE FROM film_genre WHERE film_id = ?";
        String sqlQueryLikedFilms = "DELETE FROM likes WHERE film_id = ?";
        String sqlQueryUsers = "DELETE FROM films WHERE id = ?;";
        jdbcTemplate.update(sqlQueryFilmGenre, film.getId());
        jdbcTemplate.update(sqlQueryLikedFilms, film.getId());
        jdbcTemplate.update(sqlQueryUsers, film.getId());
        log.debug("Удаление фильма - {}", film);
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(long id) {
        try {
            String sqlQuery = "SELECT * FROM films WHERE id = ?;";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
        } catch (EmptyResultDataAccessException e) {
            log.error(String.valueOf(id), e);
            throw new FilmNotFoundException("Фильм не найден");
        }
    }

    @Override
    public Film addLike(long filmId, long userId) {
        String sqlQuery = "INSERT INTO likes (user_id, film_id) VALUES (?, ?);";
        jdbcTemplate.update(sqlQuery, userId, filmId);
        return getFilmById(filmId);
    }

    @Override
    public Film deleteLike(long filmId, long userId) {
        String sqlQuery = "DELETE FROM likes WHERE user_id = ? AND film_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, filmId);
        return getFilmById(filmId);
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        LocalDate releaseDate = resultSet.getDate("release_date").toLocalDate();
        int duration = resultSet.getInt("duration");
        Long likes = calculateLikes(id);
        List<Genre> genres = getGenresByFilm(id);
        Film film = new Film(name, description, releaseDate, duration);

        if (resultSet.getInt("mpa_id") != 0) {
            film.setMpa(mpaStorage.getMpaById(resultSet.getInt("mpa_id")));
        }

        film.setId(id);
        film.setGenres(genres);
        film.setLikes(likes);
        return film;
    }

    private Long calculateLikes(long filmId) {
        String sqlQuery = "SELECT COUNT(user_id) FROM likes WHERE film_id = ?;";
        return jdbcTemplate.queryForObject(sqlQuery, Long.class, filmId);
    }

    private List<Genre> getGenresByFilm(long filmId) {
        String sqlQuery = "SELECT genre_id FROM film_genre WHERE film_id = ? ORDER BY genre_id ASC";
        List<Genre> genres = new ArrayList<>();
        SqlRowSet genreRow = jdbcTemplate.queryForRowSet(sqlQuery, filmId);

        while (genreRow.next()) {
            genres.add(genreStorage.getGenreById(genreRow.getInt("genre_id")));
        }

        return genres;
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            ValidException e = new ValidException("Дата релиза фильма не может быть раньше 28.12.1895");
            log.error(film.toString(), e);
            throw e;
        }
    }
}
