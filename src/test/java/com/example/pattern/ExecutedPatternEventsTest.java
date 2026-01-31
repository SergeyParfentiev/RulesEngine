package com.example.pattern;

import com.example.AbstractCleanDBTest;
import com.example.market.common.candlestick.model.Candlestick;
import com.example.pattern.condition.parser.PatternConditionExecutor;
import com.example.pattern.data.PatternData;
import com.example.pattern.event.impl.PrintConsoleEvent;
import com.example.pattern.event.parser.PatternEventsExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.List;

import static com.example.filler.CandlestickFiller.fill;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExecutedPatternEventsTest extends AbstractCleanDBTest {

    @Autowired
    public ObjectMapper mapper;

    @SpyBean
    public PrintConsoleEvent printConsoleEvent;

    @Autowired
    public PatternEventsExecutor eventsExecutor;

    @Autowired
    public PatternConditionExecutor patternParserEvaluator;

    private final List<Candlestick> candlesticks = List.of(
            fill(1525421700000L, 10.5, 0.95001000, 0.91020000, 25.1, 171304.5600000000, 1525421759999L, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000),
            fill(1525421760000L, 5, 0.92981000, 0.91020000, 40, 62126.1600000000, 1525421819999L, 57208.8847960000, 139, 39303.3500000000, 36393.6817856000),
            fill(1525421820000L, 12.3, 0.92977000, 0.91400000, 18.7, 162886.5000000000, 1525421879999L, 149223.8808422000, 352, 29658.9200000000, 27210.1669172000));

    private static final String JSON_FIRST_PART = """
            {
              "condition": "%s",
              "events": %s
            }
            """;

    @Test
    public void testWithoutTriggeringFunction() throws Exception {
        String patternCondition = "1 != 1";
        String events = "[ \"printConsole.print()\" ]";

        String json = String.format(JSON_FIRST_PART, patternCondition, events);
        PatternData req = mapper.readValue(json, PatternData.class);

        if (patternParserEvaluator.execute(candlesticks, req.condition())) {
            eventsExecutor.execute(candlesticks, req.events());
        }

        verify(printConsoleEvent, times(0)).print();
        verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
        verify(printConsoleEvent, times(0)).printWithParameters(
                anyString(), any(BigDecimal.class), anyBoolean());
    }

    @Test
    public void testFunctionWithoutParameters() throws Exception {
        String patternCondition = "1 == 1";
        String events = "[ \"printConsole.print()\" ]";

        String json = String.format(JSON_FIRST_PART, patternCondition, events);
        PatternData req = mapper.readValue(json, PatternData.class);

		if (patternParserEvaluator.execute(candlesticks, req.condition())) {
			eventsExecutor.execute(candlesticks, req.events());
		}

        verify(printConsoleEvent, times(1)).print();
        verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
        verify(printConsoleEvent, times(0)).printWithParameters(
                anyString(), any(BigDecimal.class), anyBoolean());
    }

    @Test
    public void testFunctionWithParameter() throws Exception {
        String patternCondition = "1 == 1";
        String text = "Hello there";
        String events = "[ \"printConsole.printWithStringParameter('" + text + "')\" ]";

        String json = String.format(JSON_FIRST_PART, patternCondition, events);
        PatternData req = mapper.readValue(json, PatternData.class);

		if (patternParserEvaluator.execute(candlesticks, req.condition())) {
			eventsExecutor.execute(candlesticks, req.events());
		}

        verify(printConsoleEvent, times(0)).print();
        verify(printConsoleEvent, times(1)).printWithStringParameter(text);
        verify(printConsoleEvent, times(0)).printWithParameters(
                anyString(), any(BigDecimal.class), anyBoolean());
    }

    @Test
    public void testFunctionsWithParameters() throws Exception {
        String patternCondition = "1 == 1";
        String text = "Hello there";
        BigDecimal number = BigDecimal.TEN;
        boolean printText = true;
        String events = "[ " +
                "\"printConsole.printWithStringParameter('" + text + "')\"," +
                "\"printConsole.printWithParameters('" + text + "', " + number + ", " + printText + ")\"" +
                " ]";

        String json = String.format(JSON_FIRST_PART, patternCondition, events);
        PatternData req = mapper.readValue(json, PatternData.class);

		if (patternParserEvaluator.execute(candlesticks, req.condition())) {
			eventsExecutor.execute(candlesticks, req.events());
		}

        verify(printConsoleEvent, times(0)).print();
        verify(printConsoleEvent, times(1)).printWithStringParameter(text);
        verify(printConsoleEvent, times(1)).printWithParameters(text, number, printText);
    }

    @Test
    public void testFunctionWithElementParameter() throws Exception {
        String patternCondition = "1 == 1";
        String events = "[ " +
                "\"printConsole.printWithNumberParameter(E2.openPrice)\"" +
                " ]";

        String json = String.format(JSON_FIRST_PART, patternCondition, events);
        PatternData req = mapper.readValue(json, PatternData.class);

		if (patternParserEvaluator.execute(candlesticks, req.condition())) {
			eventsExecutor.execute(candlesticks, req.events());
		}

        verify(printConsoleEvent, times(0)).print();
        verify(printConsoleEvent, times(0)).printWithStringParameter(anyString());
        verify(printConsoleEvent, times(1)).printWithNumberParameter(BigDecimal.valueOf(10.5));
        verify(printConsoleEvent, times(0)).printWithParameters(
                anyString(), any(BigDecimal.class), anyBoolean());
    }
}
