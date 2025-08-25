package com.citi.volume.util;

import com.citi.volume.data.BucketType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class ConstantUtils {

    public static SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");
    public static double EPILSON = 0.000001;

    public static boolean stringNotEmpty(String str) {
        return str != null && str.trim().length() != 0;
    }

    public static BucketType extractBucketType(String value) {
        BucketType retVal = null;
        if (stringNotEmpty(value)) {
            try {
                retVal = BucketType.valueOf(value.trim());
            } catch (IllegalArgumentException ignored) {

            }
        }
        return retVal;
    }

    public static Date extractDateTime(String value) {
        Date retVal = null;
        if (stringNotEmpty(value)) {
            try {
                retVal = DATE_TIME_FORMATTER.parse(value);
            } catch (ParseException e) {

            }

        }
        return retVal;
    }

    public static Date extractTime(String value) {
        Date retVal = null;
        if (stringNotEmpty(value)) {
            try {
                LocalDateTime dt = LocalDate.now().atTime(LocalTime.parse(value));
                retVal = new Date(dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                retVal = DATE_TIME_FORMATTER.parse(value);
            } catch (ParseException e) {

            }

        }
        return retVal;
    }

    public static String printTime(long value) {
        return TIME_FORMATTER.format(new Date(value));
    }

    public static double extractDouble(String value) {
        double retVal = -1;
        if (stringNotEmpty(value)) {
            try {
                retVal = Double.parseDouble(value.trim());
            } catch (NumberFormatException ignored) {
            }

        }
        return retVal;
    }

    public static boolean equalDouble(double value1, double value2) {
        return Math.abs(value1 - value2) < EPILSON;
    }
}
