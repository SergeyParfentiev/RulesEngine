package com.example.market.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Getter
public enum Symbol {

//XRP/USDT: XRP - baseRound, USDT - counterRound

    XRP_USDT("XRP/USDT"), BTC_USDT("BTC/USDT"), ETH_USDT("ETH/USDT"), XRP_BTC("XRP/BTC");

    @Getter(AccessLevel.NONE)
    public final String name;
    public final String simple;
    public final String telegram;
    public final String baseCurrency;
    public final String counterCurrency;

    Symbol(String telegram) {
        this.name = telegram.replace("/", "_");
        this.simple = telegram.replace("/", "");
        this.telegram = telegram;
        this.baseCurrency = telegram.split("/")[0];
        this.counterCurrency = telegram.split("/")[1];
    }

    public static Symbol simpleEnumUSDT(String baseCurrency) {
        return simpleEnum(baseCurrency.concat("USDT"));
    }

    public static Symbol simpleEnum(String simple) {
        for (Symbol symbol : values()) {
            if (symbol.simple.equalsIgnoreCase(simple)) {
                return symbol;
            }
        }
        throw new IllegalArgumentException("SymbolEnum simple: " + simple + " symbol doesnt exist");
    }

    public static Optional<Symbol> mrketOpt(String marketSymbol) {
        for (Symbol symbol : values()) {
            if (symbol.simple.equalsIgnoreCase(marketSymbol)) {
                return Optional.of(symbol);
            }
        }
        return Optional.empty();
    }

    @JsonCreator
    public static Symbol byName(String name) {
        for (Symbol symbol : values()) {
            if (symbol.name.equalsIgnoreCase(name)) {
                return symbol;
            }
        }
        throw new IllegalArgumentException("SymbolEnum name: " + name + " symbol doesnt exist");
    }

    // TODO: 1/3/2026 temporary until MarketSymbol is added

    private static final Map<Symbol, BigDecimal> priceStepBySymbol = Map.of(
            XRP_USDT, BigDecimal.valueOf(0.0001)
    );

    public BigDecimal priceStep() {
        return priceStepBySymbol.get(this);
    }
}
