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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCandlestickRepository<T extends Candlestick> {

    @PersistenceContext
    private EntityManager entityManager;

    private final String tableName;
    private final Class<T> candlestickClass;
    protected final JpaRepositoryImplementation<T, Long> repository;

    public AbstractCandlestickRepository(JpaRepositoryImplementation<T, Long> repository, Class<T> candlestickClass) {
        this.repository = repository;
        this.candlestickClass = candlestickClass;
        this.tableName = candlestickClass.getAnnotation(Table.class).name();
    }

    public T save(T candlestick) {
        return repository.save(candlestick);
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
        List<MillisRange> milliRanges = TimeIntervalUtil.getRoundedAndPartTime(timeInterval, start, end, wholeLastPart);

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
}
