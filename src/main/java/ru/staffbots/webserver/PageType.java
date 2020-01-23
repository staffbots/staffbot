package ru.staffbots.webserver;

import ru.staffbots.tools.Translator;

import java.util.HashMap;
import java.util.Map;

public enum PageType {

    ENTRY   (0, -1, false),
    BASE    (0, -1, false),
    CONTROL (1, 1 , true ),
    STATUS  (2, 0 , true ),
    JOURNAL (3, 0 , true ),
    USERS   (4, 2 , true ),
    SYSTEM  (5, 2 , true ),
    ABOUT   (6, 0 , false);

    private int value;
    private int accessLevel;
    private boolean databaseDepend;

    private static Map map = new HashMap<>();

    PageType(int value, int accessLevel, boolean databaseDepend) {
        this.value = value;
        this.accessLevel = accessLevel;
        this.databaseDepend = databaseDepend;
    }

    public String getDescription(){
        return Translator.getValue(getName(), "page_hint");
    }

    public String getCaption(){
        return Translator.getValue(getName(), "page_title");
    }

    public boolean getDatabaseDepend(){
        return databaseDepend;
    }

    static {
        for (PageType pageType : PageType.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static PageType valueOf(int pageType) {
        return (PageType) map.get(pageType);
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public int getAccessLevel(){
        return accessLevel;
    }

}
