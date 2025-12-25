package com.example.pattern.event.parser;

import com.example.pattern.condition.parser.ConditionParser;
import com.example.pattern.event.EventRegistry;
import com.example.pattern.model.Element;
import com.example.pattern.token.Tokenizer;
import com.example.pattern.token.Tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventsParserEvaluator {

	private final ObjectMapper mapper;
	private final EventRegistry registry;

	public void evaluate(
		boolean condition, List<Element> elements, List<String> events) throws Exception {
		if (condition) {
			for (String event : events) {
				List<Token> tokens = new Tokenizer(event).tokenize();
				ConditionParser parser = new ConditionParser(elements, tokens, registry, mapper);
				parser.parse();
			}
		}
	}
}
