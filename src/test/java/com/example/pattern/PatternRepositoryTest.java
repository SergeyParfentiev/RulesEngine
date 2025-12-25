package com.example.pattern;

import com.example.pattern.model.Pattern;
import com.example.pattern.model.PatternId;
import com.example.pattern.repository.PatternRepository;
import com.example.pattern.repository.specification.PatternSpecifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class PatternRepositoryTest {

    @Autowired
    private PatternRepository repository;

    @Test
    public void saveTest() {
        PatternId patternId = new PatternId(1L, 1);
        String name = "TestPattern";
        BigDecimal price = BigDecimal.ZERO;
        Pattern pattern = new Pattern(patternId, name, null, price);

        repository.save(pattern);

        Optional<Pattern> savedPattern = repository.findById(patternId);

        assertThat(savedPattern).isPresent();
        assertEquals(name, savedPattern.get().name());
        assertEquals(price, savedPattern.get().price());
    }

    private static final Object[] ALL_CORRECT = {1L, 1, BigDecimal.TEN, "price", BigDecimal.TEN, ">= * 1", true};
    private static final Object[] ALL_CORRECT_2 = {1L, 1, BigDecimal.TEN, "price", BigDecimal.TEN, "<= * 1", true};
    private static final Object[] WRONG_ID = {1L, 2, BigDecimal.TEN, "price", BigDecimal.TEN, ">= * 1", false};
    private static final Object[] WRONG_STRATEGY_ID = {2L, 1, BigDecimal.TEN, "price", BigDecimal.TEN, ">= * 1", false};
    private static final Object[] WRONG_BIGGER_CONDITION = {1L, 1, BigDecimal.TEN, "price", BigDecimal.TEN, "> * 1", false};
    private static final Object[] WRONG_LOWER_CONDITION = {1L, 1, BigDecimal.TEN, "price", BigDecimal.TEN, "> * 1", false};

    /* step = 1 */
    BigDecimal step = BigDecimal.ONE;

    private static final Object[] CORRECT_EQUALS_STEP_CONDITION =
            {1L, 1, BigDecimal.TEN, "price", BigDecimal.ONE, "== + step * 9", true};
    private static final Object[] CORRECT_BIGGER_STEP_CONDITION =
            {1L, 1, BigDecimal.TEN, "price", new BigDecimal("4"), "> + step * 5", true};
    private static final Object[] WRONG_LOWER_STEP_CONDITION =
            {1L, 1, BigDecimal.TEN, "price", new BigDecimal("4"), "< + step * 2", false};

    @ParameterizedTest
    @MethodSource("dependencyTestArguments")
    public void dependencyTest(long strategyId, int id, BigDecimal firstPrice, String field, BigDecimal secondPrice,
                               String condition, boolean result) {
        PatternId patternId = new PatternId(1L, 1);
        String name = "TestPattern";
        Pattern pattern = new Pattern(patternId, name, null, firstPrice);

        repository.save(pattern);

        Specification<Pattern> spec = Specification
                .where(PatternSpecifications.strategyIdAndId(strategyId, id)
                        .and(PatternSpecifications.priceCondition(field, condition, secondPrice, step)));
        Optional<Pattern> savedPattern = repository.findOne(spec);

        assertEquals(result, savedPattern.isPresent());
    }

    private static Stream<Arguments> dependencyTestArguments() {
        return Stream.of(
                Arguments.of(ALL_CORRECT),
                Arguments.of(ALL_CORRECT_2),
                Arguments.of(WRONG_ID),
                Arguments.of(WRONG_STRATEGY_ID),
                Arguments.of(WRONG_BIGGER_CONDITION),
                Arguments.of(WRONG_LOWER_CONDITION),

                /* with step */
                Arguments.of(CORRECT_EQUALS_STEP_CONDITION),
                Arguments.of(CORRECT_BIGGER_STEP_CONDITION),
                Arguments.of(WRONG_LOWER_STEP_CONDITION)
        );
    }

    private static final Object[] WRONG_COMPARISON_CONDITION = {"price", "! + step * 2",
            InvalidDataAccessApiUsageException.class, IllegalArgumentException.class, "Unexpected comparison: ! + step * 2"};
    private static final Object[] WRONG_FIRST_OPERATOR_CONDITION = {"price", "> & step * 2",
            IllegalArgumentException.class, IllegalArgumentException.class, "Unexpected condition: > & step * 2"};
    private static final Object[] WRONG_SECOND_OPERATOR_CONDITION = {"price", "< + step ( 2",
            IllegalArgumentException.class, IllegalArgumentException.class, "Unexpected condition: < + step ( 2"};

    @ParameterizedTest
    @MethodSource("dependencyExceptionTestArguments")
    public void dependencyExceptionTest(String field, String condition, Class<Throwable> expectedExceptionClass,
                                        Class<Throwable> expectedCauseClass, String expectedCauseMessage) {
        long strategyId = 1L;
        int id = 1;
        PatternId patternId = new PatternId(strategyId, id);
        String name = "TestPattern";
        Pattern pattern = new Pattern(patternId, name, null, BigDecimal.TEN);

        repository.save(pattern);

        Throwable exception = assertThrows(expectedExceptionClass, () -> {
            Specification<Pattern> spec = Specification
                    .where(PatternSpecifications.strategyIdAndId(strategyId, id)
                            .and(PatternSpecifications.priceCondition(field, condition, BigDecimal.ONE, step)));

            repository.findOne(spec);
        });

        Throwable cause = exception.getCause() == null ? exception : exception.getCause();

        assertEquals(expectedCauseClass, cause.getClass());
        assertEquals(expectedCauseMessage, cause.getMessage());
    }

    private static Stream<Arguments> dependencyExceptionTestArguments() {
        return Stream.of(
                Arguments.of(WRONG_COMPARISON_CONDITION),
                Arguments.of(WRONG_FIRST_OPERATOR_CONDITION),
                Arguments.of(WRONG_SECOND_OPERATOR_CONDITION)
        );
    }
}
