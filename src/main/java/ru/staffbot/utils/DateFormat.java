package ru.staffbot.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum DateFormat {

    FULLDATETIME(0, "dd.MM.yyyy HH:mm:ss.SSS", DateScale.MILLISECOND),
    FULLTIMEDATE(1, "HH:mm:ss.SSS dd.MM.yyyy", DateScale.MILLISECOND),
    DATETIME(2, "dd.MM.yyyy HH:mm:ss", DateScale.SECOND),
    TIMEDATE(3, "HH:mm:ss dd.MM.yyyy", DateScale.SECOND),
    SHORTDATETIME(4, "dd.MM.yyyy HH:mm", DateScale.MINUTE),
    SHORTTIMEDATE(5, "HH:mm dd.MM.yyyy", DateScale.MINUTE),
    CUTSHORTDATETIME(4, "dd.MM HH:mm", DateScale.MINUTE),
    DATE(6, "dd.MM.yyyy", DateScale.DAY),
    TIME(7, "HH:mm:ss", DateScale.SECOND),
    SHORTTIME(8, "HH:mm", DateScale.MINUTE),
    FULLTIME(9, "HH:mm:ss.SSS", DateScale.SECOND);

    private String format;
    private int value;
    public DateScale accuracy; //Точность
    private static Map map = new HashMap<>();

    DateFormat(int value, String format, DateScale accuracy) {
        this.value = value;
        this.format = format;
        this.accuracy = accuracy;
    }

    static {
        for (DateFormat dateFormat : DateFormat.values()) {
            map.put(dateFormat.value, dateFormat);
        }
    }

    public static DateFormat valueOf(int dateFormat) {
        return (DateFormat) map.get(dateFormat);
    }

    public int getValue() {
        return value;
    }

    public String getFormat() {
        return format;
    }

    public String get() {
        return format;
    }



}