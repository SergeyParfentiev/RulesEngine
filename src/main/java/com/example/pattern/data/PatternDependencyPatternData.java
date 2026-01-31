package com.example.pattern.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PatternDependencyPatternData(String price) {

    @JsonCreator
    public PatternDependencyPatternData(@JsonProperty("price") String price) {
        this.price = price;
    }
}
