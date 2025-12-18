package com.example.rule.controller;

import com.example.rule.dto.RuleRequest;
import com.example.rule.condition.parser.ConditionParserEvaluator;
import com.example.rule.model.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

	private final ConditionParserEvaluator evaluator;

	public RuleController(ConditionParserEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	@PostMapping("/evaluate")
	public ResponseEntity<?> evaluate(@RequestBody RuleRequest request) {
		try {
			List<Element> elements = request.elements();
			boolean result = evaluator.evaluate(elements, request.condition());
			return ResponseEntity.ok(result);
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}
}
