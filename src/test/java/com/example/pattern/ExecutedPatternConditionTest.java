package com.example.pattern;

import com.example.AbstractCleanDBTest;
import com.example.market.common.candlestick.model.Candlestick;
import com.example.pattern.condition.parser.PatternConditionExecutor;
import com.example.pattern.data.PatternData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static com.example.filler.CandlestickFiller.fill;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutedPatternConditionTest extends AbstractCleanDBTest {

	@Autowired
	public ObjectMapper mapper;

	@Autowired
	public PatternConditionExecutor evaluator;

	private static final List<Candlestick> candlesticks = List.of(
			fill(1525421700000L, 10.5, 0.95001000, 0.91020000, 25.1, 171304.5600000000, 1525421759999L, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000),
			fill(1525421760000L, 5, 0.92981000, 0.91020000, 40, 62126.1600000000, 1525421819999L, 57208.8847960000, 139, 39303.3500000000, 36393.6817856000),
			fill(1525421820000L, 12.3, 0.92977000, 0.91400000, 18.7, 162886.5000000000, 1525421879999L, 149223.8808422000, 352, 29658.9200000000, 27210.1669172000));

	private static final String JSON_FIRST_PART = """
		{
		  "condition": "%s",
		  "events": []
		}
		""";

	private static final Object[] PARENTHESIS_EQUALS_CONDITION = {"((8 / (3 - 1)) * (7 + (5 + 6))) == 72", true};
	private static final Object[] PARENTHESIS_LESS_CONDITION = {"((3 - 1) * (5 + 6)) < 8", false};
	private static final Object[] PARAMETERS_CONDITION = {"(E0.closePrice - E2.openPrice) == 8.2", true};
	private static final Object[] PARAMETER_AND_VALUES_CONDITION =
		{"(E0.closePrice - 18.7) == 0", true};
	private static final Object[] FUNCTION_BOOLEAN_CONDITION = {"bool.test(true)", true};
	private static final Object[] FUNCTION_AND_VALUES_CONDITION =
		{"math.absValue(E0.closePrice - E2.openPrice) < 8.3", true};
	private static final Object[] FUNCTION_AND_PARENTHESIS_VALUES_CONDITION =
		{"math.absValue(((E0.closePrice - E2.openPrice) / 2) * (8 + 4)) >= 49.2", true};
	private static final Object[] FUNCTIONS_CONDITION =
		{"math.absValue(E0.closePrice - E2.openPrice) < weather.temperatureDifference(E1.closePrice, E2.openPrice))", true};
	private static final Object[] OR_CONDITION =
		{"(" + PARAMETERS_CONDITION[0] + ") OR weather.isCold()", true};
	private static final Object[] AND_CONDITION =
		{"(" + PARAMETERS_CONDITION[0] + ") AND weather.isCold()", false};
	private static final Object[] AND_BOOLEAN_CONDITION =
		{"(" + PARAMETERS_CONDITION[0] + ") AND true", true};
	private static final Object[] COMPLEX_CONDITION =
		{"((E2.closePrice - E1.openPrice) > weather.temperatureDifference(E1.closePrice, E2.openPrice)) " +
			"AND (weather.isCold() OR weather.isBiggerTemperature(E0.openPrice, 10)) " +
			"AND (time.isEvenSecond() AND time.isLaterThan('2025-01-10T10:15:30')) " +
			"AND (math.isPrime(E1.openPrice + 2) OR (math.absValue(E0.closePrice - E2.openPrice) < 100))", false};
	private static final Object[] NOT_TRUE_BOOLEAN = {"NOT true", false};
	private static final Object[] NOT_FALSE_BOOLEAN = {"NOT false", true};
	private static final Object[] NOT_FUNCTION_BOOLEAN = {"NOT weather.isCold()", true};
	private static final Object[] AND_NOT_FUNCTION_BOOLEAN =
		{"weather.isCold() AND NOT weather.isCold()", false};

	@ParameterizedTest
	@MethodSource("arguments")
	public void testCondition(String patternCondition, boolean result) throws Exception {
		String json = String.format(JSON_FIRST_PART, patternCondition);
		PatternData req = mapper.readValue(json, PatternData.class);

		assertEquals(result, evaluator.execute(candlesticks, req.condition()));
	}

	private static Stream<Arguments> arguments() {
		return Stream.of(
			Arguments.of(PARENTHESIS_EQUALS_CONDITION),
			Arguments.of(PARENTHESIS_LESS_CONDITION),
			Arguments.of(PARAMETERS_CONDITION),
			Arguments.of(PARAMETER_AND_VALUES_CONDITION),
			Arguments.of(FUNCTION_BOOLEAN_CONDITION),
			Arguments.of(FUNCTION_AND_VALUES_CONDITION),
			Arguments.of(FUNCTION_AND_PARENTHESIS_VALUES_CONDITION),
			Arguments.of(FUNCTIONS_CONDITION),
			Arguments.of(OR_CONDITION),
			Arguments.of(AND_CONDITION),
			Arguments.of(AND_BOOLEAN_CONDITION),
			Arguments.of(COMPLEX_CONDITION),
			Arguments.of(NOT_TRUE_BOOLEAN),
			Arguments.of(NOT_FALSE_BOOLEAN),
			Arguments.of(NOT_FUNCTION_BOOLEAN),
			Arguments.of(AND_NOT_FUNCTION_BOOLEAN)
//			Arguments.of(FUNCTION_AND_PARENTHESIS_VALUES_CONDITION)
		);
	}
}
