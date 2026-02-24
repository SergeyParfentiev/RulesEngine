package com.example.market.common.candlestick.model;

import com.example.market.common.data.Market;
import com.example.market.common.data.Pair;
import com.example.market.common.data.Symbol;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@MappedSuperclass
@EqualsAndHashCode
public abstract class Candlestick {

    @Id
    private long openTime;

    @Column(precision = 16, scale = 8, nullable = false)
    private BigDecimal openPrice;

    @Column(precision = 16, scale = 8, nullable = false)
    private BigDecimal highPrice;

    @Column(precision = 16, scale = 8, nullable = false)
    private BigDecimal lowPrice;

    @Column(precision = 16, scale = 8, nullable = false)
    private BigDecimal closePrice;

    @Column(precision = 20, scale = 10, nullable = false)
    private BigDecimal volume;

    @Column
    private long closeTime;

    @Column(precision = 20, scale = 10, nullable = false)
    private BigDecimal quoteAssetVolume;

    @Column
    private int tradesCount;

    @Column(precision = 20, scale = 10, nullable = false)
    private BigDecimal takerBuyBaseAssetVolume;

    @Column(precision = 20, scale = 10, nullable = false)
    private BigDecimal takerBuyQuoteAssetVolume;

    public Candlestick() {
    }

    public Candlestick copy() {
        Candlestick copy = instance();
        copy.fill(this);
        return copy;
    }

    protected abstract Candlestick instance();

    public void fill(Candlestick candlestick) {
        fill(candlestick.openTime, candlestick.openPrice, candlestick.highPrice, candlestick.lowPrice,
                candlestick.closePrice, candlestick.volume, candlestick.closeTime, candlestick.quoteAssetVolume,
                candlestick.tradesCount, candlestick.takerBuyBaseAssetVolume, candlestick.takerBuyQuoteAssetVolume);
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

