package com.example.market.common.candlestick.model;

import com.example.market.common.data.Market;
import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@MappedSuperclass
@EqualsAndHashCode
public abstract class Candlestick {

    @Id
    private long openTime;

    @Column(precision = 16, scale = 8)
    private BigDecimal openPrice;

    @Column(precision = 16, scale = 8)
    private BigDecimal highPrice;

    @Column(precision = 16, scale = 8)
    private BigDecimal lowPrice;

    @Column(precision = 16, scale = 8)
    private BigDecimal closePrice;

    @Column(precision = 20, scale = 10)
    private BigDecimal volume;

    @Column
    private long closeTime;

    @Column(precision = 20, scale = 10)
    private BigDecimal quoteAssetVolume;

    @Column
    private int tradesCount;

    @Column(precision = 20, scale = 10)
    private BigDecimal takerBuyBaseAssetVolume;

    @Column(precision = 20, scale = 10)
    private BigDecimal takerBuyQuoteAssetVolume;

    public Candlestick() {
    }

    public void fill(long openTime, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice,
                     BigDecimal closePrice, BigDecimal volume, long closeTime, BigDecimal quoteAssetVolume,
                     int tradesCount, BigDecimal takerBuyBaseAssetVolume, BigDecimal takerBuyQuoteAssetVolume) {
        this.openTime = openTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.closeTime = closeTime;
        this.quoteAssetVolume = quoteAssetVolume;
        this.tradesCount = tradesCount;
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }

    public boolean bodyExists() {
        return openPrice.compareTo(closePrice) != 0;
    }

    public BigDecimal topBodyPrice() {
        return openPrice.max(closePrice);
    }

    public BigDecimal bottomBodyPrice() {
        return closePrice.min(openPrice);
    }

    public abstract Market market();

    public abstract Symbol symbol();

    public Pair<Market, Symbol> marketSymbolPair() {
        return new Pair<>(market(), symbol());
    }
}

