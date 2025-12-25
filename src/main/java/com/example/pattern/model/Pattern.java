package com.example.pattern.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Pattern {

    @EmbeddedId
    private PatternId id;

    @Column(nullable = false)
    private String name;

    @Column
    private Integer dependency;

    @Column(nullable = false)
    private BigDecimal price;
}
