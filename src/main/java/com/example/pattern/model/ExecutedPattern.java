package com.example.pattern.model;

import com.example.market.common.data.Pair;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"overlaps"})
@Table(name = "executed_pattern")
public class ExecutedPattern {

    @EmbeddedId
    private ExecutedPatternId id;

    @Column(nullable = false)
    private String name;

    @Column(precision = 16, scale = 8, nullable = false)
    private BigDecimal price;

    @OneToMany(mappedBy = "executedPattern", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<ExecutedPatternOverlap> overlaps = new HashSet<>();

    public void addOverlaps(Set<Pair<Integer, Long>> notOverlappedPatternNumbersById) {
        for (Pair<Integer, Long> notOverlappedPatternNumberById : notOverlappedPatternNumbersById) {
            Integer overlappedId = notOverlappedPatternNumberById.first();
            Long overlappedNumber = notOverlappedPatternNumberById.second();

            if (overlappedId.equals(this.id.id()) && overlappedNumber.equals(this.id.number())) {
                throw new IllegalArgumentException("Pattern cannot overlap with itself");
            }

            overlaps.add(new ExecutedPatternOverlap(
                    new ExecutedPatternOverlapId(this.id, overlappedId, overlappedNumber), this));
        }
    }
}
