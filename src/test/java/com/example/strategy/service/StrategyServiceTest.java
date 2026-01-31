package com.example.strategy.service;

import com.example.AbstractCleanDBTest;
import com.example.strategy.model.Strategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.market.common.data.Market.BINANCE;
import static com.example.market.common.data.Symbol.XRP_USDT;
import static com.example.market.common.data.TimeInterval.ONE_MINUTE;
import static org.assertj.core.api.Assertions.assertThat;

public class StrategyServiceTest extends AbstractCleanDBTest {

    @Autowired
    public StrategyService service;

    @Test
    public void testSave() {
        Strategy strategy = service.save(new Strategy(0, "{}", "TestStrategy", BINANCE, XRP_USDT, ONE_MINUTE));

        assertThat(service.find(strategy.id())).isPresent();
    }
}
