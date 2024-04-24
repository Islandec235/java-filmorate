package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NotNull
public class User {
    private long id;
    @Email(message = "Некорректный Email")
    @NotBlank(message = "Email не может быть пустым")
    @NonNull
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    @NonNull
    private String login;
    private String name;
    @PastOrPresent(message = "День рождения не может быть в будущем")
    @NonNull
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
    private Set<Long> likedFilms = new HashSet<>();
}
