package com.example.market.common.candlestick.service;

import com.example.AbstractCleanDBTest;
import com.example.filler.CandlestickFiller;
import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.data.Market;
import com.example.market.common.data.Symbol;
import com.example.market.common.service.TimeIntervalUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CandlestickServiceTest extends AbstractCleanDBTest {

    @Autowired
    public CandlestickService service;

    private static final Object[] BINANCE__BTC_USDT = {Market.BINANCE, Symbol.BTC_USDT, 1525421700000L, 1525421759999L};
    private static final Object[] BINANCE__XRP_USDT = {Market.BINANCE, Symbol.XRP_USDT, 1525421700000L, 1525421759999L};

    @ParameterizedTest
    @MethodSource("testSave_SpecificMarketAndSymbolArguments")
    public void testSave_SpecificMarketAndSymbol(Market market, Symbol symbol, long openTime, long closeTime) {
        iteratedByMarketAndSymbol(((markt, symbl) ->
                assertEquals(0, service.inRange(markt, symbl, openTime, closeTime).size())));

        service.save(market, symbol, (candlestick -> CandlestickFiller
                .fill(candlestick, openTime, 0.92999000, 0.95001000, 0.91020000, 0.91020000,
                        171304.5600000000, closeTime, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000)));

        iteratedByMarketAndSymbol(((markt, symbl) -> {
            List<Candlestick> candlesticks = service.inRange(markt, symbl, openTime, closeTime);

            if (market.equals(markt) && symbol.equals(symbl)) {
                assertEquals(1, candlesticks.size());
            } else {
                assertEquals(0, candlesticks.size());
            }
        }));
    }

    private static void iteratedByMarketAndSymbol(BiConsumer<Market, Symbol> marketSymbolBiConsumer) {
        for (Market market : Market.values()) {
            for (Symbol symbol : new Symbol[]{Symbol.BTC_USDT, Symbol.XRP_USDT}) {
                // TODO: 2/24/2026 change when all repositories will be implemented
//            for (Symbol symbol : Symbol.values()) {
                marketSymbolBiConsumer.accept(market, symbol);
            }
        }
    }

    private static Stream<Arguments> testSave_SpecificMarketAndSymbolArguments() {
        return Stream.of(
                Arguments.of(BINANCE__BTC_USDT),
                Arguments.of(BINANCE__XRP_USDT));
    }

    @Test
    public void testSave__IncorrectRangeOfOpenAndCloseTimes() {
        long openTime = 1525421700000L;
        long closeTime = 1525421760000L;

        InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            service.save(Market.BINANCE, Symbol.BTC_USDT, (candlestick -> CandlestickFiller
                    .fill(candlestick, openTime, 0.92999000, 0.95001000, 0.91020000, 0.91020000,
                            171304.5600000000, closeTime, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000)));
        });

        assertEquals("Incorrect open close candlestick time range during save. " +
                "New candlestick open time %d, new candlestick close time %d"
                        .formatted(openTime, closeTime), exception.getMessage());
    }

    @Test
    public void testSave__IncorrectRangeBetweenOpenTimes() {
        long openTime = 1525421700000L;
        long closeTime = 1525421759999L;

        service.save(Market.BINANCE, Symbol.BTC_USDT, (candlestick -> CandlestickFiller
                .fill(candlestick, openTime, 0.92999000, 0.95001000, 0.91020000, 0.91020000,
                        171304.5600000000, closeTime, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000)));

        long newOpenTime = openTime + TimeIntervalUtil.ONE_MINUTE + 1;
        long newCloseTime = closeTime + TimeIntervalUtil.ONE_MINUTE + 1;

        InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            service.save(Market.BINANCE, Symbol.BTC_USDT, (candlestick -> CandlestickFiller
                    .fill(candlestick, newOpenTime, 0.92999000, 0.95001000, 0.91020000, 0.91020000,
                            171304.5600000000, newCloseTime, 157893.8514214000, 535, 28832.4000000000, 26792.2072038000)));
        });

        assertEquals("Incorrect open time range between candlesticks during save. " +
                "Previous candlestick open time %d, new candlestick open time %d."
                        .formatted(openTime, newOpenTime), exception.getMessage());
    }
}
