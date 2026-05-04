package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {

    private static final String FIND_ALL = "SELECT * FROM genre ORDER BY genre_id";
    private static final String FIND_BY_ID = "SELECT * FROM genre WHERE genre_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }
}