package com.example.strategy.service;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.candlestick.repository.AbstractCandlestickRepository;
import com.example.market.common.candlestick.service.CandlestickService;
import com.example.market.common.data.Market;
import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import com.example.pattern.condition.parser.PatternConditionExecutor;
import com.example.pattern.data.PatternData;
import com.example.pattern.event.parser.PatternEventsExecutor;
import com.example.pattern.model.ExecutedPattern;
import com.example.pattern.model.ExecutedPatternId;
import com.example.pattern.service.ExecutedPattenService;
import com.example.pattern.service.view.ExecutedPattenViewService;
import com.example.pattern.view.ExecutedPatternView;
import com.example.strategy.data.StrategyData;
import com.example.strategy.model.Strategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@AllArgsConstructor
public class StrategyProcessService {

    private final ObjectMapper mapper;
    private final StrategyService service;
    private final PatternEventsExecutor patternEventsExecutor;
    private final PatternConditionExecutor patternConditionExecutor;
    private final ExecutedPattenService executedPattenService;
    private final ExecutedPattenViewService executedPattenViewService;
    private final Map<Pair<Market, Symbol>, AbstractCandlestickRepository<? extends Candlestick>> candlestickRepositories;

    public <T extends Candlestick> void process(T currentCandlestick, List<T> previousCandlesticks, TimeInterval timeInterval) {
// TODO: 1/27/2026 add TimeInterval variation
        List<Candlestick> candlesticks = new ArrayList<>(previousCandlesticks);
        candlesticks.add(currentCandlestick);

        for (Strategy strategy : service.findAll(currentCandlestick.market(), currentCandlestick.symbol(), timeInterval)) {
            // TODO: 1/2/2026 do from ExecutedPatternDependencyTest test method
            try {
                StrategyData strategyData = mapper.readValue(strategy.json(), StrategyData.class);
                processPatterns(strategy, strategyData, candlesticks, currentCandlestick.closePrice());
            } catch (JsonProcessingException e) {
                // TODO: 1/2/2026 should save exception id to DB and notify admin and user(optional)
                e.printStackTrace();
            } catch (Exception e) {
                // TODO: 1/5/2026 something wrong with pattern handling, do like in previous exception
                e.printStackTrace();
            }
        }
    }

    private void processPatterns(Strategy strategy, StrategyData strategyData,
                                 List<Candlestick> candlesticks, BigDecimal currentPrice) throws Exception {
        for (Map.Entry<Integer, PatternData> patternDataEntry : strategyData.patterns().entrySet()) {
            if (patternConditionExecutor.execute(candlesticks, patternDataEntry.getValue().condition())) {
                saveExecutedPattern(candlesticks, patternDataEntry.getValue(),
                        strategy, patternDataEntry.getKey(), currentPrice);
            }
        }
    }

    private void saveExecutedPattern(List<Candlestick> candlesticks, PatternData patternData,
                                     Strategy strategy, int pattenId, BigDecimal currentPrice) throws Exception {
        if (patternData.dependency() == null) {
            executedPattenService.save(new ExecutedPattern(
                    new ExecutedPatternId(strategy.id(), pattenId, System.currentTimeMillis()),
                    patternData.name(), currentPrice));
            patternEventsExecutor.execute(candlesticks, patternData.events());
        } else {
            if (patternData.dependency().patterns() != null && !patternData.dependency().patterns().isEmpty()) {
                List<ExecutedPatternView> notOverlappedPatterns = executedPattenViewService.findNotOverlappedPatterns(
                        strategy.id(), patternData.dependency(), currentPrice, strategy.symbol());

                Set<Pair<Integer, Long>> notOverlappedPatternNumbersById = new HashSet<>();

                for (Integer patternId : patternData.dependency().patterns().keySet()) {
                    for (ExecutedPatternView notOverlappedPattern : notOverlappedPatterns) {
                        if (patternId.equals(notOverlappedPattern.id())) {
                            notOverlappedPatternNumbersById.add(new Pair<>(patternId, notOverlappedPattern.number()));
                        }
                    }
                }

                if (notOverlappedPatternNumbersById.size() == patternData.dependency().patterns().keySet().size()) {
                    executedPattenService.saveWithOverlapped(new ExecutedPattern(
                                    new ExecutedPatternId(strategy.id(), pattenId, System.currentTimeMillis()),
                                    patternData.name(), currentPrice),
                            notOverlappedPatternNumbersById);
                    patternEventsExecutor.execute(candlesticks, patternData.events());
                }
            }

//            String dependencyKey = patternData.dependency().keySet().iterator().next();
//            String[] dependencyKeyParts = dependencyKey.split("\\.");
//
//            if ("patterns".equals(dependencyKeyParts[0])) {
//                int patternId = Integer.parseInt(dependencyKeyParts[1]);
//                Map<String, String> fieldAndCondition = patternData.dependency().get(dependencyKey);
//
//
//                Specification<ExecutedPattern> orSpec = fieldAndCondition.entrySet().stream()
//                        .map((entry) -> PatternSpecifications.priceCondition(
//                                entry.getKey(), entry.getValue(), currentPrice, strategy.symbol().priceStep()))
//                .reduce(Specification.where(null), Specification::or);
//
//                Specification<ExecutedPattern> executedPatternSpec =
//                        Specification.where(strategyIdAndId(strategy.id(), patternId).and(orSpec));
//
//                long patternExecutedCount = executedPattenService.count(executedPatternSpec);
//
//                if (fieldAndCondition.entrySet().size() == patternExecutedCount) {
//                    // TODO: 1/3/2026 understand how to know that depended pattern instance already used by another pattern instance
//                }
//
//                String field = fieldAndCondition.keySet().iterator().next();
//                String condition = fieldAndCondition.get(field);
//
//                Specification<ExecutedPattern> spec = Specification.where(
//                        strategyIdAndId(strategy.id(), patternId)
//                                .and(PatternSpecifications.priceCondition(
//                                        field, condition, secondPatternPrice, step)));
//                Optional<ExecutedPattern> savedDependencyPattern = repository.findOne(spec);
//
//                savedDependencyPattern.ifPresent(executedPattern -> repository.save(new ExecutedPattern(
//                        new ExecutedPatternId(strategy.id(), patternDataEntry.getKey()),
//                        "SecondPattern", executedPattern.id().id(), secondPatternPrice)));
//            }
        }
    }

    private AbstractCandlestickRepository<? extends Candlestick> repository(Market market, Symbol symbol) {
        return candlestickRepositories.get(new Pair<>(market, symbol));
    }
}
