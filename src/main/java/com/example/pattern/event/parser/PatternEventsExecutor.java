package com.example.pattern.event.parser;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.pattern.event.PatternEventRegistry;
import com.example.pattern.token.PatternTokenExecutor;
import com.example.pattern.token.Tokenizer;
import com.example.pattern.token.Tokenizer.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class PatternEventsExecutor {

    private final ObjectMapper mapper;
    private final PatternEventRegistry registry;

    public void execute(List<Candlestick> candlesticks, List<String> events) throws Exception {
        for (String event : events) {
            List<Token> tokens = new Tokenizer(event).tokenize();
            PatternTokenExecutor parser = new PatternTokenExecutor(candlesticks, tokens, registry, mapper);
            parser.parse();
        }
    }
}
