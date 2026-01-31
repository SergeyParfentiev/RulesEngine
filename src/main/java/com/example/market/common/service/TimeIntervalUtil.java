package com.example.market.common.service;

import com.example.market.common.data.MillisRange;
import com.example.market.common.data.TimeInterval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeIntervalUtil {

    public static final int ONE_MINUTE = 60_000;

    public static List<MillisRange> getRoundedAndPartTime(TimeInterval timeInterval, long startMillis, long endMillis, boolean wholeLastPart) {
        List<MillisRange> milliRanges = new ArrayList<>();

        if (Calendar.DAY_OF_YEAR == timeInterval.calendarFiled()) {
            fillDayOfYear(milliRanges, startMillis, endMillis);
        } else {
            Calendar startCal = roundedCal(startMillis);
            Calendar endCal = roundedCal(endMillis);

//            Calendar endCal = Calendar.getInstance();
//            endCal.setTimeInMillis(endMillis);
            Calendar firstRightBorderCal = roundedRightCal(timeInterval, startMillis);

            //inside one time interval
            if (firstRightBorderCal.after(endCal) && !wholeLastPart) {
                long rows = rows(endCal, startCal) + 1;
                milliRanges.add(new MillisRange(startMillis(startCal), endCal.getTimeInMillis(), rows));
            } else {
                Calendar lastLeftBorderCal = roundedLeftCal(timeInterval, endMillis);

                //inside first and middle time intervals
//                if (lastBorderCal.getTimeInMillis() - firstBorderCal.getTimeInMillis() > ONE_MINUTE) {
                long firstBorderRows = rows(firstRightBorderCal, startCal);

                //first time interval
                if (firstBorderRows != 0) {
                    milliRanges.add(new MillisRange(startMillis(startCal), firstRightBorderCal.getTimeInMillis(), firstBorderRows));
                }

                //middle time interval
                if (firstRightBorderCal.before(lastLeftBorderCal)) {
                    long middleIntervalMillis = lastLeftBorderCal.getTimeInMillis() - firstRightBorderCal.getTimeInMillis();

                    if (!wholeLastPart || (middleIntervalMillis / ONE_MINUTE) % timeInterval.amount() == 0) {
                        milliRanges.add(new MillisRange(startMillis(firstRightBorderCal), lastLeftBorderCal.getTimeInMillis(), timeInterval.amount()));
                    }
                }
//                }

                //last time interval
                if (lastLeftBorderCal.before(endCal)) {
                    long lastPartRows = rows(endCal, lastLeftBorderCal) + 1;
                    Calendar lastRightBorderCal = roundedRightCal(timeInterval, endMillis);

                    if ((!wholeLastPart || lastRightBorderCal.getTimeInMillis() == endMillis - ONE_MINUTE) && lastPartRows != 0) {
                        milliRanges.add(new MillisRange(startMillis(lastLeftBorderCal), endCal.getTimeInMillis(), lastPartRows));
                    }
                }
            }
        }
        return milliRanges;
    }

    private static void fillDayOfYear(List<MillisRange> milliRanges, long startMillis, long endMillis) {
        Calendar startCal = roundedCal(startMillis);

        Calendar firstParCal = roundedCal(startMillis);
        firstParCal.set(Calendar.MINUTE, 0);
        firstParCal.set(Calendar.HOUR, 0);
        firstParCal.add(Calendar.DAY_OF_YEAR, 1);

        Calendar endCal = roundedCal(endMillis);

        if (firstParCal.getTimeInMillis() > endMillis) {
            addToMillisRange(milliRanges, startCal, endCal);
        } else {
            addToMillisRange(milliRanges, startCal, firstParCal);

            Calendar currentDayCal = firstParCal;
            Calendar nextDayCal;

            for (; (nextDayCal = getNextDayCal(currentDayCal)).before(endCal); currentDayCal = nextDayCal) {
                addToMillisRange(milliRanges, currentDayCal, nextDayCal);
            }

            if (!currentDayCal.equals(endCal)) {
                addToMillisRange(milliRanges, currentDayCal, endCal);
            }
        }
    }

    private static Calendar getNextDayCal(Calendar cal) {
        Calendar cloneCal = (Calendar) cal.clone();
        cloneCal.add(Calendar.DAY_OF_YEAR, 1);
        return cloneCal;
    }

    private static void addToMillisRange(List<MillisRange> milliRanges, Calendar startCal, Calendar endCal) {
        long lastPartRows = rows(endCal, startCal);

        if (lastPartRows != 0) {
            milliRanges.add(new MillisRange(startMillis(startCal), endCal.getTimeInMillis(), lastPartRows));
        }
    }

    private static long startMillis(Calendar calendar) {
        return calendar.getTimeInMillis() - ONE_MINUTE;
    }

    private static long rows(Calendar endCal, Calendar startCal) {
        return (endCal.getTimeInMillis() - startCal.getTimeInMillis()) / ONE_MINUTE;
    }

    @SuppressWarnings("all")
    private static Calendar roundedRightCal(TimeInterval timeInterval, long startMillis) {
        Calendar firstPartCal = roundedCal(startMillis);
        int startFieldAmount = timeInterval.amount() - (firstPartCal.get(timeInterval.calendarFiled()) % timeInterval.amount());

        if (startFieldAmount != timeInterval.amount()) {
            firstPartCal.add(timeInterval.calendarFiled(), startFieldAmount);
        }
        return firstPartCal;
    }

    @SuppressWarnings("all")
    private static Calendar roundedLeftCal(TimeInterval timeInterval, long endMillis) {
        Calendar firstPartCal = roundedCal(endMillis);
        int startFieldAmount = firstPartCal.get(timeInterval.calendarFiled()) % timeInterval.amount();
        firstPartCal.add(timeInterval.calendarFiled(), -startFieldAmount);
        return firstPartCal;
    }

    private static Calendar roundedCal(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
