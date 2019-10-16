package ru.staffbots.tools.dates;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum DateFormat {

    FULLDATETIME    ( 0, "dd.MM.yyyy HH:mm:ss.SSS", DateAccuracy.MILLISECOND),
    FULLTIMEDATE    ( 1, "HH:mm:ss.SSS dd.MM.yyyy", DateAccuracy.MILLISECOND),
    DATETIME        ( 2, "dd.MM.yyyy HH:mm:ss",     DateAccuracy.SECOND     ),
    TIMEDATE        ( 3, "HH:mm:ss dd.MM.yyyy",     DateAccuracy.SECOND     ),
    SHORTDATETIME   ( 4, "dd.MM.yyyy HH:mm",        DateAccuracy.MINUTE     ),
    SHORTTIMEDATE   ( 5, "HH:mm dd.MM.yyyy",        DateAccuracy.MINUTE     ),
    CUTSHORTDATETIME( 4, "dd.MM HH:mm",             DateAccuracy.MINUTE     ),
    DATE            ( 6, "dd.MM.yyyy",              DateAccuracy.DAY        ),
    TIME            ( 7, "HH:mm:ss",                DateAccuracy.SECOND     ),
    SHORTTIME       ( 8, "HH:mm",                   DateAccuracy.MINUTE     ),
    FULLTIME        ( 9, "HH:mm:ss.SSS",            DateAccuracy.SECOND     ),
    JSDATETIME      (10, "yyyy/MM/dd HH:mm:ss",     DateAccuracy.SECOND     );

    private String format;
    private int value;
    public DateAccuracy dateAccuracy; //Точность
    private static Map map = new HashMap<>();

    DateFormat(int value, String format, DateAccuracy dateAccuracy) {
        this.value = value;
        this.format = format;
        this.dateAccuracy = dateAccuracy;
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