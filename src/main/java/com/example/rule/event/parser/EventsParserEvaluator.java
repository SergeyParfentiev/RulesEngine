package com.example.rule.event.parser;

import com.example.rule.condition.parser.ConditionParser;
import com.example.rule.event.EventRegistry;
import com.example.rule.model.Element;
import com.example.rule.token.Tokenizer;
import com.example.rule.token.Tokenizer.Token;
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
