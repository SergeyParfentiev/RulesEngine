package com.example.pattern.repository.view;

import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import com.example.pattern.data.PatternDependencyData;
import com.example.pattern.model.ExecutedPattern;
import com.example.pattern.model.ExecutedPatternOverlap;
import com.example.pattern.view.ExecutedPatternView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExecutedPatternViewRepository {

    @PersistenceContext
    private EntityManager em;

    public List<ExecutedPatternView> findNotOverlappedPatterns(Long strategyId, PatternDependencyData dependency,
                                                               BigDecimal currentPrice, Symbol symbol) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExecutedPatternView> cq = cb.createQuery(ExecutedPatternView.class);

        Root<ExecutedPattern> ep = cq.from(ExecutedPattern.class);

        // ===== NOT EXISTS overlaps =====
        Subquery<Integer> overlapSubquery = cq.subquery(Integer.class);
        Root<ExecutedPatternOverlap> epo = overlapSubquery.from(ExecutedPatternOverlap.class);
        overlapSubquery.select(cb.literal(1))
                .where(
                        cb.equal(epo.get("id").get("patternId").get("strategyId"), ep.get("id").get("strategyId")),
                        cb.equal(epo.get("id").get("overlappedId"), ep.get("id").get("id")),
                        cb.equal(epo.get("id").get("overlappedNumber"), ep.get("id").get("number")));

        Predicate notOverlapped = cb.not(cb.exists(overlapSubquery));

        // ===== OR-of-AND rules =====
        List<Predicate> orGroups = new ArrayList<>();
        List<Pair<Integer, Predicate>> rules = dependency.patterns().entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(),
                        priceCondition(cb, ep, entry.getValue().price(),
                                currentPrice, symbol.priceStep(), symbol.priceStep().scale())))
                .toList();

        for (Pair<Integer, Predicate> rule : rules) {
            Predicate p = cb.and(
                    cb.equal(ep.get("id").get("id"), rule.first()),
                    rule.second());
            orGroups.add(p);
        }
        Predicate orOfAnd = cb.or(orGroups.toArray(new Predicate[0]));

        // ===== WHERE =====
        Predicate where = cb.and(
                cb.equal(ep.get("id").get("strategyId"), strategyId),
                notOverlapped,
                orOfAnd);
        cq.where(where);

        // ===== SELECT + GROUP BY =====
        cq.multiselect(
                ep.get("id").get("strategyId"),
                ep.get("id").get("id"),
                cb.min(ep.get("id").get("number")).alias("number"));

        cq.groupBy(
                ep.get("id").get("strategyId"),
                ep.get("id").get("id"));

        return em.createQuery(cq).getResultList();
    }

    /*
     * parameter field for the future if not only "price" value needed
     *
     * condition:
     *   >= * 1
     *   < / 10
     *   == + step * 4
     */
    private Predicate priceCondition(CriteriaBuilder cb, Root<ExecutedPattern> ep, String condition,
                                     BigDecimal price, BigDecimal step, int scale) {
        BigDecimal calculatedPrice;
        String[] parts = condition.trim().split(" ");

        if (parts.length == 3) {
            calculatedPrice = new BigDecimal(parts[2]);
        } else if (parts.length == 5 && parts[3].equals("*")) {
            calculatedPrice = calculateValue(parts[3], step, new BigDecimal(parts[4]), condition, scale);
        } else {
            throw new IllegalArgumentException("Unexpected condition: " + condition);
        }

        BigDecimal value = calculateValue(parts[1], price, calculatedPrice, condition, scale);

        return comparison(parts[0], cb, ep.get("price"), value, condition);
    }

    private BigDecimal calculateValue(String operator, BigDecimal value, BigDecimal operand, String condition, int scale) {
        return switch (operator) {
            case "*" -> value.multiply(operand).setScale(scale, RoundingMode.FLOOR);
            case "/" -> value.divide(operand, RoundingMode.FLOOR);
            case "+" -> value.add(operand).setScale(scale, RoundingMode.FLOOR);
            case "-" -> value.subtract(operand).setScale(scale, RoundingMode.FLOOR);
            default -> throw new IllegalArgumentException("Unexpected condition: " + condition);
        };
    }

    private <Y extends Comparable<? super Y>> Predicate comparison(
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
