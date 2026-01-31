package com.example.market.impl.binance.candlestick.xrp_usdt.repository;

import com.example.market.common.candlestick.repository.AbstractCandlestickRepository;
import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.impl.binance.candlestick.xrp_usdt.model.BinanceXrpUsdtCandlestick;
import org.springframework.stereotype.Repository;

@Repository
public class BinanceXrpUsdtCandlestickRepository extends AbstractCandlestickRepository<BinanceXrpUsdtCandlestick> {

    public BinanceXrpUsdtCandlestickRepository(BinanceXrpUsdtCandlestickRepositoryImplementation repository) {
        super(repository, BinanceXrpUsdtCandlestick.class);
    }

    @Override
    public Market market() {
        return Market.BINANCE;
    }

    @Override
    public Symbol symbol() {
        return Symbol.XRP_USDT;
    }
}
