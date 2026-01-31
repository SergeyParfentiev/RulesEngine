package com.example.strategy.service;

import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import com.example.strategy.model.Strategy;
import com.example.strategy.repository.StrategyRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public List<Strategy> findAll(Market market, Symbol symbol, TimeInterval timeInterval) {
        return repository.findAllByMarketAndSymbolAndTimeInterval(market, symbol, timeInterval);
    }
}
