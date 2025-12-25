package com.example.strategy.service;

import com.example.strategy.model.Strategy;
import com.example.strategy.repository.StrategyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StrategyService {

    private final StrategyRepository repository;

    public Strategy save(Strategy strategy) {
        return repository.save(strategy);
    }

    public Optional<Strategy> find(long id) {
        return repository.findById(id);
    }

    public List<Strategy> findAll() {
        return repository.findAll();
    }
}
