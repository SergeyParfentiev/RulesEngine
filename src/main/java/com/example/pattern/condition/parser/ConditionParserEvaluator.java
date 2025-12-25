package com.example.pattern.condition.parser;

import com.example.pattern.condition.ConditionRegistry;
import com.example.pattern.model.Element;
import com.example.pattern.token.Tokenizer;
import com.example.pattern.token.Tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConditionParserEvaluator {

	private final ObjectMapper mapper;
	private final ConditionRegistry registry;

	public boolean evaluate(List<Element> elements, String condition) throws Exception {
		List<Token> tokens = new Tokenizer(condition).tokenize();
		ConditionParser parser = new ConditionParser(elements, tokens, registry, mapper);

		return parser.parse();
	}
}
