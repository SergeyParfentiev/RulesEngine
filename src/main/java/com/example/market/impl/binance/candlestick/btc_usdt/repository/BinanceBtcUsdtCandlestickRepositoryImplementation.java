package com.example.market.impl.binance.candlestick.btc_usdt.repository;

import com.example.market.impl.binance.candlestick.btc_usdt.model.BinanceBtcUsdtCandlestick;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface BinanceBtcUsdtCandlestickRepositoryImplementation
        extends JpaRepositoryImplementation<BinanceBtcUsdtCandlestick, Long> {
}
