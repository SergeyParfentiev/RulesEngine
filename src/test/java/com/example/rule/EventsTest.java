package com.example.rule;

import com.example.rule.condition.parser.ConditionParserEvaluator;
import com.example.rule.dto.RuleRequest;
import com.example.rule.event.impl.PrintConsoleEvent;
import com.example.rule.event.parser.EventsParserEvaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EventsTest {

	@Autowired
	public ObjectMapper mapper;

	@Autowired
	public EventsParserEvaluator evaluator;

	@SpyBean
	public PrintConsoleEvent printConsoleEvent;

	@Autowired
	public ConditionParserEvaluator conditionParserEvaluator;

	private static final String JSON_FIRST_PART = """
		{
		  "elements": {
		    "E0": { "first": 10.5, "second": 25.1 },
		    "E1": { "first": 5.0,  "second": 40.0 },
		    "E2": { "first": 12.3, "second": 18.7 }
		  },
		  "condition": "%s",
		  "events": %s
		}
		""";

	@Test
	public void testWithoutTriggeringFunction() throws Exception {
		String condition = "1 != 1";
		String events = "[ \"printConsole.print()\" ]";

		String json = String.format(JSON_FIRST_PART, condition, events);
		RuleRequest req = mapper.readValue(json, RuleRequest.class);

		evaluator.evaluate(conditionParserEvaluator.evaluate(req.elements(), req.condition()),
			req.elements(), req.events());

		verify(printConsoleEvent, times(0)).print();
		verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
		verify(printConsoleEvent, times(0)).printWithParameters(
			anyString(), any(BigDecimal.class), anyBoolean());
	}

	@Test
	public void testFunctionWithoutParameters() throws Exception {
		String condition = "1 == 1";
		String events = "[ \"printConsole.print()\" ]";

		String json = String.format(JSON_FIRST_PART, condition, events);
		RuleRequest req = mapper.readValue(json, RuleRequest.class);

		evaluator.evaluate(conditionParserEvaluator.evaluate(req.elements(), req.condition()),
			req.elements(), req.events());

		verify(printConsoleEvent, times(1)).print();
		verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
		verify(printConsoleEvent, times(0)).printWithParameters(
			anyString(), any(BigDecimal.class), anyBoolean());
	}

	@Test
	public void testFunctionWithParameter() throws Exception {
		String condition = "1 == 1";
		String text = "Hello there";
		String events = "[ \"printConsole.printWithStringParameter('" + text + "')\" ]";

		String json = String.format(JSON_FIRST_PART, condition, events);
		RuleRequest req = mapper.readValue(json, RuleRequest.class);

		evaluator.evaluate(conditionParserEvaluator.evaluate(req.elements(), req.condition()),
			req.elements(), req.events());

		verify(printConsoleEvent, times(0)).print();
		verify(printConsoleEvent, times(1)).printWithStringParameter(text);
		verify(printConsoleEvent, times(0)).printWithParameters(
			anyString(), any(BigDecimal.class), anyBoolean());
	}

	@Test
	public void testFunctionsWithParameters() throws Exception {
		String condition = "1 == 1";
		String text = "Hello there";
		BigDecimal number = BigDecimal.TEN;
		boolean printText = true;
		String events = "[ " +
			"\"printConsole.printWithStringParameter('" + text + "')\"," +
			"\"printConsole.printWithParameters('" + text + "', " + number + ", " + printText + ")\"" +
			" ]";

		String json = String.format(JSON_FIRST_PART, condition, events);
		RuleRequest req = mapper.readValue(json, RuleRequest.class);

		evaluator.evaluate(conditionParserEvaluator.evaluate(req.elements(), req.condition()),
			req.elements(), req.events());

		verify(printConsoleEvent, times(0)).print();
		verify(printConsoleEvent, times(1)).printWithStringParameter(text);
		verify(printConsoleEvent, times(1)).printWithParameters(text, number, printText);
	}

	@Test
	public void testFunctionWithElementParameter() throws Exception {
		String condition = "1 == 1";
		String events = "[ " +
			"\"printConsole.printWithNumberParameter(E0.first)\"" +
			" ]";

		String json = String.format(JSON_FIRST_PART, condition, events);
		RuleRequest req = mapper.readValue(json, RuleRequest.class);

		evaluator.evaluate(conditionParserEvaluator.evaluate(req.elements(), req.condition()),
			req.elements(), req.events());

		verify(printConsoleEvent, times(0)).print();
		verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
		verify(printConsoleEvent, times(1)).printWithNumberParameter(BigDecimal.valueOf(10.5));
		verify(printConsoleEvent, times(0)).printWithParameters(
			anyString(), any(BigDecimal.class), anyBoolean());
	}
}
