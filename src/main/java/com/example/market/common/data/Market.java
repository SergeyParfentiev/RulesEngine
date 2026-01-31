package com.example.market.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Market {
    BINANCE("BINANCE");

    private final String name;

    Market(String name) {
        this.name = name;
    }

    @JsonCreator
    public static Market byName(String name) {
        for (Market symbol : values()) {
            if (symbol.name.equalsIgnoreCase(name)) {
                return symbol;
            }
        }
        throw new IllegalArgumentException("Market name: " + name + " symbol doesnt exist");
    }
}
