package com.datalinkx.common.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.SneakyThrows;

public final class DateUtils {

    private DateUtils() { }

    @SneakyThrows
    public static List<Date> getEveryDay(SimpleDateFormat sdf, String startDate, String endDate) {

        Date begin = sdf.parse(startDate);
        Date end = sdf.parse(endDate);
        return findDates(begin, end);
    }

    public static List<Date> findDates(Date dBegin, Date dEnd) {
        List lDate = new ArrayList();
        lDate.add(dBegin);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        while (dEnd.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }

    @SneakyThrows
    public static List<Date> getEveryHour(SimpleDateFormat sdf, String startDate, String endDate) {

        Date startTime = sdf.parse(startDate);
        Date endTime = sdf.parse(endDate);
        return findHours(startTime, endTime);
    }

    private static List<Date> findHours(Date startTime, Date endTime) {

        List<Date> lDate = new ArrayList<>();
        lDate.add(startTime);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(startTime);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(endTime);
        // 测试此日期是否在指定日期之后
        while (endTime.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.HOUR_OF_DAY, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }
}
