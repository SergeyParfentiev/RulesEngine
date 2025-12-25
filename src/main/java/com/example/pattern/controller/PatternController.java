package com.example.pattern.controller;

import com.example.pattern.dto.PatternRequest;
import com.example.pattern.condition.parser.ConditionParserEvaluator;
import com.example.pattern.model.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class PatternController {

	private final ConditionParserEvaluator evaluator;

	public PatternController(ConditionParserEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	@PostMapping("/evaluate")
	public ResponseEntity<?> evaluate(@RequestBody PatternRequest request) {
		try {
			List<Element> elements = List.of();
			boolean result = evaluator.evaluate(elements, request.condition());
			return ResponseEntity.ok(result);
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}
}
