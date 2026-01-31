package com.example.pattern.condition.parser;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.pattern.condition.PatternConditionRegistry;
import com.example.pattern.token.PatternTokenExecutor;
import com.example.pattern.token.Tokenizer;
import com.example.pattern.token.Tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class PatternConditionExecutor {

    private final ObjectMapper mapper;
    private final PatternConditionRegistry registry;

    public boolean execute(List<Candlestick> candlesticks, String condition) throws Exception {
        List<Token> tokens = new Tokenizer(condition).tokenize();
        PatternTokenExecutor parser = new PatternTokenExecutor(candlesticks, tokens, registry, mapper);

        return parser.parse();
    }
}
