package com.example.rule.condition.parser;

import com.example.rule.condition.ConditionRegistry;
import com.example.rule.model.Element;
import com.example.rule.token.Tokenizer;
import com.example.rule.token.Tokenizer.Token;
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
