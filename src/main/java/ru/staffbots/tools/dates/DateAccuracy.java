package ru.staffbots.tools.dates;

import ru.staffbots.tools.languages.Languages;

public enum DateAccuracy {

    MILLISECOND(1),
    SECOND     (1000),
    MINUTE     (1000 * 60),
    HOUR       (1000 * 60 * 60),
    DAY        (1000 * 60 * 60 * 24),
    WEEK       (1000 * 60 * 60 * 24 * 7);

    private long milliseconds;

    DateAccuracy(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public String getDescription(String languageCode){
        return Languages.get(languageCode).getValue("dateaccuracy", getName());
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public String getName() {
        return name().toLowerCase();
    }

}
