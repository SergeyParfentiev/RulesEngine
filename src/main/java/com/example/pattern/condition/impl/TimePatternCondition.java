package com.example.pattern.condition.impl;

import com.example.pattern.condition.PatternCondition;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@PatternCondition("time")
public class TimePatternCondition {

    public boolean isEvenSecond() {
        return (Instant.now().getEpochSecond() % 2) == 0;
    }

    public boolean isLaterThan(String isoDateTime) {
        LocalDateTime then = LocalDateTime.parse(isoDateTime);
        return LocalDateTime.now().isAfter(then);
    }

    public boolean isHour(BigDecimal hour) {
        return java.time.LocalTime.now().getHour() == hour.intValue();
    }
}
