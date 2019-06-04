package ru.staffbot.database.journal;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum NoteType {

    // ! Изменения могут привести к сбоям на закладке "Журнал" (см. JournalServlet)
    CONFIRM(0, "Информация"),
    WRINING(1, "Предупреждение"),
    ERROR(2, "Ошибка");

    private String description;
    private int value;
    private static Map map = new HashMap<>();

    NoteType(int value, String description) {
        this.value = value;
        this.description = description;
    }


    public String getDescription(){
        return description;
    }

    static {
        for (NoteType pageType : NoteType.values()) {
            map.put(pageType.value, pageType);
        }
    }
    public static NoteType valueOf(int noteType) {
        return (NoteType) map.get(noteType);
    }
    public int getValue() {
        return value;
    }
}
