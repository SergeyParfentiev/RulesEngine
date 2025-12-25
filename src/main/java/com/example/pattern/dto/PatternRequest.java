package com.example.pattern.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class PatternRequest {

	private final String condition;
	private final List<String> events;
	private final Map<String, Map<String, String>> dependency;

	@JsonCreator
	public PatternRequest(@JsonProperty("condition") String condition,
						  @JsonProperty("events") List<String> events,
						  @JsonProperty("dependency") Map<String, Map<String, String>> dependency) {
		this.condition = condition;
		this.events = events;
		this.dependency = dependency;
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
}
