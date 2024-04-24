package ru.yandex.practicum.filmorate.model;


import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@NotNull
public class Film {
    private Long id;
    @NonNull
    @NotBlank(message = "Название фильма не может быть пустым")
    private final String name;
    @NonNull
    @Size(max = 200, message = "Максимальная длина описания 200")
    private final String description;
    @NonNull
    private final LocalDate releaseDate;
    @Positive(message = "Длительность фильма меньше или равна нулю")
    @NonNull
    private final int duration;
    @PositiveOrZero
    private Long likes;
    private List<Genre> genres;
    private Mpa mpa;
}
