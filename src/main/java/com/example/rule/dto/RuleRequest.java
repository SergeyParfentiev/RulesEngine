package com.example.rule.dto;

import com.example.rule.model.Element;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RuleRequest {
	private final List<Element> elements;
	private final String condition;
	private final List<String> events;

	@JsonCreator
	public RuleRequest(@JsonProperty("elements") Map<String, Element> elementsMap,
	                   @JsonProperty("condition") String condition,
	                   @JsonProperty("events") List<String> events) {
		if (elementsMap == null) {
			this.elements = List.of();
		} else {
			this.elements = elementsMap.entrySet().stream()
				.sorted(Comparator.comparingInt(e -> parseIndex(e.getKey())))
				.map(Map.Entry::getValue)
				.toList();
		}

		this.condition = condition;
		this.events = events;
	}

	private static int parseIndex(String key) {
		if (key == null || !key.startsWith("E")) {
			return Integer.MAX_VALUE;
		}

		try {
			return Integer.parseInt(key.substring(1));
		} catch (Exception ex) {
			return Integer.MAX_VALUE;
		}
	}

	public List<Element> elements() {
		return elements;
	}

	public String condition() {
		return condition;
	}

	public List<String> events() {
		return events;
	}
}
