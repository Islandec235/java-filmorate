package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NotNull
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
