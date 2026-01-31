package com.example.pattern.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "executed_pattern_overlap")
public class ExecutedPatternOverlap {

    @EmbeddedId
    private ExecutedPatternOverlapId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("patternId")
    @JoinColumns({
            @JoinColumn(name = "strategy_id", referencedColumnName = "strategy_id"),
            @JoinColumn(name = "id", referencedColumnName = "id"),
            @JoinColumn(name = "number", referencedColumnName = "number")
    })
    private ExecutedPattern executedPattern;
}
