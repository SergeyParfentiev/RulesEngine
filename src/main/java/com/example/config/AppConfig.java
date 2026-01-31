package com.example.config;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.candlestick.repository.AbstractCandlestickRepository;
import com.example.market.common.data.Market;
import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Map<Pair<Market, Symbol>, AbstractCandlestickRepository<? extends Candlestick>> getCandlestickRepositoryMap(
            List<AbstractCandlestickRepository<? extends Candlestick>> repositories) {
        Map<Pair<Market, Symbol>, AbstractCandlestickRepository<? extends Candlestick>> repositoryMap = new HashMap<>();

        for (AbstractCandlestickRepository<? extends Candlestick> repository : repositories) {
            repositoryMap.put(new Pair<>(repository.market(), repository.symbol()), repository);
        }
        return repositoryMap;
    }
}
