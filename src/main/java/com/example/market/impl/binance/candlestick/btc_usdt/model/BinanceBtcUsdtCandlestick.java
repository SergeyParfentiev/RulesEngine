package com.example.market.impl.binance.candlestick.btc_usdt.model;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "binance_btc_usdt_candlestick")
public class BinanceBtcUsdtCandlestick extends Candlestick {

    @Override
    protected Candlestick instance() {
        return new BinanceBtcUsdtCandlestick();
    }

    @Override
    public Market market() {
        return Market.BINANCE;
    }

    @Override
    public Symbol symbol() {
        return Symbol.BTC_USDT;
    }
}
