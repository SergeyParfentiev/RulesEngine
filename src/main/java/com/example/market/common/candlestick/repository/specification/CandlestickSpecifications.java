package com.example.market.common.candlestick.repository.specification;

import com.example.market.common.candlestick.model.Candlestick;
import org.springframework.data.jpa.domain.Specification;

public class CandlestickSpecifications {

    private CandlestickSpecifications() {
    }

    public static <T extends Candlestick> Specification<T> inRange(long start, long end) {
        return (root, query, cb) ->
                cb.and(
                        cb.greaterThanOrEqualTo(root.get("openTime"), start),
                        cb.lessThanOrEqualTo(root.get("closeTime"), end));
    }
}
