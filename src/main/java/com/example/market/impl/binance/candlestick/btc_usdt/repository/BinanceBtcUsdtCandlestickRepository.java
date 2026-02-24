package com.example.market.impl.binance.candlestick.btc_usdt.repository;

import com.example.market.common.candlestick.repository.AbstractCandlestickRepository;
import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.impl.binance.candlestick.btc_usdt.model.BinanceBtcUsdtCandlestick;
import org.springframework.stereotype.Repository;

@Repository
public class BinanceBtcUsdtCandlestickRepository extends AbstractCandlestickRepository<BinanceBtcUsdtCandlestick> {

    public BinanceBtcUsdtCandlestickRepository(BinanceBtcUsdtCandlestickRepositoryImplementation repository) {
        super(repository, BinanceBtcUsdtCandlestick.class);
    }

    @Override
    public Market market() {
        return Market.BINANCE;
    }

    @Override
    public Symbol symbol() {
        return Symbol.BTC_USDT;
    }

    @Override
    public BinanceBtcUsdtCandlestick candlestickInstance() {
        return new BinanceBtcUsdtCandlestick();
    }
}
