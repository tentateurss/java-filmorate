package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.enums.FriendStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"email"})
@Builder
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthday;

    @Builder.Default
    private Map<Long, FriendStatus> friends = new HashMap<>();
}