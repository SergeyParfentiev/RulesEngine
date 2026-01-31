package com.example.pattern.service.view;

import com.example.market.common.data.Symbol;
import com.example.pattern.data.PatternDependencyData;
import com.example.pattern.repository.view.ExecutedPatternViewRepository;
import com.example.pattern.view.ExecutedPatternView;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class ExecutedPattenViewService {

    private final ExecutedPatternViewRepository repository;

    public List<ExecutedPatternView> findNotOverlappedPatterns(Long strategyId, PatternDependencyData dependency,
                                                               BigDecimal currentPrice, Symbol symbol) {
        return repository.findNotOverlappedPatterns(strategyId, dependency, currentPrice, symbol);
    }
}
