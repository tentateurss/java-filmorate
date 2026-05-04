package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = null;
        Long mpaId = rs.getLong("rating_mpa_id");
        if (!rs.wasNull()) {
            mpa = new Mpa(mpaId, rs.getString("mpa_name"));
        }

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date") != null
                        ? rs.getDate("release_date").toLocalDate() : null)
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();
    }
}