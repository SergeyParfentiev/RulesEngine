package com.example.pattern.condition.impl;

import com.example.pattern.condition.Condition;
import org.springframework.stereotype.Component;

@Component("bool")
public class BoolCondition implements Condition {

	public boolean test(boolean test) {
		return test;
	}
}
