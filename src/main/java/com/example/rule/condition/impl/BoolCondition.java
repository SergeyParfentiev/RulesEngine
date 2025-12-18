package com.example.rule.condition.impl;

import com.example.rule.condition.Condition;
import org.springframework.stereotype.Component;

@Component("bool")
public class BoolCondition implements Condition {

	public boolean test(boolean test) {
		return test;
	}
}
