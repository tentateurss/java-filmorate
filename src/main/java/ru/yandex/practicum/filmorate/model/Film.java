package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"name", "releaseDate"})
@Builder
public class Film {
    private Long id;
    private String name;
    private String description;

    @Builder.Default
    private List<Genre> genres = new ArrayList<>();
    private Mpa mpa;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate;
    private Integer duration;

    @Builder.Default
    private Set<Long> likes = new HashSet<>();
}