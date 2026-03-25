package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"name", "releaseDate"})
@Builder
public class Film {
    private Long id;
    private String name;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate; // собственная аннотация ValidReleaseDate будет тут

    private Integer duration;
}