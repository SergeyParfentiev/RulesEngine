package com.example.strategy.dto;

import com.example.pattern.dto.PatternRequest;
import com.example.pattern.model.Element;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public final class StrategyRequest {

    private final Map<String, Element> elements;
    private final Map<Integer, PatternRequest> patterns;

    @JsonCreator
    public StrategyRequest(@JsonProperty("elements") Map<String, Element> elements,
                           @JsonProperty("patterns") LinkedHashMap<Integer, PatternRequest> patterns) {
        this.elements = elements;
        this.patterns = patterns;
    }
}
