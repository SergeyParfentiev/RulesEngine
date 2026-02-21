package com.example.market.common.service;

import com.example.market.common.data.TimeInterval;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

import static com.example.market.common.data.TimeInterval.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeIntervalUtilTest {

    private static final Object[] TIME_1m = {0, 1,
            new TimeInterval[]{ONE_MINUTE}};
    private static final Object[] TIME_6m = {0, 6,
            new TimeInterval[]{ONE_MINUTE}};
    private static final Object[] TIME_21m = {0, 21,
            new TimeInterval[]{ONE_MINUTE}};
    private static final Object[] TIME_5m = {0, 5,
            new TimeInterval[]{ONE_MINUTE, FIVE_MINUTES}};
    private static final Object[] TIME_25m = {0, 25,
            new TimeInterval[]{ONE_MINUTE, FIVE_MINUTES}};
    private static final Object[] TIME_45m = {0, 45,
            new TimeInterval[]{ONE_MINUTE, FIVE_MINUTES}};
    private static final Object[] TIME_30m = {0, 30,
            new TimeInterval[]{ONE_MINUTE, FIVE_MINUTES, THIRTY_MINUTES}};
    private static final Object[] TIME_10h_30m = {10, 30,
            new TimeInterval[]{ONE_MINUTE, FIVE_MINUTES, THIRTY_MINUTES}};
    private static final Object[] TIME_0m = {0, 0,
            new TimeInterval[]{ONE_MINUTE, FIVE_MINUTES, THIRTY_MINUTES, ONE_DAY}};

    @ParameterizedTest
    @MethodSource("testSuitableTimeIntervalsArguments")
    public void testSuitableTimeIntervals(int hours, int minutes, TimeInterval[] expectedTimeIntervals) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        calendar.set(Calendar.YEAR, 2026);
        calendar.set(Calendar.DAY_OF_YEAR, 51);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        List<TimeInterval> all = TimeIntervalUtil.findSuitableTimeIntervals(calendar.getTimeInMillis());

        assertEquals(expectedTimeIntervals.length, all.size());

        for (TimeInterval expectedTimeInterval : expectedTimeIntervals) {
            assertTrue(all.contains(expectedTimeInterval));
        }
    }

    private static Stream<Arguments> testSuitableTimeIntervalsArguments() {
        return Stream.of(
                Arguments.of(TIME_1m),
                Arguments.of(TIME_6m),
                Arguments.of(TIME_21m),
                Arguments.of(TIME_5m),
                Arguments.of(TIME_25m),
                Arguments.of(TIME_45m),
                Arguments.of(TIME_30m),
                Arguments.of(TIME_10h_30m),
                Arguments.of(TIME_0m)
        );
    }
}
