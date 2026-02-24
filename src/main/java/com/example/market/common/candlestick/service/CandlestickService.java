package com.example.market.common.candlestick.service;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.candlestick.repository.AbstractCandlestickRepository;
import com.example.market.common.data.Market;
import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class CandlestickService {

    private final Map<Pair<Market, Symbol>, AbstractCandlestickRepository<Candlestick>> candlestickRepositories;

    public void save(Market market, Symbol symbol, Consumer<Candlestick> filledConsumer) {
        var repository = repository(market, symbol);

        Candlestick candlestick = repository.candlestickInstance();
        filledConsumer.accept(candlestick);

        repository.save(candlestick);
    }

    public List<Candlestick> inRange(Market market, Symbol symbol, long start, long end) {
        return repository(market, symbol).inRange(start, end);
    }

    private AbstractCandlestickRepository<Candlestick> repository(Market market, Symbol symbol) {
        return candlestickRepositories.get(new Pair<>(market, symbol));
    }
}
