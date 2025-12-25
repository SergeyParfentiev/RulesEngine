package com.example.pattern.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;

public class Element {

	private final BigDecimal first;
	private final BigDecimal second;

	@JsonCreator
	public Element(@JsonProperty("first") BigDecimal first,
				   @JsonProperty("second") BigDecimal second) {
		this.first = first;
		this.second = second;
	}

	public BigDecimal first() {
		return first;
	}

	public BigDecimal second() {
		return second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Element) obj;
		return Objects.equals(this.first, that.first) &&
				Objects.equals(this.second, that.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "Element[" +
				"first=" + first + ", " +
				"second=" + second + ']';
	}
}
