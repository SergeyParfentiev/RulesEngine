package com.example.pattern;

import com.example.pattern.condition.parser.ConditionParserEvaluator;
import com.example.pattern.dto.PatternRequest;
import com.example.pattern.model.Element;
import com.example.pattern.model.Pattern;
import com.example.pattern.model.PatternId;
import com.example.pattern.repository.PatternRepository;
import com.example.pattern.repository.specification.PatternSpecifications;
import com.example.strategy.dto.StrategyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class PatternDependencyTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    public ConditionParserEvaluator conditionParserEvaluator;

    @Autowired
    private PatternRepository repository;

    private final List<Element> elements = List.of(
            new Element(BigDecimal.valueOf(10.5), BigDecimal.valueOf(25.1)),
            new Element(BigDecimal.valueOf(5), BigDecimal.valueOf(40)),
            new Element(BigDecimal.valueOf(12.3), BigDecimal.valueOf(18.7)));

    private static final String JSON = """
            {
              "elements": {
                "E0": { "first": 10.5, "second": 25.1 },
                "E1": { "first": 5.0,  "second": 40.0 },
                "E2": { "first": 12.3, "second": 18.7 }
              },
              "patterns": {
                "1": {
                  "condition": "true",
                  "events": ["printConsole.print()"]
                },
                "2": {
                  "condition": "true",
                  "events": ["printConsole.print()"],
                  "dependency": {
                    "patterns.1": {
                      "price": "== - step * 5"
                    }
                  }
                }
              }
            }
            """;

    @Test
    public void test() throws Exception {
        long strategyId = 1L;
        BigDecimal step = new BigDecimal("2");
        BigDecimal firstPatternPrice = BigDecimal.TEN;
        BigDecimal secondPatternPrice = new BigDecimal("20");

        StrategyRequest request = mapper.readValue(JSON, StrategyRequest.class);

        for (Map.Entry<Integer, PatternRequest> patternRequestEntry : request.patterns().entrySet()) {
            if (conditionParserEvaluator.evaluate(elements, patternRequestEntry.getValue().condition())) {
                if (patternRequestEntry.getValue().dependency() == null) {
                    repository.save(new Pattern(
                            new PatternId(strategyId, patternRequestEntry.getKey()),
                            "FirstPattern", null, firstPatternPrice));
                } else if (!patternRequestEntry.getValue().dependency().isEmpty()) {
                    String dependencyKey = patternRequestEntry.getValue().dependency().keySet().iterator().next();
                    String[] dependencyKeyParts = dependencyKey.split("\\.");

                    if ("patterns".equals(dependencyKeyParts[0])) {
                        int patternId = Integer.parseInt(dependencyKeyParts[1]);
                        Map<String, String> fieldAndCondition =
                                patternRequestEntry.getValue().dependency().get(dependencyKey);
                        String field = fieldAndCondition.keySet().iterator().next();
                        String condition = fieldAndCondition.get(field);

                        Specification<Pattern> spec = Specification
                                .where(PatternSpecifications.strategyIdAndId(strategyId, patternId)
                                        .and(PatternSpecifications.priceCondition(
                                                field, condition, secondPatternPrice, step)));
                        Optional<Pattern> savedDependencyPattern = repository.findOne(spec);

                        savedDependencyPattern.ifPresent(pattern -> repository.save(new Pattern(
                                new PatternId(strategyId, patternRequestEntry.getKey()),
                                "SecondPattern", pattern.id().id(), secondPatternPrice)));
                    }
                }
            }
        }

        List<Pattern> allPatterns = repository.findAll();

        assertEquals(2, allPatterns.size());
        assertNull(allPatterns.get(0).dependency());
        assertEquals(allPatterns.get(0).id().id(), allPatterns.get(1).dependency());
    }
}
