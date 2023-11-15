package com.datalinkx.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public final class DateUtil {
    private DateUtil() { }

    public static boolean isFormatted(String dateStr, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setLenient(false);
        try {
            if (dateFormat.parse(dateStr) != null) {
                return true;
            }
        } catch (ParseException e) {
            return false;
        }

        return false;
    }
}
