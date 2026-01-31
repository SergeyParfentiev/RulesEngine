package com.example.market.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Calendar;

public enum TimeInterval {

    ONE_MINUTE("1m") {
        @Override
        public int amount() {
            return 1;
        }

        @Override
        public int calendarFiled() {
            return Calendar.MINUTE;
        }

        @Override
        public boolean needOver() {
            return false;
        }
    },

    FIVE_MINUTES("5m") {
        @Override
        public int amount() {
            return 5;
        }

        @Override
        public int calendarFiled() {
            return Calendar.MINUTE;
        }
    },

    THIRTY_MINUTES("30m") {
        @Override
        public int amount() {
            return 30;
        }

        @Override
        public int calendarFiled() {
            return Calendar.MINUTE;
        }
    },

    ONE_DAY("1d") {
        @Override
        public int amount() {
            return 1440;
        }

        @Override
        public int calendarFiled() {
            return Calendar.DAY_OF_YEAR;
        }
    };

    private final String name;

    TimeInterval(String name) {
        this.name = name;
    }

    @JsonCreator
    public static TimeInterval byName(String name) {
        for (TimeInterval timeInterval : TimeInterval.values()) {
            if (timeInterval.name.equalsIgnoreCase(name)) {
                return timeInterval;
            }
        }
        throw new RuntimeException("There is no TimeInterval by name: " + name);
    }

    public static int maxAmount() {
        int max = 0;

        for (TimeInterval value : values()) {
            max = Math.max(max, value.amount());
        }
        return max;
    }

    public abstract int amount();

    public abstract int calendarFiled();

    public boolean needOver() {
        return true;
    }
}
