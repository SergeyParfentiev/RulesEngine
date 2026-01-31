package com.example.pattern.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PatternData(String name, String condition, List<String> events,
                          PatternDependencyData dependency) {

    @JsonCreator
    public PatternData(@JsonProperty("name") String name,
                       @JsonProperty("condition") String condition, @JsonProperty("events") List<String> events,
                       @JsonProperty("dependency") PatternDependencyData dependency) {
        this.name = name;
        this.condition = condition;
        this.events = events;
        this.dependency = dependency;
    }

    private static int parseIndex(String key) {
        if (key == null || !key.startsWith("E")) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(key.substring(1));
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }
}
