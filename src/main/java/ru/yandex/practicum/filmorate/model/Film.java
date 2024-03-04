package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NotNull
public class Film {
    private int id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания 200")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Длительность фильма меньше или равна нулю")
    private int duration;
}
