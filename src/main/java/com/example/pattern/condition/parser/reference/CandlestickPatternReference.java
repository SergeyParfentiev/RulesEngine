package com.example.pattern.condition.parser.reference;

import com.example.market.common.candlestick.model.Candlestick;

import java.math.BigDecimal;

public class CandlestickPatternReference {

    public BigDecimal value(String method, Candlestick candlestick) {
        return switch (method) {
            case "openPrice" -> candlestick.openPrice();
            case "highPrice" -> candlestick.highPrice();
            case "lowPrice" -> candlestick.lowPrice();
            case "closePrice" -> candlestick.closePrice();

            default -> throw new IllegalStateException("Unexpected candlestick method: " + method);
        };
    }
}
