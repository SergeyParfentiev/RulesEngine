package com.example.pattern.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExecutedPatternId implements Serializable {

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId;

    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    private long number;
}
