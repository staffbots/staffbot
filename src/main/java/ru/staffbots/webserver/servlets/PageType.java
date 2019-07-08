package ru.staffbots.webserver.servlets;

import java.util.HashMap;
import java.util.Map;

public enum PageType {
    ENTRY   (0, "Выход",        -1, "Авторизация",            false),
    CONTROL (1, "Управление",   1,  "Управление параметрами", false),
    STATUS  (2, "Состояние",    2,  "Состояние системы",      false),
    JOURNAL (3, "Журнал",       2,  "Журнал событий",         true ),
    USERS   (4, "Пользователи", 0,  "Список пользователей",   true ),
    SYSTEM  (5, "Система",      0,  "Системные настройки",    false),
    ABOUT   (6, "Сведения",     2,  "Сведения о системе",     false);

    private int value;
    private String caption;
    private String description;
    private int accessLevel;
    private boolean databaseDepend;
    private static Map map = new HashMap<>();

    PageType(int value, String caption, int accessLevel, String description, boolean databaseDepend) {
        this.value = value;
        this.description = description;
        this.accessLevel = accessLevel;
        this.caption = caption;
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
