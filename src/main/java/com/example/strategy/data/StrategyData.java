package com.example.strategy.data;

import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import com.example.pattern.data.PatternData;
import com.example.pattern.model.Element;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public final class StrategyData {

    private final String name;
    private final Market market;
    private final Symbol symbol;
    private final TimeInterval timeInterval;
    private final Map<String, Element> elements;
    private final Map<Integer, PatternData> patterns;

    @JsonCreator
    public StrategyData(@JsonProperty("name") String name,
                        @JsonProperty("market") Market market,
                        @JsonProperty("symbol") Symbol symbol,
                        @JsonProperty("timeInterval") TimeInterval timeInterval,
                        @JsonProperty("elements") Map<String, Element> elements,
                        @JsonProperty("patterns") LinkedHashMap<Integer, PatternData> patterns) {
        this.name = name;
        this.market = market;
        this.symbol = symbol;
        this.timeInterval = timeInterval;
        this.elements = elements;
        this.patterns = patterns;
    }
}
