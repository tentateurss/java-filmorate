package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public List<Mpa> getAllMpa() {
        return mpaDbStorage.findAll();
    }

    public Mpa getMpaById(Long id) {
        return mpaDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + id + " не найден"));
    }
}