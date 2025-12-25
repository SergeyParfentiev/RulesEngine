package com.example.pattern.repository.specification;

import com.example.pattern.model.Pattern;
import com.example.pattern.model.PatternId;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PatternSpecifications {

    public static Specification<Pattern> strategyIdAndId(long strategyId, int id) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("id"), new PatternId(strategyId, id)));
    }

    /*
    * parameter field for the future if not only price needed
    *
    * condition:
    *   >= * 1
    *   < / 10
    *   == + step * 4
    */
    public static Specification<Pattern> priceCondition(String field, String condition,
                                                        BigDecimal requestedPrice, BigDecimal step) {
        String[] parts = condition.trim().split(" ");

        BigDecimal price;

        if (parts.length == 3) {
            price = new BigDecimal(parts[2]);
        } else if (parts.length == 5 && parts[3].equals("*")) {
            price = calculateValue(parts[3], step, new BigDecimal(parts[4]), condition);
        } else {
            throw new IllegalArgumentException("Unexpected condition: " + condition);
        }

        BigDecimal value = calculateValue(parts[1], requestedPrice, price, condition);

        return (root, query, cb) -> comparison(parts[0], cb, root.get("price"), value, condition);
    }

    private static BigDecimal calculateValue(String operator, BigDecimal value, BigDecimal operand, String condition) {
        return switch (operator) {
            case "*" -> value.multiply(operand).setScale(value.scale(), RoundingMode.FLOOR);
            case "/" -> value.divide(operand, RoundingMode.FLOOR);
            case "+" -> value.add(operand).setScale(value.scale(), RoundingMode.FLOOR);
            case "-" -> value.subtract(operand).setScale(value.scale(), RoundingMode.FLOOR);
            default -> throw new IllegalArgumentException("Unexpected condition: " + condition);
        };
    }

    private static <Y extends Comparable<? super Y>> Predicate comparison(
            String comparison, CriteriaBuilder cb, Expression<? extends Y> field, Y value, String condition) {
        return switch (comparison) {
            case "==" -> cb.equal(field, value);
            case ">" -> cb.greaterThan(field, value);
            case ">=" -> cb.greaterThanOrEqualTo(field, value);
            case "<" -> cb.lessThan(field, value);
            case "<=" -> cb.lessThanOrEqualTo(field, value);
            default -> throw new IllegalArgumentException("Unexpected comparison: " + condition);
        };
    }
}
