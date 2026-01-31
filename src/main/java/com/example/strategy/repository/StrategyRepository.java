package com.example.strategy.repository;

import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import com.example.strategy.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    List<Strategy> findAllByMarketAndSymbolAndTimeInterval(Market market, Symbol symbol, TimeInterval timeInterval);
}
