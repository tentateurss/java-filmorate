package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseDbStorage<Mpa> {

    private static final String FIND_ALL = "SELECT * FROM rating_mpa ORDER BY rating_mpa_id";
    private static final String FIND_BY_ID = "SELECT * FROM rating_mpa WHERE rating_mpa_id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Mpa> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Mpa> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }
}