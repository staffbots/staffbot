package ru.staffbot.utils;

import java.util.HashMap;
import java.util.Map;

public enum DateScale {

    MILLISECOND(1, "Миллисекунда"),
    SECOND(1000, "Секунда"),
    MINUTE(1000 * 60, "Минута"),
    HOUR(1000 * 60 * 60, "Час"),
    DAY(1000 * 60 * 60 * 24, "Сутки"),
    WEEK(1000 * 60 * 60 * 24 * 7, "Неделя");

    private String description;
    private long milliseconds;
    private static Map valueMap = new HashMap<>();

    DateScale(long milliseconds, String description) {
        this.milliseconds = milliseconds;
        this.description = description;
    }


    public String getDescription(){
        return description;
    }

    static {
        for (DateScale dateScale : DateScale.values()) {
            valueMap.put(dateScale.milliseconds, dateScale);
        }
    }

    public static DateScale valueOf(long dateScaleValue) {
        return (DateScale) valueMap.get(dateScaleValue);
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public DateScale getNext(){
        switch (this){
            case MILLISECOND: return DateScale.SECOND;
            case SECOND : return DateScale.MINUTE;
            case MINUTE : return DateScale.HOUR;
            case HOUR : return DateScale.DAY;
            case DAY : return DateScale.WEEK;
            case WEEK : return DateScale.WEEK;
            default : return DateScale.DAY;
        }
    }
}
