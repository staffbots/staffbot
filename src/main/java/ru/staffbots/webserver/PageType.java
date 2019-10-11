package ru.staffbots.webserver;

import java.util.HashMap;
import java.util.Map;

public enum PageType {

    ENTRY   (0, "Выход"       , -1, "Авторизация"           , false),
    CONTROL (1, "Управление"  , 1 , "Управление параметрами", true ),
    STATUS  (2, "Состояние"   , 0 , "Состояние системы"     , true ),
    JOURNAL (3, "Журнал"      , 0 , "Журнал событий"        , true ),
    USERS   (4, "Пользователи", 2 , "Список пользователей"  , true ),
    SYSTEM  (5, "Система"     , 2 , "Системные настройки"   , true ),
    ABOUT   (6, "Сведения"    , 0 , "Сведения о системе"    , false);

    private int value;
    private String caption;
    private int accessLevel;
    private String description;
    private boolean databaseDepend;

    private static Map map = new HashMap<>();

    PageType(int value, String caption, int accessLevel, String description, boolean databaseDepend) {
        this.value = value;
        this.caption = caption;
        this.accessLevel = accessLevel;
        this.description = description;
        this.databaseDepend = databaseDepend;
    }

    public String getDescription(){
        return description;
    }

    public boolean getDatabaseDepend(){
        return databaseDepend;
    }

    public String getCaption(){
        return caption;
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
