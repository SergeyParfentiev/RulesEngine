package com.example.pattern;

import com.example.pattern.condition.parser.ConditionParserEvaluator;
import com.example.pattern.dto.PatternRequest;
import com.example.pattern.model.Element;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PatternConditionTest {

	@Autowired
	public ObjectMapper mapper;

	@Autowired
	public ConditionParserEvaluator evaluator;

	private final List<Element> elements = List.of(
		new Element(BigDecimal.valueOf(10.5), BigDecimal.valueOf(25.1)),
		new Element(BigDecimal.valueOf(5), BigDecimal.valueOf(40)),
		new Element(BigDecimal.valueOf(12.3), BigDecimal.valueOf(18.7)));

	private static final String JSON_FIRST_PART = """
		{
		  "condition": "%s",
		  "events": []
		}
		""";

	private static final Object[] PARENTHESIS_CONDITION = {"((3 - 1) * (5 + 6)) < 8", false};
	private static final Object[] PARAMETERS_CONDITION = {"(E2.second - E0.first) == 8.2", true};
	private static final Object[] PARAMETER_AND_VALUES_CONDITION =
		{"(E2.second - 18.7) == 0", true};
	private static final Object[] FUNCTION_BOOLEAN_CONDITION = {"bool.test(true)", true};
	private static final Object[] FUNCTION_AND_VALUES_CONDITION =
		{"math.absValue(E2.second - E0.first) < 8.3", true};
	private static final Object[] FUNCTION_AND_PARENTHESIS_VALUES_CONDITION =
		{"math.absValue(((E2.second - E0.first) / 2) * (8 + 4)) >= 49.2", true};
	private static final Object[] FUNCTIONS_CONDITION =
		{"math.absValue(E2.second - E0.first) < weather.temperatureDifference(E1.second, E0.first))", true};
	private static final Object[] OR_CONDITION =
		{"(" + PARAMETERS_CONDITION[0] + ") OR weather.isCold()", true};
	private static final Object[] AND_CONDITION =
		{"(" + PARAMETERS_CONDITION[0] + ") AND weather.isCold()", false};
	private static final Object[] AND_BOOLEAN_CONDITION =
		{"(" + PARAMETERS_CONDITION[0] + ") AND true", true};
	private static final Object[] COMPLEX_CONDITION =
		{"((E0.second - E1.first) > weather.temperatureDifference(E1.second, E0.first)) " +
			"AND (weather.isCold() OR weather.isBiggerTemperature(E2.first, 10)) " +
			"AND (time.isEvenSecond() AND time.isLaterThan('2025-01-10T10:15:30')) " +
			"AND (math.isPrime(E1.first + 2) OR (math.absValue(E2.second - E0.first) < 100))", false};
	private static final Object[] NOT_TRUE_BOOLEAN = {"NOT true", false};
	private static final Object[] NOT_FALSE_BOOLEAN = {"NOT false", true};
	private static final Object[] NOT_FUNCTION_BOOLEAN = {"NOT weather.isCold()", true};
	private static final Object[] AND_NOT_FUNCTION_BOOLEAN =
		{"weather.isCold() AND NOT weather.isCold()", false};

	@ParameterizedTest
	@MethodSource("arguments")
	public void testCondition(String condition, boolean result) throws Exception {
		String json = String.format(JSON_FIRST_PART, condition);
		PatternRequest req = mapper.readValue(json, PatternRequest.class);

		assertEquals(result, evaluator.evaluate(elements, req.condition()));
	}

	private static Stream<Arguments> arguments() {
		return Stream.of(
			Arguments.of(PARENTHESIS_CONDITION),
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
		);
	}
}
