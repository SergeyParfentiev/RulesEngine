package com.example.pattern.condition.impl;

import com.example.pattern.condition.Condition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("weather")
public class WeatherCondition implements Condition {

	public boolean isCold() {
		return false;
	}

	public BigDecimal temperatureDifference(BigDecimal a, BigDecimal b) {
		if (a == null || b == null) return BigDecimal.ZERO;
		return a.subtract(b).abs();
	}

	public boolean isBiggerTemperature(BigDecimal value, BigDecimal threshold) {
		if (value == null) return false;
		return value.compareTo(threshold) > 0;
	}
}
