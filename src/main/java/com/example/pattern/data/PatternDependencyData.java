package com.example.pattern.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class PatternDependencyData {

    private final Map<Integer, PatternDependencyPatternData> patterns;

    @JsonCreator
    public PatternDependencyData(@JsonProperty("patterns") Map<Integer, PatternDependencyPatternData> patterns) {
        this.patterns = patterns;
    }
}
