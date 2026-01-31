package com.example.market.impl.binance.candlestick.xrp_usdt.model;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "binance_xrp_usdt_candlestick")
public class BinanceXrpUsdtCandlestick extends Candlestick {

    @Override
    public Market market() {
        return Market.BINANCE;
    }

    @Override
    public Symbol symbol() {
        return Symbol.XRP_USDT;
    }
}
