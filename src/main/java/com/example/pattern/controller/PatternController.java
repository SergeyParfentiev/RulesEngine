package com.example.pattern.controller;

import com.example.pattern.condition.parser.PatternConditionExecutor;
import com.example.pattern.data.PatternData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class PatternController {

    private final PatternConditionExecutor evaluator;

    public PatternController(PatternConditionExecutor evaluator) {
        this.evaluator = evaluator;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody PatternData request) {
        try {
//			List<Element> elements = List.of();
//			boolean result = evaluator.evaluate(elements, request.condition());
            boolean result = true;
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
