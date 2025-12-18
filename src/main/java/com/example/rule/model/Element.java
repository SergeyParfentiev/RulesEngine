package com.example.rule.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record Element(BigDecimal first, BigDecimal second) {

	@JsonCreator
	public Element(@JsonProperty("first") BigDecimal first,
	               @JsonProperty("second") BigDecimal second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return "Element{first=" + first + ", second=" + second + '}';
	}
}
