package com.example.filler;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.impl.binance.candlestick.xrp_usdt.model.BinanceXrpUsdtCandlestick;

import java.math.BigDecimal;

public class CandlestickFiller {

    public static BinanceXrpUsdtCandlestick fill(long openTime, double openPrice, double highPrice, double lowPrice,
                                                 double closePrice, double volume, long closeTime, double quoteAssetVolume,
                                                 int tradesCount, double takerBuyBaseAssetVolume, double takerBuyQuoteAssetVolume) {
        return fill(new BinanceXrpUsdtCandlestick(), openTime, openPrice, highPrice, lowPrice, closePrice, volume,
                closeTime, quoteAssetVolume, tradesCount, takerBuyBaseAssetVolume, takerBuyQuoteAssetVolume);
    }

    public static <T extends Candlestick> T fill(T t, long openTime, double openPrice, double highPrice, double lowPrice,
                                                 double closePrice, double volume, long closeTime, double quoteAssetVolume,
                                                 int tradesCount, double takerBuyBaseAssetVolume, double takerBuyQuoteAssetVolume) {
        t.fill(openTime, BigDecimal.valueOf(openPrice), BigDecimal.valueOf(highPrice), BigDecimal.valueOf(lowPrice),
                BigDecimal.valueOf(closePrice), BigDecimal.valueOf(volume), closeTime, BigDecimal.valueOf(quoteAssetVolume),
                tradesCount, BigDecimal.valueOf(takerBuyBaseAssetVolume), BigDecimal.valueOf(takerBuyQuoteAssetVolume));
        return t;
    }
}
