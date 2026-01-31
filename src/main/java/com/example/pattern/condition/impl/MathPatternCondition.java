package com.example.pattern.condition.impl;

import com.example.pattern.condition.PatternCondition;

import java.math.BigDecimal;

@PatternCondition("math")
public class MathPatternCondition {

    public boolean isPrime(BigDecimal n) {
        if (n == null) return false;
        int v = n.intValue();
        if (v <= 1) return false;
        for (int i = 2; i * i <= v; i++) if (v % i == 0) return false;
        return true;
    }

    public BigDecimal absValue(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x.abs();
    }
}
