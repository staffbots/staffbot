package ru.staffbot.utils;

import java.util.HashMap;
import java.util.Map;

public enum DateScale {

    MILLISECOND(1, "Миллисекунда", "миллисекунд"),
    SECOND(1000, "Секунда", "секунд"),
    MINUTE(1000 * 60, "Минута", "минут"),
    HOUR(1000 * 60 * 60, "Час", "часов"),
    DAY(1000 * 60 * 60 * 24, "Сутки", "суток"),
    WEEK(1000 * 60 * 60 * 24 * 7, "Неделя", "недель");

    private String description;
    private String accusative;
    private int value;
    private static Map valueMap = new HashMap<>();
    private static Map accusativeMap = new HashMap<>();

    DateScale(int value, String description, String accusative) {
        this.value = value;
        this.description = description;
        this.accusative = accusative;
    }


    public String getDescription(){
        return description;
    }

    static {
        for (DateScale dateScale : DateScale.values()) {
            valueMap.put(dateScale.value, dateScale);
        }
        for (DateScale dateScale : DateScale.values()) {
            accusativeMap.put(dateScale.accusative, dateScale);
        }
    }

    public static DateScale valueOf(int dateScaleValue) {
        return (DateScale) valueMap.get(dateScaleValue);
    }

    public static DateScale valueByAccusative(String accusativeValue) {
        return (DateScale) accusativeMap.get(accusativeValue);
    }

    public int getValue() {
        return value;
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
