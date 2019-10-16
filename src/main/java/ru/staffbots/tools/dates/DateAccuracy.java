package ru.staffbots.tools.dates;

public enum DateAccuracy {

    MILLISECOND(1,                       "Миллисекунда"),
    SECOND     (1000,                    "Секунда"     ),
    MINUTE     (1000 * 60,               "Минута"      ),
    HOUR       (1000 * 60 * 60,          "Час"         ),
    DAY        (1000 * 60 * 60 * 24,     "Сутки"       ),
    WEEK       (1000 * 60 * 60 * 24 * 7, "Неделя"      );

    private String description;
    private long milliseconds;

    DateAccuracy(long milliseconds, String description) {
        this.milliseconds = milliseconds;
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

}
