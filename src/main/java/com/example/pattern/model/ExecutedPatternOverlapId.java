package com.example.pattern.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExecutedPatternOverlapId implements Serializable {

    @Embedded
    private ExecutedPatternId patternId;

    @Column(name = "overlapped_id", nullable = false)
    private Integer overlappedId;

    @Column(name = "overlapped_number", nullable = false)
    private Long overlappedNumber;
}

