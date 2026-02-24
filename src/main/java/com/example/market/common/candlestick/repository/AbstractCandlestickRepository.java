package com.example.market.common.candlestick.repository;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.market.common.candlestick.repository.specification.CandlestickSpecifications;
import com.example.market.common.data.Market;
import com.example.market.common.data.MillisRange;
import com.example.market.common.data.Symbol;
import com.example.market.common.data.TimeInterval;
import com.example.market.common.service.TimeIntervalUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractCandlestickRepository<T extends Candlestick> {

    @PersistenceContext
    private EntityManager entityManager;

    private final String tableName;
    private final Class<T> candlestickClass;
    protected final JpaRepositoryImplementation<T, Long> repository;

    private final Map<TimeInterval, List<Candlestick>> candlesticksByTimeInterval =
            Arrays.stream(TimeInterval.values())
                    .collect(Collectors.toMap(
                            Function.identity(),
                            timeInterval -> new LinkedList<>(),
                            (a, b) -> a,
                            () -> new EnumMap<>(TimeInterval.class)));

    private final List<Candlestick> candlesticks = new ArrayList<>();

    // TODO: 2/21/2026 create algorithm for real month(or another) back shifting
    private final static long MONTH_TIME = 2592000000L;

    public AbstractCandlestickRepository(JpaRepositoryImplementation<T, Long> repository, Class<T> candlestickClass) {
        this.repository = repository;
        this.candlestickClass = candlestickClass;
        this.tableName = candlestickClass.getAnnotation(Table.class).name();
    }

    public T save(T candlestick) {
        checkSave(candlestick);

        T saved = repository.save(candlestick);
        candlesticks.add(saved);

        return saved;
    }

    private void checkSave(T candlestick) {
        if (candlesticks.size() > 0) {
            long lastOpenTime = candlesticks.get(candlesticks.size() - 1).openTime();

            if (candlestick.openTime() - lastOpenTime != TimeIntervalUtil.ONE_MINUTE) {
                throw new IllegalArgumentException("Incorrect open time range between candlesticks during save. " +
                        "Previous candlestick open time %d, new candlestick open time %d."
                        .formatted(lastOpenTime, candlestick.openTime()));
            }
        }

        if (candlestick.closeTime() - candlestick.openTime() != TimeIntervalUtil.ONE_MINUTE - 1) {
            throw new IllegalArgumentException("Incorrect open close candlestick time range during save. " +
                    "New candlestick open time %d, new candlestick close time %d"
                    .formatted(candlestick.openTime(), candlestick.closeTime()));
        }
    }

    public List<T> saveAll(List<T> candlesticks) {
        return repository.saveAll(candlesticks);
    }

    public List<T> inRange(long start, long end) {
        return repository.findAll(CandlestickSpecifications.inRange(start, end));
    }

    @SuppressWarnings("unchecked")
    public List<T> inRange(TimeInterval timeInterval, long start, long end, boolean wholeLastPart) {
        String baseTableName = "binance_xrp_usdt_candlestick";

        StringBuilder queryBuilder = new StringBuilder();
        List<MillisRange> milliRanges = TimeIntervalUtil.roundedAndPartTime(timeInterval, start, end, wholeLastPart);

        if (milliRanges.size() > 0) {
            for (MillisRange mr : milliRanges) {
                String query = "SELECT * " +
                        "FROM (SELECT (array_agg(open_price) OVER (ORDER BY open_time ROWS (" + mr.rows() + " - 1) PRECEDING))[1]  as open_price," +
                        "           max(high_price) OVER (ORDER BY open_time ROWS (" + mr.rows() + " - 1) PRECEDING) AS high_price," +
                        "           min(low_price) OVER (ORDER BY open_time ROWS (" + mr.rows() + " - 1) PRECEDING) AS low_price," +
                        "           (array_agg(open_time) OVER (ORDER BY open_time ROWS (" + mr.rows() + " - 1) PRECEDING))[1] as open_time," +
                        "           close_price, close_time, quote_asset_volume, taker_buy_base_asset_volume, taker_buy_quote_asset_volume," +
                        "           trades_count, volume, row_number() OVER (ORDER BY open_time) AS n " +
                        "       FROM " + baseTableName + " WHERE open_time > " + mr.startMillis() + " AND open_time <= " + mr.endMillis() + ") x " +
                        "WHERE n % " + mr.rows() + " = 0 " +
                        "UNION ";

                queryBuilder.append(query.replace(baseTableName, tableName));
            }
            queryBuilder.setLength(queryBuilder.length() - 6);
            queryBuilder.append("ORDER BY open_time");

            return entityManager.createNativeQuery(queryBuilder.toString(), candlestickClass).getResultList();
        }
        return new ArrayList<>(0);
    }

    public abstract Market market();

    public abstract Symbol symbol();

    public abstract T candlestickInstance();
}
