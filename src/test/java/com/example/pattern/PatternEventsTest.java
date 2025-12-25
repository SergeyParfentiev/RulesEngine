package com.example.pattern;

import com.example.pattern.condition.parser.ConditionParserEvaluator;
import com.example.pattern.dto.PatternRequest;
import com.example.pattern.event.impl.PrintConsoleEvent;
import com.example.pattern.event.parser.EventsParserEvaluator;
import com.example.pattern.model.Element;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
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
public class PatternEventsTest {

	@Autowired
	public ObjectMapper mapper;

	@SpyBean
	public PrintConsoleEvent printConsoleEvent;

	@Autowired
	public EventsParserEvaluator eventsParserEvaluator;

	@Autowired
	public ConditionParserEvaluator conditionParserEvaluator;

	private final List<Element> elements = List.of(
		new Element(BigDecimal.valueOf(10.5), BigDecimal.valueOf(25.1)),
		new Element(BigDecimal.valueOf(5), BigDecimal.valueOf(40)),
		new Element(BigDecimal.valueOf(12.3), BigDecimal.valueOf(18.7)));

	private static final String JSON_FIRST_PART = """
		{
		  "condition": "%s",
		  "events": %s
		}
		""";

	@Test
	public void testWithoutTriggeringFunction() throws Exception {
		String condition = "1 != 1";
		String events = "[ \"printConsole.print()\" ]";

		String json = String.format(JSON_FIRST_PART, condition, events);
		PatternRequest req = mapper.readValue(json, PatternRequest.class);

		eventsParserEvaluator.evaluate(conditionParserEvaluator.evaluate(elements, req.condition()),
			elements, req.events());

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
		PatternRequest req = mapper.readValue(json, PatternRequest.class);

		eventsParserEvaluator.evaluate(conditionParserEvaluator.evaluate(elements, req.condition()),
			elements, req.events());

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
		PatternRequest req = mapper.readValue(json, PatternRequest.class);

		eventsParserEvaluator.evaluate(conditionParserEvaluator.evaluate(elements, req.condition()),
			elements, req.events());

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
		PatternRequest req = mapper.readValue(json, PatternRequest.class);

		eventsParserEvaluator.evaluate(conditionParserEvaluator.evaluate(elements, req.condition()),
			elements, req.events());

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
		PatternRequest req = mapper.readValue(json, PatternRequest.class);

		eventsParserEvaluator.evaluate(conditionParserEvaluator.evaluate(elements, req.condition()),
			elements, req.events());

		verify(printConsoleEvent, times(0)).print();
		verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
		verify(printConsoleEvent, times(1)).printWithNumberParameter(BigDecimal.valueOf(10.5));
		verify(printConsoleEvent, times(0)).printWithParameters(
			anyString(), any(BigDecimal.class), anyBoolean());
	}
}
