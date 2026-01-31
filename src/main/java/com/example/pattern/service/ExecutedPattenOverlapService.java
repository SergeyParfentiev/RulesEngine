package com.example.pattern.service;

import com.example.pattern.model.ExecutedPatternOverlap;
import com.example.pattern.repository.ExecutedPatternOverlapRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExecutedPattenOverlapService {

    private final ExecutedPatternOverlapRepository repository;

    public List<ExecutedPatternOverlap> findAll() {
        return repository.findAll();
    }
}
