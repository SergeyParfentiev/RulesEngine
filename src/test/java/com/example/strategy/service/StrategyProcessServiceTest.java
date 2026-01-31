package com.example.strategy.service;

import com.example.AbstractCleanDBTest;
import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.data.Market;
import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import com.example.pattern.event.impl.PrintConsoleEvent;
import com.example.pattern.model.ExecutedPattern;
import com.example.pattern.model.ExecutedPatternId;
import com.example.pattern.model.ExecutedPatternOverlap;
import com.example.pattern.service.ExecutedPattenOverlapService;
import com.example.pattern.service.ExecutedPattenService;
import com.example.strategy.data.StrategyData;
import com.example.strategy.model.Strategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.filler.CandlestickFiller.fill;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StrategyProcessServiceTest extends AbstractCleanDBTest {

    @Autowired
    public ObjectMapper mapper;

    @Autowired
    public StrategyService strategyService;

    @SpyBean
    public PrintConsoleEvent printConsoleEvent;

    @Autowired
    public ExecutedPattenService executedPattenService;

    @Autowired
    public ExecutedPattenOverlapService executedPattenOverlapService;

    @Autowired
    public StrategyProcessService strategyProcessService;

    private static final String JSON_PATTERN_CONDITION = """
            {
              "name": "StrategyPatternConditionProcessTest",
              "market": "BINANCE",
              "symbol": "XRP_USDT",
              "timeInterval": "1m",
              "elements": {
                "E2": { "first": 10.5, "second": 25.1 },
                "E1": { "first": 10.5, "second": 25.1 },
                "E0": { "first": 5.0,  "second": 40.0 }
              },
              "patterns": {
                "1": {
                  "name": "FirstPattern",
                  "condition": "E0.openPrice == 12.3 AND E1.highPrice == 0.92981000 AND E2.lowPrice == 0.91020000",
                  "events": ["printConsole.printWithStringParameter('Hello There')"]
                }
              }
            }
            """;

    @Test
    public void testPatternWithoutDependency() throws JsonProcessingException {
        StrategyData strategyData = mapper.readValue(JSON_PATTERN_CONDITION, StrategyData.class);
        Strategy strategy = new Strategy(0, JSON_PATTERN_CONDITION, strategyData.name(),
                strategyData.market(), strategyData.symbol(), strategyData.timeInterval());
        strategyService.save(strategy);

        Candlestick currentCandlestick =
                fill(1525421820000L, 12.3, 0.92977000, 0.91400000, 18.7, 162886.5000000000, 1525421879999L, 149223.8808422000, 352, 29658.9200000000, 27210.1669172000);

        List<Candlestick> previousCandlesticks = List.of(
                fill(1525421700000L, 10.5, 0.95001000, 0.91020000, 25.1, 171304.5600000000, 1525421759999L, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000),
                fill(1525421760000L, 5, 0.92981000, 0.91020000, 40, 62126.1600000000, 1525421819999L, 57208.8847960000, 139, 39303.3500000000, 36393.6817856000)
        );

        strategyProcessService.process(currentCandlestick, previousCandlesticks, strategyData.timeInterval());

        List<ExecutedPattern> all = executedPattenService.findAll();
        assertEquals(1, all.size());

        verify(printConsoleEvent, times(1)).printWithStringParameter("Hello There");
    }

    private static final String JSON_PATTERN_DEPENDENCY = """
            {
              "name": "StrategyPatternDependencyProcessTest",
              "market": "BINANCE",
              "symbol": "XRP_USDT",
              "timeInterval": "1m",
              "elements": {
                "E2": { "first": 10.5, "second": 25.1 },
                "E1": { "first": 10.5, "second": 25.1 },
                "E0": { "first": 5.0,  "second": 40.0 }
              },
              "patterns": {
                "1": {
                  "name": "FirstPattern",
                  "condition": "%b",
                  "events": ["printConsole.print()"]
                },
                "2": {
                  "name": "SecondPattern",
                  "condition": "%b",
                  "events": ["printConsole.print()"]
                },
                "3": {
                  "name": "ThirdPattern",
                  "condition": "%b",
                  "events": ["printConsole.print()"],
                  "dependency": {
                    "patterns": {
                      "1": {
                        "price": "%s"
                      },
                      "2": {
                        "price": "%s"
                      }
                    }
                  }
                }
              }
            }
            """;

    //step = 0.0001

    private static final Object[] EQUALS_CORRECT_DEPENDENCY_PATTERN =
            {true, true, true, "== - step * 5", "== - step * 5", 10, 10.0005, 2, 5};
    private static final Object[] CORRECT_FIRST_EQUALS_DEPENDENCY_PATTERN =
            {true, true, true, "== - step * 5", "== + step * 5", 10, 10.0005, 2, 4};
    private static final Object[] CORRECT_SECOND_EQUALS_DEPENDENCY_PATTERN =
            {true, true, true, "== + step * 5", "== - step * 5", 10, 10.0005, 2, 4};
    private static final Object[] BIGGER_AND_LOWER_CORRECT_DEPENDENCY_PATTERN =
            {true, true, true, "> - 0.0006", "< - 0.0004", 10, 10.0005, 2, 5};
    private static final Object[] BIGGER_OR_EQUALS_AND_LOWER_AND_EQUALS_CORRECT_DEPENDENCY_PATTERN =
            {true, true, true, ">= - 0.0005", "<= - 0.0004", 10, 10.0005, 2, 5};
    private static final Object[] MULTIPLY_AND_DIVIDE_PATTERNS =
            {true, true, true, "< * 1", "> / 10", 10, 10.0005, 2, 5};
    private static final Object[] WRONG_DEPENDENCY_PATTERNS =
            {true, true, true, "== - step * 4", "== - step * 4", 10, 10.0005, 2, 4};


    @ParameterizedTest
    @MethodSource("testPatternDependencyArguments")
    public void testPatternDependency(boolean firstPatternCondition, boolean secondPatternCondition, boolean thirdPatternCondition,
                                      String thirdDependencyFirstPatternPrice, String thirdDependencySecondPatternPrice,
                                      double firstCurrentPrice, double secondCurrentPrice, int firstSize, int secondSize) throws JsonProcessingException {
        String json = JSON_PATTERN_DEPENDENCY.formatted(firstPatternCondition, secondPatternCondition, thirdPatternCondition,
                thirdDependencyFirstPatternPrice, thirdDependencySecondPatternPrice);

        StrategyData strategyData = mapper.readValue(json, StrategyData.class);
        Strategy strategy = new Strategy(0, json, strategyData.name(),
                strategyData.market(), strategyData.symbol(), strategyData.timeInterval());
        strategyService.save(strategy);

        process(firstCurrentPrice, strategyData.timeInterval());

        List<ExecutedPattern> firstAll = executedPattenService.findAll();
        assertEquals(firstSize, firstAll.size());

        process(secondCurrentPrice, strategyData.timeInterval());

        List<ExecutedPattern> secondAll = executedPattenService.findAll();
        assertEquals(secondSize, secondAll.size());

        assertEquals(firstAll.get(0), secondAll.get(0));
        assertEquals(firstAll.get(1), secondAll.get(1));
    }

    @Test
    public void testMinSave() throws JsonProcessingException {
        String json = JSON_PATTERN_DEPENDENCY.formatted(true, true, true, "== - step * 5", "== - step * 5");

        StrategyData strategyData = mapper.readValue(json, StrategyData.class);
        Strategy strategy = new Strategy(0, json, strategyData.name(),
                strategyData.market(), strategyData.symbol(), strategyData.timeInterval());
        strategy = strategyService.save(strategy);

        long firstMinPatternNumber = System.currentTimeMillis();
        long secondMinPatternNumber = firstMinPatternNumber + 1;

        long firstNextMinPatternNumber = secondMinPatternNumber + 1;
        long secondNextMinPatternNumber = firstNextMinPatternNumber + 1;

        executedPattenService.save(new ExecutedPattern(
                new ExecutedPatternId(strategy.id(), 1, firstMinPatternNumber),
                strategyData.patterns().get(1).name(), new BigDecimal(10)));
        executedPattenService.save(new ExecutedPattern(
                new ExecutedPatternId(strategy.id(), 2, secondMinPatternNumber),
                strategyData.patterns().get(2).name(), new BigDecimal(10)));

        executedPattenService.save(new ExecutedPattern(
                new ExecutedPatternId(strategy.id(), 1, firstNextMinPatternNumber),
                strategyData.patterns().get(1).name(), new BigDecimal(10)));
        executedPattenService.save(new ExecutedPattern(
                new ExecutedPatternId(strategy.id(), 2, secondNextMinPatternNumber),
                strategyData.patterns().get(2).name(), new BigDecimal(10)));

        executedPattenService.save(new ExecutedPattern(
                new ExecutedPatternId(strategy.id(), 1, secondNextMinPatternNumber + 1),
                strategyData.patterns().get(1).name(), new BigDecimal(10)));
        executedPattenService.save(new ExecutedPattern(
                new ExecutedPatternId(strategy.id(), 2, secondNextMinPatternNumber + 2),
                strategyData.patterns().get(2).name(), new BigDecimal(10)));

        long thirdMinPatternNumber = secondNextMinPatternNumber + 3;

        executedPattenService.saveWithOverlapped(new ExecutedPattern(
                        new ExecutedPatternId(strategy.id(), 3, thirdMinPatternNumber),
                        strategyData.patterns().get(3).name(), BigDecimal.valueOf(10.0005)),
                Set.of(new Pair<>(1, firstMinPatternNumber), new Pair<>(2, secondMinPatternNumber)));

        List<ExecutedPattern> patterns = executedPattenService.findAll();
        assertEquals(7, patterns.size());

        List<ExecutedPatternOverlap> patternOverlaps = executedPattenOverlapService.findAll();
        assertEquals(2, patternOverlaps.size());

        assertEquals(1, countByOverlappedNumber(patternOverlaps, firstMinPatternNumber));
        assertEquals(1, countByOverlappedNumber(patternOverlaps, secondMinPatternNumber));
        assertEquals(2, countWithOverlappedId(patternOverlaps, thirdMinPatternNumber));

        process(10.0005, strategyData.timeInterval());

        patterns = executedPattenService.findAll();
        assertEquals(10, patterns.size());

        patternOverlaps = executedPattenOverlapService.findAll();
        assertEquals(4, patternOverlaps.size());

        long thirdNextMinPatternNumber = patternOverlaps.stream()
                .max(Comparator.comparingLong(o -> o.id().patternId().number()))
                .map(overlap -> overlap.id().patternId().number())
                .orElseThrow();

        assertEquals(1, countByOverlappedNumber(patternOverlaps, firstMinPatternNumber));
        assertEquals(1, countByOverlappedNumber(patternOverlaps, secondMinPatternNumber));
        assertEquals(2, countWithOverlappedId(patternOverlaps, thirdMinPatternNumber));

        assertEquals(1, countByOverlappedNumber(patternOverlaps, firstNextMinPatternNumber));
        assertEquals(1, countByOverlappedNumber(patternOverlaps, secondNextMinPatternNumber));
        assertEquals(2, countWithOverlappedId(patternOverlaps, thirdNextMinPatternNumber));
    }

    private long countByOverlappedNumber(List<ExecutedPatternOverlap> patternOverlaps, long overlappedNumber) {
        return patternOverlaps.stream()
                .map(ExecutedPatternOverlap::id)
                .filter(id -> id.overlappedNumber() == overlappedNumber)
                .count();
    }

    private long countWithOverlappedId(List<ExecutedPatternOverlap> patternOverlaps, long patternNumber) {
        return patternOverlaps.stream()
                .filter(overlap -> overlap.id().patternId().number() == patternNumber)
                .count();
    }

    private void process(double currentPrice, TimeInterval timeInterval) {
        Candlestick currentCandlestick =
                fill(1525421820000L, 12.3, 0.92977000, 0.91400000, currentPrice, 162886.5000000000, 1525421879999L, 149223.8808422000, 352, 29658.9200000000, 27210.1669172000);

        List<Candlestick> previousCandlesticks = List.of(
                fill(1525421700000L, 10.5, 0.95001000, 0.91020000, 25.1, 171304.5600000000, 1525421759999L, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000),
                fill(1525421760000L, 5, 0.92981000, 0.91020000, 40, 62126.1600000000, 1525421819999L, 57208.8847960000, 139, 39303.3500000000, 36393.6817856000)
        );

        strategyProcessService.process(currentCandlestick, previousCandlesticks, timeInterval);
    }

    private static Stream<Arguments> testPatternDependencyArguments() {
        return Stream.of(
                Arguments.of(EQUALS_CORRECT_DEPENDENCY_PATTERN),
                Arguments.of(CORRECT_FIRST_EQUALS_DEPENDENCY_PATTERN),
                Arguments.of(CORRECT_SECOND_EQUALS_DEPENDENCY_PATTERN),
                Arguments.of(BIGGER_AND_LOWER_CORRECT_DEPENDENCY_PATTERN),
                Arguments.of(BIGGER_OR_EQUALS_AND_LOWER_AND_EQUALS_CORRECT_DEPENDENCY_PATTERN),
                Arguments.of(MULTIPLY_AND_DIVIDE_PATTERNS),
                Arguments.of(WRONG_DEPENDENCY_PATTERNS)
        );
    }
}
