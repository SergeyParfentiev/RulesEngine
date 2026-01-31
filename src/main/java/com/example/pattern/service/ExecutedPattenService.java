package com.example.pattern.service;

import com.example.market.common.data.Pair;
import com.example.pattern.model.ExecutedPattern;
import com.example.pattern.repository.ExecutedPatternRepository;
import com.example.pattern.view.ExecutedPatternView;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ExecutedPattenService {

    private final ExecutedPatternRepository repository;

    public void save(ExecutedPattern executedPattern) {
        repository.save(executedPattern);
    }

    @Transactional
    public void saveWithOverlapped(ExecutedPattern executedPattern, Set<Pair<Integer, Long>> notOverlappedPatternNumbersById) {
        executedPattern.addOverlaps(notOverlappedPatternNumbersById);
        repository.save(executedPattern);
    }

    public List<ExecutedPattern> findAll() {
        return repository.findAll();
    }

    public List<ExecutedPattern> findAll(Specification<ExecutedPattern> spec, Sort sort) {
        return repository.findAll(spec, sort);
    }

    public List<ExecutedPatternView> findAll(Specification<ExecutedPattern> spec) {
        return repository.findBy(spec, query -> query.as(ExecutedPatternView.class).all());
    }
}
