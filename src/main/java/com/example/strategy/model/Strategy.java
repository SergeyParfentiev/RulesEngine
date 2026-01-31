package com.example.strategy.model;

import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Lob
    @Column(nullable = false)
    private String json;

    @Column(nullable = false)
    private String name;

    @Enumerated(STRING)
    private Market market;

    @Enumerated(STRING)
    private Symbol symbol;

    @Enumerated(STRING)
    private TimeInterval timeInterval;
}
